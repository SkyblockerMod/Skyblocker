package de.hysky.skyblocker.skyblock.dungeon.device;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

public class SimonSays {
	private static final Box BOARD_AREA = Box.enclosing(new BlockPos(111, 123, 92), new BlockPos(111, 120, 95));
	private static final Box BUTTONS_AREA = Box.enclosing(new BlockPos(110, 123, 92), new BlockPos(110, 120, 95));
	private static final BlockPos START_BUTTON = new BlockPos(110, 121, 91);
	private static final float[] GREEN = ColorUtils.getFloatComponents(DyeColor.LIME);
	private static final float[] YELLOW = ColorUtils.getFloatComponents(DyeColor.YELLOW);
	private static final ObjectSet<BlockPos> CLICKED_BUTTONS = new ObjectOpenHashSet<>();
	private static final ObjectList<BlockPos> SIMON_PATTERN = new ObjectArrayList<>();

	@Init
	public static void init() {
		UseBlockCallback.EVENT.register(SimonSays::onBlockInteract);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(SimonSays::render);
	}

	//When another player is pressing the buttons hypixel doesnt send block or block state updates
	//so you can't see it which means the solver can only count the buttons you press yourself
	private static ActionResult onBlockInteract(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (shouldProcess()) {
			BlockPos pos = hitResult.getBlockPos();
			Block block = world.getBlockState(pos).getBlock();

			if (block.equals(Blocks.STONE_BUTTON)) {
				if (BUTTONS_AREA.contains(Vec3d.of(pos))) {
					CLICKED_BUTTONS.add(new BlockPos(pos)); //Copy just in case it becomes mutable in the future
				} else if (pos.equals(START_BUTTON)) {
					reset();
				}
			}
		}

		//This could also be used to cancel incorrect clicks in the future
		return ActionResult.PASS;
	}

	//If the player goes out of the range required to receive block/chunk updates then their solver won't detect stuff but that
	//doesn't matter because if they're doing pre-4 or something they won't be doing the ss, and if they end up needing to they can
	//just reset it or have the other person finish the current sequence first then let them do it.
	public static void onBlockUpdate(BlockPos pos, BlockState state) {
		if (shouldProcess()) {
			Vec3d posVec = Vec3d.of(pos);
			Block block = state.getBlock();

			if (BOARD_AREA.contains(posVec) && block.equals(Blocks.SEA_LANTERN)) {
				SIMON_PATTERN.add(pos.toImmutable()); //Convert to immutable because chunk delta updates use the mutable variant
			} else if (BUTTONS_AREA.contains(posVec) && block.equals(Blocks.AIR)) {
				//Upon reaching the showing of the next sequence we need to reset the state so that we don't show old data
				//Otherwise, the nextIndex will go beyond 5 and that can cause bugs, it also helps with the other case noted above
				reset();
			}
		}
	}

	private static void render(WorldRenderContext context) {
		if (shouldProcess()) {
			int buttonsRendered = 0;

			for (BlockPos pos : SIMON_PATTERN) {
				//Offset to west (x - 1) to get the position of the button from the sea lantern block
				BlockPos buttonPos = pos.west();
				ClientWorld world = Objects.requireNonNull(MinecraftClient.getInstance().world); //Should never be null here
				BlockState state = world.getBlockState(buttonPos);

				//If the button hasn't been clicked yet
				//Also don't do anything if the button isn't there which means the device is showing the sequence
				if (!CLICKED_BUTTONS.contains(buttonPos) && state.getBlock().equals(Blocks.STONE_BUTTON)) {
					Box outline = RenderHelper.getBlockBoundingBox(world, state, buttonPos);
					float[] colour = buttonsRendered == 0 ? GREEN : YELLOW;

					RenderHelper.renderFilled(context, outline, colour, 0.5f, true);
					RenderHelper.renderOutline(context, outline, colour, 5f, true);

					if (++buttonsRendered == 2) return;
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
