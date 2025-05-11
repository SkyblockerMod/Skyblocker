package de.hysky.skyblocker.skyblock.shortcut;

import com.demonwav.mcdev.annotations.Translatable;
import de.hysky.skyblocker.debug.Debug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ShortcutsConfigListWidget extends ElementListWidget<ShortcutsConfigListWidget.AbstractShortcutEntry> {
	private final ShortcutsConfigScreen screen;
	private final List<Map<String, String>> shortcutMaps = new ArrayList<>();

	/**
	 * @param width      the width of the widget
	 * @param height     the height of the widget
	 * @param y          the y coordinate to start rendering/placing the widget from
	 * @param itemHeight the height of each item
	 */
	public ShortcutsConfigListWidget(MinecraftClient minecraftClient, ShortcutsConfigScreen screen, int width, int height, int y, int itemHeight) {
		super(minecraftClient, width, height, y, itemHeight);
		this.screen = screen;
		ShortcutCategoryEntry commandCategory = new ShortcutCategoryEntry(Shortcuts.commands, "skyblocker.shortcuts.command.target", "skyblocker.shortcuts.command.replacement");
		if (Shortcuts.isShortcutsLoaded()) {
			commandCategory.shortcutsMap.keySet().stream().sorted().forEach(commandTarget -> addEntry(new ShortcutEntry(commandCategory, commandTarget)));
		} else {
			addEntry(new ShortcutLoadingEntry());
		}
		ShortcutCategoryEntry commandArgCategory = new ShortcutCategoryEntry(Shortcuts.commandArgs, "skyblocker.shortcuts.commandArg.target", "skyblocker.shortcuts.commandArg.replacement", "skyblocker.shortcuts.commandArg.tooltip");
		if (Shortcuts.isShortcutsLoaded()) {
			commandArgCategory.shortcutsMap.keySet().stream().sorted().forEach(commandArgTarget -> addEntry(new ShortcutEntry(commandArgCategory, commandArgTarget)));
		} else {
			addEntry(new ShortcutLoadingEntry());
		}
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 200;
	}

	@Override
	protected int getScrollbarX() {
		return super.getScrollbarX();
	}

	protected Optional<ShortcutCategoryEntry> getCategory() {
		return switch (getSelectedOrNull()) {
			case ShortcutCategoryEntry category -> Optional.of(category);
			case ShortcutEntry shortcutEntry -> Optional.of(shortcutEntry.category);

			case null, default -> Optional.empty();
		};
	}

	@Override
	public void setSelected(@Nullable ShortcutsConfigListWidget.AbstractShortcutEntry entry) {
		super.setSelected(entry);
		screen.updateButtons();
	}

	protected void addShortcutAfterSelected() {
		getCategory().ifPresent(category -> children().add(children().indexOf(getSelectedOrNull()) + 1, new ShortcutEntry(category)));
	}

	protected void updatePositions() {
		for (AbstractShortcutEntry child : children()) {
			child.updatePositions();
		}
	}

	/**
	 * Returns true if the client is in debug mode and the entry at the given index is selected.
	 * <p>
	 * Used to show the box around the selected entry in debug mode.
	 */
	@Override
	protected boolean isSelectedEntry(int index) {
		return Debug.debugEnabled() ? Objects.equals(getSelectedOrNull(), children().get(index)) : super.isSelectedEntry(index);
	}

	@Override
	protected boolean removeEntry(AbstractShortcutEntry entry) {
		return super.removeEntry(entry);
	}

	protected boolean hasChanges() {
		ShortcutEntry[] notEmptyShortcuts = getNotEmptyShortcuts().toArray(ShortcutEntry[]::new);
		return notEmptyShortcuts.length != shortcutMaps.stream().mapToInt(Map::size).sum() || Arrays.stream(notEmptyShortcuts).anyMatch(ShortcutEntry::isChanged);
	}

	protected void saveShortcuts() {
		shortcutMaps.forEach(Map::clear);
		getNotEmptyShortcuts().forEach(ShortcutEntry::save);
		Shortcuts.saveShortcuts(MinecraftClient.getInstance()); // Save shortcuts to disk
	}

	private Stream<ShortcutEntry> getNotEmptyShortcuts() {
		return children().stream().filter(ShortcutEntry.class::isInstance).map(ShortcutEntry.class::cast).filter(ShortcutEntry::isNotEmpty);
	}

	public static abstract class AbstractShortcutEntry extends ElementListWidget.Entry<AbstractShortcutEntry> {
		protected void updatePositions() {}
	}

	protected class ShortcutCategoryEntry extends AbstractShortcutEntry {
		private final Map<String, String> shortcutsMap;
		private final Text targetName;
		private final Text replacementName;
		private final Text bindName = Text.translatable("skyblocker.shortcuts.bind");
		@Nullable
		private final Text tooltip;

		private ShortcutCategoryEntry(Map<String, String> shortcutsMap, @Translatable String targetName, @Translatable String replacementName) {
			this(shortcutsMap, targetName, replacementName, (Text) null);
		}

		private ShortcutCategoryEntry(Map<String, String> shortcutsMap, @Translatable String targetName, @Translatable String replacementName, @Translatable String tooltip) {
			this(shortcutsMap, targetName, replacementName, Text.translatable(tooltip));
		}

		private ShortcutCategoryEntry(Map<String, String> shortcutsMap, @Translatable String targetName, @Translatable String replacementName, @Nullable Text tooltip) {
			this.shortcutsMap = shortcutsMap;
			this.targetName = Text.translatable(targetName);
			this.replacementName = Text.translatable(replacementName);
			this.tooltip = tooltip;
			shortcutMaps.add(shortcutsMap);
			addEntry(this);
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(new Selectable() {
				@Override
				public SelectionType getType() {
					return SelectionType.HOVERED;
				}

				@Override
				public void appendNarrations(NarrationMessageBuilder builder) {
					builder.put(NarrationPart.TITLE, targetName, replacementName);
				}
			});
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			context.drawCenteredTextWithShadow(client.textRenderer, targetName, width / 2 - 85, y + 5, 0xFFFFFF);
			context.drawCenteredTextWithShadow(client.textRenderer, replacementName, width / 2 + 85, y + 5, 0xFFFFFF);
			context.drawCenteredTextWithShadow(client.textRenderer, bindName, width / 2 + 180, y + 5, 0xFFFFFF);
			if (tooltip != null && isMouseOver(mouseX, mouseY)) {
				screen.setTooltip(tooltip);
			}
		}

		/**
		 * Returns true so that category entries can be focused and selected, so that we can add shortcut entries after them.
		 */
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return true;
		}
	}

	private class ShortcutLoadingEntry extends AbstractShortcutEntry {
		private final Text text;

		private ShortcutLoadingEntry() {
			this.text = Text.translatable("skyblocker.shortcuts.notLoaded");
		}

		@Override
		public List<? extends Element> children() {
			return List.of();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(new Selectable() {
				@Override
				public SelectionType getType() {
					return SelectionType.HOVERED;
				}

				@Override
				public void appendNarrations(NarrationMessageBuilder builder) {
					builder.put(NarrationPart.TITLE, text);
				}
			});
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			context.drawCenteredTextWithShadow(client.textRenderer, text, width / 2, y + 5, 0xFFFFFF);
		}
	}

	protected class ShortcutEntry extends AbstractShortcutEntry {
		private final List<Element> children;
		private final List<Selectable> selectable;
		private final ShortcutCategoryEntry category;
		private final TextFieldWidget target;
		private final TextFieldWidget replacement;
		private final ButtonWidget bindButton;
		private boolean waitingForKey = false;

		private ShortcutEntry(ShortcutCategoryEntry category) {
			this(category, "");
		}

		private ShortcutEntry(ShortcutCategoryEntry category, String targetString) {
			this.category = category;
			target = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 160, 5, 150, 20, category.targetName);
			replacement = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 + 10, 5, 150, 20, category.replacementName);
			target.setMaxLength(48);
			replacement.setMaxLength(48);
			target.setText(targetString);
			replacement.setText(category.shortcutsMap.getOrDefault(targetString, ""));
			this.bindButton = ButtonWidget.builder(Text.of(""),
							btn -> {
								screen.beginEdit(this);
								waitingForKey = true;
							})
					.dimensions(0, 0, 30, 20)
					.build();
			children = List.of(target, replacement, bindButton);
			selectable = List.of(target, replacement, bindButton);
		}

		@Override
		public String toString() {
			return target.getText() + " → " + replacement.getText();
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return selectable;
		}

		private boolean isNotEmpty() {
			return !target.getText().isEmpty() && !replacement.getText().isEmpty();
		}

		private boolean isChanged() {
			return !category.shortcutsMap.containsKey(target.getText()) || !category.shortcutsMap.get(target.getText()).equals(replacement.getText());
		}

		private void save() {
			category.shortcutsMap.put(target.getText(), replacement.getText());
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			target.setY(y);
			replacement.setY(y);
			bindButton.setY(y);
			bindButton.active = !waitingForKey;
			target.render(context, mouseX, mouseY, tickDelta);
			replacement.render(context, mouseX, mouseY, tickDelta);
			bindButton.render(context, mouseX, mouseY, tickDelta);
			context.drawCenteredTextWithShadow(client.textRenderer, "→", width / 2, y + 5, 0xFFFFFF);
			int code = getBoundKeyCode();
			String label;

			if (waitingForKey) {
				label = "…";
			} else if (code == -1) {
				label = "–";
			} else if (code <= -2000) {
				int btn = -2000 - code;
				label = InputUtil.Type.MOUSE.createFromCode(btn).getLocalizedText().getString();
			} else {
				label = InputUtil.fromKeyCode(code, 0).getLocalizedText().getString();
			}
			int bound = getBoundKeyCode();
			boolean duplicate = bound != -1 && Shortcuts.keyCodes.values().stream().filter(c -> c == bound).count() > 1;

			MutableText msg = Text.literal(label);
			if (duplicate) msg = msg.copy().formatted(Formatting.RED);

			bindButton.setMessage(msg);
		}

		public int getBoundKeyCode() {
			return Shortcuts.keyCodes.getOrDefault(getTargetKey(), -1);
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			target.setX(width / 2 - 160);
			replacement.setX(width / 2 + 10);
			bindButton.setX(width / 2 + 10 + replacement.getWidth() + 5);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (bindButton.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		public String getTargetKey() {
			return target.getText();
		}

		public void setBoundKeyCode(int code) {
			Shortcuts.keyCodes.put(getTargetKey(), code);
			waitingForKey = false;
		}
	}
}
