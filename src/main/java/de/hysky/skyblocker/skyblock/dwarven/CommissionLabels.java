package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommissionLabels {

    private static final Map<String, MiningLocationLabels.dwarvenCategory> DWARVEN_LOCATIONS = Arrays.stream(MiningLocationLabels.dwarvenCategory.values()).collect(Collectors.toMap(MiningLocationLabels.dwarvenCategory::toString, Function.identity()));
    private static final Map<String, MiningLocationLabels.glaciteCategory> GLACITE_LOCATIONS = Arrays.stream(MiningLocationLabels.glaciteCategory.values()).collect(Collectors.toMap(MiningLocationLabels.glaciteCategory::toString, Function.identity()));

    protected static List<MiningLocationLabels> activeWaypoints = new ArrayList<>();

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CommissionLabels::render);
    }

    protected static void update(List<String> newCommissions) {
        System.out.println(newCommissions);
        activeWaypoints.clear();
        String location = Utils.getIslandArea().substring(2);
        //find commission locations in glacite
        if (location.equals("Dwarven Base Camp") || location.equals("Glacite Tunnels")) {
            for (String commission : newCommissions) {
                for (Map.Entry<String, MiningLocationLabels.glaciteCategory> glaciteLocation : GLACITE_LOCATIONS.entrySet()) {
                    if (commission.contains(glaciteLocation.getKey())) {
                        MiningLocationLabels.glaciteCategory category = glaciteLocation.getValue();
                        for (BlockPos gemstoneLocation : category.getLocations()) {
                            activeWaypoints.add(new MiningLocationLabels(category, gemstoneLocation));
                        }
                    }
                }
            }
            return;
        }
        //find commission locations in dwarven mines
        for (String commission : newCommissions) {
            for (Map.Entry<String, MiningLocationLabels.dwarvenCategory> dwarvenLocation : DWARVEN_LOCATIONS.entrySet()) {
                if (commission.contains(dwarvenLocation.getKey())) {
                    MiningLocationLabels.dwarvenCategory category = dwarvenLocation.getValue();
                    category.isTitanium = commission.contains("Titanium");
                    activeWaypoints.add(new MiningLocationLabels(category, category.getLocation()));
                }
            }
        }
    }

    private static void render(WorldRenderContext context) {
        if (!Utils.isInDwarvenMines()) {
            return;
        }
        for (MiningLocationLabels MiningLocationLabels : activeWaypoints) {
            MiningLocationLabels.render(context);
        }
    }
}
