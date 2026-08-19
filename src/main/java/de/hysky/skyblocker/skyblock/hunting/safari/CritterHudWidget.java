package de.hysky.skyblocker.skyblock.hunting.safari;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Items;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Elements;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;

@RegisterWidget
public class CritterHudWidget extends ElementBasedWidget {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final MutableComponent TITLE = Component.literal("Critters").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
	private static final Component CAVERN_NAME = Component.literal("Cavern").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
	private static final Component FOREST_NAME = Component.literal("Forest").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD);
	private static final Component HAUNTED_NAME = Component.literal("Haunted").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
	private static final Component ICY_NAME = Component.literal("Icy").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
	private static final FlexibleItemStack SNOOZLE_WALL_ITEM = new FlexibleItemStack(Items.COBBLED_DEEPSLATE);

	private static @Nullable CritterHudWidget instance = null;

	public static CritterHudWidget getInstance() {
		if (instance == null) instance = new CritterHudWidget();
		return instance;
	}

	private boolean shouldUpdate = false;

	public CritterHudWidget() {
		super(TITLE, TextColor.GREEN.getValue(), new Information("hud_critters", Component.literal("Critters"), Location.SAFARI));
		instance = this;
		update();
	}

	private Component getDisplayName(SafariUtils.Critters critter) {
		return Component.literal(
				Arrays.stream(critter.name().split("_"))
				.map(word -> Character.toTitleCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ENGLISH))
				.collect(Collectors.joining(" "))
		).withColor(SafariUtils.CRITTER_DETAILS.get(critter).rarity().color);
	}

	private void addDefaultElements() {
		addElement(new PlainTextElement(Component.literal("Enter a biome!")));
	}

	private void addListCritter(SafariUtils.Critters critter, int count, boolean forceSnoozle) {
		if (critter == SafariUtils.Critters.SNOOZLE) {
			// if all walls broken and there's a nearby snoozle it should show correctly
			if (forceSnoozle && count != 0) {
				addElement(Elements.iconTextComponent(SafariUtils.CRITTER_DETAILS.get(critter).head(), Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter))));
			} else {
				addElement(Elements.iconTextComponent(SNOOZLE_WALL_ITEM, Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter)).append(Component.literal(" wall"))));
			}
		} else if (count == 0) {
			addElement(Elements.iconTextComponent(SafariUtils.CRITTER_DETAILS.get(critter).head(), getDisplayName(critter)));
		} else {
			addElement(Elements.iconTextComponent(SafariUtils.CRITTER_DETAILS.get(critter).head(), Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter))));
		}
	}

	private void addListCritter(SafariUtils.Critters critter, int count) {
		addListCritter(critter, count, false);
	}

	private void addBiomeElements(Component name, EnumSet<SafariUtils.Critters> critters) {
		addElement(new PlainTextElement(Component.literal("Biome: ").append(name)));

		// show only sparkling critters if any are found
		var sparkling = SafariCritters.getSparklings();
		if (sparkling != null) {
			addElement(new PlainTextElement(Component.literal("SPARKLING" + (sparkling.size() == 1 ? "" : "S")).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD).append(Component.literal(" found:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))));
			for (SafariUtils.Critters critter : sparkling.keySet()) {
				addListCritter(critter, sparkling.get(critter));
			}
			return;
		}

		var uniques = EnumSet.noneOf(SafariUtils.Critters.class);
		var remaining = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		var nearby = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		for (SafariUtils.Critters critter : critters) {
			if (SafariCritters.hasUnique(critter)) {
				int count = critter == SafariUtils.Critters.SNOOZLE ? SafariCritters.getMinimum(critter) : SafariCritters.getMinimum(critter) - SafariCritters.getCaught(critter);
				int near = SafariCritters.getNearby(critter);
				if (count > 0) remaining.put(critter, count);
				if (near > 0) nearby.put(critter, near);
			} else {
				uniques.add(critter);
			}
		}
		// show full critter list when reasonably small instead of a simple count
		boolean shouldFullDisplay = Stream.concat(
				Stream.concat(uniques.stream(), remaining.keySet().stream()),
				nearby.keySet().stream()
		).distinct().count() <= 5;
		long minimum = Stream.concat(uniques.stream(), remaining.keySet().stream()).distinct().count();

		if (!uniques.isEmpty()) {
			if (shouldFullDisplay) {
				addElement(new PlainTextElement(Component.literal("Missing:")));
				for (SafariUtils.Critters critter : uniques) {
					addListCritter(critter, critter == SafariUtils.Critters.SNOOZLE ? SafariCritters.getMinimum(critter) : 0);
				}
			} else {
				addElement(new PlainTextElement(Component.literal("Unique catches: " + (critters.size() - uniques.size()) + "/" + critters.size())));
			}
		}
		if (remaining.isEmpty()) {
			if (uniques.isEmpty()) {
				addElement(new PlainTextElement(Component.literal("Minimum catches: ").append(Component.literal("Done!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))));

				if (nearby.isEmpty()) {
					addElement(new PlainTextElement(Component.literal("Nearby: ").append(Component.literal("None!").withStyle(ChatFormatting.GRAY))));
				} else {
					addElement(new PlainTextElement(Component.literal("Nearby: ")));
					for (SafariUtils.Critters critter : nearby.keySet()) {
						addListCritter(critter, nearby.get(critter));
					}
				}
			} else {
				addElement(new PlainTextElement(Component.literal("Minimum catches: " + (critters.size() - minimum) + "/" + critters.size())));
			}
		} else if (shouldFullDisplay) {
			addElement(new PlainTextElement(Component.literal("Remaining:")));
			Stream.concat(remaining.keySet().stream(), nearby.keySet().stream()).distinct().forEach(critter -> {
				int near = nearby.getOrDefault(critter, 0);
				int count = remaining.getOrDefault(critter, 0);
				addListCritter(critter, Math.max(near, count), near > count);
			});
		} else {
			addElement(new PlainTextElement(Component.literal("Minimum catches: " + (critters.size() - minimum) + "/" + critters.size())));
		}
	}

	public void enableUpdate() {
		shouldUpdate = true;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		// Only update if information has changed.
		if (shouldUpdate) {
			shouldUpdate = false;
			return true;
		}
		return false;
	}

	@Override
	public void updateContent() {
		if (MINECRAFT.player == null || MINECRAFT.level == null || !Utils.isInSafari()) {
			addDefaultElements();
		} else if (SafariUtils.isInCavernBiome()) {
			addBiomeElements(CAVERN_NAME, SafariUtils.CAVERN_CRITTERS);
		} else if (SafariUtils.isInForestBiome()) {
			addBiomeElements(FOREST_NAME, SafariUtils.FOREST_CRITTERS);
		} else if (SafariUtils.isInHauntedBiome()) {
			addBiomeElements(HAUNTED_NAME, SafariUtils.HAUNTED_CRITTERS);
		} else if (SafariUtils.isInIcyBiome()) {
			addBiomeElements(ICY_NAME, SafariUtils.ICY_CRITTERS);
		} else {
			addDefaultElements();
		}
	}

}
