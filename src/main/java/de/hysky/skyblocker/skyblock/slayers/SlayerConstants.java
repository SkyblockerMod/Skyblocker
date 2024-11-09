package de.hysky.skyblocker.skyblock.slayers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;

public class SlayerConstants {

	public static final String REVENANT = "Revenant Horror";
	public static final String TARA = "Tarantula Broodfather";
	public static final String SVEN = "Sven Packmaster";
	public static final String VOIDGLOOM = "Voidgloom Seraph";
	public static final String VAMPIRE = "Riftstalker Bloodfiend";
	public static final String DEMONLORD = "Inferno Demonlord";

	public static final int[] regularXpPerTier = {5, 25, 100, 500, 1500};
	public static final int[] vampireXpPerTier = {10, 25, 60, 120, 150};

	public static final int[] ZombieLevelMilestones = {5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000};
	public static final int[] SpiderLevelMilestones = {5, 25, 200, 1000, 5000, 20000, 100000, 400000, 1000000};
	public static final int[] VampireLevelMilestones = {20, 75, 240, 840, 2400};
	//wolf, enderman and blaze level milestones are different from zombie and spider.
	public static final int[] RegularLevelMilestones = {10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000};

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

	public static final Map<String, Class<? extends Entity>> SLAYER_MOB_TYPE = Map.of(
			REVENANT, ZombieEntity.class,
			TARA, SpiderEntity.class,
			SVEN, WolfEntity.class,
			VOIDGLOOM, EndermanEntity.class,
			DEMONLORD, BlazeEntity.class,
			VAMPIRE, PlayerEntity.class
	);
}
