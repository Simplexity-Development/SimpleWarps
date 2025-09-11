package simplexity.simplewarps.warp;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public enum WarpAccessType {
    PUBLIC {
        @Override
        public boolean canAccess(Player player, AccessControl accessControl) {
            return permissionsAllowAccess(player, accessControl);
        }
    },
    PUBLIC_DENYLIST {
        @Override
        public boolean canAccess(Player player, AccessControl accessControl) {
            return permissionsAllowAccess(player, accessControl) ||
                   !accessControl.getBlockList().contains(player.getUniqueId());
        }
    },
    PRIVATE {
        @Override
        public boolean canAccess(Player player, AccessControl accessControl) {
            return accessControl.getOwner().equals(player.getUniqueId());
        }
    },
    PRIVATE_ALLOWLIST {
        @Override
        public boolean canAccess(Player player, AccessControl accessControl) {
            return permissionsAllowAccess(player, accessControl) ||
                   accessControl.getOwner().equals(player.getUniqueId()) ||
                   accessControl.getAllowList().contains(player.getUniqueId());
        }
    };

    public abstract boolean canAccess(Player player, AccessControl accessControl);

    public boolean permissionsAllowAccess(Player player, AccessControl accessControl) {
        if (accessControl.getAllowedPermissions().isEmpty() && accessControl.getBlockedPermissions().isEmpty())
            return true;
        for (Permission permission : accessControl.getBlockedPermissions()) {
            if (player.hasPermission(permission)) return false;
        }
        for (Permission permission : accessControl.getAllowedPermissions()) {
            if (player.hasPermission(permission)) return true;
        }
        return false;
    }
}
