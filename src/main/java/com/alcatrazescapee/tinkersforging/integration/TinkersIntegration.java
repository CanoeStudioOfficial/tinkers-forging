/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Optional;

import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader;
import com.alcatrazescapee.tinkersforging.util.material.MaterialConfigLoader.MaterialDefinition;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

public final class TinkersIntegration
{
    @Optional.Method(modid = "tconstruct")
    public static void init()
    {
        updateSharedConfig();
    }

    @Optional.Method(modid = "tconstruct")
    public static void writeMaterialConfig(File dir)
    {
        List<MaterialDefinition> definitions = new ArrayList<>();
        for (Material material : TinkerRegistry.getAllMaterials())
        {
            if (material.isCastable())
            {
                HeadMaterialStats headStats = material.getStats("head");
                if (headStats != null)
                {
                    float baseTemp = 200 * headStats.harvestLevel + 200;
                    if (material.hasFluid())
                    {
                        baseTemp = material.getFluid().getTemperature();
                    }
                    float meltTemp = Math.max(300, baseTemp * 3.5f - 250);
                    float workTemp = MathHelper.clamp(meltTemp * 0.8f, 150, 1400);

                    MaterialDefinition definition = MaterialConfigLoader.definition(material.getIdentifier(), MaterialConfigLoader.getDefaultOreName(material.getIdentifier()), material.materialTextColor, headStats.harvestLevel, workTemp, meltTemp, true, false, "tconstruct");
                    definition.enabled = true;
                    definition.replaceExisting = true;
                    definition.comment = "Generated from Tinkers Construct. anvil=true creates tinkersforging:tinkers_anvil/" + material.getIdentifier() + ".";
                    definitions.add(definition);
                }
            }
        }
        definitions.sort(Comparator.comparing(definition -> definition.id));
        MaterialConfigLoader.writeIfMissing(new File(dir, "compat/tconstruct.json"), definitions);
    }

    @Optional.Method(modid = "tconstruct")
    public static void updateSharedConfig()
    {
        // Hey Tinkers Construct, stop trying to access the tinker's anvil display inventory slot!
        Config.craftingStationBlacklist.add(MOD_ID + ":tinkers_anvil");
    }

}
