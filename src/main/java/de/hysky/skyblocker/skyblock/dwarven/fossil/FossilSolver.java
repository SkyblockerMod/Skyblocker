package de.hysky.skyblocker.skyblock.dwarven.fossil;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dwarven.fossil.Structures.TileGrid;
import de.hysky.skyblocker.skyblock.item.tooltip.adders.LineSmoothener;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.hysky.skyblocker.skyblock.dwarven.fossil.FossilCalculations.*;

public class FossilSolver extends SimpleContainerSolver implements TooltipAdder {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("Fossil Excavation Progress: (\\d{1,2}.\\d)%");
	private static final Pattern CHARGES_PATTERN = Pattern.compile("Chisel Charges Remaining: (\\d{1,2})");

	private static String percentage;
	private static double[] probability;
	private static int chiselLeft = -1;

	public FossilSolver() {
		super("Fossil Excavator");
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		//convert to container
		TileGrid mainTileGrid = convertItemsToTiles(slots);
		//get how many chisels the player has left
		if (!slots.isEmpty()) {
			chiselLeft = getChiselLeft(slots.values().stream().findAny().get());
		} else {
			chiselLeft = -1;
		}
		//get the fossil chance percentage
		if (percentage == null) {
			percentage = getFossilPercentage(slots);
		}
		//get chance for each
		probability = getFossilChance(mainTileGrid, percentage);
		//get the highlight amount and return
		return convertChanceToColor(probability);
	}

	/**
	 * Checks the tooltip of an item to see how many chisel uses a player has left
	 *
	 * @param itemStack item to use to check the tooltip
	 * @return how many chisels are left and "-1" if not found
	 */
	private int getChiselLeft(ItemStack itemStack) {
		for (Text line : itemStack.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.BASIC)) {
			Matcher matcher = CHARGES_PATTERN.matcher(line.getString());
			if (matcher.find()) {
				return Integer.parseInt(matcher.group(1));
			}
		}
		return -1;
	}

	/**
	 * See if there is any found fossils then see if there is a fossil chance percentage in the tooltip
	 *
	 * @param slots items to check tooltip of
	 * @return null if there is none or the value of the percentage
	 */
	private String getFossilPercentage(Int2ObjectMap<ItemStack> slots) {
		for (ItemStack item : slots.values()) {
			for (Text line : item.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, TooltipType.BASIC)) {
				Matcher matcher = PERCENTAGE_PATTERN.matcher(line.getString());
				if (matcher.matches()) {
					return matcher.group(1);
				}
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().mining.glacite.fossilSolver;
	}

	/**
	 * Converts tile probability into a color for each of the remaining tiles
	 * @param chances the probability in order from 0-1
	 * @return the colors formated for the {@link SimpleContainerSolver#getColors(Int2ObjectMap)}
	 */
	private static List<ColorHighlight> convertChanceToColor(double[] chances) {
		List<ColorHighlight> outputColors = new ArrayList<>();
		Color gradientColor = Color.BLUE;
		//loop though all the chance values and set the color to match probability. full color means that its 100%
		OptionalDouble highProbability = Arrays.stream(chances).max();
		for (int i = 0; i < chances.length; i++) {
			double chance = chances[i];
			if (Double.isNaN(chances[i]) || chances[i] == 0) {
				continue;
			}
			if (chances[i] == highProbability.getAsDouble()) {
				outputColors.add(ColorHighlight.green(i));
				continue;
			}
			outputColors.add(new ColorHighlight(i, (int) (chance * 255) << 24 | gradientColor.getRed() << 16 | gradientColor.getGreen() << 8 | gradientColor.getBlue()));
		}
		return outputColors;
	}

	/**
	 * Add solver info to tooltips
	 *
	 * @param focusedSlot the slot focused by the player
	 * @param stack       unused
	 * @param lines       the lines for the tooltip
	 */
	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) { //todo translatable
		//only add if fossil or dirt
		if (stack.getItem() != Items.GRAY_STAINED_GLASS_PANE && stack.getItem() != Items.BROWN_STAINED_GLASS_PANE) {
			return;
		}
		//add spacer
		lines.add(LineSmoothener.createSmoothLine());

		//if no permutation say this instead of other stats
		if (permutations == 0) {
			lines.add(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.toolTip.noFossilFound").formatted(Formatting.GOLD));
			return;
		}

		//add permutation count
		lines.add(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.toolTip.possiblePatterns").append(Text.literal(String.valueOf(permutations)).formatted(Formatting.YELLOW)));
		//add minimum tiles left count
		lines.add(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.toolTip.minimumTilesLeft").append(Text.literal(String.valueOf(minimumTiles)).formatted(chiselLeft >= minimumTiles ? Formatting.YELLOW : Formatting.RED)));
		//add probability if available and not uncovered
		if (focusedSlot != null && probability != null && probability.length > focusedSlot.getIndex() && stack.getItem() == Items.BROWN_STAINED_GLASS_PANE) {
			lines.add(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.toolTip.probability").append(Text.literal(Math.round(probability[focusedSlot.getIndex()] * 100) + "%").formatted(Formatting.YELLOW)));
		}
		//if only 1 type of fossil left and a fossil is partially uncovered add the fossil name
		if (fossilName != null && percentage != null) {
			lines.add(Text.translatable("skyblocker.config.mining.glacite.fossilSolver.toolTip.foundFossil").append(Text.literal(fossilName).formatted(Formatting.YELLOW)));
		}
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void reset() {
		chiselLeft = -1;
		percentage = null;
	}


}
