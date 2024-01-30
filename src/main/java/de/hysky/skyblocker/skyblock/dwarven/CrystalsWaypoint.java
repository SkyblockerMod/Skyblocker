package de.hysky.skyblocker.skyblock.dwarven;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class CrystalsWaypoint extends Waypoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrystalsWaypoint.class);
    public static final Codec<CrystalsWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Category.CODEC.fieldOf("category").forGetter(crystalsWaypoint -> crystalsWaypoint.category),
            TextCodecs.CODEC.fieldOf("name").forGetter(crystalsWaypoint -> crystalsWaypoint.name),
            BlockPos.CODEC.fieldOf("pos").forGetter(crystalsWaypoint -> crystalsWaypoint.pos)
    ).apply(instance, CrystalsWaypoint::new));
    public static final Codec<List<CrystalsWaypoint>> LIST_CODEC = CODEC.listOf();
    static final List<String> SECRET_ITEMS = List.of("Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
    private static final Supplier<SkyblockerConfig.CrystalsWaypoints> CONFIG = () -> SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints;
    static final Supplier<Type> TYPE_SUPPLIER = () -> CONFIG.get().waypointType;
    final Category category;
    final Text name;
    private final Vec3d centerPos;

    CrystalsWaypoint( JsonObject waypoint, String name, BlockPos pos) {
        this(Category.get(waypoint), name, pos);
    }

    CrystalsWaypoint(Category category, String name, BlockPos pos) {
        this( category, Text.of(name), pos);
    }

    CrystalsWaypoint( Category category, Text name, BlockPos pos) {
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
        //TODO In the future, shrink the box for wither essence and items so its more realistic
        super.render(context);


        Vec3d posUp = centerPos.add(0, 1, 0);
        RenderHelper.renderText(context, name, posUp, true);
        double distance = context.camera().getPos().distanceTo(centerPos);
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);

    }



    enum Category implements StringIdentifiable { //todo set better colours and remove extra data
        JUNGLETEMPLE("Jungle Temple", 0, 255, 0),
        MINESOFDIVAN("Mines Of Divan", 255, 0, 0),
        GOBLINQUEENSDEN("Goblin Queen's Den", 2, 213, 250),
        LOSTPRECURSORCITY("Lost Precursor City", 2, 64, 250),
        KHAZADUM("Khazad-dûm", 142, 66, 0),
        FAIRYGROTTO("Fairy Grotto", 30, 30, 30),
        DRAGONSLAIR("Dragon's Lair", 250, 217, 2),
        DEFAULT("default", 190, 255, 252);

        private static final Codec<Category> CODEC = StringIdentifiable.createCodec(Category::values);
        private final String name;

        private final float[] colorComponents;

        Category(String name, int... intColorComponents) {
            this.name = name;

            colorComponents = new float[intColorComponents.length];
            for (int i = 0; i < intColorComponents.length; i++) {
                colorComponents[i] = intColorComponents[i] / 255F;
            }
        }

        static Category get(JsonObject waypointJson) {
            return CODEC.parse(JsonOps.INSTANCE, waypointJson.get("category")).resultOrPartial(LOGGER::error).orElse(Category.DEFAULT);
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String asString() {
            return name;
        }

        static class CategoryArgumentType extends EnumArgumentType<Category> {
            CategoryArgumentType() {
                super(Category.CODEC, Category::values);
            }

            static CategoryArgumentType category() {
                return new CategoryArgumentType();
            }

            static <S> Category getCategory(CommandContext<S> context, String name) {
                return context.getArgument(name, Category.class);
            }
        }
    }
}
