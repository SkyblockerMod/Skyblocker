package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Supplier;

public class RareRoomAlert {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Supplier<DungeonsConfig.RareRoomAlert> CONFIG = () -> SkyblockerConfigManager.get().dungeons.rareRoomAlert;
	private static final String TRINITY_ROOM_NAME = "trinity-4";
	private static final String TOMIOKA_ROOM_NAME = "tomioka-0";
	private static final String DUNCAN_ROOM_NAME = "duncan-1";

	@Init
	public static void init() {
		DungeonEvents.ROOM_MATCHED.register(room -> {
			if (!CONFIG.get().enabled) return;
			String roomName = room.getName();
			switch (roomName) {
				case TRINITY_ROOM_NAME -> {
					if (!CONFIG.get().showForTrinity) return;
				}
				case TOMIOKA_ROOM_NAME -> {
					if (!CONFIG.get().showForTomioka) return;
				}
				case DUNCAN_ROOM_NAME -> {
					if (!CONFIG.get().showForDuncan) return;
				}
				case null, default -> {
					return;
				}
			}
			showAlert(roomName);
		});
	}

	private static void showAlert(String roomId) {
		if (CLIENT.player == null) return;

		var roomData = DungeonManager.getRoomMetadata(roomId);
		String roomName = roomData != null ? roomData.name() : roomId;

		TitleContainer.addTitle(new Title(Component.literal(roomName.toUpperCase(Locale.ENGLISH)).withStyle(ChatFormatting.LIGHT_PURPLE)), 100);
		TitleContainer.playNotificationSound();

		CLIENT.player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.rareRoomAlert.foundRoom", roomName)));
	}
}
