package me.xmrvizzy.skyblocker.skyblock.filters;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.chat.ChatFilterResult;

public class MoltenWaveFilter extends SimpleChatFilter {
    public MoltenWaveFilter() {
        super("^Your Molten Wave hit " + NUMBER + " enem(?:y|ies) for " + NUMBER + " damage\\.$");
    }

    @Override
    public ChatFilterResult state() {
        return SkyblockerConfig.get().messages.hideMoltenWave;
    }
}
