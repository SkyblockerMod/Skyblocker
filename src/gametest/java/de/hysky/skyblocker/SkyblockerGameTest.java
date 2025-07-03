package de.hysky.skyblocker;

import de.hysky.skyblocker.debug.SnapshotDebug;
import de.hysky.skyblocker.skyblock.fancybars.FancyStatusBars;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
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
			// Set up the world
			singleplayer.getServer().runCommand("/fill 180 63 -13 184 67 -17 air");
			singleplayer.getServer().runCommand("/setblock 175 66 -4 minecraft:barrier");
			singleplayer.getServer().runCommand("/tp @a 175 67 -4");

			context.runOnClient(client -> {
				assert client.player != null;
				client.player.setYaw(180);
				client.player.setPitch(20);
			});

			// Save the current fancy status bars config and reset it to default
			var config = context.computeOnClient(client -> {
				var curConfig = FancyStatusBars.statusBars.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue().toJson())).toList();

				int[] counts = new int[7];
				FancyStatusBars.statusBars.forEach((type, bar) -> {
					bar.anchor = type.getDefaultAnchor();
					bar.gridY = type.getDefaultGridY();
					bar.gridX = counts[type.getDefaultAnchor().ordinal()]++;
				});
				FancyStatusBars.placeBarsInPositioner();
				FancyStatusBars.updatePositions(false);
				return curConfig;
			});

			// Take a screenshot and compare it
			singleplayer.getClientWorld().waitForChunksRender();
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("skyblocker_render").saveWithFileName("skyblocker_render"));

			// Restore the fancy status bars config
			context.runOnClient(client -> {
				config.forEach(pair -> FancyStatusBars.statusBars.get(pair.key()).loadFromJson(pair.value()));
				FancyStatusBars.placeBarsInPositioner();
				FancyStatusBars.updatePositions(false);
			});
		}
	}
}
