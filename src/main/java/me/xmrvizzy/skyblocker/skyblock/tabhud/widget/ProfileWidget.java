package me.xmrvizzy.skyblocker.skyblock.tabhud.widget;

import java.util.List;

import me.xmrvizzy.skyblocker.skyblock.tabhud.util.Ico;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.StrMan;
import me.xmrvizzy.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// this widget shows info about your profile and bank

public class ProfileWidget extends Widget {

    private static final MutableText TITLE = Text.literal("Profile").formatted(Formatting.YELLOW, Formatting.BOLD);

    public ProfileWidget(List<PlayerListEntry> list) {
        super(TITLE, Formatting.YELLOW.getColorValue());

        Text profileType = StrMan.stdEntry(list, 61, "Type:", Formatting.GREEN);
        IcoTextComponent profile = new IcoTextComponent(Ico.SIGN, profileType);
        this.addComponent(profile);

        Text petInfo = StrMan.stdEntry(list, 62, "Pet Sitter:", Formatting.AQUA);
        IcoTextComponent pet = new IcoTextComponent(Ico.BONE, petInfo);
        this.addComponent(pet);

        Text bankAmt = StrMan.stdEntry(list, 63, "Balance:", Formatting.GOLD);
        IcoTextComponent bank = new IcoTextComponent(Ico.EMERALD, bankAmt);
        this.addComponent(bank);

        Text interest = StrMan.stdEntry(list, 64, "Interest in:", Formatting.GOLD);
        IcoTextComponent inter = new IcoTextComponent(Ico.CLOCK, interest);
        this.addComponent(inter);
        this.pack();
    }


}
