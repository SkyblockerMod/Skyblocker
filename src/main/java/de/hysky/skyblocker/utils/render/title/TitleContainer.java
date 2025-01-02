package de.hysky.skyblocker.utils.render.title;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedHashSet;
import java.util.Set;

public class TitleContainer {
	/**
	 * The set of titles which will be rendered.
	 *
	 * @see #containsTitle(Title)
	 * @see #addTitle(Title)
	 * @see #addTitle(Title, int)
	 * @see #removeTitle(Title)
	 */
	private static final Set<Title> titles = new LinkedHashSet<>();

	@Init
	public static void init() {
		HudRenderEvents.BEFORE_CHAT.register(TitleContainer::render);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("hud")
						.then(ClientCommandManager.literal("titleContainer")
								.executes(Scheduler.queueOpenScreenCommand(TitleContainerConfigScreen::new))))));
	}

	/**
	 * Returns {@code true} if the title is currently shown.
	 *
	 * @param title the title to check
	 * @return whether the title in currently shown
	 */
	public static boolean containsTitle(Title title) {
		return titles.contains(title);
	}

	/**
	 * Adds a title to be shown
	 *
	 * @param title the title to be shown
	 * @return whether the title is already currently being shown
	 */
	public static boolean addTitle(Title title) {
		if (titles.add(title)) {
			title.resetPos();
			return true;
		}
		return false;
	}

	/**
	 * Adds a title to be shown for a set number of ticks
	 *
	 * @param title the title to be shown
	 * @param ticks the number of ticks to show the title
	 * @return whether the title is already currently being shown
	 */
	public static boolean addTitle(Title title, int ticks) {
		if (addTitle(title)) {
			Scheduler.INSTANCE.schedule(() -> TitleContainer.removeTitle(title), ticks);
			return true;
		}
		return false;
	}

	/**
	 * Stops showing a title
	 *
	 * @param title the title to stop showing
	 */
	public static void removeTitle(Title title) {
		titles.remove(title);
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		render(context, titles, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y, tickCounter.getTickDelta(true));
	}

	protected static void render(DrawContext context, Set<Title> titles, int xPos, int yPos, float tickDelta) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		// Calculate Scale to use
		float scale = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.getRenderScale();
		// Grab direction and alignment values
		UIAndVisualsConfig.Direction direction = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction;
		UIAndVisualsConfig.Alignment alignment = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.alignment;

		// x/y refer to the starting position for the text
		// If left or right aligned or middle aligned vertically, start at xPos, we will shift each text later
		float x = xPos;
		// y always starts at yPos
		float y = yPos;

		// Calculate the width of combined text
		float totalWidth = getWidth(textRenderer, titles);
		if (alignment == UIAndVisualsConfig.Alignment.MIDDLE && direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
			// If middle aligned horizontally, start the xPosition at half of the width to the left.
			x = xPos - totalWidth / 2;
		}

		for (Title title : titles) {
			//Calculate which x the text should use
			float xTextLeft = x;
			if (alignment == UIAndVisualsConfig.Alignment.RIGHT) {
				//if right aligned we need the text position to be aligned on the right side.
				xTextLeft = x - textRenderer.getWidth(title.getText()) * scale;
			} else if (direction == UIAndVisualsConfig.Direction.VERTICAL && alignment == UIAndVisualsConfig.Alignment.MIDDLE) {
				//if middle aligned we need the text position to be aligned in the middle.
				xTextLeft = x - (textRenderer.getWidth(title.getText()) * scale) / 2;
			}

			//Start displaying the title at the correct position, not at the default position
			if (title.isDefaultPos()) {
				title.x = xTextLeft;
				title.y = y;
			}

			//Lerp the texts x and y variables
			title.x = MathHelper.lerp(tickDelta * 0.5F, title.x, xTextLeft);
			title.y = MathHelper.lerp(tickDelta * 0.5F, title.y, y);

			//Translate the matrix to the texts position and scale
			context.getMatrices().push();
			context.getMatrices().translate(title.x, title.y, 0);
			context.getMatrices().scale(scale, scale, scale);

			//Draw text
			context.drawTextWithShadow(textRenderer, title.getText(), 0, 0, 0xFFFFFF);
			context.getMatrices().pop();

			//Calculate the x and y positions for the next title
			if (direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
				if (alignment == UIAndVisualsConfig.Alignment.MIDDLE || alignment == UIAndVisualsConfig.Alignment.LEFT) {
					//Move to the right if middle or left aligned
					x += (textRenderer.getWidth(title.getText()) + 10) * scale;
				} else if (alignment == UIAndVisualsConfig.Alignment.RIGHT) {
					//Move to the left if right aligned
					x -= (textRenderer.getWidth(title.getText()) + 10) * scale;
				}
			} else {
				//Y always moves by the same amount if vertical
				y += (textRenderer.fontHeight + 1) * scale;
			}
		}
	}

	protected static int getWidth(TextRenderer textRenderer, Set<Title> titles) {
		float scale = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.getRenderScale();
		return SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction == UIAndVisualsConfig.Direction.HORIZONTAL ?
				(int) ((titles.stream().map(Title::getText).mapToInt(textRenderer::getWidth).mapToDouble(width -> width + 10).sum() - 10) * scale) :
				(int) (titles.stream().map(Title::getText).mapToInt(textRenderer::getWidth).max().orElse(0) * scale);
	}

	protected static int getHeight(TextRenderer textRenderer, Set<Title> titles) {
		float scale = SkyblockerConfigManager.get().uiAndVisuals.titleContainer.getRenderScale();
		return SkyblockerConfigManager.get().uiAndVisuals.titleContainer.direction == UIAndVisualsConfig.Direction.HORIZONTAL ?
				(int) (textRenderer.fontHeight * scale) :
				(int) ((textRenderer.fontHeight + 1) * titles.size() * scale);
	}
}
