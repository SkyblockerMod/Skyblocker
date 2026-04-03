package de.hysky.skyblocker.skyblock;

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
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
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
import org.jspecify.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusBarTracker {
	private static final Pattern STATUS_PATTERN = Pattern.compile("(?<status>.+?)(?: {2,}|$)");
	private static final Pattern RIFT_TIME_STATUS = Pattern.compile("(?:[\\d,]+m)?[\\d,]+sф Left");
	private static final Pattern HEALTH_STATUS = Pattern.compile("(?<health>[\\d,]+)/(?<max>[\\d,]+)❤(?<healing>\\+([\\d,]+)[▁-▆])?");
	private static final Pattern HEALING = Pattern.compile("(?:§[\\da-z])*❤");
	private static final Pattern DEFENSE_STATUS = Pattern.compile("(?<defense>[\\d,]+)❈ Defense");
	private static final Pattern MANA_USE = Pattern.compile("-(?:[\\d,]+) Mana \\(.*?\\)");
	private static final Pattern MANA_STATUS = Pattern.compile("(?<mana>[\\d,]+)/(?<max>[\\d,]+)✎ (?:Mana|(?<overflow>[\\d,]+)ʬ)");

	private static final Minecraft client = Minecraft.getInstance();
	private static Resource health = new Resource(100, 100, 0);
	private static Resource mana = new Resource(100, 100, 0);
	private static Resource speed = new Resource(100, 400, 0);
	private static Resource air = new Resource(100, 300, 0);
	private static int defense = 0;

	private static int ticks;
	private static int lastManaTick;
	private static int lastMana;
	private static int manaPerSecond;

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

	public static Resource getMana() {
		return mana;
	}

	public static boolean isManaEstimated() {
		return ticks - lastManaTick > 30;
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
		if (client.player == null || !Utils.isOnSkyblock()) return;
		ticks++;
		updateHealth(health.value, health.max, health.overflow);
		updateSpeed();
		updateAir();
		if (ticks - lastManaTick > 0 && (ticks - lastManaTick) % 20 == 0) {
			mana = new Resource(Math.min(mana.value() + manaPerSecond, mana.max()), mana.max(), mana.overflow());
		}
	}

	@SuppressWarnings("SameReturnValue")
	private static InteractionResult interactItem(Player player, Level world, InteractionHand hand) {
		if (client.player == null) return InteractionResult.PASS;
		ItemStack handStack = client.player.getMainHandItem();
		int manaCost = 0;
		for (ItemAbility ability : handStack.skyblocker$getAbilities()) {
			if (ability.activation().isRightClick()) {
				manaCost = ability.manaCost().orElse(0);
				break;
			}
		}
		if (manaCost > 0 && manaCost <= mana.value()) {
			mana = new Resource(Math.max(mana.value() - manaCost, 0), mana.max(), mana.overflow());
		}
		return InteractionResult.PASS;
	}

	private static boolean allowOverlayMessage(Component text, boolean overlay) {
		onOverlayMessage(text, overlay);
		return true;
	}

	private static Component onOverlayMessage(Component text, boolean overlay) {
		if (!overlay || !Utils.isOnSkyblock()) {
			return text;
		}
		if (FancyStatusBars.isEnabled()) {
			return Component.nullToEmpty(update(text.getString(), SkyblockerConfigManager.get().chat.hideMana));
		} else {
			//still update values for other parts of the mod to use
			update(text.getString(), SkyblockerConfigManager.get().chat.hideMana);
			return text;
		}
	}

	public static @Nullable String update(String actionBar, boolean filterManaUse) {
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
							status.find();

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

		String result = statuses.appendTail(output).toString().trim();

		return result.isEmpty() ? null : result;
	}

	private static void updateHealth(Matcher matcher) {
		int health = RegexUtils.parseIntFromMatcher(matcher, "health");
		int max = RegexUtils.parseIntFromMatcher(matcher, "max");
		updateHealth(health, max, Math.max(0, health - max));
	}

	private static void updateHealth(int value, int max, int overflow) {
		// Client doesn't exist in test environment.
		if (!Debug.isTestEnvironment() && client.player != null) {
			value = (int) (client.player.getHealth() * max / client.player.getMaxHealth());
			overflow = (int) (client.player.getAbsorptionAmount() * max / client.player.getMaxHealth());
		}
		health = new Resource(Math.min(value, max), max, Math.min(overflow, max));
	}

	private static void updateMana(Matcher m) {
		int mana = RegexUtils.parseIntFromMatcher(m, "mana");
		int max = RegexUtils.parseIntFromMatcher(m, "max");
		int overflow = m.group("overflow") == null ? 0 : RegexUtils.parseIntFromMatcher(m, "overflow");
		StatusBarTracker.mana = new Resource(mana, max, overflow);
		if (mana != max && lastMana < mana) manaPerSecond = Math.max(mana - lastMana, 0);
		if (lastMana != mana || mana == max) lastManaTick = ticks;
		lastMana = mana;
	}

	private static void updateSpeed() {
		// Black cat and racing helm are untested - I don't have the money to test atm, but no reason why they shouldn't work
		assert client.player != null;
		int value = (int) (client.player.isSprinting() ? (client.player.getSpeed() / 1.3f) * 1000 : client.player.getSpeed() * 1000);
		int max = 400; // hardcoded limit (except for with cactus knife, black cat, snail, racing helm, young drag)
		if (client.player.getMainHandItem().getHoverName().getString().contains("Cactus Knife") && Utils.getLocation() == Location.GARDEN) {
			max = 500;
		}
		Iterable<ItemStack> armor = ItemUtils.getArmor(client.player);
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
		assert client.player != null;
		int max = client.player.getMaxAirSupply();
		int value = Math.clamp(client.player.getAirSupply(), 0, max);
		air = new Resource(value, max, 0);
	}

	public record Resource(int value, int max, int overflow) {}
}
