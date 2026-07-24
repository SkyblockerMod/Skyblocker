package de.hysky.skyblocker.utils.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class CommandUtils {
	public static final Command<FabricClientCommandSource> noOp = _ -> -1;

	public static boolean failOnMissingProfile(CommandContext<FabricClientCommandSource> context) {
		if (Utils.getProfile() == null || Utils.getProfile().isEmpty()) {
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.command.profileMissing").withStyle(ChatFormatting.RED)));
			return true;
		}
		return false;
	}
}
