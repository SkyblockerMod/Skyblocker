package de.hysky.skyblocker.skyblock.item.custom.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ArmorPresetsScreen extends Screen {
	private final Screen parent;
	private ArmorPresetListWidget list;
	private double savedScroll;

	public ArmorPresetsScreen(Screen parent) {
		super(Text.translatable("skyblocker.armorPresets.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int listWidth = (int) (width * 0.7f);
		int listHeight = (int) (height * 0.7f);
		int listX = (width - listWidth) / 2;
		int listY = (height - listHeight) / 2;
		if (list != null) {
			savedScroll = list.getScrollY();
		}
		list = new ArmorPresetListWidget(listWidth, listHeight, listY, this::returnToParent);
		list.setX(listX);
		addDrawableChild(list);
		list.setScrollY(savedScroll);
		if (parent instanceof CustomizeArmorScreen cas) {
			ButtonWidget save = ButtonWidget.builder(Text.translatable("skyblocker.armorPresets.save"), b -> {
						b.active = false;
						var future = cas.savePresetAsync();
						// rebuild the list and scroll after clearing old preview data
						ArmorPresetCardWidget.clearTempData();
						list.refresh();
						list.setScrollY(list.getMaxScrollY());
						future.thenRunAsync(() -> b.active = true, MinecraftClient.getInstance());
					})
					.width(100).position(width / 2 - 50, listY + listHeight + 4).build();
			addDrawableChild(save);
		}
		ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, b -> returnToParent())
				.width(100).position(width / 2 - 50, height - 20).build();
		addDrawableChild(done);
	}

	private void returnToParent() {
		ArmorPresetCardWidget.clearTempData();
		client.setScreen(parent);
		if (parent instanceof CustomizeArmorScreen cas) {
			cas.updateWidgets();
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);
	}

	@Override
	public void close() {
		returnToParent();
	}
}
