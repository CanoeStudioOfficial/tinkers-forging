/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.client.model.material.ForgingMaterialRenderInfo;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.material.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.Material;

@SideOnly(Side.CLIENT)
public final class TinkersClientIntegration
{
    @Optional.Method(modid = "tconstruct")
    public static void reloadMaterialRenderInfo(IResourceManager resourceManager)
    {
        MaterialRenderInfoLoader.INSTANCE.onResourceManagerReload(resourceManager);
    }

    @Nullable
    @Optional.Method(modid = "tconstruct")
    public static ForgingMaterialRenderInfo getRenderInfo(MaterialType materialType)
    {
        Material material = TinkerRegistry.getMaterial(materialType.getName());
        if (material == Material.UNKNOWN || material.renderInfo == null)
        {
            return null;
        }
        return new Adapter(material.renderInfo);
    }

    private TinkersClientIntegration() {}

    private static final class Adapter implements ForgingMaterialRenderInfo
    {
        private final MaterialRenderInfo renderInfo;

        private Adapter(MaterialRenderInfo renderInfo)
        {
            this.renderInfo = renderInfo;
        }

        @Override
        public TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location)
        {
            return renderInfo.getTexture(baseTexture, location);
        }

        @Override
        public boolean isStitched()
        {
            return renderInfo.isStitched();
        }

        @Override
        public boolean useVertexColoring()
        {
            return renderInfo.useVertexColoring();
        }

        @Override
        public int getVertexColor()
        {
            return renderInfo.getVertexColor();
        }
    }
}
