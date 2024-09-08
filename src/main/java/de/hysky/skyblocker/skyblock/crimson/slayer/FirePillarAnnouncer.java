package de.hysky.skyblocker.skyblock.crimson.slayer;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.slayers.Slayer;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirePillarAnnouncer {

	private static final Pattern FIRE_PILLAR_PATTERN = Pattern.compile("(\\d+)s \\d+ hits");
	private static final int PROXIMITY_CHECK = 15;

	/**
	 * Checks if an entity is the fire pillar when it has been updated (i.e. name change). This triggers twice on
	 * seven seconds remaining, so it's been reduced down to only announce the last 5 seconds until explosion.
	 * <p>
	 * There's also not a great way to detect ownership of the fire pillar, so a crude range calculation is used to try and
	 * prevent another player's FirePillar appearing on the HUD.
	 *
	 * @param entity The updated entity that is checked to be a fire pillar
	 */
	public static void checkFirePillar(Entity entity) {
		Slayer slayer = Slayer.getInstance();
		if (Utils.isInCrimson() && slayer.isInSlayerFight() && entity instanceof ArmorStandEntity) {

			String entityName = entity.getName().getString();
			Matcher matcher = FIRE_PILLAR_PATTERN.matcher(entityName);

			if (matcher.matches()) {
				int seconds = Integer.parseInt(matcher.group(1));
				if (seconds > 5) return;

				// There is an edge case where the slayer has entered demon phase and temporarily despawned with
				// an active fire pillar in play, So fallback to the player
				float healthBound = (float) (slayer.getBossTier() < 4 ? 0.5 : 0.66);
				int distance = PROXIMITY_CHECK;
				Entity referenceEntity = slayer.getSlayerArmorStand();
				if (slayer.getSlayerArmorStand() == null) {
					referenceEntity = MinecraftClient.getInstance().player;
					distance += 5;
				}

				if (referenceEntity.getBlockPos().isWithinDistance(entity.getPos(), distance) && (float) (slayer.getCurrentHealth()) / slayer.getMaxHealth() <= healthBound) {
					announceFirePillarDetails(entityName);
				}
			}
		}
	}

	private static void announceFirePillarDetails(String entityName) {
		Title title = new Title(Text.literal(entityName).formatted(Formatting.BOLD, Formatting.DARK_PURPLE));

		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown == SlayersConfig.BlazeSlayer.FirePillar.SOUND_AND_VISUAL) {
			RenderHelper.displayInTitleContainerAndPlaySound(title, 15);
		} else {
			TitleContainer.addTitle(title, 15);
		}
	}
}
