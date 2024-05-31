package de.hysky.skyblocker.skyblock.tabhud.screenbuilder;

import java.util.*;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.*;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.util.ScreenConst;
import de.hysky.skyblocker.skyblock.tabhud.widget.*;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ScreenBuilder {

    public static boolean positionsNeedsUpdating = true;

    // layout pipeline
    private final ArrayList<PipelineStage> layoutPipeline = new ArrayList<>();

    // all widget instances this builder knows
    private final ArrayList<HudWidget> instances = new ArrayList<>();
    // maps alias -> widget instance
    private final HashMap<String, HudWidget> objectMap = new HashMap<>();

    //private final String builderName;

    private final Map<String, PositionRule> positioning = new Object2ObjectOpenHashMap<>();
    private final Location location;

    /**
     * Create a ScreenBuilder from a json.
     */
    public ScreenBuilder(Location location) {
        this.location = location;

        /*try (BufferedReader reader = MinecraftClient.getInstance().getResourceManager().openAsReader(ident)) {
            this.builderName = ident.getPath();

            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            JsonArray widgets = json.getAsJsonArray("widgets");
            JsonArray layout = json.getAsJsonArray("layout");

            for (JsonElement w : widgets) {
                JsonObject widget = w.getAsJsonObject();
                String name = widget.get("name").getAsString();
                String alias = widget.get("alias").getAsString();

                HudWidget wid = instanceFrom(name, widget);
                objectMap.put(alias, wid);
                instances.add(wid);
            }

            for (JsonElement l : layout) {
                PipelineStage ps = createStage(l.getAsJsonObject());
                layoutPipeline.add(ps);
            }
        } catch (Exception ex) {
            // rethrow as unchecked exception so that I don't have to catch anything in the ScreenMaster
            throw new IllegalStateException("Failed to load file " + ident + ". Reason: " + ex.getMessage());
        }*/
    }

    /**
     * Try to find a class in the widget package that has the supplied name and
     * call it's constructor. Manual work is required if the class has arguments.
     */
    public HudWidget instanceFrom(String name, JsonObject widget) {

        /*// do widgets that require args the normal way
        JsonElement arg;
        switch (name) {
            case "DungeonPlayerWidget" -> {
                return new DungeonPlayerWidget(widget.get("player").getAsInt());
            }
            case "ErrorWidget" -> {
                arg = widget.get("text");
                if (arg == null) {
                    return new ErrorWidget();
                } else {
                    return new ErrorWidget(arg.getAsString());
                }
            }
            case "Widget" ->
                // clown case sanity check. don't instantiate the superclass >:|
                    throw new NoSuchElementException(builderName + "[ERROR]: No such Widget type \"Widget\"!");
        }

        // reflect something together for the "normal" ones.

        // list all packages that might contain widget classes
        // using Package isn't reliable, as some classes might not be loaded yet,
        // causing the packages not to show.
        String packbase = "de.hysky.skyblocker.skyblock.tabhud.widget";
        String[] packnames = {
                packbase,
                packbase + ".rift"
        };

        // construct the full class name and try to load.
        Class<?> clazz = null;
        for (String pn : packnames) {
            try {
                clazz = Class.forName(pn + "." + name);
            } catch (LinkageError | ClassNotFoundException ex) {
                continue;
            }
        }

        // load failed.
        if (clazz == null) {
            throw new NoSuchElementException(builderName + "/[ERROR]: No such Widget type \"" + name + "\"!");
        }

        // return instance of that class.
        try {
            Constructor<?> ctor = clazz.getConstructor();
            return (HudWidget) ctor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | IllegalArgumentException | InvocationTargetException | SecurityException ex) {
            throw new IllegalStateException(builderName + "/" + name + ": Internal error...");
        }*/
        return null;
    }

    /**
     * Create a PipelineStage from a json object.
     */
    public PipelineStage createStage(JsonObject descr) throws NoSuchElementException {

        String op = descr.get("op").getAsString();

        return switch (op) {
            case "place" -> new PlaceStage(this, descr);
            case "stack" -> new StackStage(this, descr);
            case "align" -> new AlignStage(this, descr);
            case "collideAgainst" -> new CollideStage(this, descr);
            default -> throw new NoSuchElementException("No such op " + op + " as requested by ");
        };
    }

    public @Nullable PositionRule getPositionRule(String widgetInternalId) {
        return positioning.get(widgetInternalId);
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

        WidgetPositioner newPositioner = DefaultPositioner.CENTERED.getNewPositioner(screenW, screenH);

        for (HudWidget widget : ScreenMaster.widgetInstances.values()) {
            if (widget.shouldRender(location)) {
                hudScreen.add(widget);
                widget.update();
                widget.setPositioned(false);
            }
        }

        // TODO check things and stuff
        mainTabScreen.addAll(PlayerListMgr.tabWidgetsToShow);

        for (HudWidget widget : mainTabScreen) {
            newPositioner.positionWidget(widget);
            widget.setPositioned(true);
        }
        newPositioner.finalizePositioning();
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

    public void renderWidgets(DrawContext context, Layer layer) {
        List<HudWidget> widgetsToRender = getHudWidgets(layer);

        for (HudWidget widget : widgetsToRender) {
            widget.render(context);
        }
    }

    public List<HudWidget> getHudWidgets(Layer layer) {
        return switch (layer) {
            case MAIN_TAB -> mainTabScreen;
            case SECONDARY_TAB -> secondaryTabScreen;
            case HUD -> hudScreen;
            case null -> List.of();
        };
    }

    /**
     * Run the pipeline to build a Screen
     */
    public void run(DrawContext context, int screenW, int screenH) {

        int i = 0;
        for (TabHudWidget value : PlayerListMgr.tabWidgetInstances.values()) {
            context.drawText(MinecraftClient.getInstance().textRenderer, value.getHypixelWidgetName(), 0, i, PlayerListMgr.tabWidgetsToShow.contains(value) ? Colors.LIGHT_YELLOW : -1, true);
            i += 9;
        }

        if (positionsNeedsUpdating) {
            positionsNeedsUpdating = false;
            positionWidgets(screenW, screenH);
        }

        renderWidgets(context, Layer.MAIN_TAB);
    }

    public enum Layer {
        MAIN_TAB,
        SECONDARY_TAB,
        HUD
    }

    private enum DefaultPositioner {
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
