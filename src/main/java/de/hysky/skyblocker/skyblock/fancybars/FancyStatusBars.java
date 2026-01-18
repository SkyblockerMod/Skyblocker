package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.VisibleForTesting;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class FancyStatusBars {
	private static final Identifier HUD_LAYER = SkyblockerMod.id("fancy_status_bars");
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("status_bars.json");
	private static final Logger LOGGER = LoggerFactory.getLogger(FancyStatusBars.class);

	public static BarPositioner barPositioner = new BarPositioner();
	public static Map<StatusBarType, StatusBar> statusBars = new EnumMap<>(StatusBarType.class);
	private static boolean updatePositionsNextFrame;

	public static boolean isHealthFancyBarEnabled() {
		return isBarEnabled(StatusBarType.HEALTH);
	}

	public static boolean isExperienceFancyBarEnabled() {
		return isBarEnabled(StatusBarType.EXPERIENCE);
	}

	public static boolean isBarEnabled(StatusBarType type) {
		StatusBar statusBar = statusBars.get(type);
		return Debug.isTestEnvironment() || statusBar.enabled || statusBar.inMouse;
	}

	@SuppressWarnings("deprecation")
	@Init
	public static void init() {
		Function<HudElement, HudElement> hideIfFancyStatusBarsEnabled = hudElement -> {
			if (Utils.isOnSkyblock() && isEnabled())
				return (context, tickCounter) -> {};
			return hudElement;
		};

		HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, hudElement -> {
			if (!Utils.isOnSkyblock() || !isEnabled()) return hudElement;
			if (isHealthFancyBarEnabled()) {
				return (context, tickCounter) -> {};
			} else if (isExperienceFancyBarEnabled()) {
				return (context, tickCounter) -> {
					Matrix3x2fStack pose = context.pose();
					pose.pushMatrix();
					pose.translate(0, 6);
					hudElement.render(context, tickCounter);
					pose.popMatrix();
				};
			}
			return hudElement;
		});
		HudElementRegistry.replaceElement(VanillaHudElements.EXPERIENCE_LEVEL, hudElement -> {
			if (!Utils.isOnSkyblock() || !isEnabled() || !isExperienceFancyBarEnabled()) return hudElement;
			return (context, tickCounter) -> {};
		});
		HudElementRegistry.replaceElement(VanillaHudElements.INFO_BAR, hudElement -> {
			if (!Utils.isOnSkyblock() || !isEnabled() || !isExperienceFancyBarEnabled()) return hudElement;
			return (context, tickCounter) -> {};
		});
		HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR, hideIfFancyStatusBarsEnabled);
		HudElementRegistry.replaceElement(VanillaHudElements.MOUNT_HEALTH, hideIfFancyStatusBarsEnabled);
		HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, hideIfFancyStatusBarsEnabled);
		HudElementRegistry.replaceElement(VanillaHudElements.AIR_BAR, hideIfFancyStatusBarsEnabled);

		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, HUD_LAYER, (context, tickCounter) -> {
			if (Utils.isOnSkyblock()) render(context, Minecraft.getInstance());
		});

		statusBars.put(StatusBarType.HEALTH, StatusBarType.HEALTH.newStatusBar());
		statusBars.put(StatusBarType.INTELLIGENCE, StatusBarType.INTELLIGENCE.newStatusBar());
		statusBars.put(StatusBarType.DEFENSE, StatusBarType.DEFENSE.newStatusBar());
		statusBars.put(StatusBarType.EXPERIENCE, StatusBarType.EXPERIENCE.newStatusBar());
		statusBars.put(StatusBarType.SPEED, StatusBarType.SPEED.newStatusBar());
		statusBars.put(StatusBarType.AIR, StatusBarType.AIR.newStatusBar());

		// Fetch from old status bar config
		int[] counts = new int[3]; // counts for RIGHT, LAYER1, LAYER2
		UIAndVisualsConfig.LegacyBarPositions barPositions = SkyblockerConfigManager.get().uiAndVisuals.bars.barPositions;
		initBarPosition(statusBars.get(StatusBarType.HEALTH), counts, barPositions.healthBarPosition);
		initBarPosition(statusBars.get(StatusBarType.INTELLIGENCE), counts, barPositions.manaBarPosition);
		initBarPosition(statusBars.get(StatusBarType.DEFENSE), counts, barPositions.defenceBarPosition);
		initBarPosition(statusBars.get(StatusBarType.EXPERIENCE), counts, barPositions.experienceBarPosition);
		initBarPosition(statusBars.get(StatusBarType.SPEED), counts, UIAndVisualsConfig.LegacyBarPosition.RIGHT);
		initBarPosition(statusBars.get(StatusBarType.AIR), counts, UIAndVisualsConfig.LegacyBarPosition.RIGHT);

		CompletableFuture.supplyAsync(FancyStatusBars::loadBarConfig, Executors.newVirtualThreadPerTaskExecutor()).thenAccept(object -> {
			if (object != null) {
				for (String s : object.keySet()) {
					StatusBarType type = StatusBarType.from(s);
					if (statusBars.containsKey(type)) {
						try {
							statusBars.get(type).loadFromJson(object.get(s).getAsJsonObject());
						} catch (Exception e) {
							LOGGER.error("[Skyblocker] Failed to load {} status bar", s, e);
						}
					} else {
						LOGGER.warn("[Skyblocker] Unknown status bar: {}", s);
					}
				}
			}
			placeBarsInPositioner();
			configLoaded = true;
		}).exceptionally(throwable -> {
			LOGGER.error("[Skyblocker] Failed reading status bars config", throwable);
			return null;
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> {
			saveBarConfig();
			GLFW.glfwDestroyCursor(StatusBarsConfigScreen.RESIZE_CURSOR);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
						.then(ClientCommandManager.literal("bars").executes(Scheduler.queueOpenScreenCommand(StatusBarsConfigScreen::new)))));

		SkyblockEvents.LOCATION_CHANGE.register(location -> updatePositionsNextFrame = true);
	}

	/**
	 * Loads the bar position from the old config. Should be used to initialize new bars too.
	 *
	 * @param bar      the bar to load the position for
	 * @param counts   the counts for each bar position (LAYER1, LAYER2, RIGHT)
	 * @param position the position to load
	 */
	@SuppressWarnings("incomplete-switch")
	private static void initBarPosition(StatusBar bar, int[] counts, UIAndVisualsConfig.LegacyBarPosition position) {
		switch (position) {
			case RIGHT:
				bar.anchor = BarPositioner.BarAnchor.HOTBAR_RIGHT;
				bar.gridY = 0;
				bar.gridX = counts[position.ordinal()]++;
				break;
			case LAYER1:
				bar.anchor = BarPositioner.BarAnchor.HOTBAR_TOP;
				bar.gridY = 0;
				bar.gridX = counts[position.ordinal()]++;
				break;
			case LAYER2:
				bar.anchor = BarPositioner.BarAnchor.HOTBAR_TOP;
				bar.gridY = 1;
				bar.gridX = counts[position.ordinal()]++;
				break;
		}
	}

	private static boolean configLoaded = false;

	@VisibleForTesting
	public static void placeBarsInPositioner() {
		barPositioner.clear();
		for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
			List<StatusBar> barList = statusBars.values().stream().filter(bar -> bar.anchor == barAnchor)
					.sorted(Comparator.<StatusBar>comparingInt(bar -> bar.gridY).thenComparingInt(bar -> bar.gridX)).toList();
			if (barList.isEmpty()) continue;

			int y = -1;
			int rowNum = -1;
			for (StatusBar statusBar : barList) {
				if (statusBar.gridY > y) {
					barPositioner.addRow(barAnchor);
					rowNum++;
					y = statusBar.gridY;
				}
				barPositioner.addBar(barAnchor, rowNum, statusBar);
			}
		}
	}

	public static @Nullable JsonObject loadBarConfig() {
		try (BufferedReader reader = Files.newBufferedReader(FILE)) {
			return SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
		} catch (NoSuchFileException e) {
			LOGGER.warn("[Skyblocker] No status bar config file found, using defaults");
		} catch (Exception e) {
			LOGGER.error("[Skyblocker] Failed to load status bars config", e);
		}
		return null;
	}

	public static void saveBarConfig() {
		JsonObject output = new JsonObject();
		statusBars.forEach((s, statusBar) -> output.add(s.getSerializedName(), statusBar.toJson()));
		try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
			SkyblockerMod.GSON.toJson(output, writer);
			LOGGER.info("[Skyblocker] Saved status bars config");
		} catch (IOException e) {
			LOGGER.error("[Skyblocker] Failed to save status bars config", e);
		}
	}

	public static void updatePositions(boolean ignoreVisibility) {
		if (!configLoaded) return;
		final int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
		final int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();

		// Put these in the corner for the config screen
		int offset = 0;
		for (StatusBar statusBar : statusBars.values()) {
			if (!statusBar.enabled) {
				statusBar.setX(5);
				statusBar.setY(50 + offset);
				statusBar.setWidth(30);
				offset += statusBar.getHeight();
			} else if (statusBar.anchor == null) {
				statusBar.width = Math.clamp(statusBar.width, 30f / width, 1);
				statusBar.x = Math.clamp(statusBar.x, 0, 1 - statusBar.width);
				statusBar.y = Math.clamp(statusBar.y, 0, 1 - (float) statusBar.getHeight() / height);
				statusBar.setX((int) (statusBar.x * width));
				statusBar.setY((int) (statusBar.y * height));
				statusBar.setWidth((int) (statusBar.width * width));
			}
		}

		for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
			ScreenPosition anchorPosition = barAnchor.getAnchorPosition(width, height);
			BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();

			int targetSize = sizeRule.targetSize();
			boolean visibleHealthMove = barAnchor == BarPositioner.BarAnchor.HOTBAR_TOP && !isHealthFancyBarEnabled();
			if (visibleHealthMove) {
				targetSize /= 2;
			}

			if (sizeRule.isTargetSize()) {
				for (int row = 0; row < barPositioner.getRowCount(barAnchor); row++) {
					LinkedList<StatusBar> barRow = barPositioner.getRow(barAnchor, row);
					if (barRow.isEmpty()) continue;

					// FIX SIZES
					int totalSize = 0;
					for (StatusBar statusBar : barRow)
						totalSize += (statusBar.size = Math.clamp(statusBar.size, sizeRule.minSize(), sizeRule.maxSize()));

					whileLoop:
					while (totalSize != targetSize) {
						if (totalSize > targetSize) {
							for (StatusBar statusBar : barRow) {
								if (statusBar.size > sizeRule.minSize()) {
									statusBar.size--;
									totalSize--;
									if (totalSize == targetSize) break whileLoop;
								}
							}
						} else {
							for (StatusBar statusBar : barRow) {
								if (statusBar.size < sizeRule.maxSize()) {
									statusBar.size++;
									totalSize++;
									if (totalSize == targetSize) break whileLoop;
								}
							}
						}
					}

				}
			}

			int row = 0;
			for (int i = 0; i < barPositioner.getRowCount(barAnchor); i++) {
				List<StatusBar> barRow = barPositioner.getRow(barAnchor, i);
				if (barRow.isEmpty()) continue;


				// Update the positions
				float widthPerSize;
				if (sizeRule.isTargetSize()) {
					int size = 0;
					for (StatusBar bar : barRow) {
						if (bar.visible || ignoreVisibility) size += bar.size;
					}
					widthPerSize = (float) sizeRule.totalWidth() / size;

				}
				else
					widthPerSize = sizeRule.widthPerSize();

				if (visibleHealthMove) widthPerSize /= 2;

				int currSize = 0;
				int rowSize = barRow.size();
				for (int j = 0; j < rowSize; j++) {
					// A bit of a padding
					int offsetX = 0;
					int lessWidth = 0;
					if (rowSize > 1) { // Technically bars in the middle of 3+ bars will be smaller than the 2 side ones but shh
						if (j == 0) lessWidth = 1;
						else if (j == rowSize - 1) {
							lessWidth = 1;
							offsetX = 1;
						} else {
							lessWidth = 2;
							offsetX = 1;
						}
					}
					StatusBar statusBar = barRow.get(j);
					statusBar.size = Math.clamp(statusBar.size, sizeRule.minSize(), sizeRule.maxSize());

					if (!statusBar.visible && !ignoreVisibility) continue;

					float x = barAnchor.isRight() ?
							anchorPosition.x() + (visibleHealthMove ? sizeRule.totalWidth() / 2.f : 0) + currSize * widthPerSize :
							anchorPosition.x() - currSize * widthPerSize - statusBar.size * widthPerSize;
					statusBar.setX(Mth.ceil(x) + offsetX);

					int y = barAnchor.isUp() ?
							anchorPosition.y() - (row + 1) * (statusBar.getHeight() + 1) :
							anchorPosition.y() + row * (statusBar.getHeight() + 1);
					statusBar.setY(y);

					statusBar.setWidth(Mth.floor(statusBar.size * widthPerSize) - lessWidth);
					currSize += statusBar.size;
				}
				if (currSize > 0) row++;
			}

		}
	}

	public static boolean isEnabled() {
		return SkyblockerConfigManager.get().uiAndVisuals.bars.enableBars && (!Utils.isInTheRift() || SkyblockerConfigManager.get().uiAndVisuals.bars.enableBarsRift);
	}

	public static boolean render(GuiGraphics context, Minecraft client) {
		LocalPlayer player = client.player;
		if (!isEnabled() || player == null) return false;

		Collection<StatusBar> barCollection = statusBars.values();
		for (StatusBar statusBar : barCollection) {
			if (!statusBar.enabled || !statusBar.visible) continue;
			statusBar.renderBar(context);
		}
		for (StatusBar statusBar : barCollection) {
			if (!statusBar.enabled || !statusBar.visible) continue;
			statusBar.renderText(context);
		}

		if (Utils.isInTheRift()) {
			final int div = SkyblockerConfigManager.get().uiAndVisuals.bars.riftHealthHP ? 1 : 2;
			statusBars.get(StatusBarType.HEALTH).updateValues(Math.round(player.getHealth()) / player.getMaxHealth(), 0, Math.round(player.getHealth()) / div, Math.round(player.getMaxHealth()) / div, null);
			statusBars.get(StatusBarType.DEFENSE).visible = false;
		} else {
			StatusBarTracker.Resource health = StatusBarTracker.getHealth();
			statusBars.get(StatusBarType.HEALTH).updateWithResource(health);
			int defense = StatusBarTracker.getDefense();
			StatusBar defenseBar = statusBars.get(StatusBarType.DEFENSE);
			defenseBar.visible = true;
			defenseBar.updateValues(defense / (defense + 100.f), 0, defense, null, null);
		}

		StatusBarTracker.Resource intelligence = StatusBarTracker.getMana();
		if (SkyblockerConfigManager.get().uiAndVisuals.bars.intelligenceDisplay == UIAndVisualsConfig.IntelligenceDisplay.ACCURATE) {
			float totalIntelligence = (float) intelligence.max() + intelligence.overflow();
			statusBars.get(StatusBarType.INTELLIGENCE).updateValues(intelligence.value() / totalIntelligence + intelligence.overflow() / totalIntelligence, intelligence.overflow() / totalIntelligence, intelligence.value(), intelligence.max(), intelligence.overflow());
		} else statusBars.get(StatusBarType.INTELLIGENCE).updateWithResource(intelligence);

		StatusBarTracker.Resource speed = StatusBarTracker.getSpeed();
		statusBars.get(StatusBarType.SPEED).updateWithResource(speed);
		statusBars.get(StatusBarType.EXPERIENCE).updateValues(player.experienceProgress, 0, player.experienceLevel, null, null);
		StatusBarTracker.Resource air = StatusBarTracker.getAir();
		StatusBar airBar = statusBars.get(StatusBarType.AIR);
		airBar.updateWithResource(air);
		if (player.isUnderWater() != airBar.visible) {
			airBar.visible = player.isUnderWater();
			updatePositionsNextFrame = true;
		}
		if (updatePositionsNextFrame) {
			updatePositions(false);
			updatePositionsNextFrame = false;
		}
		return true;
	}
}
