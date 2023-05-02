package me.xmrvizzy.skyblocker.chat;

import me.xmrvizzy.skyblocker.chat.filters.*;
import me.xmrvizzy.skyblocker.skyblock.api.ApiKeyListener;
import me.xmrvizzy.skyblocker.skyblock.barn.HungryHiker;
import me.xmrvizzy.skyblocker.skyblock.dungeon.Reparty;
import me.xmrvizzy.skyblocker.skyblock.dungeon.ThreeWeirdos;
import me.xmrvizzy.skyblocker.skyblock.dungeon.Trivia;
import me.xmrvizzy.skyblocker.skyblock.dwarven.Fetchur;
import me.xmrvizzy.skyblocker.skyblock.dwarven.Puzzler;
import me.xmrvizzy.skyblocker.skyblock.barn.TreasureHunter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;

public interface ChatMessageListener {
    Event<ChatMessageListener> EVENT = EventFactory.createArrayBacked(ChatMessageListener.class,
            (listeners) -> (message, asString) -> {
                for (ChatMessageListener listener : listeners) {
                    ChatFilterResult result = listener.onMessage(message, asString);
                    if (result != ChatFilterResult.PASS) return result;
                }
                return ChatFilterResult.PASS;
            });

    static void init() {
        ChatMessageListener[] listeners = new ChatMessageListener[]{
                // Features
                new ApiKeyListener(),
                new Fetchur(),
                new Puzzler(),
                new Reparty(),
                new ThreeWeirdos(),
                new Trivia(),
                new TreasureHunter(),
                new HungryHiker(),
                // Filters
                new AbilityFilter(),
                new AdFilter(),
                new AoteFilter(),
                new ComboFilter(),
                new HealFilter(),
                new ImplosionFilter(),
                new MoltenWaveFilter(),
                new TeleportPadFilter(),
                new AutopetFilter(),
        };
        for (ChatMessageListener listener : listeners)
            EVENT.register(listener);
    }

    ChatFilterResult onMessage(Text message, String asString);
}
