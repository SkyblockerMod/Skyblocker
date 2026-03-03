package de.hysky.skyblocker.skyblock.speedpreset;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class SpeedPresetsScreen extends Screen {

	protected final Screen parent;
	protected SpeedPresetListWidget list;

	public SpeedPresetsScreen(Screen parent) {
		super(Component.translatable("skyblocker.config.general.speedPresets.config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (this.list == null)
			this.list = new SpeedPresetListWidget(0, 0, 24);
		this.list.setSize(this.width, this.height - 24 - 32);
		this.list.updatePosition();
		this.addRenderableWidget(this.list);

		var grid = new GridLayout();
		grid.spacing(4);
		var doneButton = Button.builder(CommonComponents.GUI_DONE,
						button -> {
							this.list.save();
							assert this.minecraft != null;
							this.minecraft.setScreen(parent);
						})
				.width(Math.max(font.width(CommonComponents.GUI_DONE) + 8, 100))
				.build();
		grid.addChild(doneButton, 0, 0, 1, 2);
		var plusButton = Button.builder(Component.literal("+"),
						button -> list.newEntry())
				.width(20)
				.build();
		grid.addChild(plusButton, 0, 2, 1, 1);
		grid.arrangeElements();
		FrameLayout.alignInRectangle(grid, 0, this.height - 24, this.width, 24, 0.5f, 0.5f);
		grid.visitWidgets(this::addRenderableWidget);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		assert this.minecraft != null;
		var renderer = this.minecraft.font;
		context.drawCenteredString(renderer, this.title, this.width / 2,
				8, CommonColors.WHITE);
	}

	@Override
	public void onClose() {
		assert this.minecraft != null;
		if (this.list.hasBeenChanged()) {
			minecraft.setScreen(new ConfirmScreen(confirmedAction -> {
				if (confirmedAction) {
					this.minecraft.setScreen(parent);
				} else {
					this.minecraft.setScreen(this);
				}
			}, Component.translatable("text.skyblocker.quit_config"), Component.translatable("text.skyblocker.quit_config_sure"), Component.translatable("text.skyblocker.quit_discard")
					.withStyle(ChatFormatting.RED), CommonComponents.GUI_CANCEL));
			return;
		}
		this.minecraft.setScreen(parent);
	}
}
