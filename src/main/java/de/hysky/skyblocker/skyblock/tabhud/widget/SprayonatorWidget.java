package de.hysky.skyblocker.skyblock.tabhud.widget;

import de.hysky.skyblocker.skyblock.garden.Sprayonator;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TextIcoTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SprayonatorWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Sprayonator").formatted(Formatting.GOLD, Formatting.BOLD);

	public SprayonatorWidget() { //Todo: Add support for top-aligned pack
		super(TITLE, Formatting.GOLD.getColorValue());
	}

	@Override
	public void updateContent() {
		TableComponent tc = new TableComponent(5, 5, Formatting.GOLD.getColorValue());
		long time = System.currentTimeMillis(); //This should be accurate enough
		for (int row = 0; row < 5; row++) {
			for (int column = 0; column < 5; column++) {
				byte plot = Sprayonator.getPlots()[row][column];
				if (plot == -1) continue;
				MutableText beforeText = Text.literal("");
				if (plot == 0) {
					tc.addToCell(column, row, new PlainTextComponent(beforeText));
					continue;
				}

				Sprayonator.SprayData sd = Sprayonator.getSprayData(plot - 1);
				beforeText.append(Text.literal(String.format("%02d", plot)).formatted(plot == Sprayonator.getCurrentPlot() ? Formatting.GREEN : Formatting.RESET))
						  .append(Text.literal(":"));

				if (sd == null) {
					tc.addToCell(column, row, new TextIcoTextComponent(beforeText, Ico.BARRIER));
					continue;
				}

				long remainingTime = sd.time() + 1800000 - time; //30 minutes in milliseconds
				Formatting timeFormatting = Formatting.GREEN;
				if (remainingTime <= 180000) timeFormatting = Formatting.RED; //3 minutes in milliseconds
				else if (remainingTime <= 600000) timeFormatting = Formatting.GOLD; //10 minutes in milliseconds

				MutableText afterText = Text.literal(String.format("%d:%02d", remainingTime / 60000, (remainingTime / 1000) % 60)).formatted(timeFormatting);

				ItemStack ico = switch (sd.matter()) {
					case "Tasty Cheese" -> Ico.TASTY_CHEESE;
					case "Compost" -> Ico.COMPOST;
					case "Dung" -> Ico.DUNG;
					case "Honey Jar" -> Ico.HONEY_JAR;
					case "Plant Matter" -> Ico.PLANT_MATTER;
					default -> null;
				};
				tc.addToCell(column, row, new TextIcoTextComponent(beforeText, ico, afterText));
			}
		}
		this.addComponent(tc);
	}
}
