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
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
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
								Block block = getBlockFromRepo(skyblockBlockType.itemId, skyblockBlockType.damage);
								if (block == Blocks.GRAY_WOOL || block == Blocks.CYAN_TERRACOTTA) {
									addStrength(location, block, 500, data.breakingPower);
								} else if (block == Blocks.LIGHT_BLUE_WOOL) {
									addStrength(location, block, 1500, data.breakingPower);
								} else {
									addStrength(location, block, 800, data.breakingPower);
								}
								continue;
							}

							addStrength(location, getBlockFromRepo(skyblockBlockType.itemId, skyblockBlockType.damage), data.blockStrength, data.breakingPower);
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

	public static Block getBlockFromRepo(String id, int damage) {
		return Optional.ofNullable(ItemStackTheFlatteningFix.updateItem(id, damage))
				.map(Identifier::tryParse)
				.flatMap(BuiltInRegistries.BLOCK::getOptional)
				.orElse(BuiltInRegistries.BLOCK.getValue(Identifier.tryParse(id)));
	}

}
