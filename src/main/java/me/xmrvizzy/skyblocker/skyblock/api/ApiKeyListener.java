package me.xmrvizzy.skyblocker.skyblock.api;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.chat.ChatListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

public class ApiKeyListener extends ChatListener {
    public ApiKeyListener() {
        super("^Your new API key is (.*)$");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean onMessage(String[] groups) {
        SkyblockerConfig.get().general.apiKey = groups[1];
        AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        MinecraftClient.getInstance().player.sendMessage(new TranslatableText("skyblocker.api.got_key"), false);
        return false;
    }
}
