package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.*;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.*;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;

public class ScreenBuilder {

    public static boolean positionsNeedsUpdating = true;
    // maps alias -> widget instance
    private final HashMap<String, HudWidget> objectMap = new HashMap<>();

    //private final String builderName;

    private final Map<String, PositionRule> positioning = new Object2ObjectOpenHashMap<>();
    private Map<String, PositionRule> positioningBackup = null;
    private final Location location;

    /**
     * Create a ScreenBuilder from a json.
     */
    public ScreenBuilder(Location location) {
        this.location = location;
    }

    public @Nullable PositionRule getPositionRule(String widgetInternalId) {
        return positioning.get(widgetInternalId);
    }

    public void forEachPositionRuleEntry(BiConsumer<String, PositionRule> action) {
        positioning.forEach(action);
    }

    public PositionRule getPositionRuleOrDefault(String widgetInternalId) {
        PositionRule positionRule = getPositionRule(widgetInternalId);
        return positionRule == null ? PositionRule.DEFAULT : positionRule;
    }

    public void setPositionRule(String widgetInternalId, @Nullable PositionRule newPositionRule) {
        if (newPositionRule == null) positioning.remove(widgetInternalId);
        else positioning.put(widgetInternalId, newPositionRule);
    }

    public void backupPositioning() {
        positioningBackup = Map.copyOf(positioning);
    }

    public void restorePositioningFromBackup() {
        if (positioningBackup == null) return;
        positioning.clear();
        positioning.putAll(positioningBackup);
        positioningBackup = null;
    }

    /**
     * Lookup Widget instance from alias name
     */
    public HudWidget getInstance(String name) {
        if (!this.objectMap.containsKey(name)) {
            throw new NoSuchElementException("No widget with alias " + name + " in screen ");
        }
        return this.objectMap.get(name);
    }

    private final List<HudWidget> hudScreen = new ArrayList<>();
    private final List<HudWidget> mainTabScreen = new ArrayList<>();
    private final List<HudWidget> secondaryTabScreen = new ArrayList<>();

    public void positionWidgets(int screenW, int screenH) {
        hudScreen.clear();
        mainTabScreen.clear();
        secondaryTabScreen.clear();

        WidgetPositioner newPositioner = SkyblockerConfigManager.get().uiAndVisuals.tabHud.defaultPositioning.getNewPositioner(screenW, screenH);

        for (HudWidget widget : ScreenMaster.widgetInstances.values()) {
            if (widget.shouldRender(location)) { // TabHudWidget has this at false
                // TODO maybe behavior to change? (having no position rule on a normal hud widget shouldn't quite be possible)
                PositionRule rule = getPositionRule(widget.getInternalID());
                if (rule == null) {
                    hudScreen.add(widget);
                } else {
                    switch (rule.screenLayer()) {
                        case MAIN_TAB -> mainTabScreen.add(widget);
                        case SECONDARY_TAB -> secondaryTabScreen.add(widget);
                        case null, default -> hudScreen.add(widget);
                    }
                }
                widget.update();
                widget.setPositioned(false);
            }
        }

        for (TabHudWidget widget : PlayerListMgr.tabWidgetsToShow) {
            PositionRule rule = getPositionRule(widget.getInternalID());
            if (rule == null) {
                mainTabScreen.add(widget);
            } else {
                widget.setPositioned(false);
                switch (rule.screenLayer()) {
                    case HUD -> hudScreen.add(widget);
                    case SECONDARY_TAB -> secondaryTabScreen.add(widget);
                    case null, default -> mainTabScreen.add(widget);
                }
            }
        }

        // Auto positioning
        for (HudWidget widget : mainTabScreen) {

            if (getPositionRule(widget.getInternalID()) != null) {
                widget.setPositioned(false);
            } else {
                newPositioner.positionWidget(widget);
                widget.setPositioned(true);
            }
        }
        newPositioner.finalizePositioning();
        // Custom positioning
        for (HudWidget widget : mainTabScreen) {
            if (!widget.isPositioned()) {
                WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
            }
        }

        for (HudWidget widget : hudScreen) {
            if (!widget.isPositioned()) {
                WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
            }
        }
        for (HudWidget widget : secondaryTabScreen) {
            if (!widget.isPositioned()) {
                WidgetPositioner.applyRuleToWidget(widget, screenW, screenH, this::getPositionRule);
            }
        }
    }

    /**
     * Renders the widgets present on the specified layer. Doesn't scale with the config option.
     */
    public void renderWidgets(DrawContext context, ScreenMaster.ScreenLayer screenLayer) {
        List<HudWidget> widgetsToRender = getHudWidgets(screenLayer);

        for (HudWidget widget : widgetsToRender) {
            widget.render(context);
        }
    }

    public List<HudWidget> getHudWidgets(ScreenMaster.ScreenLayer screenLayer) {
        return switch (screenLayer) {
            case MAIN_TAB -> mainTabScreen;
            case SECONDARY_TAB -> secondaryTabScreen;
            case HUD -> hudScreen;
            case null, default -> List.of();
        };
    }

    /**
     * Run the pipeline to build a Screen
     */
    public void run(DrawContext context, int screenW, int screenH, ScreenMaster.ScreenLayer screenLayer) {

        /*int i = 0;
        for (TabHudWidget value : PlayerListMgr.tabWidgetInstances.values()) {
            context.drawText(MinecraftClient.getInstance().textRenderer, value.getHypixelWidgetName(), 0, i, PlayerListMgr.tabWidgetsToShow.contains(value) ? Colors.LIGHT_YELLOW : -1, true);
            i += 9;
        }*/

        if (positionsNeedsUpdating) {
            positionsNeedsUpdating = false;
            positionWidgets(screenW, screenH);
        }

        renderWidgets(context, screenLayer);
    }



    public enum DefaultPositioner {
        TOP(TopAlignedWidgetPositioner::new),
        CENTERED(CenteredWidgetPositioner::new);

        private final BiFunction<Integer, Integer, WidgetPositioner> function;

        DefaultPositioner(BiFunction<Integer, Integer, WidgetPositioner> widgetPositionerSupplier) {
            function = widgetPositionerSupplier;
        }

        public WidgetPositioner getNewPositioner(int screenWidth, int screenHeight) {
            return function.apply(screenWidth, screenHeight);
        }
    }

}
