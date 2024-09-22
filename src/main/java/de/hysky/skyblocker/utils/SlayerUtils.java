package de.hysky.skyblocker.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SlayerUtils {
	public static final String REVENANT = "Revenant Horror";
	public static final String TARA = "Tarantula Broodfather";
	public static final String SVEN = "Sven Packmaster";
	public static final String VOIDGLOOM = "Voidgloom Seraph";
	public static final String VAMPIRE = "Bloodfiend";
	public static final String DEMONLORD = "Inferno Demonlord";
	public static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?[kM]?)(?=❤)");
	public static final Pattern SLAYER_PATTERN = Pattern.compile("(Revenant Horror|Tarantula Broodfather|Sven Packmaster|Voidgloom Seraph|Inferno Demonlord|Bloodfiend) (X|IX|VIII|VII|VI|IV|V|III|II|I)");
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

	public static final Map<String, long[]> SLAYER_MOB_MAX_HP = Map.of(
			REVENANT, new long[]{500, 20_000L, 400_000, 1_500_000, 10_000_000},
			TARA, new long[]{750, 30_000, 900_000, 2_400_000},
			SVEN, new long[]{2000, 40_000, 750_000, 2_000_000},
			VOIDGLOOM, new long[]{300_000, 12_000_000, 50_000_000, 210_000_000},
			VAMPIRE, new long[]{625, 1100, 1800, 2400, 3000},
			DEMONLORD, new long[]{2_500_000, 10_000_000, 45_000_000, 150_000_000}
	);

	public static final Map<String, Class<? extends LivingEntity>> SLAYER_MOB_TYPE = Map.of(
			REVENANT, ZombieEntity.class,
			TARA, SpiderEntity.class,
			SVEN, WolfEntity.class,
			VOIDGLOOM, EndermanEntity.class,
			DEMONLORD, BlazeEntity.class,
			VAMPIRE, PlayerEntity.class
	);

	public static List<Entity> getEntityArmorStands(Entity entity, float expandY) {
		return entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox().expand(0.3F, expandY, 0.3F), x -> x instanceof ArmorStandEntity && x.hasCustomName());
	}

}
