package me.xmrvizzy.skyblocker.skyblock.api;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.xmrvizzy.skyblocker.skyblock.api.records.PlayerProfiles;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

import java.text.DecimalFormat;

public class StatsCommand {
    public static void init(){
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("skyblocker")
                .then(ClientCommandManager.literal("debug")
                        .then(ClientCommandManager.literal("stats").then(ClientCommandManager.argument("username", StringArgumentType.string())
                                .then(ClientCommandManager.argument("cute name", StringArgumentType.string()).executes(context -> {
                                    new Thread(() -> {
                                        PlayerProfiles playerProfiles = ProfileUtils.getProfiles(StringArgumentType.getString(context, "username"));
                                        for (String profileId : playerProfiles.profiles().keySet()){
                                            if (playerProfiles.profiles().get(profileId).cuteName().equalsIgnoreCase(StringArgumentType.getString(context, "cute name"))){
                                                System.out.println(playerProfiles);
                                            }
                                        }
                                    }).start();
                                    return 1;
                                }))))));
    }
}
