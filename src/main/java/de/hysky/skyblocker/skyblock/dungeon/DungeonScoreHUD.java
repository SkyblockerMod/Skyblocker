package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

import java.util.Set;

@RegisterWidget
public class DungeonScoreHUD extends HudWidget {
	private static final Set<Location> AVAILABLE_LOCATIONS = Set.of(Location.DUNGEON);
	//This is 4+5 wide, needed to offset the extra width from bold numbers (3×1 wide) in S+ and the "+" (6 wide) so that it doesn't go off the screen if the score is S+ and the hud element is at the right edge of the screen
	private static final Text extraSpace = Text.literal(" ").append(Text.literal(" ").formatted(Formatting.BOLD));

	private static Text scoreText = getFormattedScoreText();
	private static float previousScale = -1f;

	public DungeonScoreHUD() {
		super("dungeon_score");
		Scheduler.INSTANCE.scheduleCyclic(this::updateFromScheduler, 5);
	}

	@Override
	public Set<Location> availableLocations() {
		return AVAILABLE_LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return;
		SkyblockerConfigManager.get().dungeons.dungeonScore.enableScoreHUD = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!AVAILABLE_LOCATIONS.contains(location)) return false;
		return SkyblockerConfigManager.get().dungeons.dungeonScore.enableScoreHUD;
	}

	private void updateFromScheduler() {
		if (!shouldRender(Utils.getLocation())) return;
		update();
	}

	@Override
	public void update() {
		scoreText = getFormattedScoreText();
		// Update widget size
		float scale = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		if (scale != previousScale) {
			previousScale = scale;
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			setDimensions((int) (textRenderer.getWidth(scoreText) * scale), (int) (textRenderer.fontHeight * scale));
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
			if (previousScale == -1) update();
		} else if (!DungeonScore.isDungeonStarted()) return;
		render(context, x, y);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("skyblocker.config.dungeons.dungeonScore");
	}

	public static void render(DrawContext context, int x, int y) {
		float scale = SkyblockerConfigManager.get().dungeons.dungeonScore.scoreScaling;
		Matrix3x2fStack matrixStack = context.getMatrices();
		matrixStack.pushMatrix();
		matrixStack.translate(x, y);
		matrixStack.scale(scale, scale);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, scoreText, 0, 0, 0xFFFFFFFF);
		matrixStack.popMatrix();
	}

	public static Text getFormattedScoreText() {
		return Text.translatable("skyblocker.dungeons.dungeonScore.scoreText", formatScore(DungeonScore.getScore()));
	}

	private static Text formatScore(int score) {
		if (score < 100) return Text.literal(String.format("%03d", score)).withColor(0xDC1A1A).append(Text.literal(" (D)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 160) return Text.literal(String.format("%03d", score)).withColor(0x4141FF).append(Text.literal(" (C)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 230) return Text.literal(String.format("%03d", score)).withColor(0x7FCC19).append(Text.literal(" (B)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 270) return Text.literal(String.format("%03d", score)).withColor(0x7F3FB2).append(Text.literal(" (A)").formatted(Formatting.GRAY)).append(extraSpace);
		if (score < 300) return Text.literal(String.format("%03d", score)).withColor(0xF1E252).append(Text.literal(" (S)").formatted(Formatting.GRAY)).append(extraSpace);
		return Text.literal("").append(Text.literal(String.format("%03d", score)).withColor(0xF1E252).formatted(Formatting.BOLD)).append(Text.literal(" (S+)").formatted(Formatting.GRAY));
	}
}
