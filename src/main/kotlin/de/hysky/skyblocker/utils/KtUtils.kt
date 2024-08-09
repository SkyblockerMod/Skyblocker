package de.hysky.skyblocker.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.text.Text

//These fields can be static imported, but the get method is called each time the field is accessed. Essentially a shortcut.
val client: MinecraftClient get() = MinecraftClient.getInstance()
val player get() = client.player
val world get() = client.world

object KtUtil {
	/**
	 * Sends a message to the player with the [Skyblocker prefix][Constants.PREFIX].
	 *
	 * @param text The text to send.
	 * @param overlay Whether the message should be displayed as an overlay. Defaults to `false`.
	 */
	fun ClientPlayerEntity.sendSkyblockerMessage(text: Text, overlay: Boolean = false) = sendMessage(Constants.PREFIX.get().append(text), overlay)
}
