package me.xmrvizzy.skyblocker.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.item.PriceInfoTooltip;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;

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
            for (int i = 0; i < sidebarLines.size(); i++) {
                if(sidebarLines.get(i).contains("⏣")) location = sidebarLines.get(i);
            }
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
            for (int i = 0; i < sidebarLines.size(); i++) {
                if(sidebarLines.get(i).contains("Piggy:")) purseString = sidebarLines.get(i);
                if(sidebarLines.get(i).contains("Purse:")) purseString = sidebarLines.get(i);
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
            for (int i = 0; i < sidebarLines.size(); i++) {
                if(sidebarLines.get(i).contains("Bits")) bitsString = sidebarLines.get(i);
            }
            bits = Integer.parseInt(bitsString.replaceAll("Bits:", "").strip());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return bits;
    }


    public static List<String> getSidebar() {
        List<String> lines = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return lines;

        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) return lines;
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(1);
        if (sidebar == null) return lines;

        Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(sidebar);
        List<ScoreboardPlayerScore> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (ScoreboardPlayerScore score : scores) {
            Team team = scoreboard.getPlayerTeam(score.getPlayerName());
            if (team == null) return lines;
            String text = team.getPrefix().getString() + team.getSuffix().getString();
            if (text.trim().length() > 0)
                lines.add(text);
        }

        lines.add(sidebar.getDisplayName().getString());
        Collections.reverse(lines);

        return lines;
    }
}