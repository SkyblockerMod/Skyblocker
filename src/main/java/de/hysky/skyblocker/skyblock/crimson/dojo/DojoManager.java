package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.events.WorldEvents;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DojoManager {

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final String START_MESSAGE = "[NPC] Master Tao: Ahhh, here we go! Let's get you into the Arena.";
	private static final Pattern TEST_OF_PATTERN = Pattern.compile("\\s+Test of (\\w+) OBJECTIVES");
	private static final String CHALLENGE_FINISHED_REGEX = "\\s+CHALLENGE ((COMPLETED)|(FAILED))";


	protected enum DojoChallenges {
		NONE("none", enabled -> false),
		FORCE("Force", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableForceHelper),
		STAMINA("Stamina", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableStaminaHelper),
		MASTERY("Mastery", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableMasteryHelper),
		DISCIPLINE("Discipline", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableDisciplineHelper),
		SWIFTNESS("Swiftness", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableSwiftnessHelper),
		CONTROL("Control", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableControlHelper),
		TENACITY("Tenacity", enabled -> SkyblockerConfigManager.get().crimsonIsle.dojo.enableTenacityHelper);

		private final String name;
		private final BooleanPredicate enabled;

		DojoChallenges(String name, BooleanPredicate enabled) {
			this.name = name;
			this.enabled = enabled;
		}

		public static DojoChallenges from(String name) {
			return Arrays.stream(DojoChallenges.values()).filter(n -> name.equals(n.name)).findFirst().orElse(NONE);
		}
	}

	protected static DojoChallenges currentChallenge = DojoChallenges.NONE;
	public static boolean inArena = false;
	protected static long ping = -1;

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(DojoManager::onMessage);
		WorldRenderExtractionCallback.EVENT.register(DojoManager::extractRendering);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		ClientEntityEvents.ENTITY_LOAD.register(DojoManager::onEntitySpawn);
		ClientEntityEvents.ENTITY_UNLOAD.register(DojoManager::onEntityDespawn);
		AttackEntityCallback.EVENT.register(DojoManager::onEntityAttacked);
		Scheduler.INSTANCE.scheduleCyclic(DojoManager::update, 3);
		WorldEvents.BLOCK_STATE_UPDATE.register(DojoManager::onBlockUpdate);
		ParticleEvents.FROM_SERVER.register(DojoManager::onParticle);
	}

	private static void reset() {
		inArena = false;
		currentChallenge = DojoChallenges.NONE;
		ForceTestHelper.reset();
		StaminaTestHelper.reset();
		MasteryTestHelper.reset();
		SwiftnessTestHelper.reset();
		ControlTestHelper.reset();
		TenacityTestHelper.reset();
	}

	/**
	 * works out if the player is in dojo and if so what challenge based on chat messages
	 *
	 * @param text    message
	 * @param overlay is overlay
	 */
	private static boolean onMessage(Component text, Boolean overlay) {
		if (!Utils.isInCrimson() || overlay) {
			return true;
		}
		if (Objects.equals(ChatFormatting.stripFormatting(text.getString()), START_MESSAGE)) {
			inArena = true;
			//update the players ping
			getPing();
			return true;
		}
		if (!inArena) {
			return true;
		}
		if (text.getString().matches(CHALLENGE_FINISHED_REGEX)) {
			reset();
			return true;
		}

		//look for a message saying what challenge is starting if one has not already been found
		if (currentChallenge != DojoChallenges.NONE) {
			return true;
		}
		Matcher nextChallenge = TEST_OF_PATTERN.matcher(text.getString());
		if (nextChallenge.matches()) {
			currentChallenge = DojoChallenges.from(nextChallenge.group(1));
			if (!currentChallenge.enabled.test(true)) {
				currentChallenge = DojoChallenges.NONE;
			}
		}

		return true;
	}

	private static void getPing() {
		ClientPacketListener networkHandler = CLIENT.getConnection();
		if (networkHandler != null) {
			networkHandler.send(new ServerboundPingRequestPacket(Util.getMillis()));
		}
	}

	public static void onPingResult(long ping) {
		DojoManager.ping = ping;
	}

	@SuppressWarnings("incomplete-switch")
	private static void update() {
		if (!Utils.isInCrimson() || !inArena) {
			return;
		}
		switch (currentChallenge) {
			case STAMINA -> StaminaTestHelper.update();
			case CONTROL -> ControlTestHelper.update();
		}
	}

	/**
	 * called from the {@link de.hysky.skyblocker.skyblock.entity.MobGlow} class and checks the current challenge to see if zombies should be glowing
	 *
	 * @param name name of the zombie
	 * @return if the zombie should glow
	 */
	public static boolean shouldGlow(String name) {
		if (!Utils.isInCrimson() || !inArena) {
			return false;
		}
		return switch (currentChallenge) {
			case FORCE -> ForceTestHelper.shouldGlow(name);
			case DISCIPLINE -> DisciplineTestHelper.shouldGlow(name);
			default -> false;
		};
	}

	/**
	 * called from the {@link de.hysky.skyblocker.skyblock.entity.MobGlow} class and checks the current challenge to see zombie outline color
	 *
	 * @return if the zombie should glow
	 */
	public static int getColor() {
		if (!Utils.isInCrimson() || !inArena) {
			return 0xF57738;
		}
		return switch (currentChallenge) {
			case FORCE -> ForceTestHelper.getColor();
			case DISCIPLINE -> DisciplineTestHelper.getColor();
			default -> 0xF57738;
		};
	}

	/**
	 * when a block is updated check the current challenge and send the packet to correct helper
	 *
	 * @param pos   the location of the updated block
	 * @param newState the state of the new block
	 */
	@SuppressWarnings("incomplete-switch")
	private static void onBlockUpdate(BlockPos pos, BlockState oldStatem, BlockState newState) {
		if (!Utils.isInCrimson() || !inArena) {
			return;
		}
		switch (currentChallenge) {
			case MASTERY -> MasteryTestHelper.onBlockUpdate(pos.immutable(), newState);
			case SWIFTNESS -> SwiftnessTestHelper.onBlockUpdate(pos.immutable(), newState);
		}
	}

	@SuppressWarnings("incomplete-switch")
	private static void onEntitySpawn(Entity entity, ClientLevel clientWorld) {
		if (!Utils.isInCrimson() || !inArena || CLIENT == null || CLIENT.player == null) {
			return;
		}
		// Check if within 50 blocks and 5 blocks vertically
		if (!entity.closerThan(CLIENT.player, 50, 5)) {
			return;
		}
		switch (currentChallenge) {
			case FORCE -> ForceTestHelper.onEntitySpawn(entity);
			case CONTROL -> ControlTestHelper.onEntitySpawn(entity);
			case TENACITY -> TenacityTestHelper.onEntitySpawn(entity);
		}
	}

	@SuppressWarnings("incomplete-switch")
	private static void onEntityDespawn(Entity entity, ClientLevel clientWorld) {
		if (!Utils.isInCrimson() || !inArena) {
			return;
		}
		switch (currentChallenge) {
			case FORCE -> ForceTestHelper.onEntityDespawn(entity);
			case TENACITY -> TenacityTestHelper.onEntityDespawn(entity);
		}
	}

	private static InteractionResult onEntityAttacked(Player playerEntity, Level world, InteractionHand hand, Entity entity, EntityHitResult entityHitResult) {
		if (!Utils.isInCrimson() || !inArena) {
			return InteractionResult.PASS;
		}
		if (currentChallenge == DojoChallenges.FORCE) {
			ForceTestHelper.onEntityAttacked(entity);
		}
		return InteractionResult.PASS;
	}

	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (!Utils.isInCrimson() || !inArena) {
			return;
		}
		if (currentChallenge == DojoChallenges.TENACITY) {
			TenacityTestHelper.onParticle(packet);
		}
	}

	@SuppressWarnings("incomplete-switch")
	private static void extractRendering(PrimitiveCollector collector) {
		if (!Utils.isInCrimson() || !inArena) {
			return;
		}
		switch (currentChallenge) {
			case FORCE -> ForceTestHelper.extractRendering(collector);
			case STAMINA -> StaminaTestHelper.extractRendering(collector);
			case MASTERY -> MasteryTestHelper.extractRendering(collector);
			case SWIFTNESS -> SwiftnessTestHelper.extractRendering(collector);
			case CONTROL -> ControlTestHelper.extractRendering(collector);
			case TENACITY -> TenacityTestHelper.extractRendering(collector);
		}
	}

}
