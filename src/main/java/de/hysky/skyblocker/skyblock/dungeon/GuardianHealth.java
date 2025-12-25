package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuardianHealth {
	private static final AABB bossRoom = new AABB(34, 65, -32, -32, 100, 36);
	private static final Pattern guardianRegex = Pattern.compile("^(.*?) Guardian (.*?)([A-Za-z])❤$");
	private static final Pattern professorRegex = Pattern.compile("^﴾ (The Professor) (.*?)([A-za-z])❤ ﴿$");
	private static boolean inBoss;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(GuardianHealth::onChatMessage);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> GuardianHealth.reset());
		WorldRenderExtractionCallback.EVENT.register(GuardianHealth::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().dungeons.theProfessor.floor3GuardianHealthDisplay) return;

		Minecraft client = Minecraft.getInstance();

		if (Utils.isInDungeons() && inBoss && client.player != null && client.level != null) {
			List<Guardian> guardians =
					client.level.getEntitiesOfClass(
							Guardian.class, bossRoom, guardianEntity -> true);

			for (Guardian guardian : guardians) {
				List<ArmorStand> armorStands =
						client.level.getEntities(
								EntityType.ARMOR_STAND,
								guardian.getBoundingBox().inflate(0, 1, 0),
								GuardianHealth::isGuardianName);

				for (ArmorStand armorStand : armorStands) {
					if (armorStand.getDisplayName() == null) continue;
					String display = armorStand.getDisplayName().getString();
					boolean professor = display.contains("The Professor");
					Matcher matcher =
							professor
									? professorRegex.matcher(display)
									: guardianRegex.matcher(display);
					if (!matcher.matches()) continue;

					String health = matcher.group(2);
					String quantity = matcher.group(3);

					double distance = RenderHelper.getCamera().getPosition().distanceTo(guardian.getPosition(RenderHelper.getTickCounter().getGameTimeDeltaPartialTick(false)));

					collector.submitText(
							Component.literal(health + quantity).withStyle(ChatFormatting.GREEN),
							guardian.getPosition(RenderHelper.getTickCounter().getGameTimeDeltaPartialTick(false)),
							(float) (1 + (distance / 10)),
							true);
				}
			}
		}
	}

	private static void reset() {
		inBoss = false;
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (Utils.isInDungeons() && SkyblockerConfigManager.get().dungeons.theProfessor.floor3GuardianHealthDisplay && !inBoss) {
			String unformatted = ChatFormatting.stripFormatting(text.getString());

			inBoss = unformatted.equals("[BOSS] The Professor: I was burdened with terrible news recently...");
		}

		return true;
	}

	private static boolean isGuardianName(ArmorStand entity) {
		if (entity.getDisplayName() == null) return false;
		String display = entity.getDisplayName().getString();

		if (display.contains("The Professor")) {
			return professorRegex.matcher(display).matches();
		}

		return !display.equals("Armor Stand") && guardianRegex.matcher(display).matches();
	}
}
