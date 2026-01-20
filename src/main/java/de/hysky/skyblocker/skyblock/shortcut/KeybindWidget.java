package de.hysky.skyblocker.skyblock.shortcut;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics.HoveredTextEffects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class KeybindWidget extends Button {
	private final ShortcutKeyBinding keyBinding;
	private final Runnable updateListener;
	private boolean editing;

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Component message, Runnable updateListener) {
		this(keyBinding, x, y, width, height, message, DEFAULT_NARRATION, updateListener);
	}

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Component message, CreateNarration narrationSupplier, Runnable updateListener) {
		this(keyBinding, x, y, width, height, message, button -> {}, narrationSupplier, updateListener);
	}

	protected KeybindWidget(ShortcutKeyBinding keyBinding, int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration narrationSupplier, Runnable updateListener) {
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
	public void onPress(InputWithModifiers input) {
		editing = true;
		keyBinding.clearBoundKeys();
		updateListener.run();
		super.onPress(input);
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsScreen#mouseClicked(MouseButtonEvent, boolean) KeybindsScreen#mouseClicked(Click, boolean)}.
	 */
	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (editing) {
			keyBinding.addBoundKey(InputConstants.Type.MOUSE.getOrCreate(click.button()));
			updateListener.run();
			return true;
		}
		return super.mouseClicked(click, doubled);
	}

	/**
	 * Modified from {@link net.minecraft.client.gui.screens.options.controls.KeyBindsScreen#keyPressed(KeyEvent) KeybindsScreen#keyPressed(KeyInput)}.
	 */
	@Override
	public boolean keyPressed(KeyEvent input) {
		if (editing) {
			if (input.isEscape()) {
				// This should never happen because ESC is handled in ShortcutsConfigScreen#keyPressed
				keyBinding.addBoundKey(InputConstants.UNKNOWN);
			} else {
				keyBinding.addBoundKey(InputConstants.getKey(input));
			}
			updateListener.run();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		this.renderDefaultSprite(context);
		this.renderDefaultLabel(context.textRenderer(HoveredTextEffects.NONE));
	}
}
