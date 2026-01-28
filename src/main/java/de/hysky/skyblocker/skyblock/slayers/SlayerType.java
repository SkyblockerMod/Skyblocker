package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static de.hysky.skyblocker.skyblock.profileviewer.slayers.SlayerWidget.HEAD_ICON;

public enum SlayerType implements StringRepresentable {
	REVENANT("revenant", EntityType.ZOMBIE, "Revenant Horror", HEAD_ICON.get("Zombie"), new int[]{500, 20_000, 400_000, 1_500_000, 10_000_000}, new int[]{5, 25, 100, 500, 1500}, new int[]{5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000}, List.of("Revenant Sycophant"), List.of("Revenant Champion", "Deformed Revenant"), List.of("Atoned Champion", "Atoned Revenant")),
	TARANTULA("tarantula", EntityType.SPIDER, "Tarantula Broodfather", HEAD_ICON.get("Spider"), new int[]{750, 30_000, 900_000, 2_400_000, 10_000_000}, new int[]{5, 25, 100, 500, 1500}, new int[]{5, 25, 200, 1000, 5000, 20000, 100000, 400000, 1000000}, List.of("Tarantula Vermin"), List.of("Tarantula Beast", "Mutant Tarantula"), List.of("Primordial Jockey", "Primordial Viscount")),
	SVEN("sven", EntityType.WOLF, "Sven Packmaster", HEAD_ICON.get("Wolf"), new int[]{2_000, 40_000, 750_000, 2_000_000}, new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Pack Enforcer"), List.of("Sven Follower", "Sven Alpha"), List.of()),
	VOIDGLOOM("voidgloom", EntityType.ENDERMAN, "Voidgloom Seraph", HEAD_ICON.get("Enderman"), new int[]{300_000, 12_000_000, 50_000_000, 210_000_000}, new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Voidling Devotee"), List.of("Voidling Radical", "Voidcrazed Maniac"), List.of()),
	DEMONLORD("demonlord", EntityType.BLAZE, "Inferno Demonlord", HEAD_ICON.get("Blaze"), new int[]{2_500_000, 10_000_000, 45_000_000, 150_000_000}, new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Flare Demon"), List.of("Kindleheart Demon", "Burningsoul Demon"), List.of()),
	VAMPIRE("vampire", EntityType.PLAYER, "Riftstalker Bloodfiend", HEAD_ICON.get("Vampire"), new int[]{625, 1_100, 1_800, 2_400, 3_000}, new int[]{10, 25, 60, 120, 150}, new int[]{20, 75, 240, 840, 2400}, List.of(), List.of(), List.of());

	public static final Codec<SlayerType> CODEC = StringRepresentable.fromEnum(SlayerType::values);
	public final String name;
	public final EntityType<? extends Entity> mobType;
	public final String bossName;
	public final Identifier texture;
	private final int[] health;
	public final int maxLevel;
	public final int[] xpPerTier;
	public final int[] levelMilestones;
	private final List<String> t3Minibosses;
	private final List<String> t4Minibosses;
	private final List<String> t5Minibosses;

	SlayerType(String name, EntityType<? extends Entity> mobType, String bossName, Identifier texture, int[] health, int[] xpPerTier, int[] levelMilestones, List<String> t3Minibosses, List<String> t4Minibosses, List<String> t5Minibosses) {
		this.name = name;
		this.mobType = mobType;
		this.bossName = bossName;
		this.texture = texture;
		this.health = health;
		this.maxLevel = levelMilestones.length;
		this.xpPerTier = xpPerTier;
		this.levelMilestones = levelMilestones;
		this.t3Minibosses = t3Minibosses;
		this.t4Minibosses = t4Minibosses;
		this.t5Minibosses = t5Minibosses;
	}

	public static @Nullable SlayerType fromBossName(String bossName) {
		return switch (bossName) {
			case "Revenant Horror", "Atoned Horror" -> REVENANT;
			case "Tarantula Broodfather", "Conjoined Brood" -> TARANTULA;
			case "Sven Packmaster" -> SVEN;
			case "Voidgloom Seraph" -> VOIDGLOOM;
			case "Inferno Demonlord" -> DEMONLORD;
			case "Riftstalker Bloodfiend", "Bloodfiend" -> VAMPIRE;
			default -> null;
		};
	}

	public boolean isMiniboss(String name, SlayerTier slayerTier) {
		List<List<String>> minibossLists = switch (slayerTier) {
			case III -> List.of(t3Minibosses);
			case IV -> List.of(t3Minibosses, t4Minibosses);
			case V -> List.of(t3Minibosses, t4Minibosses, t5Minibosses);
			default -> List.of();
		};

		return minibossLists.stream().flatMap(List::stream).anyMatch(name::contains);
	}

	public EntityType<?> getMinibossType(String minibossName) {
		if (this == TARANTULA) {
			if (minibossName.contains("Primordial Jockey")) return EntityType.SKELETON;
			if (minibossName.contains("Primordial Viscount")) return EntityType.CAVE_SPIDER;
		}
		return this.mobType;
	}

	public int getHealth(String bossName, SlayerTier slayerTier) {
		if (bossName.contains("Conjoined Brood")) return 20_000_000;
		return health[slayerTier.ordinal()];
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
