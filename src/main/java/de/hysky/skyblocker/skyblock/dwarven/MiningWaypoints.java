package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.awt.*;
import java.util.function.Supplier;

public class MiningWaypoints extends Waypoint {
    private static final Supplier<SkyblockerConfig.Waypoints> CONFIG = () -> SkyblockerConfigManager.get().general.waypoints;
    private static final Supplier<Type> TYPE_SUPPLIER = () -> CONFIG.get().waypointType;
    final Text name;
    final Category category;
    private final Vec3d centerPos;

    MiningWaypoints(Category category, Text name, BlockPos pos) {
        super(pos, TYPE_SUPPLIER, category.getColorComponents());
        this.name = name;
        this.category = category;
        this.centerPos = pos.toCenterPos();
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || obj instanceof MiningWaypoints other && name.equals(other.name) && pos.equals(other.pos);
    }

    /**
     * Renders the secret waypoint, including a waypoint through {@link Waypoint#render(WorldRenderContext)}, the name, and the distance from the player.
     */
    @Override
    public void render(WorldRenderContext context) {
        super.render(context);

        Vec3d posUp = centerPos.add(0, 1, 0);
        RenderHelper.renderText(context, name, posUp, true);
        double distance = context.camera().getPos().distanceTo(centerPos);
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);

    }

    interface Category {
        Color getColor();

        String getName();

        float[] getColorComponents();

    }

    /**
     * enum for the different waypoints used int the crystals hud each with a {@link crystalCategory#name} and associated {@link crystalCategory#color}
     */

    enum crystalCategory implements Category {
        JUNGLE_TEMPLE("Jungle Temple", new Color(DyeColor.PURPLE.getSignColor())),
        MINES_OF_DIVAN("Mines of Divan", Color.GREEN),
        GOBLIN_QUEENS_DEN("Goblin Queen's Den", new Color(DyeColor.ORANGE.getSignColor())),
        LOST_PRECURSOR_CITY("Lost Precursor City", Color.CYAN),
        KHAZAD_DUM("Khazad-dûm", Color.YELLOW),
        FAIRY_GROTTO("Fairy Grotto", Color.PINK),
        DRAGONS_LAIR("Dragon's Lair", Color.BLACK),
        CORLEONE("Corleone", Color.WHITE),
        KING_YOLKAR("King Yolkar", Color.RED),
        ODAWA("Odawa", Color.MAGENTA),
        KEY_GUARDIAN("Key Guardian", Color.LIGHT_GRAY);

        public final Color color;
        private final String name;
        private final float[] colorComponents;

        crystalCategory(String name, Color color) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float[] getColorComponents() {
            return colorComponents;
        }
    }


    enum dwarvenCategory implements Category {
        SOMTHING("s", Color.BLACK, new BlockPos(0, 0, 0));

        public final Color color;
        private final String name;
        private final float[] colorComponents;
        private final BlockPos location;

        dwarvenCategory(String name, Color color, BlockPos location) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
            this.location = location;

        }

        public BlockPos getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float[] getColorComponents() {
            return colorComponents;
        }

    }

    enum glaciteCategory implements Category {
        AQUAMARINE("Aquamarine", Color.CYAN, new BlockPos[]{new BlockPos(-1, 139, 437), new BlockPos(90, 151, 229), new BlockPos(56, 151, 400), new BlockPos(51, 117, 303)}),
        ONYX("Onyx", Color.BLACK, new BlockPos[]{new BlockPos(79, 119, 411), new BlockPos(-14, 132, 386), new BlockPos(18, 136, 370), new BlockPos(16, 138, 411), new BlockPos(-68, 130, 408)}),
        PERIDOT("Peridot", Color.GREEN, new BlockPos[]{new BlockPos(-61, 147, 302), new BlockPos(91, 122, 397),new BlockPos(-73, 122, 458), new BlockPos(-77, 120, 282)}),
        CITRINE("Citrine", Color.YELLOW, new BlockPos[]{new BlockPos(-104, 144, 244), new BlockPos(39, 119, 386), new BlockPos(-57, 144, 421), new BlockPos(-47, 126, 418) });

        public final Color color;
        private final String name;
        private final float[] colorComponents;
        private final BlockPos[] location;

        glaciteCategory(String name, Color color, BlockPos[] location) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
            this.location = location;

        }

        public BlockPos[] getLocations() {
            return location;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float[] getColorComponents() {
            return colorComponents;
        }

    }
}
