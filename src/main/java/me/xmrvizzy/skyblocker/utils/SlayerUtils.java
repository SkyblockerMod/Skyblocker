package me.xmrvizzy.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;

import java.util.List;

//TODO Slayer Packet system that can provide information about the current slayer boss, abstract so that different bosses can have different info
public class SlayerUtils {
    //TODO: Cache this, probably included in Packet system
    public static List<Entity> GetEntityArmorStands(Entity entity)
    {
        return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(1F, 2.5F, 1F), x-> x instanceof ArmorStandEntity && x.hasCustomName());
    }

    //Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
    public static Entity GetSlayerEntity()
    {
        if (MinecraftClient.getInstance().world != null) {
            var nearestEntities = MinecraftClient.getInstance().world.getEntities();
            for (var entity : nearestEntities)
            {
                if(entity.hasCustomName()) {
                    //Check if entity is Bloodfiend
                    if(entity.getCustomName().getString().contains("Bloodfiend"))
                    {
                        //Grab the players username
                        var siblings = MinecraftClient.getInstance().player.getDisplayName().getSiblings();
                        for (var sib : siblings)
                        {
                            var text = sib.getString();
                            if(!text.contains("[") && !text.isEmpty())
                            {
                                //Check all armor stands on the bloodfiend
                                for(var armorStand : GetEntityArmorStands(entity))
                                {
                                    //Check if the displayname contains the players username
                                    if(armorStand.getDisplayName().getString().contains(text))
                                    {
                                        return entity;
                                    }
                                }
                            }
                        }
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
                    if(line.contains("Slay the boss!"))
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
