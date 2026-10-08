package de.hysky.skyblocker.debug;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.hunting.AttributesDebug;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.TextTransformer;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;

public class ReforgesDebug {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path REFORGES_DEST = SkyblockerMod.CONFIG_DIR.resolve("debug/reforges.json");
	private static final Path ADVANCED_REFORGES_DEST = SkyblockerMod.CONFIG_DIR.resolve("debug/reforgestones.json");

	private static final Map<String, Reforge> REFORGES = new TreeMap<>();
	private static final Map<String, ReforgeStone> ADVANCED_REFORGES = new TreeMap<>();

	@Init
	public static void init() {
		if (!Debug.debugEnabled() & !SkyblockerConfigManager.get().debug.enableRepoDev) return;
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return;
			if (!(containerScreen.getMenu() instanceof ChestMenu)) return;
			ScreenKeyboardEvents.afterKeyPress(screen).register((_, input) -> {
				if (input.key() == InputConstants.KEY_G) {
					dumpReforge(containerScreen);
				} else if (input.key() == InputConstants.KEY_J) {
					exportReforges();
				}
			});
		});
	}

	private static void dumpReforge(AbstractContainerScreen<?> screen) {
		Int2ObjectMap<@Nullable ItemStack> slots = getSlots(screen);
		ItemStack potentialReforge = slots.get(4);
		if (potentialReforge == null || potentialReforge.isEmpty()) return;
		String reforgeName = screen.getTitle().getString();
		if (!potentialReforge.getHoverName().getString().equals(reforgeName)) return;

		boolean isAdvancedReforge = potentialReforge.skyblocker$getLoreStrings().stream().noneMatch(x -> x.contains("Rolled at random"));
		List<String> itemTypes = parseItemTypes(potentialReforge.skyblocker$getLoreStrings());

		Map<String, Map<String, Float>> reforgeStats = new LinkedHashMap<>();
		List<String> rarities = new ArrayList<>();
		Map<String, Integer> coinCost = new LinkedHashMap<>();

		for (int i = 19; i <= 25; i++) {
			ItemStack rarityStat = slots.get(i);
			if (rarityStat == null || rarityStat.isEmpty()) continue;
			if (rarityStat.is(Items.STAINED_GLASS_PANE.black())) continue;
			String rarity = rarityStat.getHoverName().getString().split(" ")[0];
			Map<String, Float> rarityStats = parseRarityStats(rarityStat.skyblocker$getLoreStrings());
			if (rarityStat.isEmpty()) continue;
			rarities.add(rarity);
			reforgeStats.put(rarity, rarityStats);
			if (isAdvancedReforge) coinCost.put(rarity, parseCoinCost(rarityStat.skyblocker$getLoreStrings()));
		}

		if (isAdvancedReforge) {
			String internalName = potentialReforge.getNeuName();

			ItemStack bonusSlot = slots.get(31);
			if (bonusSlot == null || bonusSlot.isEmpty()) return;
			IO.println(bonusSlot.getHoverName().getString());

			//noinspection deprecation
			Optional<String> reforgeBonus = parseBonus(bonusSlot.getHoverName().getString(), ItemUtils.getLore(bonusSlot));
			reforgeName = potentialReforge.skyblocker$getLoreStrings().getFirst().replace("Can be used to apply the ", "");

			ADVANCED_REFORGES.put(internalName, new ReforgeStone(internalName, reforgeName, "blacksmith/reforge_stone",
					String.join("/", itemTypes), rarities, coinCost, reforgeBonus, reforgeStats));
		} else {
			REFORGES.put(reforgeName, new Reforge(reforgeName, String.join("/", itemTypes), rarities, reforgeStats));
		}
	}

	// TODO: better parsing of item types
	private static List<String> parseItemTypes(List<String> lore) {
		List<String> itemTypes = new ArrayList<>();
		StringBuilder combinedBuilder = new StringBuilder();
		for (String line : lore) {
			if (line.startsWith("Can be applied to")) {
				line = line.replace("Can be applied to ", "");
			}
			// Advanced Reforges Jank
			if (line.startsWith("Can be used to apply the")) continue;
			if (line.startsWith("reforge to a ")) {
				line = line.replace("reforge to a ", "");
			}
			if (line.isEmpty()) {
				break;
			}
			combinedBuilder.append(line);
		}
		String combined = combinedBuilder.toString().toUpperCase(Locale.ENGLISH).replace(".", "").replace(" OR", "");
		combined = combined.replace("MINING TOOL", "PICKAXE");
		combined = combined.replace("MELEE WEAPON", "SWORD");
		combined = combined.replace("FISHING ROD", "FISHING_ROD");
		String[] rawTypes = combined.split(" ");
		Arrays.stream(rawTypes).map(x -> x.replace(" ", "_")).forEach(itemTypes::add);
		return itemTypes;
	}

	private static Map<String, Float> parseRarityStats(List<String> lore) {
		Map<String, Float> rarityStats = new LinkedHashMap<>();
		boolean reachedStats = false;
		for (String line : lore) {
			if (line.isEmpty()) {
				if (reachedStats) break;
				reachedStats = true;
				continue;
			}
			if (!reachedStats) continue;

			String[] parts = line.split(" ", 3);
			if (parts.length != 3) {
				System.out.println("Stat line doesn't match pattern: ");
				System.out.println(String.join(" ", parts));
				continue;
			}

			String increase = parts[0];
			if (increase.charAt(0) == '+') increase = increase.substring(1);
			String statName = parts[2].toLowerCase(Locale.ENGLISH).replace(" ", "_");
			if (increase.endsWith("%")) {
				increase = increase.substring(0, increase.length() - 1);
				statName = statName + "%";
			}
			rarityStats.put(statName, Float.parseFloat(increase));
		}
		return rarityStats;
	}

	private static Optional<String> parseBonus(String name, List<Component> componentLore) {
		if (name.equals("Reforge Bonus")) return Optional.empty();

		MutableComponent combined = Component.empty();
		boolean isEmpty = true;
		for (Component component : componentLore) {
			if (!isEmpty) combined.append(CommonComponents.SPACE);
			combined.append(component);
			isEmpty = false;
		}
		return Optional.of(TextTransformer.toLegacy(combined).replace('&', '§'));
	}

	private static Integer parseCoinCost(List<String> lore) {
		for (String line : lore) {
			if (!line.endsWith("Coins")) continue;
			return Integer.parseInt(line.replace(" Coins", "").replace(",", ""));
		}
		return -1;
	}

	private static void exportReforges() {
		CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(REFORGES_DEST.getParent());
				Files.writeString(REFORGES_DEST, Reforge.MAP_CODEC.encodeStart(JsonOps.INSTANCE, REFORGES).getOrThrow().toString());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Reforges Debug] Failed to export basic reforges!", e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);

		CompletableFuture.runAsync(() -> {
			try {
				Files.createDirectories(ADVANCED_REFORGES_DEST.getParent());
				Files.writeString(ADVANCED_REFORGES_DEST, ReforgeStone.MAP_CODEC.encodeStart(JsonOps.INSTANCE, ADVANCED_REFORGES).getOrThrow().toString());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Reforges Debug] Failed to export advanced reforges!", e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	/// Copied from {@link AttributesDebug}
	private static Int2ObjectMap<ItemStack> getSlots(AbstractContainerScreen<?> screen) {
		@SuppressWarnings("unchecked")
		Int2ObjectMap<ItemStack> slots = ContainerSolverManager.slotMap(screen.getMenu().slots.subList(0, ((AbstractContainerScreen<ChestMenu>) screen).getMenu().getRowCount() * 9));
		return slots;
	}

	public record Reforge(String reforgeName, String itemTypes, List<String> requiredRarities, Map<String, Map<String, Float>> reforgeStats) {
		public static Codec<Reforge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("reforgeName").forGetter(Reforge::reforgeName),
				Codec.STRING.fieldOf("itemTypes").forGetter(Reforge::itemTypes),
				Codec.STRING.listOf().fieldOf("requiredRarities").forGetter(Reforge::requiredRarities),
				Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.FLOAT)).fieldOf("reforgeStats").forGetter(Reforge::reforgeStats)
		).apply(instance, Reforge::new));

		public static UnboundedMapCodec<String, Reforge> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
	}

	public record ReforgeStone(String internalName, String reforgeName, String reforgeType, String itemTypes, List<String> requiredRarities, Map<String, Integer> reforgeCosts,
							Optional<String> reforgeAbility, Map<String, Map<String, Float>> reforgeStats) {
		public static Codec<ReforgeStone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("internalName").forGetter(ReforgeStone::internalName),
				Codec.STRING.fieldOf("reforgeName").forGetter(ReforgeStone::reforgeName),
				Codec.STRING.fieldOf("reforgeType").forGetter(ReforgeStone::reforgeType),
				Codec.STRING.fieldOf("itemTypes").forGetter(ReforgeStone::itemTypes),
				Codec.STRING.listOf().fieldOf("requiredRarities").forGetter(ReforgeStone::requiredRarities),
				Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("reforgeCosts").forGetter(ReforgeStone::reforgeCosts),
				Codec.STRING.optionalFieldOf("reforgeAbility").forGetter(ReforgeStone::reforgeAbility),
				Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.FLOAT)).fieldOf("reforgeStats").forGetter(ReforgeStone::reforgeStats)
		).apply(instance, ReforgeStone::new));

		public static UnboundedMapCodec<String, ReforgeStone> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC);
	}
}
