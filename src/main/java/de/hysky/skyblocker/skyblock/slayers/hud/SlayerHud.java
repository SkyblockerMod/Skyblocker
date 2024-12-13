package de.hysky.skyblocker.skyblock.slayers.hud;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SlayerHud {
	@Init
	public static void init() {
		HudRenderEvents.AFTER_MAIN_HUD.register((context, tickCounter) -> {
			if (shouldRender()) {
				SlayerHudWidget.INSTANCE.update();
				SlayerHudWidget.INSTANCE.render(context, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("hud").then(literal("slayers")
				.executes(Scheduler.queueOpenScreenCommand(() -> new SlayerHudConfigScreen(null)))))));
	}

	private static boolean shouldRender() {
		return SkyblockerConfigManager.get().slayers.slayerHud.enableHud && Utils.isOnSkyblock() && SlayerManager.isInSlayer() && !SlayerManager.getSlayerType().isUnknown() && !SlayerManager.getSlayerTier().isUnknown();
	}
}
