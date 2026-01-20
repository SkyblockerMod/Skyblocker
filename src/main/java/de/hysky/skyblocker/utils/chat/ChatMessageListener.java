package de.hysky.skyblocker.utils.chat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.barn.CallTrevor;
import de.hysky.skyblocker.skyblock.barn.HungryHiker;
import de.hysky.skyblocker.skyblock.barn.TreasureHunter;
import de.hysky.skyblocker.skyblock.chat.filters.AbilityFilter;
import de.hysky.skyblocker.skyblock.chat.filters.AdFilter;
import de.hysky.skyblocker.skyblock.chat.filters.AoteFilter;
import de.hysky.skyblocker.skyblock.chat.filters.AutopetFilter;
import de.hysky.skyblocker.skyblock.chat.filters.ComboFilter;
import de.hysky.skyblocker.skyblock.chat.filters.DeathFilter;
import de.hysky.skyblocker.skyblock.chat.filters.DungeonBreakerFilter;
import de.hysky.skyblocker.skyblock.chat.filters.HealFilter;
import de.hysky.skyblocker.skyblock.chat.filters.ImplosionFilter;
import de.hysky.skyblocker.skyblock.chat.filters.LotteryFilter;
import de.hysky.skyblocker.skyblock.chat.filters.MimicFilter;
import de.hysky.skyblocker.skyblock.chat.filters.MoltenWaveFilter;
import de.hysky.skyblocker.skyblock.chat.filters.ShowOffFilter;
import de.hysky.skyblocker.skyblock.chat.filters.SkyMallFilter;
import de.hysky.skyblocker.skyblock.chat.filters.TeleportPadFilter;
import de.hysky.skyblocker.skyblock.dungeon.Reparty;
import de.hysky.skyblocker.skyblock.dwarven.CallMismyla;
import de.hysky.skyblocker.skyblock.dwarven.RedialOnBadSignal;
import de.hysky.skyblocker.skyblock.dwarven.Fetchur;
import de.hysky.skyblocker.skyblock.dwarven.Puzzler;
import de.hysky.skyblocker.skyblock.galatea.SweepDetailsListener;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

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
	@SuppressWarnings("incomplete-switch")
	@Init
	static void init() {
		ChatMessageListener[] listeners = new ChatMessageListener[]{
				// Features
				new Fetchur(),
				new Puzzler(),
				new Reparty(),
				new TreasureHunter(),
				new HungryHiker(),
				new SweepDetailsListener(),
				new CallTrevor(),
				new CallMismyla(),
				new RedialOnBadSignal(),
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
				new SkyMallFilter(),
				new LotteryFilter(),
				new MimicFilter(),
				new DeathFilter(),
				new DungeonBreakerFilter(),
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

			ChatFilterResult result = EVENT.invoker().onMessage(message, ChatFormatting.stripFormatting(message.getString()));

			switch (result) {
				case ACTION_BAR -> {
					if (overlay) {
						return true;
					}

					LocalPlayer player = Minecraft.getInstance().player;

					if (player != null) {
						player.displayClientMessage(message, true);

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

	ChatFilterResult onMessage(Component message, String asString);
}
