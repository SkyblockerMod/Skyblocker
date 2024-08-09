package de.hysky.skyblocker.skyblock.crimson.slayer;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttunementColours {
    private static final Pattern COLOUR_PATTERN = Pattern.compile("ASHEN|SPIRIT|CRYSTAL|AURIC");

    /**
     * Fetches highlight colour based on the Inferno Demonlord, or its demons', Hellion Shield Attunement
     */
    public static int getColour(LivingEntity e) {
        if (!SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights) return 0xf57738;
        for (Entity entity : SlayerUtils.getEntityArmorStands(e)) {
            Matcher matcher = COLOUR_PATTERN.matcher(entity.getDisplayName().getString());
            if (matcher.find()) {
                String matchedColour = matcher.group();
                return switch (matchedColour) {
                    case "ASHEN" -> Color.DARK_GRAY.getRGB();
                    case "SPIRIT" -> Color.WHITE.getRGB();
                    case "CRYSTAL" -> Color.CYAN.getRGB();
                    case "AURIC" -> Color.YELLOW.getRGB();
                    default -> Color.RED.getRGB();
                };
            }
        }
        return Color.RED.getRGB();
    }
}