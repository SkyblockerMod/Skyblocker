package de.hysky.skyblocker.skyblock.dwarven;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.Utils;
import io.github.moulberry.repo.NEURepoFile;
import io.github.moulberry.repo.NEURepositoryException;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockBreakPrediction {
	private static final Map<String, Map<Block, Integer>> blockStrengths = new HashMap<>();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern MINING_SPEED_PATTERN = Pattern.compile("Mining Speed: ⸕(\\d+)");

	private static boolean newBlock = false;
	private static boolean soundPlayed = false;
	private static long currentBlockBreakTime;
	private static long startAttackingTime;


	@Init
	public static void init() {
		AttackBlockCallback.EVENT.register(BlockBreakPrediction::onBlockInteract);
		NEURepoManager.runAsyncAfterLoad(BlockBreakPrediction::loadBlockStrength);

	}


	private static ActionResult onBlockInteract(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
		newBlock = true;
		startAttackingTime = System.currentTimeMillis();
		soundPlayed = false;
		return ActionResult.PASS;
	}

	public static int getBlockBreakPrediction(BlockPos pos, int progression) {
		if (CLIENT.player == null || !SkyblockerConfigManager.get().mining.BlockBreakPrediction.enabled) return progression;
		if (CLIENT.crosshairTarget instanceof BlockHitResult hitResult) {
			if (!hitResult.getBlockPos().equals(pos)) {
				return progression;
			}
		}

		//make sure it's the block the player is looking at
		if (newBlock) {
			newBlock = false;
			currentBlockBreakTime = getBreakTime(pos);
		}
		if (currentBlockBreakTime > 0) {
			long timeElapsed = System.currentTimeMillis() - startAttackingTime;
			if (SkyblockerConfigManager.get().mining.BlockBreakPrediction.playSound && !soundPlayed && (int) ((timeElapsed * 10) / (currentBlockBreakTime)) == 10) {
				soundPlayed = true;
				CLIENT.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 1f);
			}
			return Math.min((int) ((timeElapsed * 10) / (currentBlockBreakTime)), 9);
		}
		System.out.println(progression);
		return progression;

	}

	private static int getCurrentMiningSpeed() {
		if (CLIENT.player == null) {
			return -1;
		}

		Optional<Matcher> speed = PlayerListManager.getPlayerStringList().stream().map(MINING_SPEED_PATTERN::matcher).filter(Matcher::matches).findFirst();
		//make sure the data is in tab and if not tell the user
		if (speed.isEmpty()) {
			CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("TODO tell user to enable mining speed stat")), false);
			return -1;
		}


		return NumberUtils.toInt(speed.get().group(1));
	}

	private static long getBreakTime(BlockPos pos) {
		if (CLIENT.world == null) return -1;
		Block targetBlock = CLIENT.world.getBlockState(pos).getBlock();
		if (!blockStrengths.containsKey(Utils.getLocationRaw()) || !blockStrengths.get(Utils.getLocationRaw()).containsKey(targetBlock)) return -1;
		int blockStrength = blockStrengths.get(Utils.getLocationRaw()).get(targetBlock);
		int miningSpeed = getCurrentMiningSpeed();
		//using equation mining time (ticks) = (block strength x 30) / mining speed

		return (long) (50 * Math.min(((blockStrength * 30f) / miningSpeed), (20f / 3) * blockStrength));

	}

	public static void addStrength(String location, Block blockId, int strength) {
		blockStrengths
				.computeIfAbsent(location, k -> new HashMap<>())
				.put(blockId, strength);
	}

	private static void loadBlockStrength() {
		try {
			List<NEURepoFile> blocks = NEURepoManager.tree("mining/blocks").toList();
			for (NEURepoFile file : blocks) {
				if (!file.isFile()) continue;
				try (InputStream stream = file.stream()) {
					//get block id data
					blockFile data = blockFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(new String(stream.readAllBytes()))).getOrThrow();
					//add each block to lookup table for location
					for (block blockType : data.blocks) {
						for (String location : blockType.onlyIn) {
							//if its mithril edit it to the actual strength as that is not in the repo
							if (data.name.equals("Mithril Ore")) {
								Block block = LegacyLookup.get(blockType.itemId, blockType.damage);
								if (block == Blocks.GRAY_WOOL || block == Blocks.CYAN_TERRACOTTA) {
									addStrength(location, block, 500);
								} else if (block == Blocks.LIGHT_BLUE_WOOL) {
									addStrength(location, block, 1500);
								} else {
									addStrength(location, block, 800);
								}
								continue;
							}

							addStrength(location, LegacyLookup.get(blockType.itemId, blockType.damage), data.blockStrength);
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					//LOGGER.error("[Skyblocker Attributes] Failed to load attributes!", ex);
				}
			}


		} catch (NEURepositoryException exception) {
			exception.printStackTrace();
		}


	}

	public record blockFile(int blockStrength, String name, List<block> blocks) {
		public static final Codec<blockFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("blockStrength").forGetter(blockFile::blockStrength),
				Codec.STRING.fieldOf("name").forGetter(blockFile::name),
				block.LIST_CODEC.fieldOf("blocks189").forGetter(blockFile::blocks)
		).apply(instance, blockFile::new));
	}

	public record block(String itemId, int damage, List<String> onlyIn) { //
		private static final Codec<block> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("itemId").forGetter(block::itemId),
				Codec.INT.fieldOf("damage").forGetter(block::damage),
				Codec.STRING.listOf().fieldOf("onlyIn").forGetter(block::onlyIn)
		).apply(instance, block::new));
		public static final Codec<List<block>> LIST_CODEC = CODEC.listOf();
	}

	// I don't like this code but don't see another way to do it but it basicaly removes the point from loading it from the repo
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
			return Registries.BLOCK.get(Identifier.of(TABLE.get(id + "," + damage)));
		}
	}


}
