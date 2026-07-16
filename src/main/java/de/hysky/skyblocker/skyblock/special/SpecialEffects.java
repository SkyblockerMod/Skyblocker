package de.hysky.skyblocker.skyblock.special;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.lang.ref.WeakReference;

public class SpecialEffects {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	public static WeakReference<ItemStack> ITEM = new WeakReference<>(ItemStack.EMPTY);

	protected static void displaySpecialEffect(ItemStack stack, SimpleParticleType particle) {
		ITEM = new WeakReference<>(SkyblockerConfigManager.get().general.specialEffects.displayItemName ? stack : ItemStack.EMPTY);
		CLIENT.gameRenderer.displayItemActivation(stack);
		if (CLIENT.player != null) {
			CLIENT.particleEngine.createTrackingEmitter(CLIENT.player, particle, 30);
			CLIENT.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1, 1f);
		}
	}
}
