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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlayerMobs {
    private static final Map<String, String> SLAYER_MINI_NAMES = Stream.of(new String[][]{
            {"Revenant Sycophant", SlayerUtils.REVENANT},
            {"Revenant Champion", SlayerUtils.REVENANT},
            {"Deformed Revenant", SlayerUtils.REVENANT},
            {"Atoned Champion", SlayerUtils.REVENANT},
            {"Atoned Revenant", SlayerUtils.REVENANT},
            {"Tarantula Vermin", SlayerUtils.TARA},
            {"Tarantula Beast", SlayerUtils.TARA},
            {"Mutant Tarantula", SlayerUtils.TARA},
            {"Pack Enforcer", SlayerUtils.SVEN},
            {"Sven Follower", SlayerUtils.SVEN},
            {"Sven Alpha", SlayerUtils.SVEN},
            {"Voidling Devotee", SlayerUtils.VOIDGLOOM},
            {"Voidling Radical", SlayerUtils.VOIDGLOOM},
            {"Voidcrazed Maniac", SlayerUtils.VOIDGLOOM},
            {"Flare Demon", SlayerUtils.DEMONLORD},
            {"Kindleheart Demon", SlayerUtils.DEMONLORD},
            {"Burningsoul Demon", SlayerUtils.DEMONLORD}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private static final Map<String, Class<? extends MobEntity>> SLAYER_MOB_TYPE = Map.of(
            SlayerUtils.REVENANT, ZombieEntity.class,
            SlayerUtils.TARA, SpiderEntity.class,
            SlayerUtils.SVEN, WolfEntity.class,
            SlayerUtils.VOIDGLOOM, EndermanEntity.class,
            SlayerUtils.DEMONLORD, BlazeEntity.class
    );

    private static final Pattern COLOUR_PATTERN = Pattern.compile("ASHEN|SPIRIT|CRYSTAL|AURIC");

    private static Set<UUID> mobsToGlow = new HashSet<>();

    public static boolean shouldGlow(UUID entityUUID) {
        return mobsToGlow.contains(entityUUID);
    }

    public static boolean isSlayer(LivingEntity e) {
        return SlayerUtils.isInSlayer() && SlayerUtils.getEntityArmorStands(e).stream().anyMatch(entity -> entity.getDisplayName().getString().contains(MinecraftClient.getInstance().getSession().getUsername()));
    }

    public static int getColour(LivingEntity e) {
        for (Entity entity : SlayerUtils.getEntityArmorStands(e)) {
            Matcher matcher = COLOUR_PATTERN.matcher(entity.getDisplayName().getString());
            if (matcher.find()) {
                String matchedColour = matcher.group();
                return switch (matchedColour) {
                    case "ASHEN" -> Color.DARK_GRAY.getRGB();
                    case "SPIRIT" -> Color.WHITE.getRGB();
                    case "CRYSTAL" -> Color.CYAN.getRGB();
                    case "AURIC" -> Color.YELLOW.getRGB();
                    default -> Color.RED.getRGB();
                };
            }
        }
        return Color.RED.getRGB();
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
            if (closestEntity != null) mobsToGlow.add(closestEntity.getUuid());
        }
    }

    public static void onEntityDeath(Entity entity) {
        mobsToGlow.remove(entity.getUuid());
    }
}
