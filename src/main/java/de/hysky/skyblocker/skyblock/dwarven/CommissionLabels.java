package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.MiningConfig;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.CommsWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class CommissionLabels {

	private static final Map<String, MiningLocationLabel.DwarvenCategory> DWARVEN_LOCATIONS = Arrays.stream(MiningLocationLabel.DwarvenCategory.values()).collect(Collectors.toMap(MiningLocationLabel.DwarvenCategory::toString, Function.identity()));
	private static final List<MiningLocationLabel.DwarvenEmissaries> DWARVEN_EMISSARIES = Arrays.stream(MiningLocationLabel.DwarvenEmissaries.values()).toList();
	private static final Map<String, MiningLocationLabel.GlaciteCategory> GLACITE_LOCATIONS = Arrays.stream(MiningLocationLabel.GlaciteCategory.values()).collect(Collectors.toMap(MiningLocationLabel.GlaciteCategory::toString, Function.identity()));

	protected static List<MiningLocationLabel> activeWaypoints = new ArrayList<>();
	private static List<String> commissions = List.of();
	private static boolean commissionDone = false;


	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(CommissionLabels::render);
		Scheduler.INSTANCE.scheduleCyclic(CommissionLabels::tick, 20);
	}

	public static boolean enabled() {
		return SkyblockerConfigManager.get().mining.commissionWaypoints.mode != MiningConfig.CommissionWaypointMode.OFF;
	}

	private static void tick() {
		if (!Utils.isInDwarvenMines() || !enabled()) return;
		boolean foundCommissions = false;

		List<String> newCommissions = new ArrayList<>();
		boolean newCommissionDone = false;

		for (int i = 0; i < PlayerListManager.getPlayerList().size(); i++) {
			PlayerListEntry entry = PlayerListManager.getPlayerList().get(i);
			Text displayName = entry.getDisplayName();
			if (displayName == null) continue;
			String string = displayName.getString();
			if (foundCommissions) {
				if (!string.startsWith(" ")) break;
				string = string.substring(1);
				Matcher matcher = CommsWidget.COMM_PATTERN.matcher(string);
				if (matcher.matches()) {
					String name = matcher.group("name");
					String progress = matcher.group("progress");
					newCommissionDone |= "DONE".equals(progress);
					newCommissions.add(name);
				}
			} else if (string.startsWith("Commissions")) {
				foundCommissions = true;
			}
		}
		if (!newCommissions.equals(commissions) || newCommissionDone != commissionDone) {
			commissions = newCommissions;
			commissionDone = newCommissionDone;
			update(commissions, commissionDone);
		}
	}

	/**
	 * update the activeWaypoints when there is a change in commissions
	 *
	 * @param newCommissions the new commissions to get the waypoints from
	 * @param completed      if there is a commission completed
	 */
	public static void update(List<String> newCommissions, boolean completed) {
		if (!enabled()) return;

		MiningConfig.CommissionWaypointMode currentMode = SkyblockerConfigManager.get().mining.commissionWaypoints.mode;
		activeWaypoints.clear();
		String location = Utils.getIslandArea().substring(2);
		//find commission locations in glacite
		if (location.equals("Dwarven Base Camp") || location.equals("Glacite Tunnels") || location.equals("Glacite Mineshafts") || location.equals("Glacite Lake")) {
			if (currentMode != MiningConfig.CommissionWaypointMode.BOTH && currentMode != MiningConfig.CommissionWaypointMode.GLACITE) {
				return;
			}

			for (String commission : newCommissions) {
				for (Map.Entry<String, MiningLocationLabel.GlaciteCategory> glaciteLocation : GLACITE_LOCATIONS.entrySet()) {
					if (commission.contains(glaciteLocation.getKey())) {
						MiningLocationLabel.GlaciteCategory category = glaciteLocation.getValue();
						for (BlockPos gemstoneLocation : category.getLocations()) {
							activeWaypoints.add(new MiningLocationLabel(category, gemstoneLocation));
						}
					}
				}
			}
			//add base waypoint if enabled
			if (SkyblockerConfigManager.get().mining.commissionWaypoints.showBaseCamp) {
				activeWaypoints.add(new MiningLocationLabel(MiningLocationLabel.GlaciteCategory.CAMPFIRE, MiningLocationLabel.GlaciteCategory.CAMPFIRE.getLocations()[0]));
			}
			return;
		}
		//find commission locations in dwarven mines
		if (currentMode != MiningConfig.CommissionWaypointMode.BOTH && currentMode != MiningConfig.CommissionWaypointMode.DWARVEN) {
			return;
		}

		for (String commission : newCommissions) {
			for (Map.Entry<String, MiningLocationLabel.DwarvenCategory> dwarvenLocation : DWARVEN_LOCATIONS.entrySet()) {
				if (commission.contains(dwarvenLocation.getKey())) {
					MiningLocationLabel.DwarvenCategory category = dwarvenLocation.getValue();
					category.isTitanium = commission.contains("Titanium");
					activeWaypoints.add(new MiningLocationLabel(category, category.getLocation()));
				}
			}
		}
		//if there is a commission completed and enabled show emissary
		if (SkyblockerConfigManager.get().mining.commissionWaypoints.showEmissary && completed) {
			for (MiningLocationLabel.DwarvenEmissaries emissaries : DWARVEN_EMISSARIES) {
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
		// Only render in the dwarven mines and not the mineshaft.
		if (Location.DWARVEN_MINES != Utils.getLocation() || !enabled()) return;

		for (MiningLocationLabel MiningLocationLabel : activeWaypoints) {
			MiningLocationLabel.render(context);
		}
	}
}
