package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PickobulusHelper {
	private static final Set<Block> STAINED_GLASS_BLOCKS = Set.of(
			Blocks.WHITE_STAINED_GLASS,
			Blocks.ORANGE_STAINED_GLASS,
			Blocks.MAGENTA_STAINED_GLASS,
			Blocks.LIGHT_BLUE_STAINED_GLASS,
			Blocks.YELLOW_STAINED_GLASS,
			Blocks.LIME_STAINED_GLASS,
			Blocks.PINK_STAINED_GLASS,
			Blocks.GRAY_STAINED_GLASS,
			Blocks.LIGHT_GRAY_STAINED_GLASS,
			Blocks.CYAN_STAINED_GLASS,
			Blocks.PURPLE_STAINED_GLASS,
			Blocks.BLUE_STAINED_GLASS,
			Blocks.BROWN_STAINED_GLASS,
			Blocks.GREEN_STAINED_GLASS,
			Blocks.RED_STAINED_GLASS,
			Blocks.BLACK_STAINED_GLASS,
			Blocks.WHITE_STAINED_GLASS_PANE,
			Blocks.ORANGE_STAINED_GLASS_PANE,
			Blocks.MAGENTA_STAINED_GLASS_PANE,
			Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
			Blocks.YELLOW_STAINED_GLASS_PANE,
			Blocks.LIME_STAINED_GLASS_PANE,
			Blocks.PINK_STAINED_GLASS_PANE,
			Blocks.GRAY_STAINED_GLASS_PANE,
			Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
			Blocks.CYAN_STAINED_GLASS_PANE,
			Blocks.PURPLE_STAINED_GLASS_PANE,
			Blocks.BLUE_STAINED_GLASS_PANE,
			Blocks.BROWN_STAINED_GLASS_PANE,
			Blocks.GREEN_STAINED_GLASS_PANE,
			Blocks.RED_STAINED_GLASS_PANE,
			Blocks.BLACK_STAINED_GLASS_PANE
	);
	private static final float[] LIGHT_BLUE = ColorUtils.getFloatComponents(DyeColor.LIGHT_BLUE);
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Nullable
	private static Text errorMessage;
	private static final BlockState[][][] blocks = new BlockState[8][8][8];
	private static final Set<BlockPos> breakBlocks = new HashSet<>();
	private static final int[] drops = new int[MiningDrop.values().length];

	@Nullable
	public static Text getErrorMessage() {
		return errorMessage;
	}

	public static int getTotalBlocks() {
		return breakBlocks.size();
	}

	public static int[] getDrops() {
		return drops;
	}

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(PickobulusHelper::update, 1);
		WorldRenderExtractionCallback.EVENT.register(PickobulusHelper::extractRendering);
	}

	private static void update() {
		if (!SkyblockerConfigManager.get().mining.enablePickobulusHelper) return;

		errorMessage = null;
		breakBlocks.clear();
		Arrays.fill(drops, 0);

		if (client.player == null || client.world == null) {
			errorMessage = Text.literal("Can't find player or world?").formatted(Formatting.RED);
			return;
		}

		if (ItemUtils.getLoreLineIf(client.player.getMainHandStack(), s -> s.contains("Ability: Pickobulus")) == null) {
			errorMessage = Text.literal("Not holding a tool with pickobulus").formatted(Formatting.RED);
			return;
		}

		Vec3d start = client.player.getEyePos().add(0, 0.53625, 0); // Magic number according to https://youtu.be/5hdDrr6jk4E
		BlockHitResult blockHitResult = client.world.raycast(new RaycastContext(start, start.add(client.player.getRotationVecClient().multiply(20)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));
		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			errorMessage = Text.literal("Not looking at a block").formatted(Formatting.RED);
			return;
		}

		calculatePickobulus(blockHitResult.getBlockPos());
	}

	private static void calculatePickobulus(BlockPos pos) {
		BlockPos.Mutable posMutable = pos.mutableCopy().move(-4, -4, -4);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 8; k++) {
					blocks[i][j][k] = client.world.getBlockState(posMutable);
					posMutable.move(Direction.SOUTH);
				}
				posMutable.move(0, 1, -8);
			}
			posMutable.move(1, -8, 0);
		}

		for (int i = 1; i < 7; i++) {
			for (int j = 1; j < 7; j++) {
				for (int k = 1; k < 7; k++) {
					if (blocks[i][j][k].isAir() || blocks[i][j][k].isOf(Blocks.BEDROCK)) continue;
					boolean exposed = blocks[i - 1][j][k].isAir()
									  || blocks[i + 1][j][k].isAir()
									  || blocks[i][j - 1][k].isAir()
									  || blocks[i][j + 1][k].isAir()
									  || blocks[i][j][k - 1].isAir()
									  || blocks[i][j][k + 1].isAir();
					if (!exposed) continue;

					if (Utils.getArea().equals(Area.GLACITE_TUNNELS)) handleGlaciteTunnels(pos, i, j, k);
					else switch (Utils.getLocation()) {
						case PRIVATE_ISLAND -> handleBreakable(pos, i, j, k);
						case GOLD_MINE, DEEP_CAVERNS, DWARVEN_MINES -> handleConvertIntoBedrock(pos, i, j, k);
						case CRYSTAL_HOLLOWS -> handleCrystalHollows(pos, i, j, k);
						case GLACITE_MINESHAFTS -> handleGlaciteMineshafts(pos, i, j, k);
					}
				}
			}
		}
	}

	private static void handleBreakable(BlockPos pos, int i, int j, int k) {
		blocks[i][j][k] = Blocks.AIR.getDefaultState();
		breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
	}

	private static void handleConvertIntoBedrock(BlockPos pos, int i, int j, int k) {
		if (blocks[i][j][k].isOf(Blocks.STONE)
			|| blocks[i][j][k].isOf(Blocks.COBBLESTONE)
			|| blocks[i][j][k].isOf(Blocks.POLISHED_DIORITE)
			|| blocks[i][j][k].isOf(Blocks.PRISMARINE)
			|| blocks[i][j][k].isOf(Blocks.PRISMARINE_BRICKS)
			|| blocks[i][j][k].isOf(Blocks.DARK_PRISMARINE)
			|| blocks[i][j][k].isOf(Blocks.CYAN_TERRACOTTA)
			|| blocks[i][j][k].isOf(Blocks.LIGHT_BLUE_WOOL)
			|| blocks[i][j][k].isOf(Blocks.GRAY_WOOL)
			|| blocks[i][j][k].isOf(Blocks.LAPIS_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.GOLD_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.IRON_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.DIAMOND_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.EMERALD_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.REDSTONE_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.COAL_BLOCK)
			|| blocks[i][j][k].isOf(Blocks.GOLD_ORE)
			|| blocks[i][j][k].isOf(Blocks.IRON_ORE)
			|| blocks[i][j][k].isOf(Blocks.COAL_ORE)
			|| blocks[i][j][k].isOf(Blocks.LAPIS_ORE)
			|| blocks[i][j][k].isOf(Blocks.REDSTONE_ORE)
			|| blocks[i][j][k].isOf(Blocks.DIAMOND_ORE)
			|| blocks[i][j][k].isOf(Blocks.EMERALD_ORE)
			|| blocks[i][j][k].isOf(Blocks.NETHER_QUARTZ_ORE)
			|| blocks[i][j][k].isOf(Blocks.NETHERRACK)
			|| blocks[i][j][k].isOf(Blocks.GLOWSTONE)
			|| blocks[i][j][k].isOf(Blocks.OBSIDIAN)
			|| blocks[i][j][k].isOf(Blocks.END_STONE)
			|| STAINED_GLASS_BLOCKS.contains(blocks[i][j][k].getBlock())) {
			breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
		}
	}

	private static void handleCrystalHollows(BlockPos pos, int i, int j, int k) {
		if (STAINED_GLASS_BLOCKS.contains(blocks[i][j][k].getBlock())) {
			drops[MiningDrop.GEMSTONES.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (blocks[i][j][k].isOf(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		}
		handleBreakable(pos, i, j, k);
	}

	private static void handleGlaciteTunnels(BlockPos pos, int i, int j, int k) {
		if (blocks[i][j][k].isOf(Blocks.PACKED_ICE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.ICE.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.getDefaultState();
		} else if (STAINED_GLASS_BLOCKS.contains(blocks[i][j][k].getBlock())) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.GEMSTONES.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.getDefaultState();
		} else if (blocks[i][j][k].isOf(Blocks.POLISHED_DIORITE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 4;
			drops[MiningDrop.TITANIUM.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.INFESTED_STONE)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.LIGHT_GRAY_CARPET)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (blocks[i][j][k].isOf(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (blocks[i][j][k].isOf(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.BROWN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.RED_SANDSTONE_SLAB)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.INFESTED_COBBLESTONE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else if (blocks[i][j][k].isOf(Blocks.CLAY)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else {
			return;
		}
		breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
	}

	private static void handleGlaciteMineshafts(BlockPos pos, int i, int j, int k) {
		if (blocks[i][j][k].isOf(Blocks.STONE)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		}
		handleBreakable(pos, i, j, k);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		for (BlockPos breakPos : breakBlocks) {
			collector.submitOutlinedBox(breakPos, LIGHT_BLUE, 2f, true);
		}
	}

	enum MiningDrop {
		MINESHAFT_PITY("Mineshaft Pity"),
		HARDSTONE("Hardstone"),
		ICE("Ice"),
		TUNGSTEN("Tungsten"),
		UMBER("Umber"),
		MITHRIL("Mithril"),
		TITANIUM("Titanium"),
		GEMSTONES("Gemstones"),
		MITHRIL_POWDER("Mithril Powder");

		private final String friendlyName;

		MiningDrop(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		public String friendlyName() {
			return friendlyName;
		}
	}
}
