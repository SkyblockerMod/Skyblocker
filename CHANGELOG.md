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