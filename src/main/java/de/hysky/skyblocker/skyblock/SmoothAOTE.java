package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothAOTE {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private static final Pattern MANA_LORE = Pattern.compile("Mana Cost: (\\d+)");
	private static final long MAX_TELEPORT_TIME = 2500; //2.5 seconds

	private static long startTime;
	private static Vec3d startPos;
	private static Vec3d cameraStartPos;
	private static Vec3d teleportVector;
	private static long lastPing;
	private static long currentTeleportPing;
	private static int teleportsAhead;
	private static long lastTeleportTime;
	public static boolean teleportDisabled;

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(SmoothAOTE::onItemInteract);
		UseBlockCallback.EVENT.register(SmoothAOTE::onBlockInteract);
	}

	/**
	 * When a player receives a teleport packet finish a teleport
	 */
	public static void playerTeleported() {
		//the player has been teleported so 1 less teleport ahead
		teleportsAhead = Math.max(0, teleportsAhead - 1);
		//re-enable the animation if the player is teleported as this means they can teleport again. and reset timer for last teleport update
		lastTeleportTime = System.currentTimeMillis();
		teleportDisabled = false;

		//if the server is in sync in number of teleports
		if (teleportsAhead == 0) {
			//see if the teleport has a small amount left to continue animating instead of jumping to the end
			long timeLeft = (currentTeleportPing - (System.currentTimeMillis() - startTime));
			if (timeLeft > 0 && timeLeft <= SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag) {
				return;
			}
			//reset when player has reached the end of the teleports
			startPos = null;
			teleportVector = null;

		}
	}

	/**
	 * checks to see if a teleport device is using transmission tuner to increase the range
	 *
	 * @param customData the custom data of the teleport device
	 * @param baseRange  the base range for the device without tuner
	 * @return the range with tuner
	 */
	private static int extractTunedCustomData(NbtCompound customData, int baseRange) {
		return customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getInt("tuned_transmission", 0) : baseRange;
	}

	/**
	 * When an item is right-clicked send off to calculate teleport with the clicked item
	 *
	 * @param playerEntity player
	 * @param world        world
	 * @param hand         held item
	 * @return pass
	 */
	private static ActionResult onItemInteract(PlayerEntity playerEntity, World world, Hand hand) {
		if (CLIENT.player == null) {
			return null;
		}
		calculateTeleportUse(hand);
		return ActionResult.PASS;
	}

	/**
	 * Allows shovel teleport items to be used when aiming at interactable blocks
	 *
	 * @param playerEntity   player
	 * @param world          world
	 * @param hand           hand item
	 * @param blockHitResult target block
	 * @return always pass
	 */
	private static ActionResult onBlockInteract(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
		ItemStack itemStack = playerEntity.getStackInHand(hand);
		if (isShovel(itemStack) && canShovelActOnBlock(world.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
			calculateTeleportUse(hand);
		}
		return ActionResult.PASS;
	}

	private static boolean isShovel(ItemStack itemStack) {
		return itemStack.isOf(Items.WOODEN_SHOVEL) ||
				itemStack.isOf(Items.STONE_SHOVEL) ||
				itemStack.isOf(Items.IRON_SHOVEL) ||
				itemStack.isOf(Items.GOLDEN_SHOVEL) ||
				itemStack.isOf(Items.DIAMOND_SHOVEL);
	}

	/**
	 * Checks if the block is one that the shovel can turn into a path (e.g., grass or dirt)
	 *
	 * @param block block to check
	 * @return if block can be turned into path
	 */
	private static boolean canShovelActOnBlock(Block block) {
		return block == Blocks.GRASS_BLOCK ||
				block == Blocks.DIRT ||
				block == Blocks.COARSE_DIRT ||
				block == Blocks.PODZOL;
	}

	/**
	 * Finds if a player uses a teleport and then saves the start position and time. then works out final position and saves that too
	 *
	 * @param hand what the player is holding
	 */

	private static void calculateTeleportUse(Hand hand) {
		//stop checking if player does not exist
		if (CLIENT.player == null || CLIENT.world == null) {
			return;
		}
		//get return item
		ItemStack stack = CLIENT.player.getStackInHand(hand);

		//make sure it's not disabled
		if (teleportDisabled) {
			return;
		}

		// make sure the camera is not in 3rd person
		if (CLIENT.options.getPerspective() != Perspective.FIRST_PERSON) {
			return;
		}

		//make sure the player is in an area teleporting is allowed not allowed in glacite mineshafts and floor 7 boss
		if (!isAllowedLocation()) {
			return;
		}

		//work out if the player is holding a teleporting item that is enabled and if so how far the item will take them
		ItemStack heldItem = CLIENT.player.getMainHandStack();
		String itemId = heldItem.getSkyblockId();
		NbtCompound customData = ItemUtils.getCustomData(heldItem);

		int distance;
		switch (itemId) {
			case "ASPECT_OF_THE_LEECH_1" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
					distance = 3;
					break;
				}
				return;

			}
			case "ASPECT_OF_THE_LEECH_2" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
					distance = 4;
					break;
				}
				return;
			}
			case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
				if (CLIENT.options.sneakKey.isPressed() && customData.getInt("ethermerge", 0) == 1) {
					if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
						distance = extractTunedCustomData(customData, 57);
						break;
					}
				} else if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableInstantTransmission) {
					distance = extractTunedCustomData(customData, 8);
					break;
				}
				return;
			}
			case "ETHERWARP_CONDUIT" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
					distance = extractTunedCustomData(customData, 57);
					break;
				}
				return;
			}
			case "SINSEEKER_SCYTHE" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableSinrecallTransmission) {
					distance = extractTunedCustomData(customData, 4);
					break;
				}
				return;
			}
			case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWitherImpact) {
					distance = 10;
					break;
				}
				return;
			}
			default -> {
				return;
			}
		}
		//make sure the player has enough mana to do the teleport
		Matcher manaNeeded = ItemUtils.getLoreLineIfMatch(heldItem, MANA_LORE);
		if (manaNeeded != null && manaNeeded.matches()) {
			int manaCost = Integer.parseInt(manaNeeded.group(1));
			int predictedMana = StatusBarTracker.getMana().value() - teleportsAhead * manaCost;
			if (predictedMana < manaCost) { // todo the players mana can lag behind as it is updated server side. client side mana calculations would help with this
				return;
			}
		}

		//work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
		if (teleportsAhead == 0 || startPos == null || teleportVector == null) {
			//start of teleport sequence
			startPos = CLIENT.player.getPos().add(0, 1.62, 0); // the eye poss should not be affected by crouching
			cameraStartPos = CLIENT.player.getEyePos();
			lastTeleportTime = System.currentTimeMillis();
			// update the ping used for the teleport
			currentTeleportPing = lastPing;
		} else {
			//add to the end of the teleport sequence
			startPos = startPos.add(teleportVector);
			//set the camera start pos to how far though the teleport the player is to make is smoother
			cameraStartPos = getInterpolatedPos();
			//update the ping used for this part of the teleport
			currentTeleportPing = lastPing;
		}

		startTime = System.currentTimeMillis();

		// calculate the vector the player will follow for the teleport
		//get direction
		float pitch = CLIENT.player.getPitch();
		float yaw = CLIENT.player.getYaw();
		Vec3d look = CLIENT.player.getRotationVector(pitch, yaw);

		//find target location depending on how far the item they are using takes them
		teleportVector = raycast(distance, look, startPos);
		if (teleportVector == null) {
			startPos = null;
			return;
		}

		//compensate for hypixel round to center of block (to x.5 y.62 z.5)
		Vec3d predictedEnd = startPos.add(teleportVector);
		Vec3d offsetVec = new Vec3d(predictedEnd.x - roundToCenter(predictedEnd.x), predictedEnd.y - (Math.ceil(predictedEnd.y) + 0.62), predictedEnd.z - roundToCenter(predictedEnd.z));
		teleportVector = teleportVector.subtract(offsetVec);
		//add 1 to teleports ahead
		teleportsAhead += 1;
	}

	/**
	 * Rounds a value to the nearest 0.5
	 *
	 * @param input number to round
	 * @return rounded number
	 */
	private static double roundToCenter(double input) {
		return Math.round(input - 0.5) + 0.5;
	}

	/**
	 * Works out if the players location lets them use teleportation or not
	 *
	 * @return if the player should be allowed to teleport
	 */
	private static boolean isAllowedLocation() {
		//check mines shafts
		if (Utils.getMap().equals("Mineshaft")) {
			return false;
		} else if (Utils.getIslandArea().equals("⏣ Jungle Temple")) { //do not allow in jungle temple
			return false;
		} else if (Utils.getLocation() == Location.PRIVATE_ISLAND && !Utils.getIslandArea().equals("⏣ Your Island")) { //do not allow it when visiting
			return false;
		} else if (Utils.isInDungeons()) { //check places in dungeons where you can't teleport
			if (DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR) {
				return false;
			}
			//make sure the player is in a room then check for disallowed rooms
			if (!DungeonManager.isCurrentRoomMatched()) {
				return true;
			}
			//does not work in boulder room
			if (DungeonManager.getCurrentRoom().getName().equals("boxes-room")) {
				return false;
			}
			//does not work in teleport maze room
			if (DungeonManager.getCurrentRoom().getName().equals("teleport-pad-room")) {
				return false;
			}
			//does not work in trap room
			if (DungeonManager.getCurrentRoom().getName().startsWith("trap")) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Custom raycast for teleporting checks for blocks for each 1 block forward in teleport. (very similar to hypixels method)
	 *
	 * @param distance maximum distance
	 * @return teleport vector
	 */
	private static Vec3d raycast(int distance, Vec3d direction, Vec3d startPos) {
		if (CLIENT.world == null || direction == null || startPos == null) {
			return null;
		}

		//based on which way the ray is going get the needed vector for checking diagonals
		BlockPos xDiagonalOffset;
		BlockPos zDiagonalOffset;
		if (direction.getX() > 0) {
			xDiagonalOffset = new BlockPos(-1, 0, 0);
		} else {
			xDiagonalOffset = new BlockPos(1, 0, 0);
		}
		if (direction.getZ() > 0) {
			zDiagonalOffset = new BlockPos(0, 0, -1);
		} else {
			zDiagonalOffset = new BlockPos(0, 0, 1);
		}

		//initialise the closest floor value outside of possible values
		int closeFloorY = 1000;

		//loop though each block of a teleport checking each block if there are blocks in the way
		for (double offset = 0; offset <= distance; offset++) {
			Vec3d pos = startPos.add(direction.multiply(offset));
			BlockPos checkPos = BlockPos.ofFloored(pos);

			//check if there is a block at the check location
			if (!canTeleportThrough(checkPos)) {
				if (offset == 0) {
					// no teleport can happen
					return null;
				}
				return direction.multiply(offset - 1);
			}

			//check if the block at head height is free
			if (!canTeleportThrough(checkPos.up())) {
				if (offset == 0) {
					//cancel the check if starting height is too low
					Vec3d justAhead = startPos.add(direction.multiply(0.2));
					if ((justAhead.getY() - Math.floor(justAhead.getY())) <= 0.495) {
						continue;
					}
					// no teleport can happen
					return null;
				}
				return direction.multiply(offset - 1);
			}

			//check the diagonals to make sure player is not going through diagonal wall (full height block in the way on both sides at either height)
			if (offset != 0 && (isBlockFloor(checkPos.add(xDiagonalOffset)) || isBlockFloor(checkPos.up().add(xDiagonalOffset))) && (isBlockFloor(checkPos.add(zDiagonalOffset)) || isBlockFloor(checkPos.up().add(zDiagonalOffset)))) {
				return direction.multiply(offset - 1);
			}

			//if the player is close to the floor (including diagonally) save Y and when player goes bellow this y finish teleport
			if (offset != 0 && (isBlockFloor(checkPos.down()) || (isBlockFloor(checkPos.down().subtract(xDiagonalOffset)) && isBlockFloor(checkPos.down().subtract(zDiagonalOffset)))) && (pos.getY() - Math.floor(pos.getY())) < 0.31) {
				closeFloorY = checkPos.getY() - 1;
			}

			//if the checking Y is same as closeY finish
			if (closeFloorY == checkPos.getY()) {
				return direction.multiply(offset - 1);
			}
		}

		//return full distance if no collision found
		return direction.multiply(distance);
	}

	/**
	 * Checks to see if a block is in the allowed list to teleport though
	 * Air, Buttons, carpets, crops, pots, mushrooms, nether wart, redstone, ladder, water, fire, lava, 3 or less snow layers
	 *
	 * @param blockPos block location
	 * @return if a block location can be teleported though
	 */
	private static Boolean canTeleportThrough(BlockPos blockPos) {
		if (CLIENT.world == null) {
			return false;
		}

		BlockState blockState = CLIENT.world.getBlockState(blockPos);
		if (blockState.isAir()) {
			return true;
		}
		Block block = blockState.getBlock();
		return block instanceof ButtonBlock || block instanceof CarpetBlock || block instanceof CropBlock || block instanceof FlowerPotBlock || block.equals(Blocks.BROWN_MUSHROOM) || block.equals(Blocks.RED_MUSHROOM) || block.equals(Blocks.NETHER_WART) || block.equals(Blocks.REDSTONE_WIRE) || block.equals(Blocks.LADDER) || block.equals(Blocks.FIRE) || (block.equals(Blocks.SNOW) && blockState.get(Properties.LAYERS) <= 3) || block.equals(Blocks.WATER) || block.equals(Blocks.LAVA);
	}

	/**
	 * Checks to see if a block goes to the top if so class it as a floor
	 *
	 * @param blockPos block location
	 * @return if it's a floor block
	 */
	private static Boolean isBlockFloor(BlockPos blockPos) {
		if (CLIENT.world == null) {
			return false;
		}

		BlockState blockState = CLIENT.world.getBlockState(blockPos);
		VoxelShape shape = blockState.getCollisionShape(CLIENT.world, blockPos);
		if (shape.isEmpty()) {
			return false;
		}
		return shape.getBoundingBox().maxY == 1;
	}

	/**
	 * works out where they player should be based on how far though the predicted teleport time.
	 *
	 * @return the camera position for the interpolated pos
	 */

	public static Vec3d getInterpolatedPos() {
		if (CLIENT.player == null || teleportVector == null || startPos == null || teleportDisabled) {
			return null;
		}
		long gap = System.currentTimeMillis() - startTime;
		//make sure the player is actually getting teleported if not disable teleporting until they are teleported again
		if (System.currentTimeMillis() - lastTeleportTime > Math.min(Math.max(lastPing, currentTeleportPing) + SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag, MAX_TELEPORT_TIME)) {
			teleportDisabled = true;
			startPos = null;
			teleportVector = null;
			teleportsAhead = 0;
			return null;
		}
		long estimatedTeleportTime = Math.min(currentTeleportPing, MAX_TELEPORT_TIME);
		double percentage = Math.clamp((double) (gap) / estimatedTeleportTime, 0, 1); // Sanity clamp

		//if the animation is done and the player has finished the teleport server side finish the teleport
		if (teleportsAhead == 0 && gap >= estimatedTeleportTime + SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag) {
			//reset when player has reached the end of the teleports
			startPos = null;
			teleportVector = null;
			return null;
		}

		return cameraStartPos.add(teleportVector.multiply(percentage));
	}

	public static void updatePing(long ping) {
		lastPing = ping;
	}


}
