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
	private static final Set<Block> CONVERT_INTO_BEDROCK_BLOCKS = Set.of(
			Blocks.STONE,
			Blocks.COBBLESTONE,
			Blocks.POLISHED_DIORITE,
			Blocks.PRISMARINE,
			Blocks.PRISMARINE_BRICKS,
			Blocks.DARK_PRISMARINE,
			Blocks.CYAN_TERRACOTTA,
			Blocks.LIGHT_BLUE_WOOL,
			Blocks.GRAY_WOOL,
			Blocks.LAPIS_BLOCK,
			Blocks.GOLD_BLOCK,
			Blocks.IRON_BLOCK,
			Blocks.DIAMOND_BLOCK,
			Blocks.EMERALD_BLOCK,
			Blocks.REDSTONE_BLOCK,
			Blocks.COAL_BLOCK,
			Blocks.GOLD_ORE,
			Blocks.IRON_ORE,
			Blocks.COAL_ORE,
			Blocks.LAPIS_ORE,
			Blocks.REDSTONE_ORE,
			Blocks.DIAMOND_ORE,
			Blocks.EMERALD_ORE,
			Blocks.NETHER_QUARTZ_ORE,
			Blocks.NETHERRACK,
			Blocks.GLOWSTONE,
			Blocks.OBSIDIAN,
			Blocks.END_STONE
	);
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
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static boolean shouldRender;
	@Nullable
	private static Text errorMessage;
	private static final BlockState[][][] blocks = new BlockState[8][8][8];
	private static final Set<BlockPos> breakBlocks = new HashSet<>();
	private static final int[] drops = new int[MiningDrop.values().length];

	public static boolean shouldRender() {
		return shouldRender;
	}

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

		shouldRender = true;
		errorMessage = null;
		breakBlocks.clear();
		Arrays.fill(drops, 0);

		if (CLIENT.player == null || CLIENT.world == null) {
			errorMessage = Text.literal("Can't find player or world?").formatted(Formatting.RED);
			return;
		}

		if (ItemUtils.getLoreLineContains(CLIENT.player.getMainHandStack(), "Ability: Pickobulus") == null) {
			shouldRender = false;
			errorMessage = Text.literal("Not holding a tool with pickobulus").formatted(Formatting.RED);
			return;
		}

		Vec3d start = CLIENT.player.getEntityPos().add(0, Utils.getEyeHeight(CLIENT.player) + 0.53625, 0); // Magic number according to https://youtu.be/5hdDrr6jk4E
		BlockHitResult blockHitResult = CLIENT.world.raycast(new RaycastContext(start, start.add(CLIENT.player.getRotationVecClient().multiply(20)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, CLIENT.player));
		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			errorMessage = Text.literal("Not looking at a block").formatted(Formatting.RED);
			return;
		}

		calculatePickobulus(blockHitResult.getBlockPos());
	}

	private static void calculatePickobulus(BlockPos pos) {
		assert CLIENT.world != null;
		BlockPos.Mutable posMutable = pos.mutableCopy().move(-4, -4, -4);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 8; k++) {
					blocks[i][j][k] = CLIENT.world.getBlockState(posMutable);
					posMutable.move(Direction.SOUTH);
				}
				posMutable.move(0, 1, -8);
			}
			posMutable.move(1, -8, 0);
		}

		for (int i = 1; i < 7; i++) {
			for (int j = 1; j < 7; j++) {
				for (int k = 1; k < 7; k++) {
					BlockState state = blocks[i][j][k];
					if (state.isAir() || state.isOf(Blocks.BEDROCK)) continue;
					boolean exposed = blocks[i - 1][j][k].isAir() || blocks[i + 1][j][k].isAir()
									|| blocks[i][j - 1][k].isAir() || blocks[i][j + 1][k].isAir()
									|| blocks[i][j][k - 1].isAir() || blocks[i][j][k + 1].isAir();
					if (!exposed) continue;

					if (Utils.getArea().equals(Area.GLACITE_TUNNELS)) handleGlaciteTunnels(pos, state, i, j, k);
					else switch (Utils.getLocation()) {
						case PRIVATE_ISLAND -> handleBreakable(pos, i, j, k);
						case GOLD_MINE, DEEP_CAVERNS, DWARVEN_MINES -> handleConvertIntoBedrock(pos, state, i, j, k);
						case CRYSTAL_HOLLOWS -> handleCrystalHollows(pos, state, i, j, k);
						case GLACITE_MINESHAFTS -> handleGlaciteMineshafts(pos, state, i, j, k);
					}
				}
			}
		}
	}

	private static void handleBreakable(BlockPos pos, int i, int j, int k) {
		blocks[i][j][k] = Blocks.AIR.getDefaultState();
		breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
	}

	private static void handleConvertIntoBedrock(BlockPos pos, BlockState state, int i, int j, int k) {
		if (CONVERT_INTO_BEDROCK_BLOCKS.contains(state.getBlock()) || STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
		}
	}

	private static void handleCrystalHollows(BlockPos pos, BlockState state, int i, int j, int k) {
		if (STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			drops[MiningDrop.GEMSTONES.ordinal()]++;
		} else if (state.isOf(Blocks.PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (state.isOf(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.isOf(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		}
		handleBreakable(pos, i, j, k);
	}

	private static void handleGlaciteTunnels(BlockPos pos, BlockState state, int i, int j, int k) {
		if (state.isOf(Blocks.PACKED_ICE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.ICE.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.getDefaultState();
		} else if (STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.GEMSTONES.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.getDefaultState();
		} else if (state.isOf(Blocks.POLISHED_DIORITE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 4;
			drops[MiningDrop.TITANIUM.ordinal()]++;
		} else if (state.isOf(Blocks.INFESTED_STONE)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (state.isOf(Blocks.LIGHT_GRAY_CARPET)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (state.isOf(Blocks.PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.isOf(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (state.isOf(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.isOf(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.isOf(Blocks.BROWN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.isOf(Blocks.TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.isOf(Blocks.SMOOTH_RED_SANDSTONE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.isOf(Blocks.INFESTED_COBBLESTONE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else if (state.isOf(Blocks.CLAY)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else {
			return;
		}
		breakBlocks.add(pos.add(i - 4, j - 4, k - 4));
	}

	private static void handleGlaciteMineshafts(BlockPos pos, BlockState state, int i, int j, int k) {
		if (state.isOf(Blocks.STONE)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		}
		handleBreakable(pos, i, j, k);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().mining.enablePickobulusHelper) return;
		for (BlockPos breakPos : breakBlocks) {
			collector.submitOutlinedBox(breakPos, LIGHT_BLUE, 2f, false);
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
