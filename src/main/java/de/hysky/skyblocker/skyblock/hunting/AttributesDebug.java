package de.hysky.skyblocker.skyblock.hunting;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RomanNumerals;
import de.hysky.skyblocker.utils.container.ContainerSolver;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.text.WordUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributesDebug {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Path ATTRIBUTE_EXPORT_DEST = SkyblockerMod.CONFIG_DIR.resolve("attribute_export.json");
	// Attribute Menu
	private static final Pattern SOURCE_PATTERN = Pattern.compile("Source: (?<shardName>[A-za-z ]+) Shard \\((?<id>[CUREL]\\d+)\\)");
	private static final Pattern RARITY_PATTERN = Pattern.compile("Rarity: (?<rarity>\\w+)");
	// Hunting Box
	private static final Pattern FAMILY_PATTERN = Pattern.compile("^(?<family>[\\w ]+) Family$");
	private static final Pattern ALIGNMENT_PATTERN = Pattern.compile("^(?:COMMON|UNCOMMON|RARE|EPIC|LEGENDARY) (?<alignment>[\\w ]+) SHARD \\(ID [A-Z0-9]+\\)");

	private static final Map<String, Attribute> DUMPED_ATTRIBUTES = new Object2ObjectOpenHashMap<>();

	@Init
	public static void init() {
		if (!Debug.debugEnabled() && !SkyblockerConfigManager.get().debug.enableRepoDev) return;
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			final AbstractContainerScreen<?> attributeMenu = checkForAttributeMenu(screen);
			if (attributeMenu != null) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((_, input) -> {
					if (input.key() == InputConstants.KEY_G) {
						dumpAttributes(attributeMenu);
					} else if (input.key() == InputConstants.KEY_J) {
						exportAttributes(attributeMenu);
					}
				});
				return;
			}
			final AbstractContainerScreen<?> huntingBox = checkForHuntingBox(screen);
			if (huntingBox != null) {
				ScreenKeyboardEvents.afterKeyPress(screen).register((_, input) -> {
					if (input.key() == InputConstants.KEY_G) {
						fixAttributes(huntingBox);
					} else if (input.key() == InputConstants.KEY_J) {
						exportAttributes(huntingBox);
					}
				});
			}
		});
	}

	private static Int2ObjectMap<ItemStack> getSlots(AbstractContainerScreen<?> screen) {
		@SuppressWarnings("unchecked")
		Int2ObjectMap<ItemStack> slots = ContainerSolverManager.slotMap(screen.getMenu().slots.subList(0, ((AbstractContainerScreen<ChestMenu>) screen).getMenu().getRowCount() * 9));
		ContainerSolver.trimEdges(slots, 6);
		return slots;
	}

	private static void dumpAttributes(AbstractContainerScreen<?> screen) {
		Int2ObjectMap<ItemStack> slots = getSlots(screen);
		for (ItemStack stack : slots.values()) {
			if (stack.isEmpty()) continue;

			String abilityName = stack.getHoverName().getString();
			Matcher sourceMatcher = ItemUtils.getLoreLineIfMatch(stack, SOURCE_PATTERN);
			Matcher rarityMatcher = ItemUtils.getLoreLineIfMatch(stack, RARITY_PATTERN);

			//Remove roman numeral from name
			List<String> words = new ArrayList<>(Arrays.asList(abilityName.split(" ")));
			if (RomanNumerals.isValidRomanNumeral(words.getLast().strip())) {
				words.removeLast();
				abilityName = String.join(" ", words);
			}

			if (sourceMatcher != null && rarityMatcher != null) {
				String shardName = sourceMatcher.group("shardName");
				String shardId = sourceMatcher.group("id");
				String rarity = rarityMatcher.group("rarity");

				String bazaarNameGuess = "SHARD_" + shardName.replace(' ', '_').toUpperCase(Locale.ENGLISH);
				String neuIdGuess = "ATTRIBUTE_SHARD_" + abilityName.replace("'", "").replace(' ', '_').toUpperCase(Locale.ENGLISH) + ";1";

				boolean hasDataForId = TooltipInfoType.BAZAAR.getData().containsKey(bazaarNameGuess);
				//Most attributes follow the format above but some have different ids so this is to catch those
				if (!hasDataForId) LOGGER.warn("[Skyblocker Attributes Debug] No data found for shard. Shard Name: {}", shardName);

				Attribute attribute = new Attribute(bazaarNameGuess, shardName, rarity, neuIdGuess,
						abilityName, "", new ArrayList<>(), shardId);
				DUMPED_ATTRIBUTES.put(shardName, attribute);
			} else {
				LOGGER.warn("[Skyblocker Attributes Debug] Failed to match shard! Name: {}", abilityName);
			}
		}
	}

	private static void fixAttributes(AbstractContainerScreen<?> screen) {
		Int2ObjectMap<ItemStack> slots = getSlots(screen);
		for (ItemStack stack : slots.values()) {
			if (stack.isEmpty()) continue;

			String abilityName = stack.getHoverName().getString();
			if (!DUMPED_ATTRIBUTES.containsKey(abilityName)) {
				LOGGER.error("[Skyblocker Attributes Debug] Could not find attribute for {}", abilityName);
				continue;
			}

			Matcher familyMatcher = ItemUtils.getLoreLineIfMatch(stack, FAMILY_PATTERN);
			Matcher alignmentMatcher = ItemUtils.getLoreLineIfMatch(stack, ALIGNMENT_PATTERN);

			if (alignmentMatcher != null) {
				DUMPED_ATTRIBUTES.computeIfPresent(abilityName, (_, o) -> {
					String alignment = WordUtils.capitalizeFully(alignmentMatcher.group("alignment"));
					return new Attribute(o.bazaarName, o.displayName, o.rarity, o.internalName,
							o.abilityName, alignment, o.family, o.shardId);
				});
			}
			if (familyMatcher != null) {
				String family = familyMatcher.group("family");
				List<String> families = DUMPED_ATTRIBUTES.get(abilityName).family();
				Arrays.stream(family.split(",| and ")).map(String::trim).forEach(families::add);
			}
		}
	}

	private static void exportAttributes(AbstractContainerScreen<?> screen) {
		List<Attribute> copy = DUMPED_ATTRIBUTES.values().stream().distinct().sorted(Comparator.comparing(attribute -> {
			if (attribute.shardId.length() == 3) return attribute.shardId;
			return attribute.shardId.charAt(0) + "0" + attribute.shardId.substring(1);
		})).toList();

		CompletableFuture.runAsync(() -> {
			try {
				Files.writeString(ATTRIBUTE_EXPORT_DEST, Attribute.LIST_CODEC.encodeStart(JsonOps.INSTANCE, copy).getOrThrow().toString());
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Attributes Debug] Failed to export attributes!", e);
			}
		}, SkyblockerMod.VIRTUAL_THREAD_EXECUTOR);
	}

	private static @Nullable AbstractContainerScreen<?> checkForAttributeMenu(Screen screen) {
		if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getTitle().getString().endsWith("Attribute Menu")) {
			return containerScreen;
		}
		return null;
	}

	private static @Nullable AbstractContainerScreen<?> checkForHuntingBox(Screen screen) {
		if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getTitle().getString().endsWith("Hunting Box")) {
			return containerScreen;
		}
		return null;
	}

	public record Attribute(String bazaarName, String displayName, String rarity, String internalName,
							String abilityName, String alignment, List<String> family, String shardId) {
		private static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("bazaarName").forGetter(Attribute::bazaarName),
				Codec.STRING.fieldOf("displayName").forGetter(Attribute::displayName),
				Codec.STRING.fieldOf("rarity").forGetter(Attribute::rarity),
				Codec.STRING.fieldOf("internalName").forGetter(Attribute::internalName),
				Codec.STRING.fieldOf("abilityName").forGetter(Attribute::abilityName),
				Codec.STRING.fieldOf("alignment").forGetter(Attribute::alignment),
				Codec.STRING.listOf().fieldOf("family").forGetter(Attribute::family),
				Codec.STRING.fieldOf("shardId").forGetter(Attribute::shardId)
		).apply(instance, Attribute::new));
		public static final Codec<List<Attribute>> LIST_CODEC = CODEC.listOf();
	}

}
