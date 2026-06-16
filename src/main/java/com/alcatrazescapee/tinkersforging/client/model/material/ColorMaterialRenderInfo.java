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
public final class ColorMaterialRenderInfo implements ForgingMaterialRenderInfo
{
    private final int color;

    ColorMaterialRenderInfo(int color)
    {
        this.color = color;
    }

    @Override
    public TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location)
    {
        return null;
    }

    @Override
    public boolean isStitched()
    {
        return false;
    }

    @Override
    public boolean useVertexColoring()
    {
        return true;
    }

    @Override
    public int getVertexColor()
    {
        return color;
    }
}
