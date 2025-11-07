package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class MobGlow {
	public static final int NO_GLOW = EntityRenderState.NO_OUTLINE;
	/**
	 * Attached to {@code EntityRenderState}s to apply the custom glow colour.
	 */
	public static final RenderStateDataKey<Integer> ENTITY_CUSTOM_GLOW_COLOUR = RenderStateDataKey.create(() -> "Skyblocker custom glow colour");
	/**
	 * Attached to {@code WorldRenderState}s to indicate that the custom glow is being used this frame.
	 */
	public static final RenderStateDataKey<Boolean> FRAME_USES_CUSTOM_GLOW = RenderStateDataKey.create(() -> "Skyblocker frame uses custom glow");
	private static final List<MobGlowAdder> ADDERS = new ArrayList<>();
	/**
	 * Cache for mob glow. Absence means the entity does not have custom glow.
	 * If an entity is in the cache, it must have custom glow.
	 */
	private static final Object2IntMap<Entity> CACHE = new Object2IntOpenHashMap<>();

	@Init
	public static void init() {
		// Clear the cache every tick
		ClientTickEvents.END_WORLD_TICK.register(client -> clearCache());
	}

	protected static void registerGlowAdder(MobGlowAdder adder) {
		ADDERS.add(adder);
	}

	public static boolean hasOrComputeMobGlow(Entity entity) {
		if (CACHE.containsKey(entity)) {
			return true;
		}
		int color = computeMobGlow(entity);
		if (color != NO_GLOW) {
			CACHE.put(entity, color);
			return true;
		}
		return false;
	}

	public static int getMobGlow(Entity entity) {
		return CACHE.getInt(entity);
	}

	public static int getMobGlowOrDefault(Entity entity, int defaultColor) {
		return CACHE.getOrDefault(entity, defaultColor);
	}

	public static void clearCache() {
		CACHE.clear();
	}

	/**
	 * Computes the glow color for the given entity.
	 * <p>Only non-zero colors are valid.
	 */
	private static int computeMobGlow(Entity entity) {
		for (MobGlowAdder adder : ADDERS) {
			if (adder.isEnabled()) {
				int glowColour = adder.computeColour(entity);

				if (glowColour != NO_GLOW) return glowColour;
			}
		}

		return NO_GLOW;
	}

	/**
	 * Returns name of entity by finding closed armor stand and getting name of that
	 *
	 * @param entity the entity to check
	 * @return the name string of the entities  label
	 */
	public static String getArmorStandName(Entity entity) {
		List<ArmorStandEntity> armorStands = getArmorStands(entity);
		if (armorStands.isEmpty()) {
			return "";
		}
		return armorStands.getFirst().getName().getString();
	}

	public static List<ArmorStandEntity> getArmorStands(Entity entity) {
		return getArmorStands(entity.getEntityWorld(), entity.getBoundingBox());
	}

	public static List<ArmorStandEntity> getArmorStands(World world, Box box) {
		return world.getEntitiesByClass(ArmorStandEntity.class, box.expand(0, 2, 0), EntityPredicates.NOT_MOUNTED);
	}
}
