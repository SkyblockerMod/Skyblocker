package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static boolean isOnSkyblock = false;
    public static boolean isInDungeons = false;
    public static boolean isInjected = false;

    public static void sbChecker() {
        MinecraftClient client = MinecraftClient.getInstance();
        List<String> sidebar;

        if (client.world == null || client.isInSingleplayer() || (sidebar = getSidebar()) == null) {
            isOnSkyblock = false;
            isInDungeons = false;
            return;
        }
        String string = sidebar.toString();

        if (sidebar.isEmpty()) return;
        if (sidebar.get(0).contains("SKYBLOCK") || sidebar.get(0).contains("SKIBLOCK")) {
            if (!isOnSkyblock) {
                if (!isInjected) {
                    isInjected = true;
                    ItemTooltipCallback.EVENT.register(PriceInfoTooltip::onInjectTooltip);
                }
                SkyblockEvents.JOIN.invoker().onSkyblockJoin();
                isOnSkyblock = true;
            }
        } else if (isOnSkyblock) {
            SkyblockEvents.LEAVE.invoker().onSkyblockLeave();
            isOnSkyblock = false;
            isInDungeons = false;
        }
        isInDungeons = isOnSkyblock && string.contains("The Catacombs");
    }

    public static String getLocation() {
        String location = null;
        List<String> sidebarLines = getSidebar();
        try {
            if( sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("⏣")) location = sidebarLine;
                }
                if (location == null) location = "Unknown";
                location = location.replace('⏣', ' ').strip();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return location;
    }

    public static double getPurse() {
        String purseString = null;
        double purse = 0;

        List<String> sidebarLines = getSidebar();
        try {

            if (sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("Piggy:")) purseString = sidebarLine;
                    if (sidebarLine.contains("Purse:")) purseString = sidebarLine;
                }
            }
            if (purseString != null) purse = Double.parseDouble(purseString.replaceAll("[^0-9.]", "").strip());
            else purse = 0;

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return purse;
    }

    public static int getBits() {
        int bits = 0;
        String bitsString = null;
        List<String> sidebarLines = getSidebar();
        try {
            if (sidebarLines != null) {
                for (String sidebarLine : sidebarLines) {
                    if (sidebarLine.contains("Bits")) bitsString = sidebarLine;
                }
            }
            if (bitsString != null) {
                bits = Integer.parseInt(bitsString.replaceAll("[^0-9.]", "").strip());
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return bits;
    }


    public static List<String> getSidebar() {
        try {
            ClientPlayerEntity client = MinecraftClient.getInstance().player;
            if (client == null) return Collections.emptyList();
            Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
            List<String> lines = new ArrayList<>();
            for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                Team team = scoreboard.getPlayerTeam(score.getPlayerName());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if (line.trim().length() > 0) {
                        String formatted = Formatting.strip(line);
                        lines.add(formatted);
                    }
                }
            }

            if (objective != null) {
                lines.add(objective.getDisplayName().getString());
                Collections.reverse(lines);
            }
            return lines;
        } catch (NullPointerException e) {
            return null;
        }
    }
}