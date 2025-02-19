package de.hysky.skyblocker.skyblock.garden;

import com.mojang.brigadier.arguments.FloatArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class GardenViewControl {

    public static MinecraftClient CLIENT = MinecraftClient.getInstance();

    @Init
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher
                .register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE).then(ClientCommandManager
                        .literal("garden")
                        .requires(source -> (Utils.getLocation() == Location.GARDEN))
                                .then(ClientCommandManager
                                        .literal("setyaw")
                                        .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg(-180, 180))
                                                .executes(ctx -> {
                                                    setYaw(FloatArgumentType.getFloat(ctx, "yaw"));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager
                                        .literal("setpitch")
                                        .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                                .executes(ctx -> {
                                                    setPitch(FloatArgumentType.getFloat(ctx, "pitch"));
                                                    return 1;
                                                })))
                                .then(ClientCommandManager
                                        .literal("setboth")
                                        .then(ClientCommandManager.argument("yaw", FloatArgumentType.floatArg(-180, 180))
                                        .then(ClientCommandManager.argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                                .executes(ctx -> {
                                                    setYaw(FloatArgumentType.getFloat(ctx, "yaw"));
                                                    setPitch(FloatArgumentType.getFloat(ctx, "pitch"));
                                                    return 1;
                                                }))))
                                ))));
    }

    public static void setYaw(float yaw) {
        assert CLIENT.player != null;
        CLIENT.player.setYaw(yaw);
    }

    public static void setPitch(float pitch) {
        assert CLIENT.player != null;
        CLIENT.player.setPitch(pitch);
    }
}
