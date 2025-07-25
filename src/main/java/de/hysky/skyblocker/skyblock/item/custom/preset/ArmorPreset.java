package de.hysky.skyblocker.skyblock.item.custom.preset;

import de.hysky.skyblocker.skyblock.item.custom.CustomArmorAnimatedDyes;
import de.hysky.skyblocker.skyblock.item.custom.CustomArmorTrims;

/**
 * Stores the styling data for a saved armor preset.
 */
public record ArmorPreset(String name, Piece helmet, Piece chestplate, Piece leggings, Piece boots) {

    /**
     * Styling data for a single armor piece. Only the fields relevant for the
     * custom armor system are stored.
     */
    public record Piece(Integer dye,
                        CustomArmorAnimatedDyes.AnimatedDye animation,
                        Trim trim,
                        String texture) {
        public record Trim(String material, String pattern) {}
    }
}
