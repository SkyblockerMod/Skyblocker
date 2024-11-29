package de.hysky.skyblocker.skyblock.slayers;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

public enum SlayerType implements StringIdentifiable {
	REVENANT("revenant", EntityType.ZOMBIE, "Revenant Horror", new int[]{5, 25, 100, 500, 1500}, new int[]{5, 15, 200, 1000, 5000, 20000, 100000, 400000, 1000000}, List.of("Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant")),
	TARANTULA("tarantula", EntityType.SPIDER, "Tarantula Broodfather", new int[]{5, 25, 100, 500, 1500}, new int[]{5, 25, 200, 1000, 5000, 20000, 100000, 400000, 1000000}, List.of("Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula")),
	SVEN("sven", EntityType.WOLF, "Sven Packmaster", new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Pack Enforcer", "Sven Follower", "Sven Alpha")),
	VOIDGLOOM("voidgloom", EntityType.ENDERMAN, "Voidgloom Seraph", new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac")),
	VAMPIRE("vampire", EntityType.PLAYER, "Riftstalker Bloodfiend", new int[]{5, 25, 100, 500, 1500}, new int[]{20, 75, 240, 840, 2400}, List.of()),
	DEMONLORD("demonlord", EntityType.BLAZE, "Inferno Demonlord", new int[]{5, 25, 100, 500, 1500}, new int[]{10, 30, 250, 1500, 5000, 20000, 100000, 400000, 1000000}, List.of("Flare Demon", "Kindleheart Demon", "Burningsoul Demon")),
	UNKNOWN("unknown", null, "Unknown", new int[]{}, new int[]{}, List.of());
	public static final Codec<SlayerType> CODEC = StringIdentifiable.createCodec(SlayerType::values);
	public final String name;
	public final EntityType<? extends Entity> mobType;
	public final String bossName;
	public final int maxLevel;
	public final int[] xpPerTier;
	public final int[] levelMilestones;
	public final List<String> minibossNames;

	SlayerType(String name, EntityType<? extends Entity> mobType, String bossName, int[] xpPerTier, int[] levelMilestones, List<String> minibossNames) {
		this.name = name;
		this.mobType = mobType;
		this.bossName = bossName;
		this.maxLevel = levelMilestones.length;
		this.xpPerTier = xpPerTier;
		this.levelMilestones = levelMilestones;
		this.minibossNames = minibossNames;
	}

	public boolean isUnknown() {
		return this == UNKNOWN;
	}

	public static SlayerType fromBossName(String bossName) {
		for (SlayerType type : values()) {
			if (type.bossName.equalsIgnoreCase(bossName)) {
				return type;
			}
		}
		return UNKNOWN;
	}

	@Override
	public String asString() {
		return name;
	}
}
