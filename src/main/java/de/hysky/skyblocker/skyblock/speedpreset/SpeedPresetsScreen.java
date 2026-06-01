package de.hysky.skyblocker.skyblock.speedpreset;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class SpeedPresetsScreen extends Screen {
	protected final Screen parent;

	protected @Nullable HeaderAndFooterLayout layout;
	protected @Nullable SpeedPresetListWidget list;

	public SpeedPresetsScreen(Screen parent) {
		super(Component.translatable("skyblocker.config.general.speedPresets.config"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		if (this.list == null)
			this.list = new SpeedPresetListWidget(0, 0, 24);

		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		layout.addTitleHeader(this.title, font);

		list.setWidth(layout.getWidth());
		list.setHeight(layout.getContentHeight());
		layout.addToContents(this.list);

		LinearLayout footerLayout = LinearLayout.horizontal();
		footerLayout.addChild(Button.builder(CommonComponents.GUI_DONE,
						_ -> {
							this.list.save();
							this.minecraft.gui.setScreen(parent);
						})
				.width(Math.max(font.width(CommonComponents.GUI_DONE) + 8, 100))
				.build(), s -> s.paddingRight(2));
		footerLayout.addChild(Button.builder(Component.literal("+"),
						_ -> list.newEntry())
				.width(20)
				.build(), s -> s.paddingLeft(2));
		layout.addToFooter(footerLayout);

		layout.visitWidgets(this::addRenderableWidget);
		layout.arrangeElements();
		list.refreshScrollAmount();
		list.updatePosition();
	}

	@Override
	public void onClose() {
		if (this.list != null && this.list.hasBeenChanged()) {
			minecraft.gui.setScreen(new ConfirmScreen(confirmedAction -> {
				if (confirmedAction) {
					this.minecraft.gui.setScreen(parent);
				} else {
					this.minecraft.gui.setScreen(this);
				}
			}, Component.translatable("text.skyblocker.quit_config"), Component.translatable("text.skyblocker.quit_config_sure"), Component.translatable("text.skyblocker.quit_discard")
					.withStyle(ChatFormatting.RED), CommonComponents.GUI_CANCEL));
			return;
		}
		this.minecraft.gui.setScreen(parent);
	}
}
