package de.hysky.skyblocker.skyblock;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarType;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemAbility;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.SkyBlockIcons;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class StatusBarTracker {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern STATUS_PATTERN = Pattern.compile("(?<status>.+?)(?: {2,}|$)");
	private static final Pattern RIFT_TIME_STATUS = Pattern.compile(String.format("(?:[\\d,]+m)?[\\d,]+s[ф%s] Left", SkyBlockIcons.RIFT_TIME));
	private static final Pattern HEALTH_STATUS = Pattern.compile(String.format("(?<health>[\\d,]+)/(?<max>[\\d,]+)[❤%s](?<healing>\\+([\\d,]+)[▁-▆])?", SkyBlockIcons.HEALTH));
	private static final Pattern VITALITY_STATUS = Pattern.compile(String.format("(?<vitality>[\\d,]+)/(?<max>[\\d,]+)%s", SkyBlockIcons.VITALITY));
	private static final Pattern HEALING = Pattern.compile(String.format("(?:§[\\da-z])*[❤%s]", SkyBlockIcons.HEALTH));
	private static final Pattern DEFENSE_STATUS = Pattern.compile(String.format("(?<defense>[\\d,]+)[❈%s]( Defense)?", SkyBlockIcons.DEFENSE));
	private static final Pattern MANA_USE = Pattern.compile("-([\\d,]+) Mana \\(.*?\\)");
	private static final Pattern MANA_STATUS = Pattern.compile(String.format("(?<mana>[\\d,]+)/(?<max>[\\d,]+)[✎%s] ?(?:Mana|(?<overflow>[\\d,]+)[ʬ%s])?", SkyBlockIcons.MANA, SkyBlockIcons.OVERFLOW_MANA));

	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	/// Caches the last message to avoid parsing the same message multiple times.
	private static Component lastMessage = Component.empty();
	/// Caches the last return value of {@link #onOverlayMessage(Component, boolean)}.
	private static Component lastReturn = Component.empty();
	private static long lastMessageTime = 0;

	private static Resource health = new Resource(100, 100, 0);
	private static final EstimatedResource vitality = new EstimatedResource(new Resource(100, 100, 0));
	private static final EstimatedResource mana = new EstimatedResource(new Resource(100, 100, 0));
	private static Resource speed = new Resource(100, 400, 0);
	private static Resource air = new Resource(100, 300, 0);
	private static int defense = 0;
	private static int absorption = 0;

	private static int ticks;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(StatusBarTracker::allowOverlayMessage);
		ClientReceiveMessageEvents.MODIFY_GAME.register(StatusBarTracker::onOverlayMessage);
		UseItemCallback.EVENT.register(StatusBarTracker::interactItem);
		Scheduler.INSTANCE.scheduleCyclic(StatusBarTracker::tick, 1);
	}

	public static Resource getHealth() {
		return health;
	}

	public static EstimatedResource getVitality() {
		return vitality;
	}

	public static EstimatedResource getMana() {
		return mana;
	}

	public static int getDefense() {
		return defense;
	}

	public static Resource getSpeed() {
		return speed;
	}

	public static Resource getAir() {
		return air;
	}

	private static void tick() {
		if (MINECRAFT.player == null || !Utils.isOnSkyblock()) return;
		ticks++;
		updateHealth(health.value, health.max);
		updateSpeed();
		updateAir();
		mana.tick();
		vitality.tick();
	}

	@SuppressWarnings("SameReturnValue")
	private static InteractionResult interactItem(Player player, Level world, InteractionHand hand) {
		if (MINECRAFT.player == null) return InteractionResult.PASS;
		ItemStack handStack = MINECRAFT.player.getMainHandItem();
		int manaCost = 0;
		for (ItemAbility ability : handStack.skyblocker$getAbilities()) {
			if (ability.activation().isRightClick()) {
				manaCost = ability.manaCost().orElse(0);
				break;
			}
		}
		if (manaCost > 0 && manaCost <= mana.resource().value()) {
			mana.resource = new Resource(Math.max(mana.resource().value() - manaCost, 0), mana.resource().max(), mana.resource().overflow());
		}
		return InteractionResult.PASS;
	}

	private static boolean allowOverlayMessage(Component text, boolean overlay) {
		return !onOverlayMessage(text, overlay).getString().isEmpty();
	}

	private static Component onOverlayMessage(Component text, boolean overlay) {
		if (!overlay || !Utils.isOnSkyblock()) {
			return text;
		}

		long now = System.currentTimeMillis();
		if (lastMessage.equals(text) && lastMessageTime + 367 > now) { // Prime ms for a prime 7 ticks
			return lastReturn;
		}
		lastMessage = text;
		lastMessageTime = now;
		String stringified = text.getString();

		try {
			String returned = update(stringified, SkyblockerConfigManager.get().chat.hideMana);

			if (FancyStatusBars.isEnabled() && !stringified.equals(returned)) {
				return lastReturn = Component.literal(returned);
			}
		} catch (Exception e) {
			String stripped = ChatFormatting.stripFormatting(stringified);
			LOGGER.error("[Skyblocker Status Bar Tracker] Failed to update status bars! Content: '{}'", stripped, e);
		}

		return lastReturn = text;
	}

	@VisibleForTesting
	protected static String update(String actionBar, boolean filterManaUse) {
		Matcher statuses = STATUS_PATTERN.matcher(actionBar);
		var output = new StringBuilder();

		while (statuses.find()) {
			Matcher status;

			if (Utils.isInTheRift()) {
				status = RIFT_TIME_STATUS.matcher(ChatFormatting.stripFormatting(statuses.group("status")));

				// Rift time
				if (FancyStatusBars.isExperienceFancyBarEnabled() && status.find())
					statuses.appendReplacement(output, "");
			} else {
				status = HEALTH_STATUS.matcher(ChatFormatting.stripFormatting(statuses.group("status")));

				// Health
				if (status.find()) {
					updateHealth(status);

					if (FancyStatusBars.isHealthFancyBarEnabled()) {
						if (status.group("healing") == null) {
							statuses.appendReplacement(output, "");
						// Parse healing again to add back formatting
						} else {
							status = HEALING.matcher(statuses.group());
							if (!status.find()) continue;

							if (!status.group().startsWith("§"))
								output.append("§c");

							statuses.appendReplacement(output, statuses.group().substring(status.start()));
						}
					} else {
						statuses.appendReplacement(output, "$0");
					}
				// Defense
				} else if (status.usePattern(DEFENSE_STATUS).find()) {
					defense = RegexUtils.parseIntFromMatcher(status, "defense");

					if (FancyStatusBars.isHealthFancyBarEnabled())
						statuses.appendReplacement(output, "");
					else
						statuses.appendReplacement(output, "$0");
				// Vitality
				} else if (status.usePattern(VITALITY_STATUS).find()) {
					updateVitality(status);
					if (FancyStatusBars.isBarEnabled(StatusBarType.VITALITY))
						statuses.appendReplacement(output, "");
					else
						statuses.appendReplacement(output, "$0");
				}
			}
			// Mana use
			if (status.usePattern(MANA_USE).find()) {
				if (filterManaUse)
					statuses.appendReplacement(output, "");
			// Mana
			} else if (status.usePattern(MANA_STATUS).find()) {
				updateMana(status);

				if (FancyStatusBars.isBarEnabled(StatusBarType.INTELLIGENCE))
					statuses.appendReplacement(output, "");
				else
					statuses.appendReplacement(output, "$0");
			}
		}

		return statuses.appendTail(output).toString().trim();
	}

	private static void updateHealth(Matcher matcher) {
		int health = RegexUtils.parseIntFromMatcher(matcher, "health");
		int max = RegexUtils.parseIntFromMatcher(matcher, "max");

		if (Debug.isTestEnvironment() || MINECRAFT.player == null || MINECRAFT.player.getHealth() == MINECRAFT.player.getMaxHealth()) {
			// If at full HP or in test environment, then use simple absorption math.
			absorption = Math.max(0, health - max);
		} else {
			// Otherwise approximate absorption based on player health.
			absorption = (int) (health - (MINECRAFT.player.getHealth() * max / MINECRAFT.player.getMaxHealth()));
		}

		updateHealth(health, max);
	}

	private static void updateHealth(int value, int max) {
		// Client doesn't exist in test environment.
		if (!Debug.isTestEnvironment() && MINECRAFT.player != null) {
			value = (int) (MINECRAFT.player.getHealth() * max / MINECRAFT.player.getMaxHealth());
		}
		health = new Resource(Math.min(value, max), max, absorption);
	}

	private static void updateVitality(Matcher m) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.bars.hasSeenVitalityAtLeastOnce) {
			SkyblockerConfigManager.updateOnly(config -> config.uiAndVisuals.bars.hasSeenVitalityAtLeastOnce = true);
			FancyStatusBars.makeVitalityVisible();
		}
		vitality.update(new Resource(RegexUtils.parseIntFromMatcher(m, "vitality"), RegexUtils.parseIntFromMatcher(m, "max"), 0));
	}

	private static void updateMana(Matcher m) {
		int mana = RegexUtils.parseIntFromMatcher(m, "mana");
		int max = RegexUtils.parseIntFromMatcher(m, "max");
		int overflow = m.group("overflow") == null ? 0 : RegexUtils.parseIntFromMatcher(m, "overflow");
		StatusBarTracker.mana.update(new Resource(mana, max, overflow));
	}

	private static void updateSpeed() {
		// Black cat and racing helm are untested - I don't have the money to test atm, but no reason why they shouldn't work
		assert MINECRAFT.player != null;
		int value = (int) (MINECRAFT.player.isSprinting() ? (MINECRAFT.player.getSpeed() / 1.3f) * 1000 : MINECRAFT.player.getSpeed() * 1000);
		int max = 400; // hardcoded limit (except for with cactus knife, black cat, snail, racing helm, young drag)
		if (MINECRAFT.player.getMainHandItem().getHoverName().getString().contains("Cactus Knife") && Utils.getLocation() == Location.GARDEN) {
			max = 500;
		}
		Iterable<ItemStack> armor = ItemUtils.getArmor(MINECRAFT.player);
		int youngDragCount = 0;
		for (ItemStack armorPiece : armor) {
			if (armorPiece.getHoverName().getString().contains("Racing Helmet")) {
				max = 500;
			} else if (armorPiece.getHoverName().getString().contains("Young Dragon")) {
				youngDragCount++;
			}
		}
		if (youngDragCount == 4) {
			max = 500;
		}

		PetInfo pet = PetCache.getCurrentPet();
		if (pet != null) {
			if (pet.type().contains("BLACK_CAT")) {
				max = 500;
			} else if (pet.type().contains("SNAIL")) {
				max = 100;
			}
		}
		speed = new Resource(value, max, 0);
	}

	private static void updateAir() {
		assert MINECRAFT.player != null;
		int max = MINECRAFT.player.getMaxAirSupply();
		int value = Math.clamp(MINECRAFT.player.getAirSupply(), 0, max);
		air = new Resource(value, max, 0);
	}

	public record Resource(int value, int max, int overflow) {}

	public static class EstimatedResource {
		private Resource resource;
		private int perSecond;
		private int lastTick;
		private int lastValue;

		public EstimatedResource(Resource baseValue) {
			this.resource = baseValue;
		}

		private void update(Resource newValue) {
			this.resource = newValue;
			if (resource.value() != resource.max() && lastValue < resource.value()) perSecond = Math.max(resource.value() - lastValue, 0);
			if (lastValue != resource.value() || resource.value() == resource.max()) lastTick = ticks;
			lastValue = resource.value();
		}

		private void tick() {
			if (ticks - lastTick > 0 && (ticks - lastTick) % 20 == 0) {
				resource = new Resource(Math.min(resource.value() + perSecond, resource.max()), resource.max(), resource.overflow());
			}
		}

		public boolean isEstimated() {
			return ticks - lastTick > 30;
		}

		public Resource resource() {
			return resource;
		}

		public int value() {
			return resource.value();
		}
		public int max() {
			return resource.max();
		}
		public int overflow() {
			return resource.overflow();
		}
	}
}
