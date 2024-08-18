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