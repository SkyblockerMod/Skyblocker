package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

public class DisciplineTestHelper {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    /**
     * Stores what sword is needed for the name of a zombie
     */
    private static final HashMap<String, String> SWORD_TO_NAME_LOOKUP = Util.make(new HashMap<>(), map -> {
        map.put("WOOD_SWORD", "Wood");
        map.put("IRON_SWORD", "Iron");
        map.put("GOLD_SWORD", "Gold");
        map.put("DIAMOND_SWORD", "Diamond");
    });

    /**
     * Stores a color related to the color of the sword: wood = brown, iron = silver, gold = gold, diamond = cyan
     */
    private static final Object2IntMap<String> SWORD_TO_COLOR_LOOKUP = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(Map.of(
            "WOOD_SWORD", 0xa52a2a,
            "IRON_SWORD", 0xc0c0c0,
            "GOLD_SWORD", 0xffd700,
            "DIAMOND_SWORD", 0x00ffff
    )));

    /**
     * Works out if a zombie should glow based on its name and the currently held item by the player
     *
     * @param name name of the zombie to see if it should glow
     * @return if the zombie should glow
     */
    protected static boolean shouldGlow(String name) {
        if (CLIENT == null || CLIENT.player == null) {
            return false;
        }
        String heldId = ItemTooltip.getInternalNameFromNBT(CLIENT.player.getMainHandStack(), true);
        if (SWORD_TO_NAME_LOOKUP.containsKey(heldId)) {

            return SWORD_TO_NAME_LOOKUP.get(heldId).equals(name);
        }
        return false;
    }

    /**
     * gets the color linked to the currently held sword for zombies to glow
     *
     * @return color linked to sword
     */
    protected static int getColor() {
        if (DojoManager.currentChallenge != DojoManager.DojoChallenges.DISCIPLINE || CLIENT == null || CLIENT.player == null) {
            return 0;
        }
        String heldId = ItemTooltip.getInternalNameFromNBT(CLIENT.player.getMainHandStack(), true);
        if (SWORD_TO_COLOR_LOOKUP.containsKey(heldId)) {
            return SWORD_TO_COLOR_LOOKUP.getInt(heldId);
        }
        return 0;
    }


}
