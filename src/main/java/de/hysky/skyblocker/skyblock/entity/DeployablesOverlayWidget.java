package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterWidget
public class DeployablesOverlayWidget extends ComponentBasedWidget {

	private static Deployable current = null;
	private static final Pattern deployablePattern = Pattern.compile("^([A-Za-z ]+) (\\d{1,3})s$"); // e.g. "Radiant 20s"
	private static long lastWarn = 0;

	private static DeployablesOverlayWidget instance;

	private static final Map<String, DeployableType> deployableTypes = new Object2ObjectOpenHashMap<>();
	private static final Map<UUID, Deployable> currentActiveDeployables = new Object2ObjectOpenHashMap<>();

	private static final Comparator<Deployable> deployableComparator = Comparator.comparing((Deployable d) -> d.type.priority)
			.thenComparing(Deployable::timeLeft);

	static {
		for (DeployableType type : DeployableType.values()) { // grab all the custom skull models from the skull textures to display on players hud
			type.stack = new ItemStack(Items.PLAYER_HEAD);
			type.stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.of(UUID.randomUUID()),
					ItemUtils.propertyMapWithTexture(type.skullTexture)
			));

			deployableTypes.put(type.name, type);
		}
	}

	public DeployablesOverlayWidget() {
		super(Text.literal("Deployables"), Formatting.AQUA.getColorValue(), "Deployables Overlay");
		instance = this;
	}

	public static DeployablesOverlayWidget getInstance() { return instance;}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	@Override
	public void updateContent() {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(DeployableType.OVER_FLUX.stack, DeployableType.OVER_FLUX.name + " ", Formatting.GREEN, "45s");
			return;
		}

		if (!currentDeployableIsValid()) {
			recalculateBestDeployable();
		}

		if (current == null) {
			return;
		}

		addSimpleIcoText(current.type.stack, current.type.name + " ", getTimerColor(), current.timeLeft + "s"); // todo name colour

		if (current.timeLeft <= 1) {
			playExpiringWarning();
		}
	}

	@Override
	public boolean shouldRender(Location location) {
		if (super.shouldRender(location)) {
			if (SkyblockerConfigManager.get().uiAndVisuals.deployablesOverlay.enabled) {
				return currentDeployableIsValid();
			}
		}
		return false;
	}

	@Override
	public Set<Location> availableLocations() {
		return ALL_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		SkyblockerConfigManager.get().uiAndVisuals.deployablesOverlay.enabled = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return SkyblockerConfigManager.get().uiAndVisuals.deployablesOverlay.enabled;
	}

	public static void refreshDeployable(ArmorStandEntity armorStand) {
		var player = MinecraftClient.getInstance().player;
		if (player == null) {
			return;
		}
		if (!SkyblockerConfigManager.get().uiAndVisuals.deployablesOverlay.enabled) {
			return;
		}

		String name = armorStand.getName().getString();
		if (!name.endsWith("s")) { // nametag should end with 's' for num of seconds left
			return;
		}

		Matcher deployableMatcher = deployablePattern.matcher(name);
		if (!deployableMatcher.find()) {
			return;
		}


		DeployableType type = deployableTypes.get(deployableMatcher.group(1));
		int timeLeft = Integer.parseInt(deployableMatcher.group(2));

		Deployable deployable = new Deployable(type, timeLeft, System.currentTimeMillis(), armorStand.getPos());

		if (!deployable.isWithinDistance(player.getPos())) {
			return;
		}

		currentActiveDeployables.put(armorStand.getUuid(), deployable);

		// remove if last update more than 3 seconds ago - should account for lag
		currentActiveDeployables.values().removeIf(Deployable::dirty);

		recalculateBestDeployable();
	}

	private static void recalculateBestDeployable() {
		current = currentActiveDeployables.values().stream()
				.filter(d -> d.isWithinDistance(MinecraftClient.getInstance().player.getPos())).max(deployableComparator)
				.orElse(null);
	}

	private static boolean currentDeployableIsValid() {
		if (current == null || MinecraftClient.getInstance().player == null) {
			return false;
		}
		return !current.dirty() && current.isWithinDistance(MinecraftClient.getInstance().player.getPos());
	}


	public record Deployable(DeployableType type, int timeLeft, long lastUpdate, Vec3d pos) {
		public boolean isWithinDistance(Vec3d playerPos) {
			double distance = pos.distanceTo(playerPos);

			// todo test this in more detail, power orb bounds seem to be a square not euclidean distance
			// plasmaflux has slightly greater bounds, this should account for it.
			return (distance <= 18) || (type == DeployableType.PLASMA_FLUX && distance <= 20);
		}

		// if the deployable needs to be refreshed/removed from the map
		public boolean dirty() {
			return System.currentTimeMillis() - lastUpdate >= 2000 || timeLeft <= 0;
		}
	}

	public enum DeployableType {
		RADIANT("Radiant", 1, null, "ewogICJ0aW1lc3RhbXAiIDogMTYwNzQ0Nzk4NTQxNCwKICAicHJvZmlsZUlkIiA6ICI2OTBkMDM2OGM2NTE0OGM5ODZjMzEwN2FjMmRjNjFlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5emZyXzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzk0ZDBhMDY4ZWE1MGE5ZGUyY2VmMjNhOTJiY2E0YjM2NzhkMTJjYThhMTgxNWQxM2JlYWM5NGRmZDU1NzEyNSIKICAgIH0KICB9Cn0="),
		MANA_FLUX("Mana Flux", 2, null, "ewogICJ0aW1lc3RhbXAiIDogMTYyMTM0MjI5MzI5NiwKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyYWRhMWM3ZmNjOGNmMzVkZWZlYjk0NGE0ZjhmZmE5YTlkMjYwNTYwZmM3ZjVmNTgyNmRlODA4NTQzNTk2N2MiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ=="),
		OVER_FLUX("Overflux", 3, null, "ewogICJ0aW1lc3RhbXAiIDogMTcwODY4ODA2MjE4OCwKICAicHJvZmlsZUlkIiA6ICIzNzRhZGZlMjkyOWI0ZDBiODJmYmVjNTg2ZTI5ODk4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfR2xvenpfIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzIwZGU1ZTg5NzQ5NDAzNzU5MzRkMzJmNzFjOTFhZDJkNTcyOGQzOGU1MTY0N2RjYzhmMzkyMDZjMDk5YTU0YzIiCiAgICB9CiAgfQp9"),
		PLASMA_FLUX("Plasmaflux", 4, null, "ewogICJ0aW1lc3RhbXAiIDogMTYwMzE3NjQyODEzNSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNlZDRjZTIzOTMzZTY2ZTA0ZGYxNjA3MDY0NGY3NTk5ZWViNTUzMDdmN2VhZmU4ZDkyZjQwZmIzNTIwODYzYyIKICAgIH0KICB9Cn0");

		private final int priority;
		private ItemStack stack;
		private final String skullTexture;
		private final String name;

		DeployableType(String name, int priority, ItemStack stack, String skullTexture) {
			this.priority = priority;
			this.stack = stack;
			this.skullTexture = skullTexture;
			this.name = name;
		}
	}

	private static void playExpiringWarning() {
		if (System.currentTimeMillis() - lastWarn < 10_000) {
			return;  // need > 10 seconds since last warn
		}
		if (!SkyblockerConfigManager.get().uiAndVisuals.deployablesOverlay.warnWhenExpiring) {
			return;
		}

		lastWarn = System.currentTimeMillis();

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		player.playSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE);

		MinecraftClient.getInstance().inGameHud.setTitleTicks(0, 30, 0);
		MinecraftClient.getInstance().inGameHud.setTitle(Text.literal("Power orb expiring!").withColor(Colors.RED));
	}


	private static Formatting getTimerColor() {
		if (current.timeLeft > 30) {
			return Formatting.GREEN;
		} else if (current.timeLeft > 10) {
			return Formatting.YELLOW;
		} else {
			return Formatting.RED;
		}
	}

	private static Formatting getNameColor() {
		return switch (current.type) {
			case RADIANT -> Formatting.GREEN;
			case MANA_FLUX -> Formatting.BLUE;
			case OVER_FLUX -> Formatting.DARK_PURPLE;
			case PLASMA_FLUX -> Formatting.GOLD;
		};
	}
}
