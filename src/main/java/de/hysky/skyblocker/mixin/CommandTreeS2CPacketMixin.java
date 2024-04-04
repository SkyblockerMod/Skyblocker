package de.hysky.skyblocker.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.hysky.skyblocker.skyblock.WarpAutocomplete;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket$CommandTree")
public class CommandTreeS2CPacketMixin {
    @ModifyExpressionValue(method = "getNode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket$CommandTree;getNode(I)Lcom/mojang/brigadier/tree/CommandNode;", ordinal = 1))
    public CommandNode<? extends CommandSource> modifyCommandSuggestions(CommandNode<CommandSource> original) {
        if (Utils.isOnHypixel() && WarpAutocomplete.commandNode != null && original instanceof LiteralCommandNode<?> literalCommandNode && literalCommandNode.getLiteral().equals("warp")) {
            return WarpAutocomplete.commandNode;
        }
        return original;
    }
}
