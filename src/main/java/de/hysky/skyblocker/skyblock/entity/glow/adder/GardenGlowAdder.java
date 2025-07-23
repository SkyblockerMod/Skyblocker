package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;

public class GardenGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final GardenGlowAdder INSTANCE = new GardenGlowAdder();
	private static final int PEST_COLOUR = 0xb62f00;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		return switch (entity) {
			case ArmorStandEntity as when SkyblockerConfigManager.get().farming.garden.pestHighlighter && isPestHead(as) -> PEST_COLOUR;
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInGarden();
	}

	/**
	 * Compares the armor items of an armor stand to the Pest head texture to determine if it is a Pest head.
	 */
	private static boolean isPestHead(ArmorStandEntity entity) {
		return entity.hasStackEquipped(EquipmentSlot.HEAD) && HeadTextures.PEST_HEADS.contains(ItemUtils.getHeadTexture(entity.getEquippedStack(EquipmentSlot.HEAD)));
	}
}
