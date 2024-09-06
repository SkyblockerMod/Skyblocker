package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.chat.ChatFilterResult;
import de.hysky.skyblocker.utils.chat.ChatPatternListener;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.azureaaron.hmapi.data.party.PartyRole;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v2.s2c.PartyInfoS2CPacket;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;

import org.slf4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.logging.LogUtils;

public class Reparty extends ChatPatternListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final int BASE_DELAY = 10;

	private boolean repartying;
	private String partyLeader;

	public Reparty() {
		super("^(?:([\\[A-z+\\]]* )?(?<disband>.*) has disbanded .*" +
				"|.*\n([\\[A-z+\\]]* )?(?<invite>.*) has invited you to join their party!" +
				"\nYou have 60 seconds to accept. Click here to join!\n.*)$");

		this.repartying = false;
		HypixelPacketEvents.PARTY_INFO.register(this::onPacket);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("rp").executes(context -> {
			if (!Utils.isOnSkyblock() || this.repartying || CLIENT.player == null) return 0;

			this.repartying = true;
			HypixelNetworking.sendPartyInfoC2SPacket(2);

			return Command.SINGLE_SUCCESS;
		})));
	}

	private void onPacket(HypixelS2CPacket packet) {
		switch (packet) {
			case PartyInfoS2CPacket(var inParty, var members) when this.repartying -> {
				UUID ourUuid = Objects.requireNonNull(CLIENT.getSession().getUuidOrNull());

				if (inParty && members.get(ourUuid) == PartyRole.LEADER) {
					sendCommand("/p disband", 1);
					int count = 0;

					for (Map.Entry<UUID, PartyRole> entry : members.entrySet()) {
						UUID uuid = entry.getKey();
						PartyRole role = entry.getValue();

						//Don't invite ourself
						if (role != PartyRole.LEADER) sendCommand("/p " + uuid.toString(), ++count + 2);
					}

					Scheduler.INSTANCE.schedule(() -> this.repartying = false, count * BASE_DELAY);
				} else {
					CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.reparty.notInPartyOrNotLeader")));
					this.repartying = false;
				}
			}

			case ErrorS2CPacket(var id, var error) when id.equals(PartyInfoS2CPacket.ID) && this.repartying -> {
				CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.reparty.error")));
				LOGGER.error("[Skyblocker Reparty] The party info packet returned an unexpected error! {}", error);

				this.repartying = false;
			}

			default -> {} //Do nothing
		}
	}

	@Override
	public ChatFilterResult state() {
		return SkyblockerConfigManager.get().general.acceptReparty ? ChatFilterResult.FILTER : ChatFilterResult.PASS;
	}

	@Override
	public boolean onMatch(Text message, Matcher matcher) {
		if (matcher.group("disband") != null && !matcher.group("disband").equals(CLIENT.getSession().getUsername())) {
			partyLeader = matcher.group("disband");
			Scheduler.INSTANCE.schedule(() -> partyLeader = null, 61);
		} else if (matcher.group("invite") != null && matcher.group("invite").equals(partyLeader)) {
			String command = "/party accept " + partyLeader;
			sendCommand(command, 0);
		}

		return false;
	}

	private void sendCommand(String command, int delay) {
		MessageScheduler.INSTANCE.queueMessage(command, false, delay * BASE_DELAY);
	}
}