package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class FixBazaarGraphs extends SimpleContainerSolver {
	boolean hasRun = false;

	@Override
	public void start(GenericContainerScreen screen) {
		hasRun = false;
	}

	@Override
	public void markDirty() {
		hasRun = false;
	}

	public FixBazaarGraphs() {
		super(Pattern.compile("^([\\w ])+➜ Graphs$"));
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		if (hasRun) return List.of();
		hasRun = true;
		slots.int2ObjectEntrySet().stream()
				.filter(entry -> entry.getValue().isOf(Items.PAPER))
				.forEach(entry -> fixLore(entry.getValue()));
		return List.of();
	}

	private void fixLore(ItemStack stack) {
		List<Text> lore = ItemUtils.getLore(stack);
		if (lore.getLast().getString().contains(Constants.PREFIX.get().getString())) return;

		List<Text> fixedLore = new ArrayList<>();
		lore.stream().map(this::fixLine).forEach(fixedLore::add);
		fixedLore.add(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.fixBazaarGraphs.loreMessage")).styled(style -> style.withItalic(false).withColor(Formatting.WHITE)));

		stack.set(DataComponentTypes.LORE, new LoreComponent(fixedLore));
	}

	private Text fixLine(Text line) {
		BazaarChartVisitor visitor = new BazaarChartVisitor();
		line.visit(visitor);
		return visitor.getText().orElse(line);
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.fixBazaarGraphs;
	}

	private static class BazaarChartVisitor implements StringVisitable.Visitor<List<Text>> {
		MutableText text = Text.empty();
		boolean isValid = false;

		@Override
		public Optional<List<Text>> accept(String asString) {
			if (!isValid && !asString.startsWith("│")) return Optional.empty();

			Formatting color = Formatting.DARK_GRAY;

			switch (asString.charAt(0)) {
				case '│': { // U+2502
					asString = asString.replace("│", "│ ");
					isValid = true;
				}
				case '+': {
					if (asString.length() == 1) asString = asString.replace("+", "+ ");
					color = Formatting.WHITE;
					break;
				}
				case 'x': {
					color = Formatting.AQUA;
					break;
				}
				case '·': { // U+B7
					asString = asString.replace("·", "· ");
					break;
				}
				default: {
					if (asString.endsWith("h") || asString.endsWith("│")) {
						color = Formatting.WHITE;
					} else {
						color = Formatting.GOLD;
					}
					break;
				}
			}

			Formatting finalColor = color;
			text.append(Text.literal(asString).styled(style ->
					style.withColor(finalColor).withItalic(false)));

			return Optional.empty();
		}

		public Optional<Text> getText() {
			if (!isValid) return Optional.empty();
			return Optional.of(this.text);
		}
	}
}
