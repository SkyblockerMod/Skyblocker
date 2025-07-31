package de.hysky.skyblocker.skyblock.speedPreset;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpeedPresetListWidget extends ElementListWidget<SpeedPresetListWidget.AbstractEntry> {

	private static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");
	// Alphanumeric sequence that doesn't start with a number.
	private static final Pattern TITLE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

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
		var presets = SpeedPresets.getInstance();
		// If there are fewer children than presets, some were removed, and all further checks are pointless
		if (children().size() < presets.getPresetCount()) return true;
		var childrenMap = this.children().stream()
				.filter(SpeedPresetEntry.class::isInstance)
				.map(SpeedPresetEntry.class::cast)
				.map(SpeedPresetEntry::getMapping)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(ObjectIntPair::key, ObjectIntPair::valueInt));
		return !presets.arePresetsEqual(childrenMap);
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

		protected void updatePosition() {}

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
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.general.speedPresets.config.title"), width / 2 - 50, y + 8, Colors.WHITE);
			context.drawCenteredTextWithShadow(client.textRenderer, Text.translatable("skyblocker.config.general.speedPresets.config.speed"), width / 2 + 50, y + 8, Colors.WHITE);
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

		public SpeedPresetEntry(String title, String speed) {
			var client = SpeedPresetListWidget.this.client;

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

			this.removeButton = ButtonWidget.builder(Text.literal("-"),
							(btn) -> SpeedPresetListWidget.this.removeEntry(this))
					.dimensions(0, 0, 20, 20)
					.build();

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
			var mapping = getMapping();
			if (mapping != null)
				SpeedPresets.getInstance().setPreset(mapping.key(), mapping.valueInt());
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

		@Nullable
		protected ObjectIntPair<String> getMapping() {
			if (isEmpty()) return null;
			try {
				return ObjectIntPair.of(titleInput.getText(), Integer.parseInt(speedInput.getText()));
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
}
