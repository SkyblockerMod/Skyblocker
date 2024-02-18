package de.hysky.skyblocker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;

/**
 * HUD render events that allow for proper layering between different HUD elements.
 * This should always be preferred over Fabric's {@link net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback}.
 * 
 * Perhaps in the future this system could be PR'd to Fabric.
 */
public class HudRenderEvents {
	/**
	 * Called after the hotbar, status bars, and experience bar have been rendered.
	 */
	public static final Event<HudRenderStage> AFTER_MAIN_HUD = createEventForStage();

	/**
	 * Called before the {@link net.minecraft.client.gui.hud.ChatHud} is rendered.
	 */
	public static final Event<HudRenderStage> BEFORE_CHAT = createEventForStage();

	/**
	 * Called after the entire HUD is rendered.
	 */
	public static final Event<HudRenderStage> LAST = createEventForStage();

	private static Event<HudRenderStage> createEventForStage() {
		return EventFactory.createArrayBacked(HudRenderStage.class, listeners -> (context, tickDelta) -> {
			for (HudRenderStage listener : listeners) {
				listener.onRender(context, tickDelta);
			}
		});
	}

	/**
	 * @implNote Similar to Fabric's {@link net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback}
	 */
	@FunctionalInterface
	public interface HudRenderStage {
		/**
		 * Called sometime during a specific HUD render stage.
		 *
		 * @param drawContext The {@link DrawContext} instance
		 * @param tickDelta Progress for linearly interpolating between the previous and current game state
		 */
		void onRender(DrawContext context, float tickDelta);
	}
}
