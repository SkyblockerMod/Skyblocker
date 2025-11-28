# Release 5.9.0

## Highlight
* Update to 1.21.10
* Add an improved waypoint screen
* Add Hoppity's Hunt Egg Waypoint Sharing
  * Egg locations are now shared between players with this feature enabled
* Add Room Name Labels on the Dungeon Map
* Add Arrow Align Device Solver
* Add Target Practice Device Solver
* Add more customization
  * Item Model & Armor Model can now be changed
  * Animated Player Head textures
  * Enchantment Glint Override
  * Improved customization screen
* Add Kuudra Chest Profit Calculator
* Add JEI Support
* Add Cursor Changing on Screens to match 1.21.10 Vanilla
* Add an option to always show corpse profit text in English
* Add support for Galatea Sea Creatures
* Add Quadrillion to Sign Calculator
* Add a way to update Zealot kills from the Bestiary
* Add an option to only highlight donated items in Salvage Helper
* Add support for new attribute shards by using the NEU Repo
* Add a description for the Drill Fuel config option
* Add a command to quickly search the config (MoulConfig only)
  * /skyblocker config (search text)
* Add REI support for the Museum Overlay
* Improve Smooth AOTE/Teleport Overlay when sneaking
* Improve lever highlight size on Lights On Device Solver
* Fix text in the world not rendering properly with Caxton
* Fix Enchanted Book Names in Search Overlay
* Fix Ice Fill Solver lag
* Fix Sign Calculator showing on the Bestiary search sign
* Fix some items not being detected for Corpse Profit
* Fix found Fairy Souls being reset when Fairy Souls are moved
* Fix various issues with the Widget Configuration Screen
* Fix Vampire Slayer XP being incorrect
* Fix Chronomatron solver when using Firmament
* Fix End Hud "Kills Since Last Eye" not being reset sometimes
* Fix Dungeon Score messages showing in Sent Chat History
* Fix block break progress resetting with Custom Durability Bar
* Fix Zombie names for Dojo Discipline Test helper
* Fix Hud Widgets getting stuck off-screen
* Fix Title Resizing Issues in the Title Container Config Screen
* Fix Mythological Ritual Network Protocol Error Disconnect
* Fix shards in Croesus Profit
* Fix the location for Emissary Eliza in the Dwarven Mines
* Fix Mangrove Equipment name in Museum Search Overlay
* Fix dropdown widgets not being prominent enough
* Fix Dungeons Preview Tab not working when in Dungeons
* Fix Croesus Profit & Chest Value
* Fix Mythological Ritual Waypoints
* Fix Wardrobe Keybinds

## What's Changed
* F7 Arrow Align Solver by @layou233 in https://github.com/SkyblockerMod/Skyblocker/pull/1754
* Add checkstyle whitespace around by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1761
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1756
* Customization screen shenanigans by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1655
* Caxton compat the sequel by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1766
* Fix Enchanted Book Names in Search Overlay  by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1767
* Fix ice fill solver by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1768
* Exclude Sign Calculator from Bestiary Search by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1770
* Improve smooth AOTE/ overlay when sneeking by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1769
* Various mining related fixes by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1724
* Add Galatea Sea Creature by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1775
* Fix unknown found fairy soul locations resetting other found fairy souls by @rafern in https://github.com/SkyblockerMod/Skyblocker/pull/1780
* Update Zealot Kills from Bestiary by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1771
* Tab HUD Config Changes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1786
* Add Q to Calculator.java by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1779
* Fix vampire slayer xp per tier by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1794
* Widget Configuration Screen Fixes/Changes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1797
* Salvage Helper - Add Option to Only Highlight Donated Items by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1791
* Fix Chronomatron Solver by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1796
* End Stats Changes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1799
* Don't Put Dungeon Messages in Sent Chat History by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1801
* Fix Scrolling in Widgets Config Screen by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1804
* avoid changing the item's data for custom durability bar by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1798
* fix dojo bug by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1805
* Very Minor Mythological Ritual Fixes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1788
* Fix Croesus Helper / Profit by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1778
* Fix potential crash in Croesus Profit by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1806
* Fix Dungeon Chest Value by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1808
* quick widget fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1812
* Limit Title Size in Title Container Config Screen by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1813
* Fix Mythological Ritual NPE by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1816
* Switch to NEU Repo for Attribute Shards by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1818
* Add a description for Drill Fuel config option by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1814
* Fix Mythological Ritual Helper Again by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1820
* waypoints screen improvements by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1684
* 1.21.9/1.21.10 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1815
* Split Dungeon Secrets by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1661
* Hoppity Waypoints over WS by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1719
* New Location For Eliza Emissary by @jadencodes in https://github.com/SkyblockerMod/Skyblocker/pull/1824
* Show Room Names on the Dungeon Map by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1782
* Add Config Search Command by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1785
* Fix Mangrove Equipment Showing as Mangrove Armor by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1831
* Add Matching for Mini Boss Rooms by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1833
* Make Dropdown Widget More Obvious by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1828
* Hopefully Fix Dungeons Loading Issue by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1837
* Dungeon Preview Tab Fixes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1834
* Undo StringUtils Removals by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1839
* Add REI Focused Stack Provider by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1594
* Fix Widget Reordering on 1.21.10 by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1840
* Add a Config Search Tag for New Features by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1843
* Config Backup Screen Fixes for 1.21.10 by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1845
* Fix Wardrobe Keybinds After 3rd Page Update by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1846

## New Contributors
* @rafern made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1780

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.8.0...v5.9.0+1.21.10
___
# Release 5.8.0

## Highlight
* Improve Dungeons item secret detection
* Add New /call features:
    * Add prompts for calling Trevor, Maddox, and Queen Mismyla
    * Add recall prompt for failed calls due to "bad signal"
    * Add /call NPC name autocomplete
* Add Shortcut Key Combos
* Add Iris shader support
* Add bazaar order tracker
* Add Builder's wand preview
* Add support for NPC Shop Recipes in REI
* Add support for Pet items and calendar events to Wiki Lookup
* Add an item selector for Quick Navigation buttons
* Add Coralot & Shellwise Highlights
* Add Regex capture group support for Chat Rules
* Add 1.8 Cactus Hitbox
* Add Name Sorting option to Player List tab widget
* Add support for Search Overlay in the Museum
* Make sweep overlay more accurate on Galatea
* Add correct axe preview in Sweep Details Widget for Hub/The Park
* Improve Config Backup Preview
* Improve chronomatron solver
* Add disabled slot texture in Personal Deletor/Compactor preview
* Reduce item repository initial download size
* Add highlighting for the correct answer buttons in Quiz puzzle room
* Add Lasso Hud for hunting on Galatea
* Add true quiver count
* Add support for End Stone sword ability in Compact Damage
* Add responsive mode for Smooth AOTE
* Add mana estimation for Status Bars
* Add Chat Waypoints dedicated option and shareCoords alias
* Add a double click option for Quick Navigation buttons
* Update dungeon crypts message config text to reflect the new default
* Update line width in Waterboard Puzzle to match Vanilla and other Features
* Change progress percentage on hud widgets to show 2 decimal places.
* Change slot lock default style
* Change halfBarColor to FFFF00
* Change item rarity iteration order
* Fix item background color not updating when the rarity changes
* Fix a freeze when purchasing with Fancy AH enabled
* Fix REI items being added outside SkyBlock
* Fix Melon not being recognized after latest SkyBlock update
* Fix the Item Pickup Widget showing Quiver arrows and the SkyBlock Menu
* Fix the Recipe Book not moving with other mods
* Fix a crash with the Garden Plots Widget when using SkyHanni
* Fix input calculator power operator right associativity
* Fix the party leader always being wrong in Fancy Party Finder
* Fix Blaze Puzzle and Guardian Health Text not working with Custom Health Bars enabled
* Fix items failing to load if they have invalid formatting styles.
* Fix being kicked to limbo when clicking a plot widget with a custom icon from SkyHanni
* Fix being able to auction protected items in some cases
* Fix WebSocket Waypoint message spam when joining the Crystal Hollows
* Fix Sign/Input calculator showing up on some search signs
* Fix Sea Creature Tracker double hook not working with SkyHanni
* Fix item count in the Gemstones Sack
* Fix Pest Highlighter for Melon Slice
* Fix ModernUI compatibility
* Fix Don't show Seen Waypoints through walls
* Fix Dungeon Profit Calculator for Scarf & Thorn shards
* Fix Skin Transparency
* Fix QuickNav buttons being tab navigable
* Fix Bestiary Regex
* Remove 1.8 Farmland hitbox feature

## What's Changed
* Update item secrets detection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1624
* Use disable slot texture by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1637
* Change halfBarColor to FFFF00 by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1628
* Add Call Trevor Prompt by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1625
* Add Call Maddox Prompts by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1630
* Add Call Mismyla Prompt by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1631
* Shortcut keybind combos by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1633
* Fix Bestiary Regex by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1642
* Fix: Setting Tooltip error by @Fyelne in https://github.com/SkyblockerMod/Skyblocker/pull/1654
* [Feat] Wiki Lookup refactor by @SteveKunG in https://github.com/SkyblockerMod/Skyblocker/pull/1559
* Fix AH Popup Freeze by @ninjaguardian in https://github.com/SkyblockerMod/Skyblocker/pull/1481
* Update config backup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1663
* Pickup widget ignore skyblock menu and quiver slot by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1667
* Do shallow clone for NEU repo by @layou233 in https://github.com/SkyblockerMod/Skyblocker/pull/1644
* use vanilla methods in recipe book for mod compatibility by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1668
* Remove Daedalus Axe from Farming Tools by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1676
* Update Spider's Den Relic Position by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1678
* Highlight the Answer Block in Quiz Puzzle Room by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1612
* lasso hud by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1664
* Improved chronomatron solver by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1666
* Fix Cactus Knife Blocks per Second in Farming Hud by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1677
* Remove Buttons Room Prince Waypoint by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1681
* fix rendering order of status bars by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1692
* Calculator improvements by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1671
* add waypoint type config to ender node helper by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1685
* getrdhytfyifuyufuk by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1693
* fix immutable list problem by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1695
* Fix location check for Lasso Hud by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1696
* add iris shader support to renderpipelines by @MicrocontrollersDev in https://github.com/SkyblockerMod/Skyblocker/pull/1691
* Make various icons on tablist widgets work with custom resource packs by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/1649
* Add bazaar order tracker by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1670
* Fix protected item dropping in dungeon by @layou233 in https://github.com/SkyblockerMod/Skyblocker/pull/1683
* Builder's wand preview by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1690
* Dungeon Chest Profit Changes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1694
* Fix Fancy AH not showing for some searches by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1698
* Add support for NPC Shop Recipes in REI by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1700
* Dungeons Stuff by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1702
* Fix item count in the Gemstones Sack by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1705
* Fix Compact Damage for Tarantula Armor Ability by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1709
* Fix potential crash in GardenPlotsWidget by @lunaynx in https://github.com/SkyblockerMod/Skyblocker/pull/1714
* Update dungeonCryptsMessageThreshold.@Tooltip by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1711
* Fix Party Leader being wrong in Fancy Party Finder by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1715
* Don't modify certain mob names in Dungeons when using Custom Health Bars by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1716
* Fix Pest Highlighter for Melon Slice by @lunaynx in https://github.com/SkyblockerMod/Skyblocker/pull/1717
* Update WaterboardOneFlow.java by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1718
* Makes progress components two decimals by @jadencodes in https://github.com/SkyblockerMod/Skyblocker/pull/1726
* Handle invalid Formatting style in NEU Repo item by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1669
* Strip formatting from Plot Names for the Garden Plots Widget by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1721
* Fix/protected item quick auction by @thqnhz in https://github.com/SkyblockerMod/Skyblocker/pull/1733
* Condense Crystal Hollows WebSocket Waypoints Initial Messages by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1708
* Exclude certain search signs from showing Input Calculator by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1731
* Refactor to use ItemStack.getUuid instead of ItemUtils.getItemUuid by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1734
* show true quiver count by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1331
* save on file size 😎  by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1736
* Compact Damage Fixes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1735
* Fix ModernUI compatibility by @layou233 in https://github.com/SkyblockerMod/Skyblocker/pull/1730
* Responsive smooth AOTE by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1587
* estimated mana by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1732
* Replace ChatEvents with Fabric's ALLOW_GAME event by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1737
* Update en_us.json by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1738
* Dungeon Map: Fix self marker not showing if fancy map is off by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1741
* [Alpha] Add New Tic-Tac-Toe Room by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1653
* Revert "[Alpha] Add New Tic-Tac-Toe Room" by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1746
* Don't show Seen Waypoints through walls by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1747
* Add a config option for Math Teacher Helper by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1750
* Fix Dungeon Profit Calculator for Scarf & Thorn shards by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1751
* Quick Nav: Add "Require Double Click" option by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1680

## New Contributors
* @Fyelne made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1654
* @MicrocontrollersDev made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1691
* @jadencodes made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1726

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.7.0...v5.8.0
___
# Release 5.7.0

## Highlight
 - Updated to 1.21.6-1.21.8
 - Add Dungeon Prince Features
    - Prince Score Calculation & Message
    - Prince Waypoints
 - Add Leap Overlay Features
    - Spirit Leap Keybinds
    - Leap messages
 - Dungeon Score Fixes & More Secret Waypoint Fixes
 - Simplified Teleport Maze Solver
 - Hide Wrong Items in Terminal Solvers
 - Add Museum HUD
    - Shows you what items you need to complete the museum.
    - Has customizable filtering.
 - Add Keybind Shortcuts
    - Visit `/skyblocker shortcuts` to create some!
 - Add back Event Notifications Config
    - Requires YACL to add/remove entries, editing existing entries is universally available.
 - Add Gyrokinetic Wand Overlay
 - Add Dungeonbreaker Filters
 - Add Tree Break Progress HUD
 - Add Sweep Details HUD
 - Add Config Backups
 - Add Glowing Mushroom Highlighter
 - Add ability to move Powder Mining Tracker Widget
 - Add ability to move Visitor Helper
 - Add Sellable Dungeon Items Highlighter
 - Add REI collapsible entries
 - Add fallback for missing recipe ingredients
 - Add support for Gardening Hoes to Mouse Lock
 - Remove customizability for Mimic Messages
    - Allowing them to be customized makes detecting them impossible.
 - Remove background blur for Fancy Party Finder
 - Ice Fill Solver Optimizations (3x faster)
 - Backpack Preview Optimizations
 - Change Price Refresh keybind to command
 - Improve Teleport Overlay & Smooth AOTE
 - Improve consistency of solver rendering
 - Improve Tuner Solver searchability
 - Fix Attribute Shard detection for price tooltips
 - Fix Fishing HUD not working
 - Fix Hide Soulweaver Skulls not working
 - Fix terminal click blocking not respecting mouse buttons for Change All to Same Colour
 - Fix dungeon teammate detection issues
 - Fix museum donation tooltip false positives
 - Fix Skyblock Info Display interfering with recipe quantities in REI
 - Fix enchantment books in REI
 - Fix tooltip compatibility with Firmament's REI
 - Fix Caxton compatibility with Fancy Bars text
 - Fix Chat Rules tolerating invalid regexes
 - Fix Garden Plots Widget bugs
 - Fix crash when pets have missing data
 - Fix crash with Mania Indicator
 - Fix rare scheduler crash
 - Fix outdated enchantment abbreviations
 - Fix some Slot Text adders working on unintended items
 - Fix missing option for Attribute Level Helper
 - Fix mission option for Evolving Items Slot Text
 - Fix translation string placeholders with Lunar Client
 - Fix missing translation strings

## What's Changed
* Add missing strings for Item Pickup Widget by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1550
* feat: Option for Evolving Items and New Year Cake Slot Text by @jani270 in https://github.com/SkyblockerMod/Skyblocker/pull/1546
* Gyro wand radius overlay by @617excelle in https://github.com/SkyblockerMod/Skyblocker/pull/1317
* Add Tree Break Progress HUD by @DaysSky in https://github.com/SkyblockerMod/Skyblocker/pull/1403
* 1.21.6-1.21.8 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1518
* Fix Garden Plots Widget tooltips being offset (on 1.21.6+) by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1557
* Fix Garden Plots Widget text not showing (on 1.21.6+) by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1558
* fix tooltip adders not working on firmament's REI by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1551
* Disable Chat Rule if Regex Pattern is invalid by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1561
* Backup Configs by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1439
* Allow moving Powder Mining Tracker Widget by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1560
* Museum HUD by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1170
* draggable visitor helper by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1556
* Fix Visitor Helper Clicks Being Offset by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1565
* caxton compat for outlined text by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1554
* fixes skyblocker settings button tooltip by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1569
* Better Party Finder Settings Page Bug Fixes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1533
* fix AttributeLevelHelper not showing in config and make CommissionLabels work when CommsWidget is disabled by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1549
* Update Enchantment Abbreviations for SB 0.23.3 by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1577
* Fix Attribute Shards by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1574
* fix fishing hud by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1576
* Museum overlay changes by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1571
* Optimize ice fill solver from around 270 ms to around 90 ms by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1580
* Fix Better Party Finder Locked Text on 1.21.6+ by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1578
* Glowing Mushroom by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1279
* Add `JsonData` and make `ProfiledData` extend it by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1345
* Hide item slots in solvers by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1441
* Improvements to Teleport overlay and smooth AOTE by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1435
* Use Line Feed by @ninjaguardian in https://github.com/SkyblockerMod/Skyblocker/pull/1485
* Spirit Leap Message by @layou233 in https://github.com/SkyblockerMod/Skyblocker/pull/1583
* Add Collapsible Entries in REI Item List by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1475
* Add Sellable Dungeon Items Highlighter by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1579
* Fix museum donation status false positives by @lunaynx in https://github.com/SkyblockerMod/Skyblocker/pull/1586
* Fix REI Skyblock Info Display Changing Recipe Quantities by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1589
* Fix Sellable Items Highlighter Slots by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1588
* Fix Enchanted Books in REI Item List by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1585
* Fix Dungeon Player Regex Not Matching When Level is Missing by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1593
* Rename Enchanted Book Collapsible Categories to the Enchantment Name by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1592
* Reload Recipes After the Item Repository Has Finished Loading by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1591
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1545
* Update spotless and clean up en_us.json by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1584
* Add Item Price Lookup to Museum Overlay by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1595
* Add Fallback for Missing Items in Recipes by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1599
* Support Basic and Advanced Gardening Hoes for Mouse Lock by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1601
* Fix: Change `/skyblocker protectItem` to always send output without respecting the config option by @Bloxigus in https://github.com/SkyblockerMod/Skyblocker/pull/1603
* Improve searchability of Tuner Solver by @lunaynx in https://github.com/SkyblockerMod/Skyblocker/pull/1606
* Sweep Details Hud Widget by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1413
* [Feat] Pest Highlight and Stereo Harmony Helper during Jacob's Contest by @SteveKunG in https://github.com/SkyblockerMod/Skyblocker/pull/1537
* Keybind Shortcuts by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1604
* [Alpha] Add Dungeonbreaker Simple Chat Filter by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1607
* Fix Puzzles Regex Not Matching if Username is Missing by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1608
* Only show Teleport Maze Pads through walls if Debug is Enabled by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1609
* Remove background blur on Fancy Party Finder Screen by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1610
* Replace %d with %s by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1611
* Fix Update Notification Channel Names Not Showing Correctly by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1617
* Add back event notifications config by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1618

## New Contributors
* @617excelle made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1317
* @layou233 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1583
* @Bloxigus made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1603

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v.5.6.0...v5.7.0
___
# Release 5.6.0

## Highlight
 * Config Changes
    * Added Support for **MoulConfig** which has much better searching
    * MoulConfig is now the default interface
    * You can switch between MoulConfig and YACL in Misc tab
    * **Fabric Language Kotlin is now required** since MoulConfig needs it
  * Add Fancy Dungeon Map
    * You can now see player heads on the map instead of markers
    * The fancy map also shows in the Spirit Leap Overlay
    * More to come!
 * Secret Waypoint Fixes
 * Add Player Head Customization
    * You can customize the look of player heads with `/skyblocker custom` just like your armour!
 * Add GUI for renaming items
    * Accessed with `/skyblocker custom renameItem`
 * Add ability to move the Fancy Bars anywhere on screen
 * Add Blood Camp Helper
 * Add Dungeon Splits Widget
 * Add Door Key Highlighter
 * Add Moonglade Beacon Tuner Helper
 * Add Bazaar Quick Quantities
 * Add Basic Networth Calculator for Profile Viewer
 * Add Block Incorrect Clicks for Experimentation Table
 * Add Sack Message Prices
 * Add Toggle Lottery Chat Filter
 * Add Disable All Command
 * Add Reorder Helper config
 * Add Autocomplete for /rngmeter, and /rng
 * Add Autocomplete for /joininstance, /joindungeon, and /joinkuudra
 * Add support for super craft input to sign calculator
 * Add Wiki Lookup support for visitors
 * Add Skyblock Info Display to REI
 * Add Bestiary Slot Text
 * Add log suppression for invalid fishing bobbers
 * Add option to split sack and inventory notifications for the Item Pickup Widget 
 * Waypoint import fixes and improvements
 * Various improvements for the Search Overlay
 * Various localization improvements and changes
 * Improved compatibility with chat message detection
 * Changed some titles to not have a prefix
 * Fix some messages not sending to party chat
 * Fix issues with legacy ordered waypoints
 * Fix for Smooth AOTE
 * Fix some Slot Text and Tooltip features
 * Fix Title Container position not defaulting to screen centre
 * Fix bottom QuickNav buttons being slightly offset
 * Fix Fetchur Solver not mentioning other door types
 * Fix integer overflow with Farming HUD
 * Fix Sea Lumies Highlighter not disabling properly
 * Fix Starred Mob Glow not applying to player mobs
 * Fix lag when opening the recipe book
 * Fix prioritization of REI Plugin
 * Fix Mute Enderman Sounds not working in some cases
 * Fix a rare crash with purse parsing
 * Fix a rare crash with the Commissions Widget
 * Fix a rare crash in F3/M3

## What's Changed
* Fix Guardian Health Text Crash by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1460
* Add other wooden doors to fetchur by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/1450
* Fix skyblocker's REI compat taking priority over Firmament's by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1474
* Config Multi Backend + MoulConfig by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1467
* Bestiary Level Slot Text & Slot Text Fixes by @MisterCheezeCake in https://github.com/SkyblockerMod/Skyblocker/pull/1452
* Bazaar Quick Quantities by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1354
* Basic Networth calculator for Profile Viewer by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1431
* Don't apply Hunting Box tooltip adder for shards in the inventory by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1479
* Fix Smooth AOTE working in Dojo by @DaysSky in https://github.com/SkyblockerMod/Skyblocker/pull/1477
* Support Item Price Lookup for Items in the Recipe Book by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1476
* Add support for new shards in Search Overlay by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1427
* Block incorrect clicks in experiment table by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1447
* Add Sack Message Prices by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1379
* Disable All Command by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1454
* Dungeon splits by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1464
* Refactor NEURepoManager by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1480
* Blood Camp Helper by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1446
* Don't append [SKYBLOCKER] to 270 and 300 Titles by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1492
* Only send Livid Color to party chat by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1494
* fix quick nav offset by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1487
* Livid Highlighter Localization Cleanup + Custom Color Option by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1486
* Tuner solver by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1430
* Fix Item Price Lookup for Enchanted Books by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1498
* Do not log invalid owner for fishing bobber on Hypixel by @SteveKunG in https://github.com/SkyblockerMod/Skyblocker/pull/1513
* Remove [SKYBLOCKER] Prefix from Livid Color Title by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1507
* Fix powder mining codec initialization order by @ninjaguardian in https://github.com/SkyblockerMod/Skyblocker/pull/1502
* Add a description to quick navigation toggle by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1515
* Add a Config Option to Disable Bazaar Reorder Helper by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1508
* Fix Max Pet Level for Jade Dragon and Searching for Null Ovoids in Search Overlay by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1509
* Make quicknav grab backgroundScreen by @ninjaguardian in https://github.com/SkyblockerMod/Skyblocker/pull/1488
* waypoint import options + fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1434
* save warps to file in case api takes a nap by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1313
* Strong and independent bars by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1432
* [WIP] Standardized text style by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1380
* use longs to prevent overflow in FarmingHud by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1527
* Only Send "Mimic Dead" Message to Party Chat by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1523
* Standardized text style by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1522
* smooth aote fix by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1520
* Customize player heads by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1423
* REI /viewrecipe Crafting, Skyblock Info Display  by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1496
* Added Visitor Wiki Lookup by @SteveKunG in https://github.com/SkyblockerMod/Skyblocker/pull/1512
* Fix Sea Lumies being highlighted when highlighter is disabled by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1493
* Fix speed cap preset and sign calculator conflict by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1530
* fix: some mixin injections preventing things in cases they shouldn't by @meowora in https://github.com/SkyblockerMod/Skyblocker/pull/1495
* Fancy Dungeon Map by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1239
* Make title container default to center by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1538
* Waypoints import fix by @CapinolDev in https://github.com/SkyblockerMod/Skyblocker/pull/1484
* add option to split sack and inventory notifications of pickup widget by @salbeira in https://github.com/SkyblockerMod/Skyblocker/pull/1387
* Improve secrets detection by @GatienDoesStuff in https://github.com/SkyblockerMod/Skyblocker/pull/1483
* Fix black glow on dungeon mobs when Class-Based Player Glow is enabled by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1540
* Rename item GUI by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1490
* fix bug and set text field focused by default by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1542

## New Contributors
* @MisterCheezeCake made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1452
* @DaysSky made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1477
* @SteveKunG made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1513
* @CapinolDev made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1484

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.5.0...v.5.6.0
___
# Release 5.5.0

## Highlight
* Resource pack compatibility
* Sweep Overlay
  Disclaimer: Sweep Overlay is not accurate on Galatea due to technical limitations
  the sweep formula is currently unknown.
* Forge recipe 
* Show Input Calculator on Bazaar flip input sign
* Add up/down buttons to move waypoint indices
* Fix ColorSelectionWidget showing on chain armor
* Fix Farming Hud Skill EXP not working when Fancy Status bars are disabled

## What's Changed
* Initial Sweep Overlay by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1415
* Small improvement to Swing Animation by @contionability in https://github.com/SkyblockerMod/Skyblocker/pull/1440
* Fix ColorSelectionWidget showing on chain armor. by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1438
* Item list things and forge!!!!!! by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/948
* Add Spotless and Checkstyle by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1189
* Fix Farming Hud Skill EXP not working when Fancy Status bars are disabled by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1449
* Show Input Calculator on Bazaar flip input sign by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1457
* resourcepack compatibility by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1407

## New Contributors
* @contionability made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1440

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.4.0...v5.5.0
___
# Release 5.4.0

## Highlight
* Use custom vanilla GSON config serialization
  The mysterious config wipe errors were the result of malformed json,
  normal GSON is extensively tested and is highly unlikely to produce that
  itself but YACL uses an intermediary parser that has had similar
  problems with this stuff in the past.
* Add HOTF perk level slot text adder
* Add hunting box helper
* Hunting Mobs features
* Hunting Box Shard Prices
* Add a minimum pickle count to sea lumies
* Add attribute level slot text
* Same Color Terminal Solver
* Copy Underbid Price to Clipboard
* Add a config option to toggle item protection chat notifications
* Calculator improvements
* Config for config button and customize button
* Add attribute list for bazaar price support
* Add new shards to dungeon profit calculators
* Sea Lumies Highlighter improvements
* Fix ARGBTextInput crash when pasting
* Fix Item Pickup Widget Network Protocol Error Disconnect
* Get rid of ETF log spam
* Fix suggestions flashing sometimes when removing history entry
* New items like Fig and Mangrove Logs are now displayed as the correct item (not a player head)

## What's Changed
* Add HOTF perk level slot text adder by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1391
* ignore seen waypoints outside loaded chunks by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1392
* Add hunting box helper by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1394
* fix ARGBTextInput crash when pasting by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1408
* Fix Item Pickup Widget Network Protocol Error Disconnect by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1416
* Add a minimum pickle count to sea lumies by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1395
* Fix hunting box helper not matching non-player head items by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1400
* Add attribute level slot text by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1399
* Get rid of ETF log spam by @ninjaguardian in https://github.com/SkyblockerMod/Skyblocker/pull/1404
* Copy Underbid Price to Clipboard by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1370
* Add a config option to toggle item protection chat notifications by @salbeira in https://github.com/SkyblockerMod/Skyblocker/pull/1382
* Fix suggestions flashing sometimes when removing history entry by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1383
* Calculator improvements by @Ownwn in https://github.com/SkyblockerMod/Skyblocker/pull/1368
* config for config button and customize button by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1390
* Same Color Terminal Solver by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1372

## New Contributors
* @ninjaguardian made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1404
* @salbeira made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1382

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.3.0...v5.4.0
___
# Release 5.3.0

## Highlight
* Fancy Armor Customization GUI
* Added legacy attribute background 
* Add Lushlilac and Sea Lumies Highlighters 
* Add forest temple puzzle solver 
* Add collapse button to waypoint screen
* Item pickup Widget
* Adds an oxygen bar
* Add waypoints to forest nodes
* Add Absorb Stacking Enchant Progress
* One flow waterboard solver
* Visitor helper added a copy amount and open Bazaar option
* Treasure Hunter Waypoints
* Farming HUD choose between NPC, Bazaar, or Both for calculating coin/h
* Farming HUD now displays which pricing source is being used
* Fancy Bar Overflow display options
* Add ingame fishing hook timer display
* Add basic ordered waypoint commands
* Item Background Refactor + Jacob's Contest Medal Background Colors
* Add /warp garden and /setspawn buttons to gardens plots widget
* Add slottext for "choose pet" and "skyblock guide"
* Confirmation Prompt Helper 2.0
* Refresh inventory search highlight when slot changes
* Show all warp autocompletes for Bingo profiles
* Add optional item name parameter to search overlay commands
* Add separate wiki lookup keybinds
* Add more slot text to chocolate factory
* Add Deleting Items from AH/BZ Search Overlay History
* Increase Chat Rule Character Limit
* Set the default of hideStatusEffectOverlay to true
* Removes the Estimated Price from Bazaar Items
* Highlights items with attributes
* Recipe book search bar remember your last input
* Fix CompactorDeletorPreview related crash
* Fix Croesus Profit
* Fix jacobs event toast being too small in certain languages
* Fix wishing compass solver distance
* Fix npc price tooltip to include price * itemCount
* Improve scheduler thread safety to prevent crashes
* Fixed a bug where the fishing widget would not update
* Fix customize button not moving when recipe book is toggled + refactor
* Fixed the garden plot widget not being clickable
* Fix Inventory status effects are now hidden while the garden plot widget is active
* Fix allow numpad enter key to close signs
* Fix chest value not accounting for item count in sacks & stashes
* Fix some tabwidget icons being incorrect
* Fix Turbo books and Ultimate Jerry names in Search Overlay
* Fix update cooldown for new axe throw ability
* Fix Bobber Time widget not working after max time was exceeded
* Fix Only allow placing protected items into item frames while on Private Island or Garden
* Fix Fancy Party Finder Debug button only in debugging mode
* Fixed issues with waypoints not saving
* Fixed bugs with dungeon secret waypoints and puzzle solvers not working
* Fixed issues with the fishing helper being inaccurate
* Fixed showing profile id fallback messages

## What's Changed
* fix gradle timout by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1251
* Add basic ordered waypoint commands by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1232
* Remove Legacy Ordered Waypoints by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1242
* Confirmation Prompt Helper 2.0 by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1245
* Add slottext for "choose pet" and "skyblock guide" by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1264
* Add /warp garden and /setspawn buttons to gardens plots widget by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1266
* Item Background Refactor + Jacob's Contest Medal Background Colors by @Aerhhh in https://github.com/SkyblockerMod/Skyblocker/pull/1262
* Fix CompactorDeletorPreview related crash by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1274
* Fix Croesus Profit by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1290
* Epic Armor Customization GUI by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1215
* Treasure Hunter Waypoints by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1246
* Add ingame fishing hook timer display by @RoxareX in https://github.com/SkyblockerMod/Skyblocker/pull/1269
* fix jacobs event toast being too small in certain languages by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1292
* Things by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1285
* Refresh inventory search highlight when slot changes by @Ownwn in https://github.com/SkyblockerMod/Skyblocker/pull/1259
* fix wishing compass solver distance by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1296
* Show all warp autocompletes for Bingo profiles. by @Kaluub in https://github.com/SkyblockerMod/Skyblocker/pull/1297
* Change tips to use to new armour customization gui by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1299
* more status bar anchors by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1298
* Fix npc price tooltip to include price * itemCount by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1301
* optional parse int by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1303
* un-hardcode slot ids in SkillLevelAdder by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1305
* Improve scheduler thread safety to prevent crashes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1306
* Verify Json Files by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1310
* kitty yay by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1293
* Fixed a bug where the fishing widget would not update by @Teyxos in https://github.com/SkyblockerMod/Skyblocker/pull/1294
* Item pickup Widget by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1295
* Bar changes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1307
* Remove hover text on chat waypoint suggestion message by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1314
* Fix customize button not moving when recipe book is toggled + refactor by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1316
* Clarify Search Overlay translation for Bazaar/AH by @jani270 in https://github.com/SkyblockerMod/Skyblocker/pull/1329
* space by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1332
* fix npe by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1336
* Add waypoints to forest nodes by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1325
* Add Absorb Stacking Enchant Progress by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1339
* Update EnchantmentLevelAdder.java by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1338
* Garden fixes by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1271
* One flow waterboard solver by @SpaceMonkeyy86 in https://github.com/SkyblockerMod/Skyblocker/pull/1283
* Allow numpad enter key to close signs by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1333
* Update EnchantmentLevelAdder again by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1347
* Add periods to sentences by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1348
* Add optional item name parameter to search overlay commands by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1323
* Add separate wiki lookup keybinds by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1324
* Fix chest value not accounting for item count in sacks & stashes by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1337
* Add more slot text to CF by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1342
* Increase Chat Rule Character Limit by @Bedrock-Armor in https://github.com/SkyblockerMod/Skyblocker/pull/1356
* Make custom item rename more friendly by @Ownwn in https://github.com/SkyblockerMod/Skyblocker/pull/1355
* Add Deleting Items from AH/BZ Search Overlay History by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1346
* Capitalize "buy"/"sell" in BazaarPriceTooltip by @Aerhhh in https://github.com/SkyblockerMod/Skyblocker/pull/1358
* Clean up fishing stuff by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1340
* fix: some tabwidget icons being incorrect by @j10a1n15 in https://github.com/SkyblockerMod/Skyblocker/pull/1362
* Add translatable text by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1363
* Fix Turbo- books and Ultimate Jerry names in Search Overlay by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1366
* Adds an oxygen bar by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1367
* update cooldown for new axe throw ability by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1343
* Add REI exclusion zone for Visitor Helper Gui by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1371
* Fix dungeon entrance pos detection by @GatienDoesStuff in https://github.com/SkyblockerMod/Skyblocker/pull/1374
* Set the default of hideStatusEffectOverlay to true by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1376
* Removes the Estimated Price from Bazaar Items by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1377
* Highlights items with attributes by @PeytonBrown in https://github.com/SkyblockerMod/Skyblocker/pull/1369
* Remove deprecated space assignments from build.gradle by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1386
* Fix Bobber Time widget not working after max time was exceeded by @Alex33856 in https://github.com/SkyblockerMod/Skyblocker/pull/1384
* Small Fixes and Recipe Book Search Persistence by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1276

## New Contributors
* @Aerhhh made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1262
* @RoxareX made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1269
* @Teyxos made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1294
* @Alex33856 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1314
* @jani270 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1329
* @PeytonBrown made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1325
* @Bedrock-Armor made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1339
* @SpaceMonkeyy86 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1283

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.2.0...v5.3.0
___
# Release 5.2.0

## Highlight
* 1.21.5 update
* Fishing HUD and Notifications
* Fix slayer Crash
* Fix foraging axes cooldown
* Fix visitor helper

## What's Changed
* Slayer crash fix by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1238
* 1.21.5 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1256
* Fishing HUD and Notifications by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1183
* Update chat cords parsing to support common "X Y Z" format by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1222
* Code of conduct by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1230
* fix foraging axes cooldown by @PonomarevIK in https://github.com/SkyblockerMod/Skyblocker/pull/1254
* visitor helper: dont display "x0 stacks" text by @PonomarevIK in https://github.com/SkyblockerMod/Skyblocker/pull/1258

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.1.2...5.2.0
___
# Release 5.1.2

## Highlight
* Fix Fossil solver
* Fix Visitor Helper

## What's Changed
* Fix fossil solver again by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1224
* the final visitor fix hopefully by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1225

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.1.1...v5.1.2
___
# Release 5.1.1

## Highlight
* Fix swing animation bug
* Fix leap overlay opening outside of dungeons
* Fix fossil solver bugs
* Fix Visitor Helper

## What's Changed
* Fix swing animation causing right clicks to also send left clicks by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1217
* fix leap overlay opening outside of dungeons by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1218
* fix fossil solver bugs by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1216
* Visitor Helper fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1219

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.1.0...v5.1.1
___
# Release 5.1.0

## Highlight
* Spirit Leap Overlay
* Corpse profit tracker
* Entity health bars
* Garden Tweaks
* Fossil Solver
* Slot text stuff
    * HOTM Perk Level
    * Discrite
    * Moby-Duck
* Teleport overlay color config
* Update Ice Fill Room solver
* Swing On Abilities
* Chat rule location config overhaul
* Fixed Vampire Slayer Helper
* Fixed rooms failing to match occasionally

## What's Changed
* Fix incorrect translation for SkillLevelAdder by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1161
* Fix right icon not rendering lol by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1171
* Add seen waypoint by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1108
* Fix recipe book crashes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1121
* Formatters by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1151
* Fix the way jacob's contest widget is parsed and displayed by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1162
* Boosted Pets Background Rarity by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1168
* Fixed Vampire Features not working by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1169
* Add "Swing On Abilities" by @Manchick0 in https://github.com/SkyblockerMod/Skyblocker/pull/1127
* Move disabled slots out of the way by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1132
* Slot Text Stuff by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1134
* Chat rule location config overhaul by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1138
* Corpse profit tracker by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1152
* Fix npc price tooltip crash with cookie's mod by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1176
* Health bars by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1028
* Garden Tweaks & Features by @WannaBeIan in https://github.com/SkyblockerMod/Skyblocker/pull/1118
* Event Start Time in Calendar menu by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1147
* Fossil Solver by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1156
* Fabric updates by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1172
* Add teleport overlay color config by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1175
* Migrate to formatters by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1185
* Fix empty validLocations set failing to be decoded by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1197
* Add support for Abbreviations in health values by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1199
* Fix dungeon entrance room physical pos being detected incorrectly by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1200
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/1163
* Spirit Leap Overlay by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1202
* Update Ice Fill Room by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1203
* Render Layer Migration by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1205
* Remove Skyblock Time Logging by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1206
* Auto Pet support for the Pet Cache by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1207
* Update Ordered Waypoints Tooltip by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1208
* Fix dungeon floor ordering in pv by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1210
* Simplify hiding world loading screens by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1209
* Add Moby-Duck as an evolving item by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/1212
* Fix catacombs level adder showing incorrect levels by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1213

## New Contributors
* @WannaBeIan made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1118

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v5.0.0...v5.1.0
___
# Release 5.0.0
To avoid confusion with Minecraft's versioning system, we are transitioning from Semantic Versioning (SemVer) to a Unity like versioning scheme: 
- x.y.z
  - x: Increment by 1 every year. Since this project started in 2020, the version number 5.0.0 corresponds to the year 2025.
  - y: Major or minor increment.
  - z: Represents patches, fixes, or hotfixes.

This approach ensures that our version numbers remain distinct and are not mistaken for Minecraft's versions.

## Highlight
* Fancy Tab Hud 2.0 (back and customizable)
* Slayer Stuff
  * Slayer Bossbars
  * Slayer HUD
  * Boss slain time
  * Personal Best slain time
  * Boss and MiniBoss spawn alert
  * Mute Enderman sounds
  * Laser phase Timer
* Teleport Maze Solver
* Wardrobe helper based on hotbar keybinds
* Daedalus axe as farming tool
* Garden Plots Widget
* Speed Presets (ranchers boots)
* Floor 7 Terminal/Device Waypoints
* Powder mining tracker
* Corpse Finder
* Carnival Helpers
* Smooth AOTE
* CTRL/CMD + F to search inventory
* Estimated Diana burrow pos
* Share position and set waypoint
* Speed Status Bar
* Sack Item Autocomplete
* Viewstash Autocomplete
* Fel Head Glow
* Bits Helper
* Salvage Helper
* Math teacher helper
* More slot text (Evolving Item, New Year Cake, Wardrobe slot)
* Tweak texture positions for quick craft
* Pet rarity backgrounds in pets menu
* 1.8.9 mushroom hitbox
* Skyblock ID tooltip
* Prevent placing protected items in item frames
* Option to share fairy grottos
* Hide latency icons in the tab
* Customizable night vision strength
* Valuable Consumable Protection
* Dwarven Mines unbreakable carpet highlighter
* Dungeon Class-based player glow
* Enchantment abbreviations as slot text
* Raffle task highlighter
* Fix Skin Transparency
* Fix Nukekubi Heads
* Fix kings scent detection
* Fix BazaarPriceTooltip crash in sacks
* Fix PV failing to fetch skins
* Fix NPE
* Fix offhand slot moving around
* Fix crash on missing pet info in slayer rewards
* Fix undyed leather armour turning black

## What's Changed
* Use ASM compile-time class transformation for class init via an `@Init` annotation by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/924
* Improved vampire icon in profile viewer by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/969
* Editor Config by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/800
* Estimated diana burrow pos thing by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/714
* BazaarLookup Small Edit by @VeritasDL in https://github.com/SkyblockerMod/Skyblocker/pull/884
* [Location Share & Waypoint] Share position and set waypoint by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/916
* Added Speed Status Bar #855 by @Westsi in https://github.com/SkyblockerMod/Skyblocker/pull/945
* [Slayers] Adds Slayer Bossbars by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/940
* Add wardrobe helper based on hotbar keybinds by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/943
* Update Networth Calculator + Fix EMI Log Spam by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1001
* Various Fixes  by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1004
* Random fixes by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1006
* Some Other fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1011
* fix kings scent detection for wishing compass by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1012
* More consistent wishing compass triangulation by @wahfl2 in https://github.com/SkyblockerMod/Skyblocker/pull/1003
* Dont glow f/m4 minis by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/996
* Add debug config and a keybind to dump nearby entities by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/961
* added 2 items to ItemCooldowns by @VeritasDL in https://github.com/SkyblockerMod/Skyblocker/pull/1018
* Add SalvageHelper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/999
* 1.21.2/1.21.3 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1030
* Glacite waypoints (update) by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/1008
* Add clamps to wherever they might be needed in DungeonScore by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1035
* Fix Skin Transparency Correction by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1036
* Fixed NukeKubi Heads not highlighing due to hypixel skin changes update by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1037
* Add math teacher helper by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1041
* Fix NPE when there is no intersection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1042
* Add more slot text adders by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1025
* Egg finder fix by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/1039
* Fix offhand slot moving around by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1045
* Fix slot text not rendering when multiple adders add to same slot by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1046
* Add colored item dump output by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1048
* Fix Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1050
* Revert Egg finder fix (#1039) Base64 skins by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/1059
* Fix PV NPE with irregular inventory data by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1053
* Crystal Hollows chest highlighter fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1055
* Add configurable waypoints for Goldor phase on f7. by @GatienDoesStuff in https://github.com/SkyblockerMod/Skyblocker/pull/934
* Shortcuts config fix by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/973
* Added Purse API and Purse Change Event by @Westsi in https://github.com/SkyblockerMod/Skyblocker/pull/950
* CTRL + F to search inventory by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/933
* Fix slot text scaling and update essence adders by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1023
* Tweak texture positions for quick craft by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1031
* add old mushroom hitbox by @KhafraDev in https://github.com/SkyblockerMod/Skyblocker/pull/1051
* Item Stuff by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1060
* Fel Head Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1061
* Random status bar changes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1009
* Item DFU Unification by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1054
* Waypoints refactor by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/976
* Alternate Skyblock ID tooltip design by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1066
* Sack Item Autocomplete by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1062
* Add viewstash suggestions by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1067
* 1.21.4 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1070
* Update issue templates by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1075
* Fix crash on missing pet info in slayer rewards by @darkkeks in https://github.com/SkyblockerMod/Skyblocker/pull/1080
* Fix hiding other players rods by @darkkeks in https://github.com/SkyblockerMod/Skyblocker/pull/1074
* Prevent placing protected items in item frames by @darkkeks in https://github.com/SkyblockerMod/Skyblocker/pull/1073
* Option to share fairy grottos by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1072
* Remove Mineshaft from Commission Label by @j10a1n15 in https://github.com/SkyblockerMod/Skyblocker/pull/1049
* Update slayer icons by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/970
* Excessive hud rework by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/816
* Add Daedalus axe as farming tool + others by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1084
* Random refactors & fixes by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1085
* Hide latency icons in the tab by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1081
* Use empty input for REI recepies intead of AIR by @darkkeks in https://github.com/SkyblockerMod/Skyblocker/pull/1076
* Add customizable night vision strength feature by @Manchick0 in https://github.com/SkyblockerMod/Skyblocker/pull/1078
* Add more detailed error messages by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1087
* Clamp between 0 and 1 by @Manchick0 in https://github.com/SkyblockerMod/Skyblocker/pull/1088
* Small Fix by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1089
* Chat Confirmation Prompt Helper by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1063
* Valuable Consumable Protection by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1090
* Fix undyed leather armour turning black by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1091
* Fix reparty command by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1092
* Fix Glowing by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1093
* Add Garden Plots Widget by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/929
* Slayer System rework by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1040
* Add player list widget by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1086
* Fix ender nodes detection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1095
* Change election over text by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1099
* forgot the garden plots texture lol by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1097
* Add unbreakable carpet highlighter by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1034
* Fix Simon Says Solver by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1100
* Mob glow refactor by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1101
* Fix the powder widget to also display the diff by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1103
* tab hud related fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1104
* Slayer Widget Preview for Tab HUD by @7azeemm in https://github.com/SkyblockerMod/Skyblocker/pull/1105
* Bits Helper by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/939
* Accept integer names by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1110
* Title hud refactor by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/951
* Add Speed Presets for configurable speed settings by @Manchick0 in https://github.com/SkyblockerMod/Skyblocker/pull/1111
* Class-based player glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1113
* Fix pet icon and command history by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1114
* Add powder mining tracker by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1065
* Cancel Component Update Animation by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1116
* Remove unused code and change translation by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1117
* Add feature to close sign screens with the enter key by @Ownwn in https://github.com/SkyblockerMod/Skyblocker/pull/1122
* Corpse Finder by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/960
* Per Slot Text Toggle by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1043
* Profiled data api by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1102
* ItemList Changes by @Manchick0 in https://github.com/SkyblockerMod/Skyblocker/pull/1119
* Dynamic ProgressComponent text color by @DatL4g in https://github.com/SkyblockerMod/Skyblocker/pull/1123
* Fix config not loading properly by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1129
* Random fixes by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1120
* Carnival Helpers by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1124
* garden plots fixes by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1126
* Tab hud additions by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1128
* Smooth AOTE by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/963
* Fix dungeon score not working when fancy tab hud is disabled by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1145
* add enchantment abbreviations as slot text by @Morazzer in https://github.com/SkyblockerMod/Skyblocker/pull/1056
* Add client game test by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1106
* Add Teleport Maze Solver + small things by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/1115
* Fix JGit using insteadOfs (how?) by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/1137
* Remove unused setting by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/1144
* Fix forge HUD tooltips about locked slots 5,6,7 by @Myitian in https://github.com/SkyblockerMod/Skyblocker/pull/1146
* Add raffle task highlighter by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/1155
* Properly empty layout in AuctionViewScreen by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1150
* Pet Widget Fix by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/1149
* Update Combofilter by @AC19970 in https://github.com/SkyblockerMod/Skyblocker/pull/1148

## New Contributors
* @Westsi made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/945
* @wahfl2 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1003
* @7azeemm made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1037
* @darkkeks made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1080
* @j10a1n15 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1049
* @Manchick0 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1078
* @Ownwn made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1122
* @DatL4g made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1123
* @Morazzer made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1056
* @Myitian made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/1146

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.22.2...v5.0.0
___
# Release 1.22.2

## Highlight
* Fix Egg Finder bugs
* Backend stuff

## What's Changed
* Fix Egg Finder bugs by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/978
* Block JGit from loading the system Git config by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/980
* mod publish upload changes  by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/977
* Update NEU repo parser to 1.6.0 by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/982

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.22.1...v1.22.2
___
# Release 1.22.1

## Highlight
* Display Slot Text and Protected Items in PV
* Request Profile Id if we don't receive it
* Fix Ultrasequencer Bugs
* Fix NoSuchElementException with modelless Abicases
* Fix PV Collections Crash
* Fix WebSocket bugs

## What's Changed
* Fix Ultrasequencer Bugs by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/942
* Fix NoSuchElementException with modelless Abicases by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/953
* Slot Text and Item Protection support for the PV by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/954
* Request Profile Id if we don't receive it by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/947
* [Pv & SlayerHighlight] Fixes and improvements by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/952
* Tooltip Info Rework + Performance improvements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/957
* Fix PV Collections Crash by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/959
* Fix WebSocket bugs by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/964

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.22.0...v1.22.1
___
# Release 1.22.0

## Highlight
- Profile Viewer (/pv or /skyblocker pv) note: You may have to delete an old default shortcut if /pv doesn't work
- Dojo helper
- Simon Says Solver & Lights On Solver
- Jerry timer
- JEI support for Item List
- Equipment in inventory
- Craft Cost Tooltip
- Special Effects for rare dye drops
- Essence Shop Price
- Complete sack bz and npc sell price
- Repeated ding when a stray rabbit appears
- Completed commission highlighter
- Crystal hollows Auto detect waypoint from chat for example when talking to king
- Bazaar Helpers (Highlights, Slot Text)
- Nucleus waypoints
- Wishing compass solver
- Correct livid highlight in m5
- Option to increase fog radius in Crimson Isles
- True HEX display for dye items
- Chest Value on minion generated stuff
- Kuudra glow
- Kuudra danger warning
- Crystal Hollows Treasure Chest Highlighter
- Quick nav tooltip
- Skyblock XP Messages
- Crystal Waypoints server-sided sharing via WebSocket
- Dungeon fairy room door highlight
- Tooltip display Estimated Item Value (chest value now uses estimated item value)
- Blaze Slayer Helper (Attunement highlighting, Fire Pillar Countdown Notifiications)
- Slayer Highlights (Hitbox Slayer Mob Highlighting, Glow Effect Slayer Mob Highlighting)
- Update Notifications by chat message and toast
- Improve location detection and reparty command with the Hypixel Mod API
- Block trading and auctioning protected items
- Make Ordered Waypoints waypoint type configurable via global setting
- Enhance shortcuts redirects
- Fix Emi and rei now displaying again
- Fix Superpairs Solver not starting properly
- Fix Nukekubi head highlight
- Fix Visitor Helper readding NPCs after you accept/deny them
- Fix a lot of config crashes because of null values
- Fix quick nav tooltip config
- Fix Farming Hud Crash
- Fix item debug crashes
- Fix slot text bugs with pets
- Fixed a memory leak
- Fix a rare crash in the Click In Order terminal
- Fixes recipe book search being case sensitive
- Fix some Egg Finder bugs
- Fix Skytils v1 waypoint importing
- Fix log spam from dungeon secrets widget

## What's Changed
* Change OptionalInt costs to OptionalLong by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/809
* Profile Viewer by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/708
* Nucleus waypoints by @UpFault in https://github.com/SkyblockerMod/Skyblocker/pull/738
* Refactor and add more tips by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/752
* Add completed commission highlight by @Vricken in https://github.com/SkyblockerMod/Skyblocker/pull/797
* Remove the development environment branch from the isInLocation methods by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/810
* Fix wrong icon being shown when the player has enough gems to instantly upgrade by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/814
* Change README.md version from 1.20.6 to 1.21 by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/819
* Dojo helper by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/747
* Jerry timer by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/748
* Migrate to setup gradle by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/768
* Equipment in inventory by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/718
* Schedule mayor cache ticking to the next year rather than every 20 mins by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/751
* Add Craft Cost Tooltip by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/781
* Multiply npc price tooltip by the amount of items in the sack by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/782
* Fix PetLevelAdder working incorrectly in the sea creatures guide by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/817
* Fix discord announcement not including minecraft version by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/821
* Make ordered waypoints' waypoint type configurable by the global waypoint config by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/825
* Same formatting in features by @Mininoob46 in https://github.com/SkyblockerMod/Skyblocker/pull/826
* Fix EggFinder highlight not being removed upon new egg spawn by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/811
* Profile Viewer some changes by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/818
* Repeated ding when a stray rabbit appears by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/828
* Fix waypoints Skytils v1 importing by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/822
* Shortcut arguments redirect by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/824
* Fix control test helper for dojo by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/831
* Locraw remover 9000 by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/829
* Simon Says Solver & Lights On Solver by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/724
* Aaron's Mod compatibility for profile viewer by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/833
* Item Protection Enhancements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/836
* Workaround "fake" transparent pixels by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/838
* Fix recipe book search being case sensitive by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/839
* Bazaar Helper by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/728
* Change colo(u)r for max collections and skills slot text by @Vricken in https://github.com/SkyblockerMod/Skyblocker/pull/804
* Add some custom command argument types by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/834
* Fix quicknav buttons 8, 9 and 12 having empty string as ui title by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/840
* Fix result text displaying weirdly when its too long in the recipe book by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/842
* Fix dungeon secret widget spamming logs by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/843
* Some fixes and some opinionated changes for the en_us lang file by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/841
* EssenceShopPrice feature by @UpFault in https://github.com/SkyblockerMod/Skyblocker/pull/737
* make all formatting use static instances of numberformat using US locale by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/765
* Update Attribute Short Names by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/844
* Fix Memory Leak by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/853
* Profileviewer Copy Instead of Mutating ItemRepo Itemstack map by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/857
* Fix rare npe in OrderTerminal by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/848
* Fix inconsistency with Hypixel's inconsistencies by @IllagerCaptain in https://github.com/SkyblockerMod/Skyblocker/pull/852
* Refactor container matcher implementations into interfaces by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/788
* Improve mining waypoint by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/791
* Make ChocolateFactorySolver a singleton to fix tooltip not working by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/863
* use bounding box instead of block size by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/726
* emi/rei, Quicknav, livid and nukekubi head (eman boss head) fix by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/812
* Fix empty categories being checked by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/851
* Move to Mod API for isOnSkyblock detection by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/854
* Bazaar Lookup/Misc Options by @VeritasDL in https://github.com/SkyblockerMod/Skyblocker/pull/792
* Option to increase fog radius when in the Crimson Isles by @IBeHunting in https://github.com/SkyblockerMod/Skyblocker/pull/856
* Fix slot text not working on favorited pets by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/868
* Center quick nav buttons in bigger UIs by @nea89o in https://github.com/SkyblockerMod/Skyblocker/pull/871
* Add slot text to Rabbit levels in the Chocolate Factory by @aiden-powers in https://github.com/SkyblockerMod/Skyblocker/pull/827
* Tooltip Tweaks by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/835
* Clean up id mess and add tests by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/867
* Fix Roman Numeral parsing crash by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/882
* Fix Farming Hud Crash by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/883
* Fix item debug crashes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/886
* Add raytracing to the egg finder to make it fairer by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/845
* Chest Value: Only show minion's generated stuff and visual upgrade by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/861
* Kuudra Part 2 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/873
* Profile Viewer Use Taming Levels Added to API. Maybe fix wardrobe + inv. by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/880
* Treasure Chest Highlighter for crystal hollows by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/849
* Quick nav tooltip by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/865
* Skyblock XP Messages by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/872
* Added description to hideEmptyTooltips option. by @aiden-powers in https://github.com/SkyblockerMod/Skyblocker/pull/893
* Crystal Waypoints server-sided sharing via WebSocket by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/895
* PV Tweaks by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/874
* Better dungeon door detection/overlay by @GatienDoesStuff in https://github.com/SkyblockerMod/Skyblocker/pull/879
* Fix Croesus profit estimator on master mode chests by @GatienDoesStuff in https://github.com/SkyblockerMod/Skyblocker/pull/900
* Fix clean pickonimbuses showing diamond pickaxe durability by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/906
* Fix visitor helper readding visitors after you accept/deny them by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/907
* True Hex Display for Vincent & Rare Dye Drop Special Effects by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/885
* Fix a lot of config crashes because of null values by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/902
* Fix quick nav tooltip config by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/909
* 1.21.1 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/898
* Networth by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/806
* Fast util refactor by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/896
* Slayer Helpers Slayer Highlights, Blaze Helpers and Glow Caching by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/888
* Crystals Waypoint WebSocket Enhancements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/917
* Fix SuperpairsSolver not starting properly by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/918
* Chat rules thingamabobs by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/897
* Fix pet neu id crash by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/921
* Update Networth Calculator to fix NPE by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/928
* Fix paul's minister perk not being accounted for by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/923
* Always load NEU repo by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/920
* Make enums translatable and fix line break issue by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/932
* Make Warp Autocomplete adapt to the player's rank by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/930
* Call ExperimentSolver reset method by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/937
* Mixins Review by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/936
* Change Rarity Background Default Settings by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/935
* Update Notifications by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/877
* Mark all secrets missing in subcommand by @btwonion in https://github.com/SkyblockerMod/Skyblocker/pull/927
* Add JEI integration by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/448
* Proper Item DFU for PV by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/913

## New Contributors
* @Vricken made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/797
* @Mininoob46 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/826
* @IllagerCaptain made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/852
* @IBeHunting made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/856
* @nea89o made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/871
* @aiden-powers made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/827
* @GatienDoesStuff made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/879

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.21.1...v1.22.0
___
# Release 1.21.1

## Highlight
- Fix Museum donating Crash
- Fix EventNotification Crash
- Fix Golden Stray Rabbits not getting highlighted

## What's Changed
* Fix Golden Stray Rabbits not getting highlighted by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/790
* Fix museum cache by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/commit/9a59ee74d4e61aa2d31076a587fbf9ed2d6c6a64
* Fix EventNotification NullPointerException by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/commit/1884bdf9e879d009e09cfd7b5b5123691ea81856
* Fix Skyblocker Api Auth No Chat report Compatibility by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/commit/4064dec28cea7a4d47140e08a5c1df097d891563


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.21.0...v1.21.0+1.21.0
___
# Release 1.21.0 for 1.21

## Highlight
- Updated to mc 1.21.0

## What's Changed
* 1.21 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/769
* Api changes the Aaron of the Azure color made by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/783


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.21.0...v1.21.0+1.21.0
___
# Release 1.21.0 for 1.20.6
mc version 1.20.6

## Highlight
* Chocolate factory helper
* Calendar Event Notifications
* Glacite Tunnels Cold Overlay
* Add onscreen info for specific items and menus like pet level (slot text)
* Config reordering
* Waypoints for commissions in dwarven mines and glacite
* Sign calculator
* Block Incorrect Terminal Clicks
* Hide Soulweaver Skulls
* Compact damage
* 2 Extra Tabs in Quick Nav
* Vanilla health/xp and fancy bar shown in parallel now possible
* Highlight chest in three weirdos
* Visitor helper copy amount to clipboard
* Search overlay improvement (better pet search, dungeon stars)
* Farming hud improvement (coins per hour)
* Waypoints UI config

## What's Changed
* Fixes and things by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/675
* Fixes to Garden UI by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/682
* 1.20.5 & 1.20.6 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/669
* Ultimate config reconfiguration by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/697
* Nothing to see here, part 2 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/698
* ??? by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/699
* Add a way to show all ordered waypoints at once by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/688
* Adds Mines Slayer starter commission and deprecates the classic hud for removal by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/692
* Fix accessories helper not working with only 1 accessory bag page by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/693
* Config Data Fixer by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/701
* Fix quite important AH bug by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/689
* add waypoints for commissions in dwarven mines and glacite. by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/690
* Add line smoothener by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/711
* Sign calculator by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/686
* Block Incorrect Terminal Clicks by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/710
* Hide Soulweaver Skulls by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/702
* Add compact damage by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/696
* Clarify the regex in CompactDamage by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/712
* Internal Staging by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/715
* Add LOCATION_CHANGE event, fired upon parsing /locraw by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/716
* Quick Nav Refactor and Clean Up, 2 Extra Tabs, Config Version 3, Config Data Fixer Refactor, Debug Dump Hovered Item Tool by @VeritasDL in https://github.com/SkyblockerMod/Skyblocker/pull/643
* Fancy status bars bits and pieces by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/704
* Migrate ThreeWeirdos to DungeonPuzzle and highlight chest by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/705
* fix bugs by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/720
* Make TerminalSolver interface & ExperimentSolver class sealed by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/722
* Add chocolate factory helper by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/683
* Glacite Overlay (Oops) by @f3shqt in https://github.com/SkyblockerMod/Skyblocker/pull/703
* chat rules - fix bugs + refactor  by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/719
* Remove extended quicknav string by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/725
* Debug class? More like Rebug class lmao gottem by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/730
* Update chocolate factory for chocolate factory 2 by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/734
* Fix egg found message being sent twice by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/729
* Fix off-by-one on cf by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/741
* Fix entrance (0) not being in the map by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/732
* VisitorHelper Tweak by @UpFault in https://github.com/SkyblockerMod/Skyblocker/pull/736
* Improve search overlay by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/739
* Calendar Event Notifications by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/691
* add locations to egg hunt by @BigloBot in https://github.com/SkyblockerMod/Skyblocker/pull/746
* Farming hud improvements by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/685
* Tooltip refactors by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/735
* Misc Changes by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/755
* Refactor Tooltips and Slot Texts by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/750
* Update jar name and add build type by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/757
* Fix CatacombsLevelAdder not working at max class levels by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/756
* A couple of chocolate factory fixes by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/749
* Waypoints by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/713
* Add event notifications criterion by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/763
* Fix egg finder sharing nonsense by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/771
* Switch metal detector regex from matches to find by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/767
* Add docs to ItemTooltip#getNeuName and ItemRepository#getItemStack and fix farming hud icon stack id by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/772
* Switch colour check for exotic tooltip by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/773
* YetAnotherChocoFactoFix by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/774
* Use NPC price if its higher by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/775
* Fix Search Overlay NPE by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/778

## New Contributors
* @BigloBot made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/682
* @f3shqt made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/703
* @UpFault made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/736

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.20.2...v1.21.0
___
# Release 1.20.2

## Highlight
* fix item repo

## What's Changed
* Fix item repo NPE by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/673

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.20.1...v1.20.2
___
# Release 1.20.1

## Highlight
* vic (the cat) made two fixes
  * fix end hud
  * added missing farming tool for detection in garden hud

## What's Changed
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/666
* fix garden icons and clamp the progress bar by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/668
* fix and things for end hud by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/667


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.20.0...v1.20.1

___
# Release 1.20.0

## Highlight
* Glacite Mining Updates 
* Fancy Crafting Table
* Fancy Auctions Browser
* Fancier Fancy Bars
* Ordered Waypoints 
* Accessories Helper 
* Metal Detector Helper
* Share crystal waypoint locations 
* Info Screen /skyblocker
* Custom Animated Dyes 
* Warp Command Auto-complete 
* Ender Nodes Helper
* Dungeon Crypts Message
* Fishing rod timer
* Hide other rods
* Prevent teammates glow during Livid

## What's Changed
* Make the crafting UI look like the vanilla crafting table by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/599
* Accessories Helper by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/613
* Ordered Waypoints by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/625
* Instanced Utils by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/632
* fix translate workflow by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/639
* Warp command auto-complete by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/630
* Custom Animated Dyes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/634
* add ability to share crystal waypoint locations by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/642
* Add Ender Nodes Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/629
* Info Screen by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/638
* Glacite Mining Updates & Fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/645
* Fancy Auctions Browser: The Sequel by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/614
* Allow drop in dungeon option for locked slots by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/624
* Add fishing fetures by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/646
* Fix highlighting empty slot by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/648
* Dungeon Crypts Message by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/640
* Add color interpolation for Dwarven Hud by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/649
* Fix has highest tier by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/650
* Metal detector helper by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/653
* Update ItemCooldowns.java by @VeritasDL in https://github.com/SkyblockerMod/Skyblocker/pull/636
* Prevent teammates glow during Livid by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/652
* Starred Mob Boxes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/647
* Java 21 CI by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/661
* Fix item repository update by @TheDearbear in https://github.com/SkyblockerMod/Skyblocker/pull/662
* Fancier bars by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/654
* Fix delete shortcut by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/660
* Fix update repository by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/663

## New Contributors
* @VeritasDL made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/636
* @TheDearbear made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/662

## New Translators
* @Vricken (Italian)
* @MrBiscuit921 (Turkish)
* @Kolobok125040 (Russian)

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.19.1...v1.20.0

___

# Release 1.19.1

## Highlight
* Fix Croesus Solver crash
* Basic garden mouse locking feature (set sensitivity to 0)
* New Year Cakes Helper
* add Zodd room (Dungeon Secrets)

## What's Changed
* Add New Year Cakes Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/603
* Basic garden mouse locking feature by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/607
* Fix Commissions Hud & Wiki Lookup by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/616
* Redraw square rarity background by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/619
* Update IF conflict by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/620
* Croesus Solver crash fix by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/621
* Fix Dwarven Hud by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/617
* Update Dungeon Secrets (Zodd Room) by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/618
* Remove Visitor from Visitor Helper when you refuse their offer by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/626
* Fix labeler by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/604

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.19.0...v1.19.1
___
# Release 1.19.0

## Highlight
* Fix dungeon detection when holding bow
* Farming HUD: shows counter, crops/min, blocks/s, farming level, farming xp/h, and yaw and pitch
* Custom Chat Rules: create custom filters for certain chat messages and then hide, announce, play sound or change the output
* Croesus profit calculator: highlights the chest with the highest outcome

## What's Changed
* Custom Chat Rules by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/560
* Automatic Labeling by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/574
* Remove no longer used Fetchur riddles by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/571
* Tweak particle direction detection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/573
* Croesus profit calculator by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/565
* Fix chat spam by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/576
* Image Repo Loader by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/578
* Fix VisitorHelper crash by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/579
* One more creeper beam by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/583
* 32 TAPs for kuudra default by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/591
* Fixes error when the directory doesn't exist by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/593
* Add breaks for force close world loading screen mod by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/587
* Farming HUD & HUD Refactor by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/589
* Fix potential stack overflow with chat rules by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/592
* Add Yang Glyph Notification by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/594
* Dungeonmap fix + rewrite by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/595
* Fix fairy and enigma souls profile loading and add chat profile detection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/596
* Fix bugs with secret detection by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/597
* Refactor Dwarven, Crystals, and Backpack Preview by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/601
* Tab hud quick fix by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/608
* Fix #609 by viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/610

## Translation update
* https://github.com/SkyblockerMod/Skyblocker/pull/582
* Chinese (Simplified) by AC19970 and PumpkinXD
* French by maDU59
* Russian by wilson-wtf, Ghost-3 and R2kip

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.18.1...v1.19.0
___
# Release 1.18.1

## Highlight
* Fix Ultrasequencer not clickable by @kevinthegreat1

## What's Changed
* Fix clicking in Ultrasequencer by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/567
* Fix workflow by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/568

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.18.0...v1.18.1
___
# Release 1.18.0

## Highlight
* **New Dungeon Solvers**:
  * Silverfish Solvers by @kevinthegreat1
  * Ice Fill Solvers by @kevinthegreat1
  * Boulder Solver by @LifeIsAParadox
* **Crystal Hollows Feature**:
  * Crystal Hollows Map by @olim88 and @AzureAaron
  * Waypoints for special locations by @olim88 and @AzureAaron
  * Powder HUD by @olim88 and @AzureAaron
    * Mithril
    * Gemstone
* **Kuudra Features** by @AzureAaron
  * Kuudra waypoints
  * No arrow poison warning
  * Low arrow poison warning
* **Search overlays for bz and ah** by @olim88
* **End HUD Widget** by @viciscat
  * Zealots
    * *Since last eye*
    * *Total zealots kills*
    * *Avg kills per eye*
  * Endstone Protector
    * *stage*
    * *Location*
* **Garden Features**:
  * Visitor helper by @esteban4567890
    * easy way to buy items that visitors require from bazaar by clicking the text
  * Disable title and chat messages for Melon/Pumpkin Dicer by @Ghost-3
* **Improve Item Protection feature** by @LifeIsAParadox
  * protect item with shortcut "v"
  * indicator in form of a star

## What's Changed
* Nothing to see here by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/526
* Crystal hollows fetures by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/523
* Beacon Highlighter performance fix + Fix fire sales widget by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/521
* Fix chest value not getting price data under certain circumstances by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/522
* Update mayor cache every 20 minutes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/525
* Update IF conflict by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/529
* Remove 1.20.2 from version selection menu in issue templates by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/530
* Add the missing starter commission to the Dwarven HUD. by @Kaluub in https://github.com/SkyblockerMod/Skyblocker/pull/532
* Kuudra Features by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/519
* Disable title and chat messages for Melon/Pumpkin Dicer by @Ghost-3 in https://github.com/SkyblockerMod/Skyblocker/pull/534
* Add Visitor helper by @esteban4567890 in https://github.com/SkyblockerMod/Skyblocker/pull/535
* Search overlays for bz and ah by @olim88 in https://github.com/SkyblockerMod/Skyblocker/pull/537
* Add End HUD Widget by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/524
* Boulder Solver by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/540
* Item Quality (dungeon drops) by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/541
* Treasure Hoarder Puncher Commission fix by @Ghost-3 in https://github.com/SkyblockerMod/Skyblocker/pull/542
* center table components in tabhud by @btwonion in https://github.com/SkyblockerMod/Skyblocker/pull/543
* Check all door blocks and fix fairy room door by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/536
* Show the diff in the Powders widget by @Ghost-3 in https://github.com/SkyblockerMod/Skyblocker/pull/544
* Add Item Floor Tier to tooltip by @Fluboxer in https://github.com/SkyblockerMod/Skyblocker/pull/546
* Better location management by @Ghost-3 in https://github.com/SkyblockerMod/Skyblocker/pull/547
* Small search overlay changes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/548
* Fix powder hud not updating when commissions hud is disabled by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/550
* Improve secrets caching behaviour by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/545
* Spider's Den Server Widget by @Ghost-3 in https://github.com/SkyblockerMod/Skyblocker/pull/553
* Fix beacon highlights persisting after the boss dies by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/554
* use mod-publish-plugin by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/556
* Add Silverfish and Ice Fill Solvers by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/533
* Fix discoveries index by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/557
* Support the local memory cache for the API by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/558
* 4 small changes by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/561
* Improve Item Protection feature by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/562

## New Contributors
* @olim88 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/523
* @Ghost-3 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/534
* @esteban4567890 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/535
* @Fluboxer made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/541

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.17.0...v1.18.0
___
# Release 1.17.0

## Highlight
* Enderman Slayer Helper by @akarahdev
* fancy Party Finder GUI by @viciscat
* dungeon puzzle Waterboard Solver by @kevinthegreat1
* f3/m3 fire freeze staff timer by @KhafraDev
* f3/m3 guardian health display by @KhafraDev
* f5/m5 livid color title by @kevinthegreat1
* remove screens when switching island by @LifeIsAParadox
* Dungeon Score by @kevinthegreat1
* greatly enhanced dungeon score by @Emirlol
* Toggle Sky Mall Filter by @AzureAaron
* tips by @kevinthegreat1
* resource pack: recolored textures in dungeons (tripwire for now) by @btwonion
* Hypixels colouring to Dwarven Mines HUD by @Kaluub
* dungeon score below map by @Emirlol
* fancy_slot_lock.png by @Thsgun
* Correct Transparent Skin Pixels by @AzureAaron

## What's Changed
* Gift Giving Emblem by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/438
* Add f3/m3 fire freeze staff timer by @KhafraDev in https://github.com/SkyblockerMod/Skyblocker/pull/436
* Fix Integer Overflow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/439
* 1.20.3 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/443
* add f3/m3 guardian health display by @KhafraDev in https://github.com/SkyblockerMod/Skyblocker/pull/442
* Update Exeriment Solvers for 1.20.3/1.20.4 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/437
* YACL 1.20.4 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/445
* Update mod to 1.16.0+1.20.3 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/444
* Update Debug Mode by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/446
* Migrate to upstreamed EnumDropdownController by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/447
* Ignore invalid entity data exceptions & broken quiz question by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/449
* Ultimate rarity by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/450
* Increase shortcuts character limit & Reset griffin burrows on world change by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/451
* Update fabric.mod.json access widener by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/455
* Update Dungeon Puzzles by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/454
* Item Utils & Random Tests by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/456
* Suppress unknown scoreboard objective warnings by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/457
* remove screens when switching island by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/458
* Suppress badlion packet warnings by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/460
* Fix Fire Sales Widget by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/461
* More Compatible Colour Parsing by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/463
* Improve occlusion culling accuracy by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/464
* Durability Fix by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/465
* Migrate to Fabric Api ClientPlayerBlockBreakEvents by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/466
* Waterboard Solver by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/467
* Correct Transparent Skin Pixels by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/468
* Add Dungeon Score by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/469
* Two fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/472
* Audit Mixins by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/473
* Fix broken quiz question by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/474
* Toggle Sky Mall Filter by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/475
* Add Waterboard toggle by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/476
* Use vanilla translations by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/479
* Add tips by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/481
* Add dungeon score title and sound by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/480
* Add livid color title by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/482
* Fix Skin Transparency applying to player skins by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/477
* Support new epoch milli obtained timestamps by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/483
* Add sync when clearing matchState by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/471
* Loom 1.5 & JGit 6.8.0 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/484
* Fix tips triggering too much by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/485
* add new built-in texture pack for recolored textures in dungeons by @btwonion in https://github.com/SkyblockerMod/Skyblocker/pull/452
* Add Hypixels colouring to Dwarven Mines HUD. by @Kaluub in https://github.com/SkyblockerMod/Skyblocker/pull/488
* 1.20.4 & 1.20.2 only issues by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/490
* Update top aligned pack.mcmeta by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/492
* More tips by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/496
* Add a custom GUI for the Party Finder in dungeons by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/487
* Dungeons improvements. by @Kaluub in https://github.com/SkyblockerMod/Skyblocker/pull/493
* Changed tips from true random to non-repeating random by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/501
* Tic Tac Toe Refactor by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/504
* YACL 3.3.2 (Critical update) by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/506
* Handle rate limits by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/507
* Fix fishing timer appearing outside Skyblock. by @Kaluub in https://github.com/SkyblockerMod/Skyblocker/pull/508
* Add dungeon score calculation on client-side by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/500
* Texture size, draw and transparency by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/509
* Fix Active Effects widget by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/510
* Make fancy_slot_lock.png by @Thsgun in https://github.com/SkyblockerMod/Skyblocker/pull/505
* Lowercase party and search strings to ignore case by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/513
* Enderman Slayer Utilities by @akarahdev in https://github.com/SkyblockerMod/Skyblocker/pull/502
* Add anitas talisman fortune boost indicator next to the relevant crop by @Emirlol in https://github.com/SkyblockerMod/Skyblocker/pull/512

## New Contributors
* @KhafraDev made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/436
* @btwonion made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/452
* @Kaluub made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/488
* @Emirlol made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/501
* @akarahdev made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/502
* @Thsgun made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/505

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.16.0...v1.17.0
___

# Release 1.16.0 (1.20.3 & 1.20.4)

## Highlight
* Mythological Ritual Helper by @kevinthegreat1
* Wither & Blood Door Highlight by @kevinthegreat1
* Enigma soul waypoint by @AzureAaron
* Player Secrets Tracker by @AzureAaron
* ChestValue by @kevinthegreat1
* Waypoints API & Custom Dungeon Secrets by @kevinthegreat1
* Exotic Tooltip by @kevinthegreat1 and @StyStatic
* F3/M3 Fire Freeze Staff Timer by @KhafraDev
* F3/M3 Guardian Health Display by @KhafraDev
* Livid Color Highlight by @kevinthegreat1
* Fix unnoticed rooms in Secret Waypoints by @kevinthegreat1
* Fix minors bugs with Experiment Solvers by @kevinthegreat1
* Blobbercysts Glow by @AzureAaron
* Museum Item Obtained Tooltips (shows on item if already donated to museum) by @AzureAaron
* Add Config Button in Skyblock Menu by @kevinthegreat1
* Backpack Preview support Custom texture packs by @kevinthegreat1
* Fix Combo Filter by @AzureAaron

## What's Changed
* Mythological Ritual Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/330
* AOTV & Pearl Secret Waypoints by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/381
* Update README.md by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/382
* Enigma Soul Waypoints by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/383
* Item Rarity Backgrounds compatibility with Backpack Preview by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/384
* Fix echo detection activating by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/385
* Refactor Backpack Preview by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/387
* Hypixel Api Proxy + Profile Id Caching by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/386
* Use Api Redirect by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/388
* Museum Item Cache by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/389
* Waypoints API & Custom Dungeon Secrets by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/390
* Fix #353 by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/391
* Exotic Tooltip & PriceInfoTooltip Cleanup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/393
* Player Secrets Tracker by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/394
* More cleanup by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/396
* Improve Diana Solver Trigger Accuracy by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/399
* Great Spook Emblem by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/400
* Issue template fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/402
* Prevent exception in fairy souls by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/403
* NIO Refactor by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/404
* The lost translation string by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/405
* Livid Color Highlight by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/406
* Square Item Rarity Background by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/407
* Fix fairy soul solver NPE if we can't connect to NEU repo by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/409
* Update yacl to 3.3.0-beta.1 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/410
* Shortcuts fix by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/411
* Fix Item Nbt Matching by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/412
* Fix Secret Waypoints by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/413
* Blobbercyst Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/414
* Fix Combo Filter by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/416
* Improve Obtained & Museum Item Tooltips by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/417
* Batched Rendering + Future Sodium Compat by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/415
* Hypixel API v2 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/419
* Workflow fixes + fix #401 by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/421
* Waypoint API by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/418
* trigger modpack by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/424
* Enhanced Museum Cache Error Handling by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/422
* Fix Garden Tab + Small Things by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/423
* Improve & Fix Compactor Preview by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/397
* Update ChestValue by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/428
* Add Wither & Blood Door Highlight by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/429
* Add Config Button in Skyblock Menu by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/430
* Update DiscordRPCManager by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/426
* Update MythologicalRitual by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/427
* Gift Giving Emblem by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/438
* Add f3/m3 fire freeze staff timer by @KhafraDev in https://github.com/SkyblockerMod/Skyblocker/pull/436
* Fix Integer Overflow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/439
* 1.20.3 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/443
* add f3/m3 guardian health display by @KhafraDev in https://github.com/SkyblockerMod/Skyblocker/pull/442
* Update Exeriment Solvers for 1.20.3/1.20.4 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/437
* YACL 1.20.4 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/445

## New Contributors
* @KhafraDev made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/436

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.15.0...v1.16.0+1.20.4

___

# Release 1.16.0

## Highlight
* Mythological Ritual Helper by @kevinthegreat1
* Wither & Blood Door Highlight by @kevinthegreat1
* Enigma soul waypoint by @AzureAaron
* Player Secrets Tracker by @AzureAaron
* ChestValue by @kevinthegreat1
* Waypoints API & Custom Dungeon Secrets by @kevinthegreat1
* Exotic Tooltip by @kevinthegreat1 and @StyStatic
* Livid Color Highlight by @kevinthegreat1
* Fix unnoticed rooms in Secret Waypoints by @kevinthegreat1
* Blobbercysts Glow by @AzureAaron
* Museum Item Obtained Tooltips (shows on item if already donated to museum) by @AzureAaron
* Add Config Button in Skyblock Menu by @kevinthegreat1
* Backpack Preview support Custom texture packs by @kevinthegreat1
* Fix Combo Filter by @AzureAaron

## What's Changed
* Mythological Ritual Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/330
* AOTV & Pearl Secret Waypoints by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/381
* Update README.md by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/382
* Enigma Soul Waypoints by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/383
* Item Rarity Backgrounds compatibility with Backpack Preview by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/384
* Fix echo detection activating by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/385
* Refactor Backpack Preview by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/387
* Hypixel Api Proxy + Profile Id Caching by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/386
* Use Api Redirect by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/388
* Museum Item Cache by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/389
* Waypoints API & Custom Dungeon Secrets by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/390
* Fix #353 by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/391
* Exotic Tooltip & PriceInfoTooltip Cleanup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/393
* Player Secrets Tracker by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/394
* More cleanup by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/396
* Improve Diana Solver Trigger Accuracy by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/399
* Great Spook Emblem by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/400
* Issue template fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/402
* Prevent exception in fairy souls by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/403
* NIO Refactor by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/404
* The lost translation string by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/405
* Livid Color Highlight by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/406
* Square Item Rarity Background by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/407
* Fix fairy soul solver NPE if we can't connect to NEU repo by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/409
* Update yacl to 3.3.0-beta.1 by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/410
* Shortcuts fix by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/411
* Fix Item Nbt Matching by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/412
* Fix Secret Waypoints by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/413
* Blobbercyst Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/414
* Fix Combo Filter by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/416
* Improve Obtained & Museum Item Tooltips by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/417
* Batched Rendering + Future Sodium Compat by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/415
* Hypixel API v2 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/419
* Workflow fixes + fix #401 by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/421
* Waypoint API by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/418
* trigger modpack by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/424
* Enhanced Museum Cache Error Handling by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/422
* Fix Garden Tab + Small Things by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/423
* Improve & Fix Compactor Preview by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/397
* Update ChestValue by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/428
* Add Wither & Blood Door Highlight by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/429
* Add Config Button in Skyblock Menu by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/430
* Update DiscordRPCManager by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/426
* Update MythologicalRitual by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/427

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.15.0...v1.16.0

___

# Release 1.15.0

## Highlight
* New configuration frontend (YACL) by @AzureAaron
* Item Protection (`/skyblocker protectItem`) by @AzureAaron
* Item Rarity Backgrounds by @AzureAaron
* Item cooldown display by @Grayray75
* Creeper beam puzzle solver by @msg-programs
* Configure Flame height by @LifeIsAParadox
* Secret Waypoint Rendering Customization by @AzureAaron
* Optimize Drill Fuel and Picko Durability with Caching by @kevinthegreat1

## What's Changed
* 1.20.2 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/324
* YACL Config by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/328
* Config Tweaks by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/334
* Item Protection by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/335
* Use standardised license identifier by @Grayray75 in https://github.com/SkyblockerMod/Skyblocker/pull/320
* Update shortcuts description by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/342
* Item Rarity Backgrounds by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/340
* sync MRREADME.md with modrinth description by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/338
* Fix recipe book slot textures by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/343
* Add Support for Custom toString Function for Enum Dropdowns by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/344
* Item cooldown display by @Grayray75 in https://github.com/SkyblockerMod/Skyblocker/pull/332
* Update Loom and Gradle by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/345
* Hook actual durability for Pickonimbus/Drills by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/341
* Optimize Scoreboard Stuff by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/346
* me.xrmvizzy -> de.hysky by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/348
* Fix potential NPE by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/352
* fix blaze by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/356
* Fix Golden Goblin Slayer showing up as Goblin Slayer on commission HUD by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/347
* Refactor Utils by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/354
* Refactor NBT Parsing by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/350
* fix quick nav background texture by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/359
* update issue template by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/360
* Creeper beam puzzle solver by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/355
* Scheduler Multithreaded Support + Refactor by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/361
* Update Fetchur solver by @alexiayaa in https://github.com/SkyblockerMod/Skyblocker/pull/363
* Codec-based Test by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/367
* Optimize Drill Fuel and Picko Durability with Caching by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/366
* fix issue_template by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/369
* Refactor NEU Repo by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/364
* change flame height by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/370
* Secret Waypoint Rendering Customization by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/371
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/325
* New Message Feedback Prefix by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/373
* Remove unused quicknav translation strings by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/374
* Fix some HUD options resetting sometimes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/337
* Patch Float/Double Field Controller Bug by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/377

## New Contributors
* @alexiayaa made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/341

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.14.0...v1.15.0
___

# Release 1.14.0

## Highlight
* Dungeon Chest Profit Calculator!
* Emi integration
* Rare Drop Special Effects! Dungeon chest
* Reduce Network Bandwidth
* Hide Status Effect Overlay
* Personal Compactor/Deletor preview
* Add new dungeon rooms secret waypoints
* hidden relic helper
* Hide fake players in social interactions screen

## What's Changed
* Fix Place Stage figure by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/272
* Utils cleanup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/274
* Update webhook_translate.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/275
* OptiFabric Compatibility by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/277
* Emi integration by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/278
* Readme fixes by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/276
* Dungeon bat by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/281
* Fix croesus helper by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/285
* change lbin to hysky.de api by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/286
* create the base64 fixer by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/283
* Fix RenderHelper#renderOutline color by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/288
* Rare Drop Special Effects! by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/289
* Cleanup by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/290
* Dev mode and refactor BackpackPreview by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/291
* lbin add shiny items by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/292
* Optimize Scheduler by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/295
* Update Quicknav pet button by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/296
* Reduce Network Bandwidth by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/297
* Dungeon Chest Profit Calculator! by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/294
* Century Emblem + Smol Refactor by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/299
* Hide Status Effect Overlay by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/298
* Modrinth Link by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/309
* fix the regexes for quicknav and add a fail-safe by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/301
* Personal Compactor/Deletor preview by @viciscat in https://github.com/SkyblockerMod/Skyblocker/pull/302
* Update Dungeon Secrets by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/306
* Improve fairy soul helper by @Grayray75 in https://github.com/SkyblockerMod/Skyblocker/pull/307
* Reduce SchedulerTest by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/308
* Optimize Base64 Replacer by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/311
* Hide fake players in social interactions screen by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/312
* Update beta.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/313
* Locraw filter fix by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/314
* Fix Jolly Pink Rocks by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/315
* Update webhook_translate.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/318
* Add hidden relic helper by @Grayray75 in https://github.com/SkyblockerMod/Skyblocker/pull/316
* Add profit calculator options and test by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/319
* Update rei and emi by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/293
* Translations update from hysky translate by @Weblate-LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/280

## New Contributors
* @viciscat made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/302
* @Grayray75 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/307

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.13.0...v1.14.0
___
# Release 1.13.0 (Dungeon Update)

## Highlight
* Dungeon Secrets by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/246
* Tic Tac Toe Solver by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/253
* Starred Mob Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/260
* Blaze Solver draws a line to the next blaze by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/248
* durability bar for Pickonimbus by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/259
* Fully configureable Fancy Tab HUD with ressourcepack by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/230
* Attribute Shard Info Display by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/263

## What's Changed
* Blaze Solver Enhancements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/248
* add issue templates by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/251
* small quicknav storage cause work in rift now by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/252
* Add alternate hypixel address argument by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/255
* Fix Croesus Helper by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/257
* Tic Tac Toe Solver! by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/253
* Very important fix by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/261
* Starred Mob Glow by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/260
* improve DungeonBlaze.java by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/254
* fix 258 and add durability bar for Pickonimbus by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/259
* Catch exceptions in CustomArmorTrims#initializeTrimCache by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/262
* Fully configureable Fancy Tab HUD by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/230
* Dungeon Secrets by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/246
* Attribute Shard Info Display by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/263
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/245
* Create webhook_translate.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/264
* Update webhook_translate.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/265
* Fix minion widget displaying wrong info when player has 11/12 minions by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/267


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.12.0...v1.13.0
___
# Release 1.12.0

## Highlight
* Item and Armour customisation, see [#commands](https://github.com/SkyblockerMod/Skyblocker#commands) for more details by @AzureAaron

## What's Changed
* Add Item Renaming & Custom Armour Dye Colours by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/234
* Separate commands sent by skyblocker from the chat history by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/235
* Make IF a conflict by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/237
* Custom Armour Trims by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/238
* Lowercased server ip by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/244


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.11.1...v1.12.0
___
# Release 1.11.1

## Highlight
* Fixes made to the mod to ensure it works with the latest Hypixel changes by @AzureAaron
* Overlay for all TP items by @Julienraptor01 and @kevinthegreat1

## What's Changed
* Update buildrelease.yml by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/223
* Mixins refactor by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/213
* add cache for beta builds by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/224
* Add Overlays for ALL forms of TP by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/220
* Fix Croesus Helper by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/226
* Fix broken Hypixel detection by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/231
* Random refactors by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/229
* Fix dungeons death counter in the tab hud by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/233


**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.11.0...v1.11.1
___
# Release 1.11.0

## Highlight
* Crimson Isle, Rift, Garden visitor Tab Hud Improvements by @AzureAaron
* Fairy Souls Helper by @kevinthegreat1
* Experiments Solvers by @kevinthegreat1
* Mirrorverse Waypoints by @AzureAaron
* Implement Vampire Slayer Features by @Futuremappermydud

## What's Changed
* Rename zh_Hant.json to zh_tw.json by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/178
* Add sound notification to fishing helper by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/179
* 1.20 Port by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/150
* Crimson Isle Tab Hud Improvements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/183
* DiscordRPC bug fixes and improvements by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/184
* Fairy Souls Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/167
* Added Rift Tab Hud + 1 Rift related fix by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/185
* Fix recipe book search field not being selectable by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/187
* Fix chat ad filter by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/188
* Workflow fix discord character limit by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/180
* Experiments Solvers by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/186
* Add Mirrorverse Waypoints + Rift Fixes by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/189
* Implement Vampire Slayer Features by @Futuremappermydud in https://github.com/SkyblockerMod/Skyblocker/pull/191
* Add Reparty Auto Rejoin by @koloiyolo in https://github.com/SkyblockerMod/Skyblocker/pull/176
* Add Motes Price Tooltip by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/192
* Fix dragged items not rendering by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/193
* Add Vampire Minion by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/194
* Title container by @Futuremappermydud in https://github.com/SkyblockerMod/Skyblocker/pull/196
* Loom 1.3 by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/197
* Clean up by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/151
* Truncate text in the recipe book by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/198
* Sort + Colourize Blessings by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/200
* Skyblock Levels fixes + Tab Hud improvements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/201
* Rendering Improvements, Fixes + Occlusion Culling by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/204
* Item fixer upper rewrite by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/203
* Shortcuts by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/195
* Add Dark Auction Tab Hud by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/205
* Fix ads filter triggering when it shouldn't by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/206
* Add show off message filter by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/207
* Garden Tab Hud Improvements by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/208
* Hide spammed warnings by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/209
* Fix hotbar lock rendering mixin by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/211
* Add new trophy fishing emblem by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/212

## New Contributors
* @Futuremappermydud made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/191
* @koloiyolo made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/176

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.10.0...v1.11.0

___
# Release 1.10.0

## Highlight
* Custom Tab HUD by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/137
* Fishing Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/157
* REI Compatibility by @lantice3720 in https://github.com/SkyblockerMod/Skyblocker/pull/148
* Barn solvers by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/134

## What's Changed
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/142
* Barn solvers by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/134
* Dungeon map scale adjustment customization  by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/127
* Add option to hide empty tooltips in inventories. by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/135
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/144
* There is now 240 fairy souls! by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/138
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/149
* minor updates by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/122
* Migrated to ClientReceiveMessageEvents and some fixes by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/131
* Renamed container package to gui and added docs by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/145
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/153
* REI Compatibility by @lantice3720 in https://github.com/SkyblockerMod/Skyblocker/pull/148
* Api migration, config command, and cleanup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/154
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/155
* Refactors & docs by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/156
* return instead of assign by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/158
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/159
* Add Fishing Helper by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/157
* Update Loom and Gradle by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/163
* Livid color by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/162
* fix repository update by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/165
* remove backup LBIN server because it desn't exist anymore by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/168
* fixing missing strings by @PumpkinXD in https://github.com/SkyblockerMod/Skyblocker/pull/170
* Translations update from hysky translate by @LifeIsAParadox in https://github.com/SkyblockerMod/Skyblocker/pull/166
* Replace tab/playerlist HUD with a more fancy version by @msg-programs in https://github.com/SkyblockerMod/Skyblocker/pull/137
* Add Dungeon Map Placement Screen by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/169

## New Contributors
* @msg-programs made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/135
* @lantice3720 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/148
* @PumpkinXD made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/170

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.9.0...v1.10.0
___
# Release 1.9.0

Supports 1.19.4

## What's Changed
* Fix trivia solver glitch by @TheColdPot in https://github.com/SkyblockerMod/Skyblocker/pull/89
* optimize Dwarven HUD & add draggable config by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/93
* Use official hypixel wiki for WikiLookup by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/94
* Corrections + Update by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/99
* Fix Fetchur solver not working by adding typo'ed variant by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/100
* Update GSON and remove old adapter by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/95
* Adding QuickNav Customization by @MiraculixxT in https://github.com/SkyblockerMod/Skyblocker/pull/111
* Fix multiple README badges by @triphora in https://github.com/SkyblockerMod/Skyblocker/pull/110
* Add Chinese translation by @catandA in https://github.com/SkyblockerMod/Skyblocker/pull/113
* Fixes crash with WikiLookup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/114
* Fixed IndexOutOfBoundsException in WikiLookup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/118
* fix LBIN by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/119
* Update to 1.19.3 by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/109
* Support for 1.19.4 & Cleanup by @kevinthegreat1 in https://github.com/SkyblockerMod/Skyblocker/pull/123
* account for the easter name change and refactor slightly by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/125
* Update gradle & mr version by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/126
* fix msg filter by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/128
* Fix config loading by @AzureAaron in https://github.com/SkyblockerMod/Skyblocker/pull/124
* port UpdateChecker to java.net.http by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/129
* Move Discord RPC connection to JOIN event + refactor by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/130
* Changes to quicknav tabs (Fixes and Additions) by @Julienraptor01 in https://github.com/SkyblockerMod/Skyblocker/pull/132
* More I18n for config by @catandA in https://github.com/SkyblockerMod/Skyblocker/pull/115
* Add Japanese by @hirochisan in https://github.com/SkyblockerMod/Skyblocker/pull/139

## New Contributors
* @TheColdPot made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/89
* @Julienraptor01 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/99
* @MiraculixxT made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/111
* @triphora made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/110
* @catandA made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/113
* @kevinthegreat1 made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/114
* @AzureAaron made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/124

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.8.2...v1.9.0

___
# Release 1.8.2

## Fixes
* Fix status bars not working after new Skyblock update by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/b3a7de41788209dab1d1f453e8b8630169096018
* Fix backpack preview not updating preview content after new Skyblock update by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/f681ad65e73b12ccd01d1fa8184a4b0abc3ee3c2
* Fix the url of the meteordev repository by @Username404-59 in https://github.com/SkyblockerMod/Skyblocker/pull/84
* Update PriceInfoTooltip.java by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/86
* Fix update notification not showing on first start-up by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/26e6a755c002a56db415d9b219ffafefbe384537

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.8.1...v1.8.2
___
# Release 1.8.1

Fixes
* Fix crash on disconnect by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/7a8ad1e4f6a7cc3e685a5199a5dd0ca0049c61ff
* Fix item price tooltip not working with some languages by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/74
* Fix spirit sceptre and other flower items having the wrong texture in the item list by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/84097869dc18443660511e656885498beb3c6bc9
* Fix "Team Treasurite Member Slayer" not showing up in commissions HUD by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/55524d92f3a15b68a52a0ea5edc9ba3765a781bb

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.8.0...v1.8.1
___
# Release 1.8.0

New Feature
* Added support for moving fancy bars by @ADON15c in https://github.com/SkyblockerMod/Skyblocker/pull/72
* Add option to use 1.8 farmland hitbox by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/678025b21af4d45518a382b546c14fb12f131114
* multi-profile support for backpack preview by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/75918834349f1068557716142e342a7d01356040

Fixes
* Fix some items having the wrong texture by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/pull/71
* reparty one at a time by @KonaeAkira in https://github.com/SkyblockerMod/Skyblocker/commit/aa5cf6bb18e84d604880ef37f2bda7250feaad2e

New Contributors
* @ADON15c made their first contribution in https://github.com/SkyblockerMod/Skyblocker/pull/72

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.7.0...v1.8.0
___
# Release 1.7.0

New Feature
* Commission HUD by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/55
* Rewrote status bars again by @ExternalTime in https://github.com/SkyblockerMod/Skyblocker/pull/68

Fixes
* Replace bootleg events with fabric api events by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/66
* Replace DiscordIPC library with a more modern one by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/69
* fixed crash on itemtooltip on legacy timestamps by @LifeIsAParadox
* Updated Trivia @LifeIsAParadox

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.6.3...v1.7.0
___
# Release 1.6.3

Fixes
* fix crash when not hovering over a slot while trying to use wikilookup by @TacoMonkey11 in https://github.com/SkyblockerMod/Skyblocker/pull/64
* Quick fix for fancybar and skyblock check for price info fetcher by @Fix3dll in https://github.com/SkyblockerMod/Skyblocker/pull/63
* Replaced most assertions with proper error handling by @ExternalTime in https://github.com/SkyblockerMod/Skyblocker/pull/65
* Fix crash when recipe in neurepo is wrong formated @LifeIsAParadox
* Use config path provided by Fabric API @KonaeAkira

**Full Changelog**: https://github.com/SkyblockerMod/Skyblocker/compare/v1.6.2...v1.6.3
___
# Release 1.6.2

Fixes
* filter for mana consumption message from action bar by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/61
* lowestbin

added french translate by @edgarogh

**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.6.1...v1.6.2
___
# Release 1.6.1

Fixes
* PriceInfoTooltip fixes by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/56
* quick and small change by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/57
* backpack preview without shift by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/60


**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.6.0...v1.6.1
___
# Release 1.6.0

This release only support 1.18.x

Skyblocker now supports 1.18.2 but requires minimum **fabricloader** version of 0.13.11

New Features
* Add backpack preview by @KonaeAkira in https://github.com/LifeIsAParadox/Skyblocker/pull/40
* Added Update Notification by @TacoMonkey11 in https://github.com/LifeIsAParadox/Skyblocker/pull/36

Fixes
* Fix item search not including lore by @KonaeAkira in https://github.com/LifeIsAParadox/Skyblocker/pull/37
* Updated fetchur solver by @ExternalTime in https://github.com/LifeIsAParadox/Skyblocker/pull/41
* customizable and improved item tooltip by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/44
* Added simple scheduler for recurring and delayed tasks by @ExternalTime in https://github.com/LifeIsAParadox/Skyblocker/pull/39
* Fix RPC crash by @TacoMonkey11 in https://github.com/LifeIsAParadox/Skyblocker/pull/45
* Use the new scheduler and use fabricLoader to get config dir by @KonaeAkira in https://github.com/LifeIsAParadox/Skyblocker/pull/49
* little fix for other items, change TimerTask to Scheduler by @Fix3dll in https://github.com/LifeIsAParadox/Skyblocker/pull/46
* Rewrote chat listener interface to use fabric events api by @ExternalTime in https://github.com/LifeIsAParadox/Skyblocker/pull/53
* migrate from log4j to slf4j by @Lifeisaparadox
* Fix RPC crash by @Lifeisaparadox
* Fix Backpack preview crash by @Lifeisaparadox

added Indonesian translate by @null2264
added russian translate by @HyperSoop

New Contributor
* @Fix3dll made their first contribution in https://github.com/LifeIsAParadox/Skyblocker/pull/44

**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.5.0...v1.6.0
___
# Release 1.5.0

New Features
* Added Wiki Lookup by @TacoMonkey11 in https://github.com/LifeIsAParadox/Skyblocker/pull/30
* Discord Rich Presence by @TacoMonkey11
* Quicknavigation by @KonaeAkira
* Recepe book by @KonaeAkira

Fixes
* Simplified drawing of status bars by @ExternalTime in https://github.com/LifeIsAParadox/Skyblocker/pull/29
* Added Discord Rich Presence with a few other small fixes by @TacoMonkey11 in https://github.com/LifeIsAParadox/Skyblocker/pull/31
* Make the item list display in the recipe book by @KonaeAkira in https://github.com/LifeIsAParadox/Skyblocker/pull/33
* Add recipe view to item list by @KonaeAkira in https://github.com/LifeIsAParadox/Skyblocker/pull/35
* Added inventory wiki lookup + small bug fixes + DiscordRPC cycle option by @TacoMonkey11 in https://github.com/LifeIsAParadox/Skyblocker/pull/34

New Contributors
* @TacoMonkey11 made their first contribution in https://github.com/LifeIsAParadox/Skyblocker/pull/30
* @KonaeAkira made their first contribution in https://github.com/LifeIsAParadox/Skyblocker/pull/32

**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.4.3...v1.5.0
___
# Release 1.4.3

changed dependency Skyblocker requires minimum **fabricloader** 0.12.11

Fixes
* security fix 

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.4.1...v1.4.2
___
# Release 1.4.2

supporting 1.18.x

Fixes
* Simplified ad filter implementation by @ExternalTime in #28

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.4.1...v1.4.2
___
# Release 1.4.1

Fixes
* Fixed color terminal solver not working for light blue color by @ExternalTime in #27
* Cleaning in PriceInfoTooltip by @Zailer43 in #26

New Contributor
* @Zailer43 made their first contribution in #26

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.4.0...v1.4.1
___
# Release 1.4.0

New features
* Added npc, lbin price into tooltip by @LifeIsAParadox
* Added museum info into tooltip by @LifeIsAParadox

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.3.0...v1.4.0
___
# Release 1.3.0

New features
* Added feature changing lever hitboxes to ones from 1.8 by @ExternalTime in https://github.com/LifeIsAParadox/Skyblocker/pull/23

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.2.2...v1.3.0
___
# Release 1.2.2

Fixes
* Small status bar changes by @ExternalTime in #21
* Fixed crash on opening color terminal with unknown color by @ExternalTime in #22

Full Changelog: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.2.1...v1.2.2
___
# Release 1.2.1

Fixes
* Various fixes by @ExternalTime in #20

**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.2.0...v1.2.1
___
# Release 1.2.0

New features
* F7 Terminal Solver: Order, Color, Name by @ExternalTime in #19
* Auto highlight Blaze puzzle by @LifeIsAParadox
* Added bazaar price to tooltip by @LifeIsAParadox

Fixes
* Small fixes to solvers by @ExternalTime in #16
* Fixed status bar parsing when using healing wands by @ExternalTime in #18
* Blaze outline off center fix @LifeIsAParadox

added German language

**Full Changelog**: https://github.com/LifeIsAParadox/Skyblocker/compare/v1.1.0...v1.2.0
___
# Release 1.1.0
Now using semantic Versioning \
thanks to @ExternalTime for the fixes and for implementing the new feature

New features
* added reparty /rp #14
* Added absorption bar on top of health bar (currently the bar length for absorption is really tiny, if you have an idea how to implement it better post a suggestion here or in the skyblocker discord). #9


Fixes
* fixed health bar always full visual bug #9
* fixed lag when opening inventory for the first time #12
* and other fixes #

___
# Release 1.0.7.1

* small fix
* fixed bug in health and map inside dungeon

___
# Release 1.0.7

* bugfixes
* add spam filter [thanks to Dessahw aka  ExternalTime] #5
* add Blaze Solver

___
# Skyblocker Fabric 1.17.1

This release updates the skyblocker mod to mc version 1.17.
This is the first time I created/edited a mod.
