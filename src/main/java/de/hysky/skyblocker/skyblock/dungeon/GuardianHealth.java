package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuardianHealth {
    private static final Box bossRoom = new Box(34, 65, -32, -32, 100, 36);
    private static final Pattern guardianRegex = Pattern.compile("^(.*?) Guardian (.*?)([A-Za-z])❤$");
    private static final Pattern professorRegex = Pattern.compile("^﴾ The Professor (.*?)([A-za-z])❤ ﴿$");
    private static boolean inBoss;

    @Init
    public static void init() {
        ClientReceiveMessageEvents.GAME.register(GuardianHealth::onChatMessage);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> GuardianHealth.reset());
        WorldRenderEvents.AFTER_ENTITIES.register(GuardianHealth::onWorldRender);
    }

    private static void onWorldRender(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().dungeons.theProfessor.floor3GuardianHealthDisplay) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (Utils.isInDungeons() && inBoss && client.player != null && client.world != null) {
            List<GuardianEntity> guardians =
                    client.world.getEntitiesByClass(
                            GuardianEntity.class, bossRoom, guardianEntity -> true);

            for (GuardianEntity guardian : guardians) {
                List<ArmorStandEntity> armorStands =
                        client.world.getEntitiesByType(
                                EntityType.ARMOR_STAND,
                                guardian.getBoundingBox().expand(0, 1, 0),
                                GuardianHealth::isGuardianName);

                for (ArmorStandEntity armorStand : armorStands) {
                    String display = armorStand.getDisplayName().getString();
                    boolean professor = display.contains("The Professor");
                    Matcher matcher =
                            professor
                                    ? professorRegex.matcher(display)
                                    : guardianRegex.matcher(display);
                    matcher.matches(); // name is validated in isGuardianName

                    String health = matcher.group(professor ? 1 : 2);
                    String quantity = matcher.group(professor ? 2 : 3);

                    double distance = context.camera().getPos().distanceTo(guardian.getPos());

                    RenderHelper.renderText(
                            context,
                            Text.literal(health + quantity).formatted(Formatting.GREEN),
                            guardian.getPos(),
                            (float) (1 + (distance / 10)),
                            true);
                }
            }
        }
    }

    private static void reset() {
        inBoss = false;
    }

    private static void onChatMessage(Text text, boolean overlay) {
        if (Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.theProfessor.floor3GuardianHealthDisplay && !inBoss) {
            String unformatted = Formatting.strip(text.getString());

            inBoss = unformatted.equals("[BOSS] The Professor: I was burdened with terrible news recently...");
        }
    }

    private static boolean isGuardianName(ArmorStandEntity entity) {
        String display = entity.getDisplayName().getString();

        if (display.contains("The Professor")) {
            return professorRegex.matcher(display).matches();
        }

        return !display.equals("Armor Stand") && guardianRegex.matcher(display).matches();
    }
}

