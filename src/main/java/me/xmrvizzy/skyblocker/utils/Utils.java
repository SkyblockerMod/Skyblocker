package me.xmrvizzy.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class Utils {
    public static boolean isSkyblock() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null && !client.isInSingleplayer()) {
            ScoreboardObjective scoreboard = client.world.getScoreboard().getObjectiveForSlot(1);
            if (scoreboard != null) {
                String name = "";
                for (Text text : scoreboard.getDisplayName().getSiblings()) {
                    name += text.getString();
                }
                if (name.contains("SKYBLOCK")) {
                    return true;
                }
            }
        }
        return false;
    }
}