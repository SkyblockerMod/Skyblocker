package de.hysky.skyblocker.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * HUD render events that allow for proper layering between different HUD elements.
 *
 * @deprecated Use {@link net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback}.
 */
@Deprecated(forRemoval = true)
public class HudRenderEvents {
	/**
	 * Called after the hotbar, status bars, and experience bar have been rendered.
	 */
	@Deprecated(forRemoval = true)
	public static final Event<HudRenderStage> AFTER_MAIN_HUD = createEventForStage();

	/**
	 * Called before the {@link net.minecraft.client.gui.hud.ChatHud} is rendered.
	 */
	@Deprecated(forRemoval = true)
	public static final Event<HudRenderStage> BEFORE_CHAT = createEventForStage();

	/**
	 * Called after the entire HUD is rendered.
	 */
	@Deprecated(forRemoval = true)
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
	@Deprecated(forRemoval = true)
	@FunctionalInterface
	public interface HudRenderStage {
		/**
		 * Called sometime during a specific HUD render stage.
		 *
		 * @param context     The {@link DrawContext} instance
		 * @param tickCounter The {@link RenderTickCounter} instance
		 */
		void onRender(DrawContext context, RenderTickCounter tickCounter);
	}
}
