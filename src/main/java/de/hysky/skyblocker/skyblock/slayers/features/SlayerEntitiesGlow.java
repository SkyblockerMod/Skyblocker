package de.hysky.skyblocker.skyblock.slayers.features;

import de.hysky.skyblocker.skyblock.slayers.SlayerConstants;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// TODO: a lot of this does the same thing as SlayerManger, need to refactor and use SlayerManager code
public class SlayerEntitiesGlow {
	private static final Set<UUID> MOBS_TO_GLOW = new HashSet<>();
	/**
	 * ARMORSTAND_TO_MOBS_TO_GLOW tracks if an armor stand already has an associated entity. This is used for trying to dedupe glows,
	 * where an armor stand has detected multiple candidates as its associated entity -  in a vain attempt to reduce the amount of false positives
	 */
	private static final ConcurrentHashMap<UUID, UUID> ARMORSTAND_TO_MOBS_TO_GLOW = new ConcurrentHashMap<>();

	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignore, ignore2, ignore3) -> clearGlow());
	}

	public static boolean shouldGlow(UUID entityUUID) {
		return MOBS_TO_GLOW.contains(entityUUID);
	}

	// TODO: this is duplicate from SlayerManager's slayerEntity
	public static boolean isSlayer(ArmorStandEntity armorStand) {
		return SlayerManager.isBossSpawned() && SlayerManager.getEntityArmorStands(armorStand, 2.5f).stream().anyMatch(entity ->
				entity.getDisplayName().getString().contains(MinecraftClient.getInstance().getSession().getUsername()));
	}

	public static boolean isSlayerMiniMob(ArmorStandEntity armorStand) {
		if (armorStand.getCustomName() == null) return false;
		String entityName = armorStand.getCustomName().getString();
		return SlayerConstants.SLAYER_MINI_NAMES.keySet().stream().anyMatch(slayerMobName -> entityName.contains(slayerMobName) && SlayerManager.isInSlayerQuestType(SlayerConstants.SLAYER_MINI_NAMES.get(slayerMobName)));
	}

	public static Box getSlayerMobBoundingBox(LivingEntity entity) {
		return switch (SlayerManager.getSlayerType()) {
			case SlayerConstants.REVENANT -> new Box(entity.getX() - 0.4, entity.getY() - 0.1, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 2.2, entity.getZ() + 0.4);
			case SlayerConstants.TARA -> new Box(entity.getX() - 0.9, entity.getY() - 0.2, entity.getZ() - 0.9, entity.getX() + 0.9, entity.getY() - 1.2, entity.getZ() + 0.9);
			case SlayerConstants.VOIDGLOOM -> new Box(entity.getX() - 0.4, entity.getY() - 0.2, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 3, entity.getZ() + 0.4);
			case SlayerConstants.SVEN -> new Box(entity.getX() - 0.5, entity.getY() - 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 1, entity.getZ() + 0.5);
			default -> entity.getBoundingBox();
		};
	}

	/**
	 * <p> Adds the Entity UUID to the Hashset of Slayer Mobs to glow </p>
	 *
	 * @param armorStand the entity that contains the display name of the Slayer (mini)boss
	 */
	public static void setSlayerMobGlow(ArmorStandEntity armorStand) {
		String slayerType = SlayerManager.getSlayerType();
		EntityType<? extends Entity> entityType = SlayerConstants.SLAYER_MOB_TYPE.get(slayerType);
		if (entityType != null) {
			Entity closestEntity = SlayerManager.findClosestMobEntity(entityType, armorStand);
			if (closestEntity != null) {
				UUID uuid = ARMORSTAND_TO_MOBS_TO_GLOW.putIfAbsent(armorStand.getUuid(), closestEntity.getUuid());
				if (uuid != null && closestEntity.getUuid() != uuid && closestEntity.age < 80) {
					Scheduler.INSTANCE.schedule(() -> recalculateMobGlow(armorStand, entityType, uuid), 30, true);
				}
				MOBS_TO_GLOW.add(closestEntity.getUuid());
			}
		}
	}

	/**
	 * This method attempts self-correct by finding the true slayer mob if there's 2 candidates
	 *
	 * @param armorStand  the armor stand we know to be a slayer mob
	 * @param entityType the java class of the entity we know the armor stand to belong to
	 * @param oldUUID     the uuid of the first detected slayer mob
	 */
	private static void recalculateMobGlow(ArmorStandEntity armorStand, EntityType<? extends Entity> entityType, UUID oldUUID) {
		Entity entity = SlayerManager.findClosestMobEntity(entityType, armorStand);
		if (entity != null && entity.getUuid() != oldUUID) {
			RenderHelper.runOnRenderThread(() -> {
				MOBS_TO_GLOW.add(entity.getUuid());
				MOBS_TO_GLOW.remove(ARMORSTAND_TO_MOBS_TO_GLOW.put(armorStand.getUuid(), entity.getUuid()));
			});

		}
	}

	public static void onEntityDeath(@Nullable Entity entity) {
		if (entity != null && entity.getUuid() != null) {
			MOBS_TO_GLOW.remove(entity.getUuid());
		}
	}

	public static void cleanupArmorstand(@Nullable ArmorStandEntity entity) {
		if (entity != null && entity.getUuid() != null) {
			ARMORSTAND_TO_MOBS_TO_GLOW.remove(entity.getUuid());
		}
	}

	private static void clearGlow() {
		MOBS_TO_GLOW.clear();
		ARMORSTAND_TO_MOBS_TO_GLOW.clear();
	}

}
