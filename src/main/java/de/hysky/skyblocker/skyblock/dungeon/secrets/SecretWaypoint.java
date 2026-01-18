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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

public class SecretWaypoint extends DistancedNamedWaypoint {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecretWaypoint.class);
	public static final Codec<SecretWaypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("secretIndex").forGetter(secretWaypoint -> secretWaypoint.secretIndex),
			Category.CODEC.fieldOf("category").forGetter(secretWaypoint -> secretWaypoint.category),
			ComponentSerialization.CODEC.fieldOf("name").forGetter(secretWaypoint -> secretWaypoint.name),
			BlockPos.CODEC.fieldOf("pos").forGetter(secretWaypoint -> secretWaypoint.pos)
	).apply(instance, SecretWaypoint::new));
	public static final Codec<List<SecretWaypoint>> LIST_CODEC = CODEC.listOf();
	static final List<String> SECRET_ITEMS = List.of("Candycomb", "Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
	private static final Supplier<DungeonsConfig.SecretWaypoints> CONFIG = () -> SkyblockerConfigManager.get().dungeons.secretWaypoints;
	static final Supplier<Type> TYPE_SUPPLIER = () -> CONFIG.get().waypointType;
	public final int secretIndex;
	public final Category category;

	public SecretWaypoint(int secretIndex, Category category, String name, BlockPos pos) {
		this(secretIndex, category, Component.nullToEmpty(name), pos);
	}

	SecretWaypoint(int secretIndex, Category category, Component name, BlockPos pos) {
		super(pos, name, TYPE_SUPPLIER, category.colorComponents);
		this.secretIndex = secretIndex;
		this.category = category;
	}

	static ToDoubleFunction<SecretWaypoint> getSquaredDistanceToFunction(Entity entity) {
		return secretWaypoint -> entity.distanceToSqr(secretWaypoint.centerPos);
	}

	static Predicate<SecretWaypoint> getRangePredicate(Entity entity) {
		return secretWaypoint -> entity.position().closerThan(secretWaypoint.centerPos, 16);
	}

	@Override
	public boolean shouldRender() {
		if (category.isPrince()) {
			return !DungeonScore.wasPrinceKilled() && category.isEnabled();
		}

		return super.shouldRender() && category.isEnabled();
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
		return Objects.hash(secretIndex, category, name, pos);
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

	SecretWaypoint relativeToActual(Room room) {
		return new SecretWaypoint(secretIndex, category, name, room.relativeToActual(pos));
	}

	public enum Category implements StringRepresentable {
		ENTRANCE("entrance", secretWaypoints -> secretWaypoints.enableEntranceWaypoints, 0, 255, 0),
		SUPERBOOM("superboom", secretWaypoints -> secretWaypoints.enableSuperboomWaypoints, 255, 0, 0),
		CHEST("chest", secretWaypoints -> secretWaypoints.enableChestWaypoints, 2, 213, 250),
		ITEM("item", secretWaypoints -> secretWaypoints.enableItemWaypoints, 2, 64, 250),
		BAT("bat", secretWaypoints -> secretWaypoints.enableBatWaypoints, 142, 66, 0),
		WITHER("wither", secretWaypoints -> secretWaypoints.enableWitherWaypoints, 30, 30, 30),
		REDSTONE_KEY("key", secretWaypoints -> secretWaypoints.enableRedstoneKeyWaypoints, 200, 30, 30),
		LEVER("lever", secretWaypoints -> secretWaypoints.enableLeverWaypoints, 250, 217, 2),
		FAIRYSOUL("fairysoul", secretWaypoints -> secretWaypoints.enableFairySoulWaypoints, 255, 85, 255),
		STONK("stonk", secretWaypoints -> secretWaypoints.enableStonkWaypoints, 146, 52, 235),
		AOTV("aotv", secretWaypoints -> secretWaypoints.enableAotvWaypoints, 252, 98, 3),
		PEARL("pearl", secretWaypoints -> secretWaypoints.enablePearlWaypoints, 57, 117, 125),
		PRINCE("prince", secretWaypoints -> secretWaypoints.enablePrinceWaypoints, 133, 21, 13),
		DEFAULT("default", secretWaypoints -> secretWaypoints.enableDefaultWaypoints, 190, 255, 252);
		public static final Codec<Category> CODEC = StringRepresentable.fromEnum(Category::values);
		private final String name;
		private final Predicate<DungeonsConfig.SecretWaypoints> enabledPredicate;
		private final float[] colorComponents;

		Category(String name, Predicate<DungeonsConfig.SecretWaypoints> enabledPredicate, int... intColorComponents) {
			this.name = name;
			this.enabledPredicate = enabledPredicate;
			colorComponents = new float[intColorComponents.length];
			for (int i = 0; i < intColorComponents.length; i++) {
				colorComponents[i] = intColorComponents[i] / 255F;
			}
		}

		static Category get(JsonObject waypointJson) {
			return CODEC.parse(JsonOps.INSTANCE, waypointJson.get("category")).resultOrPartial(LOGGER::error).orElse(Category.DEFAULT);
		}

		boolean needsInteraction() {
			return this == CHEST || this == WITHER || this == REDSTONE_KEY;
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
		public String getSerializedName() {
			return name;
		}

		static class CategoryArgumentType extends StringRepresentableArgument<Category> {
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
