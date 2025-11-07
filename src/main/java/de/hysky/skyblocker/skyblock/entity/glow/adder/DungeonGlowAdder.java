package de.hysky.skyblocker.skyblock.entity.glow.adder;

import java.util.List;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonPlayerManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.entity.MobGlowAdder;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;

public class DungeonGlowAdder extends MobGlowAdder {
	public static final DungeonGlowAdder INSTANCE = new DungeonGlowAdder();
	protected static final int STARRED_COLOUR = 0xf57738;
	private static final int LOST_ADVENTURER_COLOUR = 0xfee15c;
	private static final int SHADOW_ASSASSIN_COLOUR = 0x5b2cb2;
	private static final int ANGRY_ARCHAEOLOGIST_COLOUR = 0x57c2f7;
	private static final int ENDERMAN_EYE_COLOUR = 0xcc00fa;

	@Init
	public static void init() {}

	@Override
	public int computeColour(Entity entity) {
		String name = entity.getName().getString();

		return switch (entity) {
			// Minibosses
			case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Lost Adventurer") -> LOST_ADVENTURER_COLOUR;
			case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Shadow Assassin") -> SHADOW_ASSASSIN_COLOUR;
			case PlayerEntity p when SkyblockerConfigManager.get().dungeons.starredMobGlow && !DungeonManager.getBoss().isFloor(4) && name.equals("Diamond Guy") -> ANGRY_ARCHAEOLOGIST_COLOUR;
			case PlayerEntity p when entity.getId() == LividColor.getCorrectLividId() && LividColor.shouldGlow(name) -> LividColor.getGlowColor(name);

			// Bats
			case BatEntity b when SkyblockerConfigManager.get().dungeons.starredMobGlow -> STARRED_COLOUR;

			// Fel Heads
			case ArmorStandEntity as when SkyblockerConfigManager.get().dungeons.starredMobGlow && as.isMarker() && as.hasStackEquipped(EquipmentSlot.HEAD) && ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD)).equals(HeadTextures.FEL) -> ENDERMAN_EYE_COLOUR;

			// Wither & Blood Keys
			case ArmorStandEntity as when SkyblockerConfigManager.get().dungeons.highlightDoorKeys && as.hasStackEquipped(EquipmentSlot.HEAD) -> {
				yield switch (ItemUtils.getHeadTexture(as.getEquippedStack(EquipmentSlot.HEAD))) {
					case String s when s.equals(HeadTextures.WITHER_KEY) -> DyeColor.CYAN.getSignColor();
					case String s when s.equals(HeadTextures.BLOOD_KEY) -> DyeColor.CYAN.getSignColor();
					default -> NO_GLOW;
				};
			}

			// Armor Stands
			case ArmorStandEntity as -> 0;

			// Regular Mobs
			case Entity e when SkyblockerConfigManager.get().dungeons.starredMobGlow && isStarred(entity) -> STARRED_COLOUR;

			//Class-based glow
			//This goes after regular mobs to ensure starred player entities like dreadlords have the glow applied
			case PlayerEntity p when SkyblockerConfigManager.get().dungeons.classBasedPlayerGlow && DungeonScore.isDungeonStarted() -> DungeonPlayerManager.getClassFromPlayer(p).glowColor();

			default -> NO_GLOW;
		};
	}

	@Override
	public boolean isEnabled() {
		return Utils.isInDungeons();
	}

	/**
	 * Checks if an entity is starred by checking if its armor stand contains a star in its name.
	 *
	 * @param entity the entity to check.
	 * @return true if the entity is starred, false otherwise
	 */
	public static boolean isStarred(Entity entity) {
		List<ArmorStandEntity> armorStands = MobGlow.getArmorStands(entity);
		return !armorStands.isEmpty() && armorStands.getFirst().getName().getString().contains("✯");
	}
}
