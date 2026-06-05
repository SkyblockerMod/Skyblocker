package de.hysky.skyblocker.skyblock.garden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.item.HeadTextures;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.LZString;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class GreenhousePaste {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final float PREVIEW_ALPHA = 0.6f;
	private static final float PREVIEW_TINT = 0.4f;
	private static final long BLOCK_CHANGE_RATE_LIMIT_MS = 150;
	/*
		For normal greenhouse, the grid is 10x10. Each cell can be empty (0) or contain a crop (1-40).
		For target greenhouse, the grid is also 10x10. Each cell can be ignored (-1) or empty (0) contain a crop (1-40).
	*/
	private static int[][] greenhouse = new int[10][10];
	private static int[][] targetGreenhouse = new int[10][10];
	private static @Nullable BlockPos greenhouseCorner = null;

	private static long lastBlockChangeTimeMs;

	// Special ignores
	private static final Set<String> IGNORE_NAMES = Set.of(
			"PlantboyRoots",
			"godseedPillar"
	);

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> greenhouseCommands = literal("greenhouse")
					.then(literal("paste").executes(_ -> runGreenhousePaste()))
					.then(literal("endPaste").executes(_ -> runGreenhousePasteRemove()))
					.then(literal("rotate")
							.then(literal("right").executes(_ -> runRotateRight()))
							.then(literal("left").executes(_ -> runRotateLeft())))
					.then(literal("mirror").executes(_ -> runMirror()));

			if (Debug.debugEnabled()) {
				greenhouseCommands.then(literal("debug").executes(_ -> {
					debugPrintGreenhouses();
					return Command.SINGLE_SUCCESS;
				}));
			}

			dispatcher.register(literal(SkyblockerMod.NAMESPACE)
					.then(literal("garden")
							.then(greenhouseCommands)));
		});

		// Register render callback
		LevelRenderExtractionCallback.EVENT.register(collector -> {
			if (!Utils.isInGarden()) return;
			if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
			renderPreview(collector);
		});

		ClientPlayerBlockBreakEvents.AFTER.register((_, _, pos, _) -> onBlockChange(pos));

		WorldEvents.BLOCK_STATE_UPDATE.register((pos, _, _) -> onBlockChange(pos));
		// Initialize greenhouse arrays
		removePreview();
	}

	// If armor stand crops are changed
	public static void onEntityChange(Entity e) {
		if (!Utils.isInGarden()) return;
		if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
		if (greenhouseCorner == null) return;
		if (greenhouseCorner.getX() - 1 > e.getX() ||
				greenhouseCorner.getX() + 11 < e.getX() ||
				greenhouseCorner.getZ() - 1 > e.getZ() ||
				greenhouseCorner.getZ() + 11 < e.getZ()) return;
		locateGreenhouse();
	}

	// If non armor stand crops are changed
	private static void onBlockChange(BlockPos pos) {
		if (!Utils.isInGarden()) return;
		if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
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
		if (CLIENT.player == null) return Command.SINGLE_SUCCESS;
		loadFromLink();
		return Command.SINGLE_SUCCESS;
	}

	private static int runGreenhousePasteRemove() {
		if (CLIENT.player == null) return Command.SINGLE_SUCCESS;
		removePreview();
		CLIENT.player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePaste.unload").withStyle(ChatFormatting.GREEN)));
		return Command.SINGLE_SUCCESS;
	}

	private static int runRotateRight() {
		rotatePreview(false);
		return Command.SINGLE_SUCCESS;
	}

	private static int runRotateLeft() {
		rotatePreview(true);
		return Command.SINGLE_SUCCESS;
	}

	private static int runMirror() {
		mirrorPreview();
		return Command.SINGLE_SUCCESS;
	}

	public static void removePreview() {
		greenhouseCorner = null;
		for (int x = 0; x < 10; x++) {
			for (int z = 0; z < 10; z++) {
				greenhouse[x][z] = 0;
				targetGreenhouse[x][z] = -1;
			}
		}
	}

	public static void loadFromLink() {
		if (!SkyblockerConfigManager.get().farming.greenhouse.enabled) return;
		if (!isInGreenhouse()) return;
		String clipboard = CLIENT.keyboardHandler.getClipboard();
		String[] parts = clipboard.split("\\?layout=");
		String encoded = parts.length > 1 ? parts[1] : clipboard;

		if (encoded == null || encoded.isEmpty()) return;

		boolean success = importGreenhouse(encoded);
		if (!success) {
			CLIENT.player.sendSystemMessage(
					Constants.PREFIX.get()
							.append(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePaste.loadFail")
									.withStyle(ChatFormatting.RED))
							.append(Component.literal(encoded)
									.withStyle(ChatFormatting.GRAY))
			);
			return;
		}

		CLIENT.player.sendSystemMessage(
				Constants.PREFIX.get()
						.append(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePaste.loadSuccess")
								.withStyle(ChatFormatting.GREEN)));

		locateGreenhouse();
	}

	public static boolean isInGreenhouse() {
		BlockPos playerPos = CLIENT.player.blockPosition();
		BlockPos plotPos = playerPos.offset(240, 0, 240);

		plotPos = new BlockPos(
				((int) (plotPos.getX() / 96.0) * 96) - 240,
				60,
				((int) (plotPos.getZ() / 96.0) * 96) - 240
		);

		BlockPos plotOtherCorner = new BlockPos(plotPos.getX() + 96, 90, plotPos.getZ() + 96);

		AABB detectionBox = new AABB(
				plotPos.getX(), plotPos.getY(), plotPos.getZ(),
				plotOtherCorner.getX(), plotOtherCorner.getY(), plotOtherCorner.getZ()
		);

		for (Entity entity : CLIENT.level.getEntities(null, detectionBox)) {
			Component custom = entity.getCustomName();

			if (custom != null && custom.getString().contains("Carpenter")) {
				return true;
			}
		}

		CLIENT.player.sendSystemMessage(
				Constants.PREFIX.get()
						.append(Component.translatable("skyblocker.config.farming.greenhouse.greenhousePaste.notInGreenhouse").withStyle(ChatFormatting.RED))
		);
		return false;
	}

	// Get info of current greenhouse
	public static void locateGreenhouse() {
		BlockPos playerPos = CLIENT.player.blockPosition();
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
		for (int x = 0; x < 10; x++) {
			for (int z = 0; z < 10; z++) {
				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, 73, greenhouseCorner.getZ() + z);
				int cropId = getCropIdAtPosition(CLIENT.level, pos);
				greenhouse[x][z] = cropId;
			}
		}
		adjustForSpecialCrops();
	}

	private static int getCropIdAtPosition(Level level, BlockPos pos) {
		// Scan a 1x5x1 column
		AABB detectionBox = new AABB(
				pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 6, pos.getZ() + 1
		);

		// Determine crop ID based on the name of armor stand, should also detect non head crops
		for (Entity entity : level.getEntities(null, detectionBox)) {
			if (!(entity instanceof ArmorStand armorStand)) continue;

			ItemStack head = armorStand.getItemBySlot(EquipmentSlot.HEAD);
			if (head.isEmpty()) continue;

			Component nameComponent = head.getHoverName();
			String name = nameComponent.getString();

			if (IGNORE_NAMES.contains(name)) return 0; // Make blocks are ignored too

			for (GreenhouseCrops.Crop crop : GreenhouseCrops.CROP_ID_MAP.values()) {
				if (name.contains(crop.armorStandName())) {
					return crop.id();
				}
			}

			// Detection by texture for the special crops
			Optional<String> texture = ItemUtils.getHeadTextureOptional(head);
			if (texture.isEmpty()) continue;

			GreenhouseCrops.Crop cropByTexture = GreenhouseCrops.CROP_BY_HEAD_TEXTURE_HASH.get(texture.get());
			if (cropByTexture == null) continue;
			if (!HeadTextures.SPECIAL_CROPS.contains(cropByTexture.headSkin())) continue;

			return cropByTexture.id();
		}

		// If no armor stand found, fallback to checking block type (for non-head crops)
		BlockState state = level.getBlockState(pos.above());
		Block block = state.getBlock();
		for (GreenhouseCrops.Crop crop : GreenhouseCrops.CROP_ID_MAP.values()) {
			if (!crop.isHead() && crop.cropBlock().equals(block)) {
				return crop.id();
			}
		}

		return 0;
	}

	private static void adjustForSpecialCrops() {
		// Special handling for Plantboy (2x2 crop)
		for (int x = 0; x < 9; x++) {
			for (int z = 0; z < 9; z++) {
				adjustForPlantBoy(x, z);
				adjustForSnoozling(x, z);
				adjustForGodseed(x, z);
			}
		}
	}

	private static void adjustForPlantBoy(int x, int z) {
		if (greenhouse[x][z] != 26) return;

		BlockPos pos = greenhouseCorner.offset(x, 0, z);
		AABB detectionBox = new AABB(
				pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 6, pos.getZ() + 1
		);

		ArmorStand foundArmorStand = null;
		// Determine crop ID based on the name of armor stand, should also detect non head crops
		for (Entity entity : CLIENT.level.getEntities(null, detectionBox)) {
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

		// Strategy: since the plantboy is in the middle of the 2x2, we offset by a bit to get the bottom left corner
		foundArmorStand.position().add(-0.2, 0, -0.2);

		BlockPos bottomLeft = new BlockPos((int) Math.floor(foundArmorStand.getX()), (int) Math.floor(foundArmorStand.getY()), (int) Math.floor(foundArmorStand.getZ()));

		greenhouse[bottomLeft.getX() - greenhouseCorner.getX()][bottomLeft.getZ() - greenhouseCorner.getZ()] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX() + 1][bottomLeft.getZ() - greenhouseCorner.getZ()] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX()][bottomLeft.getZ() - greenhouseCorner.getZ() + 1] = 26;
		greenhouse[bottomLeft.getX() - greenhouseCorner.getX() + 1][bottomLeft.getZ() - greenhouseCorner.getZ() + 1] = 26;
	}

	private static void adjustForSnoozling(int x, int z) {
		if (greenhouse[x][z] != 23) return;
		try {
			greenhouse[x-1][z] = 23;
			greenhouse[x+1][z] = 23;

			greenhouse[x-1][z-1] = 23;
			greenhouse[x][z-1] = 23;
			greenhouse[x+1][z-1] = 23;

			greenhouse[x-1][z-2] = 23;
			greenhouse[x][z-2] = 23;
			greenhouse[x+1][z-2] = 23;
		}
		catch (ArrayIndexOutOfBoundsException _) { }
	}

	private static void adjustForGodseed(int x, int z) {
		if (greenhouse[x][z] != 37) return;
		try {
			greenhouse[x-1][z] = 37;
			greenhouse[x+1][z] = 37;

			greenhouse[x-1][z-1] = 37;
			greenhouse[x][z-1] = 37;
			greenhouse[x+1][z-1] = 37;

			greenhouse[x-1][z+1] = 37;
			greenhouse[x][z+1] = 37;
			greenhouse[x+1][z+1] = 37;
		}
		catch (ArrayIndexOutOfBoundsException _) { }
	}

	/**
	 * Imports the greenhouse layout from an LZ-encoded string.
	 * The string is decompressed and parsed as a JSON array.
	 * Each entry is [x, z, "crop_name", value].
	 */
	public static boolean importGreenhouse(String encoded) {
		try {
			String jsonString = LZString.decompressFromEncodedURIComponent(encoded);
			JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();

			for (int x = 0; x < 10; x++) {
				for (int z = 0; z < 10; z++) {
					targetGreenhouse[x][z] = -1;
				}
			}

			// Populate greenhouse from JSON
			for (JsonElement element : jsonArray) {
				JsonArray entry = element.getAsJsonArray();
				int x = entry.get(0).getAsInt();
				int z = entry.get(1).getAsInt();
				String cropName = entry.get(2).getAsString();
				int value = entry.get(3).getAsInt(); // 0 = desired mutation, 1 = place, according to the website
				if (value == 0) {
					targetGreenhouse[9 - x][z] = 0; // Desired mutation spot should be empty
					continue;
				}

				GreenhouseCrops.Crop crop = GreenhouseCrops.CROP_ID_MAP.get(cropName);
				if (crop == null) continue;
				if (x >= 0 && x < 10 && z >= 0 && z < 10) {
					targetGreenhouse[9 - x][z] = crop.id();
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
		// Only render if player is within greenhouse plot
		if (CLIENT.player.getX() < greenhouseCorner.getX() - 43 || CLIENT.player.getX() > greenhouseCorner.getX() + 53 ||
				CLIENT.player.getZ() < greenhouseCorner.getZ() - 43 || CLIENT.player.getZ() > greenhouseCorner.getZ() + 53) {
			return;
		}

		for (int x = 0; x < 10; x++) {
			for (int z = 0; z < 10; z++) {
				// Ignore cell if -1
				int targetCropId = targetGreenhouse[x][z];
				if (targetCropId == -1) continue;

				int currentCropId = greenhouse[x][z];
				BlockPos pos = new BlockPos(greenhouseCorner.getX() + x, greenhouseCorner.getY() + 1, greenhouseCorner.getZ() + z);

				// Mutation spots target empty blocks; render indicator and skip crop lookup.
				if (targetCropId == 0) {
					if (currentCropId == 0) {
						if (SkyblockerConfigManager.get().farming.greenhouse.showMutationSlot) // oh my days
							collector.submitOutlinedBox(
									new AABB(
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
								new AABB(pos),
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

				GreenhouseCrops.Crop targetCrop = GreenhouseCrops.CROP_BY_INT.get(targetCropId);
				if (targetCrop == null) continue;

				if (currentCropId != 0) { // Undesired spot that is not empty
					collector.submitOutlinedBox(new AABB(pos), new float[]{1f, 0f, 0f}, 0.5f, 4f, true);
				} else if (targetCrop.isHead()) {
					ItemStack stack = targetCrop.displayStack().getStack();
					ResolvableProfile profile = stack.get(DataComponents.PROFILE);
					if (profile == null) continue;

					SkullModelBase model = SkullBlockRenderer.createModel(
							CLIENT.getEntityModels(),
							SkullBlock.Types.PLAYER
					);
					if (model == null) continue;

					RenderType renderType = CLIENT.playerSkinRenderCache()
							.getOrDefault(profile)
							.renderType();

					collector.submitVanilla(
							null,
							(_, worldState, submitNodeCollector) -> {
								PoseStack matrices = new PoseStack();

								matrices.translate(
										pos.getX() - worldState.cameraRenderState.pos.x,
										pos.getY() - worldState.cameraRenderState.pos.y,
										pos.getZ() - worldState.cameraRenderState.pos.z
								);

								matrices.mulPose(SkullBlockRenderer.TRANSFORMATIONS.freeTransformations(0));

								SkullModelBase.State skullState = new SkullModelBase.State();
								skullState.animationPos = 0.0f;
								model.setupAnim(skullState);

								submitNodeCollector.submitModel(
										model,
										skullState,
										matrices,
										renderType,
										LightCoordsUtil.FULL_BRIGHT,
										OverlayTexture.pack(PREVIEW_TINT, false),
										0,
										null
								);
							}
					);
				} else {
					BlockState blockState = targetCrop.cropBlock().defaultBlockState();
					if (targetCrop.cropBlock() instanceof CropBlock) {
						blockState = targetCrop.cropBlock().defaultBlockState().setValue(CropBlock.AGE, 7);
					}
					collector.submitBlockHologram(pos, blockState, PREVIEW_ALPHA);
				}
			}
		}
	}

	private record HeadPreviewState(BlockPos pos, ResolvableProfile profile) {}

	public static void rotatePreview(boolean left) {
		/*
			x,z -> z, 9-x (left rotation, since an axis is reversed)
			x,z -> 9-z, x (right rotation)
		*/
		if (left) {
			int[][] newTarget = new int[10][10];
			for (int x = 0; x < 10; x++) {
				for (int z = 0; z < 10; z++) {
					newTarget[z][9 - x] = targetGreenhouse[x][z];
				}
			}
			targetGreenhouse = newTarget;
			return;
		}

		int[][] newTarget = new int[10][10];
		for (int x = 0; x < 10; x++) {
			for (int z = 0; z < 10; z++) {
				newTarget[9 - z][x] = targetGreenhouse[x][z];
			}
		}
		targetGreenhouse = newTarget;
	}

	public static void mirrorPreview() {
		// x,z -> 9-x, z
		int[][] newTarget = new int[10][10];
		for (int x = 0; x < 10; x++) {
			for (int z = 0; z < 10; z++) {
				newTarget[9 - x][z] = targetGreenhouse[x][z];
			}
		}
		targetGreenhouse = newTarget;
	}

	// DEBUG

	private static void debugPrintGreenhouses() {
		if (CLIENT.player == null) return;

		CLIENT.player.sendSystemMessage(Component.literal("=== CURRENT GREENHOUSE ===").withStyle(ChatFormatting.YELLOW));
		printGrid(greenhouse);

		CLIENT.player.sendSystemMessage(Component.literal("=== TARGET GREENHOUSE ===").withStyle(ChatFormatting.AQUA));
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
		CLIENT.player.sendSystemMessage(Component.literal(all.toString()));
	}

}
