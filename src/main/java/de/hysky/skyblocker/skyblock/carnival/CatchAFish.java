package de.hysky.skyblocker.skyblock.carnival;

import java.awt.Color;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CatchAFish {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Box AREA = Box.enclosing(new BlockPos(-69, 65, -5), new BlockPos(-87, 84, 22));
	private static final String YELLOW_FISH_TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTcyMDA1MjU4MjAwMSwKICAicHJvZmlsZUlkIiA6ICIzM2Y4ZGExMTU1MzQ0YWQ5OWQ0Y2Q2ZjNhYjFjMjNhYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCXzFfUl9CIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM2MGY0Zjk5Yzc4YWRkZGVhYjI3NmViZGY2YWI5YTBmY2ZmYWJkNDlmMGI1NDNlMTk4MWFjN2JkM2Q1NWIxOGEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
	private static final int YELLOW = Color.YELLOW.getRGB();

	public static int getFishGlowColor(ArmorStandEntity armorStand) {
		ItemStack stack = armorStand.getEquippedStack(EquipmentSlot.HEAD);

		if (!stack.isEmpty() && stack.isOf(Items.PLAYER_HEAD) && ItemUtils.getHeadTexture(stack).equals(YELLOW_FISH_TEXTURE)) {
			return YELLOW;
		}

		return MobGlow.NO_GLOW;
	}

	public static boolean isInCatchAFish() {
		if (ChivalrousCarnival.isInCarnival() && SkyblockerConfigManager.get().helpers.carnival.catchAFishHelper && CLIENT.player != null) {
			BlockPos pos = CLIENT.player.getBlockPos();

			return AREA.contains(pos.getX(), pos.getY(), pos.getZ());
		}

		return false;
	}
}
