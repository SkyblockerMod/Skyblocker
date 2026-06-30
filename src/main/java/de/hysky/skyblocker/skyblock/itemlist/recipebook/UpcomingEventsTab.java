package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.events.ExtraEventData;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.time.SkyblockTime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UpcomingEventsTab implements RecipeTab {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

	private final List<EventRenderer> events = new ArrayList<>();
	private @Nullable EventRenderer hovered = null;

	protected UpcomingEventsTab() {
		List<EventRenderer> renderers = SkyblockEvents.getAllEvents().stream().map(EventRenderer::new).sorted().toList();

		this.events.addAll(renderers);
	}

	@Override
	public void initialize(Minecraft client, int parentLeft, int parentTop) {}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float delta) {
		x += 9;
		y += 9;

		//Prevent things from going outside of the recipe book window
		graphics.enableScissor(x, y, x + 131, y + 150);

		//Draw the title area
		graphics.fakeItem(CLOCK, x, y + 4);
		graphics.text(CLIENT.font, "Upcoming Events", x + 17, y + 7, CommonColors.WHITE);

		int eventsY = y + 7 + 24;
		this.hovered = null;

		for (EventRenderer eventRenderer : this.events) {
			eventRenderer.extractRenderState(graphics, x + 1, eventsY, mouseX, mouseY);

			//If we're hovering over this event then set it as the hovered one to show a tooltip
			if (GuiHelper.pointIsInArea(mouseX, mouseY, x, y, x + 131, y + 150) && EventRenderer.isMouseOver(mouseX, mouseY, x + 1, eventsY)) this.hovered = eventRenderer;

			eventsY += EventRenderer.HEIGHT;
		}

		graphics.disableScissor();
	}

	@Override
	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		if (this.hovered != null) {
			graphics.tooltip(CLIENT.font, this.hovered.getTooltip(), x, y, DefaultTooltipPositioner.INSTANCE, null);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (this.hovered != null && this.hovered.getWarpCommand() != null) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown(hovered.getWarpCommand(), true);

			return true;
		}

		return false;
	}

	@Override
	public ItemStack icon() {
		return CLOCK;
	}

	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {}

	private static final class EventRenderer implements Comparable<EventRenderer> {
		private static final int HEIGHT = 20;
		private final SkyblockEvent event;
		private @Nullable EventInstance currentInstance;

		private EventRenderer(SkyblockEvent event) {
			this.event = event;
			currentInstance = EventManager.getNext(event, Instant.now(), true).orElse(null);
		}

		private void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
			Instant time = Instant.now();
			Font textRenderer = CLIENT.font;

			graphics.text(textRenderer, Component.literal(event.name()).withStyle(Style.EMPTY.withUnderlined(isMouseOver(mouseX, mouseY, x, y))), x, y, CommonColors.WHITE);

			if (currentInstance != null && time.isAfter(currentInstance.end()))
				currentInstance = EventManager.getNext(event, Instant.now(), true).orElse(null);

			if (currentInstance == null) {
				graphics.text(textRenderer, Component.literal(" ").append(Component.translatable("skyblocker.events.tab.noMore")), x, y + textRenderer.lineHeight, CommonColors.GRAY, false);
			} else {
				EventInstance event = currentInstance;
				Component formatted;
				if (event.start().isAfter(time)) {
					formatted = Component.literal(" ").append(Component.translatable("skyblocker.events.tab.startsIn", SkyblockTime.formatTime(time.until(event.start()).toSeconds()))).withStyle(ChatFormatting.YELLOW);
				} else {
					formatted = Component.literal(" ").append(Component.translatable("skyblocker.events.tab.endsIn", SkyblockTime.formatTime(time.until(event.end()).toSeconds())).withStyle(ChatFormatting.GREEN));
				}
				graphics.text(textRenderer, formatted, x, y + textRenderer.lineHeight, CommonColors.WHITE);
			}
		}

		private static boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
			return GuiHelper.pointIsInArea(mouseX, mouseY, x, y, x + 131, y + HEIGHT);
		}

		private List<ClientTooltipComponent> getTooltip() {
			EventInstance event = currentInstance;
			if (event == null) return List.of();
			return currentInstance.createTooltip();
		}

		private @Nullable String getWarpCommand() {
			return currentInstance != null ? currentInstance.additionalInfo().warpCommand().orElse(null) : null;
		}

		@Override
		public int compareTo(EventRenderer o) {
			if (currentInstance == null) {
				return (o.currentInstance == null) ? 0 : 1;
			} else if (o.currentInstance == null) {
				return -1;
			} else {
				return currentInstance.start().compareTo(o.currentInstance.start());
			}
		}
	}
}
