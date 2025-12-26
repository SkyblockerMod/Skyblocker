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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.CommonColors;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@RegisterWidget
public class TerminalHud extends ComponentBasedWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.DUNGEON);
	private static final Supplier<DungeonsConfig.TerminalHud> CONFIG = () -> SkyblockerConfigManager.get().dungeons.terminalHud;
	private static final Minecraft CLIENT = Minecraft.getInstance();
	public static TerminalHud INSTANCE;

	public TerminalHud() {
		super(FunUtils.shouldEnableFun() ? Component.literal("P3 Guide") : Component.literal("Goldor Tasks"),
				CommonColors.RED, "terminal_hud");
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

	private boolean playerIsNearTerminal(AbstractClientPlayer player, GoldorWaypointsManager.GoldorWaypoint waypoint) {
		return player.distanceToSqr(waypoint.centerPos) < 25;
	}

	private Component getStatusText(GoldorWaypointsManager.GoldorWaypoint waypoint) {
		if (waypoint.isEnabled()) {
			if (CONFIG.get().showPlayerAtTerminal && CLIENT.level != null) {
				Optional<AbstractClientPlayer> player = CLIENT.level.players().stream().filter(pl -> playerIsNearTerminal(pl, waypoint)).findFirst();
				if (player.isPresent()) {
					String playerName = player.get().getPlainTextName();
					return Component.literal(playerName.substring(0, Math.min(12, playerName.length()))).withStyle(ChatFormatting.YELLOW);
				}
			}
			return Component.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").withStyle(ChatFormatting.RED);
		} else {
			return Component.translatable("skyblocker.dungeons.terminalHud.completeStatus").withStyle(ChatFormatting.GREEN);
		}
	}

	public void updateFromScheduler() {
		if (CLIENT.screen instanceof WidgetsConfigurationScreen && !GoldorWaypointsManager.isActive()) update();
		if (!GoldorWaypointsManager.isActive() || !shouldRender(Utils.getLocation())) return;
		update();
	}

	@Override
	public void updateContent() {
		if (CLIENT.screen instanceof WidgetsConfigurationScreen && !GoldorWaypointsManager.isActive()) {
			MutableComponent status = Component.empty();
			if (CONFIG.get().showTerminalStatus) {
				status = Component.literal(" ").append(Component.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").withStyle(ChatFormatting.RED));
			}
			if (CONFIG.get().showTerminals) {
				for (int i = 0; i < 5; i++) {
					addComponent(new PlainTextComponent(Component.literal("Terminal #" + (i + 1)).append(status)));
				}
			}
			if (CONFIG.get().showDevice) {
				addComponent(new PlainTextComponent(Component.literal("Device").append(status)));
			}
			if (CONFIG.get().showLevers) {
				addComponent(new PlainTextComponent(Component.literal("Lever").append(status)));
				addComponent(new PlainTextComponent(Component.literal("Lever").append(status)));
			}
			if (CONFIG.get().showGate) {
				addComponent(new PlainTextComponent(Component.literal("Gate").append(status)));
			}
			return;
		}

		List<GoldorWaypointsManager.GoldorWaypoint> waypoints = GoldorWaypointsManager.getPhaseWaypoints();
		if (waypoints.isEmpty()) return;
		for (var waypoint : waypoints) {
			if (!waypoint.isEnabled() && !CONFIG.get().showTerminalStatus) continue;
			switch (waypoint.kind) {
				// @formatter:off
				case TERMINAL -> { if (!CONFIG.get().showTerminals) continue; }
				case DEVICE -> { if (!CONFIG.get().showDevice) continue; }
				case LEVER -> { if (!CONFIG.get().showGate) continue; }
				// @formatter:on
			}

			Component displayText;
			if (CONFIG.get().showTerminalStatus) {
				displayText = waypoint.name.copy().append(" ").append(getStatusText(waypoint));
			} else {
				displayText = waypoint.name;
			}

			addComponent(new PlainTextComponent(displayText));
		}

		if (CONFIG.get().showGate && GoldorWaypointsManager.getCurrentPhase() < 3) {
			if (GoldorWaypointsManager.isGateDestroyed() && !CONFIG.get().showTerminalStatus) return;
			MutableComponent displayText = Component.literal("Gate");

			if (CONFIG.get().showTerminalStatus) {
				if (GoldorWaypointsManager.isGateDestroyed()) {
					displayText.append(" ").append(Component.translatable("skyblocker.dungeons.terminalHud.destroyedStatus").withStyle(ChatFormatting.GREEN));
				} else {
					displayText.append(" ").append(Component.translatable("skyblocker.dungeons.terminalHud.incompleteStatus").withStyle(ChatFormatting.RED));
				}
			}

			addComponent(new PlainTextComponent(displayText));
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Goldor Tasks");
	}
}
