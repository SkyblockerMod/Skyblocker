package de.hysky.skyblocker.skyblock.slayers;

import de.hysky.skyblocker.utils.SlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlayerMobs {
    private static final Map<String, String> SLAYER_MINI_NAMES = Stream.of(new String[][]{
            {"Revenant Sycophant", "Revenant"},
            {"Revenant Champion", "Revenant"},
            {"Deformed Revenant", "Revenant"},
            {"Atoned Champion", "Revenant"},
            {"Atoned Revenant", "Revenant"},
            {"Tarantula Vermin", "Tarantula"},
            {"Tarantula Beast", "Tarantula"},
            {"Mutant Tarantula", "Tarantula"},
            {"Pack Enforcer", "Sven"},
            {"Sven Follower", "Sven"},
            {"Sven Alpha", "Sven"},
            {"Voidling Devotee", "Voidgloom"},
            {"Voidling Radical", "Voidgloom"},
            {"Voidcrazed Maniac", "Voidgloom"},
            {"Flare Demon", "Demonlord"},
            {"Kindleheart Demon", "Demonlord"},
            {"Burningsoul Demon", "Demonlord"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static final Map<String, Class<? extends MobEntity>> SLAYER_TYPE_TO_ENTITY_CLASS = new HashMap<>();
    static {
        SLAYER_TYPE_TO_ENTITY_CLASS.put("Revenant", ZombieEntity.class);
        SLAYER_TYPE_TO_ENTITY_CLASS.put("Tarantula", SpiderEntity.class);
        SLAYER_TYPE_TO_ENTITY_CLASS.put("Sven", WolfEntity.class);
        SLAYER_TYPE_TO_ENTITY_CLASS.put("Voidgloom", EndermanEntity.class);
        SLAYER_TYPE_TO_ENTITY_CLASS.put("Inferno", BlazeEntity.class);
    }

    public static Set<UUID> MOBS_TO_GLOW = new HashSet<>();

    public static boolean isSlayer(LivingEntity e) {
        return SlayerUtils.isInSlayer() && SlayerUtils.getEntityArmorStands(e).stream().anyMatch(entity -> entity.getDisplayName().getString().contains(MinecraftClient.getInstance().getSession().getUsername()));
    }


    public static int getColour(LivingEntity e) {
        for (Entity entity : SlayerUtils.getEntityArmorStands(e)) {
            String name = entity.getDisplayName().getString();
            if (name.contains("ASHEN")) return Color.DARK_GRAY.getRGB();
            if (name.contains("SPIRIT")) return Color.WHITE.getRGB();
            if (name.contains("CRYSTAL")) return Color.CYAN.getRGB();
            if (name.contains("AURIC")) return Color.YELLOW.getRGB();
        }
        return Color.RED.getRGB();
    }

    public static boolean isSlayerMiniMob(LivingEntity entity) {
        if (entity.getCustomName() == null) return false;
        String entityName = entity.getCustomName().getString();
        return SLAYER_MINI_NAMES.keySet().stream().anyMatch(slayerMobName -> entityName.contains(slayerMobName) && SlayerUtils.isInSlayerType(SLAYER_MINI_NAMES.get(slayerMobName)));
    }

    public static Box getSlayerMobBoundingBox(LivingEntity entity) {
        String type = SlayerUtils.getSlayerType();
        if (type.contains("Revenant"))
            return new Box(entity.getX() - 0.4, entity.getY()-0.1, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 2.2, entity.getZ() + 0.4);
        if (type.contains("Tarantula"))
            return new Box(entity.getX() - 0.9, entity.getY() - 0.2, entity.getZ() - 0.9, entity.getX() + 0.9, entity.getY() - 1.2, entity.getZ() + 0.9);
        if (type.contains("Voidgloom"))
            return new Box(entity.getX() - 0.4, entity.getY() - 0.2, entity.getZ() - 0.4, entity.getX() + 0.4, entity.getY() - 3, entity.getZ() + 0.4);
        if (type.contains("Sven"))
            return new Box(entity.getX() - 0.5, entity.getY() - 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 1, entity.getZ() + 0.5);
        return new Box(entity.getX() - 0.5, entity.getY() + 0.1, entity.getZ() - 0.5, entity.getX() + 0.5, entity.getY() - 2.2, entity.getZ() + 0.5);
    }

    private static MobEntity findClosestEntity(Class<? extends MobEntity> entityClass, ArmorStandEntity armorStand) {
        return armorStand.getWorld().getEntitiesByClass(entityClass, armorStand.getDimensions(null).getBoxAt(armorStand.getPos()).expand(0.9), entity -> !entity.isDead())
                .stream()
                .min(Comparator.comparingDouble((MobEntity e) -> e.squaredDistanceTo(armorStand)))
                .orElse(null);
    }

    public static void setSlayerMobGlow(ArmorStandEntity armorStand) {
    String slayerType = SlayerUtils.getSlayerType().split(" ")[0];
    Class<? extends MobEntity> entityClass = SLAYER_TYPE_TO_ENTITY_CLASS.get(slayerType);
    if (entityClass != null) {
        MobEntity closestEntity = findClosestEntity(entityClass, armorStand);
        if (closestEntity != null) MOBS_TO_GLOW.add(closestEntity.getUuid());
        }
    }

    public static void onEntityDeath(Entity entity) {
        MOBS_TO_GLOW.remove(entity.getUuid());
    }
}
