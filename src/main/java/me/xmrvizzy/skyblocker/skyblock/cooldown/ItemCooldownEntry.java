package me.xmrvizzy.skyblocker.skyblock.cooldown;

public class ItemCooldownEntry {
    private final int cooldown;
    private final long startTime;

    public ItemCooldownEntry(int cooldown) {
        this.cooldown = cooldown;
        this.startTime = System.currentTimeMillis();
    }

    public boolean isOnCooldown() {
        return (this.startTime + this.cooldown) > System.currentTimeMillis();
    }

    public long getRemainingCooldown() {
        long time = (this.startTime + this.cooldown) - System.currentTimeMillis();
        return time <= 0 ? 0 : time;
    }

    public float getRemainingCooldownPercent() {
        return this.isOnCooldown() ? ((float) this.getRemainingCooldown()) / ((float) cooldown) : 0.0f;
    }
}
