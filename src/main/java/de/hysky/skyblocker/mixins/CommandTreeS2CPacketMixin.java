package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import de.hysky.skyblocker.skyblock.CallAutocomplete;
import de.hysky.skyblocker.skyblock.JoinInstanceAutocomplete;
import de.hysky.skyblocker.skyblock.RngMeterAutocomplete;
import de.hysky.skyblocker.skyblock.SackItemAutocomplete;
import de.hysky.skyblocker.skyblock.ViewstashAutocomplete;
import de.hysky.skyblocker.skyblock.WarpAutocomplete;
import de.hysky.skyblocker.skyblock.speedpreset.SpeedPresets;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.commands.SharedSuggestionProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$NodeResolver")
public class CommandTreeS2CPacketMixin {
	@ModifyExpressionValue(method = "resolve", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket$NodeResolver;resolve(I)Lcom/mojang/brigadier/tree/CommandNode;", ordinal = 1))
	public CommandNode<? extends SharedSuggestionProvider> modifyCommandSuggestions(CommandNode<SharedSuggestionProvider> original) {
		if (Utils.isOnHypixel() && original instanceof LiteralCommandNode<?> literalCommandNode) {
			return switch (literalCommandNode.getLiteral()) {
				case String s when s.equals("setmaxspeed") -> SpeedPresets.getCommandNode();
				case String s when s.equals("warp") && WarpAutocomplete.commandNode != null -> WarpAutocomplete.commandNode;
				case String s when s.equals("getfromsacks") && SackItemAutocomplete.longCommandNode != null -> SackItemAutocomplete.longCommandNode;
				case String s when s.equals("gfs") && SackItemAutocomplete.shortCommandNode != null -> SackItemAutocomplete.shortCommandNode;
				case String s when s.equals("viewstash") -> ViewstashAutocomplete.getCommandNode();
				case String s when s.equals("joininstance") && JoinInstanceAutocomplete.joinInstanceCommand != null -> JoinInstanceAutocomplete.joinInstanceCommand;
				case String s when s.equals("joindungeon") && JoinInstanceAutocomplete.dungeonCommand != null -> JoinInstanceAutocomplete.dungeonCommand;
				case String s when s.equals("joinkuudra") && JoinInstanceAutocomplete.kuudraCommand != null -> JoinInstanceAutocomplete.kuudraCommand;
				case String s when s.equals("rngmeter") && RngMeterAutocomplete.longCommand != null -> RngMeterAutocomplete.longCommand;
				case String s when s.equals("rng") && RngMeterAutocomplete.shortCommand != null -> RngMeterAutocomplete.shortCommand;
				case String s when s.equals("call") && CallAutocomplete.commandNode != null -> CallAutocomplete.commandNode;
				default -> original;
			};
		}

		return original;
	}
}
