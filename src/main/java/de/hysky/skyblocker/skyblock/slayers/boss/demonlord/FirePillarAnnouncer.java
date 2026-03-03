package de.hysky.skyblocker.skyblock.slayers.boss.demonlord;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirePillarAnnouncer {
	private static final Pattern FIRE_PILLAR_PATTERN = Pattern.compile("(\\d+)s \\d+ hits");

	/**
	 *  Checks if an entity is the fire pillar when it has been updated (i.e. name change). This triggers twice on
	 *  seven seconds remaining, so it's been reduced down to only announce the last 5 seconds until explosion.
	 * <p>
	 *  There's also not a great way to detect ownership of the fire pillar, so a crude range calculation is used to try and
	 *  prevent another player's FirePillar appearing on the HUD.
	 *
	 * @param entity The updated entity that is checked to be a fire pillar
	 */
	public static void checkFirePillar(Entity entity) {
		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown == SlayersConfig.BlazeSlayer.FirePillar.OFF) return;
		if (Utils.isInCrimson() && SlayerManager.isFightingSlayerType(SlayerType.DEMONLORD)) {
			String entityName = entity.getName().getString();
			Matcher matcher = FIRE_PILLAR_PATTERN.matcher(entityName);

			if (matcher.matches()) {
				int seconds = Integer.parseInt(matcher.group(1));
				if (seconds > 5) return;

				// There is an edge case where the slayer has entered demon phase and temporarily despawned with
				//  an active fire pillar in play, So fallback to the player
				Entity referenceEntity = SlayerManager.getSlayerArmorStand();
				if (!(referenceEntity != null ? referenceEntity : Minecraft.getInstance().player).blockPosition().closerToCenterThan(entity.position(), 22)) return;
				announceFirePillarDetails(entityName);
			}
		}
	}

	private static void announceFirePillarDetails(String entityName) {
		Title title = new Title(Component.literal(entityName).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));

		if (SkyblockerConfigManager.get().slayers.blazeSlayer.firePillarCountdown == SlayersConfig.BlazeSlayer.FirePillar.SOUND_AND_VISUAL) {
			TitleContainer.addTitleAndPlaySound(title, 15);
		} else {
			TitleContainer.addTitle(title, 15);
		}
	}
}
