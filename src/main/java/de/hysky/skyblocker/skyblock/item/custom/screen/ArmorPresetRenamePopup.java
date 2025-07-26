package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreset;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPresets;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;


/**
 * Popup for renaming an armor preset.
 */
public class ArmorPresetRenamePopup extends AbstractPopupScreen {
	private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
	private final ArmorPreset preset;
	private final Runnable onDone;
	private EnterConfirmTextFieldWidget textField;

	public ArmorPresetRenamePopup(Screen background, ArmorPreset preset, Runnable onDone) {
		super(Text.translatable("skyblocker.armorPresets.rename"), background);
		this.preset = preset;
		this.onDone = onDone;
	}

	@Override
	protected void init() {
		super.init();
		layout = DirectionalLayoutWidget.vertical();
		layout.spacing(8).getMainPositioner().alignHorizontalCenter();
		textField = new EnterConfirmTextFieldWidget(this.textRenderer, 120, 15, Text.empty(), this::apply);
		textField.setText(preset.name());
		layout.add(new TextWidget(Text.translatable("skyblocker.armorPresets.renamePrompt").fillStyle(Style.EMPTY.withBold(true)), textRenderer));
		layout.add(textField);
		DirectionalLayoutWidget buttons = DirectionalLayoutWidget.horizontal();
		buttons.add(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> close()).width(60).build());
		buttons.add(ButtonWidget.builder(ScreenTexts.DONE, b -> apply()).width(60).build());
		layout.add(buttons);
		layout.forEachChild(this::addDrawableChild);
		layout.refreshPositions();
		SimplePositioningWidget.setPos(layout, this.getNavigationFocus());
		setInitialFocus(textField);
	}

	private void apply() {
		String newName = textField.getText().trim();
		if (!newName.isEmpty()) {
			ArmorPresets.getInstance().renamePreset(preset, newName);
			onDone.run();
			close();
		}
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
	}
}
