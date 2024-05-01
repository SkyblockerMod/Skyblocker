package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DojoManager {

    private static final String START_MESSAGE = "§e[NPC] §eMaster Tao§f: Ahhh, here we go! Let's get you into the Arena.";
    private static final Pattern TEST_OF_PATTERN = Pattern.compile("\\s+Test of (\\w+) OBJECTIVES");
    private static final String CHALLENGE_FINISHED_REGEX = "\\s+CHALLENGE ((COMPLETED)|(FAILED))";


    protected enum DojoChallenges {
        NONE("none"),
        FORCE("Force"),
        MASTERY("Mastery"),
        DISCIPLINE("Discipline"),
        SWIFTNESS("Swiftness"),
        TENACITY("Tenacity");

        private final String name;

        DojoChallenges(String name) {
            this.name = name;
        }

        public static DojoChallenges from(String name) {
            return Arrays.stream(DojoChallenges.values()).filter(n -> name.equals(n.name)).findFirst().orElse(NONE);
        }
    }

    protected static DojoChallenges currentChallenge = DojoChallenges.NONE;
    public static boolean inArena = false;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(DojoManager::onMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DojoManager::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
        ClientEntityEvents.ENTITY_LOAD.register(DojoManager::onEntitySpawn);
        ClientEntityEvents.ENTITY_UNLOAD.register(DojoManager::onEntityDespawn);

    }

    private static void reset() {
        inArena = false;
        currentChallenge = DojoChallenges.NONE;
        SwiftnessTestHelper.reset();
        MasteryTestHelper.reset();
        TenacityTestHelper.reset();
        ForceTestHelper.reset();
    }

    private static void onMessage(Text text, Boolean overlay) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || overlay) {
            return;
        }
        if (text.getString().equals(START_MESSAGE)) {
            inArena = true;
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
        }
    }

    public static boolean shouldGlow(String name) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return false;
        }
        return switch (currentChallenge) {
            case DISCIPLINE -> DisciplineTestHelper.shouldGlow(name);
            case FORCE -> ForceTestHelper.shouldGlow(name);
            default -> false;
        };
    }

    public static int getColor() {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return 0xf57738;
        }
        return switch (currentChallenge) {
            case DISCIPLINE -> DisciplineTestHelper.getColor();
            case FORCE -> ForceTestHelper.getColor();
            default -> 0xf57738;
        };
    }

    public static void onBlockUpdate(BlockUpdateS2CPacket packet) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case SWIFTNESS -> SwiftnessTestHelper.onBlockUpdate(packet);
            case MASTERY -> MasteryTestHelper.onBlockUpdate(packet);
        }
    }
    private static void onEntitySpawn(Entity entity, ClientWorld clientWorld) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case TENACITY -> TenacityTestHelper.onEntitySpawn(entity);
            case FORCE -> ForceTestHelper.onEntitySpawn(entity);
        }
    }

    private static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case TENACITY -> TenacityTestHelper.onEntityDespawn(entity);
            case FORCE -> ForceTestHelper.onEntityDespawn(entity);
        }
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return;
        }
        if (currentChallenge == DojoChallenges.TENACITY) {
            TenacityTestHelper.onParticle(packet);
        }
    }

    private static void render(WorldRenderContext context) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inArena) {
            return;
        }
        switch (currentChallenge) {
            case FORCE -> ForceTestHelper.render(context);
            case SWIFTNESS -> SwiftnessTestHelper.render(context);
            case TENACITY -> TenacityTestHelper.render(context);
            case MASTERY -> MasteryTestHelper.render(context);
        }
    }


}
