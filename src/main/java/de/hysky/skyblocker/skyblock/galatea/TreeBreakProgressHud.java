package de.hysky.skyblocker.skyblock.galatea;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.skyblock.tabhud.widget.ComponentBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

@RegisterWidget
public class TreeBreakProgressHud extends ComponentBasedWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Int2ObjectMap<ArmorStandEntity> ARMOR_STANDS = new Int2ObjectOpenHashMap<>();

	static {
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, clientWorld) -> ARMOR_STANDS.remove(entity.getId()));
	}
	public TreeBreakProgressHud() {
		super(Text.literal("Tree Break Progress").formatted(Formatting.GREEN, Formatting.BOLD), Formatting.GREEN.getColorValue(), new Information("hud_treeprogress", Text.literal("Tree Break Progress HUD"), location -> location == Location.GALATEA));
		update();
	}


	public static void onEntityUpdate(ArmorStandEntity entity) {
		if (entity.getCustomName() != null) {
			ARMOR_STANDS.put(entity.getId(), entity);
		}
	}

	@Override
	public boolean shouldUpdateBeforeRendering() {
		return true;
	}

	private ArmorStandEntity getClosestTree() {
		if (CLIENT.player == null) return null;
		return ARMOR_STANDS.values().stream()
				.filter(entity -> {
					Text name = entity.getCustomName();
					if (name == null) return false;
					return name.getString().contains("FIG TREE") || name.getString().contains("MANGROVE TREE");
				})
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(CLIENT.player)))
				.orElse(null);
	}

	private boolean isOwnTree(ArmorStandEntity tree) {
		if (CLIENT.player == null) return false;
		if (tree == null) return false;
		Vec3d treePos = tree.getEntityPos();

		List<ArmorStandEntity> groupedArmorStands = ARMOR_STANDS.values().stream()
				.filter(e -> {
					Vec3d pos = e.getEntityPos();
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
		ClientWorld world = CLIENT.world;
		ArmorStandEntity closest;

		if (CLIENT.player == null || world == null)
			return;
		closest = getClosestTree();
		if (closest == null || !isOwnTree(closest)) return;

		String closestName = closest.getName().getString();
		String treeName = closestName.contains("FIG") ? "Fig Tree" : "Mangrove Tree";
		ItemStack woodIcon = closestName.contains("FIG") ? Ico.STRIPPED_SPRUCE_WOOD : Ico.MANGROVE_LOG;
		addSimpleIcoText(woodIcon, treeName + " ", Formatting.GREEN, closestName.replaceAll("[^0-9%]", ""));
	}

	@Override
	protected List<Component> getConfigComponents() {
		Text txt = simpleEntryText("37%", "Fig Tree ", Formatting.GREEN);
		return List.of(Components.iconTextComponent(Ico.STRIPPED_SPRUCE_WOOD, txt));
	}

	@Override
	public boolean shouldRender() {
		return super.shouldRender() && isOwnTree(getClosestTree());
	}
}
