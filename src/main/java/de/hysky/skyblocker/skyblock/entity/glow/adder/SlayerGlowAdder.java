package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.AttunementColors;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;

@SuppressWarnings("unused")
public class SlayerGlowAdder extends MobGlowAdder {
	@SuppressWarnings("unused")
	private static final SlayerGlowAdder INSTANCE = new SlayerGlowAdder();
	private static final int NUKEKUBI_COLOUR = 0x990099;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		// Blaze Slayer
		if (SkyblockerConfigManager.get().slayers.blazeSlayer.attunementHighlights &&
				SlayerManager.isFightingSlayerType(SlayerType.DEMONLORD) &&
				(SlayerManager.isSelectedBoss(entity.getUuid()) || entity instanceof WitherSkeletonEntity || entity instanceof ZombifiedPiglinEntity)) {
			return AttunementColors.getColor((LivingEntity) entity);
		}

		// Slayer Boss/Miniboss
		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.GLOW)) {
			return SkyblockerConfigManager.get().slayers.highlightColor.getRGB();
		}

		// Nukebuki Skulls
		if (SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads &&
				SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM) &&
				entity instanceof ArmorStandEntity as &&
				as.isMarker() && isNukekubiHead(as)) return NUKEKUBI_COLOUR;

		return MobGlow.NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return SlayerManager.isInSlayer() || SlayerManager.isFightingSlayer();
	}

	/**
	 * Compares the armor items of an armor stand to the Nukekubi head texture to determine if it is a Nukekubi head.
	 */
	private static boolean isNukekubiHead(ArmorStandEntity entity) {
		return entity.hasStackEquipped(EquipmentSlot.HEAD) && ItemUtils.getHeadTexture(entity.getEquippedStack(EquipmentSlot.HEAD)).equals(HeadTextures.NUKEKUBI);
	}
}
