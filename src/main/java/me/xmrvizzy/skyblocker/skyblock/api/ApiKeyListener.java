package me.xmrvizzy.skyblocker.skyblock.api;

import me.shedaniel.autoconfig.AutoConfig;
import me.xmrvizzy.skyblocker.chat.ChatFilterResult;
import me.xmrvizzy.skyblocker.chat.ChatPatternListener;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;

public class ApiKeyListener extends ChatPatternListener {
    public ApiKeyListener() {
        super("^Your new API key is (.*)$");
    }

    @Override
    protected ChatFilterResult state() {
        return null;
    }

    @Override
    protected boolean onMatch(Text message, Matcher matcher) {
        SkyblockerConfig.get().general.apiKey = matcher.group(1);
        AutoConfig.getConfigHolder(SkyblockerConfig.class).save();
        MinecraftClient.getInstance().player.sendMessage(Text.translatable("skyblocker.api.got_key"), false);
        return false;
    }
}
