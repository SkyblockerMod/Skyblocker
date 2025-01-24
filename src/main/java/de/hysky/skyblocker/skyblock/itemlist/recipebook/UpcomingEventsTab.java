package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.hysky.skyblocker.mixins.accessors.DrawContextInvoker;
import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public class UpcomingEventsTab implements RecipeTab {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

	private final List<EventRenderer> events = new ArrayList<>();
	private EventRenderer hovered = null;

	protected UpcomingEventsTab() {
		List<EventRenderer> renderers = EventNotifications.getEvents().entrySet().stream()
				.sorted(Comparator.comparingLong(a -> a.getValue().isEmpty() ? Long.MAX_VALUE : a.getValue().peekFirst().start()))
				.map(stringLinkedListEntry -> new EventRenderer(stringLinkedListEntry.getKey(), stringLinkedListEntry.getValue()))
				.toList();

		this.events.addAll(renderers);
	}

	@Override
	public void initialize(MinecraftClient client, int parentLeft, int parentTop) {}

	@Override
	public void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta) {
		x += 9;
		y += 9;

		//Prevent things from going outside of the recipe book window
		context.enableScissor(x, y, x + 131, y + 150);

		//Draw the title area
		context.drawItemWithoutEntity(CLOCK, x, y + 4);
		context.drawTextWithShadow(CLIENT.textRenderer, "Upcoming Events", x + 17, y + 7, -1);

		int eventsY = y + 7 + 24;
		this.hovered = null;

		for (EventRenderer eventRenderer : this.events) {
			eventRenderer.render(context, x + 1, eventsY, mouseX, mouseY);

			//If we're hovering over this event then set it as the hovered one to show a tooltip
			if (RenderHelper.pointIsInArea(mouseX, mouseY, x, y, x + 131, y + 150) && EventRenderer.isMouseOver(mouseX, mouseY, x + 1, eventsY)) this.hovered = eventRenderer;

			eventsY += EventRenderer.HEIGHT;
		}

		context.disableScissor();
	}

	@Override
	public void drawTooltip(DrawContext context, int x, int y) {
		if (this.hovered != null) {
			((DrawContextInvoker) context).invokeDrawTooltip(CLIENT.textRenderer, this.hovered.getTooltip(), x, y, HoveredTooltipPositioner.INSTANCE, null);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

	@Override
	public void initializeSearchResults(String query) {}

	private static class EventRenderer {
		private static final int HEIGHT = 20;

		private final String eventName;
		private final LinkedList<EventNotifications.SkyblockEvent> events;

		private EventRenderer(String eventName, LinkedList<EventNotifications.SkyblockEvent> events) {
			this.eventName = eventName;
			this.events = events;
		}

		private void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
			long time = System.currentTimeMillis() / 1000;
			TextRenderer textRenderer = CLIENT.textRenderer;

			context.drawTextWithShadow(textRenderer, Text.literal(eventName).fillStyle(Style.EMPTY.withUnderline(isMouseOver(mouseX, mouseY, x, y))), x, y, -1);

			if (events.isEmpty()) {
				context.drawText(textRenderer, Text.literal(" ").append(Text.translatable("skyblocker.events.tab.noMore")), x, y + textRenderer.fontHeight, Colors.GRAY, false);
			} else if (events.peekFirst().start() > time) {
				Text formatted = Text.literal(" ").append(Text.translatable("skyblocker.events.tab.startsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() - time)))).formatted(Formatting.YELLOW);

				context.drawTextWithShadow(textRenderer, formatted, x, y + textRenderer.fontHeight, -1);
			} else {
				Text formatted = Text.literal(" ").append(Text.translatable( "skyblocker.events.tab.endsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() + events.peekFirst().duration() - time)))).formatted(Formatting.GREEN);

				context.drawTextWithShadow(textRenderer, formatted, x, y + textRenderer.fontHeight, -1);
			}
		}

		private static boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
			return RenderHelper.pointIsInArea(mouseX, mouseY, x, y, x + 131, y + HEIGHT);
		}

		private List<TooltipComponent> getTooltip() {
			List<TooltipComponent> components = new ArrayList<>();

			if (events.peekFirst() == null) return components;
			if (eventName.equals(EventNotifications.JACOBS)) {
				components.add(new JacobsTooltip(events.peekFirst().extras()));
			}

			if (events.peekFirst().warpCommand() != null) {
				components.add(TooltipComponent.of(Text.translatable("skyblocker.events.tab.clickToWarp").formatted(Formatting.ITALIC).asOrderedText()));
			}

			return components;
		}

		@Nullable
		private String getWarpCommand() {
			return !events.isEmpty() ? events.peek().warpCommand() : null;
		}
	}

	private record JacobsTooltip(String[] crops) implements TooltipComponent {
		private static final ItemStack BARRIER = new ItemStack(Items.BARRIER);

		@Override
		public int getHeight(TextRenderer textRenderer) {
			return 20;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 16 * 3 + 4;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {
			for (int i = 0; i < this.crops.length; i++) {
				String crop = this.crops[i];

				context.drawItemWithoutEntity(JacobsContestWidget.FARM_DATA.getOrDefault(crop, BARRIER), x + 18 * i, y + 2);
			}
		}
	}
}
