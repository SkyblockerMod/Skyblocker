package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@RegisterWidget
public class TreeBreakProgressHud extends ComponentBasedWidget {

	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.GALATEA);
	private static TreeBreakProgressHud instance;
	private static Int2ObjectMap<ArmorStand> armorstands = new Int2ObjectOpenHashMap<ArmorStand>();

	static {
			ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientWorld) -> armorstands.remove(entity.getId()));
	}
	public TreeBreakProgressHud() {
		super(Component.literal("Tree Break Progress").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), ChatFormatting.GREEN.getColor(), "hud_treeprogress");
		instance = this;
		update();
	}


	public static void onEntityUpdate(ArmorStand entity) {
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
		SkyblockerConfigManager.update(config -> config.foraging.galatea.enableTreeBreakProgress = enabled);
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return availableLocations().contains(location) && SkyblockerConfigManager.get().foraging.galatea.enableTreeBreakProgress;
	}

	@Override
	public boolean shouldRender(Location location) {
		return super.shouldRender(location) && isOwnTree(getClosestTree());
	}

	private ArmorStand getClosestTree() {
		if (CLIENT.player == null) return null;
		return armorstands.values().stream()
			.filter(entity -> {
				Component name = entity.getCustomName();
				if (name == null) return false;
				return name.getString().contains("FIG TREE") || name.getString().contains("MANGROVE TREE");
			})
			.min(Comparator.comparingDouble(e -> e.distanceToSqr(CLIENT.player)))
			.orElse(null);
	}

	private boolean isOwnTree(ArmorStand tree) {
		if (CLIENT.player == null) return false;
		if (tree == null) return false;
		Vec3 treePos = tree.position();

		List<ArmorStand> groupedArmorStands = armorstands.values().stream()
		.filter(e -> {
			Vec3 pos = e.position();
			return Math.abs(pos.x - treePos.x) < 0.1 &&
				Math.abs(pos.y - treePos.y) < 2 &&
				Math.abs(pos.z - treePos.z) < 0.1;
		})
		.toList();
		String playerName = CLIENT.player.getName().getString();

		return groupedArmorStands.stream().anyMatch(armorStand -> {
			String name = armorStand.getName().getString();
			return name.contains(playerName) || name.contains(" players");
		});
	}

	@Override
	public void updateContent() {
		ClientLevel world = CLIENT.level;
		ArmorStand closest;

		if (CLIENT.screen instanceof WidgetsConfigurationScreen) {
			addSimpleIcoText(Ico.STRIPPED_SPRUCE_WOOD, "Fig Tree ", ChatFormatting.GREEN, "37%");
			return;
		}

		if (CLIENT.player == null || world == null)
			return;
		closest = getClosestTree();
		if (closest == null || !isOwnTree(closest)) return;

		String closestName = closest.getName().getString();
		String treeName = closestName.contains("FIG") ? "Fig Tree" : "Mangrove Tree";
		ItemStack woodIcon = closestName.contains("FIG") ? Ico.STRIPPED_SPRUCE_WOOD : Ico.MANGROVE_LOG;
		addSimpleIcoText(woodIcon, treeName + " ", ChatFormatting.GREEN, closestName.replaceAll("[^0-9%]", ""));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Tree Break Progress HUD");
	}

}
