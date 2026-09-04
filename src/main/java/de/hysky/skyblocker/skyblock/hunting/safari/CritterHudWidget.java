package de.hysky.skyblocker.skyblock.hunting.safari;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;

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
	private static final Component CAVERN_NAME = Component.translatable("skyblocker.config.hunting.safari.critterHud.biome.cavern").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
	private static final Component FOREST_NAME = Component.translatable("skyblocker.config.hunting.safari.critterHud.biome.forest").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD);
	private static final Component HAUNTED_NAME = Component.translatable("skyblocker.config.hunting.safari.critterHud.biome.haunted").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
	private static final Component ICY_NAME = Component.translatable("skyblocker.config.hunting.safari.critterHud.biome.icy").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
	private static final FlexibleItemStack SNOOZLE_WALL_ITEM = new FlexibleItemStack(Items.COBBLED_DEEPSLATE);
	private static final FlexibleItemStack HONEYBUG_NEST_ITEM = new FlexibleItemStack(Items.BEE_NEST);

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
		addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.biome.none")));
	}

	private void addListCritter(SafariUtils.Critters critter, int count, boolean showLocation) {
		// TODO: Figure out how to read translatable text so I can parse multi-component ones!
		if (critter == SafariUtils.Critters.SNOOZLE && showLocation) {
			addElement(Elements.iconTextComponent(SNOOZLE_WALL_ITEM, Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter)).append(Component.literal(" wall"))));
		} else if (critter == SafariUtils.Critters.HONEYBUG && showLocation) {
			addElement(Elements.iconTextComponent(HONEYBUG_NEST_ITEM, Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter)).append(Component.literal(" nest"))));
		} else if (count == 0) {
			addElement(Elements.iconTextComponent(SafariUtils.CRITTER_DETAILS.get(critter).head(), getDisplayName(critter)));
		} else {
			addElement(Elements.iconTextComponent(SafariUtils.CRITTER_DETAILS.get(critter).head(), Component.literal(count + "x ").withStyle(ChatFormatting.GRAY).append(getDisplayName(critter))));
		}
	}

	private void addBiomeElements(Component name, EnumSet<SafariUtils.Critters> critters) {
		addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.biome").append(name)));

		// Show only sparkling critters if any are found
		var sparkling = SafariCritters.getSparklings(critters);
		if (sparkling != null) {
			addElement(new PlainTextElement(Component.literal("SPARKLING").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD).append(Component.literal(" found:").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))));
			for (SafariUtils.Critters critter : sparkling.keySet()) {
				addListCritter(critter, sparkling.get(critter), false);
			}
			return;
		}

		int unknownSnoozles = SafariCritters.getUnknownSnoozles();
		int unknownHoneybugs = SafariCritters.getUnknownHoneybugs();
		var uniques = EnumSet.noneOf(SafariUtils.Critters.class);
		var remaining = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		var nearby = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		for (SafariUtils.Critters critter : critters) {
			int near = SafariCritters.getNearby(critter);
			if (near > 0) nearby.put(critter, near);

			if (SafariCritters.hasUnique(critter)) {
				int count = critter == SafariUtils.Critters.SNOOZLE ? SafariCritters.getMinimum(critter) : SafariCritters.getMinimum(critter) - SafariCritters.getCaught(critter);
				if (count > 0) remaining.put(critter, count);
			} else {
				uniques.add(critter);
			}
		}

		// Show full itemized list instead of simple counts when reasonably small
		boolean showFullDisplay = ((!SafariUtils.isInCavernBiome() || unknownSnoozles == 0 ? 0 : 1)
				+ (!SafariUtils.isInForestBiome() || unknownHoneybugs == 0 ? 0 : 1)
				+ uniques.size()
				+ remaining.size()
				+ nearby.size()) <= 6;

		if (uniques.isEmpty()) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.unique").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.done").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))));
		} else if (showFullDisplay) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.unique")));
			for (SafariUtils.Critters critter : uniques) {
				addListCritter(critter, critter == SafariUtils.Critters.SNOOZLE ? SafariCritters.getMinimum(critter) : 0, true);
			}
		} else {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.unique").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.critters", critters.size() - uniques.size(), critters.size()).withStyle(ChatFormatting.GRAY))));
		}

		// Show unknown snoozle walls
		if (SafariUtils.isInCavernBiome()) {
			int unknown = SafariCritters.getUnknownSnoozles();
			if (unknown != 0) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.unknown")));
				addListCritter(SafariUtils.Critters.SNOOZLE, unknown, true);
			}
		// Show unknown honeybug nests
		} else if (SafariUtils.isInForestBiome()) {
			int unknown = SafariCritters.getUnknownHoneybugs();
			if (unknown != 0) {
				addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.unknown")));
				addListCritter(SafariUtils.Critters.HONEYBUG, unknown, true);
			}
		}

		long minimum = uniques.size() + remaining.size();
		if (minimum == 0) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.minimum").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.done").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))));
		} else if (remaining.isEmpty() || !showFullDisplay) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.minimum").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.critters", critters.size() - minimum, critters.size()).withStyle(ChatFormatting.GRAY))));
		} else {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.minimum")));
			for (SafariUtils.Critters critter : remaining.keySet()) {
				addListCritter(critter, remaining.get(critter), true);
			}
		}

		if (nearby.isEmpty()) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.nearby").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.none").withStyle(ChatFormatting.GRAY))));
		} else if (showFullDisplay) {
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.nearby")));
			for (SafariUtils.Critters critter : nearby.keySet()) {
				addListCritter(critter, nearby.get(critter), false);
			}
		} else {
			int total = nearby.values().stream().reduce(0, Integer::sum);
			addElement(new PlainTextElement(Component.translatable("skyblocker.config.hunting.safari.critterHud.header.nearby").append(Component.translatable("skyblocker.config.hunting.safari.critterHud.value.critters." + (total == 1 ? "singular" : "plural"), total).withStyle(ChatFormatting.GRAY))));
		}
	}

	public void enableUpdate() {
		shouldUpdate = true;
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		// Only update if information has changed
		if (shouldUpdate) {
			shouldUpdate = false;
			return true;
		}
		return false;
	}

	// TODO: Use translatable components?
	@Override
	public void updateContent() {
		if (MINECRAFT.player == null || MINECRAFT.level == null || !Utils.isInSafari() || SafariUtils.isInSpawn(MINECRAFT.player)) {
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
