package de.hysky.skyblocker.skyblock.itemlist.recipebook.events;

import de.hysky.skyblocker.skyblock.events.EventInstance;
import de.hysky.skyblocker.skyblock.events.EventManager;
import de.hysky.skyblocker.skyblock.events.SkyblockEvent;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.time.SkyblockTime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public abstract class EventEntry extends ContainerObjectSelectionList.Entry<EventEntry> {
	protected final Minecraft minecraft;

	protected boolean textHovered = false;
	private final boolean showWarp;

	public EventEntry(Minecraft minecraft, boolean showWarp) {
		this.minecraft = minecraft;
		this.showWarp = showWarp;
	}

	public abstract @Nullable EventInstance getInstance();

	protected abstract Component getText();

	protected void extractText(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int maxX, Instant now, boolean entryHovered) {
		Font font = minecraft.font;
		graphics.enableScissor(getX(), getContentY(), maxX, getContentBottom());
		ActiveTextCollector textRenderer = graphics.textRenderer();

		this.textHovered = entryHovered && mouseX < maxX;
		int availableWidth = getContentWidth();

		Component mainText = getText();
		if (entryHovered && font.width(mainText) > availableWidth) textRenderer.acceptScrollingWithDefaultCenter(mainText, getContentX(), getContentRight(), getContentY() - 1, getContentY() + 8);
		else textRenderer.accept(getContentX(), getContentY(), mainText);

		Component formatted;
		EventInstance currentInstance = getInstance();
		if (currentInstance == null) {
			formatted = Component.translatable("skyblocker.events.tab.noMore").withStyle(style -> style.withoutShadow().withColor(TextColor.GRAY));
		} else {
			if (currentInstance.start().isAfter(now)) {
				formatted = Component.translatable("skyblocker.events.tab.startsIn", SkyblockTime.formatTime(now.until(currentInstance.start()).toSeconds())).withStyle(ChatFormatting.YELLOW);
			} else if (currentInstance.end().isAfter(now)) {
				formatted = Component.translatable("skyblocker.events.tab.endsIn", SkyblockTime.formatTime(now.until(currentInstance.end()).toSeconds())).withStyle(ChatFormatting.GREEN);
			} else {
				formatted = Component.translatable("skyblocker.events.tab.over").withStyle(style -> style.withoutShadow().withColor(TextColor.GRAY));
			}
		}
		// the split method creates a new array list everytime which kinda sucks
		textRenderer.accept(getContentX() + 4, getContentY() + font.lineHeight, font.split(formatted, availableWidth - 4).getFirst());
		graphics.disableScissor();
	}

	public void extractTooltip(GuiGraphicsExtractor graphics, int x, int y) {
		EventInstance currentInstance = getInstance();
		if (textHovered && currentInstance != null) {
			graphics.tooltip(minecraft.font, currentInstance.createTooltip(showWarp), x, y, DefaultTooltipPositioner.INSTANCE, null);
		}
	}

	public static class AutoUpdate extends EventEntry implements Comparable<AutoUpdate> {
		private final Button moreButton;
		private final SkyblockEvent event;
		private final List<AbstractWidget> children;
		private @Nullable EventInstance currentInstance;

		public AutoUpdate(Minecraft minecraft, SkyblockEvent event, Consumer<SkyblockEvent> moreButtonClicked) {
			super(minecraft, true);
			moreButton = Button.builder(Component.literal("..."), _ -> moreButtonClicked.accept(event))
					.tooltip(Tooltip.create(Component.translatable("skyblocker.events.tab.more")))
					.size(16, 16).build();
			children = List.of(moreButton);
			this.event = event;
			this.currentInstance = EventManager.getNext(event, true).orElse(null);
		}

		@Override
		protected Component getText() {
			return Component.literal(event.name()).withStyle(Style.EMPTY.withUnderlined(this.textHovered));
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			moreButton.setPosition(getContentRight() - moreButton.getWidth() - 4, getContentYMiddle() - moreButton.getHeight() / 2);
			int maxX = getContentRight();
			if (mouseX >= moreButton.getX() - 4 && hovered) {
				moreButton.visible = true;
				moreButton.extractRenderState(graphics, mouseX, mouseY, a);
				maxX = moreButton.getX() - 4;
			} else {
				moreButton.visible = false;
			}

			if (currentInstance != null && Instant.now().isAfter(currentInstance.end()))
				currentInstance = EventManager.getNext(event, Instant.now(), true).orElse(null);
			extractText(graphics, mouseX, mouseY, maxX, Instant.now(), hovered);
		}

		@Override
		public @Nullable EventInstance getInstance() {
			return currentInstance;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if (super.mouseClicked(event, doubleClick)) return true;
			EventInstance currentInstance = getInstance();
			if (textHovered && currentInstance != null) {
				currentInstance.additionalInfo().warpCommand().ifPresent(command -> MessageScheduler.INSTANCE.sendMessageAfterCooldown(command, true));
				return true;
			}
			return false;
		}

		@Override
		public int compareTo(EventEntry.AutoUpdate o) {
			if (currentInstance == null) {
				return (o.currentInstance == null) ? 0 : 1;
			} else if (o.currentInstance == null) {
				return -1;
			} else {
				return currentInstance.start().compareTo(o.currentInstance.start());
			}
		}
	}

	public static class Static extends EventEntry {
		private final EventInstance instance;

		public Static(Minecraft minecraft, EventInstance instance) {
			super(minecraft, false);
			this.instance = instance;
		}

		@Override
		public @Nullable EventInstance getInstance() {
			return instance;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		protected Component getText() {
			return Component.literal(Formatters.DATE_FORMATTER_SHORT.format(instance.start()));
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			extractText(graphics, mouseX, mouseY, getContentRight(), Instant.now(), hovered);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}
	}
}
