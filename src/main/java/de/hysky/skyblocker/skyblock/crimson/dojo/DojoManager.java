package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DojoManager {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
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
        ClientReceiveMessageEvents.GAME.register(DojoManager::onMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DojoManager::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
        ClientEntityEvents.ENTITY_LOAD.register(DojoManager::onEntitySpawn);
        ClientEntityEvents.ENTITY_UNLOAD.register(DojoManager::onEntityDespawn);
        AttackEntityCallback.EVENT.register(DojoManager::onEntityAttacked);
        Scheduler.INSTANCE.scheduleCyclic(DojoManager::update, 3);
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
    private static void onMessage(Text text, Boolean overlay) {
        if (!Utils.isInCrimson() || overlay) {
            return;
        }
        if (Objects.equals(Formatting.strip(text.getString()), START_MESSAGE)) {
            inArena = true;
            //update the players ping
            getPing();
            return;
        }
        if (!inArena) {
            return;
        }
        if (text.getString().matches(CHALLENGE_FINISHED_REGEX)) {
            reset();
            return;
        }

        //look for a message saying what challenge is starting if one has not already been found
        if (currentChallenge != DojoChallenges.NONE) {
            return;
        }
        Matcher nextChallenge = TEST_OF_PATTERN.matcher(text.getString());
        if (nextChallenge.matches()) {
            currentChallenge = DojoChallenges.from(nextChallenge.group(1));
            if (!currentChallenge.enabled.test(true)) {
                currentChallenge = DojoChallenges.NONE;
            }
        }
    }

    private static void getPing() {
        ClientPlayNetworkHandler networkHandler = CLIENT.getNetworkHandler();
        if (networkHandler != null) {
            networkHandler.sendPacket(new QueryPingC2SPacket(Util.getMeasuringTimeMs()));
        }
    }

    public static void onPingResult(long ping) {
        DojoManager.ping = ping;
    }

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
            return 0xf57738;
        }
        return switch (currentChallenge) {
            case FORCE -> ForceTestHelper.getColor();
            case DISCIPLINE -> DisciplineTestHelper.getColor();
            default -> 0xf57738;
        };
    }

    /**
     * when a block is updated check the current challenge and send the packet to correct helper
     *
     * @param pos   the location of the updated block
     * @param state the state of the new block
     */
    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (!Utils.isInCrimson() || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case MASTERY -> MasteryTestHelper.onBlockUpdate(pos, state);
            case SWIFTNESS -> SwiftnessTestHelper.onBlockUpdate(pos, state);
        }
    }

    private static void onEntitySpawn(Entity entity, ClientWorld clientWorld) {
        if (!Utils.isInCrimson() || !inArena || CLIENT == null || CLIENT.player == null) {
            return;
        }
        // Check if within 50 blocks and 5 blocks vertically
        if (entity.squaredDistanceTo(CLIENT.player) > 2500 || Math.abs(entity.getBlockY() - CLIENT.player.getBlockY()) > 5) {
            return;
        }
        switch (currentChallenge) {
            case FORCE -> ForceTestHelper.onEntitySpawn(entity);
            case CONTROL -> ControlTestHelper.onEntitySpawn(entity);
            case TENACITY -> TenacityTestHelper.onEntitySpawn(entity);
        }
    }

    private static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
        if (!Utils.isInCrimson() || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case FORCE -> ForceTestHelper.onEntityDespawn(entity);
            case TENACITY -> TenacityTestHelper.onEntityDespawn(entity);
        }
    }

    private static ActionResult onEntityAttacked(PlayerEntity playerEntity, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (!Utils.isInCrimson() || !inArena) {
            return ActionResult.PASS;
        }
        if (currentChallenge == DojoChallenges.FORCE) {
            ForceTestHelper.onEntityAttacked(entity);
        }
        return ActionResult.PASS;
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (!Utils.isInCrimson() || !inArena) {
            return;
        }
        if (currentChallenge == DojoChallenges.TENACITY) {
            TenacityTestHelper.onParticle(packet);
        }
    }

    private static void render(WorldRenderContext context) {
        if (!Utils.isInCrimson() || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case FORCE -> ForceTestHelper.render(context);
            case STAMINA -> StaminaTestHelper.render(context);
            case MASTERY -> MasteryTestHelper.render(context);
            case SWIFTNESS -> SwiftnessTestHelper.render(context);
            case CONTROL -> ControlTestHelper.render(context);
            case TENACITY -> TenacityTestHelper.render(context);
        }
    }

}
