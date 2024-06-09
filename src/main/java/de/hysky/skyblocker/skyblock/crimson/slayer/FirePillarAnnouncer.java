package de.hysky.skyblocker.skyblock.crimson.slayer;

import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirePillarAnnouncer {

    private static final Pattern FIRE_PILLAR_PATTERN = Pattern.compile("(\\d+)s \\d+ hits");

    public static void CheckFirePillar(Entity entity) {
        if (Utils.isInCrimsonIsle() && SlayerUtils.isInSlayer() && entity instanceof ArmorStandEntity) {

            String entityName = entity.getName().getString();
            Matcher matcher = FIRE_PILLAR_PATTERN.matcher(entityName);

            if (matcher.matches()) {
                Entity slayerentity = SlayerUtils.getSlayerEntity();
                if (slayerentity == null || !(slayerentity.getBlockPos().isWithinDistance(entity.getPos(), 24))) return;
                AnnounceFirePillarDetails(entityName);
            }
        }
    }

    private static void AnnounceFirePillarDetails(String entityName) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.inGameHud.setTitleTicks(4, 10, 4);
        client.inGameHud.setTitle(Text.literal(""));
        client.inGameHud.setSubtitle(Text.literal(entityName)
                .formatted(Formatting.BOLD, Formatting.YELLOW));
    }
}
