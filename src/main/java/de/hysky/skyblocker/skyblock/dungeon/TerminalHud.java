package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@RegisterWidget
public class TerminalHud extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.DUNGEON);
	private static final Supplier<DungeonsConfig.TerminalHud> CONFIG = () -> SkyblockerConfigManager.get().dungeons.terminalHud;
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static TerminalHud INSTANCE;

	public TerminalHud() {
		super(FunUtils.shouldEnableFun() ? Text.literal("P3 Guide") : Text.literal("Goldor Tasks"),
				Colors.RED, "terminal_hud");
		INSTANCE = this;
		Scheduler.INSTANCE.scheduleCyclic(this::updateFromScheduler, 50);
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return;
		CONFIG.get().enableTerminalHud = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return false;
		return CONFIG.get().enableTerminalHud;
	}

	@Override
	public boolean shouldRender(Location location) {
		if (!super.shouldRender(location)) return false;
		return GoldorWaypointsManager.isActive();
	}

	private boolean playerIsNearTerminal(PlayerEntity player, GoldorWaypointsManager.GoldorWaypoint waypoint) {
		return player.squaredDistanceTo(waypoint.centerPos) < 25;
	}

	private Text getStatusText(GoldorWaypointsManager.GoldorWaypoint waypoint) {
		if (waypoint.isEnabled()) {
			if (CONFIG.get().determineInProgressStatus && CLIENT.world != null) {
				if (CLIENT.world.getPlayers().stream().anyMatch(player -> playerIsNearTerminal(player, waypoint))) {
					return Text.literal("In Progress").formatted(Formatting.YELLOW);
				}
			}
			return Text.literal("Incomplete").formatted(Formatting.RED);
		} else {
			return Text.literal("Complete").formatted(Formatting.GREEN);
		}
	}

	public void updateFromScheduler() {
		if (!GoldorWaypointsManager.isActive() || !shouldRender(Utils.getLocation())) return;
		update();
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen && !GoldorWaypointsManager.isActive()) {
			addComponent(new PlainTextComponent(Text.literal("TEST")));
			return;
		}

		List<GoldorWaypointsManager.GoldorWaypoint> waypoints = GoldorWaypointsManager.getPhaseWaypoints();
		if (waypoints.isEmpty()) return;
		for (var waypoint : waypoints) {
			if (!waypoint.isEnabled() && !CONFIG.get().showCompletedTerminals) continue;

			Text displayText;
			if (CONFIG.get().showTerminalStatus) {
				displayText = waypoint.name.copy().append(" ").append(getStatusText(waypoint));
			} else {
				displayText = waypoint.name;
			}

			if (CONFIG.get().showTerminalIcons) {
				ItemStack icon = switch (waypoint.kind) {
					case TERMINAL -> Items.REPEATING_COMMAND_BLOCK.getDefaultStack();
					case DEVICE -> Items.GOLD_BLOCK.getDefaultStack();
					case LEVER -> Items.LEVER.getDefaultStack();
				};

				addComponent(Components.iconTextComponent(icon, displayText));
			} else {
				addComponent(new PlainTextComponent(displayText));
			}
		}

		if (CONFIG.get().includeGate) {
			if (GoldorWaypointsManager.isGateDestroyed() && !CONFIG.get().showCompletedTerminals) return;
			MutableText displayText = Text.literal("Gate");

			if (CONFIG.get().showTerminalStatus) {
				if (GoldorWaypointsManager.isGateDestroyed()) {
					displayText.append(" ").append(Text.literal("DESTROYED").formatted(Formatting.GREEN));
				} else {
					displayText.append(" ").append(Text.literal("Incomplete").formatted(Formatting.RED));
				}
			}

			if (CONFIG.get().showTerminalIcons) {
				addComponent(Components.iconTextComponent(Items.IRON_BARS.getDefaultStack(), displayText));
			} else {
				addComponent(new PlainTextComponent(displayText));
			}
		}
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("skyblocker.config.dungeons.terminalHud");
	}
}
