package de.hysky.skyblocker.skyblock.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.utils.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCooldowns {
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemCooldowns.class);
	private static final List<String> BAT_ARMOR_IDS = List.of("BAT_PERSON_HELMET", "BAT_PERSON_CHESTPLATE", "BAT_PERSON_LEGGINGS", "BAT_PERSON_BOOTS");
	private static final Map<String, CooldownConfigEntry> ITEM_CONFIGS = new HashMap<>();
	private static final Map<String, CooldownEntry> ITEM_COOLDOWNS = new HashMap<>();

	private static final int[] EXPERIENCE_LEVELS = {
			0, 660, 730, 800, 880, 960, 1050, 1150, 1260, 1380, 1510, 1650, 1800, 1960, 2130,
			2310, 2500, 2700, 2920, 3160, 3420, 3700, 4000, 4350, 4750, 5200, 5700, 6300, 7000,
			7800, 8700, 9700, 10800, 12000, 13300, 14700, 16200, 17800, 19500, 21300, 23200,
			25200, 27400, 29800, 32400, 35200, 38200, 41400, 44800, 48400, 52200, 56200, 60400,
			64800, 69400, 74200, 79200, 84700, 90700, 97200, 104200, 111700, 119700, 128200,
			137200, 147700, 156700, 167700, 179700, 192700, 206700, 221700, 237700, 254700,
			272700, 291700, 311700, 333700, 357700, 383700, 411700, 441700, 476700, 516700,
			561700, 611700, 666700, 726700, 791700, 861700, 936700, 1016700, 1101700, 1191700,
			1286700, 1386700, 1496700, 1616700, 1746700, 1886700
	};
	private static int monkeyLevel = 1;
	private static double monkeyExp = 0;
	private static boolean wasPressed = false;

	@Init
	public static void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(ItemCooldowns::loadCooldownConfig);
		UseItemCallback.EVENT.register((player, world, hand) -> applyCooldownIfConfigured(player, world, "right_click") ? ActionResult.SUCCESS : ActionResult.PASS);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) return;
			boolean currentlyPressed = client.options.attackKey.isPressed();
			if (currentlyPressed && !wasPressed) {
				applyCooldownIfConfigured(client.player, client.world, "left_click");
			}
			wasPressed = currentlyPressed;
		});
		ClientPlayerBlockBreakEvents.AFTER.register(ItemCooldowns::afterBlockBreak);
	}

	public static void updateCooldown() {
		PetInfo pet = PetCache.getCurrentPet();

		if (pet != null && pet.tier().equals(SkyblockItemRarity.LEGENDARY)) {
			monkeyExp = pet.exp();

			monkeyLevel = 0;
			for (int xpLevel : EXPERIENCE_LEVELS) {
				if (monkeyExp < xpLevel) {
					break;
				} else {
					monkeyExp -= xpLevel;
					monkeyLevel++;
				}
			}
		}
	}

	private static int getCooldown4Foraging(CooldownConfigEntry config) {
		int baseCooldown = config.cooldown;
		int monkeyPetCooldownReduction = baseCooldown * monkeyLevel / 200;
		return baseCooldown - monkeyPetCooldownReduction;
	}

	//	"ITEM_ID": {
	//		"cooldown": 1000,                // Cooldown duration in milliseconds
	//		"cooldown_type": "foraging",     // Optional: defines dynamic cooldown logic
	//		"trigger": "right_click",        // Action that triggers the ability (e.g. "right_click", "left_click", "block_break")
	//		"condition": "someCondition",    // Optional: name of a special condition to check before triggering
	//		"block_tag": "minecraft:logs",   // Optional: required block tag for block_break triggers
	//		"toggle": true,                  // If true, item can be toggled on/off with one click (logic not implemented yet)
	//		"requires_sneak": true,          // If true, player must be sneaking to trigger the item
	//		"cooldown_group": "GROUP_ID"     // Optional: group of items that share the same cooldown (e.g. jungle axe and treecapitator)
	// FIXME: add logic for toggle (Wither cloak) and Logic for items that have two abilities with separate cooldowns (e.g., Gyrokinetic Wand)
	//	}
	private static void loadCooldownConfig(MinecraftClient client) {
		try (BufferedReader reader = client.getResourceManager().openAsReader(Identifier.of(SkyblockerMod.NAMESPACE, "cooldown/item_cooldown.json"))) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
				JsonObject obj = entry.getValue().getAsJsonObject();
				CooldownConfigEntry cfg = new CooldownConfigEntry();
				cfg.trigger = obj.get("trigger").getAsString();
				cfg.cooldown = obj.has("cooldown") && obj.get("cooldown").isJsonPrimitive() && obj.get("cooldown").getAsJsonPrimitive().isNumber() ? obj.get("cooldown").getAsInt() : 0;
				cfg.cooldownType = obj.has("cooldown_type") ? obj.get("cooldown_type").getAsString() : null;
				cfg.condition = obj.has("condition") ? obj.get("condition").getAsString() : null;
				cfg.block_tag = obj.has("block_tag") ? obj.get("block_tag").getAsString() : null;
				cfg.toggle = obj.has("toggle") && obj.get("toggle").getAsBoolean();
				cfg.requiresSneak = obj.has("requires_sneak") && obj.get("requires_sneak").getAsBoolean();
				cfg.cooldownGroup = obj.has("cooldown_group") ? obj.get("cooldown_group").getAsString() : null;
				ITEM_CONFIGS.put(entry.getKey(), cfg);
			}
			LOGGER.info("[Skyblocker] Loaded item cooldown");
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to load item cooldown", e);
		}
	}

	private static boolean applyCooldownIfConfigured(PlayerEntity player, World world, String triggerType) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns) return false;
		String itemId = ItemUtils.getItemId(player.getMainHandStack());
		CooldownConfigEntry config = ITEM_CONFIGS.get(itemId);

		if (config == null || !triggerType.equals(config.trigger)) return false;
		if (config.requiresSneak && !player.isSneaking()) return false;

		if (isOnCooldown(itemId)) return false;
		if (config.condition != null && !checkCondition(config.condition, player)) return false;

		int cooldown = resolveCooldown(config);
		ITEM_COOLDOWNS.put(itemId, new CooldownEntry(cooldown));

		if (config.cooldownGroup != null) {
			for (Map.Entry<String, CooldownConfigEntry> e : ITEM_CONFIGS.entrySet()) {
				if (config.cooldownGroup.equals(e.getValue().cooldownGroup)) {
					ITEM_COOLDOWNS.put(e.getKey(), new CooldownEntry(cooldown));
				}
			}
		}
		return true;
	}

	public static void afterBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.itemCooldown.enableItemCooldowns) return;

		String itemId = ItemUtils.getItemId(player.getMainHandStack());
		CooldownConfigEntry config = ITEM_CONFIGS.get(itemId);
		if (config == null || !"block_break".equals(config.trigger) || isOnCooldown(itemId)) return;

		if (config.block_tag != null) {
			TagKey<Block> tag = TagKey.of(Registries.BLOCK.getKey(), Identifier.of(config.block_tag));
			if (!state.isIn(tag)) return;
		}

		ITEM_COOLDOWNS.put(itemId, new CooldownEntry(resolveCooldown(config)));
	}

	public static boolean isOnCooldown(ItemStack itemStack) {
		String itemId = ItemUtils.getItemId(itemStack);
		CooldownConfigEntry config = ITEM_CONFIGS.get(itemId);

		if (config == null) return false;

		if (isOnCooldown(itemId)) return true;

		if (config.cooldownGroup != null) {
			for (Map.Entry<String, CooldownConfigEntry> e : ITEM_CONFIGS.entrySet()) {
				if (config.cooldownGroup.equals(e.getValue().cooldownGroup)) {
					if (isOnCooldown(e.getKey())) return true;
				}
			}
		}

		return false;
	}

	private static int resolveCooldown(CooldownConfigEntry config) {
		if ("foraging".equals(config.cooldownType)) {
			updateCooldown();
			return getCooldown4Foraging(config);
		}
		return config.cooldown;
	}

	public static boolean isOnCooldown(String itemId) {
		CooldownConfigEntry config = ITEM_CONFIGS.get(itemId);

		if (ITEM_COOLDOWNS.containsKey(itemId)) {
			CooldownEntry entry = ITEM_COOLDOWNS.get(itemId);
			if (entry.isOnCooldown()) return true;
			ITEM_COOLDOWNS.remove(itemId);
		}

		if (config != null && config.cooldownGroup != null) {
			for (Map.Entry<String, CooldownEntry> e : ITEM_COOLDOWNS.entrySet()) {
				CooldownConfigEntry otherConfig = ITEM_CONFIGS.get(e.getKey());
				if (otherConfig != null && config.cooldownGroup.equals(otherConfig.cooldownGroup)) {
					if (e.getValue().isOnCooldown()) return true;
				}
			}
		}

		return false;
	}

	public static CooldownEntry getItemCooldownEntry(ItemStack itemStack) {
		String itemId = ItemUtils.getItemId(itemStack);
		CooldownConfigEntry config = ITEM_CONFIGS.get(itemId);

		if (config == null) return null;

		CooldownEntry entry = ITEM_COOLDOWNS.get(itemId);
		if (entry != null && entry.isOnCooldown()) return entry;

		if (config.cooldownGroup != null) {
			for (Map.Entry<String, CooldownEntry> e : ITEM_COOLDOWNS.entrySet()) {
				CooldownConfigEntry otherConfig = ITEM_CONFIGS.get(e.getKey());
				if (otherConfig != null && config.cooldownGroup.equals(otherConfig.cooldownGroup)) {
					if (e.getValue().isOnCooldown()) return e.getValue();
				}
			}
		}

		return null;
	}

	private static boolean checkCondition(String condition, PlayerEntity player) {
		return switch (condition) {
			case "hasFishingHookAndNotBatArmor" -> player.fishHook != null && !isWearingBatArmor(player);
			default -> true;
		};
	}

	private static boolean isWearingBatArmor(PlayerEntity player) {
		for (ItemStack stack : ItemUtils.getArmor(player)) {
			String itemId = ItemUtils.getItemId(stack);
			if (!BAT_ARMOR_IDS.contains(itemId)) {
				return false;
			}
		}
		return true;
	}

	public record CooldownEntry(int cooldown, long startTime) {
		public CooldownEntry(int cooldown) {
			this(cooldown, System.currentTimeMillis());
		}

		public boolean isOnCooldown() {
			return (this.startTime + this.cooldown) > System.currentTimeMillis();
		}

		public long getRemainingCooldown() {
			long time = (this.startTime + this.cooldown) - System.currentTimeMillis();
			return Math.max(time, 0);
		}

		public float getRemainingCooldownPercent() {
			return this.isOnCooldown() ? (float) this.getRemainingCooldown() / cooldown : 0.0f;
		}
	}

	private static class CooldownConfigEntry {
		public String trigger;
		public Integer cooldown;
		public String condition;
		public String cooldownType;
		public String block_tag;
		public boolean toggle;
		public boolean requiresSneak;
		public String cooldownGroup;
	}
}
