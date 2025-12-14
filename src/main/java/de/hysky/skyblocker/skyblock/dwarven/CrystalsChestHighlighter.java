package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.events.PlaySoundEvents;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.HashSet;
import java.util.Set;

public class CrystalsChestHighlighter {

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String CHEST_SPAWN_MESSAGE = "You uncovered a treasure chest!";
	private static final long MAX_PARTICLE_LIFE_TIME = 250;
	private static final Vec3 LOCK_HIGHLIGHT_SIZE = new Vec3(0.1, 0.1, 0.1);

	private static int waitingForChest = 0;
	private static final Set<BlockPos> activeChests = new HashSet<>();
	private static final Object2LongOpenHashMap<Vec3> activeParticles = new Object2LongOpenHashMap<>();
	private static int currentLockCount = 0;
	private static int neededLockCount = 0;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(CrystalsChestHighlighter::extractLocationFromMessage);
		WorldRenderExtractionCallback.EVENT.register(CrystalsChestHighlighter::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldEvents.BLOCK_STATE_UPDATE.register(CrystalsChestHighlighter::onBlockUpdate);
		ParticleEvents.FROM_SERVER.register(CrystalsChestHighlighter::onParticle);
		PlaySoundEvents.FROM_SERVER.register(CrystalsChestHighlighter::onSound);
	}

	private static void reset() {
		waitingForChest = 0;
		activeChests.clear();
		activeParticles.clear();
		currentLockCount = 0;
	}

	private static boolean extractLocationFromMessage(Component text, boolean b) {
		if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
			return true;
		}
		//if a chest is spawned add chest to look for
		if (text.getString().matches(CHEST_SPAWN_MESSAGE)) {
			waitingForChest += 1;
		}

		return true;
	}

	/**
	 * When a block is updated in the crystal hollows if looking for a chest see if it's a chest and if so add to active. or remove active chests from where air is placed
	 *
	 * @param pos   location of block update
	 * @param newState the new state of the block
	 */
	private static void onBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState) {
		if (!SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter || CLIENT.player == null || !Utils.isInCrystalHollows()) {
			return;
		}

		BlockPos immutable = pos.immutable();

		if (waitingForChest > 0 && newState.is(Blocks.CHEST)) {
			//make sure it is not too far from the player (more than 10 blocks away)
			if (immutable.distToCenterSqr(CLIENT.player.position()) > 100) {
				return;
			}
			activeChests.add(immutable);
			currentLockCount = 0;
			waitingForChest -= 1;
		} else if (newState.isAir() && activeChests.contains(immutable)) {
			currentLockCount = 0;
			activeChests.remove(immutable);
		}
	}

	/**
	 * When a particle is spawned add that particle to active particles if correct for lock picking
	 *
	 * @param packet particle spawn packet
	 */
	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
			return;
		}
		if (ParticleTypes.CRIT.equals(packet.getParticle().getType())) {
			activeParticles.put(new Vec3(packet.getX(), packet.getY(), packet.getZ()), System.currentTimeMillis());
		}
	}

	/**
	 * Updates {@link CrystalsChestHighlighter#currentLockCount} and clears {@link CrystalsChestHighlighter#activeParticles} based on lock pick related sound events.
	 *
	 * @param packet sound packet
	 */
	private static void onSound(ClientboundSoundPacket packet) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
			return;
		}
		SoundEvent sound = packet.getSound().value();
		//lock picked sound
		if (sound.location().equals(SoundEvents.EXPERIENCE_ORB_PICKUP.location()) && packet.getPitch() == 1 && !activeChests.isEmpty()) {
			Vec3 eyePos = player.getEyePosition(0);
			Vec3 rotationVec = player.getViewVector(0);
			double range = player.blockInteractionRange();
			Vec3 vec3d3 = eyePos.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
			BlockHitResult raycast = player.level().isBlockInLine(new ClipBlockStateContext(eyePos, vec3d3, blockState -> blockState.is(Blocks.CHEST)));
			if (!raycast.getType().equals(HitResult.Type.MISS)) {
				currentLockCount += 1;
				activeParticles.clear();
			}
			//lock pick fail sound
		} else if (sound.location().equals(SoundEvents.VILLAGER_NO.location())) {
			currentLockCount = 0;
			activeParticles.clear();
			//lock pick finish sound
		} else if (sound.location().equals(SoundEvents.CHEST_OPEN.location())) {
			//set the needed lock count to the current, so we know how many locks a chest has
			neededLockCount = Math.min(currentLockCount, 5);
			currentLockCount = 0;
			activeParticles.clear();
		}
	}

	/**
	 * If enabled, renders a box around active treasure chests, taking the color from the config.
	 * Additionally, calculates and displaces the highlight to indicate lock-picking spots on chests.
	 * Finally, renders text showing how many lock picks the player has done.
	 */
	private static void extractRendering(PrimitiveCollector collector) {
		if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
			return;
		}
		//render chest outline
		float[] color = SkyblockerConfigManager.get().mining.crystalHollows.chestHighlightColor.getComponents(new float[]{0, 0, 0, 0});
		for (BlockPos chest : activeChests) {
			collector.submitOutlinedBox(AABB.ofSize(chest.getCenter().subtract(0, 0.0625, 0), 0.885, 0.885, 0.885), color, color[3], 3, false);
		}

		//render lock picking if player is looking at chest that is in the active chests list
		if (CLIENT.player == null) {
			return;
		}
		HitResult target = CLIENT.hitResult;
		if (target instanceof BlockHitResult blockHitResult && activeChests.contains(blockHitResult.getBlockPos())) {
			Vec3 chestPos = blockHitResult.getBlockPos().getCenter();

			if (!activeParticles.isEmpty()) {
				//the player is looking at a chest use active particle to highlight correct spot
				Vec3 highlightSpot = Vec3.ZERO;

				//if to old remove particle
				activeParticles.object2LongEntrySet().removeIf(e -> System.currentTimeMillis() - e.getLongValue() > MAX_PARTICLE_LIFE_TIME);

				//add up all particle within range of active block
				int addedParticles = 0;
				for (Vec3 particlePos : activeParticles.keySet()) {
					if (particlePos.closerThan(chestPos, 0.8)) {
						highlightSpot = highlightSpot.add(particlePos);
						addedParticles++;
					}
				}

				//render the spot
				highlightSpot = highlightSpot.scale((double) 1 / addedParticles).subtract(LOCK_HIGHLIGHT_SIZE.scale(0.5));
				collector.submitFilledBox(highlightSpot, LOCK_HIGHLIGHT_SIZE, color, color[3], true);
			}

			//render total text if needed is more than 0
			if (neededLockCount <= 0) {
				return;
			}
			collector.submitText(Component.literal(Math.min(currentLockCount, neededLockCount) + "/" + neededLockCount).withColor(SkyblockerConfigManager.get().mining.crystalHollows.chestHighlightColor.getRGB()), chestPos, true);
		}
	}
}
