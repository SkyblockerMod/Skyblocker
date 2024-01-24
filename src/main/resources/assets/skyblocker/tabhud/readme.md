# Directory layout
The resource pack should look like this:
```
my_pack
├───pack.mcmeta
└───assets
    └───skyblocker
        └───tabhud
            ├───version.json
            ├───screen_a
            ├───screen_b
            └───standard
```
The three dirs `screen_a`, `screen_b` and `standard` contain the screen definition JSON files as described below. `standard` describes the screens shown when just the TAB key is pressed, while `screen_a/b` describe the screens shown when using the modifier keys A and B (default binds: N and B).
The file names are used to describe where a screen is shown. They are case sensitive. For a full list of possible names see Appendix A. While this scheme necessitates duplicate file contents and results in similar file names in different directories, it also allows for much control both when creating a complete layout or when only overriding specific screens.
The `version.json` file contains a single line `{ "version": X }`, where X is the version of the internal resource pack format. This number is incremented when major changes to the resource pack structure are made in the future. It's currently 1; packs with a different version number are rejected.
IF A MAJOR CHANGE IS MADE THAT MODIFIES THE VERSION NUMBER AND THEREFORE BREAKS YOUR LAYOUT, EDITING THE VERSION FILE WITHOUT REMAKING THE RESOURCE PACK DOES NOT FIX ANYTHING.

# Screen definition JSONs
This file consists of one widgets section and one layout section.

## Widget section
The widget section defines all of the widgets that a screen uses. It also defines an alias that is used in the layout section and possibly a widget's argument if its behaviour can be changed by one.
For a full list of widgets, their names and their arguments see Appendix B.

Be advised that most widgets only work correctly in some areas. The MinionWidget for example can't show your minions when you're not on the home island. General rule of thumb: If the info is displayed in the unmodded tab hud, the correlating widget works.

## Layout section
The layout section defines how the widgets are arranged on the screen. This is done using a pipeline; an ordered list of stages where each stage moves the widgets in some way. The initial position of the widgets is undefined. As a rule of thumb, every stage (except "Place", see below) only moves the widget in one direction, so at least two operations need to be done executed on each widget for it to be in a well defined position. A full list of operations and their arguments is found in Appendix C.

# Appendices

## Appendix A: Area identifiers
Some are self-explaining, others have clarification
- default: Any area without its own dedicated layout file.
- unknown: The mod doesn't know where you are.
- dungeon: Inside a dungon.
- dungeon_hub: In the dungeon hub.
- farming_island: The island with the barn, wheat, melons and pumpkins.
- park: The island with the trees and the harp.
- garden: The new(er) personal farming area
- hub: The main hub area.
- deep_caverns
- home_island
- guest_island
- crimson_isle
- dwarven_mines
- crystal_hollows
- end
- gold_mine
- spider_den
- jerry_workshop
- kuudra
- rift

## Appendix B: Widget identifiers and arguments
Grouped by themes (roughly)

- CookieWidget: Your super cookie.
- EffectWidget: Currently active effects.
- ElectionWidget: The current mayor election.
- EssenceWidget: Your dungeon essences.
- ErrorWidget: Displays an error message, "No data available" by default. Optional arg `text`: The error to be displayed.
- EventWidget: Current events. Arg `inGarden: true|false`: Is this widget displayed in the garden area?
- FireSaleWidget: Ongoing fire sales.
- MinionWidget: The minions on your island.
- ProfileWidget: Data about the player's profile.
- SkillsWidget: The player's skill levels and stats
- TrapperWidget: Trapper pelts on the farming island.
- UpgradeWidget: Currently running upgrades
- CameraPositionWidget: Shows orientation (pitch/yaw) of camera

#### Garden
- ComposterWidget: The composter in the garden.
- GardenServerWidget: Server widget specialized for the garden.
- GardenSkillsWidget: Skills widget specialized for the garden.
- JacobsContestWidget: The current Jacob's contest when in the garden.
- SprayonatorWidget: Spray status of every plot in the garden.

#### Mining
- CommsWidget: Shows the king's commissions in the dwarven mines and the crystal hollows.
- ForgeWidget: Items in the forge
- PowderWidget: Gemstone and Mithril powder collection.

#### Crimson Isle
- QuestWidget: Crimson Isle faction quests.
- ReputationWidget: Crimson Isle faction reputation.
- VolcanoWidget: Volcano status on the crimson isle

#### Dungeon In-game
- DungeonBuffWidget: The dungeon buffs you've found in this run.
- DungeonDeathWidget: Various dungeon stats (deaths, but also milestones, healing, damage taken).
- DungeonDownedWidget: Downed people in the dungeon.
- DungeonPlayerWidget: A single dungeon player. Arg `player: 1|2|3|4|5`: For which player should this widget display info? One for each player is recommended.
- DungeonPuzzleWidget: A list of all dungeon puzzles and their status.
- DungeonSecretWidget: How many secrets and crypts you've found in this run.
- DungeonServerWidget: Server widget specialized for the dungeon.

#### Rift
- AdvertisementWidget: Shows rift ads.
- GoodToKnowWidget: Lifetime Motes earned and/or times you visited the rift.
- RiftProfileWidget: Info about your player profile while in the rift.
- RiftProgressWidget: Info about Montezuma, Timecharms and Enigma Souls.
- RiftServerInfoWidget: Server widget specialized for the rift.
- RiftStatsWidget: Server widget specialized for the rift.
- ShenWidget: Shows Shen's countdown

#### Player lists
- PlayerListWidget: Generic list of players in the area.
- IslandGuestsWidget: Players visiting you or the same private island as you.
- IslandOwnersWidget: Owners of the island you're visiting.
- IslandSelfWidget: Owners of your home island.

#### Server info
- ServerWidget: Generic server information.
- GuestServerWidget: Server widget specialized for guesting.
- IslandServerWidget: Server widget specialized for the home island.
- ParkServerWidget: Server widget specialized for the park.

## Appendix C: Pipeline stages

#### Align
- op: `align`
- reference: One of `horizontalCenter`, `verticalCenter`, `leftOfCenter`, `rightOfCenter`, `topOfCenter`, `botOfCenter`, `top`, `bot`, `left`, `right`
- apply_to: List of widgets to individually apply this operation to.
- Moves a widget in *one* direction (up/down OR left/right) until it's positioned in the way described by `reference`. This reference may be thought of as a straight line, with some describing the screen's borders and others referring to it's two center axis.

Example: align A and B with "horizontalCenter"
```
+-----------+       +-----------+
|           |       |           |
|  A        |       | ->  A     |
|           |  -->  |           |
|           |       |           |
|          B|       |     B  <- |
+-----------+       +-----------+
```

#### Collide
- op: `collideAgainst`
- direction: One of `left`, `right`, `top`, `bot`
- widgets: List of widgets to individually move.
- colliders: List static reference widgets to "collide against".
- Moves a widget in the `direction` until it would overlap with any one of the colliders. Doesn't move the widget in the other direction, doesn't move the widget if it wouldn't collide with anything.

Example: Collide A and B from the right with X, Y and Z
```
+-----------+       +-----------+
|  A        |       |      A    |
|  A    XX  |       |   -> AXX  |
|       XX  |  -->  |       XX  |
|      Y    |       |      Y    |
|      Y  BB|       | -> BBY    |
+-----------+       +-----------+
```

#### Stack
- op: `stack`
- direction: One of `vertical`, `horizontal`
- align: One of `center`, `top`, `bot`, `left`, `right`
- apply_to: List of widgets to stack, order of the list is important (top -> bot or left -> right)
- Move the widgets vertically or horizontally as indicated by the `direction`. `align` indicates if the list is touching a screen border or is centered on the screen. This only works with borders in the direction of the stacking; combinations such as `vertical`/`left` are considered invalid and act like `align` was set to `center`. This only moves the widget in one direction so in order to get a clean list, an Align operation must be executed as well.

Example: Stack A, B, C vertically/center-aligned, then align with leftOfCenter
```
+-----------+       +-----------+       +-----------+
|  A        |       |  v        |       |           |
|           | stack |  A        | align | -> A      |
|     C     |  -->  |     v   B |  -->  |    B  <-  |
|           |       |     C   ^ |       |    C  <-  |
|         B |       |           |       |           |
+-----------+       +-----------+       +-----------+
```

#### Place
- op: `place`
- where: One of `center`, `centerTop`, `centerBot`, `centerLeft`, `centerRight`, `cornerTopRight`, `cornerTopLeft`, `cornerBotRight`, `cornerBotLeft`.
- apply_to: Widget to place an the indicated position.
- Places a widget at some position, moving it in any direction as needed. Only makes sense to use on one widget at a time, as applying the same Place op to multiple widgtes causes them to be on top of each other.

Example: Place A at "center"
```
+-----------+       +-----------+
|           |       |           |
|  A        |       | ->  v     |
|           |  -->  |     A     |
|           |       |           |
|           |       |           |
+-----------+       +-----------+
```