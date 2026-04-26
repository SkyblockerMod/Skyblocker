package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class GreenhousePaste {
	private static Minecraft client = Minecraft.getInstance();
	private static final int PREVIEW_COLOR_ARGB = 0x99FFFFFF;

	/*
		For normal greenhouse, the grid is 10x10. Each cell can be empty (0) or contain a crop (1-40).
		For target greenhouse, the grid is also 10x10. Each cell can be ignored (-1) or empty (0) contain a crop (1-40).
	*/
	private static final Map<String, Crop> CROP_ID_MAP = new HashMap<>();
	private static final Map<Integer, Crop> CROP_BY_INT = new HashMap<>();
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
			new Crop("Lonelily", "", 5, HeadTextures.LONELILY),
			new Crop("Scourroot", "", 6, HeadTextures.SCOURROOT),
			new Crop("Shadevine", "", 7, HeadTextures.SHADEVINE),
			new Crop("Veilshroom", "", 8, HeadTextures.VEILSHROOM),
			new Crop("Witherbloom", "", 9, HeadTextures.WITHERBLOOM),
			new Crop("Chocoberry", "", 10, HeadTextures.CHOCOBERRY),
			new Crop("Cindershade", "", 11, HeadTextures.CINDERSHADE),
			new Crop("Coalroot", "", 12, HeadTextures.COALROOT),
			new Crop("Creambloom", "", 13, HeadTextures.CREAMBLOOM),
			new Crop("Duskbloom", "", 14, HeadTextures.DUSKBLOOM),
			new Crop("Thornshade", "", 15, HeadTextures.THORNSHADE),
			new Crop("Blastberry", "", 16, HeadTextures.BLASTBERRY),
			new Crop("Cheesebite", "", 17, HeadTextures.CHEESEBITE),
			new Crop("Chloronite", "", 18, HeadTextures.CHLORONITE),
			new Crop("Do Not Eat Shroom", "donoteatshroom", 19, HeadTextures.DO_NOT_EAT_SHROOM),
			new Crop("Fleshtrap", "", 20, HeadTextures.FLESHTRAP),
			new Crop("Magic Jellybean", "", 21, HeadTextures.MAGIC_JELLYBEAN),
			new Crop("noctilume", "", 22, HeadTextures.NOCTILUME),
			new Crop("Snoozling", "", 23, HeadTextures.SNOOZLING),
			new Crop("Soggybud", "", 24, HeadTextures.SOGGYBUD),
			new Crop("Chorus Fruit", "", 25, HeadTextures.CHORUS_FRUIT),
			new Crop("PlantBoy Advance", "", 26, HeadTextures.PLANTBOY_ADVANCE),
			new Crop("Puffercloud", "", 27, HeadTextures.PUFFERCLOUD),
			new Crop("Shellfruit", "", 28, HeadTextures.SHELLFRUIT),
			new Crop("Startlevine", "", 29, HeadTextures.STARTLEVINE),
			new Crop("stoplightpetal", "", 30, HeadTextures.STOPLIGHT_PETAL),
			new Crop("Thunderling", "", 31, HeadTextures.THUNDERLING),
			new Crop("Turtlellini", "", 32, HeadTextures.TURTLELLINI),
			new Crop("Zombud", "", 33, HeadTextures.ZOMBUD),
			new Crop("All In Aloe", "", 34, HeadTextures.ALL_IN_ALOE),
			new Crop("Devourer", "", 35, HeadTextures.DEVOURER),
			new Crop("Glasscorn", "", 36, HeadTextures.GLASSCORN),
			new Crop("Godseed", "", 37, HeadTextures.GODSEED),
			new Crop("Jerryflower", "", 38, HeadTextures.JERRYFLOWER),
			new Crop("Phantomleaf", "", 39, HeadTextures.PHANTOMLEAF),
			new Crop("Timestalk", "", 40, HeadTextures.TIMESTALK),

			// Crops
			new Crop("Cocoa Beans", "coco", 41, HeadTextures.COCOA_BEANS),
			new Crop("Wild Rose", "wildrose", 42, HeadTextures.WILD_ROSE),
			new Crop("Moonflower", "moonflower", 43, HeadTextures.MOONFLOWER),
			new Crop("Sunflower", "sunflower", 44, HeadTextures.SUNFLOWER),
			new Crop("Cactus", "", 45, HeadTextures.CACTUS),
			new Crop("Brown Mushroom", "", 46, Blocks.BROWN_MUSHROOM),
			new Crop("Red Mushroom", "", 47, Blocks.RED_MUSHROOM),
			new Crop("Wheat Seeds", "", 48, Blocks.WHEAT),
			new Crop("Melon Seeds", "", 49, Blocks.MELON_STEM),
			new Crop("Fire", "", 50, Blocks.FIRE),
			new Crop("Carrot", "", 51, Blocks.CARROTS),
			new Crop("Pumpkin Seeds", "", 52, Blocks.PUMPKIN_STEM),
			new Crop("Potato", "", 53, Blocks.POTATOES),
			new Crop("Nether Wart", "", 54, Blocks.NETHER_WART),
			new Crop("Sugar Cane", "", 55, Blocks.SUGAR_CANE),
			new Crop("Dead Plant", "", 56, Blocks.DEAD_BUSH),
		};

		// Populate both maps
		for (Crop crop : crops) {
			CROP_ID_MAP.put(crop.name, crop);
			CROP_BY_INT.put(crop.id, crop);
		}
	}

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("greenhousepaste").executes(context -> runGreenhousePaste()))
				.then(literal("greenhousepasteremove").executes(context -> runGreenhousePasteRemove()))));

		// Register render callback
		LevelRenderExtractionCallback.EVENT.register(collector -> {
			if (!Utils.isInGarden()) return;
			renderPreview(collector);
		});

		// Initialize greenhouse arrays
		removePreview();
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
				pos.getX() + 1, pos.getY() + 5, pos.getZ() + 1
		);

		// Determine crop ID based on the name of armor stand, should also detect non head crops
		for (net.minecraft.world.entity.Entity entity : level.getEntities(null, detectionBox)) {
			if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
				String name = armorStand.getCustomName() != null ? armorStand.getCustomName().getString() : "";
				for (Crop crop : CROP_ID_MAP.values()) {
					if (name.contains(crop.name)) {
						return crop.id;
					}
				}
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
				SkullRenderer.submitSkull(collector, pos, targetCrop.displayStack, PREVIEW_COLOR_ARGB);
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
}
