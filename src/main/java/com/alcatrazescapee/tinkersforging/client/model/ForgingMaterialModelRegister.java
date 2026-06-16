/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
public final class ForgingMaterialModelRegister
{
    public static void register(Item item, String modelName)
    {
        final ResourceLocation location = new ResourceLocation(MOD_ID, modelName + ForgingMaterialModelLoader.EXTENSION);

        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
        {
            @Nonnull
            @Override
            public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
            {
                return new ModelResourceLocation(location, "inventory");
            }
        });
        ModelLoader.registerItemVariants(item, location);
    }

    private ForgingMaterialModelRegister() {}
}
