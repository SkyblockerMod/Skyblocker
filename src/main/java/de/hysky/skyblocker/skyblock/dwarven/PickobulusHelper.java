package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.ItemAbility;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
			Blocks.QUARTZ_BLOCK,
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
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static boolean shouldRender;
	private static @Nullable Component errorMessage;
	private static final BlockState[][][] blocks = new BlockState[8][8][8];
	private static final Set<BlockPos> breakBlocks = new HashSet<>();
	private static final int[] drops = new int[MiningDrop.values().length];

	public static boolean shouldRender() {
		return shouldRender;
	}

	public static @Nullable Component getErrorMessage() {
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
		if (!(SkyblockerConfigManager.get().mining.enablePickobulusHelper || SkyblockerConfigManager.get().mining.pickobulusHelper.enablePickobulusHud)) return;

		shouldRender = true;
		errorMessage = null;
		breakBlocks.clear();
		Arrays.fill(drops, 0);

		if (CLIENT.player == null || CLIENT.level == null) {
			errorMessage = Component.literal("Can't find player or world?").withStyle(ChatFormatting.RED);
			return;
		}

		// Process cooldown info before checking whether the user is holding a pickaxe with pickobulus so cooldown info is always displayed
		Optional<String> pickobulusCooldownHud = PlayerListManager.getPlayerStringList().stream().map(String::trim).filter(entry -> entry.startsWith("Pickobulus: ")).findAny();
		// Only process if the pickobulus ability info is in the player list, so pickobulus helper will still render if this info is not in the player list
		if (pickobulusCooldownHud.isPresent() && !pickobulusCooldownHud.get().equals("Pickobulus: Available")) {
			shouldRender = !SkyblockerConfigManager.get().mining.pickobulusHelper.hideHudOnCooldown;
			errorMessage = Component.literal("Pickobulus is on cooldown: " + pickobulusCooldownHud.get().substring(12)).withStyle(ChatFormatting.RED);
			return;
		}

		if (!ItemAbility.hasAbility(CLIENT.player.getMainHandItem(), "Pickobulus")) {
			shouldRender = false;
			errorMessage = Component.literal("Not holding a tool with pickobulus").withStyle(ChatFormatting.RED);
			return;
		}

		Vec3 start = CLIENT.player.position().add(0, Utils.getEyeHeight(CLIENT.player) + 0.53625, 0); // Magic number according to https://youtu.be/5hdDrr6jk4E
		BlockHitResult blockHitResult = CLIENT.level.clip(new ClipContext(start, start.add(CLIENT.player.getForward().scale(20)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CLIENT.player));
		if (blockHitResult.getType() != HitResult.Type.BLOCK) {
			errorMessage = Component.literal("Not looking at a block").withStyle(ChatFormatting.RED);
			return;
		}

		calculatePickobulus(blockHitResult.getBlockPos());
	}

	@SuppressWarnings("incomplete-switch")
	private static void calculatePickobulus(BlockPos pos) {
		assert CLIENT.level != null;
		BlockPos.MutableBlockPos posMutable = pos.mutable().move(-4, -4, -4);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				for (int k = 0; k < 8; k++) {
					blocks[i][j][k] = CLIENT.level.getBlockState(posMutable);
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
					if (state.isAir() || state.is(Blocks.BEDROCK)) continue;
					boolean exposed = blocks[i - 1][j][k].isAir() || blocks[i + 1][j][k].isAir()
									|| blocks[i][j - 1][k].isAir() || blocks[i][j + 1][k].isAir()
									|| blocks[i][j][k - 1].isAir() || blocks[i][j][k + 1].isAir();
					if (!exposed) continue;

					if (Utils.getArea() == Area.DwarvenMines.GLACITE_TUNNELS) handleGlaciteTunnels(pos, state, i, j, k);
					else if (Utils.getArea() == Area.DwarvenMines.GLACITE_MINESHAFTS) handleGlaciteMineshafts(pos, state, i, j, k);
					else switch (Utils.getLocation()) {
						case PRIVATE_ISLAND -> handleBreakable(pos, i, j, k);
						case GOLD_MINE, DEEP_CAVERNS, DWARVEN_MINES -> handleConvertIntoBedrock(pos, state, i, j, k);
						case CRYSTAL_HOLLOWS -> handleCrystalHollows(pos, state, i, j, k);
						case GLACITE_MINESHAFTS -> handleGlaciteMineshafts(pos, state, i, j, k); // This doesn't seem to be actually possible according to the API?
					}
				}
			}
		}
	}

	private static void handleBreakable(BlockPos pos, int i, int j, int k) {
		blocks[i][j][k] = Blocks.AIR.defaultBlockState();
		breakBlocks.add(pos.offset(i - 4, j - 4, k - 4));
	}

	private static void handleConvertIntoBedrock(BlockPos pos, BlockState state, int i, int j, int k) {
		if (CONVERT_INTO_BEDROCK_BLOCKS.contains(state.getBlock()) || STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			breakBlocks.add(pos.offset(i - 4, j - 4, k - 4));
		}
	}

	private static void handleCrystalHollows(BlockPos pos, BlockState state, int i, int j, int k) {
		if (STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			drops[MiningDrop.GEMSTONES.ordinal()]++;
		} else if (state.is(Blocks.PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (state.is(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.is(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		}
		handleBreakable(pos, i, j, k);
	}

	private static void handleGlaciteTunnels(BlockPos pos, BlockState state, int i, int j, int k) {
		if (state.is(Blocks.PACKED_ICE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.ICE.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.defaultBlockState();
		} else if (STAINED_GLASS_BLOCKS.contains(state.getBlock())) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.GEMSTONES.ordinal()]++;
			blocks[i][j][k] = Blocks.AIR.defaultBlockState();
		} else if (state.is(Blocks.POLISHED_DIORITE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 4;
			drops[MiningDrop.TITANIUM.ordinal()]++;
		} else if (state.is(Blocks.INFESTED_STONE)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (state.is(Blocks.LIGHT_GRAY_CARPET)) {
			drops[MiningDrop.HARDSTONE.ordinal()]++;
		} else if (state.is(Blocks.PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.PRISMARINE_BRICKS)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.DARK_PRISMARINE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 3;
		} else if (state.is(Blocks.LIGHT_BLUE_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()] += 5;
		} else if (state.is(Blocks.GRAY_WOOL)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.is(Blocks.CYAN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()]++;
			drops[MiningDrop.MITHRIL.ordinal()]++;
			drops[MiningDrop.MITHRIL_POWDER.ordinal()]++;
		} else if (state.is(Blocks.BROWN_TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.is(Blocks.TERRACOTTA)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.is(Blocks.SMOOTH_RED_SANDSTONE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.UMBER.ordinal()]++;
		} else if (state.is(Blocks.INFESTED_COBBLESTONE)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else if (state.is(Blocks.CLAY)) {
			drops[MiningDrop.MINESHAFT_PITY.ordinal()] += 2;
			drops[MiningDrop.TUNGSTEN.ordinal()]++;
		} else {
			return;
		}
		breakBlocks.add(pos.offset(i - 4, j - 4, k - 4));
	}

	private static void handleGlaciteMineshafts(BlockPos pos, BlockState state, int i, int j, int k) {
		if (state.is(Blocks.STONE)) {
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
