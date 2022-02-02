package me.xmrvizzy.skyblocker.chat.chatevents;

import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.utils.ToastBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

public class AuctionSoldEvent extends ChatListener{
    private final TranslatableText toastTitle = new TranslatableText("skyblocker.auction_sold.title");
    private final TranslatableText toastDesc = new TranslatableText("skyblocker.auction_sold.desc");

    public AuctionSoldEvent() {
        super("/^(?=.*Auction)(?=.*bought).*$/gm");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean onMessage(String[] groups) {
        MinecraftClient.getInstance().getToastManager().add(new ToastBuilder(toastTitle, toastDesc));
        return true;
    }
}
