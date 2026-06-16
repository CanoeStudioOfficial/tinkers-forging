![Tinker's Forging Banner Image](https://github.com/alcatrazEscapee/tinkers-forging/blob/1.12/src/main/resources/assets/banner.png?raw=true)

A mod that adds TFC Style Forging to 1.12+

**This mod has been discontinued. It will not be receiving updates or support!**

Author: AlcatrazEscapee

This mod adds basic forging to the game. You must first acquire a Tinker's Anvil and Hammer - the basic tools of the trade. Then you will need to master heating items - either in a large open Charcoal Forge or a more conservative Brick Forge. When you heat ingots to the right temperature, they become Workable. Place them in an anvil and start hitting them to try and work them into shape. For detail about how forging works, see TerraFirmaCraft 1.7.10 forging rules.

This mod has a Guide Book which is provided by Patchouli. It has explicit compatibility with Tinker's Construct, Construct's Armory, JEI, and Craft Tweaker for all your modpack making shenanigans.

### Content

* The Tinker's Anvil - an anvil for all your forging needs
* Tinker's Hammers in many modded metals
* Charcoal Piles + Forge or a Brick Forge for heating up items
* In game guide book provided by [Patchouli](https://minecraft.curseforge.com/projects/patchouli)
* Explicit mod integration with Tinker's Construct and Construct's Armor
* JEI and Craft Tweaker integration.

### Modpack Material Examples

Tinker's Forging writes material config files to `config/tinkersforging/materials` on first launch. The main material id controls generated registry names such as `tinkersforging:tinkers_anvil/diamond`. Built-in materials are written to `builtin.json`, Tinkers Construct integration writes `compat/tconstruct.json`, Adventurer's Toolbox integration writes `compat/adventurers_toolbox.json`, and custom JSON files can be added anywhere under the same folder.

```json
{
  "comment": "Example diamond material. Set load=true to enable it.",
  "load": true,
  "id": "diamond",
  "ore": "gemDiamond",
  "color": "#5eead4",
  "tier": 3,
  "workTemperature": 800.0,
  "meltingTemperature": 1400.0,
  "replaceExisting": true,
  "anvil": true,
  "enabled": true,
  "noTreePunching": false,
  "tinkersConstruct": false,
  "adventurersToolbox": false,
  "sourceItem": "minecraft:diamond",
  "sourceMeta": 0
}
```

`anvil=true` creates a matching Tinker's Anvil block. `replaceExisting=false` lets a compat JSON only add flags such as `adventurersToolbox=true` to an existing material id. `sourceItem` is optional; when present it also creates extended Tinker's Forging parts rendered from that item's item model texture.

CraftTweaker can also register item-backed extended parts:

```zenscript
mods.TinkersForging.Materials.addItemMaterial(<minecraft:diamond>);
mods.TinkersForging.Materials.addItemMaterial("diamond", <minecraft:diamond>, 3, 800, 1400);
```

![Splash Image](https://github.com/alcatrazEscapee/tinkers-forging/blob/1.12/src/main/resources/assets/splash.png?raw=true)
