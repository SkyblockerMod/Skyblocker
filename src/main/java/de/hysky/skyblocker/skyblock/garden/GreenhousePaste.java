package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class GreenhousePaste {
	private static Minecraft client = Minecraft.getInstance();
	private static SkullModelBase PLAYER_SKULL_MODEL;

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
			new Crop("Ashwreath", 1, HeadTextures.ASHWREATH),
			new Crop("Choconut", 2, HeadTextures.CHOCONUT),
			new Crop("Dustgrain", 3, HeadTextures.DUSTGRAIN),
			new Crop("Gloomgourd", 4, HeadTextures.GLOOMGOURD),
			new Crop("Lonelily", 5, HeadTextures.LONELILY),
			new Crop("Scourroot", 6, HeadTextures.SCOURROOT),
			new Crop("Shadevine", 7, HeadTextures.SHADEVINE),
			new Crop("Veilshroom", 8, HeadTextures.VEILSHROOM),
			new Crop("Witherbloom", 9, HeadTextures.WITHERBLOOM),
			new Crop("Chocoberry", 10, HeadTextures.CHOCOBERRY),
			new Crop("Cindershade", 11, HeadTextures.CINDERSHADE),
			new Crop("Coalroot", 12, HeadTextures.COALROOT),
			new Crop("Creambloom", 13, HeadTextures.CREAMBLOOM),
			new Crop("Duskbloom", 14, HeadTextures.DUSKBLOOM),
			new Crop("Thornshade", 15, HeadTextures.THORNSHADE),
			new Crop("Blastberry", 16, HeadTextures.BLASTBERRY),
			new Crop("Cheesebite", 17, HeadTextures.CHEESEBITE),
			new Crop("Chloronite", 18, HeadTextures.CHLORONITE),
			new Crop("Do Not Eat Shroom", 19, HeadTextures.DO_NOT_EAT_SHROOM),
			new Crop("Fleshtrap", 20, HeadTextures.FLESHTRAP),
			new Crop("Magic Jellybean", 21, HeadTextures.MAGIC_JELLYBEAN),
			new Crop("Noctilume", 22, HeadTextures.NOCTILUME),
			new Crop("Snoozling", 23, HeadTextures.SNOOZLING),
			new Crop("Soggybud", 24, HeadTextures.SOGGYBUD),
			new Crop("Chorus Fruit", 25, HeadTextures.CHORUS_FRUIT),
			new Crop("Plantboy Advance", 26, HeadTextures.PLANTBOY_ADVANCE),
			new Crop("Puffercloud", 27, HeadTextures.PUFFERCLOUD),
			new Crop("Shellfruit", 28, HeadTextures.SHELLFRUIT),
			new Crop("Startlevine", 29, HeadTextures.STARTLEVINE),
			new Crop("Stoplight Petal", 30, HeadTextures.STOPLIGHT_PETAL),
			new Crop("Thunderling", 31, HeadTextures.THUNDERLING),
			new Crop("Turtlellini", 32, HeadTextures.TURTLELLINI),
			new Crop("Zombud", 33, HeadTextures.ZOMBUD),
			new Crop("All In Aloe", 34, HeadTextures.ALL_IN_ALOE),
			new Crop("Devourer", 35, HeadTextures.DEVOURER),
			new Crop("Glasscorn", 36, HeadTextures.GLASSCORN),
			new Crop("Godseed", 37, HeadTextures.GODSEED),
			new Crop("Jerryflower", 38, HeadTextures.JERRYFLOWER),
			new Crop("Phantomleaf", 39, HeadTextures.PHANTOMLEAF),
			new Crop("Timestalk", 40, HeadTextures.TIMESTALK)
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

		// determine crop ID based on the name of armor stand
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

		return 0;
	}

	/**
	 * Imports a greenhouse layout from an LZ-encoded string.
	 * The string is decompressed and parsed as a JSON array.
	 * Each entry is [x, y, "crop_name", value].
	 */
	public static boolean importGreenhouse(String encoded) {
		try {
			String jsonString = decodeLZString(encoded);

			// Parse JSON array
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
				int value = entry.get(3).getAsInt();

				// Use crop ID from map, or use the provided value as fallback
				Crop crop = CROP_ID_MAP.get(cropName);
				int cropId = crop != null ? crop.id : value;

				if (x >= 0 && x < 10 && y >= 0 && y < 10) {
					targetGreenhouse[x][y] = cropId;
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

				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, greenhouseCorner.getY(), greenhouseCorner.getZ() + y);
				collector.submitVanilla(new SkullPreviewState(pos, targetCrop.displayStack), GreenhousePaste::renderSkullPreview);
			}
		}
	}

	private static void renderSkullPreview(
			SkullPreviewState state,
			LevelRenderState levelState,
			SubmitNodeCollector submitNodeCollector
	) {
		ItemStack stack = state.skull().getStack();
		if (stack == null || stack.isEmpty()) return;

		ResolvableProfile profile = stack.get(DataComponents.PROFILE);
		if (profile == null) return;

		SkullModelBase model = getPlayerSkullModel();
		if (model == null) return;

		RenderType renderType = Minecraft.getInstance()
				.playerSkinRenderCache()
				.getOrDefault(profile)
				.renderType();
		if (renderType == null) return;

		PoseStack matrices = new PoseStack();
		matrices.pushPose();

		BlockPos pos = state.pos();

		matrices.translate(
				pos.getX() - levelState.cameraRenderState.pos.x,
				pos.getY() + 1.0 - levelState.cameraRenderState.pos.y,
				pos.getZ() - levelState.cameraRenderState.pos.z
		);

		matrices.mulPose(SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(0));

		SkullBlockRenderer.submitSkull(
				0.0f,
				matrices,
				submitNodeCollector,
				LightCoordsUtil.FULL_BRIGHT,
				model,
				renderType,
				0,
				null
		);

		matrices.popPose();
	}
	// Lazy loads player skull model
	private static SkullModelBase getPlayerSkullModel() {
		if (PLAYER_SKULL_MODEL == null) {
			Minecraft client = Minecraft.getInstance();
			if (client.getEntityModels() == null) return null;
			PLAYER_SKULL_MODEL = SkullBlockRenderer.createModel(
					client.getEntityModels(),
					SkullBlock.Types.PLAYER
			);
		}
		return PLAYER_SKULL_MODEL;
	}

	// ts is so fried
	private static String decodeLZString(String input) {
		if (input == null || input.isEmpty()) return "";

		final String keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-$";

		class Data {
			int val;
			int position = 32;
			int index = 1;

			Data(String input) {
				this.val = keyStr.indexOf(input.charAt(0));
			}

			int readBit(String input) {
				int resb = val & position;
				position >>= 1;

				if (position == 0) {
					position = 32;
					if (index < input.length()) {
						val = keyStr.indexOf(input.charAt(index++));
					}
				}

				return resb > 0 ? 1 : 0;
			}

			int readBits(String input, int numBits) {
				int bits = 0;
				int maxpower = 1 << numBits;
				int power = 1;

				while (power != maxpower) {
					bits |= readBit(input) * power;
					power <<= 1;
				}

				return bits;
			}
		}

		Data data = new Data(input);

		List<String> dictionary = new ArrayList<>();
		for (int i = 0; i < 3; i++) dictionary.add("");

		int enlargeIn = 4;
		int dictSize = 4;
		int numBits = 3;

		int next = data.readBits(input, 2);
		String c;

		switch (next) {
			case 0 -> c = String.valueOf((char) data.readBits(input, 8));
			case 1 -> c = String.valueOf((char) data.readBits(input, 16));
			case 2 -> {return "";}
			default -> throw new IllegalStateException("Invalid LZ string");
		}

		dictionary.add(c);
		String w = c;
		StringBuilder result = new StringBuilder(c);

		while (true) {
			if (data.index > input.length()) return result.toString();

			int cc = data.readBits(input, numBits);
			String entry;

			switch (cc) {
				case 0 -> {
					dictionary.add(String.valueOf((char) data.readBits(input, 8)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 1 -> {
					dictionary.add(String.valueOf((char) data.readBits(input, 16)));
					cc = dictSize++;
					enlargeIn--;
				}
				case 2 -> {
					return result.toString();
				}
			}

			if (enlargeIn == 0) {
				enlargeIn = 1 << numBits;
				numBits++;
			}

			if (cc < dictionary.size() && dictionary.get(cc) != null) {
				entry = dictionary.get(cc);
			} else if (cc == dictSize) {
				entry = w + w.charAt(0);
			} else {
				throw new IllegalStateException("Bad compressed code: " + cc);
			}

			result.append(entry);

			dictionary.add(w + entry.charAt(0));
			dictSize++;
			enlargeIn--;

			w = entry;

			if (enlargeIn == 0) {
				enlargeIn = 1 << numBits;
				numBits++;
			}
		}
	}

	private record SkullPreviewState(BlockPos pos, FlexibleItemStack skull) {}

	public static class Crop {
		private final String name;
		private final int id;
		private final String headSkin;
		private final FlexibleItemStack displayStack;

		public Crop(String name, int id, String headSkin) {
			this.name = name;
			this.id = id;
			this.headSkin = headSkin;
			this.displayStack = ItemUtils.createSkull(headSkin);
		}
	}
}
