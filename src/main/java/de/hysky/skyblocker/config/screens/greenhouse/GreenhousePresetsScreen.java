package de.hysky.skyblocker.config.screens.greenhouse;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.skyblock.garden.GreenhousePaste;

public class GreenhousePresetsScreen extends Screen {
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final @Nullable Screen parent;
	private PresetsList presetsList;

	public GreenhousePresetsScreen(@Nullable Screen parent) {
		super(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePresets.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.addTitleHeader(getTitle(), font);
		presetsList = layout.addToContents(new PresetsList(minecraft, width, layout.getContentHeight()));
		GreenhousePaste.PRESETS_DATA.getData().forEach((name, link) -> presetsList.addEntry(new PresetEntry(name, link)));
		LinearLayout footer = LinearLayout.horizontal().spacing(8);
		footer.addChild(Button.builder(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePresets.addPreset"), _ -> presetsList.addEntry(new PresetEntry())).build());
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose()).build());
		layout.addToFooter(footer);
		repositionElements();
		layout.visitWidgets(this::addRenderableWidget);
	}

	@Override
	protected void repositionElements() {
		presetsList.updateSize(width, layout);
		layout.arrangeElements();
	}

	@Override
	public void onClose() {
		GreenhousePaste.PRESETS_DATA.getData().clear();
		presetsList.children().forEach(entry -> {
			String name = entry.nameBox.getValue();
			String link = entry.linkBox.getValue();
			if (name.isBlank() || link.isBlank()) return;
			GreenhousePaste.PRESETS_DATA.getData().put(name.trim(), link.trim());
		});
		GreenhousePaste.PRESETS_DATA.save();
		minecraft.gui.setScreen(parent);
	}

	private static class PresetsList extends ContainerObjectSelectionList<PresetEntry> {
		private PresetsList(Minecraft minecraft, int width, int height) {
			super(minecraft, width, height, 0, 24);
		}

		@Override
		protected void removeEntry(PresetEntry entry) {
			super.removeEntry(entry);
		}

		@Override
		protected int addEntry(PresetEntry entry) {
			return super.addEntry(entry);
		}

		@Override
		public int getRowWidth() {
			return 360;
		}
	}

	private class PresetEntry extends ContainerObjectSelectionList.Entry<PresetEntry> {
		private final EditBox nameBox;
		private final EditBox linkBox;
		private final List<AbstractWidget> widgets;
		private final LinearLayout layout = LinearLayout.horizontal().spacing(8);

		private PresetEntry(String name, String link) {
			nameBox = layout.addChild(new EditBox(font, Component.empty()));
			nameBox.setWidth(100);
			nameBox.setValue(name);
			nameBox.setMaxLength(64);
			nameBox.setHint(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePresets.name"));
			linkBox = layout.addChild(new EditBox(font, Component.empty()));
			linkBox.setWidth(180);
			linkBox.setMaxLength(2048);
			linkBox.setValue(link);
			linkBox.setHint(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePresets.link"));
			layout.addChild(Button.builder(Component.translatable("gui.remove"), _ -> presetsList.removeEntry(this)).width(80).build());
			widgets = new ArrayList<>();
			layout.arrangeElements();
			layout.visitWidgets(widgets::add);
		}

		private PresetEntry() {
			this("", "");
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return widgets;
		}

		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
			layout.setPosition(getContentXMiddle() - layout.getWidth() / 2, getContentYMiddle() - layout.getHeight() / 2);
			for (AbstractWidget widget : widgets) {
				widget.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return widgets;
		}
	}
}
