package me.xmrvizzy.skyblocker;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class SkyblockerMod implements ClientModInitializer {
	public static final String NAMESPACE = "skyblocker";
	private static int TICKS = 0;

	@Override
	public void onInitializeClient() {
		SkyblockerConfig.init();
	}

	public static void onTick() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null) return;

		TICKS++;
		if (TICKS % 20 == 0) {
			if (client.world != null && !client.isInSingleplayer())
				Utils.sbChecker();
			TICKS = 0;
		}
	}
}