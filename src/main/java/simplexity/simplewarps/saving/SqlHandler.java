package simplexity.simplewarps.saving;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import simplexity.simplewarps.SimpleWarps;
import simplexity.simplewarps.config.ConfigHandler;
import simplexity.simplewarps.warp.AccessControl;
import simplexity.simplewarps.warp.Warp;
import simplexity.simplewarps.warp.WarpAccessType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class SqlHandler {

    private final Logger logger = SimpleWarps.getInstance().getSLF4JLogger();
    private static final HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource dataSource;
    private static SqlHandler instance;

    private SqlHandler() {
    }

    public static SqlHandler getInstance() {
        if (instance == null) instance = new SqlHandler();
        return instance;
    }


    /**
     * Initializes all required tables if they do not exist.
     */
    public void initialize() throws SQLException {
        setupConfig();
        try (Statement statement = getConnection().createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS warps (
                        warp_id CHAR(36) PRIMARY KEY,
                        owner_id CHAR(36),
                        name VARCHAR(64) NOT NULL,
                        world_id VARCHAR(36) NOT NULL,
                        x DOUBLE NOT NULL,
                        y DOUBLE NOT NULL,
                        z DOUBLE NOT NULL,
                        yaw FLOAT NOT NULL,
                        pitch FLOAT NOT NULL,
                        description TEXT,
                        access_type VARCHAR(32) NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS warp_allow_list (
                        warp_id CHAR(36),
                        player_id CHAR(36),
                        PRIMARY KEY (warp_id, player_id),
                        FOREIGN KEY (warp_id) REFERENCES warps(warp_id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS warp_block_list (
                        warp_id CHAR(36),
                        player_id CHAR(36),
                        PRIMARY KEY (warp_id, player_id),
                        FOREIGN KEY (warp_id) REFERENCES warps(warp_id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS warp_allowed_permissions (
                        warp_id CHAR(36),
                        permission VARCHAR(128),
                        PRIMARY KEY (warp_id, permission),
                        FOREIGN KEY (warp_id) REFERENCES warps(warp_id) ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS warp_blocked_permissions (
                        warp_id CHAR(36),
                        permission VARCHAR(128),
                        PRIMARY KEY (warp_id, permission),
                        FOREIGN KEY (warp_id) REFERENCES warps(warp_id) ON DELETE CASCADE
                    )
                    """);
        }

        debug("Database initialized successfully.");
    }

    // ----------------- CRUD -----------------

    public void saveWarp(Warp warp) {
        String saveQuery;
        if (ConfigHandler.getInstance().isMySqlEnabled()) {
            saveQuery = """
                    INSERT INTO warps (warp_id, owner_id, name, world_id, x, y, z, yaw, pitch, description, access_type)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        owner_id=VALUES(owner_id),
                        name=VALUES(name),
                        world_id=VALUES(world),
                        x=VALUES(x),
                        y=VALUES(y),
                        z=VALUES(z),
                        yaw=VALUES(yaw),
                        pitch=VALUES(pitch),
                        description=VALUES(description),
                        access_type=VALUES(access_type)
                    """;
        } else {
            saveQuery = """
                        INSERT INTO warps (name, world_id, x, y, z, yaw, pitch, owner)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT(name) DO UPDATE SET
                          world_id=excluded.world_id,
                          x=excluded.x,
                          y=excluded.y,
                          z=excluded.z,
                          yaw=excluded.yaw,
                          pitch=excluded.pitch,
                          owner=excluded.owner
                    """;
        }

        try (PreparedStatement statement = getConnection().prepareStatement(saveQuery)) {
            bindWarp(statement, warp);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Failed to save warp '{}' with id: {}, reason: {}", warp.getName(), warp.getWarpId(), e.getMessage(), e);
        }

        clearAccessControl(warp.getWarpId());
        insertAccessControl(warp);

        debug("Saved warp '" + warp.getName() + "' (" + warp.getWarpId() + ")");
    }

    public Warp getWarp(UUID warpId) {
        String sql = "SELECT * FROM warps WHERE warp_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, warpId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readWarp(rs);
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to find warp with id: {}, reason: {}", warpId, e.getMessage(), e);
        }
        return null;
    }


    public List<Warp> getWarpsByOwner(UUID owner) {
        List<Warp> result = new ArrayList<>();
        String sql = "SELECT * FROM warps WHERE owner_id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, owner.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(readWarp(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to find warps by user with uuid: {}, reason: {}", owner, e.getMessage(), e);
        }
        return result;
    }

    public List<Warp> searchWarpsByDescription(String keyword) {
        List<Warp> result = new ArrayList<>();
        String sql = "SELECT * FROM warps WHERE description LIKE ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, "%" + keyword + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(readWarp(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to get warps with description containing the word: {}, reason: {}", keyword, e.getMessage(), e);
        }
        return result;
    }

    public void deleteWarp(UUID warpId) {
        String deleteQuery = "DELETE FROM warps WHERE warp_id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(deleteQuery)) {
            statement.setString(1, warpId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.warn("Failed to delete warp with id: {}, reason: {}", warpId, e.getMessage(), e);
        }
        debug("Deleted warp (%s)", warpId);
    }

    // ----------------- Helpers -----------------

    private void bindWarp(PreparedStatement statement, Warp warp) {
        try {
            Location location = warp.getLocation();
            statement.setString(1, warp.getWarpId().toString());
            statement.setString(2, warp.getAccessControl().getOwner() != null ? warp.getAccessControl().getOwner().toString() : null);
            statement.setString(3, warp.getName());
            statement.setString(4, location.getWorld().getUID().toString());
            statement.setDouble(5, location.getX());
            statement.setDouble(6, location.getY());
            statement.setDouble(7, location.getZ());
            statement.setFloat(8, location.getYaw());
            statement.setFloat(9, location.getPitch());
            statement.setString(10, warp.getDescription());
            statement.setString(11, warp.getAccessControl().getAccessType().name());
        } catch (SQLException e) {
            logger.warn("Failed to deserialize location for warp with id: {}, reason: {}", warp.getWarpId(), e.getMessage(), e);
        }
    }

    private void clearAccessControl(UUID warpId) {
        for (String table : List.of("warp_allow_list", "warp_block_list", "warp_allowed_permissions", "warp_blocked_permissions")) {
            try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM " + table + " WHERE warp_id = ?")) {
                statement.setString(1, warpId.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to clear access control lists for warp with id: {}, reason: {}", warpId, e.getMessage(), e);
            }
        }
    }

    private void insertAccessControl(Warp warp) {
        UUID warpId = warp.getWarpId();
        AccessControl accessControl = warp.getAccessControl();

        for (UUID uuid : accessControl.getAllowList()) {
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO warp_allow_list VALUES (?, ?)")) {
                statement.setString(1, warpId.toString());
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to set allowed users for warp with id: {}, reason: {}", warpId, e.getMessage(), e);
            }
        }
        for (UUID uuid : accessControl.getBlockList()) {
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO warp_block_list VALUES (?, ?)")) {
                statement.setString(1, warpId.toString());
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to set blocked users for warp with id: {}, reason: {}", warpId, e.getMessage(), e);
            }
        }
        for (Permission perm : accessControl.getAllowedPermissions()) {
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO warp_allowed_permissions VALUES (?, ?)")) {
                statement.setString(1, warpId.toString());
                statement.setString(2, perm.getName());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to set allowed permissions for warp with id: {}, reason: {}", warpId, e.getMessage(), e);
            }
        }
        for (Permission perm : accessControl.getBlockedPermissions()) {
            try (PreparedStatement statement = getConnection().prepareStatement("INSERT INTO warp_blocked_permissions VALUES (?, ?)")) {
                statement.setString(1, warpId.toString());
                statement.setString(2, perm.getName());
                statement.executeUpdate();
            } catch (SQLException e) {
                logger.warn("Failed to set blocked permissions for warp with id: {}, reason: {}", warpId, e.getMessage(), e);
            }
        }
    }

    private Warp readWarp(@NotNull ResultSet resultSet) {
        try {
            UUID warpId = UUID.fromString(resultSet.getString("warp_id"));
            UUID ownerId = resultSet.getString("owner_id") != null ? UUID.fromString(resultSet.getString("owner_id")) : null;
            String name = resultSet.getString("name");
            UUID world = UUID.fromString(resultSet.getString("world_id"));
            Location loc = new Location(Bukkit.getWorld(world),
                    resultSet.getDouble("x"),
                    resultSet.getDouble("y"),
                    resultSet.getDouble("z"),
                    resultSet.getFloat("yaw"),
                    resultSet.getFloat("pitch"));
            String desc = resultSet.getString("description");
            WarpAccessType type = WarpAccessType.valueOf(resultSet.getString("access_type"));
            AccessControl ac = loadAccessControl(warpId, ownerId, type);
            return new Warp(warpId, name, loc, desc, ac);
        } catch (SQLException e) {
            logger.warn("Failed to read warp, reason: {}", e.getMessage(), e);
        }
        return null;

    }

    public List<Warp> getAllWarps() {
        List<Warp> warps = new ArrayList<>();
        String warpQuery = "SELECT * FROM warps";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(warpQuery);
             ResultSet resultSet = statement.executeQuery()) {
            Map<UUID, Set<UUID>> allowMap = loadWarpUuidMap(connection, "warp_allow_list");
            Map<UUID, Set<UUID>> blockMap = loadWarpUuidMap(connection, "warp_block_list");
            Map<UUID, Set<Permission>> allowedPermsMap = loadWarpPermissionMap(connection, "warp_allowed_permissions");
            Map<UUID, Set<Permission>> blockedPermsMap = loadWarpPermissionMap(connection, "warp_blocked_permissions");
            while (resultSet.next()) {
                String warpName = resultSet.getString("name");
                UUID worldId = UUID.fromString(resultSet.getString("world_id"));
                UUID warpId = UUID.fromString(resultSet.getString("warp_id"));
                double x = resultSet.getDouble("x");
                double y = resultSet.getDouble("y");
                double z = resultSet.getDouble("z");
                float yaw = resultSet.getFloat("yaw");
                float pitch = resultSet.getFloat("pitch");

                UUID owner = resultSet.getString("owner") != null ? UUID.fromString(resultSet.getString("owner")) : null;
                WarpAccessType accessType = WarpAccessType.valueOf(resultSet.getString("type").toUpperCase());

                Location location = new Location(Bukkit.getWorld(worldId), x, y, z, yaw, pitch);
                String warpDescription = resultSet.getString("description");

                AccessControl accessControl = new AccessControl(
                        owner,
                        allowMap.getOrDefault(warpId, Set.of()),
                        blockMap.getOrDefault(warpId, Set.of()),
                        allowedPermsMap.getOrDefault(warpId, Set.of()),
                        blockedPermsMap.getOrDefault(warpId, Set.of()),
                        accessType
                );

                Warp warp = new Warp(warpId, warpName, location, warpDescription, accessControl);
                warps.add(warp);
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch warps from database, Error: {}", e.getMessage(), e);
        }
        return warps;
    }


    private AccessControl loadAccessControl(UUID warpId, UUID owner, WarpAccessType type) {
        Set<UUID> allow = new HashSet<>();
        Set<UUID> block = new HashSet<>();
        Set<org.bukkit.permissions.Permission> allowedPerms = new HashSet<>();
        Set<org.bukkit.permissions.Permission> blockedPerms = new HashSet<>();

        loadUuidSet("warp_allow_list", warpId, allow);
        loadUuidSet("warp_block_list", warpId, block);
        loadPermissionSet("warp_allowed_permissions", warpId, allowedPerms);
        loadPermissionSet("warp_blocked_permissions", warpId, blockedPerms);

        return new AccessControl(owner, allow, block, allowedPerms, blockedPerms, type);
    }


    private Map<UUID, Set<UUID>> loadWarpUuidMap(Connection connection, String table) throws SQLException {
        Map<UUID, Set<UUID>> map = new HashMap<>();
        String query = "SELECT warp_id, player_id FROM " + table;
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID warpId = UUID.fromString(rs.getString("warp_id"));
                UUID playerId = UUID.fromString(rs.getString("player_id"));
                map.computeIfAbsent(warpId, k -> new HashSet<>()).add(playerId);
            }
        }
        return map;
    }

    private Map<UUID, Set<Permission>> loadWarpPermissionMap(Connection connection, String table) throws SQLException {
        Map<UUID, Set<Permission>> map = new HashMap<>();
        String query = "SELECT warp_id, permission FROM " + table;
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID warpId = UUID.fromString(rs.getString("warp_id"));
                Permission permission = new Permission(rs.getString("permission"));
                map.computeIfAbsent(warpId, k -> new HashSet<>()).add(permission);
            }
        }
        return map;
    }

    private void loadUuidSet(String table, UUID warpId, Set<UUID> target) {
        try (PreparedStatement statement = getConnection().prepareStatement("SELECT player_id FROM " + table + " WHERE warp_id = ?")) {
            statement.setString(1, warpId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    target.add(UUID.fromString(resultSet.getString("player_id")));
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to load {} table for warp with id: {}, reason: {}", table, warpId, e.getMessage(), e);

        }
    }

    private void loadPermissionSet(String table, UUID warpId, Set<Permission> target) {
        try (PreparedStatement ps = getConnection().prepareStatement("SELECT permission FROM " + table + " WHERE warp_id = ?")) {
            ps.setString(1, warpId.toString());
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    target.add(new org.bukkit.permissions.Permission(resultSet.getString("permission")));
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to load permissions for warp with id: {}, reason: {}", warpId, e.getMessage(), e);

        }
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeDatabase() {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }

    private void setupConfig() {
        if (ConfigHandler.getInstance().isMySqlEnabled()) {
            hikariConfig.setJdbcUrl("jdbc:mysql://" + ConfigHandler.getInstance().getMySqlIp() + "/" + ConfigHandler.getInstance().getMySqlName());
            hikariConfig.setUsername(ConfigHandler.getInstance().getMySqlUsername());
            hikariConfig.setPassword(ConfigHandler.getInstance().getMySqlPassword());
            hikariConfig.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(hikariConfig);
            debug("Initialized MySQL connection to '{}'", hikariConfig.getJdbcUrl());
        }
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + SimpleWarps.getInstance().getDataFolder() + "/simplewarps.db?foreign_keys=on");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTestQuery("PRAGMA journal_mode = WAL;");
        dataSource = new HikariDataSource(hikariConfig);
        debug("Initialized SQLite connection");
    }

    private void debug(String message, Object... args) {
        logger.info("[SQL DEBUG] {}, {}", message, args);
    }
}
