package me.xmrvizzy.skyblocker;

import net.fabricmc.api.ClientModInitializer;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;

public class SkyblockerMod implements ClientModInitializer {
	public static final String NAMESPACE = "skyblocker";
	private static SkyblockerMod INSTANCE;

	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		SkyblockerConfig.init();
	}

	public static SkyblockerMod get() {
		return INSTANCE;
	}
}