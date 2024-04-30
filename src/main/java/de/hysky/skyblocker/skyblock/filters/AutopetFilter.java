package de.hysky.skyblocker.skyblock.filters;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.regex.Matcher;

public class AutopetFilter extends ChatPatternListener {
    public AutopetFilter() {
        super("^Autopet equipped your .*! VIEW RULE$");
    }

    @Override
    public boolean onMatch(Text _message, Matcher matcher) {
        if (SkyblockerConfigManager.get().messages.hideAutopet == ChatFilterResult.ACTION_BAR) {
            Objects.requireNonNull(MinecraftClient.getInstance().player).sendMessage(
                    Text.literal(
                            _message.getString().replace("VIEW RULE", "")
                    ), true);
        }
        return true;
    }

    @Override
    public ChatFilterResult state() {
        if (SkyblockerConfigManager.get().messages.hideAutopet == ChatFilterResult.ACTION_BAR)
            return ChatFilterResult.FILTER;
        else
            return SkyblockerConfigManager.get().messages.hideAutopet;
    }
}