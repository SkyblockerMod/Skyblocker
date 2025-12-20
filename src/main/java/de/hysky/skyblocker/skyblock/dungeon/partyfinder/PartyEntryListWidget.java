package de.hysky.skyblocker.skyblock.dungeon.partyfinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PartyEntryListWidget extends ContainerObjectSelectionList<PartyEntry> {
	protected List<PartyEntry> partyEntries;

	protected boolean isActive = true;

	private String search = "";

	public static String BASE_SKULL_NBT = """
			{
			"SkullOwner": {
				"Id": [
						1215241996,
						-1849412511,
						-1161255720,
						-889217537
					],
				"Properties": {
				"textures": [
					{
					"Value": "%TEXTURE%"
					}
				]
				}
			}
			}
			""";

	public PartyEntryListWidget(Minecraft minecraftClient, int width, int height, int y, int itemHeight) {
		super(minecraftClient, width, height, y, itemHeight);
	}

	@Override
	public int getRowWidth() {
		return 336;
	}

	public void setEntries(List<PartyEntry> partyEntries) {
		this.partyEntries = partyEntries;
		updateDisplay();
	}

	public void updateDisplay() {
		List<PartyEntry> entries = new ArrayList<>(partyEntries);
		entries.removeIf(partyEntry -> !partyEntry.note.toLowerCase(Locale.ENGLISH).contains(search) && !(partyEntry instanceof PartyEntry.YourParty));
		entries.sort(Comparator.comparing(PartyEntry::isLocked));
		entries.sort(Comparator.comparing(partyEntry -> !(partyEntry instanceof PartyEntry.YourParty)));
		if (entries.isEmpty() && !partyEntries.isEmpty()) {
			entries.add(new PartyEntry.NoParties());
		}
		replaceEntries(entries);
	}

	public void setSearch(String s) {
		search = s.toLowerCase(Locale.ENGLISH);
		updateDisplay();
		refreshScrollAmount();
	}

	@Override
	protected int scrollBarX() {
		return this.width / 2 + getRowWidth() / 2 + 2;
	}


	public void setWidgetActive(boolean active) {
		isActive = active;
	}

	// Mojmap conversion: this is not meant to override isActive
	public boolean isWidgetActive() {
		return isActive;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!visible) return false;
		return super.mouseClicked(click, doubled);
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		//context.drawGuiTexture(BACKGROUND_TEXTURE, x, top-8, getRowWidth()+16+6, bottom-top+16);

		if (children().isEmpty()) {
			Component string = Component.translatable("skyblocker.partyFinder.loadingError");
			Font textRenderer = Minecraft.getInstance().font;
			context.drawWordWrap(textRenderer, string, getRowLeft(), getY() + 10, getRowWidth(), 0xFFFFFFFF, false);
		} else super.renderWidget(context, mouseX, mouseY, delta);
	}

	@Override
	protected void renderListSeparators(GuiGraphics context) {
	}

	@Override
	protected void renderListBackground(GuiGraphics context) {
	}
}
