package me.xmrvizzy.skyblocker.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static boolean isSkyblock = false;
    public static boolean isDungeons = false;
    public static boolean isInjected = false;

    public static void sbChecker() {
        List<String> sidebar = getSidebar();
        if (sidebar == null) {
            isSkyblock = false;
            isDungeons = false;
            return;
        }
        String string = sidebar.toString();

        if (sidebar.isEmpty()) return;
        if (sidebar.get(sidebar.size() - 1).equals("www.hypixel.net")) {
            if (sidebar.get(0).contains("SKYBLOCK")){
                if(!isInjected){
                    isInjected = true;
                    ItemTooltipCallback.EVENT.register(PriceInfoTooltip::onInjectTooltip);
                }
                isSkyblock = true;

            }
            else isSkyblock = false;

            isDungeons = isSkyblock && string.contains("The Catacombs");

        } else {
            isSkyblock = false;
            isDungeons = false;
        }
    }

    public static String getLocation() {
        String location = null;
        List<String> sidebarLines = getSidebar();
        try{
            assert sidebarLines != null;
            for (String sidebarLine : sidebarLines) {
                if (sidebarLine.contains("⏣")) location = sidebarLine;
            }
            if (location == null) location = "Unknown";
            location = location.replace('⏣', ' ').strip();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return location;
    }
    public static double getPurse() {
        String purseString = null;
        double purse = 0;

        List<String> sidebarLines = getSidebar();
        try{
            assert sidebarLines != null;
            for (String sidebarLine : sidebarLines) {
                if (sidebarLine.contains("Piggy:")) purseString = sidebarLine;
                if (sidebarLine.contains("Purse:")) purseString = sidebarLine;
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
        try{
            assert sidebarLines != null;
            for (String sidebarLine : sidebarLines) {
                if (sidebarLine.contains("Bits")) bitsString = sidebarLine;
            }
            if (bitsString !=null) {
                bits = Integer.parseInt(bitsString.replaceAll("[^0-9.]", "").strip());
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return bits;
    }


    public static List<String> getSidebar() {
        try {
            assert MinecraftClient.getInstance().player != null;
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