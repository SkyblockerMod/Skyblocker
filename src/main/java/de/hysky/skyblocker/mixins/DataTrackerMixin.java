package de.hysky.skyblocker.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.mixins.accessors.EndermanEntityAccessor;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracked;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(DataTracker.class)
public abstract class DataTrackerMixin {
    @Shadow
    @Final
    private DataTracked trackedEntity;

    @SuppressWarnings("ConstantValue")
    @Inject(method = "writeUpdatedEntries", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;copyToFrom(Lnet/minecraft/entity/data/DataTracker$Entry;Lnet/minecraft/entity/data/DataTracker$SerializedEntry;)V"))
    private <T> void skyblocker$onWriteUpdatedEntries(CallbackInfo ci, @Local DataTracker.Entry<T> entry, @Local DataTracker.SerializedEntry<T> serializedEntry) {
        if (Utils.isInTheEnd() && SkyblockerConfigManager.get().slayer.endermanSlayer.enableYangGlyphsNotification && entry.getData() == EndermanEntityAccessor.getCARRIED_BLOCK() && entry.get() instanceof Optional<?> value && value.isPresent() && value.get() instanceof BlockState state && state.isOf(Blocks.BEACON) && ((Optional<?>) serializedEntry.value()).isEmpty()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (trackedEntity instanceof Entity entity && MobGlow.getArmorStands(entity).stream().anyMatch(armorStand -> armorStand.getName().getString().contains(client.getSession().getUsername()))) {
                client.inGameHud.setTitleTicks(5, 20, 10);
                client.inGameHud.setTitle(Text.literal("Yang Glyph!").formatted(Formatting.RED));
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
            }
        }
    }

    @Inject(method = "copyToFrom", at = @At(value = "NEW", target = "Ljava/lang/IllegalStateException;"), cancellable = true)
    public void skyblocker$ignoreInvalidDataExceptions(CallbackInfo ci) {
        //These exceptions cause annoying small lag spikes for some reason
        if (Utils.isOnHypixel()) ci.cancel();
    }
}
