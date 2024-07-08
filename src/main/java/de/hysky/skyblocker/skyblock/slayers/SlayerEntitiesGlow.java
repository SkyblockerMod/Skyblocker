package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SlayerEntitiesGlow {
    private static final Map<String, String> SLAYER_MINI_NAMES = Map.ofEntries(
            Map.entry("Revenant Sycophant", SlayerUtils.REVENANT),
            Map.entry("Revenant Champion", SlayerUtils.REVENANT),
            Map.entry("Deformed Revenant", SlayerUtils.REVENANT),
            Map.entry("Atoned Champion", SlayerUtils.REVENANT),
            Map.entry("Atoned Revenant", SlayerUtils.REVENANT),
            Map.entry("Tarantula Vermin", SlayerUtils.TARA),
            Map.entry("Tarantula Beast", SlayerUtils.TARA),
            Map.entry("Mutant Tarantula", SlayerUtils.TARA),
            Map.entry("Pack Enforcer", SlayerUtils.SVEN),
            Map.entry("Sven Follower", SlayerUtils.SVEN),
            Map.entry("Sven Alpha", SlayerUtils.SVEN),
            Map.entry("Voidling Devotee", SlayerUtils.VOIDGLOOM),
            Map.entry("Voidling Radical", SlayerUtils.VOIDGLOOM),
            Map.entry("Voidcrazed Maniac", SlayerUtils.VOIDGLOOM),
            Map.entry("Flare Demon", SlayerUtils.DEMONLORD),
            Map.entry("Kindleheart Demon", SlayerUtils.DEMONLORD),
            Map.entry("Burningsoul Demon", SlayerUtils.DEMONLORD)
    );

    private static final Map<String, Class<? extends MobEntity>> SLAYER_MOB_TYPE = Map.of(
            SlayerUtils.REVENANT, ZombieEntity.class,
            SlayerUtils.TARA, SpiderEntity.class,
            SlayerUtils.SVEN, WolfEntity.class,
            SlayerUtils.VOIDGLOOM, EndermanEntity.class,
            SlayerUtils.DEMONLORD, BlazeEntity.class
    );

    private static final Set<UUID> MOBS_TO_GLOW = new HashSet<>();

    public static boolean shouldGlow(UUID entityUUID) {
        return MOBS_TO_GLOW.contains(entityUUID);
    }

    public static boolean isSlayer(LivingEntity e) {
        return SlayerUtils.isInSlayer() && SlayerUtils.getEntityArmorStands(e).stream().anyMatch(entity -> entity.getDisplayName().getString().contains(MinecraftClient.getInstance().getSession().getUsername()));
    }

    public static boolean isSlayerMiniMob(LivingEntity entity) {
        if (entity.getCustomName() == null) return false;
        String entityName = entity.getCustomName().getString();
        return SLAYER_MINI_NAMES.keySet().stream().anyMatch(slayerMobName -> entityName.contains(slayerMobName) && SlayerUtils.isInSlayerQuestType(SLAYER_MINI_NAMES.get(slayerMobName)));
    }

    public static Box getSlayerMobBoundingBox(LivingEntity entity) {
        return switch (SlayerUtils.getSlayerType()) {
            case SlayerUtils.REVENANT ->
                    new Box(entity.getX() - 0.4, entity.getY() - 0.1, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 2.2, entity.getZ() + 0.4);
            case SlayerUtils.TARA ->
                    new Box(entity.getX() - 0.9, entity.getY() - 0.2, entity.getZ() - 0.9, entity.getX() + 0.9, entity.getY() - 1.2, entity.getZ() + 0.9);
            case SlayerUtils.VOIDGLOOM ->
                    new Box(entity.getX() - 0.4, entity.getY() - 0.2, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 3, entity.getZ() + 0.4);
            case SlayerUtils.SVEN ->
                    new Box(entity.getX() - 0.5, entity.getY() - 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 1, entity.getZ() + 0.5);
            default ->
                    new Box(entity.getX() - 0.5, entity.getY() + 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 2.2, entity.getZ() + 0.5);
        };
    }

    /**
     * <p> Finds the closest matching MobEntity for the armorStand using entityClass and armorStand age difference to filter
     * out impossible candidates, returning the closest mob of those remaining in the search box by block distance </p>
     *
     * @param entityClass the mob type of the Slayer (i.e. ZombieEntity.class)
     * @param armorStand  the entity that contains the display name of the Slayer (mini)boss
     */
    private static MobEntity findClosestMobEntity(Class<? extends MobEntity> entityClass, ArmorStandEntity armorStand) {
        return armorStand.getWorld().getEntitiesByClass(entityClass, armorStand.getDimensions(null).getBoxAt(armorStand.getPos()).expand(1.5), entity -> !entity.isDead() && entity.age > armorStand.age - 4 && entity.age < armorStand.age + 4)
                .stream()
                .filter(entity -> !(entity instanceof CaveSpiderEntity)) // CaveSpider extends Spider so filter out mob for BroodFather highlight
                .min(Comparator.comparingDouble((MobEntity e) -> e.distanceTo(armorStand)))
                .orElse(null);
    }

    /**
     * <p> Adds the Entity UUID to the Hashset of Slayer Mobs to glow </p>
     *
     * @param armorStand the entity that contains the display name of the Slayer (mini)boss
     */
    public static void setSlayerMobGlow(ArmorStandEntity armorStand) {
        String slayerType = SlayerUtils.getSlayerType();
        Class<? extends MobEntity> entityClass = SLAYER_MOB_TYPE.get(slayerType);
        if (entityClass != null) {
            MobEntity closestEntity = findClosestMobEntity(entityClass, armorStand);
            if (closestEntity != null) MOBS_TO_GLOW.add(closestEntity.getUuid());
        }
    }

    public static void onEntityDeath(@Nullable Entity entity) {
        if (entity != null && entity.getUuid() != null) MOBS_TO_GLOW.remove(entity.getUuid());
    }

    private static void clearGlow(Location location) {
        MOBS_TO_GLOW.clear();
    }

    public static void init() {
        SkyblockEvents.LOCATION_CHANGE.register(SlayerEntitiesGlow::clearGlow);
    }
}
