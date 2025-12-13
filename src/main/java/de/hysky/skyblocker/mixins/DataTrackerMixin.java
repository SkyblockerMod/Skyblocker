package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.EndermanEntityAccessor;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.Utils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SynchedEntityData.class)
public abstract class DataTrackerMixin {
	@Shadow
	@Final
	private SyncedDataHolder entity;

	@SuppressWarnings("ConstantValue")
	@Inject(method = "assignValues", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValue(Lnet/minecraft/network/syncher/SynchedEntityData$DataItem;Lnet/minecraft/network/syncher/SynchedEntityData$DataValue;)V"))
	private <T> void skyblocker$onWriteUpdatedEntries(CallbackInfo ci, @Local SynchedEntityData.DataItem<T> entry, @Local SynchedEntityData.DataValue<T> serializedEntry) {
		if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayers.endermanSlayer.enableYangGlyphsNotification && entry.getAccessor() == EndermanEntityAccessor.getDATA_CARRY_STATE() && entry.getValue() instanceof Optional<?> value && value.isPresent() && value.get() instanceof BlockState state && state.is(Blocks.BEACON) && ((Optional<?>) serializedEntry.value()).isEmpty()) {
			Minecraft client = Minecraft.getInstance();
			if (entity instanceof Entity entity && MobGlow.getArmorStands(entity).stream().anyMatch(armorStand -> armorStand.getName().getString().contains(client.getUser().getName()))) {
				client.gui.setTimes(5, 20, 10);
				client.gui.setTitle(Component.literal("Yang Glyph!").withStyle(ChatFormatting.RED));
				client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 100f, 0.1f);
			}
		}
	}

	@Inject(method = "assignValue", at = @At(value = "NEW", target = "Ljava/lang/IllegalStateException;"), cancellable = true)
	public void skyblocker$ignoreInvalidDataExceptions(CallbackInfo ci) {
		//These exceptions cause annoying small lag spikes for some reason
		if (Utils.isOnHypixel()) ci.cancel();
	}
}
