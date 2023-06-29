package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class EffigyWaypoints {
    private static final List<BlockPos> effigies = new ArrayList<>();
    private static final List<BlockPos> unBrokenEffigies = new ArrayList<>();

    public static void init() {
        effigies.add(new BlockPos(150,79,95)); //Effigy 1
        effigies.add(new BlockPos(193,93,119)); //Effigy 2
        effigies.add(new BlockPos(235,110,147)); //Effigy 3
        effigies.add(new BlockPos(293,96,134)); //Effigy 4
        effigies.add(new BlockPos(262,99,94)); //Effigy 5
        effigies.add(new BlockPos(240,129,118)); //Effigy 6
    }
    public static void updateEffigies() {
        if(!SkyblockerConfig.get().slayer.vampireSlayer.enableEffigyWaypoints) return;
        if(!Utils.isOnSkyblock()) return;
        if(!(Utils.getLocation().contains("Stillgore Château"))) return;

        unBrokenEffigies.clear();
        try {
            ClientPlayerEntity client = MinecraftClient.getInstance().player;
            if (client == null) return;
            Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
            for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                Team team = scoreboard.getPlayerTeam(score.getPlayerName());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if(line.contains("Effigies")) {
                        List<Text> newList = new ArrayList<>(team.getPrefix().getSiblings());
                        newList.addAll(team.getSuffix().getSiblings());
                        for (int i = 1; i < newList.size(); i++) {
                            if(newList.get(i).getStyle().getColor() == TextColor.parse("gray")) {
                                unBrokenEffigies.add((BlockPos) effigies.toArray()[i-1]);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfig.get().slayer.vampireSlayer.enableEffigyWaypoints && (Utils.getLocation().contains("Stillgore Château"))) {
            updateEffigies();
            for (BlockPos effigy : unBrokenEffigies) {
                float[] colorComponents = DyeColor.RED.getColorComponents();
                if(SkyblockerConfig.get().slayer.vampireSlayer.compactEffigyWaypoints) {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, new BlockPos(effigy.getX(), effigy.getY() - 6, effigy.getZ()), colorComponents, 0.5F);
                }
                else {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, effigy, colorComponents, 0.5F);
                    for (int i = 1; i < 6; i++) {
                        RenderHelper.renderFilledThroughWalls(context, new BlockPos(effigy.getX(), effigy.getY() - i, effigy.getZ()), colorComponents, 0.5F - (0.075F * i));
                    }
                }
            }
        }
    }
}