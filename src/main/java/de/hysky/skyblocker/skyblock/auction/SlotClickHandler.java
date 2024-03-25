package de.hysky.skyblocker.skyblock.auction;

@FunctionalInterface
public interface SlotClickHandler {

    void click(int slot, int button);

    default void click(int slot) {
        click(slot, 0);
    }
}
