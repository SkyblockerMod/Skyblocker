## Skyblocker Tab Menu Fork
This will be merged into Skyblocker when ready. It's not at the moment, but you can help by testing and opening issues.

### Known Issues
* Relevant for players:
    * Crimson Isle is not yet supported. (Getting Combat 24 is just... oof)
    * Minor visual bugs when using ImmediatelyFast
* Relevant for devs:
    * Scraping the entire player list every frame isn't ideal. (possible optimisation for later)
    * The error catching is broken and fails to deactivate the mod on parse failure. (this won't be in the final release anyways)
    * Placement of the text and icons for rendering should probably be done with the `MatrixStack` arg and not with my hacky "external" solution (`ItemRenderer.render()` couldn't accept one pre 1.19.4)

Apart from that, it's working!

------------------------ 

<img height="150" src="https://hypixel.net/attachments/skyblocker-png.2715347" />

## Skyblocker
[![modrinth statistic](https://img.shields.io/modrinth/dt/skyblocker-liap?color=00AF5C&label=Download&labelColor=cecece00AF5C&logo=modrinth)](https://modrinth.com/mod/skyblocker-liap)
[![github statistic](https://img.shields.io/github/downloads/SkyblockerMod/skyblocker/total?labelColor=cecece&color=000000&label=Download&logo=github&logoColor=black)](https://github.com/SkyblockerMod/Skyblocker/releases/latest) 
[![Build Beta](https://img.shields.io/github/actions/workflow/status/SkyblockerMod/Skyblocker/beta.yml?labelColor=cecece&label=beta&logo=github&logoColor=black)](https://github.com/SkyblockerMod/Skyblocker/actions/workflows/beta.yml) 
[![Discord](https://img.shields.io/discord/879732108745125969?logo=discord&labelColor=cecece&color=7289DA&label=)](https://discord.com/invite/aNNJHQykck)
[![modrinth statistic](https://img.shields.io/badge/buy%20me%20coffee-skyblocker?color=434B57&logo=kofi)](https://ko-fi.com/wohlhabend)


Hypixel Skyblock Mod for Minecraft 1.17.x + 1.18.x + 1.19.x

Installation guide is [here](https://github.com/SkyblockerMod/Skyblocker/wiki/installation)

## Features
<details open>
<summary>open</summary>

* Bars: Health and absorption, Mana, Defense, XP (moving fancy bars)
* Hide Messages: Ability Cooldown, Heal, AOTE, Implosion, Molten Wave, Teleport Pad Messages
* Dungeon Minimap
* Dungeon Puzzle Solver:
  * Three Weirdos
  * Blaze
  * F7 Terminal: Order, Color, Name
* Dwarven Mines Solver: Fetchur, Puzzler
* Drill Fuel in Item Durability Bar
* Hotbar Slot Lock Keybind (Select the hotbar slot you want to lock/unlock and press the lockbutton)
* price tooltip: npc, bazaar (avg, lbin), ah, museum
* reparty: write /rp to reparty
* Wiki Lookup: press f4 to open the wiki page about the held item
* Discord Rich Presence: Allows user to show either their Piggy, Bits, or location. Along with a custom message
* Quicknav: fast navigate between pets, armor, enderchest, skill, collection, crafting, enchant, envil, warp dungeon, warp hub
* Recipe book: in the vanilla recipe book all skyblock items are listed, and you can see the recipe of the item
* Backpack preview: after you clicked your backpack or enderchest once you can hover over the backpack or enderchest and hold shift to preview
* Update notification
* Commission HUD: Dwarven Mines quests
* 1.8 hitbox for lever and farmland

</details>

___
## Images
<details open>
<summary>open</summary>

<img padding="10px,0px"  src="https://user-images.githubusercontent.com/27798256/170806938-f858f0ae-4d8b-4767-9b53-8fe5a65edf56.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/minimap.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/tooltip1.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/tooltip2.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/drill.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/richpresencesmall.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/recipe.png" />
<img padding="10px,0px" height="150" src="https://hysky.de/backpack-preview.png" />

</details>

## Contribute

Everyone can contribute to Skyblocker, read [this](https://github.com/SkyblockerMod/Skyblocker/wiki/Contribute) for more information.

## Stargazers

[![Stargazers repo roster for @LifeIsAParadox/Skyblocker](https://reporoster.com/stars/SkyblockerMod/Skyblocker)](https://github.com/SkyblockerMod/Skyblocker/stargazers)

## Credits

|     [<img alt="Kraineff" src="https://github.com/Kraineff.png" width="100">](https://github.com/Kraineff)     |             [<img alt="d3dx9" src="https://github.com/d3dx9.png" width="100">](https://github.com/d3dx9)              | [<img alt="LifeIsAParadox" src="https://github.com/LifeIsAParadox.png" width="100">](https://github.com/LifeIsAParadox) | [<img alt="ExternalTime" src="https://github.com/ExternalTime.png" width="100">](https://github.com/ExternalTime) |
|:-------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
|                                    [Kraineff](https://github.com/Kraineff)                                    |                                           [d3dx9](https://github.com/d3dx9)                                           |                                   [LifeIsAParadox](https://github.com/LifeIsAParadox)                                   |                                  [ExternalTime](https://github.com/ExternalTime)                                  |

| [<img alt="Zailer43" src="https://github.com/Zailer43.png" width="100">](https://github.com/Zailer43) | [<img alt="TacoMonkey11" src="https://github.com/TacoMonkey11.png" width="100">](https://github.com/TacoMonkey11) |   [<img alt="KonaeAkira" src="https://github.com/KonaeAkira.png" width="100">](https://github.com/KonaeAkira)   | [<img alt="Fix3dll" src="https://github.com/Fix3dll.png" width="100">](https://github.com/Fix3dll) |
|:-----------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------:|
|                                [Zailer43](https://github.com/Zailer43)                                |                                  [TacoMonkey11](https://github.com/TacoMonkey11)                                  |                                   [KonaeAkira](https://github.com/KonaeAkira)                                   |                               [Fix3dll](https://github.com/Fix3dll)                                |


| [<img alt="Zailer43" src="https://github.com/ADON15c.png" width="100">](https://github.com/ADON15c) |
|:---------------------------------------------------------------------------------------------------:|
|                                [ADON15c](https://github.com/ADON15c)                                |
### Translators
German ([LifeIsAParadox](https://github.com/LifeIsAParadox)) \
Indonesian ([null2264](https://github.com/null2264)) \
Russian ([HyperSoop](https://github.com/HyperSoop)) \
French ([edgarogh](https://github.com/edgarogh) & [Julienraptor01](https://github.com/Julienraptor01))
