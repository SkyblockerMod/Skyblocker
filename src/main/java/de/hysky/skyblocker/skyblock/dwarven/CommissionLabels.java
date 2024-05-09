package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommissionLabels {

    private static final Map<String, MiningLocationLabel.dwarvenCategory> DWARVEN_LOCATIONS = Arrays.stream(MiningLocationLabel.dwarvenCategory.values()).collect(Collectors.toMap(MiningLocationLabel.dwarvenCategory::toString, Function.identity()));
    private static final List<MiningLocationLabel.dwarvenEmissaries> DWARVEN_EMISSARYS = Arrays.stream(MiningLocationLabel.dwarvenEmissaries.values()).toList();
    private static final Map<String, MiningLocationLabel.glaciteCategory> GLACITE_LOCATIONS = Arrays.stream(MiningLocationLabel.glaciteCategory.values()).collect(Collectors.toMap(MiningLocationLabel.glaciteCategory::toString, Function.identity()));

    protected static List<MiningLocationLabel> activeWaypoints = new ArrayList<>();

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CommissionLabels::render);
    }

    /**
     * update the activeWaypoints when there is a change in commissions
     *
     * @param newCommissions the new commissions to get the waypoints from
     * @param completed      if there is a commission completed
     */
    protected static void update(List<String> newCommissions, boolean completed) {
        MiningConfig.CommissionWaypointMode currentMode = SkyblockerConfigManager.get().mining.commissionWaypoints.mode;
        if (currentMode == MiningConfig.CommissionWaypointMode.OFF) {
            return;
        }
        activeWaypoints.clear();
        String location = Utils.getIslandArea().substring(2);
        //find commission locations in glacite
        if (location.equals("Dwarven Base Camp") || location.equals("Glacite Tunnels") || location.equals("Glacite Mineshafts") || location.equals("Glacite Lake")) {
            if (currentMode != MiningConfig.CommissionWaypointMode.BOTH && currentMode != MiningConfig.CommissionWaypointMode.GLACITE) {
                return;
            }

            for (String commission : newCommissions) {
                for (Map.Entry<String, MiningLocationLabel.glaciteCategory> glaciteLocation : GLACITE_LOCATIONS.entrySet()) {
                    if (commission.contains(glaciteLocation.getKey())) {
                        MiningLocationLabel.glaciteCategory category = glaciteLocation.getValue();
                        for (BlockPos gemstoneLocation : category.getLocations()) {
                            activeWaypoints.add(new MiningLocationLabel(category, gemstoneLocation));
                        }
                    }
                }
            }
            //add base waypoint if enabled
            if (SkyblockerConfigManager.get().mining.commissionWaypoints.showBaseCamp) {
                activeWaypoints.add(new MiningLocationLabel(MiningLocationLabel.glaciteCategory.CAMPFIRE, MiningLocationLabel.glaciteCategory.CAMPFIRE.getLocations()[0]));
            }
            return;
        }
        //find commission locations in dwarven mines
        if (currentMode != MiningConfig.CommissionWaypointMode.BOTH && currentMode != MiningConfig.CommissionWaypointMode.DWARVEN) {
            return;
        }

        for (String commission : newCommissions) {
            for (Map.Entry<String, MiningLocationLabel.dwarvenCategory> dwarvenLocation : DWARVEN_LOCATIONS.entrySet()) {
                if (commission.contains(dwarvenLocation.getKey())) {
                    MiningLocationLabel.dwarvenCategory category = dwarvenLocation.getValue();
                    category.isTitanium = commission.contains("Titanium");
                    activeWaypoints.add(new MiningLocationLabel(category, category.getLocation()));
                }
            }
        }
        //if there is a commission completed and enabled show emissary
        if (SkyblockerConfigManager.get().mining.commissionWaypoints.showEmissary && completed) {
            for (MiningLocationLabel.dwarvenEmissaries emissaries : DWARVEN_EMISSARYS) {
                activeWaypoints.add(new MiningLocationLabel(emissaries, emissaries.getLocation()));
            }
        }
    }

    /**
     * render all the active waypoints
     *
     * @param context render context
     */
    private static void render(WorldRenderContext context) {
        if (!Utils.isInDwarvenMines() || SkyblockerConfigManager.get().mining.commissionWaypoints.mode == MiningConfig.CommissionWaypointMode.OFF) {
            return;
        }
        for (MiningLocationLabel MiningLocationLabel : activeWaypoints) {
            MiningLocationLabel.render(context);
        }
    }
}
