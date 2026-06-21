package com.alcatrazescapee.tinkersforging.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.Optional;

import api.materials.HeadMaterial;
import api.materials.Materials;
import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader;
import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader.MaterialDefinition;

public final class AdvToolboxIntegration
{
    @Optional.Method(modid = "toolbox")
    public static void writeMaterialConfig(File dir)
    {
        List<MaterialDefinition> definitions = new ArrayList<>();
        for (Map.Entry<String, HeadMaterial> entry : Materials.head_registry.entrySet())
        {
            HeadMaterial mat = entry.getValue();
            String name = mat.getName();
            MaterialDefinition definition = MaterialConfigLoader.definition(name, mat.getCraftingItem(), MaterialConfigLoader.getFallbackMaterialColor(name, 0xffffff), mat.getHarvestLevel(), getWorkTemperature(mat.getHarvestLevel()), getMeltTemperature(mat.getHarvestLevel()), false, true, "toolbox");
            definition.replaceExisting = false;
            definition.comment = "Generated from Adventurer's Toolbox. If this id already exists, replaceExisting=false only adds toolbox part recipe support. Set replaceExisting=true to also replace material stats or create tinkersforging:tinkers_anvil/" + name + ".";
            definitions.add(definition);
        }
        definitions.sort(Comparator.comparing(definition -> definition.id));
        MaterialConfigLoader.writeIfMissing(new File(dir, "compat/adventurers_toolbox.json"), definitions);
    }

    @Optional.Method(modid = "toolbox")
    public static void addRecipes()
    {
        // Anvil recipes are intentionally pack-defined through CraftTweaker.
    }

    private static float getWorkTemperature(int tier)
    {
        return Math.min(1400f, 250f + 250f * tier);
    }

    private static float getMeltTemperature(int tier)
    {
        return getWorkTemperature(tier) + 350f;
    }
}
