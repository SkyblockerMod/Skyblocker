package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.LZURISafeBase64Decoder;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.SkullRenderer;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class GreenhousePaste {
	private static Minecraft client = Minecraft.getInstance();
	private static final int PREVIEW_COLOR_ARGB = 0x62FFFFFF; // 38% opacity
	private static final float PREVIEW_ALPHA = 0.38f; // for block holograms
	private static final long BLOCK_CHANGE_RATE_LIMIT_MS = 150;
	/*
		For normal greenhouse, the grid is 10x10. Each cell can be empty (0) or contain a crop (1-40).
		For target greenhouse, the grid is also 10x10. Each cell can be ignored (-1) or empty (0) contain a crop (1-40).
	*/
	private static final Map<String, Crop> CROP_ID_MAP = new HashMap<>();
	private static final Map<Integer, Crop> CROP_BY_INT = new HashMap<>();
	private static final Map<String, Crop> CROP_BY_HEAD_TEXTURE_HASH = new HashMap<>();
	private static int[][] greenhouse = new int[10][10];
	private static int[][] targetGreenhouse = new int[10][10];
	private static BlockPos greenhouseCorner = null;
	private static long lastBlockChangeTimeMs;

	// Special ignores
	private static final Set<String> IGNORE_NAMES = Set.of(
			"PlantboyRoots",
			"godseedPillar"
	);

	static {
		// Create crops once and store them in both maps
		// TODO - verfiy armorstand names and name from website
		Crop[] crops = {
			new Crop("Ashwreath", "ashwreath", 1, HeadTextures.ASHWREATH),
			new Crop("Choconut", "Choconut", 2, HeadTextures.CHOCONUT),
			new Crop("Dustgrain", "Dustgrain", 3, HeadTextures.DUSTGRAIN),
			new Crop("Gloomgourd", "Gloomgourd", 4, HeadTextures.GLOOMGOURD),
			new Crop("Lonelily", "Lonelilly", 5, HeadTextures.LONELILY),
			new Crop("Scourroot", "Scourroot", 6, HeadTextures.SCOURROOT),
			new Crop("Shadevine", "shadevine", 7, HeadTextures.SHADEVINE),
			new Crop("Veilshroom", "Veilshroom", 8, HeadTextures.VEILSHROOM),
			new Crop("Witherbloom", "Witherbloom", 9, HeadTextures.WITHERBLOOM),
			new Crop("Chocoberry", "Chocoberry", 10, HeadTextures.CHOCOBERRY),
			new Crop("Cindershade", "Cindershade", 11, HeadTextures.CINDERSHADE),
			new Crop("Coalroot", "Coalroot", 12, HeadTextures.COALROOT),
			new Crop("Creambloom", "Creambloom", 13, HeadTextures.CREAMBLOOM),
			new Crop("Duskbloom", "duskbloom", 14, HeadTextures.DUSKBLOOM),
			new Crop("Thornshade", "thornshade", 15, HeadTextures.THORNSHADE),
			new Crop("Blastberry", "blastberry", 16, HeadTextures.BLASTBERRY),
			new Crop("Cheesebite", "cheesebite", 17, HeadTextures.CHEESEBITE),
			new Crop("Chloronite", "chloronite", 18, HeadTextures.CHLORONITE),
			new Crop("Do Not Eat Shroom", "donoteatshroom", 19, HeadTextures.DO_NOT_EAT_SHROOM),
			new Crop("Fleshtrap", "fleshtrap", 20, HeadTextures.FLESHTRAP),
			new Crop("Magic Jellybean", "magicjellybean", 21, HeadTextures.MAGIC_JELLYBEAN),
			new Crop("Noctilume", "noctilume", 22, HeadTextures.NOCTILUME),
			new Crop("Snoozling", "snoozlingFlower", 23, HeadTextures.SNOOZLING), // requires special handling, only look for head
			new Crop("Soggybud", "soggybud", 24, HeadTextures.SOGGYBUD),
			new Crop("Chorus Fruit", "chorusFruit", 25, HeadTextures.CHORUS_FRUIT),
			new Crop("PlantBoy Advance", "Plantboy", 26, HeadTextures.PLANTBOY_ADVANCE), // plantboyroot is also a thing. also is 2x2, so adjust accordingly
			new Crop("Puffercloud", "puffercloud", 27, HeadTextures.PUFFERCLOUD),
			new Crop("Shellfruit", "shellfruit", 28, HeadTextures.SHELLFRUIT),
			new Crop("Startlevine", "startlevine", 29, HeadTextures.STARTLEVINE),
			new Crop("Stoplight Petal", "stoplightpetal", 30, HeadTextures.STOPLIGHT_PETAL),
			new Crop("Thunderling", "thunderling", 31, HeadTextures.THUNDERLING),
			new Crop("Turtlellini", "turtlellini", 32, HeadTextures.TURTLELLINI),
			new Crop("Zombud", "zombud", 33, HeadTextures.ZOMBUD),
			new Crop("All In Aloe", "allinaloe", 34, HeadTextures.ALL_IN_ALOE),
			new Crop("Devourer", "devourer", 35, HeadTextures.DEVOURER),
			new Crop("Glasscorn", "glasscorn", 36, HeadTextures.GLASSCORN),
			new Crop("Godseed", "godseed", 37, HeadTextures.GODSEED), // also has godseedpillar 's around it
			new Crop("Jerryflower", "jerryseed", 38, HeadTextures.JERRYFLOWER), // weird naming??
			new Crop("Phantomleaf", "phantomleaf", 39, HeadTextures.PHANTOMLEAF),
			new Crop("Timestalk", "timestalk", 40, HeadTextures.TIMESTALK), // verify

			// Crops
			new Crop("Cocoa Beans", "coco", 41, HeadTextures.COCOA_BEANS),
			new Crop("Wild Rose", "wildrose", 42, HeadTextures.WILD_ROSE),
			new Crop("Moonflower", "moonflower", 43, HeadTextures.MOONFLOWER),
			new Crop("Sunflower", "sunflower", 44, HeadTextures.SUNFLOWER),
			new Crop("Cactus", "cactus", 45, HeadTextures.CACTUS),
			new Crop("Melon Seeds", "melon", 46, HeadTextures.MELON),
			new Crop("Pumpkin Seeds", "pumpkin", 47, HeadTextures.PUMPKIN),
			new Crop("Brown Mushroom", "brownmushroom", 48, HeadTextures.BROWN_MUSHROOM), // TODO: make mushrooms interchangeable, also replace with head texture
			new Crop("Red Mushroom", "redmushroom", 49, HeadTextures.RED_MUSHROOM),
			new Crop("Wheat Seeds", "wheat", 50, Blocks.WHEAT),
			new Crop("Carrot", "carrot", 51, Blocks.CARROTS),
			new Crop("Potato", "potato", 52, Blocks.POTATOES),
			new Crop("Nether Wart", "netherwart", 53, Blocks.NETHER_WART),
			new Crop("Sugar Cane", "sugarcane", 54, Blocks.SUGAR_CANE),

			// Misc
			new Crop("Dead Plant", "deadplant", 55, Blocks.DEAD_BUSH),
			new Crop("Fire", "fire", 56, Blocks.FIRE),

			// Special crops
			new Crop("Helianthus", "helianthus", 57, HeadTextures.HELIANTHUS),
			new Crop("Fermento", "fermento", 58, HeadTextures.FERMENTO),
			new Crop("Squash", "squash", 59, HeadTextures.SQUASH),
			new Crop("Cropie", "cropie", 60, HeadTextures.CROPIE),
		};

		// Populate both maps
		for (Crop crop : crops) {
			CROP_ID_MAP.put(crop.name, crop);
			CROP_BY_INT.put(crop.id, crop);
			if (crop.isHead) {
				CROP_BY_HEAD_TEXTURE_HASH.put(crop.headSkin, crop);
			}
		}
	}

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("greenhousepaste").executes(_ -> runGreenhousePaste()))
				.then(literal("greenhousepasteremove").executes(_ -> runGreenhousePasteRemove()))
						.then(literal("greenhousedebug").executes(_ -> {
							debugPrintGreenhouses();
							return Command.SINGLE_SUCCESS;
						}))));

		// Register render callback
		LevelRenderExtractionCallback.EVENT.register(collector -> {
			if (!Utils.isInGarden()) return;
			renderPreview(collector);
		});

		PlayerBlockBreakEvents.AFTER.register((_, _, pos, _, _) -> onBlockChange(pos));

		UseBlockCallback.EVENT.register((_, _, _, hitResult) -> {
			onBlockChange(hitResult.getBlockPos());
			return net.minecraft.world.InteractionResult.PASS;
		});

		// Initialize greenhouse arrays
		removePreview();
	}

	// If armor stand crops are changed
	public static void onEntityChange(Entity e) {
		if (greenhouseCorner == null) return;
		if (greenhouseCorner.getX() - 1 > e.getX() ||
				greenhouseCorner.getX() + 11 < e.getX() ||
				greenhouseCorner.getZ() - 1 > e.getZ() ||
				greenhouseCorner.getZ() + 11 < e.getZ()) return;
		locateGreenhouse();
	}

	// If non armor stand crops are changed
	public static void onBlockChange(BlockPos pos) {
		if (greenhouseCorner == null) return;
		if (greenhouseCorner.getX() - 1 >= pos.getX() ||
				greenhouseCorner.getX() + 11 <= pos.getX() ||
				greenhouseCorner.getZ() - 1 >= pos.getZ() ||
				greenhouseCorner.getZ() + 11 <= pos.getZ()) return;

		long currentTimeMs = System.currentTimeMillis();
		if (currentTimeMs - lastBlockChangeTimeMs < BLOCK_CHANGE_RATE_LIMIT_MS) return;
		lastBlockChangeTimeMs = currentTimeMs;

		locateGreenhouse();
	}

	private static int runGreenhousePaste() {
		if (client.player == null) return Command.SINGLE_SUCCESS;
		loadFromLink();
		return Command.SINGLE_SUCCESS;
	}

	private static int runGreenhousePasteRemove() {
		if (client.player == null) return Command.SINGLE_SUCCESS;
		removePreview();
		client.player.sendSystemMessage(Constants.PREFIX.get().append(Component.literal("Greenhouse preview removed.").withStyle(ChatFormatting.GREEN)));
		return Command.SINGLE_SUCCESS;
	}

	public static void removePreview() {
		greenhouseCorner = null;
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				greenhouse[x][y] = 0;
				targetGreenhouse[x][y] = -1;
			}
		}
	}

	public static void loadFromLink() {
		if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
		String clipboard = client.keyboardHandler.getClipboard();
		String[] parts = clipboard.split("\\?layout=");
		String encoded = parts.length > 1 ? parts[1] : clipboard;

		if (encoded == null || encoded.isEmpty()) return;

		boolean success = importGreenhouse(encoded);
		if (!success) {
			client.player.sendSystemMessage(
					Constants.PREFIX.get()
							.append(Component.literal("Failed to load greenhouse layout from clipboard. Extracted content: ")
									.withStyle(ChatFormatting.RED))
							.append(Component.literal(encoded)
									.withStyle(ChatFormatting.GRAY))
			);
			return;
		}

		client.player.sendSystemMessage(
				Constants.PREFIX.get()
						.append(Component.literal("Greenhouse loaded successfully!")
								.withStyle(ChatFormatting.GREEN)));

		locateGreenhouse();
	}

	// Get info of current greenhouse
	public static void locateGreenhouse() {
		BlockPos playerPos = client.player.blockPosition();
		/*
			Math:
			- plot is 96x96 on 5x5 grid
			- center at center plot is 0,0
			- normalize to corner by adding (5/2) * 96 = 240
			- offset by 96/2 - (plot size = 10)/2 = 43 to get lower left of garden grid
			- record positions
		 */
		BlockPos plotPos = playerPos.offset(240, 0, 240);

		plotPos = new BlockPos(
				((int) (plotPos.getX() / 96.0) * 96) - 240,
				73, // Greenhouse is always at y=73 (I think)
				((int) (plotPos.getZ() / 96.0) * 96) - 240
		);

		greenhouseCorner = new BlockPos(plotPos.getX() + 43, 73, plotPos.getZ() + 43);

		// Load current greenhouse state into array
		// im poor so i dont have all greenhouse skins, so TODO: verify Y level does not vary for different skins
		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, 73, greenhouseCorner.getZ() + y);
				int cropId = getCropIdAtPosition(client.level, pos);
				greenhouse[x][y] = cropId;
			}
		}
		adjustForSpecialCrops();
	}

	private static int getCropIdAtPosition(net.minecraft.world.level.Level level, BlockPos pos) {
		// Scan a 1x5x1 column
		net.minecraft.world.phys.AABB detectionBox = new net.minecraft.world.phys.AABB(
				pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 6, pos.getZ() + 1
		);

		// Determine crop ID based on the name of armor stand, should also detect non head crops
		for (net.minecraft.world.entity.Entity entity : level.getEntities(null, detectionBox)) {
			if (!(entity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand)) continue;

			ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
			if (head.isEmpty()) continue;

			Component nameComponent = head.getHoverName();
			String name = nameComponent.getString();

			if (IGNORE_NAMES.contains(name)) return 0; // Make blocks are ignored too

			for (Crop crop : CROP_ID_MAP.values()) {
				if (name.contains(crop.armorStandName)) {
					return crop.id;
				}
			}

			// Detection by texture for the special crops
			Optional<String> texture = ItemUtils.getHeadTextureOptional(head);
			if (texture.isEmpty()) continue;

			Crop cropByTexture = CROP_BY_HEAD_TEXTURE_HASH.get(texture.get());
			if (cropByTexture == null) continue;
			if (!HeadTextures.SPECIAL_CROPS.contains(cropByTexture.headSkin)) continue;

			return cropByTexture.id;
		}

		// If no armor stand found, fallback to checking block type (for non-head crops)
		BlockState state = level.getBlockState(pos.above());
		Block block = state.getBlock();
		for (Crop crop : CROP_ID_MAP.values()) {
			if (!crop.isHead && crop.cropBlock.equals(block)) {
				return crop.id;
			}
		}

		return 0;
	}

	private static void adjustForSpecialCrops() {
		// Special handling for Plantboy (2x2 crop)
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				adjustForPlantBoy(x, y);
				adjustForSnoozling(x, y);
				adjustForGodseed(x, y);
			}
		}
	}

	private static void adjustForPlantBoy(int x, int y) {
		if (greenhouse[x][y] != 26) return;

		BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, greenhouseCorner.getY(), greenhouseCorner.getZ() + y); // sorry for cursed notation
		net.minecraft.world.phys.AABB detectionBox = new net.minecraft.world.phys.AABB(
				pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 6, pos.getZ() + 1
		);

		ArmorStand foundArmorStand = null;
		// Determine crop ID based on the name of armor stand, should also detect non head crops
		for (Entity entity : client.level.getEntities(null, detectionBox)) {
			if (!(entity instanceof ArmorStand armorStand)) continue;

			ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
			if (head.isEmpty()) continue;

			Component nameComponent = head.getHoverName();
			String name = nameComponent.getString();
			if (name.contains("Plantboy") && !name.contains("Advance")) {
				foundArmorStand = armorStand;
				break;
			}
		}

		if (foundArmorStand == null) return;

		// Stragtegy: since the plantboy is in the middle of the 2x2, we offset by a bit to get the bottom left corner
		foundArmorStand.position().add(-0.2, 0, -0.2);

		BlockPos bottomLeft = new BlockPos((int) Math.floor(foundArmorStand.getX()), (int) Math.floor(foundArmorStand.getY()), (int) Math.floor(foundArmorStand.getZ()));

		greenhouse[bottomLeft.getX() - greenhouseCorner.getX()][bottomLeft.getZ() - greenhouseCorner.getZ()] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX() + 1][bottomLeft.getZ() - greenhouseCorner.getZ()] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX()][bottomLeft.getZ() - greenhouseCorner.getZ() + 1] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX() + 1][bottomLeft.getZ() - greenhouseCorner.getZ() + 1] = 26;
	}

	private static void adjustForSnoozling(int x, int y) {
		if (greenhouse[x][y] != 23) return;
		try {
			greenhouse[x-1][y] = 23;
			greenhouse[x+1][y] = 23;

			greenhouse[x-1][y-1] = 23;
			greenhouse[x][y-1] = 23;
			greenhouse[x+1][y-1] = 23;

			greenhouse[x-1][y-2] = 23;
			greenhouse[x][y-2] = 23;
			greenhouse[x+1][y-2] = 23;
		}
		catch (ArrayIndexOutOfBoundsException _) { }
	}

	private static void adjustForGodseed(int x, int y) {
		if (greenhouse[x][y] != 37) return;
		try {
			greenhouse[x-1][y] = 37;
			greenhouse[x+1][y] = 37;

			greenhouse[x-1][y-1] = 37;
			greenhouse[x][y-1] = 37;
			greenhouse[x+1][y-1] = 37;

			greenhouse[x-1][y+1] = 37;
			greenhouse[x][y+1] = 37;
			greenhouse[x+1][y+1] = 37;
		}
		catch (ArrayIndexOutOfBoundsException _) { }
	}

	/**
	 * Imports the greenhouse layout from an LZ-encoded string.
	 * The string is decompressed and parsed as a JSON array.
	 * Each entry is [x, y, "crop_name", value].
	 */
	public static boolean importGreenhouse(String encoded) {
		try {
			String jsonString = LZURISafeBase64Decoder.decodeLZString(encoded);
			JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

			for (int x = 0; x < 10; x++) {
				for (int y = 0; y < 10; y++) {
					targetGreenhouse[x][y] = -1;
				}
			}

			// Populate greenhouse from JSON
			for (JsonElement element : jsonArray) {
				JsonArray entry = element.getAsJsonArray();
				int x = entry.get(0).getAsInt();
				int y = entry.get(1).getAsInt();
				String cropName = entry.get(2).getAsString();
				int value = entry.get(3).getAsInt(); // 0 = desired mutation, 1 = place, accoridng to the website
				if (value == 0) {
					targetGreenhouse[x][y] = 0; // Desired mutation spot should be empty
					continue;
				}

				Crop crop = CROP_ID_MAP.get(cropName);
				if (crop == null) continue;
				if (x >= 0 && x < 10 && y >= 0 && y < 10) {
					targetGreenhouse[x][y] = crop.id;
				}
			}
		} catch (Exception _) {
			return false;
		}
		return true;
	}

	public static void renderPreview(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
		if (greenhouseCorner == null) return;

		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				// Ignore cell if -1
				int targetCropId = targetGreenhouse[x][y];
				if (targetCropId == -1) continue;

				int currentCropId = greenhouse[x][y];
				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, greenhouseCorner.getY() + 1, greenhouseCorner.getZ() + y);

				// Mutation spots target empty blocks; render indicator and skip crop lookup.
				if (targetCropId == 0) {
					if (currentCropId == 0) {
						if (SkyblockerConfigManager.get().farming.greenhouse.showMutationSlot) // oh my days
							collector.submitOutlinedBox(
									new net.minecraft.world.phys.AABB(
											pos.getX(), pos.getY() - 0.05, pos.getZ(),
											pos.getX() + 1, pos.getY() + 0.1, pos.getZ() + 1
									),
									new float[]{0f, 1f, 0f},
									0.2f,
									3f,
									false
							);
					} else {
						collector.submitOutlinedBox(
								new net.minecraft.world.phys.AABB(pos),
								new float[]{1f, 0f, 0f},
								0.5f,
								4f,
								true
						);
					}
					continue;
				}

				// Already correct
				if (currentCropId == targetCropId) continue;

				if (currentCropId == 49 || currentCropId == 48) { // Mushrooms are interchangeable, so ignore if its the wrong mushroom
					if (targetCropId == 48 || targetCropId == 49) continue;
				}

				Crop targetCrop = CROP_BY_INT.get(targetCropId);
				if (targetCrop == null) continue;

				if (currentCropId != 0) { // Undesired spot that is not empty
					collector.submitOutlinedBox(new net.minecraft.world.phys.AABB(pos), new float[]{1f, 0f, 0f}, 0.5f, 4f, true);
				} else if (targetCrop.isHead) {
					SkullRenderer.submitSkull(collector, pos, targetCrop.displayStack, PREVIEW_COLOR_ARGB);
				} else {
					BlockState blockState = targetCrop.cropBlock.defaultBlockState();
					if (targetCrop.cropBlock instanceof CropBlock) {
						blockState = ((CropBlock) targetCrop.cropBlock).defaultBlockState().setValue(CropBlock.AGE, 7);
					}
					collector.submitBlockHologram(pos, blockState, PREVIEW_ALPHA);
				}
			}
		}
	}

	public static class Crop {
		private final String name;
		private final String armorStandName;
		private final int id;
		private final String headSkin;
		private final boolean isHead;
		private final Block cropBlock;
		private final FlexibleItemStack displayStack;

		public Crop(String name, String armorStandName, int id, String headSkin) {
			this.name = name;
			this.armorStandName = armorStandName;
			this.id = id;
			this.isHead = true;
			this.headSkin = headSkin;
			this.displayStack = ItemUtils.createSkull(headSkin);
			this.cropBlock = null;
		}

		public Crop(String name, String armorStandName, int id, Block cropBlock) {
			this.name = name;
			this.armorStandName = armorStandName;
			this.id = id;
			this.isHead = false;
			this.cropBlock = cropBlock;
			this.headSkin = null;
			this.displayStack = null;
		}
	}

	// DEBUG

	private static void debugPrintGreenhouses() {
		if (client.player == null) return;

		client.player.sendSystemMessage(Component.literal("=== CURRENT GREENHOUSE ===").withStyle(ChatFormatting.YELLOW));
		printGrid(greenhouse);

		client.player.sendSystemMessage(Component.literal("=== TARGET GREENHOUSE ===").withStyle(ChatFormatting.AQUA));
		printGrid(targetGreenhouse);
	}

	private static void printGrid(int[][] grid) {
		StringBuilder all = new StringBuilder();
		for (int z = 0; z < 10; z++) {
			StringBuilder row = new StringBuilder();

			for (int x = 0; x < 10; x++) {
				int val = grid[x][z];

				// nicer formatting
				if (val == -1) {
					row.append(" - ");
				} else {
					row.append(String.format("%2d ", val));
				}
			}
			all.append(row).append("\n");
		}
		client.player.sendSystemMessage(Component.literal(all.toString()));
	}

}
