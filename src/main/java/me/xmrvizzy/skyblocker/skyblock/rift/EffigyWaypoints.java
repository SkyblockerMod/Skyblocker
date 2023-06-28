package me.xmrvizzy.skyblocker.skyblock.rift;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.Hash;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.NEURepo;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Colors;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EffigyWaypoints {
    private static final Logger LOGGER = LoggerFactory.getLogger(EffigyWaypoints.class);
    private static final List<BlockPos> effigies = new ArrayList<>();
    private static final List<BlockPos> unBrokenEffigies = new ArrayList<>();

    public static void init() {
        effigies.add(new BlockPos(150,79,95)); //Effigy 1
        effigies.add(new BlockPos(193,93,119)); //Effigy 2
        effigies.add(new BlockPos(235,110,147)); //Effigy 3
        effigies.add(new BlockPos(293,96,135)); //Effigy 4
        effigies.add(new BlockPos(262,99,94)); //Effigy 5
        effigies.add(new BlockPos(240,129,118)); //Effigy 6
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EffigyWaypoints::render);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("vampire")
                        .then(literal("logEffigies").executes(context -> {
                            if(!Utils.isOnSkyblock()) return 1;
                            if(!(Utils.getLocation().contains("Stillgore Château"))) return 1;
                            for (var eff : effigies)
                            {
                                LOGGER.info(eff.toString());
                            }
                            unBrokenEffigies.clear();
                            try {
                                ClientPlayerEntity client = MinecraftClient.getInstance().player;
                                if (client == null) return 1;
                                Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
                                ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
                                for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                                    Team team = scoreboard.getPlayerTeam(score.getPlayerName());
                                    if (team != null) {
                                        String line = team.getPrefix().getString() + team.getSuffix().getString();
                                        if(line.contains("Effigies"))
                                        {
                                            List<Text> newList = new ArrayList<Text>(team.getPrefix().getSiblings());
                                            newList.addAll(team.getSuffix().getSiblings());
                                            for (int i = 1; i < newList.size(); i++) {
                                                LOGGER.info((i-1) + newList.get(i).toString() + effigies.toArray()[i-1]);
                                            }
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                            }
                            return 1;
                        })))));

    }
    public static void updateEffigies()
    {
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
                    if(line.contains("Effigies"))
                    {
                        List<Text> newList = new ArrayList<Text>(team.getPrefix().getSiblings());
                        newList.addAll(team.getSuffix().getSiblings());
                        for (int i = 1; i < newList.size(); i++) {
                            if(newList.get(i).getStyle().getColor() == TextColor.parse("gray"))
                            {
                                unBrokenEffigies.add((BlockPos) effigies.toArray()[i-1]);
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
        }
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfig.get().locations.rift.effigyWaypoints && (Utils.getLocation().contains("Stillgore Château"))) {
            for (BlockPos effigy : unBrokenEffigies) {
                float[] colorComponents = DyeColor.RED.getColorComponents();
                RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, effigy, colorComponents, 0.5F);
                for (int i = 1; i < 6; i++) {
                    RenderHelper.renderFilledThroughWalls(context, new BlockPos(effigy.getX(), effigy.getY() - i, effigy.getZ()), colorComponents, 0.5F-(0.075F*i));
                }
            }
        }
    }
}