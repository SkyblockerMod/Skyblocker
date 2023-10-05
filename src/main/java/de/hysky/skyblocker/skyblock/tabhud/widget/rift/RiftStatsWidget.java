package de.hysky.skyblocker.skyblock.tabhud.widget.rift;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.Widget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.TableComponent;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RiftStatsWidget extends Widget {

	private static final MutableText TITLE = Text.literal("Stats").formatted(Formatting.DARK_AQUA, Formatting.BOLD);

	public RiftStatsWidget() {
		super(TITLE, Formatting.DARK_AQUA.getColorValue());
    }

    @Override
    public void updateContent() {
		Text riftDamage = Widget.simpleEntryText(64, "RDG", Formatting.DARK_PURPLE);
		IcoTextComponent rdg = new IcoTextComponent(Ico.DIASWORD, riftDamage);

		Text speed = Widget.simpleEntryText(65, "SPD", Formatting.WHITE);
		IcoTextComponent spd = new IcoTextComponent(Ico.SUGAR, speed);

		Text intelligence = Widget.simpleEntryText(66, "INT", Formatting.AQUA);
		IcoTextComponent intel = new IcoTextComponent(Ico.ENCHANTED_BOOK, intelligence);

		Text manaRegen = Widget.simpleEntryText(67, "MRG", Formatting.AQUA);
		IcoTextComponent mrg = new IcoTextComponent(Ico.DIAMOND, manaRegen);

		TableComponent tc = new TableComponent(2, 2, Formatting.AQUA.getColorValue());
		tc.addToCell(0, 0, rdg);
		tc.addToCell(0, 1, spd);
		tc.addToCell(1, 0, intel);
		tc.addToCell(1, 1, mrg);

		this.addComponent(tc);
	}

}