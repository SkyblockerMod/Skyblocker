package de.hysky.skyblocker;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.util.Property;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.EditableScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.utils.Location;

@SuppressWarnings("UnstableApiUsage")
public class HudWidgetConfigPersistenceTest implements FabricClientGameTest {
	private static final String COMMISSIONS = "commissions";
	private static final String CRYSTALS = "hud_crystals";

	private static final PositionRule NORMAL_CLOSE_POSITION = position(41, 43);
	private static final PositionRule SWITCHED_LOCATION_POSITION = position(47, 53);
	private static final PositionRule SWITCHED_LAYER_POSITION = position(59, 61);
	private static final PositionRule SHUTDOWN_POSITION = position(67, 71);

	@Override
	public void runTest(ClientGameTestContext context) {
		FileSnapshot originalConfig = snapshotConfigFile();
		Screen previousScreen = context.computeOnClient(client -> client.gui.screen());
		try {
			context.runOnClient(client -> {
				WidgetManager.loadConfig();
				verifyNormalClose(client, previousScreen);
				verifyLocationAndLayerSwitches(client, previousScreen);
				verifyRemovalDuringShutdown(client, previousScreen);
				verifySaveFailureDoesNotChangeConfig();
			});
		} finally {
			context.runOnClient(client -> client.gui.setScreen(previousScreen));
			restoreConfigFile(originalConfig);
			context.runOnClient(_ -> WidgetManager.loadConfig());
		}
	}

	private static void verifyNormalClose(Minecraft client, Screen previousScreen) {
		WidgetsConfigurationScreen screen = openEditor(client, Location.DWARVEN_MINES, previousScreen);
		setPosition(screen, COMMISSIONS, NORMAL_CLOSE_POSITION);
		client.gui.setScreen(previousScreen);

		WidgetManager.loadConfig();
		assertPosition(Location.DWARVEN_MINES, COMMISSIONS, NORMAL_CLOSE_POSITION);
	}

	private static void verifyLocationAndLayerSwitches(Minecraft client, Screen previousScreen) {
		WidgetsConfigurationScreen screen = openEditor(client, Location.DWARVEN_MINES, previousScreen);
		setPosition(screen, COMMISSIONS, SWITCHED_LOCATION_POSITION);
		screen.setCurrentLocation(Location.CRYSTAL_HOLLOWS);
		screen.setCurrentScreenLayer(WidgetManager.ScreenLayer.MAIN_TAB);
		screen.setCurrentScreenLayer(WidgetManager.ScreenLayer.HUD);
		setPosition(screen, CRYSTALS, SWITCHED_LAYER_POSITION);
		client.gui.setScreen(previousScreen);

		WidgetManager.loadConfig();
		assertPosition(Location.DWARVEN_MINES, COMMISSIONS, SWITCHED_LOCATION_POSITION);
		assertPosition(Location.CRYSTAL_HOLLOWS, CRYSTALS, SWITCHED_LAYER_POSITION);
	}

	private static void verifyRemovalDuringShutdown(Minecraft client, Screen previousScreen) {
		WidgetsConfigurationScreen screen = openEditor(client, Location.CRYSTAL_HOLLOWS, previousScreen);
		setPosition(screen, CRYSTALS, SHUTDOWN_POSITION);
		if (client.gui.screen() != screen) {
			throw new AssertionError("HUD editor is not the current screen");
		}
		screen.removed();

		WidgetManager.loadConfig();
		assertPosition(Location.CRYSTAL_HOLLOWS, CRYSTALS, SHUTDOWN_POSITION);
		client.gui.setScreen(previousScreen);
	}

	private static void verifySaveFailureDoesNotChangeConfig() {
		WidgetConfig configBeforeSave = getWidgetConfig(Location.CRYSTAL_HOLLOWS, CRYSTALS);
		FileSnapshot persistedConfig = snapshotConfigFile();
		AtomicBoolean loggedFailure = new AtomicBoolean();
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		Logger logger = loggerContext.getLogger(WidgetManager.LOGGER.getName());
		AbstractAppender appender = new AbstractAppender("HudWidgetConfigPersistenceTest", null, PatternLayout.createDefaultLayout(), false, Property.EMPTY_ARRAY) {
			@Override
			public void append(LogEvent event) {
				if (event.getMessage().getFormattedMessage().contains("Failed to save hud widget config")) {
					loggedFailure.set(true);
				}
			}
		};
		appender.start();
		logger.addAppender(appender);

		try {
			deleteConfigTarget();
			Files.createDirectory(WidgetManager.FILE);
			WidgetManager.saveConfig();
			if (!configBeforeSave.equals(getWidgetConfig(Location.CRYSTAL_HOLLOWS, CRYSTALS))) {
				throw new AssertionError("Failed save changed the in-memory HUD widget config");
			}
			if (!loggedFailure.get()) {
				throw new AssertionError("Failed save did not log an error");
			}
		} catch (IOException e) {
			throw new AssertionError("Failed to make HUD config target unwritable", e);
		} finally {
			logger.removeAppender(appender);
			appender.stop();
			restoreConfigFile(persistedConfig);
		}
	}

	private static WidgetsConfigurationScreen openEditor(Minecraft client, Location location, Screen previousScreen) {
		WidgetsConfigurationScreen screen = new WidgetsConfigurationScreen(location, previousScreen);
		client.gui.setScreen(screen);
		return screen;
	}

	private static void setPosition(WidgetsConfigurationScreen screen, String widgetId, PositionRule position) {
		for (PositionedWidget widget : getEditableLayer(screen).builder().getRendered()) {
			if (widget.widget.getInternalID().equals(widgetId)) {
				widget.rule = position;
				return;
			}
		}
		throw new AssertionError("Widget is not present in the HUD editor: " + widgetId);
	}

	private static EditableScreenBuilder.EditableLayer getEditableLayer(WidgetsConfigurationScreen screen) {
		try {
			Field layerField = WidgetsConfigurationScreen.class.getDeclaredField("layer");
			layerField.setAccessible(true);
			return (EditableScreenBuilder.EditableLayer) layerField.get(screen);
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Failed to access the HUD editor layer", e);
		}
	}

	private static void assertPosition(Location location, String widgetId, PositionRule expected) {
		PositionRule actual = getWidgetConfig(location, widgetId).position().orElseThrow(() -> new AssertionError("Widget has no saved position: " + widgetId));
		if (!expected.equals(actual)) {
			throw new AssertionError("Expected %s at %s, got %s".formatted(widgetId, expected, actual));
		}
	}

	private static WidgetConfig getWidgetConfig(Location location, String widgetId) {
		return Optional.ofNullable(WidgetManager.getScreenConfig(location).hud().widgets().get(widgetId))
				.orElseThrow(() -> new AssertionError("Widget is not present in the HUD config: " + widgetId));
	}

	private static PositionRule position(int x, int y) {
		return new PositionRule("screen", PositionRule.Point.DEFAULT, PositionRule.Point.DEFAULT, x, y);
	}

	private static FileSnapshot snapshotConfigFile() {
		try {
			return Files.isRegularFile(WidgetManager.FILE) ? new FileSnapshot(true, Files.readAllBytes(WidgetManager.FILE)) : new FileSnapshot(false, new byte[0]);
		} catch (IOException e) {
			throw new AssertionError("Failed to snapshot the HUD widget config", e);
		}
	}

	private static void restoreConfigFile(FileSnapshot snapshot) {
		try {
			deleteConfigTarget();
			if (snapshot.existed()) {
				Files.createDirectories(WidgetManager.FILE.getParent());
				Files.write(WidgetManager.FILE, snapshot.contents());
			}
		} catch (IOException e) {
			throw new AssertionError("Failed to restore the HUD widget config", e);
		}
	}

	private static void deleteConfigTarget() throws IOException {
		Files.deleteIfExists(WidgetManager.FILE);
	}

	private record FileSnapshot(boolean existed, byte[] contents) {}
}
