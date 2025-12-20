package de.hysky.skyblocker.skyblock.carnival;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.ItemUtils;

public class CatchAFish {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final AABB AREA = AABB.encapsulatingFullBlocks(new BlockPos(-69, 65, -5), new BlockPos(-87, 84, 22));
	private static final int YELLOW = Color.YELLOW.getRGB();

	public static int getFishGlowColor(ArmorStand armorStand) {
		ItemStack stack = armorStand.getItemBySlot(EquipmentSlot.HEAD);

		if (!stack.isEmpty() && stack.is(Items.PLAYER_HEAD) && ItemUtils.getHeadTexture(stack).equals(HeadTextures.CARNIVAL_YELLOW_FISH)) {
			return YELLOW;
		}

		return MobGlow.NO_GLOW;
	}

	public static boolean isInCatchAFish() {
		if (ChivalrousCarnival.isInCarnival() && SkyblockerConfigManager.get().helpers.carnival.catchAFishHelper && CLIENT.player != null) {
			BlockPos pos = CLIENT.player.blockPosition();

			return AREA.contains(pos.getX(), pos.getY(), pos.getZ());
		}

		return false;
	}
}
