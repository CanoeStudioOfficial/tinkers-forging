package com.alcatrazescapee.tinkersforging.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import api.materials.HeadMaterial;
import api.materials.Materials;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader;
import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader.MaterialDefinition;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;
import toolbox.common.items.parts.ItemToolHead;

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
        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            if (!MaterialRegistry.isToolboxMaterial(material))
            {
                continue;
            }
            for (ItemType type : ItemType.advToolbox())
            {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("toolbox", type.name().substring(4).toLowerCase()));
                if (item instanceof ItemToolHead)
                {
                    ItemToolHead toolPart = (ItemToolHead) item;
                    Integer meta = getMetaFromBadlyDesignedMap(toolPart.meta_map, m -> m.getName().equals(material.getName()));
                    if (meta == null) continue;
                    ItemStack output = new ItemStack(toolPart, 1, meta);

                    String inputOre = material.getOreName();

                    if (!output.isEmpty())
                        ModRecipes.ANVIL.add(new AnvilRecipe(output, inputOre, type.getAmount(), material.getTier(), type.getRules()));
                }
            }
        }
    }

    @Nullable
    private static <K, V> K getMetaFromBadlyDesignedMap(Map<K, V> map, Predicate<V> valueTest)
    {
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            if (valueTest.test(entry.getValue()))
            {
                return entry.getKey();
            }
        }
        return null;
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
