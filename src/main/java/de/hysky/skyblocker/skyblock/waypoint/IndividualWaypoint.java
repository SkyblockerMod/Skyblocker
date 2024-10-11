package de.hysky.skyblocker.skyblock.waypoint;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class IndividualWaypoint extends NamedWaypoint {

    private static IndividualWaypoint waypoint;

    @Init
    public static void init() {

        ClientTickEvents.END_CLIENT_TICK.register(IndividualWaypoint::onTick);

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> { if (waypoint != null) waypoint.render(context); });

        ClientPlayConnectionEvents.JOIN.register((ignore, ignore2, ignore3) -> waypoint = null);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
                        .then(ClientCommandManager.literal("waypoints")
                                .then(ClientCommandManager.literal("individual")
                                        .then(ClientCommandManager.argument("x", IntegerArgumentType.integer(Integer.MIN_VALUE))
                                                .then(ClientCommandManager.argument("y", IntegerArgumentType.integer(Integer.MIN_VALUE))
                                                        .then(ClientCommandManager.argument("z", IntegerArgumentType.integer(Integer.MIN_VALUE))
                                                                .then(ClientCommandManager.argument("area", StringArgumentType.greedyString())
                                                                        .executes(context -> setWaypoint(
                                                                                IntegerArgumentType.getInteger(context, "x"),
                                                                                IntegerArgumentType.getInteger(context, "y"),
                                                                                IntegerArgumentType.getInteger(context, "z"),
                                                                                StringArgumentType.getString(context, "area")
                                                                        ))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
        ));
    }

    public IndividualWaypoint(BlockPos pos, Text name, float[] colorComponents) {
        super(pos, name, colorComponents, 0.5f, true);
    }

    private static int setWaypoint(int x, int y, int z, String area) {
        String waypointName = area != null && !area.isEmpty() ? area : "Waypoint";

        Text waypointDisplay;
        if (waypointName.charAt(0) == '⏣') {
            waypointDisplay = Text.literal("⏣").formatted(Formatting.DARK_PURPLE)
                    .append(Text.literal(waypointName.substring(1)).formatted(Formatting.AQUA));
        } else {
            waypointDisplay = Text.literal(waypointName).formatted(Formatting.AQUA);
        }

        waypoint = new IndividualWaypoint(new BlockPos(x, y, z), waypointDisplay, ColorUtils.getFloatComponents(Color.GREEN.getRGB()));
        return Command.SINGLE_SUCCESS;
    }

    private static void onTick(MinecraftClient c) {
        if (waypoint != null && c.player.getPos().distanceTo(Vec3d.ofCenter(waypoint.pos)) <= 8) {
            waypoint = null;
        }
    }

}
