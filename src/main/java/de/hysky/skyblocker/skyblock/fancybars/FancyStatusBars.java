package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FancyStatusBars {
    private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("status_bars.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(FancyStatusBars.class);

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final StatusBarTracker statusBarTracker = SkyblockerMod.getInstance().statusBarTracker;

    public static BarPositioner barPositioner = new BarPositioner();
    public static Map<String, StatusBar> statusBars = new HashMap<>();

    public static void init() {
        statusBars.put("health", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "bars/icons/health"),
                new Color[]{new Color(255, 0, 0), new Color(255, 220, 0)},
                true, new Color(255, 85, 85), Text.translatable("skyblocker.bars.config.health")));
        statusBars.put("intelligence", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "bars/icons/intelligence"),
                new Color[]{new Color(0, 255, 255), new Color(180, 0, 255)},
                true, new Color(85, 255, 255), Text.translatable("skyblocker.bars.config.intelligence")));
        statusBars.put("defense", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "bars/icons/defense"),
                new Color[]{new Color(255, 255, 255)},
                false, new Color(185, 185, 185), Text.translatable("skyblocker.bars.config.defense")));
        statusBars.put("experience", new StatusBar(new Identifier(SkyblockerMod.NAMESPACE, "bars/icons/experience"),
                new Color[]{new Color(100, 230, 70)},
                false, new Color(128, 255, 32), Text.translatable("skyblocker.bars.config.experience")));

        // Default positions
        StatusBar health = statusBars.get("health");
        health.anchor = BarPositioner.BarAnchor.HOTBAR_TOP;
        health.gridX = 0;
        health.gridY = 0;
        StatusBar intelligence = statusBars.get("intelligence");
        intelligence.anchor = BarPositioner.BarAnchor.HOTBAR_TOP;
        intelligence.gridX = 1;
        intelligence.gridY = 0;
        StatusBar defense = statusBars.get("defense");
        defense.anchor = BarPositioner.BarAnchor.HOTBAR_RIGHT;
        defense.gridX = 0;
        defense.gridY = 0;
        StatusBar experience = statusBars.get("experience");
        experience.anchor = BarPositioner.BarAnchor.HOTBAR_TOP;
        experience.gridX = 0;
        experience.gridY = 1;

        CompletableFuture.supplyAsync(FancyStatusBars::loadBarConfig).thenAccept(object -> {
            if (object != null) {
                for (String s : object.keySet()) {
                    if (statusBars.containsKey(s)) {
                        try {
                            statusBars.get(s).loadFromJson(object.get(s).getAsJsonObject());
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
        /*barGrid.addRow(1, false);
        barGrid.add(1, 1, statusBars.get("health"));
        barGrid.add(2, 1, statusBars.get("intelligence"));
        barGrid.addRow(2, false);
        barGrid.add(1, 2, statusBars.get("experience"));
        barGrid.addRow(-1, true);
        barGrid.add(1, -1, statusBars.get("defense"));*/
        //placeBarsInGrid();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
                        .then(ClientCommandManager.literal("bars").executes(Scheduler.queueOpenScreenCommand(StatusBarsConfigScreen::new)))));
    }

    private static boolean configLoaded = false;

    private static void placeBarsInPositioner() {
        List<StatusBar> original = statusBars.values().stream().toList();

        for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
            List<StatusBar> barList = new ArrayList<>(original.stream().filter(bar -> bar.anchor == barAnchor).toList());
            if (barList.isEmpty()) continue;
            barList.sort((a, b) -> a.gridY == b.gridY ? Integer.compare(a.gridX, b.gridX) : Integer.compare(a.gridY, b.gridY));

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

    public static JsonObject loadBarConfig() {
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
        statusBars.forEach((s, statusBar) -> output.add(s, statusBar.toJson()));
        try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
            SkyblockerMod.GSON.toJson(output, writer);
            LOGGER.info("[Skyblocker] Saved status bars config");
        } catch (IOException e) {
            LOGGER.error("[Skyblocker] Failed to save status bars config", e);
        }
    }

    public static void updatePositions() {
        if (!configLoaded) return;
        final int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        final int height = MinecraftClient.getInstance().getWindow().getScaledHeight();

        for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
            ScreenPos anchorPosition = barAnchor.getAnchorPosition(width, height);
            BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();

            if (sizeRule.isTargetSize()) {
                for (int row = 0; row < barPositioner.getRowCount(barAnchor); row++) {
                    LinkedList<StatusBar> barRow = barPositioner.getRow(barAnchor, row);
                    if (barRow.isEmpty()) continue;

                    // FIX SIZES
                    int totalSize = 0;
                    for (StatusBar statusBar : barRow)
                        totalSize += (statusBar.size = MathHelper.clamp(statusBar.size, sizeRule.minSize(), sizeRule.maxSize()));

                    whileLoop:
                    while (totalSize != sizeRule.targetSize()) {
                        if (totalSize > sizeRule.targetSize()) {
                            for (StatusBar statusBar : barRow) {
                                if (statusBar.size > sizeRule.minSize()) {
                                    statusBar.size--;
                                    totalSize--;
                                    if (totalSize == sizeRule.targetSize()) break whileLoop;
                                }
                            }
                        } else {
                            for (StatusBar statusBar : barRow) {
                                if (statusBar.size < sizeRule.maxSize()) {
                                    statusBar.size++;
                                    totalSize++;
                                    if (totalSize == sizeRule.targetSize()) break whileLoop;
                                }
                            }
                        }
                    }

                }
            }

            for (int row = 0; row < barPositioner.getRowCount(barAnchor); row++) {
                List<StatusBar> barRow = barPositioner.getRow(barAnchor, row);
                if (barRow.isEmpty()) continue;


                // Update the positions
                float widthPerSize;
                if (sizeRule.isTargetSize())
                    widthPerSize = (float) sizeRule.totalWidth() / sizeRule.targetSize();
                else
                    widthPerSize = sizeRule.widthPerSize();

                int currSize = 0;
                for (int i = 0; i < barRow.size(); i++) {
                    StatusBar statusBar = barRow.get(i);
                    statusBar.size = MathHelper.clamp(statusBar.size, sizeRule.minSize(), sizeRule.maxSize());

                    float x = barAnchor.isRight() ?
                            anchorPosition.x() + currSize * widthPerSize :
                            anchorPosition.x() - currSize * widthPerSize - statusBar.size * widthPerSize;
                    statusBar.setX((int) x);

                    int y = barAnchor.isUp() ?
                            anchorPosition.y() - (row + 1) * (statusBar.getHeight() + 1) :
                            anchorPosition.y() + row * (statusBar.getHeight() + 1);
                    statusBar.setY(y);

                    statusBar.setWidth((int) (statusBar.size * widthPerSize));
                    currSize += statusBar.size;
                    statusBar.gridX = i;
                    statusBar.gridY = row;

                }
            }

        }
    }

    public FancyStatusBars() {}

    public boolean render(DrawContext context, int scaledWidth, int scaledHeight) {
        var player = client.player;
        if (!SkyblockerConfigManager.get().general.bars.enableBars || player == null || Utils.isInTheRift())
            return false;

        Collection<StatusBar> barCollection = statusBars.values();
        for (StatusBar value : barCollection) {
            value.render(context, -1, -1, client.getLastFrameDuration());
        }
        for (StatusBar statusBar : barCollection) {
            if (statusBar.showText()) statusBar.renderText(context);
        }
        StatusBarTracker.Resource health = statusBarTracker.getHealth();
        statusBars.get("health").updateValues(health.value() / (float) health.max(), health.overflow() / (float) health.max(), health.value());

        StatusBarTracker.Resource intelligence = statusBarTracker.getMana();
        statusBars.get("intelligence").updateValues(intelligence.value() / (float) intelligence.max(), intelligence.overflow() / (float) intelligence.max(), intelligence.value());
        int defense = statusBarTracker.getDefense();
        statusBars.get("defense").updateValues(defense / (defense + 100.f), 0, defense);
        statusBars.get("experience").updateValues(player.experienceProgress, 0, player.experienceLevel);
        return true;
        }
}
