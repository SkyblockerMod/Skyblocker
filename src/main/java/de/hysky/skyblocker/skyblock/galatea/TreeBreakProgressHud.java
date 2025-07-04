package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Location;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RegisterWidget
public class TreeBreakProgressHud extends ComponentBasedWidget {

	private final MinecraftClient client = MinecraftClient.getInstance();
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GALATEA);
	private static TreeBreakProgressHud instance;
	private static Map<Integer, ArmorStandEntity> armorstands = new HashMap<>();
	private static ArmorStandEntity closest;

	static {
			System.out.println("Registering ENTITY_UNLOAD listener");
            ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientWorld) -> {
				System.out.println("ENTITY_UNLOAD triggered for: " + entity);
                armorstands.remove(entity.getId());
				if (entity.getCustomName() != null)
				System.out.println("removed" + entity.getCustomName().getString());
            System.out.println("TreeBreakProgressHud events registered after client start");
        });
    }
	public TreeBreakProgressHud() {
		super(Text.literal("Tree Break Progress").formatted(Formatting.GREEN, Formatting.BOLD), Formatting.GREEN.getColorValue(), "hud_treeprogress");
		instance = this;
		update();
	}


	public static void onEntityUpdate(ArmorStandEntity entity) {
		if (entity.getCustomName() != null) {
    		armorstands.put(entity.getId(), entity);
		}
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
		return super.shouldRender(location) && isOwnTree(getClosestTree());
	}

	private ArmorStandEntity getClosestTree() {
		if (client.player == null) return null;
		return armorstands.values().stream()
        	.filter(entity -> {
				Text name = entity.getCustomName();
				if (name == null) return false;
				return name.getString().contains("FIG TREE") || name.getString().contains("MANGROVE TREE");
			})
        	.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)))
        	.orElse(null);
	}

	private boolean isOwnTree(ArmorStandEntity tree) {
		if (client.player == null) return false;
		if (tree == null) return false;
		Vec3d treePos = tree.getPos();

		List<ArmorStandEntity> groupedArmorStands = armorstands.values().stream()
        .filter(e -> {
            Vec3d pos = e.getPos();
            return Math.abs(pos.x - treePos.x) < 0.1 &&
                   Math.abs(pos.y - treePos.y) < 2 &&
                   Math.abs(pos.z - treePos.z) < 0.1;
        })
        .collect(Collectors.toList());

		String playerName = client.player.getName().getString();

		return groupedArmorStands.stream().anyMatch(armorStand -> {
        	String name = armorStand.getName().getString();
        	return name.contains(playerName) || name.contains(" players");
    	});
	}

	@Override
	public void updateContent() {
		ClientWorld world = client.world;

		if (client.currentScreen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.STRIPPED_SPRUCE_WOOD, "Fig Tree ", Formatting.GREEN, "37%");
			return;
		}

		if (client.player == null || world == null)
			return;
		closest = getClosestTree();
		if (closest == null || !isOwnTree(closest)) return;

		String closestName = closest.getCustomName().getString();
		String treeName = closestName.contains("FIG") ? "Fig Tree" : "Mangrove Tree";
		ItemStack woodIcon = closestName.contains("FIG") ? Ico.STRIPPED_SPRUCE_WOOD : Ico.MANGROVE_LOG;
		addSimpleIcoText(woodIcon, treeName + " ", Formatting.GREEN, closestName.replaceAll("[^0-9%]", ""));
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("Tree Break Progress HUD");
	}

}
