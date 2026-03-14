package de.hysky.skyblocker.skyblock.dungeon.device;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.BlockPosSet;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

public class SimonSays {
	private static final AABB BOARD_AREA = AABB.encapsulatingFullBlocks(new BlockPos(111, 123, 92), new BlockPos(111, 120, 95));
	private static final AABB BUTTONS_AREA = AABB.encapsulatingFullBlocks(new BlockPos(110, 123, 92), new BlockPos(110, 120, 95));
	private static final BlockPos START_BUTTON = new BlockPos(110, 121, 91);
	private static final float[] GREEN = ColorUtils.getFloatComponents(DyeColor.LIME);
	private static final float[] YELLOW = ColorUtils.getFloatComponents(DyeColor.YELLOW);
	private static final BlockPosSet CLICKED_BUTTONS = new BlockPosSet();
	private static final BlockPosSet SIMON_PATTERN = new BlockPosSet();

	@Init
	public static void init() {
		UseBlockCallback.EVENT.register(SimonSays::onBlockInteract);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderExtractionCallback.EVENT.register(SimonSays::extractRendering);
		WorldEvents.BLOCK_STATE_UPDATE.register(SimonSays::onBlockUpdate);
	}

	//When another player is pressing the buttons hypixel doesnt send block or block state updates
	//so you can't see it which means the solver can only count the buttons you press yourself
	private static InteractionResult onBlockInteract(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (shouldProcess()) {
			BlockPos pos = hitResult.getBlockPos();
			Block block = world.getBlockState(pos).getBlock();

			if (block.equals(Blocks.STONE_BUTTON)) {
				if (BUTTONS_AREA.contains(Vec3.atLowerCornerOf(pos))) {
					CLICKED_BUTTONS.add(pos);
				} else if (pos.equals(START_BUTTON)) {
					reset();
				}
			}
		}

		//This could also be used to cancel incorrect clicks in the future
		return InteractionResult.PASS;
	}

	//If the player goes out of the range required to receive block/chunk updates then their solver won't detect stuff but that
	//doesn't matter because if they're doing pre-4 or something they won't be doing the ss, and if they end up needing to they can
	//just reset it or have the other person finish the current sequence first then let them do it.
	private static void onBlockUpdate(BlockPos pos, @Nullable BlockState oldState, BlockState newState) {
		if (shouldProcess()) {
			Vec3 posVec = Vec3.atLowerCornerOf(pos);
			Block newBlock = newState.getBlock();

			if (BOARD_AREA.contains(posVec) && newBlock.equals(Blocks.OBSIDIAN) && oldState != null && oldState.getBlock().equals(Blocks.SEA_LANTERN)) {
				SIMON_PATTERN.add(pos);
			} else if (BUTTONS_AREA.contains(posVec) && newBlock.equals(Blocks.AIR)) {
				//Upon reaching the showing of the next sequence we need to reset the state so that we don't show old data
				//Otherwise, the nextIndex will go beyond 5 and that can cause bugs, it also helps with the other case noted above
				reset();
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (shouldProcess()) {
			int buttonsRendered = 0;

			for (BlockPos pos : SIMON_PATTERN.iterateMut()) {
				//Offset to west (x - 1) to get the position of the button from the sea lantern block
				BlockPos buttonPos = pos.west();
				ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level); //Should never be null here
				BlockState state = world.getBlockState(buttonPos);

				//If the button hasn't been clicked yet
				//Also don't do anything if the button isn't there which means the device is showing the sequence
				if (!CLICKED_BUTTONS.contains(buttonPos) && state.getBlock().equals(Blocks.STONE_BUTTON)) {
					AABB outline = RenderHelper.getBlockBoundingBox(world, state, buttonPos);

					if (outline != null) {
						float[] colour = buttonsRendered == 0 ? GREEN : YELLOW;

						collector.submitFilledBox(outline, colour, 0.5f, true);
						collector.submitOutlinedBox(outline, colour, 5f, true);

						if (++buttonsRendered == 2) return;
					}
				}
			}
		}
	}

	private static boolean shouldProcess() {
		return SkyblockerConfigManager.get().dungeons.devices.solveSimonSays &&
				Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR;
	}

	private static void reset() {
		CLICKED_BUTTONS.clear();
		SIMON_PATTERN.clear();
	}
}
