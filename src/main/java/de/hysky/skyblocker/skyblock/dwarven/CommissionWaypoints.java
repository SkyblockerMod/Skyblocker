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

public class CommissionWaypoints {

    private static final Map<String, MiningWaypoints.dwarvenCategory> DWARVEN_LOCATIONS = Arrays.stream(MiningWaypoints.dwarvenCategory.values()).collect(Collectors.toMap(MiningWaypoints.dwarvenCategory::toString, Function.identity()));
    private static final Map<String, MiningWaypoints.glaciteCategory> GLACITE_LOCATIONS = Arrays.stream(MiningWaypoints.glaciteCategory.values()).collect(Collectors.toMap(MiningWaypoints.glaciteCategory::toString, Function.identity()));


    protected static List<MiningWaypoints> activeWaypoints = new ArrayList<>();

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CommissionWaypoints::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

    protected static void update(List<DwarvenHud.Commission> newCommissions) {
        activeWaypoints.clear();
        String location = Utils.getIslandArea().substring(2);
        //find commission locations in glacite
        if (location.equals("Dwarven Base Camp") || location.equals("Glacite Tunnels")) {
            for (DwarvenHud.Commission commission : newCommissions) {
                String commissionName = commission.commission();
                for (Map.Entry<String, MiningWaypoints.glaciteCategory> glaciteLocation : GLACITE_LOCATIONS.entrySet()) {
                    if (commissionName.contains(glaciteLocation.getKey())) {
                        MiningWaypoints.glaciteCategory category = glaciteLocation.getValue();
                        for (BlockPos gemstoneLocation : category.getLocations()) {
                            activeWaypoints.add(new MiningWaypoints(category, Text.of(category.getName()), gemstoneLocation));
                        }
                    }
                }
            }
            return;
        }
        //find commission locations in dwarven mines
        for (DwarvenHud.Commission commission : newCommissions) {
            String commissionName = commission.commission();
            for (Map.Entry<String, MiningWaypoints.dwarvenCategory> dwarvenLocation : DWARVEN_LOCATIONS.entrySet()) {
                if (commissionName.contains(dwarvenLocation.getKey())) {
                    MiningWaypoints.dwarvenCategory category = dwarvenLocation.getValue();
                    activeWaypoints.add(new MiningWaypoints(category, Text.of(category.getName()), category.getLocation()));
                }
            }
        }
    }

    private static void render(WorldRenderContext context) {
        for (MiningWaypoints miningWaypoints : activeWaypoints) {
            if (miningWaypoints.shouldRender()) {
                miningWaypoints.render(context);
            }
        }
        
    }

    private static void reset() {
        activeWaypoints.clear();
    }
}
