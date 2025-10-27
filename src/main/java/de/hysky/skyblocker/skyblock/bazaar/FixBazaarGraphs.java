package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class FixBazaarGraphs extends SimpleContainerSolver {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Identifier FIX_BAZAAR_GRAPHS_PHASE = SkyblockerMod.id("fix-bazaar-graphs");
	/**
	 * Temporarily holds/caches the fixed lore for the graphs on screen.
	 * Cleared when marked dirty (items are changed), or on reset (screen is closed).
	 */
	private static final Object2ObjectOpenHashMap<ItemStack, List<Text>> FIXED_LORE_MAP = new Object2ObjectOpenHashMap<>();

	@Nullable
	private GenericContainerScreen screen = null;

	@Override
	public void start(GenericContainerScreen newScreen) {
		screen = newScreen;
	}

	@Override
	public void markDirty() {
		FIXED_LORE_MAP.clear();
	}

	@Override
	public void reset() {
		screen = null;
		FIXED_LORE_MAP.clear();
	}

	public FixBazaarGraphs() {
		super(Pattern.compile("^.* ➜ Graphs$"));
		// Register this before the default phase since it clears all the lore lines.
		ItemTooltipCallback.EVENT.addPhaseOrdering(FIX_BAZAAR_GRAPHS_PHASE, Event.DEFAULT_PHASE);
		// This runs on every frame that the item is hovered :(
		ItemTooltipCallback.EVENT.register(FIX_BAZAAR_GRAPHS_PHASE, (stack, tooltipContext, tooltipType, lines) -> {
			if (CLIENT.currentScreen == null || CLIENT.currentScreen != screen) return;
			if (!stack.isOf(Items.PAPER)) return;
			List<Text> newLines = FIXED_LORE_MAP.computeIfAbsent(stack, (_s) -> fixLore(lines));
			lines.clear();
			lines.addAll(newLines);
		});
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}

	private List<Text> fixLore(List<Text> loreLines) {
		List<Text> fixedLore = new ArrayList<>();
		loreLines.stream().map(this::fixLine).forEach(fixedLore::add);
		fixedLore.add(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.fixBazaarGraphs.loreMessage")).styled(style -> style.withItalic(false).withColor(Formatting.WHITE)));
		return fixedLore;
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
					isValid = true;
				}
				case '+': {
					color = Formatting.WHITE;
					break;
				}
				case 'x': {
					color = Formatting.AQUA;
					break;
				}
				case '·': { // U+B7
					asString = asString.replace("·", " ·");
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
