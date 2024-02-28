package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.utils.render.gui.BarebonesPopupScreen;
import de.hysky.skyblocker.utils.render.gui.HandlerSignBackedScreen;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class EditBidPopup extends BarebonesPopupScreen {

    private final AuctionViewScreen auctionViewScreen;

    private DirectionalLayoutWidget layout = DirectionalLayoutWidget.vertical();
    private final String minimumBid;
    private final SignBlockEntity signBlockEntity;

    private TextFieldWidget textFieldWidget;

    private boolean packetSent = false;

    protected EditBidPopup(AuctionViewScreen auctionViewScreen, @NotNull SignBlockEntity signBlockEntity, String minimumBid) {
        super(Text.literal("Edit Bid"), auctionViewScreen);
        this.auctionViewScreen = auctionViewScreen;
        this.minimumBid = minimumBid;
        this.signBlockEntity = signBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        layout = DirectionalLayoutWidget.vertical();
        layout.spacing(8).getMainPositioner().alignHorizontalCenter();
        textFieldWidget = new TextFieldWidget(textRenderer, 120, 15, Text.empty());
        textFieldWidget.setTextPredicate(this::isStringGood);
        layout.add(new TextWidget(Text.literal("- Set Bid -").fillStyle(Style.EMPTY.withBold(true)), textRenderer));
        layout.add(textFieldWidget);
        layout.add(new TextWidget(Text.literal("Minimum Bid: " + minimumBid), textRenderer));
        DirectionalLayoutWidget horizontal = DirectionalLayoutWidget.horizontal();
        ButtonWidget buttonWidget = ButtonWidget.builder(Text.literal("Set Minimum Bid"), this::buttonMinimumBid).width(80).build();
        buttonWidget.active = isStringGood(minimumBid);
        horizontal.add(buttonWidget);
        horizontal.add(ButtonWidget.builder(Text.literal("Done"), this::done).width(80).build());
        layout.add(horizontal);
        layout.forEachChild(this::addDrawableChild);
        this.layout.refreshPositions();
        SimplePositioningWidget.setPos(layout, this.getNavigationFocus());
        setInitialFocus(textFieldWidget);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        drawPopupBackground(context, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
    }

    private boolean isStringGood(String s) {
        return this.client.textRenderer.getWidth(minimumBid) <= this.signBlockEntity.getMaxTextWidth();
    }

    private void buttonMinimumBid(ButtonWidget widget) {
        if (!isStringGood(minimumBid)) return;
        sendPacket(minimumBid);
        this.close();
    }

    private void done(ButtonWidget widget) {
        if(!isStringGood(textFieldWidget.getText().trim())) return;
        sendPacket(textFieldWidget.getText().trim());
        this.close();
    }

    private void sendPacket(String string) {
        auctionViewScreen.sendSignPacket(string.trim());
        packetSent = true;
    }

    @Override
    public void close() {
        if (!packetSent) sendPacket("");
        super.close();
    }

    @Override
    public void removed() {
        if (!packetSent) sendPacket("");
        super.removed();
    }
}
