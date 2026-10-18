package de.hysky.skyblocker.mixins.discordipc;

import java.io.IOException;

import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.connection.UnixConnection;
import meteordevelopment.discordipc.connection.WinConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.hysky.skyblocker.utils.discord.DiscordRPCManager;

@Mixin(value = { UnixConnection.class, WinConnection.class })
public class ConnectionMixin {
	@Redirect(method = "write", at = @At(value = "INVOKE", target = "Ljava/io/IOException;printStackTrace()V"))
	private void write(IOException e) {
		DiscordIPC.stop();
		DiscordRPCManager.LOGGER.warn("[Skyblocker] Discord RPC failed to update activity, connection lost", e);
	}
}
