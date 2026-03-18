package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import de.hysky.skyblocker.skyblock.events.EventNotifications;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.JacobsContestWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.render.GuiHelper;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
import org.jspecify.annotations.Nullable;

public class UpcomingEventsTab implements RecipeTab {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

	private final List<EventRenderer> events = new ArrayList<>();
	private @Nullable EventRenderer hovered = null;

	protected UpcomingEventsTab() {
		List<EventRenderer> renderers = EventNotifications.getEvents().entrySet().stream()
				.sorted(Comparator.comparingLong(a -> a.getValue().isEmpty() ? Long.MAX_VALUE : a.getValue().peekFirst().start()))
				.map(stringLinkedListEntry -> new EventRenderer(stringLinkedListEntry.getKey(), stringLinkedListEntry.getValue()))
				.toList();

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

	private record EventRenderer(String eventName, LinkedList<EventNotifications.SkyblockEvent> events) {
		private static final int HEIGHT = 20;

		private void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
			long time = System.currentTimeMillis() / 1000;
			Font textRenderer = CLIENT.font;

			graphics.text(textRenderer, Component.literal(eventName).withStyle(Style.EMPTY.withUnderlined(isMouseOver(mouseX, mouseY, x, y))), x, y, CommonColors.WHITE);

			if (events.isEmpty()) {
				graphics.text(textRenderer, Component.literal(" ").append(Component.translatable("skyblocker.events.tab.noMore")), x, y + textRenderer.lineHeight, CommonColors.GRAY, false);
			} else if (events.peekFirst().start() > time) {
				Component formatted = Component.literal(" ").append(Component.translatable("skyblocker.events.tab.startsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() - time)))).withStyle(ChatFormatting.YELLOW);

				graphics.text(textRenderer, formatted, x, y + textRenderer.lineHeight, CommonColors.WHITE);
			} else {
				Component formatted = Component.literal(" ").append(Component.translatable("skyblocker.events.tab.endsIn", SkyblockTime.formatTime((int) (events.peekFirst().start() + events.peekFirst().duration() - time)))).withStyle(ChatFormatting.GREEN);

				graphics.text(textRenderer, formatted, x, y + textRenderer.lineHeight, CommonColors.WHITE);
			}
		}

		private static boolean isMouseOver(int mouseX, int mouseY, int x, int y) {
			return GuiHelper.pointIsInArea(mouseX, mouseY, x, y, x + 131, y + HEIGHT);
		}

		private List<ClientTooltipComponent> getTooltip() {
			List<ClientTooltipComponent> components = new ArrayList<>();

			EventNotifications.SkyblockEvent event = events.peekFirst();
			if (event == null) return components;
			if (eventName.equals(EventNotifications.JACOBS) && event.extras().left().isPresent()) {
				components.add(new JacobsTooltip(event.extras().left().get()));
			} else if (eventName.equals(EventNotifications.MAYOR_JERRY) && event.extras().right().isPresent()) {
				EventNotifications.JerryPerks jerryInfo = event.extras().right().get();
				Component mayorText = Component.translatable("skyblocker.events.tab.currentJerryMayor", jerryInfo.mayorName()).withStyle(ChatFormatting.DARK_GREEN);
				components.add(ClientTooltipComponent.create(mayorText.getVisualOrderText()));

				for (String perk : jerryInfo.perks()) {
					Component perkText = Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.literal(perk).withStyle(ChatFormatting.GREEN));
					components.add(ClientTooltipComponent.create(perkText.getVisualOrderText()));
				}

				return components;
			}

			if (!event.warpCommand().isEmpty()) {
				components.add(ClientTooltipComponent.create(Component.translatable("skyblocker.events.tab.clickToWarp").withStyle(ChatFormatting.ITALIC).getVisualOrderText()));
			}

			components.add(ClientTooltipComponent.create(Component.literal(Formatters.DATE_FORMATTER.format(Instant.ofEpochSecond(event.start()))).withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY).getVisualOrderText()));

			return components;
		}

		private @Nullable String getWarpCommand() {
			return !events.isEmpty() ? events.peek().warpCommand() : null;
		}
	}

	private record JacobsTooltip(List<String> crops) implements ClientTooltipComponent {

		@Override
		public int getHeight(Font textRenderer) {
			return 20;
		}

		@Override
		public int getWidth(Font textRenderer) {
			return 16 * 3 + 4;
		}

		@Override
		public void extractImage(Font textRenderer, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
			for (int i = 0; i < this.crops.size(); i++) {
				String crop = this.crops.get(i);

				graphics.fakeItem(JacobsContestWidget.FARM_DATA.getOrDefault(crop, Ico.BARRIER).getStackOrThrow(), x + 18 * i, y + 2);
			}
		}
	}
}
