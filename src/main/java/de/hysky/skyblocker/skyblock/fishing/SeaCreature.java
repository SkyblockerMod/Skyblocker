package de.hysky.skyblocker.skyblock.fishing;

import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;

public enum SeaCreature {
	NIGHT_SQUID("Night Squid", "Pitch darkness reveals a Night Squid.", SkyblockItemRarity.COMMON, SeaCreatureCategory.PARK),
	AGARIMOO("Agarimoo", "Your Chumcap Bucket trembles, it's an Agarimoo.", SkyblockItemRarity.RARE, SeaCreatureCategory.CHUMCAP),
	CARROT_KING("Carrot King", "Is this even a fish? It's the Carrot King!", SkyblockItemRarity.RARE, SeaCreatureCategory.CARROT),
	SQUID("Squid", "A Squid appeared.", SkyblockItemRarity.COMMON, SeaCreatureCategory.WATER),
	SEA_WALKER("Sea Walker", "You caught a Sea Walker.", SkyblockItemRarity.COMMON, SeaCreatureCategory.WATER),
	SEA_GUARDIAN("Sea Guardian", "You stumbled upon a Sea Guardian.", SkyblockItemRarity.COMMON, SeaCreatureCategory.WATER),
	SEA_ARCHER("Sea Archer", "You reeled in a Sea Archer.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.WATER),
	RIDER_OF_THE_DEEP("Rider of the Deep", "The Rider of the Deep has emerged.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.WATER),
	SEA_WITCH("Sea Witch", "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.WATER),
	CATFISH("Catfish", "Huh? A Catfish!", SkyblockItemRarity.RARE, SeaCreatureCategory.WATER),
	SEA_LEECH("Sea Leech", "Gross! A Sea Leech!", SkyblockItemRarity.RARE, SeaCreatureCategory.WATER),
	GUARDIAN_DEFENDER("Guardian Defender", "You've discovered a Guardian Defender of the sea.", SkyblockItemRarity.EPIC, SeaCreatureCategory.WATER),
	DEEP_SEA_PROTECTOR("Deep Sea Protector", "You have awoken the Deep Sea Protector, prepare for a battle!", SkyblockItemRarity.EPIC, SeaCreatureCategory.WATER),
	WATER_HYDRA("Water Hydra", "The Water Hydra has come to test your strength.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WATER),
	THE_SEA_EMPEROR("The Sea Emperor", "The Sea Emperor arises from the depths.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WATER),
	FROZEN_STEVE("Frozen Steve", "Frozen Steve fell into the pond long ago, never to resurface...until now!", SkyblockItemRarity.COMMON, SeaCreatureCategory.WINTER_ISLAND),
	FROSTY("Frosty", "It's a snowman! He looks harmless.", SkyblockItemRarity.COMMON, SeaCreatureCategory.WINTER_ISLAND),
	GRINCH("Grinch", "The Grinch stole Jerry's §rGifts...get§r them back!", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.WINTER_ISLAND),
	NUTCRACKER("Nutcracker", "You found a forgotten Nutcracker laying beneath the ice.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	YETI("Yeti", "What is this creature!?", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	REINDRAKE("Reindrake", "A Reindrake forms from the depths.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WINTER_ISLAND),
	SCARECROW("Scarecrow", "Phew! It's only a Scarecrow.", SkyblockItemRarity.COMMON, SeaCreatureCategory.SPOOKY),
	NIGHTMARE("Nightmare", "You hear trotting from beneath the waves, you caught a Nightmare.", SkyblockItemRarity.RARE, SeaCreatureCategory.SPOOKY),
	WEREWOLF("Werewolf", "It must be a full moon, a Werewolf appears.", SkyblockItemRarity.EPIC, SeaCreatureCategory.SPOOKY),
	PHANTOM_FISHER("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.SPOOKY),
	GRIM_REAPER("Grim Reaper", "This can't be! The manifestation of death himself!", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.SPOOKY),
	NURSE_SHARK("Nurse Shark", "A tiny fin emerges from the water, you've caught a Nurse Shark.", SkyblockItemRarity.COMMON, SeaCreatureCategory.SHARK),
	BLUE_SHARK("Blue Shark", "You spot a fin as blue as the water it came from, it's a Blue Shark.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.SHARK),
	TIGER_SHARK("Tiger Shark", "A striped beast bounds from the depths, the wild Tiger Shark!", SkyblockItemRarity.EPIC, SeaCreatureCategory.SHARK),
	GREAT_WHITE_SHARK("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.SHARK),
	OASIS_SHEEP("Oasis Sheep", "An Oasis Sheep appears from the water.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.OASIS),
	OASIS_RABBIT("Oasis Rabbit", "An Oasis Rabbit appears from the water.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.OASIS),
	SMALL_MITHRIL_GRUBBER("Small Mithril Grubber", "A leech of the mines surfaces... you've caught a Mithril Grubber.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	MEDIUM_MITHRIL_GRUBBER("Medium Mithril Grubber", "A leech of the mines surfaces... you've caught a Medium Mithril Grubber.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	LARGE_MITHRIL_GRUBBER("Large Mithril Grubber", "A leech of the mines surfaces... you've caught a Large Mithril Grubber.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	BLOATED_MITHRIL_GRUBBER("Bloated Mithril Grubber", "A leech of the mines surfaces... you've caught a Bloated Mithril Grubber.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.ABANDONED_QUARRY),
	LAVA_BLAZE("Lava Blaze", "A Lava Blaze has surfaced from the depths!", SkyblockItemRarity.EPIC, SeaCreatureCategory.MAGMA_FIELDS),
	LAVA_PIGMAN("Lava Pigman", "A Lava Pigman arose from the depths!", SkyblockItemRarity.EPIC, SeaCreatureCategory.MAGMA_FIELDS),
	FLAMING_WORM("Flaming Worm", "A Flaming Worm surfaces from the depths!", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_PRECURSOR),
	WATER_WORM("Water Worm", "A Water Worm surfaces!", SkyblockItemRarity.RARE, SeaCreatureCategory.GOBLIN_BURROWS),
	POISONED_WATER_WORM("Poisoned Water Worm", "A Poisoned Water Worm surfaces!", SkyblockItemRarity.RARE, SeaCreatureCategory.GOBLIN_BURROWS),
	ABYSSAL_MINER("Abyssal Miner", "An Abyssal Miner breaks out of the water!", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WATER_CRYSTAL_HOLLOWS),
	MOOGMA("Moogma", "You hear a faint Moo from the lava... A Moogma appears.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	MAGMA_SLUG("Magma Slug", "From beneath the lava appears a Magma Slug.", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	PYROCLASTIC_WORM("Pyroclastic Worm", "You feel the heat radiating as a Pyroclastic Worm surfaces.", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LAVA_FLAME("Lava Flame", "A Lava Flame flies out from beneath the lava.", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	FIRE_EEL("Fire Eel", "A Fire Eel slithers out from the depths.", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LAVA_LEECH("Lava Leech", "A small but fearsome Lava Leech emerges.", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	TAURUS("Taurus", "Taurus and his steed emerge.", SkyblockItemRarity.EPIC, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	THUNDER("Thunder", "§c§lYou hear a massive rumble as Thunder emerges.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	LORD_JAWBUS("Lord Jawbus", "§c§lYou have angered a legendary creature... Lord Jawbus has arrived.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.LAVA_CRIMSON_ISLE),
	PLHLEGBLAST("Plhlegblast", "WOAH! A Plhlegblast appeared.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.PLHLEGBLAST),
	TRASH_GOBBLER("Trash Gobbler", "The Trash Gobbler is hungry for you!", SkyblockItemRarity.COMMON, SeaCreatureCategory.BACKWATER_BAYOU),
	BANSHEE("Banshee", "The desolate wail of a Banshee breaks the silence.", SkyblockItemRarity.RARE, SeaCreatureCategory.BACKWATER_BAYOU),
	ALLIGATOR("Alligator", "A long snout breaks the surface of the water. It's an Alligator!", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.BACKWATER_BAYOU),
	DUMPSTER_DIVER("Dumpster Diver", "A Dumpster Diver has emerged from the swamp!", SkyblockItemRarity.UNCOMMON, SeaCreatureCategory.BACKWATER_BAYOU),
	BAYOU_SLUDGE("Bayou Sludge", "A swampy mass of slime emerges, the Bayou Sludge!", SkyblockItemRarity.EPIC, SeaCreatureCategory.BACKWATER_BAYOU),
	TITANOBOA("Titanoboa", "§r§c§lA massive Titanoboa surfaces. It's body stretches as far as the eye can see.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.BACKWATER_BAYOU),
	RAGNAROK("Ragnarok", "§c§r§c§lThe sky darkens and the air thickens. The end times are upon us: Ragnarok is here.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.LAVA_HOTSPOT),
	FIREPROOF_WITCH("Fireproof Witch", "Trouble's brewing, it's a Fireproof Witch!", SkyblockItemRarity.RARE, SeaCreatureCategory.LAVA_HOTSPOT),
	FRIED_CHICKEN("Fried Chicken", "Smells of burning. Must be a Fried Chicken.", SkyblockItemRarity.COMMON, SeaCreatureCategory.LAVA_HOTSPOT),
	FIERY_SCUTTLER("Fiery Scuttler", "§cA Fiery Scuttler inconspicuously waddles up to you, friends in tow.", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.LAVA_HOTSPOT),
	WIKI_TIKI("Wiki Tiki", "§c§r§c§lThe water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.", SkyblockItemRarity.MYTHIC, SeaCreatureCategory.WATER_HOTSPOT),
	BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", "A garish set of tentacles arise. It's a Blue Ringed Octopus!", SkyblockItemRarity.LEGENDARY, SeaCreatureCategory.WATER_HOTSPOT),
	SNAPPING_TURTLE("Snapping Turtle", "A Snapping Turtle is coming your way, and it's ANGRY!", SkyblockItemRarity.RARE, SeaCreatureCategory.WATER_HOTSPOT),
	FROG_MAN("Frog Man", "Is it a frog? Is it a man? Well, yes, sorta, IT'S FROG MAN!!!!!!", SkyblockItemRarity.COMMON, SeaCreatureCategory.WATER_HOTSPOT);

	final String name;
	final String chatMessage;
	final SkyblockItemRarity rarity;
	final SeaCreatureCategory category;

	SeaCreature(String name, String chatMessage, SkyblockItemRarity rarity, SeaCreatureCategory category) {
		this.name = name;
		this.chatMessage = chatMessage;
		this.rarity = rarity;
		this.category = category;
	}
}
