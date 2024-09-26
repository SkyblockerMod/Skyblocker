package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
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

	private final MinecraftClient client = MinecraftClient.getInstance();
	private Resource health = new Resource(100, 100, 0);
	private Resource mana = new Resource(100, 100, 0);
	private Resource speed = new Resource(100, 400, 0);
	private int defense = 0;

	public void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(this::allowOverlayMessage);
		ClientReceiveMessageEvents.MODIFY_GAME.register(this::onOverlayMessage);
		Scheduler.INSTANCE.scheduleCyclic(this::tick, 1);
	}

	public Resource getHealth() {
		return this.health;
	}

	public Resource getMana() {
		return this.mana;
	}

	public int getDefense() {
		return this.defense;
	}

	public Resource getSpeed() {
		return this.speed;
	}

	private void tick() {
		if (client == null || client.player == null) return;
		updateHealth(health.value, health.max, health.overflow);
		updateSpeed();
	}

	private boolean allowOverlayMessage(Text text, boolean overlay) {
		onOverlayMessage(text, overlay);
		return true;
	}

	private Text onOverlayMessage(Text text, boolean overlay) {
		if (!overlay || !Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.bars.enableBars || Utils.isInTheRift()) {
			return text;
		}
		return Text.of(update(text.getString(), SkyblockerConfigManager.get().chat.hideMana));
	}

	public String update(String actionBar, boolean filterManaUse) {
		var sb = new StringBuilder();

		// Match health and don't add it to the string builder
		// Append healing to the string builder if there is any healing
		Matcher matcher = STATUS_HEALTH.matcher(actionBar);
		if (!matcher.find()) return actionBar;
		updateHealth(matcher);
		if (matcher.group("healing") != null) {
			sb.append("§c❤");
		}
		matcher.appendReplacement(sb, "$3");

		// Match defense or mana use and don't add it to the string builder
		if (matcher.usePattern(DEFENSE_STATUS).find()) {
			defense = RegexUtils.parseIntFromMatcher(matcher, "defense");
			matcher.appendReplacement(sb, "");
		} else if (filterManaUse && matcher.usePattern(MANA_USE).find()) {
			matcher.appendReplacement(sb, "");
		}

		// Match mana and don't add it to the string builder
		if (matcher.usePattern(MANA_STATUS).find()) {
			updateMana(matcher);
			matcher.appendReplacement(sb, "");
		}

		// Append the rest of the message to the string builder
		matcher.appendTail(sb);
		String res = sb.toString().trim();
		return res.isEmpty() ? null : res;
	}

	private void updateHealth(Matcher matcher) {
		int health = RegexUtils.parseIntFromMatcher(matcher, "health");
		int max = RegexUtils.parseIntFromMatcher(matcher, "max");
		updateHealth(health, max, Math.max(0, health - max));
	}

	private void updateHealth(int value, int max, int overflow) {
		if (client != null && client.player != null) {
			value = (int) (client.player.getHealth() * max / client.player.getMaxHealth());
			overflow = (int) (client.player.getAbsorptionAmount() * max / client.player.getMaxHealth());
		}
		health = new Resource(Math.min(value, max), max, Math.min(overflow, max));
	}

	private void updateMana(Matcher m) {
		int mana = RegexUtils.parseIntFromMatcher(m, "mana");
		int max = RegexUtils.parseIntFromMatcher(m, "max");
		int overflow = m.group("overflow") == null ? 0 : RegexUtils.parseIntFromMatcher(m, "overflow");
		this.mana = new Resource(mana, max, overflow);
	}

	private void updateSpeed() {
		// Black cat and racing helm are untested - I don't have the money to test atm, but no reason why they shouldn't work
		assert client.player != null;
		int value = (int) (client.player.isSprinting() ? (client.player.getMovementSpeed() / 1.3f) * 1000 : client.player.getMovementSpeed() * 1000);
		int max = 400; // hardcoded limit (except for with cactus knife, black cat, snail, racing helm, young drag)
		if (client.player.getMainHandStack().getName().getString().contains("Cactus Knife") && Utils.getLocation() == Location.GARDEN) {
			max = 500;
		}
		Iterable<ItemStack> armor = client.player.getArmorItems();
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

		PetCache.PetInfo pet = PetCache.getCurrentPet();
		if (pet != null) {
			if (pet.type().contains("BLACK_CAT")) {
				max = 500;
			} else if (pet.type().contains("SNAIL")) {
				max = 100;
			}
		}
		this.speed = new Resource(value, max, 0);
	}

	public record Resource(int value, int max, int overflow) {}
}
