package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.skyblock.item.PlayerHeadHashCache;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.LZURISafeBase64Decoder;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.SkullRenderer;
import de.hysky.skyblocker.utils.render.primitive.BlockHologramRenderer;
import de.hysky.skyblocker.utils.render.primitive.OutlinedBoxInstancedRenderer;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollectorImpl;
import de.hysky.skyblocker.utils.render.state.BlockHologramRenderState;
import de.hysky.skyblocker.utils.render.state.OutlinedBoxRenderState;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class GreenhousePaste {
	private static Minecraft client = Minecraft.getInstance();
	private static final int PREVIEW_COLOR_ARGB = 0x62FFFFFF; // 38% opacity white

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

	static {
		// Create crops once and store them in both maps
		Crop[] crops = {
			new Crop("Ashwreath", "ashwreath", 1, HeadTextures.ASHWREATH),
			new Crop("Choconut","choconut", 2, HeadTextures.CHOCONUT),
			new Crop("Dustgrain", "dustgrain", 3, HeadTextures.DUSTGRAIN),
			new Crop("Gloomgourd", "gloomgourd", 4, HeadTextures.GLOOMGOURD),
			new Crop("Lonelily", "lonelily", 5, HeadTextures.LONELILY),
			new Crop("Scourroot", "scourroot", 6, HeadTextures.SCOURROOT),
			new Crop("Shadevine", "shadevine", 7, HeadTextures.SHADEVINE),
			new Crop("Veilshroom", "veilshroom", 8, HeadTextures.VEILSHROOM),
			new Crop("Witherbloom", "witherbloom", 9, HeadTextures.WITHERBLOOM),
			new Crop("Chocoberry", "chocoberry", 10, HeadTextures.CHOCOBERRY),
			new Crop("Cindershade", "cindershade", 11, HeadTextures.CINDERSHADE),
			new Crop("Coalroot", "coalroot", 12, HeadTextures.COALROOT),
			new Crop("Creambloom", "creambloom", 13, HeadTextures.CREAMBLOOM),
			new Crop("Duskbloom", "duskbloom", 14, HeadTextures.DUSKBLOOM),
			new Crop("Thornshade", "thornshade", 15, HeadTextures.THORNSHADE),
			new Crop("Blastberry", "blastberry", 16, HeadTextures.BLASTBERRY),
			new Crop("Cheesebite", "cheesebite", 17, HeadTextures.CHEESEBITE),
			new Crop("Chloronite", "chloronite", 18, HeadTextures.CHLORONITE),
			new Crop("Do Not Eat Shroom", "donoteatshroom", 19, HeadTextures.DO_NOT_EAT_SHROOM),
			new Crop("Fleshtrap", "fleshtrap", 20, HeadTextures.FLESHTRAP),
			new Crop("Magic Jellybean", "magicjellybean", 21, HeadTextures.MAGIC_JELLYBEAN),
			new Crop("Noctilume", "noctilume", 22, HeadTextures.NOCTILUME),
			new Crop("Snoozling", "snoozling", 23, HeadTextures.SNOOZLING),
			new Crop("Soggybud", "soggybud", 24, HeadTextures.SOGGYBUD),
			new Crop("Chorus Fruit", "chorusfruit", 25, HeadTextures.CHORUS_FRUIT),
			new Crop("PlantBoy Advance", "plantboyadvance", 26, HeadTextures.PLANTBOY_ADVANCE),
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
			new Crop("Godseed", "godseed", 37, HeadTextures.GODSEED),
			new Crop("Jerryflower", "jerryflower", 38, HeadTextures.JERRYFLOWER),
			new Crop("Phantomleaf", "phantomleaf", 39, HeadTextures.PHANTOMLEAF),
			new Crop("Timestalk", "timestalk", 40, HeadTextures.TIMESTALK),

			// Crops
			new Crop("Cocoa Beans", "coco", 41, HeadTextures.COCOA_BEANS),
			new Crop("Wild Rose", "wildrose", 42, HeadTextures.WILD_ROSE),
			new Crop("Moonflower", "moonflower", 43, HeadTextures.MOONFLOWER),
			new Crop("Sunflower", "sunflower", 44, HeadTextures.SUNFLOWER),
			new Crop("Cactus", "cactus", 45, HeadTextures.CACTUS),
			new Crop("Melon Seeds", "melon", 46, HeadTextures.MELON),
			new Crop("Pumpkin Seeds", "pumpkin", 47, HeadTextures.PUMPKIN),
			new Crop("Brown Mushroom", "brownmushroom", 48, Blocks.BROWN_MUSHROOM),
			new Crop("Red Mushroom", "redmushroom", 49, Blocks.RED_MUSHROOM),
			new Crop("Wheat Seeds", "wheat", 50, Blocks.WHEAT),
			new Crop("Carrot", "carrot", 51, Blocks.CARROTS),

			new Crop("Potato", "potato", 52, Blocks.POTATOES),
			new Crop("Nether Wart", "netherwart", 53, Blocks.NETHER_WART),
			new Crop("Sugar Cane", "sugarcane", 54, Blocks.SUGAR_CANE),

			// Misc
			new Crop("Dead Plant", "deadplant", 55, Blocks.DEAD_BUSH),
			new Crop("Fire", "fire", 56, Blocks.DEAD_BUSH),
			new Crop("Fermento", "fermento", 57, HeadTextures.FERMENTO),
		};

		// Populate both maps
		for (Crop crop : crops) {
			CROP_ID_MAP.put(crop.name, crop);
			CROP_BY_INT.put(crop.id, crop);
			if (crop.isHead && HeadTextures.SPECIAL_CROPS.contains(crop.headSkin)){
				CROP_BY_HEAD_TEXTURE_HASH.put(crop.headSkin, crop);
			}
		}
	}

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("greenhousepaste").executes(context -> runGreenhousePaste()))
				.then(literal("greenhousepasteremove").executes(context -> runGreenhousePasteRemove()))
						.then(literal("greenhousedebug").executes(context -> {
							debugPrintGreenhouses();
							return Command.SINGLE_SUCCESS;
						}))));

		// Register render callback
		LevelRenderExtractionCallback.EVENT.register(collector -> {
			if (!Utils.isInGarden()) return;
			renderPreview(collector);
		});

		PlayerBlockBreakEvents.AFTER.register((_, _, pos, _, _) -> onBlockChange(pos));


		// Initialize greenhouse arrays
		removePreview();
	}

	// If armor stand crops are changed
	public static void onEntityChange(Entity e){
		if (greenhouseCorner == null) return;
		if (greenhouseCorner.getX() - 1 > e.getX() ||
				greenhouseCorner.getX() + 11 < e.getX() ||
				greenhouseCorner.getZ() - 1 > e.getZ() ||
				greenhouseCorner.getZ() + 11 < e.getZ()) return;
		locateGreenhouse();
	}

	// If non armor stand crops are changed
	public static void onBlockChange(BlockPos pos){
		if (greenhouseCorner == null) return;
		if (greenhouseCorner.getX() - 1 >= pos.getX() ||
				greenhouseCorner.getX() + 11 <= pos.getX() ||
				greenhouseCorner.getZ() - 1 >= pos.getZ() ||
				greenhouseCorner.getZ() + 11 <= pos.getZ()) return;
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
			String name = armorStand.getCustomName() != null ? armorStand.getCustomName().getString() : "n/a";
			for (Crop crop : CROP_ID_MAP.values()) {
				if (name.contains(crop.armorStandName)) {
					return crop.id;
				}
			}

			ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
			if (head.isEmpty()) continue;

			Optional<String> texture = ItemUtils.getHeadTextureOptional(head);
			if (texture.isEmpty()) continue;

			Crop cropByTexture = CROP_BY_HEAD_TEXTURE_HASH.get(texture);
			if (cropByTexture != null) {
				return cropByTexture.id;
			}
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

	/**
	 * Imports a greenhouse layout from an LZ-encoded string.
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
				int value = entry.get(3).getAsInt(); // 0 = desired, 1 = place, in the website
				if (value == 0) continue;
				if ("Dead Plants".equalsIgnoreCase(cropName) || "Dead Plant".equalsIgnoreCase(cropName)) continue;

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
		if (greenhouseCorner == null) return;

		for (int x = 0; x < 10; x++) {
			for (int y = 0; y < 10; y++) {
				// Ignore cell if -1
				int targetCropId = targetGreenhouse[x][y];
				if (targetCropId == -1) continue;

				// Already correct
				int currentCropId = greenhouse[x][y];
				if (currentCropId == targetCropId) continue;

				Crop targetCrop = CROP_BY_INT.get(targetCropId);
				if (targetCrop == null || targetCrop.headSkin == null || targetCrop.headSkin.isBlank()) continue;

				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, greenhouseCorner.getY() + 1, greenhouseCorner.getZ() + y);

				if (greenhouse[x][y] != 0) {
					OutlinedBoxRenderState state = new OutlinedBoxRenderState();
					state.minX = pos.getX();
					state.minY = pos.getY();
					state.minZ = pos.getZ();
					state.maxX = pos.getX() + 1;
					state.maxY = pos.getY() + 1;
					state.maxZ = pos.getZ() + 1;
					state.colourComponents = new float[]{1f, 0f, 0f}; // Red
					state.alpha = 0.5f;
					state.lineWidth = 2f;
					state.throughWalls = true;

					OutlinedBoxInstancedRenderer.INSTANCE.submitPrimitives(
							List.of(state),
							PrimitiveCollectorImpl.getWorldState(collector).cameraRenderState
					);
				} else if (targetCrop.isHead) {
					SkullRenderer.submitSkull(collector, pos, targetCrop.displayStack, PREVIEW_COLOR_ARGB);
				} else {
					BlockHologramRenderer.INSTANCE.submitPrimitives(new BlockHologramRenderState() {{
						this.pos = pos;
						this.state = targetCrop.cropBlock.defaultBlockState();
						this.alpha = PREVIEW_COLOR_ARGB;
						this.altModelBlockRenderer = null;
					 }}, PrimitiveCollectorImpl.getWorldState(collector).cameraRenderState);
				}
			}
		}
	}

	//	private record SkullPreviewState(BlockPos pos, FlexibleItemStack skull, int argb) {}

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

			client.player.sendSystemMessage(Component.literal(row.toString()));
		}
	}



}
