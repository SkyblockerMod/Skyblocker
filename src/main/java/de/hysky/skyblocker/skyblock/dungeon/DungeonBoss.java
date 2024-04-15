package de.hysky.skyblocker.skyblock.dungeon;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum DungeonBoss {
    NONE(-1, ""),
    BONZO(1, "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable."),
    SCARF(2, "[BOSS] Scarf: This is where the journey ends for you, Adventurers."),
    PROFESSOR(3, "[BOSS] The Professor: I was burdened with terrible news recently..."),
    THORN(4, "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!"),
    LIVID(5, "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows."),
    SADAN(6, "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"),
    MAXOR(7, "[BOSS] Maxor: WELL! WELL! WELL! LOOK WHO'S HERE!");

    private static final Map<String, DungeonBoss> BOSSES = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(DungeonBoss::message, Function.identity()));

    public final int floor;
    public final String message;

    DungeonBoss(int floor, String message) {
        this.floor = floor;
        this.message = message;
    }

    public int floor() {
        return floor;
    }

    public String message() {
        return message;
    }

    public boolean isInBoss() {
        return this != NONE;
    }

    public boolean isFloor(int floor) {
        return this.floor == floor;
    }

    public static DungeonBoss fromMessage(String message) {
        return BOSSES.getOrDefault(message, NONE);
    }
}
