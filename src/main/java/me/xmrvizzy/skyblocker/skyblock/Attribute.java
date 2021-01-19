package me.xmrvizzy.skyblocker.skyblock;

public enum Attribute {
    HEALTH(100),
    MAX_HEALTH(100),
    MANA(100),
    MAX_MANA(100),
    DEFENCE(0);

    private int value;
    Attribute(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }
    public void set(int value) {
        this.value = value;
    }
}