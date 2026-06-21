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
    private final boolean colorFromRepresentativePixel;

    private TextureAtlasSprite materialTexture;
    private int[] materialTextureData;
    private int materialTextureWidth;
    private int materialTextureHeight;
    private int materialRepresentativeColor;

    GeneratedMaterialTexture(ResourceLocation materialTextureLocation, ResourceLocation baseTextureLocation, String spriteName)
    {
        this(materialTextureLocation, baseTextureLocation, spriteName, false);
    }

    public GeneratedMaterialTexture(ResourceLocation materialTextureLocation, ResourceLocation baseTextureLocation, String spriteName, boolean colorFromRepresentativePixel)
    {
        super(spriteName);
        this.materialTextureLocation = materialTextureLocation;
        this.baseTextureLocation = baseTextureLocation;
        this.colorFromRepresentativePixel = colorFromRepresentativePixel;
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
        materialRepresentativeColor = getRepresentativeColor(materialTextureData);

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

        int texturePixel = colorFromRepresentativePixel ? materialRepresentativeColor : getMaterialPixel(pxCoord);

        int r = multiply(multiply(red(texturePixel), red(pixel)), red(pixel));
        int g = multiply(multiply(green(texturePixel), green(pixel)), green(pixel));
        int b = multiply(multiply(blue(texturePixel), blue(pixel)), blue(pixel));
        return compose(r, g, b, alpha);
    }

    private int getMaterialPixel(int pxCoord)
    {
        int x = (pxCoord % width) % materialTextureWidth;
        int y = (pxCoord / width) % materialTextureHeight;
        return normalizeMaterialPixel(materialTextureData[y * materialTextureWidth + x]);
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
            return materialRepresentativeColor;
        }

        int inverse = 255 - materialAlpha;
        int r = (red(pixel) * materialAlpha + red(materialRepresentativeColor) * inverse) / 255;
        int g = (green(pixel) * materialAlpha + green(materialRepresentativeColor) * inverse) / 255;
        int b = (blue(pixel) * materialAlpha + blue(materialRepresentativeColor) * inverse) / 255;
        return compose(r, g, b, 255);
    }

    private static int getRepresentativeColor(int[] data)
    {
        double totalWeight = 0;
        double r = 0;
        double g = 0;
        double b = 0;
        for (int pixel : data)
        {
            int alpha = alpha(pixel);
            if (alpha > 0)
            {
                int pr = red(pixel);
                int pg = green(pixel);
                int pb = blue(pixel);
                float max = Math.max(pr, Math.max(pg, pb)) / 255f;
                float min = Math.min(pr, Math.min(pg, pb)) / 255f;
                float saturation = max <= 0f ? 0f : (max - min) / max;
                float brightness = (pr + pg + pb) / (255f * 3f);
                double weight = alpha * (0.35d + saturation) * (1.35d - brightness * 0.35d);
                totalWeight += weight;
                r += pr * weight;
                g += pg * weight;
                b += pb * weight;
            }
        }
        if (totalWeight <= 0)
        {
            return 0xffffffff;
        }
        return compose((int) (r / totalWeight), (int) (g / totalWeight), (int) (b / totalWeight), 255);
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
