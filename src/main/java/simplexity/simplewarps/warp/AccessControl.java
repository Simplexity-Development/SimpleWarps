package simplexity.simplewarps.warp;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AccessControl {

    private final UUID owner;
    private final Set<UUID> allowList = new HashSet<>();
    private final Set<UUID> blockList = new HashSet<>();
    private final Set<Permission> allowedPermissions = new HashSet<>();
    private final Set<Permission> blockedPermissions = new HashSet<>();
    private WarpAccessType accessType;

    public AccessControl(UUID owner) {
        this(owner, Collections.emptySet(), Collections.emptySet(),
                Collections.emptySet(), Collections.emptySet(), WarpAccessType.PUBLIC);
    }

    public AccessControl(
            UUID owner,
            Collection<UUID> allowList,
            Collection<UUID> blockList,
            Collection<Permission> allowedPermissions,
            Collection<Permission> blockedPermissions,
            WarpAccessType accessType
    ) {
        this.owner = owner;
        this.allowList.addAll(allowList);
        this.blockList.addAll(blockList);
        this.allowedPermissions.addAll(allowedPermissions);
        this.blockedPermissions.addAll(blockedPermissions);
        this.accessType = accessType;
    }

    public UUID getOwner() {
        return owner;
    }

    public WarpAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(WarpAccessType type) {
        this.accessType = type;
    }

    public Set<UUID> getAllowList() {
        return Collections.unmodifiableSet(allowList);
    }

    public Set<UUID> getBlockList() {
        return Collections.unmodifiableSet(blockList);
    }

    public Set<Permission> getAllowedPermissions() {
        return Collections.unmodifiableSet(allowedPermissions);
    }

    public Set<Permission> getBlockedPermissions() {
        return Collections.unmodifiableSet(blockedPermissions);
    }

    public boolean addAllow(UUID uuid) {
        return allowList.add(uuid);
    }

    public boolean removeAllow(UUID uuid) {
        return allowList.remove(uuid);
    }

    public boolean addBlock(UUID uuid) {
        return blockList.add(uuid);
    }

    public boolean removeBlock(UUID uuid) {
        return blockList.remove(uuid);
    }

    public boolean addAllowedPermission(Permission permission) {
        return allowedPermissions.add(permission);
    }

    public boolean removeAllowedPermission(Permission permission) {
        return allowedPermissions.remove(permission);
    }

    public boolean addBlockedPermission(Permission permission) {
        return blockedPermissions.add(permission);
    }

    public boolean removeBlockedPermission(Permission permission) {
        return blockedPermissions.remove(permission);
    }

    public boolean canAccess(Player player) {
        return accessType.canAccess(player, this);
    }
}
