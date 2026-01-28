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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;

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
				(entity instanceof Blaze || entity instanceof WitherSkeleton || entity instanceof ZombifiedPiglin)) {
			int color = AttunementColors.getColor((LivingEntity) entity);
			if (color != MobGlow.NO_GLOW) return color;
		}

		// Slayer Boss/Miniboss
		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.GLOW)) {
			return SkyblockerConfigManager.get().slayers.highlightColor.getRGB();
		}

		// Nukebuki Skulls
		if (SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads &&
				SlayerManager.isFightingSlayerType(SlayerType.VOIDGLOOM) &&
				entity instanceof ArmorStand as && as.isMarker() && isNukekubiHead(as)) return NUKEKUBI_COLOUR;

		return MobGlow.NO_GLOW;
	}

	@Override
	public boolean isEnabled() {
		return SlayerManager.isInSlayerQuest() || SlayerManager.isFightingSlayer();
	}

	/**
	 * Compares the armor items of an armor stand to the Nukekubi head texture to determine if it is a Nukekubi head.
	 */
	private static boolean isNukekubiHead(ArmorStand entity) {
		return entity.hasItemInSlot(EquipmentSlot.HEAD) && ItemUtils.getHeadTexture(entity.getItemBySlot(EquipmentSlot.HEAD)).equals(HeadTextures.NUKEKUBI);
	}
}
