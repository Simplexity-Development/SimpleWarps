package simplexity.simplewarps.warp;

import org.bukkit.Location;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class Warp {
    private final UUID warpId;
    private String name;
    private Location location;
    private String description;
    private final AccessControl accessControl;

    public Warp(@NotNull UUID warpId, @NotNull String name, @NotNull Location location, @Nullable String description, @NotNull AccessControl accessControl) {
        this.warpId = warpId;
        this.name = name;
        this.location = location;
        this.description = description;
        this.accessControl = accessControl;
    }

    public Warp(@NotNull String name, @NotNull Location location, @Nullable String description, @NotNull UUID owner, @NotNull Set<UUID> allowList,
                @NotNull Set<UUID> blockList, @NotNull Set<Permission> allowedPermissions,
                @NotNull Set<Permission> blockedPermissions, @NotNull WarpAccessType accessType) {
        this.warpId = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.description = description;
        this.accessControl = new AccessControl(owner, allowList, blockList, allowedPermissions,
                blockedPermissions, accessType);
    }


    public UUID getWarpId() {
        return warpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AccessControl getAccessControl() {
        return accessControl;
    }

}
