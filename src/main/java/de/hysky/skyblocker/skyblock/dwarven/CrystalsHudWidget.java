package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Set;

@RegisterWidget
public class CrystalsHudWidget extends HudWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	protected static final Identifier MAP_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/crystals_map.png");
	private static final Identifier MAP_ICON = Identifier.ofVanilla("textures/map/decorations/player.png");
	private static final List<String> SMALL_LOCATIONS = List.of("Fairy Grotto", "King Yolkar", "Corleone", "Odawa", "Key Guardian", "Unknown");
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.CRYSTAL_HOLLOWS);

	private static CrystalsHudWidget instance = null;

	public static CrystalsHudWidget getInstance() {
		if (instance == null) new CrystalsHudWidget();
		return instance;
	}

	public CrystalsHudWidget() {
		super("hud_crystals");
		instance = this;
	}

	/**
	 * Converts an X and Z coordinate in the crystal hollow to an X and Y coordinate on the map.
	 *
	 * @param x the world X coordinate
	 * @param z the world Z coordinate
	 * @return a vector representing the x and y values
	 */
	protected static Vector2ic transformLocation(double x, double z) {
		//converts an x and z to a location on the map
		int transformedX = (int) ((x - 202) / 621 * 62);
		int transformedY = (int) ((z - 202) / 621 * 62);
		transformedX = Math.clamp(transformedX, 0, 62);
		transformedY = Math.clamp(transformedY, 0, 62);

		return new Vector2i(transformedX, transformedY);
	}

	/**
	 * Converts yaw to the cardinal directions that a player marker can be rotated towards on a map.
	 * The rotations of a marker follow this order: N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW.
	 * <br><br>
	 * Based off code from {@link net.minecraft.client.render.MapRenderer}
	 */
	private static float yaw2Cardinal(float yaw) {
		yaw += 180; //flip direction
		byte clipped = (byte) ((yaw + (yaw < 0.0 ? -8.0 : 8.0)) * 16.0 / 360.0);

		return (clipped * 360f) / 16f;
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		return location.equals(Location.CRYSTAL_HOLLOWS) && SkyblockerConfigManager.get().mining.crystalsHud.enabled;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!location.equals(Location.CRYSTAL_HOLLOWS)) return;
		SkyblockerConfigManager.get().mining.crystalsHud.enabled = enabled;
	}

	public void update() {
		if (CLIENT.player == null || CLIENT.getNetworkHandler() == null || !SkyblockerConfigManager.get().mining.crystalsHud.enabled) return;


		//get if the player is in the crystals
		float scale = SkyblockerConfigManager.get().mining.crystalsHud.mapScaling;
		w = h = (int) (62 * scale);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		float scale = SkyblockerConfigManager.get().mining.crystalsHud.mapScaling;

		//make sure the map renders infront of some stuff - improve this in the future with better layering (1.20.5?)
		//and set position and scale
		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x, y, 0f);
		matrices.scale(scale, scale, 0f);
		w = h = (int) (62 * scale);

		//draw map texture
		context.drawTexture(RenderLayer::getGuiTextured, MAP_TEXTURE, 0, 0, 0, 0, 62, 62, 62, 62);

		//if enabled add waypoint locations to map
		if (SkyblockerConfigManager.get().mining.crystalsHud.showLocations) {
			for (MiningLocationLabel waypoint : CrystalsLocationsManager.activeWaypoints.values()) {

			MiningLocationLabel.Category category = waypoint.category();
				Vector2ic renderPos = transformLocation(waypoint.centerPos.getX(), waypoint.centerPos.getZ());
				int locationSize = SkyblockerConfigManager.get().mining.crystalsHud.locationSize;

				if (SMALL_LOCATIONS.contains(category.getName())) {//if small location half the location size
					locationSize /= 2;
				}

				//fill square of size locationSize around the coordinates of the location
				context.fill(renderPos.x() - locationSize / 2, renderPos.y() - locationSize / 2, renderPos.x() + locationSize / 2, renderPos.y() + locationSize / 2, category.getColor());
			}
		}

		//draw player on map
		if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
			matrices.pop();
			return;
		}
		//get player location
		double playerX = CLIENT.player.getX();
		double playerZ = CLIENT.player.getZ();
		float playerRotation = CLIENT.player.getYaw(); //TODO make the transitions more rough?
		Vector2ic renderPos = transformLocation(playerX, playerZ);

		int renderX = renderPos.x() - 2;
		int renderY = renderPos.y() - 3;

		//position, scale and rotate the player marker
		matrices.translate(renderX, renderY, 0f);
		matrices.scale(0.75f, 0.75f, 0f);
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw2Cardinal(playerRotation)), 2.5f, 3.5f, 0);

		//draw marker on map
		context.drawTexture(RenderLayer::getGuiTextured, MAP_ICON, 0, 0, 2, 0, 5, 7, 8, 8);
		matrices.pop();
	}

	@Override
	public Text getDisplayName() {
		return Text.of("Crystals HUD");
	}
}
