package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import de.hysky.skyblocker.skyblock.events.SkyblockEvents;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.time.SkyblockTime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class UpcomingEventsTab implements RecipeTab {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

	private final EventsArea events;

	protected UpcomingEventsTab() {
		List<EventRenderer> renderers = SkyblockEvents.getAllEvents().stream()
				.filter(event -> EventManager.getNext(event, Instant.now(), true).isPresent())
				.map(EventRenderer::new)
				.sorted().toList();

		this.events = new EventsArea(renderers);
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
		graphics.text(CLIENT.font, "Upcoming Events", x + 17, y + 7, CommonColors.WHITE);

		int eventsY = y + 7 + 16;
		events.setPosition(x, eventsY);
		events.extractRenderState(graphics, mouseX, mouseY, delta);

		graphics.disableScissor();
	}

	@Override
	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		if (this.events.hovered != null) {
			graphics.tooltip(CLIENT.font, this.events.hovered.getTooltip(), x, y, DefaultTooltipPositioner.INSTANCE, null);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (this.events.hovered != null && this.events.hovered.getWarpCommand() != null) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown(events.hovered.getWarpCommand(), true);

			return true;
		}

		return events.updateScrolling(click);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		return events.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public ItemStack icon() {
		return CLOCK;
	}

	@Override
	public void updateSearchResults(String query, FilterOption filterOption, boolean refresh) {}

	private static final class EventRenderer implements Comparable<EventRenderer> {
		private static final int HEIGHT = 20;
		private static final int WIDTH = AVAILABLE_WIDTH - 10;
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
				graphics.text(textRenderer, textRenderer.width(formatted) > WIDTH ? ComponentRenderUtils.clipText(formatted, textRenderer, WIDTH) : formatted.getVisualOrderText(), x, y + textRenderer.lineHeight, CommonColors.WHITE);
			}
		}

		private static boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
			return GuiHelper.pointIsInArea(mouseX, mouseY, x, y, x + WIDTH, y + HEIGHT);
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

	private static class EventsArea extends AbstractScrollArea {
		private final List<EventRenderer> renderers;
		private @Nullable EventRenderer hovered;
		private EventsArea(List<EventRenderer> renderers) {
			super(0, 0, AVAILABLE_WIDTH - 4, 120, Component.empty(), AbstractScrollArea.defaultSettings(10));
			this.renderers = renderers;
		}

		@Override
		protected int contentHeight() {
			return renderers.size() * EventRenderer.HEIGHT + 6;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			graphics.enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
			Matrix3x2fStack pose = graphics.pose();
			pose.pushMatrix();
			pose.translate(0.0F, (float)(-this.scrollAmount()));

			int eventsY = getY() + 5;
			this.hovered = null;

			int mouseYScrolled = (int) (mouseY + scrollAmount());
			for (EventRenderer eventRenderer : this.renderers) {
				eventRenderer.extractRenderState(graphics, getX() + 1, eventsY, mouseX, mouseYScrolled);

				//If we're hovering over this event then set it as the hovered one to show a tooltip
				if (isMouseOver(mouseX, mouseY) && EventRenderer.isMouseOver(mouseX, mouseYScrolled, getX() + 1, eventsY)) this.hovered = eventRenderer;

				eventsY += EventRenderer.HEIGHT;
			}

			pose.popMatrix();
			graphics.disableScissor();
			this.extractScrollbar(graphics, mouseX, mouseY);
		}

		@Override
		public int scrollbarWidth() {
			return 4;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {

		}
	}
}
