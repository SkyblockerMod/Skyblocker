package de.hysky.skyblocker.skyblock.speedPreset;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

public class SpeedPresetsScreen extends Screen {

	protected final Screen parent;
	protected SpeedPresetListWidget list;

	public SpeedPresetsScreen(Screen parent) {
		super(Text.translatable("skyblocker.config.general.speedPresets.config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (this.list == null)
			this.list = new SpeedPresetListWidget(0, 0, 24);
		this.list.setDimensions(this.width, this.height - 24 - 32);
		this.list.updatePosition();
		this.addDrawableChild(this.list);

		var grid = new GridWidget();
		grid.setSpacing(4);
		var doneButton = ButtonWidget.builder(ScreenTexts.DONE,
						button -> {
							this.list.save();
							assert this.client != null;
							this.client.setScreen(parent);
						})
				.width(Math.max(textRenderer.getWidth(ScreenTexts.DONE) + 8, 100))
				.build();
		grid.add(doneButton, 0, 0, 1, 2);
		var plusButton = ButtonWidget.builder(Text.literal("+"),
						button -> list.newEntry())
				.width(20)
				.build();
		grid.add(plusButton, 0, 2, 1, 1);
		grid.refreshPositions();
		SimplePositioningWidget.setPos(grid, 0, this.height - 24, this.width, 24, 0.5f, 0.5f);
		grid.forEachChild(this::addDrawableChild);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		assert this.client != null;
		var renderer = this.client.textRenderer;
		context.drawCenteredTextWithShadow(renderer, this.title, this.width / 2,
				8, Colors.WHITE);
	}

	@Override
	public void close() {
		assert this.client != null;
		if (this.list.hasBeenChanged()) {
			client.setScreen(new ConfirmScreen(confirmedAction -> {
				if (confirmedAction) {
					this.client.setScreen(parent);
				} else {
					this.client.setScreen(this);
				}
			}, Text.translatable("text.skyblocker.quit_config"), Text.translatable("text.skyblocker.quit_config_sure"), Text.translatable("text.skyblocker.quit_discard")
					.formatted(Formatting.RED), ScreenTexts.CANCEL));
			return;
		}
		this.client.setScreen(parent);
	}
}
