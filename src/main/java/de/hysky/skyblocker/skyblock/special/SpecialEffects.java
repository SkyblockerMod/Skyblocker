package de.hysky.skyblocker.skyblock.special;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SpecialEffects {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	public static final Set<ItemStack> ITEMS = Collections.newSetFromMap(new WeakHashMap<>());

	protected static void displaySpecialEffect(ItemStack stack, SimpleParticleType particle) {
		ITEMS.add(stack);
		CLIENT.gameRenderer.displayItemActivation(stack);
		if (CLIENT.player != null) {
			CLIENT.particleEngine.createTrackingEmitter(CLIENT.player, particle, 30);
			CLIENT.player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1, 1f);
		}
	}
}
