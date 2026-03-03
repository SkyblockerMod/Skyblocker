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
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class TitleContainerConfigScreen extends HudConfigScreen {
	public static final float MIN_TITLE_SCALE = 30f;
	public static final float MAX_TITLE_SCALE = 140f;
	// ImmutableSet preserves insertion order
	private static final Set<Title> EXAMPLES = ImmutableSet.of(
			new Title(Component.literal("Test1").withStyle(ChatFormatting.RED)),
			new Title(Component.literal("Test23").withStyle(ChatFormatting.AQUA)),
			new Title(Component.literal("Testing1234").withStyle(ChatFormatting.DARK_GREEN))
	);

	private UIAndVisualsConfig.Direction direction = UIAndVisualsConfig.Direction.VERTICAL;
	private UIAndVisualsConfig.Alignment alignment = UIAndVisualsConfig.Alignment.MIDDLE;
	private float titleContainerScale;
	private float renderScale;

	protected TitleContainerConfigScreen() {
		this(null);
	}

	public TitleContainerConfigScreen(@Nullable Screen parent) {
		super(Component.nullToEmpty("Title Container HUD Config"), parent, new EmptyWidget());
	}

	@Override
	protected void init() {
		super.init();

		// Get the unpatched config options
		SkyblockerConfigManager.update(fullConfig -> {
			UIAndVisualsConfig.TitleContainer config = fullConfig.uiAndVisuals.titleContainer;
			direction = config.direction;
			alignment = config.alignment;
			titleContainerScale = config.titleContainerScale;

			// Only load config positions if they are not default
			int x = config.x, y = config.y;
			if (x >= 0 && y >= 0) {
				// Load the config positions here since #getConfigPos is used for resetting. This loads the config pos after HudConfigScreen#init calls HudConfigScreen#resetPos.
				widgets.getFirst().setPosition(x, y);
			}

			// Set the dimensions here or else Screen#textRenderer is null.
			updateWidgetDimensions();
		});
	}

	@Override
	protected void renderWidget(GuiGraphics context, List<AbstractWidget> widgets, float delta) {
		super.renderWidget(context, widgets, delta);
		TitleContainer.render(context, EXAMPLES, widgets.getFirst().getX(), widgets.getFirst().getY(), delta, renderScale, direction, alignment);
		context.drawCenteredString(font, "Press Q/E to change Alignment: " + alignment, width / 2, font.lineHeight * 2, Color.WHITE.getRGB());
		context.drawCenteredString(font, "Press R to change Direction: " + direction, width / 2, font.lineHeight * 3 + 5, Color.WHITE.getRGB());
		context.drawCenteredString(font, "Press +/- to change Scale", width / 2, font.lineHeight * 4 + 10, Color.WHITE.getRGB());
		context.drawCenteredString(font, "Right Click To Reset Position", width / 2, font.lineHeight * 5 + 15, Color.GRAY.getRGB());

		int selectionWidth = getSelectionWidth();
		int x1 = switch (alignment) {
			case LEFT -> widgets.getFirst().getX();
			case MIDDLE -> widgets.getFirst().getX() - selectionWidth / 2;
			case RIGHT -> widgets.getFirst().getX() - selectionWidth;
		};
		int y1 = widgets.getFirst().getY();
		int x2 = x1 + selectionWidth;
		int y2 = y1 + getSelectionHeight();

		context.hLine(x1, x2, y1, Color.RED.getRGB());
		context.hLine(x1, x2, y2, Color.RED.getRGB());
		context.vLine(x1, y1, y2, Color.RED.getRGB());
		context.vLine(x2, y1, y2, Color.RED.getRGB());
	}

	private void updateWidgetDimensions() {
		renderScale = titleContainerScale * TitleContainer.RENDER_SCALE;
		widgets.getFirst().setDimensions(getSelectionWidth(), getSelectionHeight());
	}

	private int getSelectionWidth() {
		return TitleContainer.getWidth(font, direction, renderScale, EXAMPLES);
	}

	private int getSelectionHeight() {
		return TitleContainer.getHeight(font, direction, renderScale, EXAMPLES);
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		switch (input.key()) {
			case GLFW.GLFW_KEY_Q -> alignment = EnumUtils.cycle(alignment);
			case GLFW.GLFW_KEY_E -> alignment = EnumUtils.cycleBackwards(alignment);
			case GLFW.GLFW_KEY_R -> {
				direction = EnumUtils.cycle(direction);
				updateWidgetDimensions();
			}
			case GLFW.GLFW_KEY_EQUAL -> {
				titleContainerScale = Math.min(MAX_TITLE_SCALE, titleContainerScale + 10);
				updateWidgetDimensions();
			}
			case GLFW.GLFW_KEY_MINUS -> {
				titleContainerScale = Math.max(MIN_TITLE_SCALE, titleContainerScale - 10);
				updateWidgetDimensions();
			}
		}
		return super.keyPressed(input);
	}

	@Override
	protected int getWidgetXOffset(AbstractWidget widget) {
		return switch (alignment) {
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
	protected void savePos(SkyblockerConfig fullConfig, List<AbstractWidget> widgets) {
		// Save to -1 if the widget is at the default position
		List<IntIntMutablePair> defaultPos = getConfigPos(fullConfig);
		UIAndVisualsConfig.TitleContainer config = fullConfig.uiAndVisuals.titleContainer;
		config.x = widgets.getFirst().getX() != defaultPos.getFirst().leftInt() ? widgets.getFirst().getX() : -1;
		config.y = widgets.getFirst().getY() != defaultPos.getFirst().rightInt() ? widgets.getFirst().getY() : -1;
		config.direction = direction;
		config.alignment = alignment;
		config.titleContainerScale = titleContainerScale;
	}
}
