package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.world.updater.WorldUpdater;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DojoManager {

    private static final String START_MESSAGE = "§e[NPC] §eMaster Tao§f: Ahhh, here we go! Let's get you into the Arena.";
    private static final Pattern TEST_OF_PATTERN = Pattern.compile("\\s+Test of (\\w+) OBJECTIVES");
    private static final String CHALLENGE_FINISHED_REGEX = "\\s+CHALLENGE ((COMPLETED)|(FAILED))";

    protected enum DojoChallenges {
        NONE("none"),
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
    private static boolean inAreana = false;

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(DojoManager::onMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(DojoManager::render);
        Scheduler.INSTANCE.scheduleCyclic(DojoManager::update, 5);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());

    }



    private static void reset() {
        inAreana = false;
        currentChallenge = DojoChallenges.NONE;
        SwiftnessTestHelper.reset();
    }


    private static void onMessage(Text text, Boolean overlay) {

        if (Utils.getLocation() != Location.CRIMSON_ISLE || overlay) {
            return;
        }
        if (text.getString().equals(START_MESSAGE)) {
            inAreana = true;
            return;
        }
        if (!inAreana) {
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

    private static void update() {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inAreana) {
            return;
        }
    }

    private static void render(WorldRenderContext context) {
        if (Utils.getLocation() != Location.CRIMSON_ISLE || !inAreana) {
            return;
        }
        switch (currentChallenge) {
            case SWIFTNESS -> SwiftnessTestHelper.render(context);
            case TENACITY -> TenacityTestHelper.render(context);
        }
    }


}
