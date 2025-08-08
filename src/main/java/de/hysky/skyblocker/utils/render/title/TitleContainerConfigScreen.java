package de.hysky.skyblocker.utils.render.title;

import com.google.common.collect.ImmutableSet;
import de.hysky.skyblocker.config.HudConfigScreen;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.render.gui.AbstractWidget;
import de.hysky.skyblocker.utils.render.gui.EmptyWidget;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class TitleContainerConfigScreen extends HudConfigScreen {
	// ImmutableSet preserves insertion order
	private static final Set<Title> EXAMPLES = ImmutableSet.of(
			new Title(Text.literal("Test1").formatted(Formatting.RED)),
			new Title(Text.literal("Test23").formatted(Formatting.AQUA)),
			new Title(Text.literal("Testing1234").formatted(Formatting.DARK_GREEN))
	);

	protected TitleContainerConfigScreen() {
		this(null);
	}

	public TitleContainerConfigScreen(Screen parent) {
		super(Text.of("Title Container HUD Config"), parent, new EmptyWidget());
	}

	@Override
	protected void init() {
		super.init();
		// Only load config positions if they are not default
		if (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x >= 0 && SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y >= 0) {
			// Load the config positions here since #getConfigPos is used for resetting. This loads the config pos after HudConfigScreen#init calls HudConfigScreen#resetPos.
			widgets.getFirst().setPosition(SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y);
		}
		// Set the dimensions here or else Screen#textRenderer is null.
		updateWidgetDimensions();
	}

	@Override
	protected void renderWidget(DrawContext context, List<AbstractWidget> widgets, float delta) {
		super.renderWidget(context, widgets, delta);
		TitleContainer.render(context, EXAMPLES, widgets.getFirst().getX(), widgets.getFirst().getY(), delta);
		UIAndVisualsConfig.Direction direction = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction;
		UIAndVisualsConfig.Alignment alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment;
		context.drawCenteredTextWithShadow(textRenderer, "Press Q/E to change Alignment: " + alignment, width / 2, textRenderer.fontHeight * 2, Color.WHITE.getRGB());
		context.drawCenteredTextWithShadow(textRenderer, "Press R to change Direction: " + direction, width / 2, textRenderer.fontHeight * 3 + 5, Color.WHITE.getRGB());
		context.drawCenteredTextWithShadow(textRenderer, "Press +/- to change Scale", width / 2, textRenderer.fontHeight * 4 + 10, Color.WHITE.getRGB());
		context.drawCenteredTextWithShadow(textRenderer, "Right Click To Reset Position", width / 2, textRenderer.fontHeight * 5 + 15, Color.GRAY.getRGB());

		int selectionWidth = getSelectionWidth();
		int x1 = switch (alignment) {
			case LEFT -> widgets.getFirst().getX();
			case MIDDLE -> widgets.getFirst().getX() - selectionWidth / 2;
			case RIGHT -> widgets.getFirst().getX() - selectionWidth;
		};
		int y1 = widgets.getFirst().getY();
		int x2 = x1 + selectionWidth;
		int y2 = y1 + getSelectionHeight();

		context.drawHorizontalLine(x1, x2, y1, Color.RED.getRGB());
		context.drawHorizontalLine(x1, x2, y2, Color.RED.getRGB());
		context.drawVerticalLine(x1, y1, y2, Color.RED.getRGB());
		context.drawVerticalLine(x2, y1, y2, Color.RED.getRGB());
	}

	private void updateWidgetDimensions() {
		widgets.getFirst().setDimensions(getSelectionWidth(), getSelectionHeight());
	}

	private int getSelectionWidth() {
		return TitleContainer.getWidth(textRenderer, EXAMPLES);
	}

	private int getSelectionHeight() {
		return TitleContainer.getHeight(textRenderer, EXAMPLES);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch (keyCode) {
			case GLFW.GLFW_KEY_Q -> SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment = EnumUtils.cycle(SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment);
			case GLFW.GLFW_KEY_E -> SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment = EnumUtils.cycleBackwards(SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment);
			case GLFW.GLFW_KEY_R -> {
				SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction = EnumUtils.cycle(SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction);
				updateWidgetDimensions();
			}
			case GLFW.GLFW_KEY_EQUAL -> {
				SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale += 10;
				updateWidgetDimensions();
			}
			case GLFW.GLFW_KEY_MINUS -> {
				SkyblockerConfigManager.get().uiAndVisuals.titleContainer.titleContainerScale -= 10;
				updateWidgetDimensions();
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected int getWidgetXOffset(AbstractWidget widget) {
		return switch (SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment) {
			case LEFT -> 0;
			case MIDDLE -> -getSelectionWidth() / 2;
			case RIGHT -> -getSelectionWidth();
		};
	}

	@Override
	protected List<IntIntMutablePair> getConfigPos(SkyblockerConfig config) {
		// This gets the reset pos. The actual config pos is loaded in #init.
		return List.of(IntIntMutablePair.of(this.width / 2, (int) (this.height * 0.6)));
	}

	@Override
	protected void savePos(SkyblockerConfig config, List<AbstractWidget> widgets) {
		// Save to -1 if the widget is at the default position
		List<IntIntMutablePair> defaultPos = getConfigPos(config);
		config.uiAndVisuals.titleContainer.x = widgets.getFirst().getX() != defaultPos.getFirst().leftInt() ? widgets.getFirst().getX() : -1;
		config.uiAndVisuals.titleContainer.y = widgets.getFirst().getY() != defaultPos.getFirst().rightInt() ? widgets.getFirst().getY() : -1;
	}
}
