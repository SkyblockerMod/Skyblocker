package de.hysky.skyblocker;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.Screen;

@SuppressWarnings("UnstableApiUsage")
public class SkyblockerConfigTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext clientGameTestContext) {
		Screen currentScreen = clientGameTestContext.computeOnClient(client -> client.screen);
		clientGameTestContext.runOnClient(client -> client.setScreen(SkyblockerConfigManager.createGUI(client.screen)));
		clientGameTestContext.waitTicks(20);
		clientGameTestContext.assertScreenshotEquals("skyblocker_config");
		clientGameTestContext.runOnClient(client -> client.setScreen(currentScreen));
	}
}
