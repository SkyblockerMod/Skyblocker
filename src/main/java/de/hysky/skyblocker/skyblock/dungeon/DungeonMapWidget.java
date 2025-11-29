package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

@RegisterWidget
public class DungeonMapWidget extends HudWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.DUNGEON);
	private static final Identifier MAP_BACKGROUND = Identifier.ofVanilla("textures/map/map_background.png");
	public static DungeonMapWidget INSTANCE;

	public DungeonMapWidget() {
		super("dungeon_map");
		INSTANCE = this;
		update();
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!availableLocations().contains(location)) return;
		SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!availableLocations().contains(location)) return false;
		return SkyblockerConfigManager.get().dungeons.dungeonMap.enableMap;
	}

	@Override
	public void update() {
		float size = 128 * SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling;
		setDimensions((int) size);
	}

	private void renderConfig(DrawContext context) {
		float scaling = SkyblockerConfigManager.get().dungeons.dungeonMap.mapScaling;
		int size = (int) (128 * scaling);
		context.drawTexture(RenderPipelines.GUI_TEXTURED, MAP_BACKGROUND, x, y, 0, 0, size, size, size, size);
	}

	private boolean shouldRender() {
		return DungeonScore.isDungeonStarted() && !DungeonManager.isInBoss();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		// Show the default map texture if outside of Dungeons
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen && !shouldRender()) {
			renderConfig(context);
			return;
		}
		if (!shouldRender()) return;
		DungeonsConfig.DungeonMap dungeonMap = SkyblockerConfigManager.get().dungeons.dungeonMap;
		DungeonMap.render(context, x, y, dungeonMap.mapScaling, dungeonMap.fancyMap);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("skyblocker.config.dungeons.map");
	}
}
