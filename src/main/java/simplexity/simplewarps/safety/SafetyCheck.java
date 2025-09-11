package simplexity.simplewarps.safety;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

public class SafetyCheck {

    public static int checkSafetyFlags(Location location, List<Material> blacklistedMaterials) {
        int flags = 0;
        Location blockAbove = location.clone().add(0, 1, 0);
        Location blockBelow = location.clone().add(0, -1, 0);
        // Fall check, is the player in the air? i.e. is the block below them empty/air?
        if (isEmpty(blockBelow) || blockBelow.getBlock().isPassable()) {
            flags |= SafetyFlags.FALLING.bitFlag;
        }
        // Is there lava?
        if (isMaterial(location, Material.LAVA) || isMaterial(blockAbove, Material.LAVA)) {
            flags |= SafetyFlags.LAVA.bitFlag;
        }
        // Is the home encased in blocks?
        if (blockAbove.getBlock().isSolid()) {
            flags |= SafetyFlags.SUFFOCATION.bitFlag;
        }
        // Is the home under water?
        if (isMaterial(blockAbove, Material.WATER)) {
            flags |= SafetyFlags.UNDERWATER.bitFlag;
        }
        // Is the home on a blacklisted block?
        if (blacklistedMaterials.contains(location.getBlock().getType())) {
            flags |= SafetyFlags.DAMAGE_RISK.bitFlag;
        }
        return flags;
    }

    private static boolean isMaterial(Location location, Material material) {
        return location.getBlock().getType() == material;
    }

    private static boolean isEmpty(Location location) {
        return location.getBlock().getType().isEmpty();
    }
}
