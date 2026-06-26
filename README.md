![Tinker's Forging Banner Image](https://github.com/alcatrazEscapee/tinkers-forging/blob/1.12/src/main/resources/assets/banner.png?raw=true)

A mod that adds TFC-style forging to Minecraft 1.12.2.

This repository is the CanoeStudio-maintained 1.12.2 branch. The original upstream project was discontinued, but this branch continues development for modpack use and compatibility fixes.

Original Author: AlcatrazEscapee

Branch Maintainer: Lonelyxiya

Branch Development: CanoeStudio

This mod adds basic forging to the game. You must first acquire a Tinker's Anvil and Hammer - the basic tools of the trade. Then you will need to master heating items - either in a large open Charcoal Forge or a more conservative Brick Forge. When you heat inputs to the right temperature, they become Workable. Sneak-right-click the required input stack into an anvil and hit the anvil with a hammer to forge directly. Anvil and welding recipes are pack-defined through CraftTweaker; JEI shows only recipes that scripts add.

This mod has a Guide Book which is provided by Patchouli. It has explicit compatibility with Tinker's Construct, Construct's Armory, JEI, and Craft Tweaker for all your modpack making shenanigans.

### Content

* The Tinker's Anvil - an anvil for all your forging needs
* Tinker's Hammers in many modded metals
* Charcoal Piles + Forge or a Brick Forge for heating up items
* In game guide book provided by [Patchouli](https://minecraft.curseforge.com/projects/patchouli)
* Explicit mod integration with Tinker's Construct and Construct's Armor
* JEI and CraftTweaker integration.

### Modpack Material Examples

Tinker's Forging writes material config files to `config/tinkersforging/materials` on first launch. The main material id controls generated registry names such as `tinkersforging:tinkers_anvil/diamond`. Built-in materials are written to `builtin.json`, Tinkers Construct integration writes `compat/tconstruct.json`, Adventurer's Toolbox integration writes `compat/adventurers_toolbox.json`, and custom JSON files can be added anywhere under the same folder.

```json
{
  "comment": "Example diamond material. JSON does not support // comments, so this comments object explains each field.",
  "comments": {
    "comment": "Human-readable note. Ignored by Tinkers Forging.",
    "comments": "Human-readable field guide. Ignored by Tinkers Forging.",
    "load": "Set true to load this entry. Set false to keep it as a disabled template.",
    "id": "Material id and registry-name suffix. id diamond creates generated material content such as tinkersforging:tinkers_anvil/diamond, hammer/diamond, hammer_head/diamond, and normal tool parts when those features are enabled.",
    "ore": "Ore dictionary name associated with this material, used for material detection and heat checks. Example: gemDiamond or ingotCopper. It does not create anvil recipes by itself.",
    "color": "Fallback tint color when no custom material texture is found. Accepts #RRGGBB, 0xRRGGBB, or decimal.",
    "tier": "Tool/anvil tier from 0 to 5.",
    "workTemperature": "Temperature where this material becomes workable.",
    "meltingTemperature": "Temperature where this material melts or becomes too hot.",
    "replaceExisting": "true replaces an existing material with the same id. false only adds compatibility flags/sourceItem to it.",
    "anvil": "true registers tinkersforging:tinkers_anvil/<id>.",
    "enabled": "true forces the material's generated content to appear even if the ore dictionary precondition is not found.",
    "noTreePunching": "true enables No Tree Punching generated material content when that compat is enabled. Anvil recipes still require CraftTweaker.",
    "tinkersConstruct": "true enables Tinkers Construct-compatible generated material content when TConstruct compat is enabled. Anvil recipes still require CraftTweaker.",
    "adventurersToolbox": "true enables Adventurer's Toolbox generated material content when that mod is installed. Anvil recipes still require CraftTweaker.",
    "requiredMod": "Optional mod id gate. The entry only loads when that mod is installed.",
    "sourceItem": "Optional item registry name used as the render texture source for this JSON material's generated parts, hammer, and anvil. Resource packs that replace this item's model/texture are followed.",
    "sourceMeta": "Metadata/damage value for sourceItem. Usually 0; use another value for 1.12 metadata items with different textures."
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

JSON material configs are still the supported way to extend Tinker's Forging materials. A loaded JSON entry adds the material id to the material registry, then generated content such as hammer heads, hammers, normal tool parts, and Tinker's Anvil blocks are created from that material according to the enabled fields and compat config. `anvil=true` creates a matching Tinker's Anvil block. JSON does not add anvil or welding recipes; use CraftTweaker for gameplay recipes. `replaceExisting=false` lets a compat JSON only add flags such as `adventurersToolbox=true` or a `sourceItem` texture source to an existing material id.

`sourceItem` does not remove the JSON material system. It makes the generated material content render from an existing item model/texture, for example `minecraft:diamond`, so resource packs and modded item textures can drive the look of the generated parts, hammer, and anvil. CraftTweaker material registration was removed; use JSON files under `config/tinkersforging/materials` for this material/texture-backed extension path.

When Tinkers Construct is installed, `Tinker's Construct Compat Mode` controls which generated part items/content exist. `BOTH` registers Tinkers Forging's own normal parts and TConstruct-compatible part content together. `TINKERS_ONLY` skips Tinkers Forging's own normal `pickaxe_head/<id>`, `axe_head/<id>`, `shovel_head/<id>`, `hoe_head/<id>`, and `sword_blade/<id>` items, while hammer heads and hammers are still registered. Anvil recipes for either mode still need CraftTweaker.

### CraftTweaker Anvil Examples

Tinker's Anvil has no default anvil recipes. Add direct forging recipes with CraftTweaker. The input count controls how many items must be sneak-right-clicked into the anvil before hammering. An optional fourth integer controls the required hammer hits, similar to Pyrotech's anvil `hits` parameter.

```zenscript
// Three iron ingots directly forge into an iron pickaxe on tier 2+ anvils.
mods.TinkersForging.Anvil.addRecipe(<minecraft:iron_ingot> * 3, <minecraft:iron_pickaxe>, 2);

// Ore dictionary inputs also support stack counts.
mods.TinkersForging.Anvil.addRecipe(<ore:ingotIron> * 5, <tinkersforging:hammer_head/iron>, 2);

// JSON material/sourceItem content can be targeted by its generated registry name.
mods.TinkersForging.Anvil.addRecipe(<minecraft:diamond> * 3, <tinkersforging:pickaxe_head/diamond>, 3);

// Add a recipe that requires exactly 6 hammer hits.
mods.TinkersForging.Anvil.addRecipe(<minecraft:diamond> * 5, <tinkersforging:hammer_head/diamond>, 3, 6);

// Old rule arguments are still accepted for compatibility, but direct forging does not require them.
mods.TinkersForging.Anvil.addRecipe(<ore:ingotGold> * 2, <tinkersforging:sword_blade/gold>, 1, "HIT_LAST");

// Rules can still be combined with an explicit hammer hit count.
mods.TinkersForging.Anvil.addRecipe(<ore:ingotCopper> * 2, <tinkersforging:sheet/copper>, 1, 4, "HIT_LAST");

mods.TinkersForging.Anvil.removeRecipe(<minecraft:iron_pickaxe>);
```

Use `mods.TinkersForging.Anvil.addItemHeat(input, workTemperature, meltingTemperature);` when the input item does not already receive forge heat data from JSON material config or another integration.

### CraftTweaker Welding Examples

Tinker's Anvil has no default welding recipes. Welding follows the TerraFirmaCraft-style shape: two inputs, one output, and a minimum anvil tier. The anvil still requires flux and weldable heat in-game.

```zenscript
mods.TinkersForging.Welding.addRecipe(<ore:ingotCopper>, <ore:ingotCopper>, <modid:double_ingot_copper>, 1);
mods.TinkersForging.Welding.addRecipe(<minecraft:iron_ingot>, <minecraft:iron_ingot>, <modid:double_ingot_iron>, 2);
mods.TinkersForging.Welding.removeRecipe(<modid:double_ingot_copper>);
```

![Splash Image](https://github.com/alcatrazEscapee/tinkers-forging/blob/1.12/src/main/resources/assets/splash.png?raw=true)
