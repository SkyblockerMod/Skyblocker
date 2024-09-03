package de.hysky.skyblocker.utils.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.barn.HungryHiker;
import de.hysky.skyblocker.skyblock.barn.TreasureHunter;
import de.hysky.skyblocker.skyblock.dungeon.Reparty;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.Trivia;
import de.hysky.skyblocker.skyblock.dwarven.Fetchur;
import de.hysky.skyblocker.skyblock.dwarven.Puzzler;
import de.hysky.skyblocker.skyblock.filters.*;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@FunctionalInterface
public interface ChatMessageListener {
    /**
     * An event called when a game message is received. Register your listeners in {@link ChatMessageListener#init()}.
     */
    Event<ChatMessageListener> EVENT = EventFactory.createArrayBacked(ChatMessageListener.class,
            (listeners) -> (message, asString) -> {
                for (ChatMessageListener listener : listeners) {
                    ChatFilterResult result = listener.onMessage(message, asString);
                    if (result != ChatFilterResult.PASS) return result;
                }
                return ChatFilterResult.PASS;
            });

    /**
     * Registers {@link ChatMessageListener}s to {@link ChatMessageListener#EVENT} and registers {@link ChatMessageListener#EVENT} to {@link ClientReceiveMessageEvents#ALLOW_GAME}
     */
    @Init
    static void init() {
        ChatMessageListener[] listeners = new ChatMessageListener[]{
                // Features
                new Fetchur(),
                new Puzzler(),
                new Reparty(),
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
                new ShowOffFilter(),
                new ToggleSkyMallFilter(),
                new MimicFilter(),
                new DeathFilter(),
                new DicerFilter()
        };
        // Register all listeners to EVENT
        for (ChatMessageListener listener : listeners) {
            EVENT.register(listener);
        }
        // Register EVENT to ClientReceiveMessageEvents.ALLOW_GAME from fabric api
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!Utils.isOnSkyblock()) {
                return true;
            }
            ChatFilterResult result = EVENT.invoker().onMessage(message, Formatting.strip(message.getString()));
            switch (result) {
                case ACTION_BAR -> {
                    if (overlay) {
                        return true;
                    }
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player != null) {
                        player.sendMessage(message, true);
                        return false;
                    }
                }
                case FILTER -> {
                    return false;
                }
            }
            return true;
        });
    }

    ChatFilterResult onMessage(Text message, String asString);
}
