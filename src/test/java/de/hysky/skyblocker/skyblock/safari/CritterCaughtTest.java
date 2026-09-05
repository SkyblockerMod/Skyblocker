package de.hysky.skyblocker.skyblock.safari;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.skyblock.hunting.safari.SafariCritters;
import de.hysky.skyblocker.skyblock.hunting.safari.SafariUtils;

class CritterCaughtTest {
	@Test
	void capture() {
		// a critter
		Assertions.assertEquals(SafariUtils.Critters.FLITTER, SafariCritters.parseCritter("CAPTURE! You caught a Flitter and gained a Flitter Shard!"));
		// an critter
		Assertions.assertEquals(SafariUtils.Critters.AREITA, SafariCritters.parseCritter("CAPTURE! You caught an Areita and gained an Areita Shard!"));
		// multi-word name
		Assertions.assertEquals(SafariUtils.Critters.MANTIS_SHRIMP, SafariCritters.parseCritter("CAPTURE! You caught a Mantis Shrimp and gained 2x Mantis Shrimp Shard!"));
		// the hideyho
		Assertions.assertEquals(SafariUtils.Critters.HIDEYHO, SafariCritters.parseCritter("CAPTURE! You found the Hideyho, and as a reward it gave you 6x Hideyho Shard!"));
		// sparkling
		Assertions.assertEquals(SafariUtils.Critters.CAVERNFISH, SafariCritters.parseCritter("CAPTURE! You caught a SPARKLING Cavernfish and received a Rainbow Feather and 20x Cavernfish Shard!"));
	}

	@Test
	void lootShare() {
		// a critter
		Assertions.assertEquals(SafariUtils.Critters.FLITTER, SafariCritters.parseCritter("LOOT SHARE! You received 2x Flitter Shard from DarthRiddler catching a Flitter!"));
		// an critter
		Assertions.assertEquals(SafariUtils.Critters.AREITA, SafariCritters.parseCritter("LOOT SHARE! You received an Areita Shard from Jetterbear catching an Areita!"));
		// multi-word name
		Assertions.assertEquals(SafariUtils.Critters.MANTIS_SHRIMP, SafariCritters.parseCritter("LOOT SHARE! You received a Mantis Shrimp Shard from knayvik catching a Mantis Shrimp!"));
		// the hideyho
		Assertions.assertEquals(SafariUtils.Critters.HIDEYHO, SafariCritters.parseCritter("LOOT SHARE! You received 4x Hideyho Shard from BabtouSenti finding the Hideyho!"));
		// sparkling
		Assertions.assertEquals(SafariUtils.Critters.NOZZLENOSE, SafariCritters.parseCritter("LOOT SHARE! You received a Rainbow Feather and 20x Nozzlenose Shard from oginthepog catching a SPARKLING Nozzlenose!"));
	}
}
