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
  "comment": "Example diamond material. JSON does not support // comments, so this comments object explains each field.",
  "comments": {
    "comment": "Human-readable note. Ignored by Tinkers Forging.",
    "comments": "Human-readable field guide. Ignored by Tinkers Forging.",
    "load": "Set true to load this entry. Set false to keep it as a disabled template.",
    "id": "Material id and registry-name suffix. id diamond creates tinkersforging:tinkers_anvil/diamond when anvil is true, plus matching hammer/hammer-head items where applicable.",
    "ore": "Ore dictionary input used by normal material recipes and heat checks. Example: gemDiamond or ingotCopper.",
    "color": "Fallback tint color when no custom material texture is found. Accepts #RRGGBB, 0xRRGGBB, or decimal.",
    "tier": "Tool/anvil tier from 0 to 5.",
    "workTemperature": "Temperature where this material becomes workable.",
    "meltingTemperature": "Temperature where this material melts or becomes too hot.",
    "replaceExisting": "true replaces an existing material with the same id. false only adds compatibility flags/sourceItem to it.",
    "anvil": "true registers tinkersforging:tinkers_anvil/<id>.",
    "enabled": "true forces the material to be usable even if the ore dictionary precondition is not found.",
    "noTreePunching": "true enables No Tree Punching compat recipes when that compat is enabled.",
    "tinkersConstruct": "true enables Tinkers Construct part recipes when TConstruct compat is enabled.",
    "adventurersToolbox": "true enables Adventurer's Toolbox part recipes when that mod is installed.",
    "requiredMod": "Optional mod id gate. The entry only loads when that mod is installed.",
    "sourceItem": "Optional item used for extended Tinkers Forging parts and an extended hammer rendered from that item's model texture.",
    "sourceMeta": "Metadata/damage value for sourceItem. Usually 0."
  },
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
  "requiredMod": "",
  "sourceItem": "minecraft:diamond",
  "sourceMeta": 0
}
```

`anvil=true` creates a matching Tinker's Anvil block. `replaceExisting=false` lets a compat JSON only add flags such as `adventurersToolbox=true` to an existing material id. `sourceItem` is optional; when present it also creates extended Tinker's Forging parts and an extended hammer rendered from that item's item model texture. The extended hammer uses one shared registry item, `tinkersforging:extended/hammer`, with the material id stored in NBT.

CraftTweaker can also register item-backed extended parts:

```zenscript
mods.TinkersForging.Materials.addItemMaterial(<minecraft:diamond>);
mods.TinkersForging.Materials.addItemMaterial("diamond", <minecraft:diamond>, 3, 800, 1400);
```

Those CraftTweaker calls generate the extended hammer head anvil recipe and the extended hammer crafting recipe automatically. Put the crafted hammer head above a stick to craft the matching extended hammer.

![Splash Image](https://github.com/alcatrazEscapee/tinkers-forging/blob/1.12/src/main/resources/assets/splash.png?raw=true)
