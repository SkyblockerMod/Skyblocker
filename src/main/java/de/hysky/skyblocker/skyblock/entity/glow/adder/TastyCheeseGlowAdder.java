package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.PetCache;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jspecify.annotations.Nullable;


public final class TastyCheeseGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final TastyCheeseGlowAdder INSTANCE = new TastyCheeseGlowAdder();

	private TastyCheeseGlowAdder() {}

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		if (!(entity instanceof ItemEntity itemEntity)) return NO_GLOW;
		// Is tasty cheese
		if (!ItemUtils.getHeadTexture(itemEntity.getItem()).equals(HeadTextures.TASTY_CHEESE)) return NO_GLOW;
		// Player has a rat pet active
		@Nullable PetInfo pet = PetCache.getCurrentPet();
		if (pet == null || !pet.type().equals("RAT")) return NO_GLOW;

		return 0xFFD700; // Gold
	}

	@Override
	public boolean isEnabled() {
		return Utils.isOnSkyblock() &&
				SkyblockerConfigManager.get().helpers.enableTastyCheeseHighlight;
	}
}
