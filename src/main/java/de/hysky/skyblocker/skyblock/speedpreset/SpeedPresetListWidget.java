package de.hysky.skyblocker.skyblock.speedpreset;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpeedPresetListWidget extends ContainerObjectSelectionList<SpeedPresetListWidget.AbstractEntry> {
	private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");
	// Alphanumeric sequence that doesn't start with a number.
	private static final Pattern TITLE = Pattern.compile("^[a-zA-Z]\\w*$");

	public SpeedPresetListWidget(int width, int height, int y) {
		super(Minecraft.getInstance(), width, height, y, 25);
		SpeedPresets instance = SpeedPresets.getInstance();
		addEntry(new TitleEntry());
		if (!instance.getPresets().isEmpty()) {
			instance.getPresets().forEach((title, speed) -> this.addEntry(new SpeedPresetEntry(title, String.valueOf(speed))));
		} else {
			this.addEntry(new SpeedPresetEntry("", ""));
		}
	}

	@Override
	public int getRowWidth() {
		return 190;
	}

	public boolean hasBeenChanged() {
		SpeedPresets instance = SpeedPresets.getInstance();
		// If there are fewer children than presets, some were removed, and all further checks are pointless
		if (children().size() < instance.getPresets().size()) return true;
		var childrenMap = this.children().stream()
				.filter(SpeedPresetEntry.class::isInstance)
				.map(SpeedPresetEntry.class::cast)
				.map(SpeedPresetEntry::getMapping)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(ObjectIntPair::key, ObjectIntPair::valueInt));
		return !instance.arePresetsEqual(childrenMap);
	}

	public void updatePosition() {
		children().forEach(AbstractEntry::updatePosition);
	}

	public void newEntry() {
		SpeedPresetEntry entry = new SpeedPresetEntry("", "");
		this.addEntry(entry);
		entry.updatePosition();
		this.centerScrollOn(entry);
		this.setSelected(entry);
		this.setFocused(entry);
	}

	public void save() {
		SpeedPresets instance = SpeedPresets.getInstance();
		instance.getPresets().clear();
		children().stream()
				.filter(SpeedPresetEntry.class::isInstance)
				.map(SpeedPresetEntry.class::cast)
				.forEach(SpeedPresetEntry::save);
		instance.savePresets(); // Write down the changes.
	}

	public abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {

		protected void updatePosition() {}

		@Override
		public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			this.children().forEach(child -> {
				if (child instanceof LayoutElement widget)
					widget.setY(this.getY());
				if (child instanceof Renderable drawable)
					drawable.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
			});
		}
	}

	public class TitleEntry extends AbstractEntry {

		@Override
		public void renderContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			// The line height is 25, the height of a single character is always 9.
			// 25 - 9 = 16, 16 / 2 = 8, therefore the Y-offset should be 8.
			graphics.centeredText(minecraft.font, Component.translatable("skyblocker.config.general.speedPresets.config.title"), width / 2 - 50, this.getY() + 8, CommonColors.WHITE);
			graphics.centeredText(minecraft.font, Component.translatable("skyblocker.config.general.speedPresets.config.speed"), width / 2 + 50, this.getY() + 8, CommonColors.WHITE);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}
	}

	public class SpeedPresetEntry extends AbstractEntry {

		protected final EditBox titleInput;
		protected final EditBox speedInput;
		protected final Button removeButton;

		protected final LinearLayout layout;

		public SpeedPresetEntry(String title, String speed) {
			Font font = Minecraft.getInstance().font;

			layout = LinearLayout.horizontal();
			layout.spacing(2);

			this.titleInput = layout.addChild(new EditBox(font, 120, 20, Component.empty()));
			this.titleInput.setFilter(str -> str.isEmpty() || TITLE.matcher(str).matches());
			this.titleInput.setValue(title);
			this.titleInput.setMaxLength(16);
			this.titleInput.setHint(Component.literal("newPreset").withStyle(ChatFormatting.DARK_GRAY));

			this.speedInput = layout.addChild(new EditBox(font, 50, 20, Component.empty()));
			this.speedInput.setFilter(str -> str.isEmpty() || NUMBER.matcher(str).matches());
			this.speedInput.setValue(speed);
			this.speedInput.setMaxLength(3);
			this.speedInput.setHint(Component.literal("0").withStyle(ChatFormatting.DARK_GRAY));

			this.removeButton = layout.addChild(Button.builder(Component.literal("-"), btn -> SpeedPresetListWidget.this.removeEntry(this))
					.size(20, 20)
					.build());
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(titleInput, speedInput, removeButton);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(titleInput, speedInput, removeButton);
		}

		public void save() {
			var mapping = getMapping();
			if (mapping == null) return;
			SpeedPresets.getInstance().setPreset(mapping.key(), mapping.valueInt());
		}

		protected boolean isEmpty() {
			return titleInput.getValue().isEmpty() && speedInput.getValue().isEmpty();
		}

		@Override
		protected void updatePosition() {
			layout.setPosition(getRowLeft(), getY());
			layout.arrangeElements();
		}

		protected @Nullable ObjectIntPair<String> getMapping() {
			if (isEmpty()) return null;
			try {
				return ObjectIntPair.of(titleInput.getValue(), Integer.parseInt(speedInput.getValue()));
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
