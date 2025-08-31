package de.hysky.skyblocker.skyblock.shortcut;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class KeybindWidget extends ButtonWidget {
	private final ShortcutKeyBinding keyBinding;
	private final Runnable updateListener;
	private boolean editing;

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Text message, Runnable updateListener) {
		this(keyBinding, x, y, width, height, message, DEFAULT_NARRATION_SUPPLIER, updateListener);
	}

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Text message, NarrationSupplier narrationSupplier, Runnable updateListener) {
		this(keyBinding, x, y, width, height, message, button -> {}, narrationSupplier, updateListener);
	}

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier, Runnable updateListener) {
		super(x, y, width, height, message, onPress, narrationSupplier);
		this.keyBinding = keyBinding;
		this.updateListener = updateListener;
	}

	public boolean isEditing() {
		return editing;
	}

	public boolean stopEditing() {
		boolean wasEditing = editing;
		editing = false;
		return wasEditing;
	}

	@Override
	public void onPress() {
		editing = true;
		keyBinding.clearBoundKeys();
		updateListener.run();
		super.onPress();
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screen.option.KeybindsScreen#mouseClicked(double, double, int) KeybindsScreen#mouseClicked(double, double, int)}.
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (editing) {
			keyBinding.addBoundKey(InputUtil.Type.MOUSE.createFromCode(button));
			updateListener.run();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screen.option.KeybindsScreen#keyPressed(int, int, int) KeybindsScreen#keyPressed(int, int, int)}.
	 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (editing) {
			if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
				// This should never happen because ESC is handled in ShortcutsConfigScreen#keyPressed
				keyBinding.addBoundKey(InputUtil.UNKNOWN_KEY);
			} else {
				keyBinding.addBoundKey(InputUtil.fromKeyCode(keyCode, scanCode));
			}
			updateListener.run();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
