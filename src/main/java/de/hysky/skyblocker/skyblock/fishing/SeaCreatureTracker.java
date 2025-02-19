package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ChatEvents;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SeaCreatureTracker {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern DOUBLE_HOOK_PATTERN = Pattern.compile("Double Hook!(?: Woot woot!)?");

	private static final List<LiveSeaCreature> seaCreatures = new ArrayList<>();
	private static SeaCreature lastCatch;
	private static boolean doubleHook = false;


	@Init
	public static void init() {
		ChatEvents.RECEIVE_STRING.register(SeaCreatureTracker::onChatMessage);
		ClientEntityEvents.ENTITY_UNLOAD.register(SeaCreatureTracker::onEntityDespawn);
	}

	private static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
		seaCreatures.removeIf(liveSeaCreature -> liveSeaCreature.entity.equals(entity));
	}

	public static void onEntitySpawn(ArmorStandEntity armorStand) {
		if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible() || lastCatch == null) {
			return;
		}
		if (armorStand.getName().getString().contains(lastCatch.name)) {
			seaCreatures.add(new LiveSeaCreature(lastCatch, armorStand, System.currentTimeMillis()));
			//either stop double hook or clear last catch
			if (doubleHook) {
				doubleHook = false;
			} else {
				lastCatch = null;
			}
			checkCapNotification();
			checkRarityNotification();
			//schedule notification for end of timer
			Scheduler.INSTANCE.schedule(SeaCreatureTracker::checkTimerNotification,SkyblockerConfigManager.get().helpers.fishing.timerLength * 20);
		}
	}

	/**
	 * Sees if a notification should be sent out about the timer running out
	 */
	private static void checkTimerNotification() {
		if (!SkyblockerConfigManager.get().helpers.fishing.seaCreatureTimerNotification || !isCreaturesAlive()) return;
		//if the timer is about to finish show notification
		if (Math.abs(SkyblockerConfigManager.get().helpers.fishing.timerLength * 1000L - getOldestSeaCreatureAge()) < 100){
			TitleContainer.addTitle(new Title(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureTimerNotification.notification").formatted(Formatting.RED)), 60);
			if (CLIENT.player == null) return;
			CLIENT.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 100f, 0.1f);
		}
	}

	/**
	 * Sends notification and sound when a sea creature is above a rarity threshold
	 */
	private static void checkRarityNotification() {
		SkyblockItemRarity rarityThreshold = SkyblockerConfigManager.get().helpers.fishing.minimumNotificationRarity;
		if (rarityThreshold == SkyblockItemRarity.UNKNOWN) return;
		SkyblockItemRarity lastCreatureRarity = seaCreatures.getLast().seaCreature.rarity;
		if (lastCreatureRarity.compareTo(rarityThreshold) >= 0) {
			TitleContainer.addTitle(new Title(Text.translatable("skyblocker.config.helpers.fishing.minimumNotificationRarity.notification", lastCreatureRarity).formatted(lastCreatureRarity.formatting)), 60);
			if (CLIENT.player == null) return;
			CLIENT.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 100f, 0.1f);
		}
	}

	/**
	 * Send notification and sound when see creature cap is reached
	 */
	private static void checkCapNotification() {
		if (!SkyblockerConfigManager.get().helpers.fishing.seaCreatureCapNotification) return;
		if (seaCreatureCount() == getSeaCreatureCap()) {
			TitleContainer.addTitle(new Title(Text.translatable("skyblocker.config.helpers.fishing.seaCreatureCapNotification.notification").formatted(Formatting.RED)), 60);
			if (CLIENT.player == null) return;
			CLIENT.player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 100f, 0.1f);
		}
	}

	/**
	 * Look for a message that is sent when a sea creature is fished up
	 *
	 * @param s Message to check
	 */
	private static void onChatMessage(String s) {
		if (!SkyblockerConfigManager.get().helpers.fishing.fishingHudEnabledLocations.contains(Utils.getLocation())) {
			return;
		}
		String message = Formatting.strip(s);
		//see if it's a double hook
		if (DOUBLE_HOOK_PATTERN.matcher(message).find()) {
			doubleHook = true;
			return;
		}
		//see if message matches any creature
		for (SeaCreature creature : SeaCreature.values()) {
			if (creature.chatMessage.equals(message)) {
				//found a sea creature add it to current creatures
				lastCatch = creature;
				break;
			}
		}
	}

	/**
	 * Finds the age in millis of the oldest sea creature
	 *
	 * @return Oldest sea creature age
	 */
	protected static long getOldestSeaCreatureAge() {
		return System.currentTimeMillis() - seaCreatures.getFirst().spawnTime;
	}

	/**
	 * Checks if at least one creature is alive
	 * @return if atleast one creature is alive
	 */
	protected static Boolean isCreaturesAlive() {
		return !seaCreatures.isEmpty();
	}


	protected static Pair<String, Float> getTimerText(long currentTime) {
		long maxTime = SkyblockerConfigManager.get().helpers.fishing.timerLength * 1000L;
		String time = SkyblockTime.formatTime((maxTime - currentTime) / 1000f).getString();
		//get how far through the timer is
		float percentage = (float) (maxTime - currentTime) / (float) maxTime;

		return Pair.of(time, percentage);

	}

	protected static int seaCreatureCount() {
		return seaCreatures.size();
	}

	/**
	 * Finds max sea creatures based on current location
	 *
	 * @return current sea creature cap
	 */
	protected static int getSeaCreatureCap() {
		return switch (Utils.getLocation()) {
			case CRYSTAL_HOLLOWS -> 20;
			case CRIMSON_ISLE -> 5;
			default -> SkyblockerConfigManager.get().helpers.fishing.seaCreatureCap;
		};
	}


	record LiveSeaCreature(SeaCreature seaCreature, Entity entity, Long spawnTime) {}


}

