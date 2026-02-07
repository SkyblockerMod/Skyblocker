package de.hysky.skyblocker.utils.render.title;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import java.util.LinkedHashSet;
import java.util.Set;

public class TitleContainer {
	private static final Identifier TITLE_CONTAINER = SkyblockerMod.id("title_container");
	private static final Minecraft CLIENT = Minecraft.getInstance();

	protected static final float RENDER_SCALE = 0.03f;

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
		HudElementRegistry.attachElementAfter(VanillaHudElements.TITLE_AND_SUBTITLE, TITLE_CONTAINER, TitleContainer::render);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("skyblocker")
				.then(ClientCommandManager.literal("hud")
						.then(ClientCommandManager.literal("titleContainer")
								.executes(Scheduler.queueOpenScreenCommand(TitleContainerConfigScreen::new))))));
	}

	/**
	 * Returns {@code true} if the title is currently shown.
	 *
	 * @param title the title to check
	 * @return whether the title is currently shown already
	 */
	public static boolean containsTitle(Title title) {
		return titles.contains(title);
	}

	/**
	 * Adds a title to be shown
	 *
	 * @param title the title to be shown
	 * @return whether the title is currently shown already
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
	 * @return whether the title is currently shown already
	 */
	public static boolean addTitle(Title title, int ticks) {
		if (addTitle(title)) {
			Scheduler.INSTANCE.schedule(() -> TitleContainer.removeTitle(title), ticks);
			return true;
		}
		return false;
	}

	/**
	 * Adds the title to {@link TitleContainer} and {@link #playNotificationSound() plays the notification sound} if the title is not in the {@link TitleContainer} already.
	 * No checking needs to be done on whether the title is in the {@link TitleContainer} already by the caller.
	 *
	 * @param title the title
	 * @return whether the title is currently shown already
	 */
	public static boolean addTitleAndPlaySound(Title title) {
		if (addTitle(title)) {
			playNotificationSound();
			return true;
		}
		return false;
	}

	/**
	 * Adds the title to {@link TitleContainer} for a set number of ticks and {@link #playNotificationSound() plays the notification sound} if the title is not in the {@link TitleContainer} already.
	 * No checking needs to be done on whether the title is in the {@link TitleContainer} already by the caller.
	 *
	 * @param title the title
	 * @param ticks the number of ticks the title will remain
	 * @return whether the title is currently shown already
	 */
	public static boolean addTitleAndPlaySound(Title title, int ticks) {
		if (addTitle(title, ticks)) {
			playNotificationSound();
			return true;
		}
		return false;
	}

	public static void playNotificationSound() {
		if (CLIENT.player != null) {
			CLIENT.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 100f, 0.1f);
		}
	}

	/**
	 * Stops showing a title
	 *
	 * @param title the title to stop showing
	 */
	public static void removeTitle(Title title) {
		titles.remove(title);
	}

	private static void render(GuiGraphics context, DeltaTracker tickCounter) {
		render(context, titles, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.x, SkyblockerConfigManager.get().uiAndVisuals.titleContainer.y, tickCounter.getGameTimeDeltaPartialTick(true));
	}

	protected static void render(GuiGraphics context, Set<Title> titles, int xPos, int yPos, float tickDelta) {
		UIAndVisualsConfig.TitleContainer config = SkyblockerConfigManager.get().uiAndVisuals.titleContainer;

		// Calculate Scale to use
		float scale = config.titleContainerScale * RENDER_SCALE;
		// Grab direction and alignment values
		UIAndVisualsConfig.Direction direction = config.direction;
		UIAndVisualsConfig.Alignment alignment = config.alignment;

		render(context, titles, xPos, yPos, tickDelta, scale, direction, alignment);
	}

	protected static void render(GuiGraphics context, Set<Title> titles, int xPos, int yPos, float tickDelta, float scale, UIAndVisualsConfig.Direction direction, UIAndVisualsConfig.Alignment alignment) {
		if (titles.isEmpty()) return;
		Font textRenderer = Minecraft.getInstance().font;

		// x/y refer to the starting position for the text
		// If xPos or yPos is negative, use the default values
		// If left or right aligned or middle aligned vertically, start at xPos, we will shift each text later
		float x = xPos >= 0 ? xPos : Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2f;
		// y always starts at yPos
		float y = yPos >= 0 ? yPos : Minecraft.getInstance().getWindow().getGuiScaledHeight() * 0.6f;

		// Calculate the width of combined text
		float totalWidth = getWidth(textRenderer, direction, scale, titles);
		if (alignment == UIAndVisualsConfig.Alignment.MIDDLE && direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
			// If middle aligned horizontally, start the xPosition at half of the width to the left.
			x = xPos - totalWidth / 2;
		}

		for (Title title : titles) {
			//Calculate which x the text should use
			float xTextLeft = x;
			if (alignment == UIAndVisualsConfig.Alignment.RIGHT) {
				//if right aligned we need the text position to be aligned on the right side.
				xTextLeft = x - textRenderer.width(title.getText()) * scale;
			} else if (direction == UIAndVisualsConfig.Direction.VERTICAL && alignment == UIAndVisualsConfig.Alignment.MIDDLE) {
				//if middle aligned we need the text position to be aligned in the middle.
				xTextLeft = x - (textRenderer.width(title.getText()) * scale) / 2;
			}

			//Start displaying the title at the correct position, not at the default position
			if (title.isDefaultPos()) {
				title.x = xTextLeft;
				title.y = y;
			}

			//Lerp the texts x and y variables
			title.x = Mth.lerp(tickDelta * 0.5F, title.x, xTextLeft);
			title.y = Mth.lerp(tickDelta * 0.5F, title.y, y);

			//Translate the matrix to the texts position and scale
			context.pose().pushMatrix();
			context.pose().translate(title.x, title.y);
			context.pose().scale(scale, scale);

			//Draw text
			context.drawString(textRenderer, title.getText(), 0, 0, CommonColors.WHITE);
			context.pose().popMatrix();

			//Calculate the x and y positions for the next title
			if (direction == UIAndVisualsConfig.Direction.HORIZONTAL) {
				if (alignment == UIAndVisualsConfig.Alignment.MIDDLE || alignment == UIAndVisualsConfig.Alignment.LEFT) {
					//Move to the right if middle or left aligned
					x += (textRenderer.width(title.getText()) + 10) * scale;
				} else if (alignment == UIAndVisualsConfig.Alignment.RIGHT) {
					//Move to the left if right aligned
					x -= (textRenderer.width(title.getText()) + 10) * scale;
				}
			} else {
				//Y always moves by the same amount if vertical
				y += (textRenderer.lineHeight + 1) * scale;
			}
		}
	}

	protected static int getWidth(Font textRenderer, UIAndVisualsConfig.Direction direction, float scale, Set<Title> titles) {
		return direction == UIAndVisualsConfig.Direction.HORIZONTAL ?
				(int) ((titles.stream().map(Title::getText).mapToInt(textRenderer::width).mapToDouble(width -> width + 10).sum() - 10) * scale) :
				(int) (titles.stream().map(Title::getText).mapToInt(textRenderer::width).max().orElse(0) * scale);
	}

	protected static int getHeight(Font textRenderer, UIAndVisualsConfig.Direction direction, float scale, Set<Title> titles) {
		return direction == UIAndVisualsConfig.Direction.HORIZONTAL ?
				(int) (textRenderer.lineHeight * scale) :
				(int) ((textRenderer.lineHeight + 1) * titles.size() * scale);
	}
}
