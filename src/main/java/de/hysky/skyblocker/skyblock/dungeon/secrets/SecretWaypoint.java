package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.DungeonsConfig;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.DistancedNamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class SecretWaypoint extends DistancedNamedWaypoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretWaypoint.class);
    public static final Codec<SecretWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("secretIndex").forGetter(secretWaypoint -> secretWaypoint.secretIndex),
            Category.CODEC.fieldOf("category").forGetter(secretWaypoint -> secretWaypoint.category),
            TextCodecs.CODEC.fieldOf("name").forGetter(secretWaypoint -> secretWaypoint.name),
            BlockPos.CODEC.fieldOf("pos").forGetter(secretWaypoint -> secretWaypoint.pos)
    ).apply(instance, SecretWaypoint::new));
    public static final Codec<List<SecretWaypoint>> LIST_CODEC = CODEC.listOf();
    static final List<String> SECRET_ITEMS = List.of("Candycomb", "Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
    private static final Supplier<DungeonsConfig.SecretWaypoints> CONFIG = () -> SkyblockerConfigManager.get().dungeons.secretWaypoints;
    static final Supplier<Type> TYPE_SUPPLIER = () -> CONFIG.get().waypointType;
    final int secretIndex;
    final Category category;

    SecretWaypoint(int secretIndex, Category category, String name, BlockPos pos) {
        this(secretIndex, category == null ? Category.DEFAULT : category, Text.of(name), pos);
    }

    SecretWaypoint(int secretIndex, Category category, Text name, BlockPos pos) {
        super(pos, name, TYPE_SUPPLIER, new float[]{1, 1, 1});
        this.secretIndex = secretIndex;
        this.category = category;
    }

    static ToDoubleFunction<SecretWaypoint> getSquaredDistanceToFunction(Entity entity) {
        return secretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos);
    }

    static Predicate<SecretWaypoint> getRangePredicate(Entity entity) {
        return secretWaypoint -> entity.getEntityPos().isInRange(secretWaypoint.centerPos, 16);
    }

    @Override
    public boolean shouldRender() {
    	if (category.isPrince()) {
    		return !DungeonScore.wasPrinceKilled() && category.isEnabled();
    	}

        return super.shouldRender() && category.isEnabled();
    }

	@Override
	public float[] getRenderColorComponents() {
		return category.getColorComponents(colorComponents);
	}

    boolean needsInteraction() {
        return category.needsInteraction();
    }

    boolean isLever() {
        return category.isLever();
    }

    boolean needsItemPickup() {
        return category.needsItemPickup();
    }

    boolean isBat() {
        return category.isBat();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SecretWaypoint other && secretIndex == other.secretIndex && category == other.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), secretIndex, category);
    }

    @Override
    protected boolean shouldRenderName() {
        return super.shouldRenderName() && CONFIG.get().showSecretText;
    }

    /**
     * Extracts the rendering for the secret waypoint, including a waypoint through {@link Waypoint#extractRendering(PrimitiveCollector)}, the name, and the distance from the player.
     */
    @Override
    public void extractRendering(PrimitiveCollector collector) {
        //TODO In the future, shrink the box for wither essence and items so its more realistic - can be done with RenderHelper
        super.extractRendering(collector);
    }

    @NotNull
    SecretWaypoint relativeToActual(Room room) {
        return new SecretWaypoint(secretIndex, category, name, room.relativeToActual(pos));
    }

    enum Category implements StringIdentifiable {
        ENTRANCE("entrance", secretWaypoints -> secretWaypoints.enableEntranceWaypoints, secretWaypoints -> secretWaypoints.colorEntranceWaypoints),
        SUPERBOOM("superboom", secretWaypoints -> secretWaypoints.enableSuperboomWaypoints, secretWaypoints -> secretWaypoints.colorSuperboomWaypoints),
        CHEST("chest", secretWaypoints -> secretWaypoints.enableChestWaypoints, secretWaypoints -> secretWaypoints.colorChestWaypoints),
        ITEM("item", secretWaypoints -> secretWaypoints.enableItemWaypoints, secretWaypoints -> secretWaypoints.colorItemWaypoints),
        BAT("bat", secretWaypoints -> secretWaypoints.enableBatWaypoints, secretWaypoints -> secretWaypoints.colorBatWaypoints),
        WITHER("wither", secretWaypoints -> secretWaypoints.enableWitherWaypoints, secretWaypoints -> secretWaypoints.colorWitherWaypoints),
        LEVER("lever", secretWaypoints -> secretWaypoints.enableLeverWaypoints, secretWaypoints -> secretWaypoints.colorLeverWaypoints),
        FAIRYSOUL("fairysoul", secretWaypoints -> secretWaypoints.enableFairySoulWaypoints, secretWaypoints -> secretWaypoints.colorFairySoulWaypoints),
        STONK("stonk", secretWaypoints -> secretWaypoints.enableStonkWaypoints, secretWaypoints -> secretWaypoints.colorStonkWaypoints),
        AOTV("aotv", secretWaypoints -> secretWaypoints.enableAotvWaypoints, secretWaypoints -> secretWaypoints.colorAotvWaypoints),
        PEARL("pearl", secretWaypoints -> secretWaypoints.enablePearlWaypoints, secretWaypoints -> secretWaypoints.colorPearlWaypoints),
        PRINCE("prince", secretWaypoints -> secretWaypoints.enablePrinceWaypoints, secretWaypoints -> secretWaypoints.colorPrinceWaypoints),
        DEFAULT("default", secretWaypoints -> secretWaypoints.enableDefaultWaypoints, secretWaypoints -> secretWaypoints.colorDefaultWaypoints);
        public static final Codec<Category> CODEC = StringIdentifiable.createCodec(Category::values);
        private final String name;
        private final Predicate<DungeonsConfig.SecretWaypoints> enabledPredicate;
        private final Function<DungeonsConfig.SecretWaypoints, Color> colorFunction;

        Category(String name, Predicate<DungeonsConfig.SecretWaypoints> enabledPredicate, Function<DungeonsConfig.SecretWaypoints, Color> colorFunction) {
            this.name = name;
            this.enabledPredicate = enabledPredicate;
            this.colorFunction = colorFunction;
        }

		private float[] getColorComponents(float[] compArray) {
			return colorFunction.apply(SkyblockerConfigManager.get().dungeons.secretWaypoints).getRGBColorComponents(compArray);
		}

        static Category get(JsonObject waypointJson) {
            return CODEC.parse(JsonOps.INSTANCE, waypointJson.get("category")).resultOrPartial(LOGGER::error).orElse(Category.DEFAULT);
        }

        boolean needsInteraction() {
            return this == CHEST || this == WITHER;
        }

        boolean isLever() {
            return this == LEVER;
        }

        boolean needsItemPickup() {
            return this == ITEM;
        }

        boolean isBat() {
            return this == BAT;
        }

        boolean isPrince() {
            return this == PRINCE;
        }

        boolean isEnabled() {
            return enabledPredicate.test(SkyblockerConfigManager.get().dungeons.secretWaypoints);
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
