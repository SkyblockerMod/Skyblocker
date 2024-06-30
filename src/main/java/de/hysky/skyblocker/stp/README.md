# Skyblocker Texture Predicates Documentation/Specification

Skyblocker Texture Predicates or "STP" is our high-performance custom texturing system which is based upon the vanilla predicates format with some notable extensions, the system is packed full of a bunch of different predicates to cover a wide variety of use-cases and maintains a consistent design for retexturing different things (e.g. items, armor); the system is also active Hypixel-wide allowing for retexturing things for other games as well.

## Advantages
Compared to the OptiFine format, our format integrates perfectly with the vanilla system and offers a wide variety of specialized predicates for different needs for texture pack authors. Skyblocker is very well maintained and updates to new versions on the day of their release (which usually contain new texture pack features) so authors won't need to wait on OptiFine/CITR which have historically been a little behind on their update game. Skyblocker is very well known and has a very large user base for the modern versions meaning that the vast majority of player's clients on these versions have support for our format.<br><br>

Our specialized predicates for item ids, and pet info for example enable added resiliency against changes in the game and on Hypixel (e.g. components broke everything), it also means that we can provide a fast path for common scenarios.

## Predicates
Predicates are evaluated in the order in which they are defined, so if one predicate needs to take on a higher priority than another it should be specified first. All predicates specified for a *[model override](https://minecraft.wiki/w/Model#Item_models)* must return true in order for that model to be used, see section on logical predicates if that functionality is undesireable. If you want to specify multiple of one predicate then logical predicates will serve your use case.

### Data Predicates
"Data" predicates are predicates which match upon some kind of item data, like the Skyblock item id, an item's custom data, or string/regex matching.<br>

| Predicate Id           | Type   | Description                                                          | Since  |
|------------------------|--------|----------------------------------------------------------------------|--------|
| skyblocker:item_id     | String | Exact match for a Skyblock Item ID.                                  | 1.22.0 |
| skyblocker:custom_data | SNBT   | Looks for a match on an item's custom data.                          | 1.22.0 |
| skyblocker:location    | String | Matches for a Skyblock location.                                     | 1.22.0 |
| skyblocker:regex       | Object | Allows for a full or partial regex match on an item's lore or name.  | 1.22.0 |
| skyblocker:string      | Object | Allows for a full or partial string match on an item's lore or name. | 1.22.0 |
| skyblocker:pet_info    | Object | Allows for matching a to a pet type or tier.                         | 1.22.0 |

#### Examples
```json
"predicate": {
	"skyblocker:item_id": "ASTRAEA"
}
```
```json
"predicate": {
	"skyblocker:custom_data": "{\"gems\":{\"JASPER_0\":{\"quality\":\"PERFECT\"}}}"
}
```
```json
"predicate": {
	"skyblocker:location": "dynamic" //Private Island
}
```
```json
"predicate": {
	"skyblocker:regex": {
		"matchType": "FULL", //FULL or PARTIAL
		"target": "NAME", //NAME or LORE
		"regex": "<some regex>" //Any regex, most should be supported by Java
	}
}
```
```json
"predicate": {
	"skyblocker:string": {
		"matchType": "EQUALS", //EQUALS, CONTAINS, STARTS_WITH, or ENDS_WITH
		"target": "NAME", //NAME or LORE
		"string": "<some string>" //The string to match against
	}
}
```
```json
"predicate": {
	"skyblocker:pet_info": { //When both the pet type & tier are defined, requires a match for both. If only the type or tier is defined it only requires a match for said field.
		"type": "GOLDEN_DRAGON", //Optional Field
		"tier": "LEGENDARY" //Optional Field
	}
}
```

### Logical Predicates
"Logical" predicates perform logical operations on *other* predicates, for example say you want to override a texture when it's Skyblock Item ID is either `POWER_WITHER_CHESTPLATE` or `POWER_WITHER_LEGGINGS`, due to the default behavior requiring a match for all specified predicates these overrides would need to be defined separately despite doing the same thing; logical predicates solve this issue by allowing you to perform "OR"s, "AND"s, or "NOT"s on a list of predicates.<br>

| Predicate ID   | Type           | Description                                           | Since  |
|----------------|----------------|-------------------------------------------------------|--------|
| skyblocker:or  | Predicate List | Performs a Logical OR on all predicates in the list.  | 1.22.0 |
| skyblocker:and | Predicate List | Performs a Logical AND on all predicates in the list. | 1.22.0 |
| skyblocker:not | Predicate List | Performs a Logical NOT on all predicates in the list. | 1.22.0 |<br><br>

When using a logical predicate, regular predicates must all be specified in an `Object` format with an `id` field representing the predicate's id. For predicates which are objects, the predicate's fields are simply specified normally alongside the predicate id field. For predicates which *are not* objects, their value must be specificed in a field named colloquially as `value`. The Logical AND, OR, and NOT predicates use the same format for listing predicates, see some examples below:

#### Examples
Say you wanted to perform a Logical OR on two `item_id` predicates, you can do this by:<br>

```json
"predicate": {
	"skyblocker:or": [ //This predicate will return true if any one of these predicates evaluate to true
		{
			"id": "skyblocker:item_id",
			"value": "POWER_WITHER_HELMET"
		},
		{
			"id": "skyblocker:item_id",
			"value": "POWER_WITHER_CHESTPLATE"
		}
	]
}
```
If you wanted to perform a Logical AND on an item's id and name, this can be done with:<br>

```json
"predicate": {
	"skyblocker:and": [
		 {
			"id": "skyblocker:item_id",
			"value": "DARK_CLAYMORE"
		},
		{
			"id": "skyblocker:string",
			"matchType": "CONTAINS",
			"target": "NAME",
			"string": "Withered"
		}
	]
}
```
If you want to check whether a predicate or multiple predicates are false you can do this with the Logical NOT predicate like so:<br>

```json
"predicate": {
	"skyblocker:not": [ //Returns true if the item id is not HYPERION
		{
			"id": "skyblocker:item_id",
			"value": "HYPERION"
		}
	]
}
```

#### Notes
Logical predicates can also be nested inside of eachother!

## Item Re-texturing
Item re-texturing is done through the vanilla predicate and model system which is also what the Custom Model Data system uses, so its expected that you have a basic understanding of that system before using ours. Item model overrides using our predicates can either be specified alongside their vanilla model or in a separate directory (see below).

### Vanilla Model File Example
This model file for the `diamond_sword` item will override the vanilla model for a diamond sword when the item's id is `ASPECT_OF_THE_DRAGON`.<br>

```json
{
	"parent": "item/handheld",
	"textures": {
		"layer0": "item/diamond_sword"
	},
	"overrides": [
		{
			"predicate": {
				"skyblocker:item_id": "ASPECT_OF_THE_DRAGON"
			},
			"model": "item/aspect_of_the_dragons" //Model file path
		}
	]
}
```
### Separate Directory Model Overrides Example
If you have a lot of model overrides for a certain item then packing them all into one model file could cause it to get a bit too large, for this purpose we support loading model overrides from the `assets/skyblocker/overrides/item` directory. Inside of that directory you are free to use subfolders for further organization as well. The name of the file doesn't matter and is up to you.<br><br>

The `parent` field is the name of the vanilla model file where the `overrides` will be injected into at runtime, the parent name should be the file name of the vanilla model without the `.json` extension.

```json
{
    "parent": "iron_sword",
	"overrides": [
		{
			"predicate":{
				"skyblocker:item_id": "VALKYRIE"
			},
			"model": "item/valkyrie" //Model file path
		}
	]
}
```
### Notes
As expected you will need to define a model file for your custom item, this model file goes into the `assets/minecraft/models/item` directory (you can also put your custom model files in subdirectories inside of that folder), here is an example:<br>

```json
{
	"parent": "item/handheld",
	"textures": {
		 "layer0": "item/valkyrie" //Path to the item texture in the textures folder
	}
}
```

## Armor Re-texturing
With Armor re-texturing you specify which armor layers you want to be used when the specified overrides (and predicates) match. You're also able to use a specific layer for an override avoiding the need to put more specific armor texture overrides for the same set in different files and hope that the load order is sufficient enough for it to work out. All overrides must be specified in the `assets/skyblocker/overrides/armor` folder, using subdirectories inside of there is also supported. The name of the file is up to you and doesn't matter.<br><br>

Skyblocker also supports **re-texturing player heads** via this system!

### Layers
For each armour layer you specify its `id`, optionally its `suffix`, and whether the layer should be `dyeable` which is whether its affected by the armor's current vanilla dye color (if applied). All texture files must go into the `assets/minecraft/textures/models/armor` folder.

### Examples

#### Basic Override Example
Say you wanted to provide a custom texture for Storm armor, an example file would look like:<br>

```json
{
	"layers": [
		{
			"id": "wise_wither",
			"suffix": "",
			"dyeable": false
		},
		{
			"id": "wise_wither",
			"suffix": "_overlay",
			"dyeable": false
		}
	],
	"overrides": [
		{
			"predicate": {
				"skyblocker:or": [
					{
						"id": "skyblocker:item_id",
						"value": "WISE_WITHER_HELMET"
					},
					{
						"id": "skyblocker:item_id",
						"value": "WISE_WITHER_CHESTPLATE"
					},
					{
						"id": "skyblocker:item_id",
						"value": "WISE_WITHER_LEGGINGS"
					},
					{
						"id": "skyblocker:item_id",
						"value": "WISE_WITHER_BOOTS"
					}
				]
			}
		}
	]
}
```

#### Advanced Override
Say you wanted to provide a custom texture for a Necron Chestplate/Boots, but also use a different texture for the chestplate while in dungeons. This can be achieved by:<br>

```json
{
	"layers": [
		{
			"id": "power_wither",
			"suffix": "",
			"dyeable": false
		},
		{
			"id": "power_wither",
			"suffix": "_overlay",
			"dyeable": false
		}
	],
	"overrides": [
		{
			"predicate": {
				"skyblocker:item_id": "POWER_WITHER_CHESTPLATE",
				"skyblocker:location": "dungeon"
			},
			"layers": [
				{
					"id": "power_wither_dungeon",
					"suffix": "",
					"dyeable": false
				},
				{
					"id": "power_wither_dungeon",
					"suffix": "_overlay",
					"dyeable": false
				}
			]
		},
		{
			"predicate": {
				"skyblocker:or": [
					{
						"id": "skyblocker:item_id",
						"value": "POWER_WITHER_CHESTPLATE"
					},
					{
						"id": "skyblocker:item_id",
						"value": "POWER_WITHER_BOOTS"
					}
				]
			}
		}
	]
}
```

### Notes about armor re-texturing
For maintaining a high performance system, all armor textures are cached per `ItemStack` instance for at least 5 minutes. The cache is cleared upon Hypixel sending a new instance of the item or when textures are reloaded.<br><br>

## Conclusion
Enjoy re-texturing to your heart's content! We plan on adding far more powerful re-texturing features very very soon. If you have any requests let us know and we will add it to our TODO list and get it done!