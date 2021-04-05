package me.xmrvizzy.skyblocker.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.Attribute;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {
    public static boolean isSkyblock = false;
    public static boolean isDungeons = false;
    public static boolean isInjected = false;
    public static String parseActionBar(String msg) {
        String[] sections = msg.split(" {3,}");
        List<String> unused = new ArrayList<String>();

        if (msg.contains("❤") && !msg.contains("❈") && sections.length == 2) {
            Attribute.DEFENCE.set(0);
        }

        for (String section : sections) {
            String clear = Pattern.compile("[^0-9 /]").matcher(section).replaceAll("").trim();
            String[] split = clear.split("/");

            if (section.contains("❤")) {
                if (section.startsWith("§6")) split[0] = split[0].substring(1);
                Attribute.HEALTH.set(Integer.parseInt(split[0]));
                Attribute.MAX_HEALTH.set(Integer.parseInt(split[1]));
            } else if (section.contains("❈")) {
                Attribute.DEFENCE.set(Integer.parseInt(clear));
            } else if (section.contains("✎")) {
                Attribute.MANA.set(Integer.parseInt(split[0]));
                Attribute.MAX_MANA.set(Integer.parseInt(split[1]));
            } else {
                if (section.contains("Drill Fuel") && SkyblockerConfig.get().locations.dwarvenMines.enableDrillFuel) continue;
                unused.add(section);
            }
        }

        return String.join("   ", unused);
    }

    public static void sbChecker() {
        List<String> sidebar = getSidebar();
        String string = sidebar.toString();

        if (sidebar.isEmpty()) return;
        if (sidebar.get(sidebar.size() - 1).equals("www.hypixel.net")) {
            if (sidebar.get(0).contains("SKYBLOCK")){
                if(isInjected == false){
                    isInjected = true;
                    ItemTooltipCallback.EVENT.register(PriceInfoTooltip::onInjectTooltip);
                }
                isSkyblock = true;
            }
            else isSkyblock = false;

            if (isSkyblock && string.contains("The Catacombs")) isDungeons = true;
            else isDungeons = false;
        } else {
            isSkyblock = false;
            isDungeons = false;
        }
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