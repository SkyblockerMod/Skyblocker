package me.xmrvizzy.skyblocker.utils;

import me.xmrvizzy.skyblocker.skyblock.rift.HealingMelonIndicator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.realms.Request;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
public class SlayerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlayerUtils.class);

    public static Entity GetSlayerEntity()
    {
        if (MinecraftClient.getInstance().world != null) {
            var nearestEntities = MinecraftClient.getInstance().world.getEntities();
            for (var entity : nearestEntities)
            {
                if(entity.hasCustomName()) {
                    if(entity.getCustomName().getString().contains("Bloodfiend"))
                    {
                        return entity;
                    }
                }
            }
        }
        return null;
    }

    public static boolean getIsInSlayer()
    {
        try {
            ClientPlayerEntity client = MinecraftClient.getInstance().player;
            if (client == null) return false;
            Scoreboard scoreboard = MinecraftClient.getInstance().player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
            for (ScoreboardPlayerScore score : scoreboard.getAllPlayerScores(objective)) {
                Team team = scoreboard.getPlayerTeam(score.getPlayerName());
                if (team != null) {
                    String line = team.getPrefix().getString() + team.getSuffix().getString();
                    if(line.contains("Slay the Boss!"))
                    {
                        return true;
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }
}
