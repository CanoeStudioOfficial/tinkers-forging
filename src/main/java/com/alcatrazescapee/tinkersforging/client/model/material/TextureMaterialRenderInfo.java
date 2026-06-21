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
public final class TextureMaterialRenderInfo implements ForgingMaterialRenderInfo
{
    private final ResourceLocation texture;

    TextureMaterialRenderInfo(ResourceLocation texture)
    {
        this.texture = texture;
    }

    @Override
    public TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location)
    {
        return new GeneratedMaterialTexture(texture, baseTexture, location, isItemTexture(texture));
    }

    private static boolean isItemTexture(ResourceLocation texture)
    {
        String path = texture.getPath();
        return path.startsWith("items/") || path.startsWith("item/");
    }
}
