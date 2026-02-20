package de.hysky.skyblocker.skyblock.profileviewer2.utils;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * {@link RemotePlayer} extension to support displaying the player in a Profile Viewer widget.
 */
public class ProfileViewerPlayer extends RemotePlayer {

	public ProfileViewerPlayer(GameProfile gameProfile) {
		super(Minecraft.getInstance().level, gameProfile);
		this.setCustomNameVisible(false);
	}

	@Override
	public PlayerSkin getSkin() {
		PlayerInfo playerInfo = new PlayerInfo(this.getGameProfile(), false);
		return playerInfo.getSkin();
	}

	@Override
	public boolean isModelPartShown(PlayerModelPart modelPart) {
		return modelPart != PlayerModelPart.CAPE;
	}

	@Override
	public boolean isInvisibleTo(Player player) {
		return true;
	}
}
