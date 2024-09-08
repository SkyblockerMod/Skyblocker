package de.hysky.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlayerUtils {
    public static final String REVENANT = "Revenant Horror";
    public static final String TARA = "Tarantula Broodfather";
    public static final String SVEN = "Sven Packmaster";
    public static final String VOIDGLOOM = "Voidgloom Seraph";
    public static final String VAMPIRE = "Riftstalker Bloodfiend";
    public static final String DEMONLORD = "Inferno Demonlord";
    public static final Pattern SLAYER_PATTERN = Pattern.compile("Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Riftstalker Bloodfiend");

	public static final Map<String, String> SLAYER_MINI_NAMES = Map.ofEntries(
			Map.entry("Revenant Sycophant", REVENANT),
			Map.entry("Revenant Champion", REVENANT),
			Map.entry("Deformed Revenant", REVENANT),
			Map.entry("Atoned Champion", REVENANT),
			Map.entry("Atoned Revenant", REVENANT),
			Map.entry("Tarantula Vermin", TARA),
			Map.entry("Tarantula Beast", TARA),
			Map.entry("Mutant Tarantula", TARA),
			Map.entry("Pack Enforcer", SVEN),
			Map.entry("Sven Follower", SVEN),
			Map.entry("Sven Alpha", SVEN),
			Map.entry("Voidling Devotee", VOIDGLOOM),
			Map.entry("Voidling Radical", VOIDGLOOM),
			Map.entry("Voidcrazed Maniac", VOIDGLOOM),
			Map.entry("Flare Demon", DEMONLORD),
			Map.entry("Kindleheart Demon", DEMONLORD),
			Map.entry("Burningsoul Demon", DEMONLORD)
	);

	public static final Map<String, Class<? extends MobEntity>> SLAYER_MOB_TYPE = Map.of(
			REVENANT, ZombieEntity.class,
			TARA, SpiderEntity.class,
			SVEN, WolfEntity.class,
			VOIDGLOOM, EndermanEntity.class,
			DEMONLORD, BlazeEntity.class
	);

    public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
        return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.3F, expandY, 0.3F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
    }

    //Eventually this should be modified so that if you hit a slayer boss all slayer features will work on that boss.
    public static ArmorStandEntity getSlayerArmorStandEntity() {
        if (MinecraftClient.getInstance().world != null) {
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (entity.hasCustomName()) {
                    String entityName = entity.getCustomName().getString();
                    Matcher matcher = SLAYER_PATTERN.matcher(entityName);
                    if (matcher.find()) {
                        String username = MinecraftClient.getInstance().getSession().getUsername();
                        for (Entity armorStand : getEntityArmorStands(entity, 1.5f)) {
                            if (armorStand.getDisplayName().getString().contains(username)) {
								return (ArmorStandEntity) entity;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
