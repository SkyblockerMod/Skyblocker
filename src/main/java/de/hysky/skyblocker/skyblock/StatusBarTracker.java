package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import de.hysky.skyblocker.skyblock.fancybars.StatusBarType;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusBarTracker {
	private static final Pattern STATUS_HEALTH = Pattern.compile("§[6c](?<health>[\\d,]+)/(?<max>[\\d,]+)❤ *(?<healing>\\+§c([\\d,]+). *)?");
	private static final Pattern DEFENSE_STATUS = Pattern.compile("§a(?<defense>[\\d,]+)§a❈ Defense *");
	private static final Pattern MANA_USE = Pattern.compile("§b-([\\d,]+) Mana \\(§.*?\\) *");
	private static final Pattern MANA_STATUS = Pattern.compile("§b(?<mana>[\\d,]+)/(?<max>[\\d,]+)✎ (?:Mana|§3(?<overflow>[\\d,]+)ʬ) *");

	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static Resource health = new Resource(100, 100, 0);
	private static Resource mana = new Resource(100, 100, 0);
	private static Resource speed = new Resource(100, 400, 0);
	private static Resource air = new Resource(100, 300, 0);
	private static int defense = 0;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(StatusBarTracker::allowOverlayMessage);
		ClientReceiveMessageEvents.MODIFY_GAME.register(StatusBarTracker::onOverlayMessage);
		Scheduler.INSTANCE.scheduleCyclic(StatusBarTracker::tick, 1);
	}

	public static Resource getHealth() {
		return health;
	}

	public static Resource getMana() {
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
		if (client == null || client.player == null) return;
		updateHealth(health.value, health.max, health.overflow);
		updateSpeed();
		updateAir();
	}

	private static boolean allowOverlayMessage(Text text, boolean overlay) {
		onOverlayMessage(text, overlay);
		return true;
	}

	private static Text onOverlayMessage(Text text, boolean overlay) {
		if (!overlay || !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.bars.enableBars || Utils.isInTheRift()) {
			return text;
		}
		return Text.of(update(text.getString(), SkyblockerConfigManager.get().chat.hideMana));
	}

	public static String update(String actionBar, boolean filterManaUse) {
		var sb = new StringBuilder();

		// Match health and don't add it to the string builder
		// Append healing to the string builder if there is any healing
		Matcher matcher = STATUS_HEALTH.matcher(actionBar);
		if (!matcher.find()) return actionBar;
		updateHealth(matcher);
		if (matcher.group("healing") != null) {
			sb.append("§c❤");
		}
		if (!FancyStatusBars.isHealthFancyBarEnabled()) matcher.appendReplacement(sb, "$0");
		else matcher.appendReplacement(sb, "$3");

		// Match defense or mana use and don't add it to the string builder
		if (matcher.usePattern(DEFENSE_STATUS).find()) {
			defense = RegexUtils.parseIntFromMatcher(matcher, "defense");
			if (FancyStatusBars.isBarEnabled(StatusBarType.DEFENSE)) matcher.appendReplacement(sb, "");
			else matcher.appendReplacement(sb, "$0");
		} else if (filterManaUse && matcher.usePattern(MANA_USE).find()) {
			matcher.appendReplacement(sb, "");
		}

		// Match mana and don't add it to the string builder
		if (matcher.usePattern(MANA_STATUS).find()) {
			updateMana(matcher);
			if (FancyStatusBars.isBarEnabled(StatusBarType.INTELLIGENCE)) matcher.appendReplacement(sb, "");
			else matcher.appendReplacement(sb, "$0");
		}

		// Append the rest of the message to the string builder
		matcher.appendTail(sb);
		String res = sb.toString().trim();
		return res.isEmpty() ? null : res;
	}

	private static void updateHealth(Matcher matcher) {
		int health = RegexUtils.parseIntFromMatcher(matcher, "health");
		int max = RegexUtils.parseIntFromMatcher(matcher, "max");
		updateHealth(health, max, Math.max(0, health - max));
	}

	private static void updateHealth(int value, int max, int overflow) {
		if (client != null && client.player != null) {
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
	}

	private static void updateSpeed() {
		// Black cat and racing helm are untested - I don't have the money to test atm, but no reason why they shouldn't work
		assert client.player != null;
		int value = (int) (client.player.isSprinting() ? (client.player.getMovementSpeed() / 1.3f) * 1000 : client.player.getMovementSpeed() * 1000);
		int max = 400; // hardcoded limit (except for with cactus knife, black cat, snail, racing helm, young drag)
		if (client.player.getMainHandStack().getName().getString().contains("Cactus Knife") && Utils.getLocation() == Location.GARDEN) {
			max = 500;
		}
		Iterable<ItemStack> armor = ItemUtils.getArmor(client.player);
		int youngDragCount = 0;
		for (ItemStack armorPiece : armor) {
			if (armorPiece.getName().getString().contains("Racing Helmet")) {
				max = 500;
			} else if (armorPiece.getName().getString().contains("Young Dragon")) {
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
		int max = client.player.getMaxAir();
		int value = Math.clamp(client.player.getAir(), 0, max);
		air = new Resource(value, max, 0);
	}

	public record Resource(int value, int max, int overflow) {}
}
