package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.FunUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
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
			if (CONFIG.get().showPlayerAtTerminal && CLIENT.world != null) {
				Optional<AbstractClientPlayerEntity> player = CLIENT.world.getPlayers().stream().filter(pl -> playerIsNearTerminal(pl, waypoint)).findFirst();
				if (player.isPresent()) {
					String playerName = player.get().getStringifiedName();
					return Text.literal(playerName.substring(0, Math.min(12, playerName.length()))).formatted(Formatting.YELLOW);
				}
			}
			return Text.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").formatted(Formatting.RED);
		} else {
			return Text.translatable("skyblocker.dungeons.terminalHud.completeStatus").formatted(Formatting.GREEN);
		}
	}

	public void updateFromScheduler() {
		if (CLIENT.currentScreen instanceof WidgetsConfigurationScreen && !GoldorWaypointsManager.isActive()) update();
		if (!GoldorWaypointsManager.isActive() || !shouldRender(Utils.getLocation())) return;
		update();
	}

	@Override
	public void updateContent() {
		if (CLIENT.currentScreen instanceof WidgetsConfigurationScreen && !GoldorWaypointsManager.isActive()) {
			Text status = Text.empty();
			if (CONFIG.get().showTerminalStatus) {
				status = Text.literal(" ").append(Text.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").formatted(Formatting.RED));
			}
			if (CONFIG.get().showTerminals) {
				for (int i = 0; i < 5; i++) {
					addComponent(new PlainTextComponent(Text.literal("Terminal #" + (i + 1)).append(status)));
				}
			}
			if (CONFIG.get().showDevice) {
				addComponent(new PlainTextComponent(Text.literal("Device").append(status)));
			}
			if (CONFIG.get().showLevers) {
				addComponent(new PlainTextComponent(Text.literal("Lever").append(status)));
				addComponent(new PlainTextComponent(Text.literal("Lever").append(status)));
			}
			if (CONFIG.get().showGate) {
				addComponent(new PlainTextComponent(Text.literal("Gate").append(status)));
			}
			return;
		}

		List<GoldorWaypointsManager.GoldorWaypoint> waypoints = GoldorWaypointsManager.getPhaseWaypoints();
		if (waypoints.isEmpty()) return;
		for (var waypoint : waypoints) {
			if (!waypoint.isEnabled() && !CONFIG.get().showTerminalStatus) continue;
			switch (waypoint.kind) {
				case TERMINAL -> { if (!CONFIG.get().showTerminals) continue; }
				case DEVICE -> { if (!CONFIG.get().showDevice) continue; }
				case LEVER -> { if (!CONFIG.get().showGate) continue; }
			}

			Text displayText;
			if (CONFIG.get().showTerminalStatus) {
				displayText = waypoint.name.copy().append(" ").append(getStatusText(waypoint));
			} else {
				displayText = waypoint.name;
			}

			addComponent(new PlainTextComponent(displayText));
		}

		if (CONFIG.get().showGate && GoldorWaypointsManager.getCurrentPhase() < 3) {
			if (GoldorWaypointsManager.isGateDestroyed() && !CONFIG.get().showTerminalStatus) return;
			MutableText displayText = Text.literal("Gate");

			if (CONFIG.get().showTerminalStatus) {
				if (GoldorWaypointsManager.isGateDestroyed()) {
					displayText.append(" ").append(Text.translatable("skyblocker.dungeons.terminalHud.destroyedStatus").formatted(Formatting.GREEN));
				} else {
					displayText.append(" ").append(Text.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").formatted(Formatting.RED));
				}
			}

			addComponent(new PlainTextComponent(displayText));
		}
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Goldor Tasks");
	}
}
