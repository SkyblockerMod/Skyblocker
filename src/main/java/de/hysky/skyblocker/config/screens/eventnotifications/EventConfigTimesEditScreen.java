package de.hysky.skyblocker.config.screens.eventnotifications;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.EventNotificationsConfig;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.config.DurationController;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class EventConfigTimesEditScreen extends Screen {

	private final EventNotificationsConfig.EventConfig eventConfig;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private EntryList list;

	public EventConfigTimesEditScreen(Screen parent, String name, EventNotificationsConfig.EventConfig eventConfig) {
		super(Component.translatable("skyblocker.config.eventNotifications.screen.title", name));
		this.eventConfig = eventConfig;
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		layout.addTitleHeader(getTitle(), font);
		list = layout.addToContents(new EntryList(width, layout.getContentHeight(), 0, 24));
		list.replaceEntries(eventConfig.reminderTimes.intStream().sorted().mapToObj(Entry::new).toList());

		LinearLayout footerLayout = LinearLayout.horizontal().spacing(2);
		footerLayout.addChild(Button.builder(Component.translatable("skyblocker.config.eventNotifications.screen.addReminder"), b -> list.addEntry(new Entry(5 * 60))).build());
		footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build());
		layout.addToFooter(footerLayout);
		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		list.updateSize(this.width, this.layout);
		layout.arrangeElements();
	}

	@Override
	public void onClose() {
		SkyblockerConfigManager.update(c -> eventConfig.reminderTimes = list.children().stream()
				.map(e -> e.box)
				.filter(b -> b.valid)
				.mapToInt(b -> b.seconds)
				.sorted()
				.collect(IntArrayList::new, IntArrayList::add, IntArrayList::addAll));
		minecraft.setScreen(parent);
	}

	private class EntryList extends ContainerObjectSelectionList<EventConfigTimesEditScreen.Entry> {

		private EntryList(int width, int height, int y, int itemHeight) {
			super(EventConfigTimesEditScreen.this.minecraft, width, height, y, itemHeight);
		}

		@Override
		protected void removeEntry(EventConfigTimesEditScreen.Entry entry) {
			super.removeEntry(entry);
		}

		@Override
		protected int addEntry(EventConfigTimesEditScreen.Entry entry) {
			return super.addEntry(entry);
		}
	}

	private class Entry extends ContainerObjectSelectionList.Entry<Entry> {
		private static final Identifier DELETE_ICON = SkyblockerMod.id("trash_can");
		private static final int ICON_WIDTH = 12, ICON_HEIGHT = 15;

		private final TimeEditBox box;
		private final List<AbstractWidget> widgets;
		private final LinearLayout entryLayout = LinearLayout.horizontal().spacing(4);

		private Entry(int seconds) {
			box = new TimeEditBox(seconds);
			Button buttonDelete = SpriteIconButton.builder(Component.translatable("selectServer.deleteButton"), ignored -> list.removeEntry(this), true).size(20, 20).sprite(DELETE_ICON, ICON_WIDTH, ICON_HEIGHT).build();
			entryLayout.addChild(box);
			entryLayout.addChild(buttonDelete);
			entryLayout.arrangeElements();
			widgets = List.of(box, buttonDelete);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return widgets;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
			entryLayout.setPosition(getContentRight() - entryLayout.getWidth(), getContentY());
			for (AbstractWidget widget : widgets) {
				widget.render(guiGraphics, i, j, f);
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return widgets;
		}
	}

	private static class TimeEditBox extends EditBox {

		private int seconds;
		private boolean valid = false;

		TimeEditBox(int seconds) {
			super(Minecraft.getInstance().font, 150, 20, Component.empty());
			this.seconds = seconds;
			setResponder(this::onUpdate);
			setValue(SkyblockTime.formatTime(seconds).getString());
			addFormatter((string, _firstCharacterIndex) -> FormattedCharSequence.forward(string, valid ? Style.EMPTY : Style.EMPTY.applyFormat(ChatFormatting.RED)));
		}

		private void onUpdate(String s) {
			valid = DurationController.isValid(s);
			if (valid) seconds = DurationController.fromString(s);
		}
	}
}
