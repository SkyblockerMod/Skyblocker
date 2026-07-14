package de.hysky.skyblocker.skyblock.itemlist.recipebook.events;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.FilterOption;
import de.hysky.skyblocker.skyblock.itemlist.recipebook.RecipeTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.Instant;

public class UpcomingEventsTab implements RecipeTab {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

	private final EventsList eventsList = new EventsList(CLIENT, AVAILABLE_WIDTH, AVAILABLE_HEIGHT - 25);
	private boolean backButtonHovered = false;

	public UpcomingEventsTab() {
		setContentsToAllEvents();
	}

	private void setContentsToAllEvents() {
		eventsList.autoFetch(null);
		eventsList.replaceEntries(SkyblockEvents.getAllEvents().stream()
				.filter(event -> EventManager.getNext(event, Instant.now(), true).isPresent())
				.map(event -> new EventEntry.AutoUpdate(CLIENT, event, this::setContentsToThisEvent))
				.sorted()
				.map(EventEntry.class::cast) // well this is stupid
				.toList());
	}

	private void setContentsToThisEvent(SkyblockEvent event) {
		eventsList.autoFetch(event);
	}

	@Override
	public void initialize(Minecraft client, int parentLeft, int parentTop) {}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
		x += EDGE;
		y += EDGE;

		//Prevent things from going outside the recipe book window
		graphics.enableScissor(x, y, x + AVAILABLE_WIDTH, y + AVAILABLE_HEIGHT);

		//Draw the title area
		graphics.fakeItem(CLOCK, x, y + 4);
		String upcoming;
		SkyblockEvent autoFetch = eventsList.autoFetch();
		if (autoFetch != null) {
			upcoming = autoFetch.name() + "s";
		} else {
			upcoming = "Events";
		}
		Component title = Component.literal("Upcoming " + upcoming);
		int titleX = x + 17;
		if (CLIENT.font.width(title) > AVAILABLE_WIDTH - 17 - 2) {
			graphics.textRenderer().acceptScrollingWithDefaultCenter(title, titleX, x + AVAILABLE_WIDTH - 2, y + 6, y + 15);
		} else graphics.textRenderer().accept(titleX, y + 7, title);

		int eventsY = y + 7 + 16;
		eventsList.setPosition(x, eventsY);
		eventsList.extractRenderState(graphics, mouseX, mouseY, delta);

		if (mouseY > y + 4 && mouseY < eventsY - 3 && eventsList.autoFetch() != null) {
			graphics.setTooltipForNextFrame(Component.literal("Go Back"), mouseX, mouseY);
			graphics.requestCursor(CursorTypes.POINTING_HAND);
			backButtonHovered = true;
		} else backButtonHovered = false;

		graphics.disableScissor();
	}

	@Override
	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		eventsList.extractTooltip(graphics, x, y);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (backButtonHovered) {
			eventsList.autoFetch(null);
			AbstractWidget.playButtonClickSound(CLIENT.getSoundManager());
			setContentsToAllEvents();
			return true;
		}
		return eventsList.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		return eventsList.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		return eventsList.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return eventsList.mouseReleased(event);
	}

	@Override
	public ItemStack icon() {
		return CLOCK;
	}

	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {}
}
