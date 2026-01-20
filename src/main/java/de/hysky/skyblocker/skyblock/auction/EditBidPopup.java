package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.calculators.SignCalculator;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class EditBidPopup extends AbstractPopupScreen {
	private LinearLayout layout = LinearLayout.vertical();
	private final String minimumBid;
	private final SignBlockEntity signBlockEntity;

	private final boolean signFront;

	private EditBox textFieldWidget;

	private boolean packetSent = false;

	public EditBidPopup(AuctionViewScreen auctionViewScreen, SignBlockEntity signBlockEntity, boolean signFront, String minimumBid) {
		super(Component.literal("Edit Bid"), auctionViewScreen);
		this.minimumBid = minimumBid;
		this.signBlockEntity = signBlockEntity;
		this.signFront = signFront;
	}

	@Override
	protected void init() {
		super.init();
		layout = LinearLayout.vertical();
		layout.spacing(8).defaultCellSetting().alignHorizontallyCenter();
		textFieldWidget = new EnterConfirmTextFieldWidget(font, 120, 15, Component.empty(), () -> done(null));
		textFieldWidget.setFilter(this::isStringGood);
		layout.addChild(new StringWidget(Component.literal("- Set Bid -").withStyle(Style.EMPTY.withBold(true)), font));
		layout.addChild(textFieldWidget);
		layout.addChild(new StringWidget(Component.literal("Minimum Bid: " + minimumBid), font));
		LinearLayout horizontal = LinearLayout.horizontal();
		Button buttonWidget = Button.builder(Component.literal("Set Minimum Bid"), this::buttonMinimumBid).width(80).build();
		buttonWidget.active = isStringGood(minimumBid);
		horizontal.addChild(buttonWidget);
		horizontal.addChild(Button.builder(Component.literal("Done"), this::done).width(80).build());
		layout.addChild(horizontal);
		layout.visitWidgets(this::addRenderableWidget);
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(layout, this.getRectangle());
		setInitialFocus(textFieldWidget);
	}

	@Override
	public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled) {
			SignCalculator.renderCalculator(context, textFieldWidget.getValue(), context.guiWidth() / 2, textFieldWidget.getY() - 8);
		}
	}

	private boolean isStringGood(String s) {
		assert this.minecraft != null;
		return this.minecraft.font.width(minimumBid) <= this.signBlockEntity.getMaxTextLineWidth();
	}

	private void buttonMinimumBid(Button widget) {
		if (!isStringGood(minimumBid)) return;
		sendPacket(minimumBid);
		this.onClose();
	}

	private void done(Button widget) {
		if (SkyblockerConfigManager.get().uiAndVisuals.inputCalculator.enabled) {
			if (!isStringGood(SignCalculator.getNewValue(false))) return;
			sendPacket(SignCalculator.getNewValue(false));
		} else {
			if (!isStringGood(textFieldWidget.getValue().trim())) return;
			sendPacket(textFieldWidget.getValue().trim());
		}
		this.onClose();
	}

	private void sendPacket(String string) {
		assert Minecraft.getInstance().player != null;
		Minecraft.getInstance().player.connection.send(new ServerboundSignUpdatePacket(signBlockEntity.getBlockPos(), signFront,
				string.replace("coins", ""),
				"",
				"",
				""
		));
		packetSent = true;
	}

	@Override
	public void onClose() {
		if (!packetSent) sendPacket("");
		assert this.minecraft != null;
		this.minecraft.setScreen(null);
	}

	@Override
	public void removed() {
		if (!packetSent) sendPacket("");
		super.removed();
	}
}
