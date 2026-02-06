package de.hysky.skyblocker.skyblock.teleport;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ItemAbility;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

public class PredictiveSmoothAOTE {
	public static final Identifier SMOOTH_AOTE_BEFORE_PHASE = SkyblockerMod.id("smooth_aote");
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final long MAX_TELEPORT_TIME = 2500; //2.5 seconds

	private static long startTime;
	private static Vec3 startPos;
	private static Vec3 cameraStartPos;
	private static Vec3 teleportVector;
	private static long lastPing;
	private static long currentTeleportPing;
	private static int teleportsAhead;
	private static long lastTeleportTime;
	public static boolean teleportDisabled;

	@Init
	public static void init() {
		UseItemCallback.EVENT.register(SMOOTH_AOTE_BEFORE_PHASE, PredictiveSmoothAOTE::onItemInteract);
		UseItemCallback.EVENT.addPhaseOrdering(SMOOTH_AOTE_BEFORE_PHASE, Event.DEFAULT_PHASE); // run this event first to check mana before it gets changed by the tracker
		UseBlockCallback.EVENT.register(PredictiveSmoothAOTE::onBlockInteract);
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
	private static int extractTunedCustomData(CompoundTag customData, int baseRange) {
		return customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getIntOr("tuned_transmission", 0) : baseRange;
	}

	/**
	 * When an item is right-clicked send off to calculate teleport with the clicked item
	 *
	 * @param playerEntity player
	 * @param world        world
	 * @param hand         held item
	 * @return pass
	 */
	private static InteractionResult onItemInteract(Player playerEntity, Level world, InteractionHand hand) {
		if (CLIENT.player == null) {
			return null;
		}
		calculateTeleportUse(hand);
		return InteractionResult.PASS;
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
	private static InteractionResult onBlockInteract(Player playerEntity, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
		ItemStack itemStack = playerEntity.getItemInHand(hand);
		if (isShovel(itemStack) && canShovelActOnBlock(world.getBlockState(blockHitResult.getBlockPos()).getBlock())) {
			calculateTeleportUse(hand);
		}
		return InteractionResult.PASS;
	}

	private static boolean isShovel(ItemStack itemStack) {
		return itemStack.is(Items.WOODEN_SHOVEL) ||
				itemStack.is(Items.STONE_SHOVEL) ||
				itemStack.is(Items.IRON_SHOVEL) ||
				itemStack.is(Items.GOLDEN_SHOVEL) ||
				itemStack.is(Items.DIAMOND_SHOVEL);
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

	private static void calculateTeleportUse(InteractionHand hand) {
		//stop checking if player does not exist
		if (CLIENT.player == null || CLIENT.level == null) {
			return;
		}

		// make sure the predictive algorithm is selected
		if (!SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.predictive) return;

		//make sure it's not disabled
		if (teleportDisabled) {
			return;
		}

		// make sure the camera is not in 3rd person
		if (CLIENT.options.getCameraType() != CameraType.FIRST_PERSON) {
			return;
		}

		//make sure the player is in an area teleporting is allowed not allowed in glacite mineshafts and floor 7 boss
		if (!isAllowedLocation()) {
			return;
		}

		//work out if the player is holding a teleporting item that is enabled and if so how far the item will take them
		ItemStack heldItem = CLIENT.player.getMainHandItem();
		String itemId = heldItem.getSkyblockId();
		CompoundTag customData = ItemUtils.getCustomData(heldItem);

		int distance = getItemDistance(itemId, customData);
		if (distance == -1) {
			return;
		}

		//make sure the player has enough mana to do the teleport
		List<ItemAbility> abilities = heldItem.skyblocker$getAbilities();
		if (!abilities.isEmpty() && abilities.getFirst().manaCost().isPresent()) {
			int manaCost = abilities.getFirst().manaCost().getAsInt();
			int predictedMana = StatusBarTracker.getMana().value();
			if (predictedMana < manaCost) {
				return;
			}
		}

		//work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
		if (teleportsAhead == 0 || startPos == null || teleportVector == null) {
			//start of teleport sequence
			startPos = CLIENT.player.position().add(0, Utils.getEyeHeight(CLIENT.player), 0); // the eye poss should not be affected by crouching
			cameraStartPos = CLIENT.player.getEyePosition();
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
		float pitch = CLIENT.player.getXRot();
		float yaw = CLIENT.player.getYRot();
		Vec3 look = CLIENT.player.calculateViewVector(pitch, yaw);

		//make sure the player is not talking to an npc. And if they are cancel the teleport
		if (startPos == null) return;
		if (isTargetingNPC(CLIENT.player, 4, startPos, look)) {
			startPos = null;
			teleportVector = null;
			return;
		}

		//find target location depending on how far the item they are using takes them
		teleportVector = raycast(distance, look, startPos, false);
		if (teleportVector == null) {
			startPos = null;
			return;
		}

		//compensate for hypixel round to center of block (to x.5 y.(eye height - 1), z.5)
		Vec3 predictedEnd = startPos.add(teleportVector);
		Vec3 offsetVec = new Vec3(predictedEnd.x - roundToCenter(predictedEnd.x), predictedEnd.y - (Math.ceil(predictedEnd.y) + Utils.getEyeHeight(CLIENT.player) - 1), predictedEnd.z - roundToCenter(predictedEnd.z));
		teleportVector = teleportVector.subtract(offsetVec);
		//add 1 to teleports ahead
		teleportsAhead += 1;
	}

	/**
	 * work out if the player is holding a teleporting item that is enabled and if so how far the item will take them
	 *
	 * @param itemId     id of item to check
	 * @param customData custom data of item to check
	 * @return distance the item teleports or -1 if not valid
	 */
	protected static int getItemDistance(String itemId, CompoundTag customData) {
		int distance;
		switch (itemId) {
			case "ASPECT_OF_THE_LEECH_1" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
					distance = 3;
					break;
				}
				return -1;

			}
			case "ASPECT_OF_THE_LEECH_2" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
					distance = 4;
					break;
				}
				return -1;
			}
			case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
				if (CLIENT.options.keyShift.isDown() && customData.getIntOr("ethermerge", 0) == 1) {
					if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
						distance = extractTunedCustomData(customData, 57);
						break;
					}
				} else if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableInstantTransmission) {
					distance = extractTunedCustomData(customData, 8);
					break;
				}
				return -1;
			}
			case "ETHERWARP_CONDUIT" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
					distance = extractTunedCustomData(customData, 57);
					break;
				}
				return -1;
			}
			case "SINSEEKER_SCYTHE" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableSinrecallTransmission) {
					distance = extractTunedCustomData(customData, 4);
					break;
				}
				return -1;
			}
			case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
				if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWitherImpact) {
					distance = 10;
					break;
				}
				return -1;
			}
			default -> {
				return -1;
			}
		}
		return distance;
	}

	/**
	 * Checks if the player is targeting an entity and then checks if it has a CLICK tag suggesting it has an interaction that will block the teleport
	 *
	 * @param player      player
	 * @param maxDistance max distance this is needed
	 * @param startPos    player starting location
	 * @param look        players looking direction
	 * @return if an NPC is targeted
	 */
	private static Boolean isTargetingNPC(Player player, double maxDistance, Vec3 startPos, Vec3 look) {
		if (startPos == null) return false;
		// Calculate end position for raycast
		Vec3 endPos = startPos.add(look.scale(maxDistance));

		// First: Raycast for blocks (to check obstructions)
		Level world = player.level();
		ClipContext context = new ClipContext(startPos, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
		double blockHitDistance = world.clip(context).getLocation().distanceTo(startPos);

		// Second: Raycast for entities (within valid range)
		AABB searchBox = player
				.getBoundingBox()
				.expandTowards(look.scale(maxDistance)) // Extend box in look direction
				.inflate(1); // Margin for safety

		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, startPos, endPos, searchBox, entity ->
						!entity.isSpectator() && entity != player,
				Mth.square(blockHitDistance) // Max distance (squared)
		);
		//if not looking at any entity return false
		if (entityHit == null) return false;

		//look for armorstand saying click to see if it's A npc or not
		Entity entity = entityHit.getEntity();
		List<ArmorStand> armorStands = MobGlow.getArmorStands(entity);

		return armorStands.stream().anyMatch(armorStand -> armorStand.getName().getString().equals("CLICK"));
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
		} else if (Utils.getArea() == Area.JUNGLE_TEMPLE) { //do not allow in jungle temple
			return false;
		} else if (Utils.getLocation() == Location.PRIVATE_ISLAND && Utils.getArea() != Area.YOUR_ISLAND) { //do not allow it when visiting
			return false;
		} else if (Utils.getArea() == Area.DOJO) { //do not allow in dojo
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
	 * Custom raycast for teleporting checks for blocks for each 1 block forward in teleport. (very similar to Hypixel's method)
	 *
	 * @param distance maximum distance
	 * @return teleport vector
	 */
	protected static Vec3 raycast(int distance, Vec3 direction, Vec3 startPos, boolean isEtherwarp) {
		if (CLIENT.level == null || direction == null || startPos == null) {
			return null;
		}

		//based on which way the ray is going get the needed vector for checking diagonals
		BlockPos xDiagonalOffset = direction.x() > 0 ? new BlockPos(1, 0, 0) : new BlockPos(-1, 0, 0);
		BlockPos zDiagonalOffset = direction.z() > 0 ? new BlockPos(0, 0, 1) : new BlockPos(0, 0, -1);


		//initialise the closest floor value outside of possible values
		int closeFloorY = Integer.MAX_VALUE;

		//loop though each block of a teleport checking each block if there are blocks in the way
		for (double offset = 0; offset <= distance; offset++) {
			Vec3 pos = startPos.add(direction.scale(offset));

			BlockPos checkPos = BlockPos.containing(pos);

			//check if there is a block at the check location
			if (!canTeleportThrough(checkPos)) {
				if (!isEtherwarp && offset == 0) {
					// no teleport can happen
					return null;
				}
				if (isEtherwarp) return direction.scale(offset - 1).add(direction);
				return direction.scale(offset - 1);
			}

			//check if the block at head height is free
			if (!canTeleportThrough(checkPos.above()) && !isEtherwarp) {
				if (offset == 0) {
					//cancel the check if starting height is too low
					Vec3 justAhead = startPos.add(direction.scale(0.2));
					if ((justAhead.y() - Math.floor(justAhead.y())) <= 0.495) {
						continue;
					}
					// no teleport can happen
					return null;
				}
				return direction.scale(offset - 1);
			}

			//check for diagonal walls for some reason this check is directional, and you can go through from some directions. This seems to emulate this as best as possible
			if (offset != 0 && direction.x() < 0 && (isBlockFloor(checkPos.east())) && (isBlockFloor(BlockPos.containing(pos.subtract(direction)).offset(zDiagonalOffset)))) {
				return direction.scale(offset - 1);
			}
			if (offset != 0 && direction.z() < 0 && direction.x() < 0 && (isBlockFloor(checkPos.south())) && (isBlockFloor(BlockPos.containing(pos.subtract(direction)).offset(xDiagonalOffset)))) {
				return direction.scale(offset - 1);
			}

			//if the player is close to the floor (including diagonally) save Y and when player goes bellow this y finish teleport
			if ((isBlockFloor(checkPos.below()) || (isBlockFloor(checkPos.below().offset(xDiagonalOffset)) && isBlockFloor(checkPos.below().offset(zDiagonalOffset)))) && (pos.y() - Math.floor(pos.y())) < 0.31) {
				closeFloorY = checkPos.getY() - 1;
			}

			//if the checking Y is same as closeY finish
			if (closeFloorY == checkPos.getY()) {
				return direction.scale(offset - 1);
			}
		}

		//return full distance if no collision found
		return direction.scale(distance);
	}

	/**
	 * Checks to see if a block is in the allowed list to teleport though
	 * Air, non-collidable blocks, carpets, pots, 3 or less snow layers
	 *
	 * @param blockPos block location
	 * @return if a block location can be teleported though
	 */
	private static Boolean canTeleportThrough(BlockPos blockPos) {
		if (CLIENT.level == null) {
			return false;
		}

		BlockState blockState = CLIENT.level.getBlockState(blockPos);
		if (blockState.isAir()) {
			return true;
		}
		Block block = blockState.getBlock();
		VoxelShape shape = blockState.getCollisionShape(CLIENT.level, blockPos);

		return shape.isEmpty() || block instanceof CarpetBlock || block instanceof FlowerPotBlock || (block.equals(Blocks.SNOW) && blockState.getValue(BlockStateProperties.LAYERS) <= 3);
	}

	/**
	 * Checks to see if a block goes to the top if so class it as a floor
	 *
	 * @param blockPos block location
	 * @return if it's a floor block
	 */
	private static Boolean isBlockFloor(BlockPos blockPos) {
		if (CLIENT.level == null) {
			return false;
		}

		BlockState blockState = CLIENT.level.getBlockState(blockPos);
		VoxelShape shape = blockState.getCollisionShape(CLIENT.level, blockPos);
		if (shape.isEmpty()) {
			return false;
		}
		return shape.bounds().maxY >= 1 || blockState.getBlock() == Blocks.MUD; //every thing 1 or above counts but there is some added extras like mud
	}

	/**
	 * works out where they player should be based on how far though the predicted teleport time.
	 *
	 * @return the camera position for the interpolated pos
	 */

	public static Vec3 getInterpolatedPos() {
		if (CLIENT.player == null || teleportVector == null || startPos == null || teleportDisabled) {
			return null;
		}
		long gap = System.currentTimeMillis() - startTime;
		//make sure the player is actually getting teleported if not disable teleporting until they are teleported again
		if (System.currentTimeMillis() - lastTeleportTime > Math.min(Math.max(lastPing, currentTeleportPing) + ((long) SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.maximumAddedLag * teleportsAhead), MAX_TELEPORT_TIME)) {
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

		return cameraStartPos.add(teleportVector.scale(percentage));
	}

	public static void updatePing(long ping) {
		lastPing = ping;
	}


}
