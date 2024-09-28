package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.ContainerSolverManager;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.networth.NetworthCalculator;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.azureaaron.networth.NetworthResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.regex.Pattern;

public class SalvageHelper extends SimpleContainerSolver {
	/**
	 * Pattern to match dungeon items that are salvageable, using a negative lookahead to exclude dungeon items.
	 */
	private static final Pattern DUNGEON_SALVAGABLE = Pattern.compile("DUNGEON(?! ITEM)");

	public SalvageHelper() {
		super("^Salvage Items");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		Screen currentScreen = MinecraftClient.getInstance().currentScreen;
		if (!(currentScreen instanceof GenericContainerScreen genericContainerScreen)) return List.of();
		return ContainerSolverManager.slotMap(genericContainerScreen.getScreenHandler().slots.subList(genericContainerScreen.getScreenHandler().getRows() * 9, genericContainerScreen.getScreenHandler().slots.size())).int2ObjectEntrySet().stream()
				.filter(entry -> ItemUtils.getLoreLineIfContainsMatch(entry.getValue(), DUNGEON_SALVAGABLE) != null)
				.filter(entry -> isPriceWithinRange(entry.getValue()))
				.map(entry -> ColorHighlight.green(entry.getIntKey() + genericContainerScreen.getScreenHandler().getRows() * 9))
				.toList();
	}

	/**
	 * Checks if the price of the item is within the range of 0 to 100,000 coins, which should be safe to salvage.
	 */
	private boolean isPriceWithinRange(ItemStack stack) {
		NetworthResult result = NetworthCalculator.getItemNetworth(stack);
		return result.price() > 0 && result.price() < 100_000;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().dungeons.salvageHelper;
	}
}
