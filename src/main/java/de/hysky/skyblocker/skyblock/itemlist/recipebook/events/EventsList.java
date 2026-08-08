package de.hysky.skyblocker.skyblock.itemlist.recipebook.events;

import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

public class EventsList extends ContainerObjectSelectionList<EventEntry> {
	private @Nullable SkyblockEvent autoFetch;
	private boolean fetch = false;

	public EventsList(Minecraft minecraft, int width, int height) {
		super(minecraft, width, height, 0, 21);
	}

	public void autoFetch(@Nullable SkyblockEvent event) {
		this.autoFetch = event;
		fetch = autoFetch != null;
		clearEntries();
		refreshScrollAmount();
	}

	public @Nullable SkyblockEvent autoFetch() {
		return this.autoFetch;
	}

	@Override
	public void setScrollAmount(double scrollAmount) {
		super.setScrollAmount(scrollAmount);
		while (fetch && autoFetch != null && maxScrollAmount() - scrollAmount < 10) {
			EventInstance last = children().isEmpty() ? null : children().getLast().getInstance();
			Optional<EventInstance> next = EventManager.getNext(autoFetch, last == null ? Instant.now() : last.end(), false);
			if (next.isPresent()) {
				addEntry(new EventEntry.Static(minecraft, next.get()));
			} else {
				fetch = false;
			}
		}
	}

	@Override
	public int getRowLeft() {
		return getX() + 1;
	}

	@Override
	protected int scrollBarX() {
		return getRowRight() + 2;
	}

	@Override
	protected void extractListBackground(GuiGraphicsExtractor graphics) {}

	@Override
	protected void extractListSeparators(GuiGraphicsExtractor graphics) {}

	@Override
	public int getRowWidth() {
		return 120;
	}

	@Override
	public int scrollbarWidth() {
		return 4;
	}

	@Override
	public void setPosition(int x, int y) {
		boolean reposition = x != getX() || y != getY();
		super.setPosition(x, y);
		if (reposition) refreshScrollAmount();
	}

	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		if (getHovered() != null) {
			getHovered().extractTooltip(graphics, x, y);
		}
	}
}
