package de.hysky.skyblocker.skyblock.dungeon.device;

import java.util.Objects;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

/**
 * Click blocking and sequence tracking approach adapted from
 * <a href="https://github.com/Noamm9/NoammAddons">NoammAddons</a> (CC0-1.0).
 */
public class SimonSays {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimonSays.class);
	private static final AABB BOARD_AREA = AABB.encapsulatingFullBlocks(new BlockPos(111, 123, 92), new BlockPos(111, 120, 95));
	private static final AABB BUTTONS_AREA = AABB.encapsulatingFullBlocks(new BlockPos(110, 123, 92), new BlockPos(110, 120, 95));
	private static final BlockPos START_BUTTON = new BlockPos(110, 121, 91);
	private static final float[] GREEN = ColorUtils.getFloatComponents(DyeColor.LIME);
	private static final float[] YELLOW = ColorUtils.getFloatComponents(DyeColor.YELLOW);
	// This MUST be an array list since this is meant to track the order in which the buttons must be clicked by the player.
	// Do not change this to use another class (especially one that does not maintain exact insertion order).
	private static final ObjectList<BlockPos> SIMON_PATTERN = new ObjectArrayList<>();
	//How many buttons of the current sequence have been pressed, by anyone. The next expected button is pattern[nextButtonIndex].west()
	private static int nextButtonIndex;
	//A press we cause is counted immediately and again by the powered-state update, so the confirmation has to be matched to it
	private static @Nullable BlockPos pendingOwnPress;

	@Init
	public static void init() {
		UseBlockCallback.EVENT.register(SimonSays::onBlockInteract);
		AttackBlockCallback.EVENT.register(SimonSays::onAttackBlock);
		ClientPlayConnectionEvents.JOIN.register((_, _, _) -> reset());
		LevelRenderExtractionCallback.EVENT.register(SimonSays::extractRendering);
		WorldEvents.BLOCK_STATE_UPDATE.register(SimonSays::onBlockUpdate);
	}

	private static void debug(String message) {
		if (Debug.debugEnabled()) LOGGER.info("[Skyblocker Simon Says] {}", message);
	}

	private static String playerContext(Player player) {
		return String.format("sneak %s, held %s", player.isShiftKeyDown(), player.getMainHandItem().getItem());
	}

	//When another player is pressing the buttons hypixel doesnt send block or block state updates
	//so you can't see it which means the solver can only count the buttons you press yourself
	private static InteractionResult onBlockInteract(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (shouldProcess()) {
			BlockPos pos = hitResult.getBlockPos();
			Block block = world.getBlockState(pos).getBlock();

			if (block.equals(Blocks.STONE_BUTTON)) {
				if (BUTTONS_AREA.contains(Vec3.atLowerCornerOf(pos))) {
					boolean wrong = isWrongButtonClick(pos);

					if (wrong) {
						debug(String.format("button click: %s -> BLOCKED (out of order), expected %s, pattern %d long, %d pressed, player %s",
								pos.toShortString(), describeExpected(), SIMON_PATTERN.size(), nextButtonIndex, playerContext(player)));
						Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.buttonOutOfOrder");
						return InteractionResult.FAIL;
					}

					debug(String.format("button click: %s -> allowed, pattern %d long, now %d pressed, player %s",
							pos.toShortString(), SIMON_PATTERN.size(), nextButtonIndex + 1, playerContext(player)));
					nextButtonIndex++;
					pendingOwnPress = pos.immutable();
				} else if (pos.equals(START_BUTTON)) {
					debug("start button pressed, resetting pattern");
					reset();
				}
			} else if (BUTTONS_AREA.contains(Vec3.atLowerCornerOf(pos))) {
				debug(String.format("block use: clicked %s is %s, not a stone button", pos.toShortString(), block.getName().toString()));
			}
		} else if (BUTTONS_AREA.contains(Vec3.atLowerCornerOf(hitResult.getBlockPos())) && world.getBlockState(hitResult.getBlockPos()).getBlock().equals(Blocks.STONE_BUTTON)) {
			debug(String.format("button click on %s IGNORED: solver inactive (solveSimonSays %s, inDungeons %s, goldorPhase %s)",
					hitResult.getBlockPos().toShortString(),
					SkyblockerConfigManager.get().dungeons.devices.solveSimonSays, Utils.isInDungeons(), BossPhase.isActive()));
		}

		return InteractionResult.PASS;
	}

	//Breaking a device button would fail the device, there is never a legitimate reason to attack one
	private static InteractionResult onAttackBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		if (!shouldProcess() || Utils.isBlockerBypassed() || !world.getBlockState(pos).getBlock().equals(Blocks.STONE_BUTTON)) return InteractionResult.PASS;
		if (!BUTTONS_AREA.contains(Vec3.atLowerCornerOf(pos)) && !pos.equals(START_BUTTON)) return InteractionResult.PASS;

		debug(String.format("attack on button %s -> BLOCKED (breaking it would fail the device), player %s", pos.toShortString(), playerContext(player)));
		Utils.sendBlockedClickMessage("skyblocker.dungeons.blockers.breakBlocked");
		return InteractionResult.FAIL;
	}

	private static boolean isWrongButtonClick(BlockPos clickedPos) {
		if (Utils.isBlockerBypassed() || !SkyblockerConfigManager.get().dungeons.devices.blockIncorrectClicks) return false;
		//Every pattern button is pressed: a further press would only un-press or break the device
		return nextButtonIndex < SIMON_PATTERN.size() && !expectedButton().equals(clickedPos);
	}

	private static BlockPos expectedButton() {
		return SIMON_PATTERN.get(nextButtonIndex).west();
	}

	private static String describeExpected() {
		return nextButtonIndex < SIMON_PATTERN.size() ? expectedButton().toShortString() : "none, sequence complete";
	}

	//If the player goes out of the range required to receive block/chunk updates then their solver won't detect stuff but that
	//doesn't matter because if they're doing pre-4 or something they won't be doing the ss, and if they end up needing to they can
	//just reset it or have the other person finish the current sequence first then let them do it.
	private static void onBlockUpdate(BlockPos pos, @Nullable BlockState oldState, BlockState newState) {
		if (shouldProcess()) {
			Vec3 posVec = Vec3.atLowerCornerOf(pos);
			Block newBlock = newState.getBlock();

			if (BOARD_AREA.contains(posVec) && newBlock.equals(Blocks.OBSIDIAN) && oldState != null && oldState.getBlock().equals(Blocks.SEA_LANTERN)) {
				SIMON_PATTERN.add(pos.immutable()); //Convert to immutable because chunk delta updates use the mutable variant
				debug(String.format("pattern button added at %s, pattern now %d long", pos.toShortString(), SIMON_PATTERN.size()));
			} else if (BUTTONS_AREA.contains(posVec) && newBlock.equals(Blocks.AIR)) {
				//Upon reaching the showing of the next sequence we need to reset the state so that we don't show old data
				//Otherwise, the nextIndex will go beyond 5 and that can cause bugs, it also helps with the other case noted above
				debug(String.format("button at %s turned to air, resetting pattern (%d was long)", pos.toShortString(), SIMON_PATTERN.size()));
				reset();
			} else if (BUTTONS_AREA.contains(posVec) && newBlock == Blocks.STONE_BUTTON && newState.getValue(BlockStateProperties.POWERED)) {
				//A button flipping to its powered state is a press, by any player. Tracking these keeps
				//the solver in sync with reality instead of only counting presses we caused ourselves
				if (pos.equals(pendingOwnPress)) {
					pendingOwnPress = null;
					debug(String.format("button press confirmed at %s, %d pressed", pos.toShortString(), nextButtonIndex));
				} else if (!SIMON_PATTERN.isEmpty() && pos.equals(expectedButton())) {
					nextButtonIndex++;
					debug(String.format("button press by another player at %s, now %d pressed", pos.toShortString(), nextButtonIndex));
				} else {
					debug(String.format("button press at %s does not match the expected button %s, the device likely broke", pos.toShortString(), describeExpected()));
				}
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess()) {
			ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level); //Should never be null here

			for (int i = nextButtonIndex; i < Math.min(nextButtonIndex + 2, SIMON_PATTERN.size()); i++) {
				//Offset to west (x - 1) to get the position of the button from the sea lantern block
				BlockPos buttonPos = SIMON_PATTERN.get(i).west();
				BlockState state = world.getBlockState(buttonPos);

				//Don't do anything if the button isn't there which means the device is showing the sequence
				if (state.getBlock().equals(Blocks.STONE_BUTTON)) {
					AABB outline = RenderHelper.getBlockBoundingBox(world, state, buttonPos);

					if (outline != null) {
						float[] colour = i == nextButtonIndex ? GREEN : YELLOW;
						collector.submitFilledBox(outline, colour, 0.5f, true);
						collector.submitOutlinedBox(outline, colour, 5f, true);
					}
				}
			}
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveSimonSays &&
				Utils.isInDungeons() && BossPhase.isActive();
	}

	private static void reset() {
		nextButtonIndex = 0;
		pendingOwnPress = null;
		SIMON_PATTERN.clear();
	}
}
