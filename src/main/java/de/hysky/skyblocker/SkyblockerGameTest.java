package de.hysky.skyblocker;

import de.hysky.skyblocker.debug.SnapshotDebug;
import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.TestSingleplayerContext;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.WorldPresets;

@SuppressWarnings("UnstableApiUsage")
public class SkyblockerGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder().adjustSettings(worldCreator -> {
			worldCreator.setWorldType(new WorldCreator.WorldType(worldCreator.getGeneratorOptionsHolder().getCombinedRegistryManager().getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.DEFAULT)));
			worldCreator.setSeed(String.valueOf(SnapshotDebug.AARON_WORLD_SEED));
		}).create()) {
			singleplayer.getServer().runCommand("/fill 180 63 -13 184 67 -17 air");
			singleplayer.getServer().runCommand("/setblock 175 66 -4 minecraft:barrier");
			singleplayer.getServer().runCommand("/tp @a 175 67 -4");
			context.runOnClient(client -> {
				assert client.player != null;
				client.player.setYaw(180);
				client.player.setPitch(20);
			});
			singleplayer.getClientWorld().waitForChunksRender();
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("skyblocker_render").saveWithFileName("skyblocker_render"));
		}
	}
}
