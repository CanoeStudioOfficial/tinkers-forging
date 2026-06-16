/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model.material;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ForgingMaterialRenderInfo
{
    TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location);

    default boolean isStitched()
    {
        return true;
    }

    default boolean useVertexColoring()
    {
        return false;
    }

    default int getVertexColor()
    {
        return 0xffffff;
    }
}
