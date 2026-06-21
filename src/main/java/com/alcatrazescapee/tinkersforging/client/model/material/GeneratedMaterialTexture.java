/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model.material;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class GeneratedMaterialTexture extends TextureAtlasSprite
{
    private final ResourceLocation materialTextureLocation;
    private final ResourceLocation baseTextureLocation;

    private TextureAtlasSprite materialTexture;
    private int[] materialTextureData;
    private int materialTextureWidth;
    private int materialTextureHeight;
    private int materialFallbackColor;

    GeneratedMaterialTexture(ResourceLocation materialTextureLocation, ResourceLocation baseTextureLocation, String spriteName)
    {
        super(spriteName);
        this.materialTextureLocation = materialTextureLocation;
        this.baseTextureLocation = baseTextureLocation;
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return ImmutableList.of(baseTextureLocation, materialTextureLocation);
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
    {
        return true;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter)
    {
        TextureAtlasSprite baseTexture = textureGetter.apply(baseTextureLocation);
        materialTexture = textureGetter.apply(materialTextureLocation);

        if (baseTexture == null || materialTexture == null || baseTexture.getFrameCount() <= 0 || materialTexture.getFrameCount() <= 0)
        {
            this.width = 1;
            this.height = 1;
            return false;
        }

        this.framesTextureData = Lists.newArrayList();
        this.frameCounter = 0;
        this.tickCounter = 0;
        this.copyFrom(baseTexture);

        int[][] data = new int[baseTexture.getFrameTextureData(0).length][];
        data[0] = Arrays.copyOf(baseTexture.getFrameTextureData(0)[0], baseTexture.getFrameTextureData(0)[0].length);
        processData(data[0]);
        this.framesTextureData.add(data);
        return false;
    }

    private void processData(int[] data)
    {
        materialTextureData = materialTexture.getFrameTextureData(0)[0];
        materialTextureWidth = materialTexture.getIconWidth();
        materialTextureHeight = materialTexture.getIconHeight();
        materialFallbackColor = getAverageOpaqueColor(materialTextureData);

        for (int pxCoord = 0; pxCoord < data.length; pxCoord++)
        {
            data[pxCoord] = colorPixel(data[pxCoord], pxCoord);
        }

        materialTextureData = null;
        materialTexture = null;
    }

    private int colorPixel(int pixel, int pxCoord)
    {
        int alpha = alpha(pixel);
        if (alpha == 0)
        {
            return pixel;
        }

        int x = (pxCoord % width) % materialTextureWidth;
        int y = (pxCoord / width) % materialTextureHeight;
        int texturePixel = normalizeMaterialPixel(materialTextureData[y * materialTextureWidth + x]);

        int r = multiply(multiply(red(texturePixel), red(pixel)), red(pixel));
        int g = multiply(multiply(green(texturePixel), green(pixel)), green(pixel));
        int b = multiply(multiply(blue(texturePixel), blue(pixel)), blue(pixel));
        return compose(r, g, b, alpha);
    }

    private int normalizeMaterialPixel(int pixel)
    {
        int materialAlpha = alpha(pixel);
        if (materialAlpha >= 255)
        {
            return pixel;
        }
        if (materialAlpha <= 0)
        {
            return materialFallbackColor;
        }

        int inverse = 255 - materialAlpha;
        int r = (red(pixel) * materialAlpha + red(materialFallbackColor) * inverse) / 255;
        int g = (green(pixel) * materialAlpha + green(materialFallbackColor) * inverse) / 255;
        int b = (blue(pixel) * materialAlpha + blue(materialFallbackColor) * inverse) / 255;
        return compose(r, g, b, 255);
    }

    private static int getAverageOpaqueColor(int[] data)
    {
        long totalAlpha = 0;
        long r = 0;
        long g = 0;
        long b = 0;
        for (int pixel : data)
        {
            int alpha = alpha(pixel);
            if (alpha > 0)
            {
                totalAlpha += alpha;
                r += red(pixel) * alpha;
                g += green(pixel) * alpha;
                b += blue(pixel) * alpha;
            }
        }
        if (totalAlpha == 0)
        {
            return 0xffffffff;
        }
        return compose((int) (r / totalAlpha), (int) (g / totalAlpha), (int) (b / totalAlpha), 255);
    }

    private static int multiply(int c1, int c2)
    {
        return (int) (c1 * (c2 / 255f)) & 0xff;
    }

    private static int compose(int r, int g, int b, int a)
    {
        int color = a;
        color = (color << 8) + r;
        color = (color << 8) + g;
        color = (color << 8) + b;
        return color;
    }

    private static int alpha(int color)
    {
        return (color >> 24) & 0xff;
    }

    private static int red(int color)
    {
        return (color >> 16) & 0xff;
    }

    private static int green(int color)
    {
        return (color >> 8) & 0xff;
    }

    private static int blue(int color)
    {
        return color & 0xff;
    }
}
