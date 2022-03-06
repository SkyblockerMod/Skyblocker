package me.xmrvizzy.skyblocker.chat.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class AutopetFilter extends ChatFilter {
    public AutopetFilter() {
        super("^§cAutopet §eequipped your §7.*§e! §a§lVIEW RULE$");
    }

    @Override
    public boolean isEnabled() {
        return SkyblockerConfig.get().messages.autopet != SkyblockerConfig.MsgOptions.Show;
    }

    @Override
    public boolean onMessage(String[] groups) {
        if (SkyblockerConfig.get().messages.autopet == SkyblockerConfig.MsgOptions.ActionBar) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText(groups[0].replace("§a§lVIEW RULE", "")), true);
        }
        return true;
    }
}