package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
@RegisterWidget
public class TreeBreakProgressHud extends ComponentBasedWidget {

	private final MinecraftClient client = MinecraftClient.getInstance();
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GALATEA);
	private static TreeBreakProgressHud instance;

	public TreeBreakProgressHud() {
		super(Text.literal("Tree Break Progress").formatted(Formatting.GREEN, Formatting.BOLD),Formatting.GREEN.getColorValue(), "hud_treeprogress");
		instance = this;
		update();
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	public static TreeBreakProgressHud getInstance() {
		return instance;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location))
			return;
		SkyblockerConfigManager.get().foraging.galatea.enableTreeBreakProgress = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return availableLocations().contains(location) && SkyblockerConfigManager.get().foraging.galatea.enableTreeBreakProgress;
	}

	@Override
	public boolean shouldRender(Location location) {
		return super.shouldRender(location) && true;
	}

	@Override
	public void updateContent() {
		ClientWorld world = client.world;

		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.SPRUCE_WOOD, "", Formatting.GREEN, "FIG TREE 37%");
			return;
		}

		if (client.player == null || world == null)
			return;

		Stream<Entity> entityStream = StreamSupport.stream(world.getEntities().spliterator(), false);
		List<Entity> armorstands = entityStream
				.filter(entity -> entity instanceof ArmorStandEntity)
				.filter(entity -> entity.getCustomName() != null)
				.collect(Collectors.toList());
		Entity closest = armorstands.stream()
				.filter(entity -> {
					Text name = entity.getCustomName();
					return name != null
							&& (name.getString().contains("FIG TREE") || name.getString().contains("MANGROVE"));
				})
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)))
				.orElse(null);
		if (closest == null) return;
		ItemStack woodIcon = closest.getCustomName().getString().contains("FIG") ? Ico.SPRUCE_WOOD : Ico.MANGROVE_LOG;
		double x = closest.getPos().getX();
		double y = closest.getPos().getY();
		double z = closest.getPos().getZ();

		List<Entity> groupedArmorStands = armorstands.stream()
				.filter(e -> {
					Vec3d pos = e.getPos();
					return Math.abs(pos.x - x) < 0.1 && Math.abs(pos.y - y) < 2 && Math.abs(pos.z - z) < 0.1;
				})
				.collect(Collectors.toList());

		if (!groupedArmorStands.stream().anyMatch(armorStand -> {
			String name = armorStand.getName().getString();
			return name.contains(client.player.getName().getString());
		 }))
			return;
		addSimpleIcoText(woodIcon, "", Formatting.GREEN, closest.getCustomName().getString());

	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Tree Break Progress HUD");
	}

}
