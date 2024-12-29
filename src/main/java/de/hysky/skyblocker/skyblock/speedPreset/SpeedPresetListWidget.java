package de.hysky.skyblocker.skyblock.speedPreset;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class SpeedPresetListWidget extends ElementListWidget<SpeedPresetListWidget.AbstractEntry> {

	private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");
	// Alphanumeric sequence that doesn't start with a number.
	private static final Pattern TITLE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");
	private static final Logger LOGGER = LoggerFactory.getLogger(SpeedPresetListWidget.class);

	public SpeedPresetListWidget(int width, int height, int y) {
		super(MinecraftClient.getInstance(), width, height, y, 25);
		var presets = SpeedPresets.getInstance();
		addEntry(new TitleEntry());
		if (presets.getPresetCount() > 0)
			presets.forEach((title, speed) ->
					this.addEntry(new SpeedPresetEntry(title, String.valueOf(speed))));
		else
			this.addEntry(new SpeedPresetEntry("", ""));
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 104;
	}

	public boolean hasBeenChanged() {
		var presets = SpeedPresets.getInstance().getPresetCount();
		// If there are fewer children than presets, some were removed, and all further checks are pointless
		if (children().size() < presets) return true;
		return presets != children().stream().filter(SpeedPresetEntry.class::isInstance)
				.filter(preset -> !((SpeedPresetEntry) preset).isEmpty())
				.count() || children().stream().filter(SpeedPresetEntry.class::isInstance).map(SpeedPresetEntry.class::cast).anyMatch(SpeedPresetEntry::hasBeenModified);
	}

	public void updatePosition() {
		children().forEach(AbstractEntry::updatePosition);
	}

	public void newEntry() {
		var entry = new SpeedPresetEntry("", "");
		this.addEntry(entry);
		this.centerScrollOn(entry);
		this.setSelected(entry);
		this.setFocused(entry);
	}

	public void save() {
		var presets = SpeedPresets.getInstance();
		presets.clear();
		children().stream().filter(SpeedPresetEntry.class::isInstance).map(SpeedPresetEntry.class::cast).forEach(SpeedPresetEntry::save);
		presets.savePresets(); // Write down the changes.
	}

	public abstract static class AbstractEntry extends ElementListWidget.Entry<AbstractEntry> {

		protected boolean hasBeenModified() {
			return false;
		}

		protected void updatePosition(){}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.children().forEach(child -> {
				if (child instanceof Widget widget)
					widget.setY(y);
				if (child instanceof Drawable drawable)
					drawable.render(context, mouseX, mouseY, tickDelta);
			});
		}
	}

	public class TitleEntry extends AbstractEntry {

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			// The line height is 25, the height of a single character is always 9.
			// 25 - 9 = 16, 16 / 2 = 8, therefore the Y-offset should be 8.
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.general.speedPresets.config.title"), width / 2 - 50, y + 8, 0xFFFFFF);
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.general.speedPresets.config.speed"), width / 2 + 50, y + 8, 0xFFFFFF);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of();
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}
	}

	public class SpeedPresetEntry extends AbstractEntry {

		protected final TextFieldWidget titleInput;
		protected final TextFieldWidget speedInput;
		protected final ButtonWidget removeButton;

		protected final String initialTitle;
		protected final String initialSpeed;

		public SpeedPresetEntry(String title, String speed) {
			var client = SpeedPresetListWidget.this.client;

			this.initialTitle = title;
			this.initialSpeed = speed;

			// All Xs and Ys are then set using the initPosition() method.
			this.titleInput = new TextFieldWidget(client.textRenderer, 0, 0, 120, 20, Text.empty());
			this.titleInput.setTextPredicate(str -> str.isEmpty() || TITLE.matcher(str).matches());
			this.titleInput.setText(title);
			this.titleInput.setMaxLength(16);
			this.titleInput.setPlaceholder(Text.literal("newPreset").formatted(Formatting.DARK_GRAY));
			this.speedInput = new TextFieldWidget(client.textRenderer, 0, 0, 50, 20, Text.empty());

			this.speedInput.setTextPredicate(str -> str.isEmpty() || NUMBER.matcher(str).matches());
			this.speedInput.setText(speed);
			this.speedInput.setMaxLength(3);
			this.speedInput.setPlaceholder(Text.literal("0").formatted(Formatting.DARK_GRAY));

			this.removeButton = ButtonWidget.builder(Text.literal("-"), (btn) -> {
				SpeedPresetListWidget.this.removeEntry(this);
			}).dimensions(0, 0, 20, 20).build();

			this.updatePosition();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(titleInput, speedInput, removeButton);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(titleInput, speedInput, removeButton);
		}

		public void save() {
			var presets = SpeedPresets.getInstance();
			assert presets != null && children().size() == 3;
			var title = ((TextFieldWidget) children().get(0)).getText();
			var speed = ((TextFieldWidget) children().get(1)).getText();
			if (title.isEmpty()) return;
			try {
				presets.setPreset(title, Short.parseShort(speed));
			} catch (NumberFormatException e) {
				LOGGER.warn("Couldn't save speed preset '{}' because of an invalid speed value: {}", title, speed);
			}
		}

		protected boolean isEmpty() {
			return titleInput.getText().isEmpty() && speedInput.getText().isEmpty();
		}

		@Override
		protected void updatePosition() {
			var grid = new GridWidget();
			grid.setSpacing(2);
			grid.add(titleInput, 0, 0, 1, 3);
			grid.add(speedInput, 0, 3, 1, 2);
			grid.add(removeButton, 0, 5, 1, 1);
			grid.refreshPositions();
			SimplePositioningWidget.setPos(grid, 0, 0, width, itemHeight, 0.5f, 0.5f);
		}

		@Override
		protected boolean hasBeenModified() {
			return !this.titleInput.getText().equals(this.initialTitle)
					|| !this.speedInput.getText().equals(this.initialSpeed);
		}
	}
}
