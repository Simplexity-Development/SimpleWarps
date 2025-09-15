package simplexity.simplewarps.saving;

import simplexity.simplewarps.warp.Warp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final Map<UUID, Warp> byId = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<Warp>> byOwner = new ConcurrentHashMap<>();
    private static final Map<String, Warp> byName = new ConcurrentHashMap<>();

    public static void loadAllWarps(){
        List<Warp> warpList = SqlHandler.getInstance().getAllWarps();
        for (Warp warp : warpList) {
            addWarp(warp);
        }
    }
    public static void addWarp(Warp warp) {
        byId.put(warp.getWarpId(), warp);
        if (warp.getAccessControl().getOwner() != null) {
            byOwner.computeIfAbsent(warp.getAccessControl().getOwner(), k -> new HashSet<>()).add(warp);
        }
        byName.put(warp.getName().toLowerCase(), warp);
    }

    public static void removeWarp(UUID warpId) {
        Warp warp = byId.remove(warpId);
        if (warp != null) {
            if (warp.getAccessControl().getOwner() != null) {
                Set<Warp> set = byOwner.get(warp.getAccessControl().getOwner());
                if (set != null) set.remove(warp);
            }
            byName.remove(warp.getName());
        }
    }

    public static Warp getWarpById(UUID warpId) {
        return byId.get(warpId);
    }

    public static Warp getWarpByName(String name) {
        return byName.get(name.toLowerCase());
    }

    public static Set<Warp> getWarpByOwner(UUID owner) {
        return byOwner.getOrDefault(owner, Collections.emptySet());
    }

    public static Collection<Warp> getAllWarps() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public static void clearCache() {
        byId.clear();
        byOwner.clear();
        byName.clear();
    }
}
