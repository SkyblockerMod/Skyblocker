package de.hysky.skyblocker;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screen.Screen;

@SuppressWarnings("UnstableApiUsage")
public class SkyblockerConfigTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext clientGameTestContext) {
		Screen currentScreen = clientGameTestContext.computeOnClient(client -> client.currentScreen);
		clientGameTestContext.runOnClient(client -> client.setScreen(SkyblockerConfigManager.createGUI(client.currentScreen)));
		clientGameTestContext.waitTicks(20);
		clientGameTestContext.takeScreenshot("skyblocker_config"); // TODO assert the screenshot once we update to 1.21.11, which prevents the panorama from rotating
		clientGameTestContext.runOnClient(client -> client.setScreen(currentScreen));
	}
}
