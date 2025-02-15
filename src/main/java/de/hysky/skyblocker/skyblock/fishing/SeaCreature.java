package de.hysky.skyblocker.skyblock.fishing;

import io.github.moulberry.repo.data.Rarity;

public enum SeaCreature {
	NIGHT_SQUID("Night Squid", "Pitch darkness reveals a Night Squid.", Rarity.COMMON, SeaCreatureCategory.PARK),
	AGARIMOO("Agarimoo", "Your Chumcap Bucket trembles, it's an Agarimoo.", Rarity.RARE, SeaCreatureCategory.CHUMCAP),
	CARROT_KING("Carrot King", "Is this even a fish? It's the Carrot King!", Rarity.RARE, SeaCreatureCategory.CARROT),
	SQUID("Squid", "A Squid appeared.", Rarity.COMMON, SeaCreatureCategory.WATER),
	SEA_WALKER("Sea Walker", "You caught a Sea Walker.", Rarity.COMMON, SeaCreatureCategory.WATER),
	SEA_GUARDIAN("Sea Guardian", "You stumbled upon a Sea Guardian.", Rarity.COMMON, SeaCreatureCategory.WATER),
	SEA_ARCHER("Sea Archer", "You reeled in a Sea Archer.", Rarity.UNCOMMON, SeaCreatureCategory.WATER),
	RIDER_OF_THE_DEEP("Rider of the Deep", "The Rider of the Deep has emerged.", Rarity.UNCOMMON, SeaCreatureCategory.WATER),
	SEA_WITCH("Sea Witch", "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!", Rarity.UNCOMMON, SeaCreatureCategory.WATER),
	CATFISH("Catfish", "Huh? A Catfish!", Rarity.RARE, SeaCreatureCategory.WATER),
	SEA_LEECH("Sea Leech", "Gross! A Sea Leech!", Rarity.RARE, SeaCreatureCategory.WATER),
	GUARDIAN_DEFENDER("Guardian Defender", "You've discovered a Guardian Defender of the sea.", Rarity.EPIC, SeaCreatureCategory.WATER),
	DEEP_SEA_PROTECTOR("Deep Sea Protector", "You have awoken the Deep Sea Protector, prepare for a battle!", Rarity.EPIC, SeaCreatureCategory.WATER),
	WATER_HYDRA("Water Hydra", "The Water Hydra has come to test your strength.", Rarity.LEGENDARY, SeaCreatureCategory.WATER),
	SEA_EMPEROR("Sea Emperor", "The Sea Emperor arises from the depths.", Rarity.LEGENDARY, SeaCreatureCategory.WATER),
	FROZEN_STEVE("Frozen Steve", "Frozen Steve fell into the pond long ago, never to §rresurface...until§r now!", Rarity.COMMON, SeaCreatureCategory.WINTER_ISLAND),
	FROSTY("Frosty", "It's a snowman! He looks harmless.", Rarity.COMMON, SeaCreatureCategory.WINTER_ISLAND),
	GRINCH("Grinch", "The Grinch stole Jerry's §rGifts...get§r them back!", Rarity.UNCOMMON, SeaCreatureCategory.WINTER_ISLAND),
	NUTCRACKER("Nutcracker", "You found a forgotten Nutcracker laying beneath the ice.", Rarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	YETI("Yeti", "What is this creature!?", Rarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	REINDRAKE("Reindrake", "A Reindrake forms from the depths.", Rarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	SCARECROW("Scarecrow", "Phew! It's only a Scarecrow.", Rarity.COMMON, SeaCreatureCategory.SPOOKY),
	NIGHTMARE("Nightmare", "You hear trotting from beneath the waves, you caught a Nightmare.", Rarity.RARE, SeaCreatureCategory.SPOOKY),
	WEREWOLF("Werewolf", "It must be a full moon, a Werewolf appears.", Rarity.EPIC, SeaCreatureCategory.SPOOKY),
	PHANTOM_FISHER("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", Rarity.LEGENDARY, SeaCreatureCategory.SPOOKY),
	GRIM_REAPER("Grim Reaper", "This can't be! The manifestation of death himself!", Rarity.LEGENDARY, SeaCreatureCategory.SPOOKY),
	NURSE_SHARK("Nurse Shark", "A tiny fin emerges from the water, you've caught a Nurse Shark.", Rarity.COMMON, SeaCreatureCategory.SHARK),
	BLUE_SHARK("Blue Shark", "You spot a fin as blue as the water it came from, it's a Blue Shark.", Rarity.UNCOMMON, SeaCreatureCategory.SHARK),
	TIGER_SHARK("Tiger Shark", "A striped beast bounds from the depths, the wild Tiger Shark!", Rarity.EPIC, SeaCreatureCategory.SHARK),
	GREAT_WHITE_SHARK("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", Rarity.LEGENDARY, SeaCreatureCategory.SHARK),
	OASIS_SHEEP("Oasis Sheep", "An Oasis Sheep appears from the water.", Rarity.UNCOMMON, SeaCreatureCategory.OASIS),
	OASIS_RABBIT("Oasis Rabbit", "An Oasis Rabbit appears from the water.", Rarity.UNCOMMON, SeaCreatureCategory.OASIS),
	SMALL_MITHRIL_GRUBBER("Small Mithril Grubber", "A leech of the mines surfaces... you've caught a Mithril Grubber.", Rarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	MEDIUM_MITHRIL_GRUBBER("Medium Mithril Grubber", "A leech of the mines surfaces... you've caught a Medium Mithril Grubber.", Rarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	LARGE_MITHRIL_GRUBBER("Large Mithril Grubber", "A leech of the mines surfaces... you've caught a Large Mithril Grubber.", Rarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	BLOATED_MITHRIL_GRUBBER("Bloated Mithril Grubber", "A leech of the mines surfaces... you've caught a Bloated Mithril Grubber.", Rarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	LAVA_BLAZE("Lava Blaze", "A Lava Blaze has surfaced from the depths!", Rarity.RARE, SeaCreatureCategory.MAGMA_FIELDS),
	LAVA_PIGMAN("Lava Pigman", "A Lava Pigman arose from the depths!", Rarity.RARE, SeaCreatureCategory.MAGMA_FIELDS),
	FLAMING_WORM("Flaming Worm", "A Flaming Worm surfaces from the depths!", Rarity.RARE, SeaCreatureCategory.LAVA_PRECURSOR),
	WATER_WORM("Water Worm", "A Water Worm surfaces!", Rarity.RARE, SeaCreatureCategory.GOBLIN_BURROWS),
	POISONED_WATER_WORM("Poisoned Water Worm", "A Poisoned Water Worm surfaces!", Rarity.RARE, SeaCreatureCategory.GOBLIN_BURROWS),
	ABYSSAL_MINER("Abyssal Miner", "An Abyssal Miner breaks out of the water!", Rarity.LEGENDARY, SeaCreatureCategory.WATER_CRYSTAL_HOLLOWS),
	MOOGMA("Moogma", "You hear a faint Moo from the lava... A Moogma appears.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	MAGMA_SLUG("Magma Slug", "From beneath the lava appears a Magma Slug.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	PYROCLASTIC_WORM("Pyroclastic Worm", "You feel the heat radiating as a Pyroclastic Worm surfaces.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LAVA_FLAME("Lava Flame", "A Lava Flame flies out from beneath the lava.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	FIRE_EEL("Fire Eel", "A Fire Eel slithers out from the depths.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LAVA_LEECH("Lava Leech", "A small but fearsome Lava Leech emerges.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	TAURUS("Taurus", "Taurus and his steed emerge.", Rarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	THUNDER("Thunder", "§c§lYou hear a massive rumble as Thunder emerges.", Rarity.MYTHIC, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LORD_JAWBUS("Lord Jawbus", "§c§lYou have angered a legendary creature... Lord Jawbus has arrived.", Rarity.MYTHIC, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	PLHLEGBLAST("Plhlegblast", "WOAH! A Plhlegblast appeared.", Rarity.COMMON, SeaCreatureCategory.PLHLEGBLAST);


	final String name;
	final String chatMessage;
	final Rarity rarity;
	final SeaCreatureCategory category;

	SeaCreature(String name, String chatMessage, Rarity rarity, SeaCreatureCategory category){
		this.name = name;
		this.chatMessage = chatMessage;
		this.rarity = rarity;
		this.category = category;

	}
}
