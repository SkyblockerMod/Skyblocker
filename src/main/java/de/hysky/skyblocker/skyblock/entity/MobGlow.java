package de.hysky.skyblocker.skyblock.entity;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.azureaaron.renderchest.api.CustomGlowCallback;
import net.azureaaron.renderchest.api.GlowConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;

public class MobGlow {
	public static final int NO_GLOW = GlowConstants.NO_GLOW;
	private static final List<MobGlowAdder> ADDERS = new ArrayList<>();
	/**
	 * Cache for mob glow. Absence means the entity does not have custom glow.
	 * If an entity is in the cache, it must have custom glow.
	 */
	private static final Object2IntMap<Entity> CACHE = new Object2IntOpenHashMap<>();

	@Init
	public static void init() {
		// Clear the cache every tick
		ClientTickEvents.END_LEVEL_TICK.register(_ -> clearCache());
		CustomGlowCallback.EVENT.register(MobGlow::applyCustomGlow);
	}

	private static int applyCustomGlow(Entity entity, EntityRenderState state) {
		boolean allowGlowInLivid = LividColor.allowGlow();
		boolean customGlow = hasOrComputeMobGlow(entity);
		boolean allowGlow = allowGlowInLivid && state.appearsGlowing() || customGlow;

		if (allowGlow && customGlow) {
			return ARGB.opaque(getMobGlow(entity));
		} else if (!allowGlow) {
			return GlowConstants.REMOVE_GLOW;
		}

		return GlowConstants.NO_GLOW;
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
		List<ArmorStand> armorStands = getArmorStands(entity);
		if (armorStands.isEmpty()) {
			return "";
		}
		return armorStands.getFirst().getName().getString();
	}

	public static List<ArmorStand> getArmorStands(Entity entity) {
		return getArmorStands(entity.level(), entity.getBoundingBox());
	}

	public static List<ArmorStand> getArmorStands(Level world, AABB box) {
		return world.getEntitiesOfClass(ArmorStand.class, box.inflate(0, 2, 0), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
	}

	public static String getArmorStandNameTrick(Entity entity) {
		Entity possibleNameTag = entity.level().getEntity(entity.getId() + 1);

		return possibleNameTag instanceof ArmorStand ? possibleNameTag.getPlainTextName() : "ERR NO ARMR STAND";
	}
}
