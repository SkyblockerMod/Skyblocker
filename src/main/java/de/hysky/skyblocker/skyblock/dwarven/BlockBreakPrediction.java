package de.hysky.skyblocker.skyblock.dwarven;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import io.github.moulberry.repo.NEURepoFile;
import io.github.moulberry.repo.NEURepositoryException;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockBreakPrediction {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final EnumMap<Location, Map<Block, IntIntPair>> blockStrengths = new EnumMap<>(Location.class);
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern MINING_SPEED_PATTERN = Pattern.compile("Mining Speed: ⸕(\\d+)");
	private static final Pattern BREAKING_POWER_PATTERN = Pattern.compile("Breaking Power (\\d+)");

	private static boolean newBlock = false;
	private static boolean soundPlayed = false;
	private static long currentBlockBreakTime;
	private static long startAttackingTime;
	private static boolean sentWarningMessage = false;


	@Init
	public static void init() {
		AttackBlockCallback.EVENT.register(BlockBreakPrediction::onBlockInteract);
		NEURepoManager.runAsyncAfterLoad(BlockBreakPrediction::loadBlockStrength);

	}

	private static InteractionResult onBlockInteract(Player player, Level level, InteractionHand interactionHand, BlockPos blockPos, Direction direction) {
		newBlock = true;
		startAttackingTime = System.currentTimeMillis();
		soundPlayed = false;
		return InteractionResult.PASS;
	}


	public static int getBlockBreakPrediction(BlockPos pos, int progression) {
		if (CLIENT.player == null || CLIENT.gameMode == null || !SkyblockerConfigManager.get().mining.blockBreakPrediction.enabled)
			return progression;

		// only modify target block
		if (CLIENT.hitResult instanceof BlockHitResult hitResult) {
			if (!hitResult.getBlockPos().equals(pos)) {
				return progression;
			}
		} else {
			return progression;
		}

		//find breaking time of new block
		if (newBlock) {
			newBlock = false;
			//get the breaking power of the current tool
			Matcher loreMatch = ItemUtils.getLoreLineIfMatch(CLIENT.player.getMainHandItem(), BREAKING_POWER_PATTERN);
			if (loreMatch != null) {
				int toolBreakingPower = NumberUtils.toInt(loreMatch.group(1));
				currentBlockBreakTime = getBreakTime(pos, toolBreakingPower);
			} else {
				currentBlockBreakTime = -1;
			}

		}

		//reset if player stops mining the block
		if (!CLIENT.gameMode.isDestroying()) {
			currentBlockBreakTime = -1;
		}

		if (currentBlockBreakTime > 0) {
			long timeElapsed = System.currentTimeMillis() - startAttackingTime;
			if (SkyblockerConfigManager.get().mining.blockBreakPrediction.playSound && !soundPlayed && (int) ((timeElapsed * 10) / (currentBlockBreakTime)) == 10) {
				soundPlayed = true;
				CLIENT.player.playSound(SoundEvents.BLAZE_HURT, 100f, 1f);
			}
			return Math.min((int) ((timeElapsed * 10) / (currentBlockBreakTime)), 9);
		}
		return progression;

	}

	private static int getCurrentMiningSpeed() {
		if (CLIENT.player == null) {
			return -1;
		}

		Optional<Matcher> speed = PlayerListManager.getPlayerStringList().stream().map(MINING_SPEED_PATTERN::matcher).filter(Matcher::matches).findFirst();
		//make sure the data is in tab and if not tell the user
		if (speed.isEmpty()) {
			if (!sentWarningMessage) {
				CLIENT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.blockBreakPrediction.enableStatsMessage")).withStyle(ChatFormatting.RED), false);
				sentWarningMessage = true;
			}
			return -1;
		}


		return NumberUtils.toInt(speed.get().group(1));
	}

	private static long getBreakTime(BlockPos pos, int toolBreakingPower) {
		if (CLIENT.level == null) return -1;
		Block targetBlock = CLIENT.level.getBlockState(pos).getBlock();
		if (!blockStrengths.containsKey(Utils.getLocation()) || !blockStrengths.get(Utils.getLocation()).containsKey(targetBlock))
			return -1;
		Pair<Integer, Integer> blockStrengthAndBreakingPower = blockStrengths.get(Utils.getLocation()).get(targetBlock);
		//if block can not be broken do not calculate
		if (blockStrengthAndBreakingPower.second() > toolBreakingPower) return -1;
		int blockStrength = blockStrengthAndBreakingPower.first();
		int miningSpeed = getCurrentMiningSpeed();
		//using equation mining time (ticks) = (block strength x 30) / mining speed

		return (long) (50 * Math.min(((blockStrength * 30f) / miningSpeed), (20f / 3) * blockStrength));

	}

	public static void addStrength(Location location, Block blockId, int strength, int breakingPower) {
		blockStrengths
				.computeIfAbsent(location, k -> new HashMap<>())
				.put(blockId, IntIntPair.of(strength, breakingPower));
	}

	private static void loadBlockStrength() {
		try {
			List<NEURepoFile> blocks = NEURepoManager.tree("mining/blocks").toList();

			for (NEURepoFile file : blocks) {
				if (!file.isFile()) continue;
				try (InputStream stream = file.stream()) {
					//get block id data
					BlockFile data = BlockFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(new String(stream.readAllBytes()))).getOrThrow();
					//add each block to lookup table for location
					for (SkyblockBlock skyblockBlockType : data.skyblockBlocks) {
						for (Location location : skyblockBlockType.onlyIn) {
							//if its mithril edit it to the actual strength as that is not in the repo
							if (data.name.equals("Mithril Ore")) {
								Block block = LegacyLookup.get(skyblockBlockType.itemId, skyblockBlockType.damage);
								if (block == Blocks.GRAY_WOOL || block == Blocks.CYAN_TERRACOTTA) {
									addStrength(location, block, 500, data.breakingPower);
								} else if (block == Blocks.LIGHT_BLUE_WOOL) {
									addStrength(location, block, 1500, data.breakingPower);
								} else {
									addStrength(location, block, 800, data.breakingPower);
								}
								continue;
							}

							addStrength(location, LegacyLookup.get(skyblockBlockType.itemId, skyblockBlockType.damage), data.blockStrength, data.breakingPower);
						}
					}

				} catch (Exception ex) {
					LOGGER.error("[Skyblocker BlockBreakPredictions] Failed to load mining blocks!", ex);
				}
			}


		} catch (NEURepositoryException exception) {
			LOGGER.error("[Skyblocker BlockBreakPredictions] Failed to load mining blocks!", exception);
		}


	}

	public record BlockFile(int blockStrength, int breakingPower, String name, List<SkyblockBlock> skyblockBlocks) {
		public static final Codec<BlockFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("blockStrength").forGetter(BlockFile::blockStrength),
				Codec.INT.fieldOf("breakingPower").forGetter(BlockFile::breakingPower),
				Codec.STRING.fieldOf("name").forGetter(BlockFile::name),
				SkyblockBlock.LIST_CODEC.fieldOf("blocks189").forGetter(BlockFile::skyblockBlocks)
		).apply(instance, BlockFile::new));
	}

	public record SkyblockBlock(String itemId, int damage, List<Location> onlyIn) {
		private static final Codec<SkyblockBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("itemId").forGetter(SkyblockBlock::itemId),
				Codec.INT.fieldOf("damage").forGetter(SkyblockBlock::damage),
				Location.CODEC.listOf().fieldOf("onlyIn").forGetter(SkyblockBlock::onlyIn)
		).apply(instance, SkyblockBlock::new));
		public static final Codec<List<SkyblockBlock>> LIST_CODEC = CODEC.listOf();
	}

	// I don't like this code but don't see another way to do it, but it basically removes the point from loading it from the repo
	public static class LegacyLookup {

		public static final Map<String, String> TABLE = new HashMap<>();

		static {

			// ---- STAINED GLASS ----
			put("minecraft:stained_glass,0", "minecraft:white_stained_glass");
			put("minecraft:stained_glass,1", "minecraft:orange_stained_glass");
			put("minecraft:stained_glass,3", "minecraft:light_blue_stained_glass");
			put("minecraft:stained_glass,4", "minecraft:yellow_stained_glass");
			put("minecraft:stained_glass,5", "minecraft:lime_stained_glass");
			put("minecraft:stained_glass,6", "minecraft:pink_stained_glass");
			put("minecraft:stained_glass,10", "minecraft:purple_stained_glass");
			put("minecraft:stained_glass,11", "minecraft:blue_stained_glass");
			put("minecraft:stained_glass,12", "minecraft:brown_stained_glass");
			put("minecraft:stained_glass,13", "minecraft:green_stained_glass");
			put("minecraft:stained_glass,14", "minecraft:red_stained_glass");
			put("minecraft:stained_glass,15", "minecraft:black_stained_glass");

			// ---- STAINED GLASS PANES ----
			put("minecraft:stained_glass_pane,0", "minecraft:white_stained_glass_pane");
			put("minecraft:stained_glass_pane,1", "minecraft:orange_stained_glass_pane");
			put("minecraft:stained_glass_pane,3", "minecraft:light_blue_stained_glass_pane");
			put("minecraft:stained_glass_pane,4", "minecraft:yellow_stained_glass_pane");
			put("minecraft:stained_glass_pane,5", "minecraft:lime_stained_glass_pane");
			put("minecraft:stained_glass_pane,6", "minecraft:pink_stained_glass_pane");
			put("minecraft:stained_glass_pane,10", "minecraft:purple_stained_glass_pane");
			put("minecraft:stained_glass_pane,11", "minecraft:blue_stained_glass_pane");
			put("minecraft:stained_glass_pane,12", "minecraft:brown_stained_glass_pane");
			put("minecraft:stained_glass_pane,13", "minecraft:green_stained_glass_pane");
			put("minecraft:stained_glass_pane,14", "minecraft:red_stained_glass_pane");
			put("minecraft:stained_glass_pane,15", "minecraft:black_stained_glass_pane");

			// ---- TERRACOTTA ----
			put("minecraft:stained_hardened_clay,7", "minecraft:gray_terracotta");
			put("minecraft:stained_hardened_clay,8", "minecraft:light_gray_terracotta");
			put("minecraft:stained_hardened_clay,9", "minecraft:cyan_terracotta");
			put("minecraft:stained_hardened_clay,12", "minecraft:brown_terracotta");
			put("minecraft:stained_hardened_clay,15", "minecraft:black_terracotta");
			put("minecraft:hardened_clay,0", "minecraft:terracotta");

			// ---- WOOL ----
			put("minecraft:wool,3", "minecraft:light_blue_wool");
			put("minecraft:wool,7", "minecraft:gray_wool");
			put("minecraft:wool,8", "minecraft:light_gray_wool");

			// ---- PRISMARINE ----
			put("minecraft:prismarine,0", "minecraft:prismarine");
			put("minecraft:prismarine,1", "minecraft:prismarine_bricks");
			put("minecraft:prismarine,2", "minecraft:dark_prismarine");

			// ---- SIMPLE BLOCKS ----
			put("minecraft:packed_ice,0", "minecraft:packed_ice");
			put("minecraft:stone,0", "minecraft:stone");
			put("minecraft:stone,4", "minecraft:polished_diorite");
			put("minecraft:cobblestone,0", "minecraft:cobblestone");
			put("minecraft:clay,0", "minecraft:clay");

			put("minecraft:coal_block,0", "minecraft:coal_block");
			put("minecraft:diamond_block,0", "minecraft:diamond_block");
			put("minecraft:emerald_block,0", "minecraft:emerald_block");
			put("minecraft:gold_block,0", "minecraft:gold_block");
			put("minecraft:iron_block,0", "minecraft:iron_block");
			put("minecraft:lapis_block,0", "minecraft:lapis_block");
			put("minecraft:quartz_block,0", "minecraft:quartz_block");
			put("minecraft:redstone_block,0", "minecraft:redstone_block");
			put("minecraft:sponge,0", "minecraft:sponge");

			put("minecraft:red_sandstone,0", "minecraft:red_sandstone");
		}

		private static void put(String legacyKey, String modernId) {
			TABLE.put(legacyKey, modernId);
		}

		public static Block get(String id, int damage) {
			return BuiltInRegistries.BLOCK.getValue(Identifier.parse(TABLE.get(id + "," + damage)));
		}
	}


}
