package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class CrystalsWaypoint extends Waypoint {
    private static final Supplier<SkyblockerConfig.Waypoints> CONFIG = () -> SkyblockerConfigManager.get().general.waypoints;
    private static final Supplier<Type> TYPE_SUPPLIER = () -> CONFIG.get().waypointType;
    final Category category;
    final Text name;
    private final Vec3d centerPos;

    CrystalsWaypoint(Category category, Text name, BlockPos pos) {
        super(pos, TYPE_SUPPLIER, category.colorComponents);
        this.category = category;
        this.name = name;
        this.centerPos = pos.toCenterPos();
    }

    static ToDoubleFunction<CrystalsWaypoint> getSquaredDistanceToFunction(Entity entity) {
        return crystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos);
    }

    static Predicate<CrystalsWaypoint> getRangePredicate(Entity entity) {
        return crystalsWaypoint -> entity.squaredDistanceTo(crystalsWaypoint.centerPos) <= 36D;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender() ;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || obj instanceof CrystalsWaypoint other && category == other.category && name.equals(other.name) && pos.equals(other.pos);
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

    /**
     * enum for the different waypoints used int the crystals hud each with a {@link Category#name} and associated {@link Category#color}
     */
    enum Category {
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

        Category(String name,Color color) {
            this.name = name;
            this.color = color;
            this.colorComponents = color.getColorComponents(null);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
