package de.hysky.skyblocker.skyblock.crimson.slayer;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirePillarAnnouncer {

    private static final Pattern FIRE_PILLAR_PATTERN = Pattern.compile("(\\d+)s \\d+ hits");

    public static void checkFirePillar(Entity entity) {
        if (Utils.isInCrimsonIsle() && SlayerUtils.isInSlayer() && entity instanceof ArmorStandEntity) {

            String entityName = entity.getName().getString();
            Matcher matcher = FIRE_PILLAR_PATTERN.matcher(entityName);

            if (matcher.matches()) {
                // The detection method is whenever the entity is updated (i.e. name change) but this triggers twice on
                // seven seconds remaining, creating a duplicate title string. Instead, round to five to skip the issue
                // and only display the more critical numbers anyway that are closer to the explosion.
                int seconds = Integer.parseInt(matcher.group(1));
                if (seconds > 5) return;

                Entity slayerEntity = SlayerUtils.getSlayerEntity();
                if (slayerEntity == null || !(slayerEntity.getBlockPos().isWithinDistance(entity.getPos(), 24))) return;
                announceFirePillarDetails(entityName);
            }
        }
    }

    private static void announceFirePillarDetails(String entityName) {
        Title title = new Title(MutableText.of(new PlainTextContent.Literal(entityName)).formatted(Formatting.BOLD, Formatting.DARK_PURPLE));

        if (SkyblockerConfigManager.get().slayers.blazeSlayer.enableFirePillarCountdownSoundIndicator) {
            RenderHelper.displayInTitleContainerAndPlaySound(title, 15);
        } else {
            TitleContainer.addTitle(title, 15);
        }
    }
}
