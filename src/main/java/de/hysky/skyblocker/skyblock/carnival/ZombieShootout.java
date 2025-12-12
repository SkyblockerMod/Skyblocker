package de.hysky.skyblocker.skyblock.carnival;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.crimson.dojo.DisciplineTestHelper;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class ZombieShootout {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final float[] RED = { 1, 0, 0 };
	private static final Box SHOOTING_BOX = Box.enclosing(new BlockPos(-100, 70, 15), new BlockPos(-102, 75, 13));
	private static final BlockPos[] LAMPS = {
			new BlockPos(-96, 76, 31),
			new BlockPos(-99, 77, 32),
			new BlockPos(-102, 75, 32),
			new BlockPos(-106, 77, 31),
			new BlockPos(-109, 75, 30),
			new BlockPos(-112, 76, 28),
			new BlockPos(-115, 77, 25),
			new BlockPos(-117, 76, 22),
			new BlockPos(-118, 76, 19),
			new BlockPos(-119, 75, 15),
			new BlockPos(-119, 77, 12),
			new BlockPos(-118, 76, 9)
			};

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(ZombieShootout::extractRendering);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (isInZombieShootout() && CLIENT.world != null) {
			for (BlockPos pos : LAMPS) {
				BlockState state = CLIENT.world.getBlockState(pos);
				Block block = state.getBlock();

				if (block.equals(Blocks.REDSTONE_LAMP) && state.contains(Properties.LIT) && state.get(Properties.LIT)) {
					collector.submitOutlinedBox(pos, RED, 5f, false);
				}
			}
		}
	}

	public static int getZombieGlowColor(ZombieEntity zombie) {
		if (!zombie.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
			Item item = zombie.getEquippedStack(EquipmentSlot.CHEST).getItem();

			return switch (item) {
				case Item i when i == Items.DIAMOND_CHESTPLATE -> DisciplineTestHelper.SWORD_TO_COLOR_LOOKUP.get("DIAMOND_SWORD");
				case Item i when i == Items.GOLDEN_CHESTPLATE -> DisciplineTestHelper.SWORD_TO_COLOR_LOOKUP.get("GOLD_SWORD");
				case Item i when i == Items.IRON_CHESTPLATE -> DisciplineTestHelper.SWORD_TO_COLOR_LOOKUP.get("IRON_SWORD");
				case Item i when i == Items.LEATHER_CHESTPLATE -> DisciplineTestHelper.SWORD_TO_COLOR_LOOKUP.get("WOOD_SWORD");

				default -> MobGlow.NO_GLOW;
			};
		}

		return MobGlow.NO_GLOW;
	}

	public static boolean isInZombieShootout() {
		if (ChivalrousCarnival.isInCarnival() && SkyblockerConfigManager.get().helpers.carnival.zombieShootoutHelper && CLIENT.player != null) {
			BlockPos pos = CLIENT.player.getBlockPos();

			return SHOOTING_BOX.contains(pos.getX(), pos.getY(), pos.getZ());
		}

		return false;
	}
}
