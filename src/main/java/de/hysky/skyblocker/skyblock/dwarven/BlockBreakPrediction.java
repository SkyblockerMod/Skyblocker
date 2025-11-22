package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockBreakPrediction {

	private static int lastProgress;
	private static long startAttackingTime;
	private static long startBreakTime;
	private static long progressTime;


	@Init
	public static void init() {
		AttackBlockCallback.EVENT.register(BlockBreakPrediction::onBlockInteract);

	}

	private static ActionResult onBlockInteract(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
		startAttackingTime = System.currentTimeMillis();
		progressTime = 0;
		System.out.println("resetAttack");
		return ActionResult.PASS;
	}

	public static int getBlockBreakPrediction(BlockPos pos, int progression) {
		//make sure it's the block the player is looking at
		if (MinecraftClient.getInstance().player == null) return -1;
		if (MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult hitResult) {
			if (!hitResult.getBlockPos().equals(pos)) {
				return progression;
			}
		} else {
			return progression;
		}
		//reset if new target block
		if (progression == 0 && progression != lastProgress) {


		}
		if (progression == 1 && progression != lastProgress) {
			System.out.println(System.currentTimeMillis() - startAttackingTime + "i love print debug i how ");
			startBreakTime = System.currentTimeMillis();

		}

		if (progression > 1 && progression != lastProgress) {
			//find average breaking progress
			progressTime = (System.currentTimeMillis() - startBreakTime) / (progression - 1);


		}
		lastProgress = progression;
		//calculate actual render state
		if (progressTime > 0 && startAttackingTime > 0) {
			long neededTime = 10 * progressTime;
			long timeElapsed = System.currentTimeMillis() - startAttackingTime;


			return Math.min((int) ((timeElapsed * 10) / (neededTime)), 9);
		}

		return progression;
	}


}
