package de.hysky.skyblocker.skyblock.entity.glow.adder;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.skyblock.slayers.boss.demonlord.AttunementColors;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.GLOW)) {
			return switch (entity) {
				case ArmorStand e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				case Blaze e when SlayerManager.isInSlayerType(SlayerType.DEMONLORD) -> AttunementColors.getColor(e);
				default -> DungeonGlowAdder.STARRED_COLOUR;
			};
		}

		return switch (entity) {
			//Nukekubi Fixation Skulls
			case ArmorStand as when SkyblockerConfigManager.get().slayers.endermanSlayer.highlightNukekubiHeads && Utils.isInTheEnd() && as.isMarker() && isNukekubiHead(as) -> NUKEKUBI_COLOUR;
			//Blaze Slayer's Demonic Minions
			case WitherSkeleton e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(Minecraft.getInstance().player) <= 15 -> AttunementColors.getColor(e);
			case ZombifiedPiglin e when SkyblockerConfigManager.get().slayers.highlightBosses == SlayersConfig.HighlightSlayerEntities.GLOW && SlayerManager.isInSlayerType(SlayerType.DEMONLORD) && e.distanceTo(Minecraft.getInstance().player) <= 15 -> AttunementColors.getColor(e);
			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return SlayerManager.isInSlayer();
	}

	/**
	 * Compares the armor items of an armor stand to the Nukekubi head texture to determine if it is a Nukekubi head.
	 */
	private static boolean isNukekubiHead(ArmorStand entity) {
		return entity.hasItemInSlot(EquipmentSlot.HEAD) && ItemUtils.getHeadTexture(entity.getItemBySlot(EquipmentSlot.HEAD)).equals(HeadTextures.NUKEKUBI);
	}
}
