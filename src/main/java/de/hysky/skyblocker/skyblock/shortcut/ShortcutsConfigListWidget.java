package de.hysky.skyblocker.skyblock.shortcut;

import com.demonwav.mcdev.annotations.Translatable;
import de.hysky.skyblocker.debug.Debug;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ShortcutsConfigListWidget extends ElementListWidget<ShortcutsConfigListWidget.AbstractShortcutEntry> {
	private final ShortcutsConfigScreen screen;

	/**
	 * @param width      the width of the widget
	 * @param height     the height of the widget
	 * @param y          the y coordinate to start rendering/placing the widget from
	 * @param itemHeight the height of each item
	 */
	public ShortcutsConfigListWidget(MinecraftClient minecraftClient, ShortcutsConfigScreen screen, int width, int height, int y, int itemHeight) {
		super(minecraftClient, width, height, y, itemHeight);
		this.screen = screen;

		ShortcutCategoryEntry<String> commandCategory = new ShortcutCategoryEntry<>(Shortcuts.shortcuts.getData().commands(), CommandShortcutEntry::new, "skyblocker.shortcuts.command.target", "skyblocker.shortcuts.command.replacement");
		if (Shortcuts.isShortcutsLoaded()) {
			commandCategory.shortcutsMap.keySet().stream().sorted().forEach(commandTarget -> addEntry(new CommandShortcutEntry(commandCategory, commandTarget)));
		} else {
			addEntry(new ShortcutLoadingEntry());
		}
		ShortcutCategoryEntry<String> commandArgCategory = new ShortcutCategoryEntry<>(Shortcuts.shortcuts.getData().commandArgs(), CommandShortcutEntry::new, "skyblocker.shortcuts.commandArg.target", "skyblocker.shortcuts.commandArg.replacement", "skyblocker.shortcuts.commandArg.tooltip");
		if (Shortcuts.isShortcutsLoaded()) {
			commandArgCategory.shortcutsMap.keySet().stream().sorted().forEach(commandArgTarget -> addEntry(new CommandShortcutEntry(commandArgCategory, commandArgTarget)));
		} else {
			addEntry(new ShortcutLoadingEntry());
		}
		ShortcutCategoryEntry<ShortcutKeyBinding> keybindCategory = new ShortcutCategoryEntry<>(Shortcuts.shortcuts.getData().keyBindings(), KeybindShortcutEntry::new, "skyblocker.shortcuts.keyBinding.target", "skyblocker.shortcuts.keyBinding.replacement", "skyblocker.shortcuts.keyBinding.tooltip");
		if (Shortcuts.isShortcutsLoaded()) {
			keybindCategory.shortcutsMap.keySet().stream().sorted().forEach(keyBinding -> addEntry(new KeybindShortcutEntry(keybindCategory, keyBinding.copy())));
		} else {
			addEntry(new ShortcutLoadingEntry());
		}
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 100;
	}

	@Override
	protected int getScrollbarX() {
		return super.getScrollbarX();
	}

	protected Optional<ShortcutCategoryEntry<?>> getCategory() {
		return switch (getSelectedOrNull()) {
			case ShortcutCategoryEntry<?> category -> Optional.of(category);
			case ShortcutEntry<?> shortcutEntry -> Optional.of(shortcutEntry.category);

			case null, default -> Optional.empty();
		};
	}

	@Override
	public void setSelected(@Nullable ShortcutsConfigListWidget.AbstractShortcutEntry entry) {
		super.setSelected(entry);
		screen.updateButtons();
	}

	protected void addShortcutAfterSelected() {
		getCategory().ifPresent(category -> children().add(children().indexOf(getSelectedOrNull()) + 1, category.entrySupplier.get()));
	}

	protected void updatePositions() {
		for (AbstractShortcutEntry child : children()) {
			child.updatePositions();
		}
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screen.option.ControlsListWidget#update() ControlsListWidget#update()}.
	 */
	protected void updateKeybinds() {
		children().stream().filter(KeybindShortcutEntry.class::isInstance).map(KeybindShortcutEntry.class::cast).forEach(KeybindShortcutEntry::update);
	}

	protected boolean stopEditing() {
		return children().stream().filter(KeybindShortcutEntry.class::isInstance).map(KeybindShortcutEntry.class::cast).anyMatch(KeybindShortcutEntry::stopEditing);
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
		ShortcutEntry<?>[] notEmptyShortcuts = getNotEmptyShortcuts().toArray(ShortcutEntry[]::new);
		return notEmptyShortcuts.length != Shortcuts.shortcuts.getData().size() || Arrays.stream(notEmptyShortcuts).anyMatch(ShortcutEntry::isChanged);
	}

	protected void saveShortcuts() {
		Shortcuts.shortcuts.getData().clear();
		getNotEmptyShortcuts().forEach(ShortcutEntry::save);
		Shortcuts.shortcuts.save(); // Save shortcuts to disk
	}

	// What are these generics... 😭
	private Stream<? extends ShortcutEntry<?>> getNotEmptyShortcuts() {
		return children().stream().filter(ShortcutEntry.class::isInstance).map(e -> (ShortcutEntry<?>) e).filter(ShortcutEntry::isNotEmpty);
	}

	public abstract static class AbstractShortcutEntry extends ElementListWidget.Entry<AbstractShortcutEntry> {
		protected void updatePositions() {}
	}

	protected class ShortcutCategoryEntry<T> extends AbstractShortcutEntry {
		private final Map<T, String> shortcutsMap;
		// Supplier for new shortcut entries. This is needed because otherwise we don't know which type of shortcut entry to create partly due to Java's annoying generics (type erasure).
		private final Supplier<ShortcutEntry<T>> entrySupplier;
		private final Text targetName;
		private final Text replacementName;
		@Nullable
		private final Text tooltip;

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName) {
			this(shortcutsMap, entryFactory, targetName, replacementName, (Text) null);
		}

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName, @Translatable String tooltip) {
			this(shortcutsMap, entryFactory, targetName, replacementName, Text.translatable(tooltip));
		}

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName, @Nullable Text tooltip) {
			this.shortcutsMap = shortcutsMap;
			this.entrySupplier = () -> entryFactory.apply(this);
			this.targetName = Text.translatable(targetName);
			this.replacementName = Text.translatable(replacementName);
			this.tooltip = tooltip;
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
			context.drawCenteredTextWithShadow(client.textRenderer, targetName, width / 2 - 85, y + 5, Colors.WHITE);
			context.drawCenteredTextWithShadow(client.textRenderer, replacementName, width / 2 + 85, y + 5, Colors.WHITE);
			if (tooltip != null && isMouseOver(mouseX, mouseY)) {
				context.drawTooltip(tooltip, mouseX, mouseY);
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
			context.drawCenteredTextWithShadow(client.textRenderer, text, width / 2, y + 5, Colors.WHITE);
		}
	}

	protected abstract class ShortcutEntry<T> extends AbstractShortcutEntry {
		protected final ShortcutCategoryEntry<T> category;
		protected final TextFieldWidget replacement;

		private ShortcutEntry(ShortcutCategoryEntry<T> category, T targetKey) {
			this.category = category;
			replacement = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 + 10, 5, 150, 20, category.replacementName);
			replacement.setMaxLength(48);
			replacement.setText(category.shortcutsMap.getOrDefault(targetKey, ""));
		}

		protected abstract boolean isNotEmpty();

		protected abstract boolean isChanged();

		protected abstract void save();

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			replacement.setY(y);
			replacement.render(context, mouseX, mouseY, tickDelta);
			context.drawCenteredTextWithShadow(client.textRenderer, "→", width / 2, y + 5, Colors.WHITE);
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			replacement.setX(width / 2 + 10);
		}
	}

	protected class CommandShortcutEntry extends ShortcutEntry<String> {
		private final List<TextFieldWidget> children;
		private final TextFieldWidget target;

		private CommandShortcutEntry(ShortcutCategoryEntry<String> category) {
			this(category, "");
		}

		private CommandShortcutEntry(ShortcutCategoryEntry<String> category, String targetString) {
			super(category, targetString);
			target = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 2 - 160, 5, 150, 20, category.targetName);
			target.setMaxLength(48);
			target.setText(targetString);
			children = List.of(target, replacement);
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
			return children;
		}

		@Override
		protected boolean isNotEmpty() {
			return !target.getText().isEmpty() && !replacement.getText().isEmpty();
		}

		@Override
		protected boolean isChanged() {
			return !category.shortcutsMap.containsKey(target.getText()) || !category.shortcutsMap.get(target.getText()).equals(replacement.getText());
		}

		@Override
		protected void save() {
			category.shortcutsMap.put(target.getText(), replacement.getText());
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
			target.setY(y);
			target.render(context, mouseX, mouseY, tickDelta);
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			target.setX(width / 2 - 160);
		}
	}

	protected class KeybindShortcutEntry extends ShortcutEntry<ShortcutKeyBinding> {
		private final List<ClickableWidget> children;
		private final ShortcutKeyBinding keyBinding;
		private final KeybindWidget keybindButton;
		private boolean duplicate = false;

		private KeybindShortcutEntry(ShortcutCategoryEntry<ShortcutKeyBinding> category) {
			this(category, new ShortcutKeyBinding(List.of(InputUtil.UNKNOWN_KEY)));
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry#KeyBindingEntry(KeyBinding, Text) ControlsListWidget.KeyBindingEntry#KeyBindingEntry(KeyBinding, Text)}
		 */
		@SuppressWarnings("JavadocReference")
		private KeybindShortcutEntry(ShortcutCategoryEntry<ShortcutKeyBinding> category, ShortcutKeyBinding keyBinding) {
			super(category, keyBinding);
			this.keyBinding = keyBinding;
			keybindButton = new KeybindWidget(keyBinding, width / 2 - 160, 5, 150, 20, keyBinding.getBoundKeysText(),
					textSupplier -> keyBinding.isUnbound()
							? Text.translatable("narrator.controls.unbound", replacement.getText())
							: Text.translatable("narrator.controls.bound", replacement.getText(), textSupplier.get()),
					ShortcutsConfigListWidget.this::updateKeybinds);
			// The duplicate warning tooltip displays replacement commands and needs to be updated.
			replacement.setChangedListener(command -> ShortcutsConfigListWidget.this.updateKeybinds());
			children = List.of(keybindButton, replacement);
			update();
		}

		@Override
		public String toString() {
			// This is used in the delete warning screen, so we use the localized text.
			return keyBinding.getBoundKeysText().getString() + " → " + replacement.getText();
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		protected boolean isNotEmpty() {
			return !keyBinding.isUnbound() && !replacement.getText().isEmpty();
		}

		@Override
		protected boolean isChanged() {
			return !category.shortcutsMap.containsKey(keyBinding) || !category.shortcutsMap.get(keyBinding).equals(replacement.getText());
		}

		@Override
		protected void save() {
			category.shortcutsMap.put(keyBinding, replacement.getText());
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry#render(DrawContext, int, int, int, int, int, int, int, boolean, float) ControlsListWidget.KeyBindingEntry#render(DrawContext, int, int, int, int, int, int, int, boolean, float)}.
		 */
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
			keybindButton.setY(y);
			keybindButton.render(context, mouseX, mouseY, tickDelta);
			if (duplicate) {
				context.fill(keybindButton.getX() - 6, y, keybindButton.getX() - 3, y + entryHeight, 0xFFFF0000);
			}
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			keybindButton.setX(width / 2 - 160);
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screen.option.ControlsListWidget.KeyBindingEntry#update() ControlsListWidget.KeyBindingEntry#update()}.
		 */
		@SuppressWarnings("JavadocReference")
		protected void update() {
			keybindButton.setMessage(keyBinding.getBoundKeysText());
			duplicate = false;
			MutableText text = Text.empty();
			if (!keyBinding.isUnbound()) {
				// Check for conflicts with regular keybinds
				for (KeyBinding otherKeyBinding : client.options.allKeys) {
					if (keyBinding.getBoundKeysTranslationKey().contains(otherKeyBinding.getBoundKeyTranslationKey())) {
						if (duplicate) {
							text.append(", ");
						}
						duplicate = true;
						text.append(Text.translatable(otherKeyBinding.getTranslationKey()));
					}
				}
				// Check for conflicts with other keybind shortcuts
				for (AbstractShortcutEntry shortcut : ShortcutsConfigListWidget.this.children()) {
					if (shortcut instanceof KeybindShortcutEntry keyBindingShortcut && keyBinding != keyBindingShortcut.keyBinding && keyBinding.equals(keyBindingShortcut.keyBinding)) {
						if (duplicate) {
							text.append(", ");
						}
						duplicate = true;
						// We display the replacement command to help users identify which shortcuts have conflicting keybinds.
						text.append(keyBindingShortcut.replacement.getText());
					}
				}
			}

			if (duplicate) {
				keybindButton.setMessage(Text.literal("[ ")
						.append(keybindButton.getMessage().copy().formatted(Formatting.WHITE))
						.append(" ]")
						.formatted(Formatting.RED));
				keybindButton.setTooltip(Tooltip.of(Text.translatable("controls.keybinds.duplicateKeybinds", text)));
			} else {
				keybindButton.setTooltip(null);
			}

			if (keybindButton.isEditing()) {
				keybindButton.setMessage(Text.literal("> ")
						.append(keybindButton.getMessage().copy().formatted(Formatting.WHITE, Formatting.UNDERLINE))
						.append(" <")
						.formatted(Formatting.YELLOW)
				);
			}
		}

		protected boolean stopEditing() {
			return keybindButton.stopEditing();
		}
	}
}
