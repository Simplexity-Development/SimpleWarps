package simplexity.simplewarps.safety;

public enum SafetyFlags {
    DAMAGE_RISK(1),
    FALLING(2),
    FIRE(4),
    LAVA(8),
    NOT_SOLID(16),
    SUFFOCATION(32),
    UNDERWATER(64),
    UNSTABLE(128);

    final int bitFlag;

    SafetyFlags(int bitFlag) {
        this.bitFlag = bitFlag;
    }

    public boolean matches(int bitFlags) {
        int result = bitFlag & bitFlags;
        return result == bitFlag;
    }
}
