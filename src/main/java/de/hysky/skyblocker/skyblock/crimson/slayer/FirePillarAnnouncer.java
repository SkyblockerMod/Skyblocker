package de.hysky.skyblocker.skyblock.crimson.slayer;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.util.Formatting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirePillarAnnouncer {

    private static final Pattern FIRE_PILLAR_PATTERN = Pattern.compile("(\\d+)s \\d+ hits");

    /**
     *  Checks if an entity is the fire pillar when it has been updated (i.e. name change). This triggers twice on
     *  seven seconds remaining, so it's rounded down to announce the last 5 seconds until explosion.
     * <p>
     *  There's not a great way to detect ownership of the firepillar, so a crude range calculation is used to try and
     *  prevent other player's FirePillars appearing on the user's HUD.
     *
     * @param entity The updated entity that is checked to be a fire pillar
     */
    public static void checkFirePillar(Entity entity) {
        if (Utils.isInCrimsonIsle() && SlayerUtils.isInSlayer() && entity instanceof ArmorStandEntity) {

            String entityName = entity.getName().getString();
            Matcher matcher = FIRE_PILLAR_PATTERN.matcher(entityName);

            if (matcher.matches()) {
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

        if (SkyblockerConfigManager.get().slayers.blazeSlayer.FirePillarCountdown == SlayersConfig.BlazeSlayer.FirePillar.SOUND_AND_VISUAL) {
            RenderHelper.displayInTitleContainerAndPlaySound(title, 15);
        } else {
            TitleContainer.addTitle(title, 15);
        }
    }
}
