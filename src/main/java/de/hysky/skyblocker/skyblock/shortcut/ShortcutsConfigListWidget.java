package de.hysky.skyblocker.skyblock.shortcut;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.blaze3d.platform.InputConstants;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;

public class ShortcutsConfigListWidget extends ContainerObjectSelectionList<ShortcutsConfigListWidget.AbstractShortcutEntry> {
	private static final int TEXT_Y_OFFSET = 5 + 2;
	private static final int TEXT_FIELD_PADDING = 2;
	private final ShortcutsConfigScreen screen;

	/**
	 * @param width      the width of the widget
	 * @param height     the height of the widget
	 * @param y          the y coordinate to start rendering/placing the widget from
	 */
	public ShortcutsConfigListWidget(Minecraft minecraftClient, ShortcutsConfigScreen screen, int width, int height, int y) {
		super(minecraftClient, width, height, y, 24);
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
		return super.getRowWidth() + 100 + 2 * TEXT_FIELD_PADDING;
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX();
	}

	protected Optional<ShortcutCategoryEntry<?>> getCategory() {
		return switch (getSelected()) {
			case ShortcutCategoryEntry<?> category -> Optional.of(category);
			case ShortcutEntry<?> shortcutEntry -> Optional.of(shortcutEntry.category);

			case null, default -> Optional.empty();
		};
	}

	@Override
	public void setSelected(ShortcutsConfigListWidget.@Nullable AbstractShortcutEntry entry) {
		super.setSelected(entry);
		screen.updateButtons();
	}

	protected void addShortcutAfterSelected() {
		getCategory().ifPresent(category -> {
			ArrayList<AbstractShortcutEntry> newEntries = new ArrayList<>(children());
			ShortcutEntry<?> newEntry = category.entrySupplier.get();
			newEntries.add(children().indexOf(getSelected()) + 1, newEntry);
			replaceEntries(newEntries);
			setSelected(newEntry);
		});
	}

	protected void updatePositions() {
		for (AbstractShortcutEntry child : children()) {
			child.updatePositions();
		}
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsList#resetMappingAndUpdateButtons() ControlsListWidget#update()}.
	 */
	protected void updateKeybinds() {
		children().stream().filter(KeybindShortcutEntry.class::isInstance).map(KeybindShortcutEntry.class::cast).forEach(KeybindShortcutEntry::update);
	}

	protected boolean stopEditing() {
		return children().stream().filter(KeybindShortcutEntry.class::isInstance).map(KeybindShortcutEntry.class::cast).anyMatch(KeybindShortcutEntry::stopEditing);
	}

	@Override
	protected boolean entriesCanBeSelected() {
		return true;
	}

	@Override
	protected void removeEntry(AbstractShortcutEntry entry) {
		super.removeEntry(entry);
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

	public abstract static class AbstractShortcutEntry extends ContainerObjectSelectionList.Entry<AbstractShortcutEntry> {
		protected void updatePositions() {}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			super.mouseClicked(click, doubled);
			return true;
		}
	}

	protected class ShortcutCategoryEntry<T> extends AbstractShortcutEntry {
		private final Map<T, String> shortcutsMap;
		// Supplier for new shortcut entries. This is needed because otherwise we don't know which type of shortcut entry to create partly due to Java's annoying generics (type erasure).
		private final Supplier<ShortcutEntry<T>> entrySupplier;
		private final Component targetName;
		private final Component replacementName;
		private final @Nullable Component tooltip;

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName) {
			this(shortcutsMap, entryFactory, targetName, replacementName, (Component) null);
		}

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName, @Translatable String tooltip) {
			this(shortcutsMap, entryFactory, targetName, replacementName, Component.translatable(tooltip));
		}

		private ShortcutCategoryEntry(Map<T, String> shortcutsMap, Function<ShortcutCategoryEntry<T>, ShortcutEntry<T>> entryFactory, @Translatable String targetName, @Translatable String replacementName, @Nullable Component tooltip) {
			this.shortcutsMap = shortcutsMap;
			this.entrySupplier = () -> entryFactory.apply(this);
			this.targetName = Component.translatable(targetName);
			this.replacementName = Component.translatable(replacementName);
			this.tooltip = tooltip;
			addEntry(this);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(new NarratableEntry() {
				@Override
				public NarrationPriority narrationPriority() {
					return NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput builder) {
					builder.add(NarratedElementType.TITLE, targetName, replacementName);
				}
			});
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredString(minecraft.font, targetName, getContentXMiddle() - 85, getY() + TEXT_Y_OFFSET, CommonColors.WHITE);
			context.drawCenteredString(minecraft.font, replacementName, getContentXMiddle() + 85, getY() + TEXT_Y_OFFSET, CommonColors.WHITE);
			if (tooltip != null && isMouseOver(mouseX, mouseY)) {
				context.setTooltipForNextFrame(tooltip, mouseX, mouseY);
			}
		}
	}

	private class ShortcutLoadingEntry extends AbstractShortcutEntry {
		private final Component text;

		private ShortcutLoadingEntry() {
			this.text = Component.translatable("skyblocker.shortcuts.notLoaded");
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(new NarratableEntry() {
				@Override
				public NarrationPriority narrationPriority() {
					return NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput builder) {
					builder.add(NarratedElementType.TITLE, text);
				}
			});
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			context.drawCenteredString(minecraft.font, text, this.getWidth() / 2, this.getY() + TEXT_Y_OFFSET, CommonColors.WHITE);
		}
	}

	protected abstract class ShortcutEntry<T> extends AbstractShortcutEntry {
		protected final ShortcutCategoryEntry<T> category;
		protected final EditBox replacement;

		private ShortcutEntry(ShortcutCategoryEntry<T> category, T targetKey) {
			this.category = category;
			replacement = new EditBox(Minecraft.getInstance().font, width / 2 + 10, TEXT_Y_OFFSET, 150, 20, category.replacementName);
			replacement.setMaxLength(48);
			replacement.setValue(category.shortcutsMap.getOrDefault(targetKey, ""));
		}

		protected abstract boolean isNotEmpty();

		protected abstract boolean isChanged();

		protected abstract void save();

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			replacement.setY(this.getY() + TEXT_FIELD_PADDING);
			replacement.render(context, mouseX, mouseY, deltaTicks);
			context.drawCenteredString(minecraft.font, "→", this.getX() + this.getWidth() / 2, this.getY() + TEXT_Y_OFFSET, CommonColors.WHITE);
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			replacement.setX(width / 2 + 10);
		}
	}

	protected class CommandShortcutEntry extends ShortcutEntry<String> {
		private final List<EditBox> children;
		private final EditBox target;

		private CommandShortcutEntry(ShortcutCategoryEntry<String> category) {
			this(category, "");
		}

		private CommandShortcutEntry(ShortcutCategoryEntry<String> category, String targetString) {
			super(category, targetString);
			target = new EditBox(Minecraft.getInstance().font, width / 2 - 160, TEXT_Y_OFFSET, 150, 20, category.targetName);
			target.setMaxLength(48);
			target.setValue(targetString);
			children = List.of(target, replacement);
		}

		@Override
		public String toString() {
			return target.getValue() + " → " + replacement.getValue();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		protected boolean isNotEmpty() {
			return !target.getValue().isEmpty() && !replacement.getValue().isEmpty();
		}

		@Override
		protected boolean isChanged() {
			return !category.shortcutsMap.containsKey(target.getValue()) || !category.shortcutsMap.get(target.getValue()).equals(replacement.getValue());
		}

		@Override
		protected void save() {
			category.shortcutsMap.put(target.getValue(), replacement.getValue());
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			super.renderContent(context, mouseX, mouseY, hovered, deltaTicks);
			target.setY(this.getY() + TEXT_FIELD_PADDING);
			target.render(context, mouseX, mouseY, deltaTicks);
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			target.setX(width / 2 - 160);
		}
	}

	protected class KeybindShortcutEntry extends ShortcutEntry<ShortcutKeyBinding> {
		private final List<AbstractWidget> children;
		private final ShortcutKeyBinding keyBinding;
		private final KeybindWidget keybindButton;
		private boolean duplicate = false;

		private KeybindShortcutEntry(ShortcutCategoryEntry<ShortcutKeyBinding> category) {
			this(category, new ShortcutKeyBinding(List.of(InputConstants.UNKNOWN)));
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsList.KeyEntry#KeyEntry(KeyBinding, Text) ControlsListWidget.KeyBindingEntry#KeyBindingEntry(KeyBinding, Text)}
		 */
		@SuppressWarnings("JavadocReference")
		private KeybindShortcutEntry(ShortcutCategoryEntry<ShortcutKeyBinding> category, ShortcutKeyBinding keyBinding) {
			super(category, keyBinding);
			this.keyBinding = keyBinding;
			keybindButton = new KeybindWidget(keyBinding, width / 2 - 160, TEXT_Y_OFFSET, 150, 20, keyBinding.getBoundKeysText(),
					textSupplier -> keyBinding.isUnbound()
							? Component.translatable("narrator.controls.unbound", replacement.getValue())
							: Component.translatable("narrator.controls.bound", replacement.getValue(), textSupplier.get()),
					ShortcutsConfigListWidget.this::updateKeybinds);
			// The duplicate warning tooltip displays replacement commands and needs to be updated.
			replacement.setResponder(command -> ShortcutsConfigListWidget.this.updateKeybinds());
			children = List.of(keybindButton, replacement);
			update();
		}

		@Override
		public String toString() {
			// This is used in the delete warning screen, so we use the localized text.
			return keyBinding.getBoundKeysText().getString() + " → " + replacement.getValue();
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		protected boolean isNotEmpty() {
			return !keyBinding.isUnbound() && !replacement.getValue().isEmpty();
		}

		@Override
		protected boolean isChanged() {
			return !category.shortcutsMap.containsKey(keyBinding) || !category.shortcutsMap.get(keyBinding).equals(replacement.getValue());
		}

		@Override
		protected void save() {
			category.shortcutsMap.put(keyBinding, replacement.getValue());
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsList.KeyEntry#renderContent(GuiGraphics, int, int, boolean, float) ControlsListWidget.KeyBindingEntry#render(DrawContext, int, int, int, int, int, int, int, boolean, float)}.
		 */
		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			super.renderContent(context, mouseX, mouseY, hovered, deltaTicks);
			keybindButton.setY(this.getY() + TEXT_FIELD_PADDING);
			keybindButton.render(context, mouseX, mouseY, deltaTicks);
			if (duplicate) {
				context.fill(keybindButton.getX() - 6, this.getY(), keybindButton.getX() - 3, this.getY() + this.getHeight(), CommonColors.YELLOW);
			}
		}

		@Override
		protected void updatePositions() {
			super.updatePositions();
			keybindButton.setX(width / 2 - 160);
		}

		/**
		 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsList.KeyEntry#resetMappingAndUpdateButtons() ControlsListWidget.KeyBindingEntry#update()}.
		 */
		@SuppressWarnings("JavadocReference")
		protected void update() {
			keybindButton.setMessage(keyBinding.getBoundKeysText());
			duplicate = false;
			MutableComponent text = Component.empty();
			if (!keyBinding.isUnbound()) {
				// Check for conflicts with regular keybinds
				for (KeyMapping otherKeyBinding : minecraft.options.keyMappings) {
					if (keyBinding.getBoundKeysTranslationKey().contains(otherKeyBinding.saveString())) {
						if (duplicate) {
							text.append(", ");
						}
						duplicate = true;
						text.append(Component.translatable(otherKeyBinding.getName()));
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
						text.append(keyBindingShortcut.replacement.getValue());
					}
				}
			}

			if (duplicate) {
				keybindButton.setMessage(Component.literal("[ ")
						.append(keybindButton.getMessage().copy().withStyle(ChatFormatting.WHITE))
						.append(" ]")
						.withStyle(ChatFormatting.RED));
				keybindButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", text)));
			} else {
				keybindButton.setTooltip(null);
			}

			if (keybindButton.isEditing()) {
				keybindButton.setMessage(Component.literal("> ")
						.append(keybindButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
						.append(" <")
						.withStyle(ChatFormatting.YELLOW)
				);
			}
		}

		protected boolean stopEditing() {
			return keybindButton.stopEditing();
		}
	}
}
