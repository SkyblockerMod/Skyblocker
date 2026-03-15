package de.hysky.skyblocker.skyblock.slayers.partycounter;

import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v2.s2c.PartyInfoS2CPacket;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PartyTracker {
		private static final Logger LOGGER = LogUtils.getLogger();
		private static final Minecraft CLIENT = Minecraft.getInstance();
		private static final Set<UUID> PARTY_MEMBERS = Collections.synchronizedSet(new HashSet<>());
		private static final long REQUEST_COOLDOWN = 2000L;

		private static volatile boolean inParty = false;
		private static long lastRequestTime = 0;

		private PartyTracker() {
		}

		public static void init() {
				HypixelPacketEvents.PARTY_INFO.register(PartyTracker::onPacket);
		}

		private static void onPacket(HypixelS2CPacket packet) {
				switch (packet) {
						case PartyInfoS2CPacket(var isInParty, var members) -> {
								inParty = isInParty;
								PARTY_MEMBERS.clear();
								if (isInParty && members != null) {
										PARTY_MEMBERS.addAll(members.keySet());
										LOGGER.debug("[Skyblocker Party Counter] Party updated with {} members", members.size());
								}
						}
						case ErrorS2CPacket(var id, var error) when id.equals(PartyInfoS2CPacket.ID) -> {
								LOGGER.warn("[Skyblocker Party Counter] Party info error: {}", error);
						}
						default -> {}
				}
		}

		public static void requestPartyInfo() {
				long now = System.currentTimeMillis();
				if (now - lastRequestTime < REQUEST_COOLDOWN) return;
				try {
						HypixelNetworking.sendPartyInfoC2SPacket(2);
						lastRequestTime = now;
				} catch (UnsupportedOperationException e) {
						LOGGER.debug("[Skyblocker Party Counter] Cannot request party info: not on Hypixel");
				}
		}

		public static boolean isInParty() {
				return inParty;
		}

		public static boolean isPartyMember(UUID uuid) {
				if (uuid == null) return false;
				if (CLIENT.player != null && CLIENT.getUser().getProfileId().equals(uuid)) return true;
				return PARTY_MEMBERS.contains(uuid);
		}

		public static Set<UUID> getPartyMemberUuids() {
				return Collections.unmodifiableSet(new HashSet<>(PARTY_MEMBERS));
		}

		public static void clearPartyData() {
				PARTY_MEMBERS.clear();
				inParty = false;
		}
}
