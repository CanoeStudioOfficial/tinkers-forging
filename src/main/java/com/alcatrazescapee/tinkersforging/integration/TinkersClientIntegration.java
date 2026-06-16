/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;

import com.alcatrazescapee.tinkersforging.client.model.material.ForgingMaterialRenderInfo;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.material.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.materials.Material;

@SideOnly(Side.CLIENT)
public final class TinkersClientIntegration
{
    private static final Set<String> CUSTOM_RENDER_INFOS = new HashSet<>();

    @Optional.Method(modid = "tconstruct")
    public static void reloadMaterialRenderInfo(IResourceManager resourceManager)
    {
        CUSTOM_RENDER_INFOS.clear();
        for (Material material : TinkerRegistry.getAllMaterials())
        {
            if (hasMaterialRenderInfo(resourceManager, material))
            {
                CUSTOM_RENDER_INFOS.add(material.getIdentifier());
            }
        }
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
        if (!CUSTOM_RENDER_INFOS.contains(material.getIdentifier()) && material.renderInfo instanceof MaterialRenderInfo.Default)
        {
            return null;
        }
        return new Adapter(material.renderInfo);
    }

    private static boolean hasMaterialRenderInfo(IResourceManager resourceManager, Material material)
    {
        ModContainer registeredBy = TinkerRegistry.getTrace(material);
        if (registeredBy != null && !"tconstruct".equals(registeredBy.getModId()))
        {
            String domain = registeredBy.getModId().toLowerCase(Locale.US);
            if (exists(resourceManager, new ResourceLocation(domain, "materials/" + material.getIdentifier() + ".json")))
            {
                return true;
            }
        }
        return exists(resourceManager, new ResourceLocation("tconstruct", "materials/" + material.getIdentifier() + ".json"))
                || exists(resourceManager, new ResourceLocation("minecraft", "materials/" + material.getIdentifier() + ".json"));
    }

    private static boolean exists(IResourceManager resourceManager, ResourceLocation location)
    {
        IResource resource = null;
        try
        {
            resource = resourceManager.getResource(location);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            IOUtils.closeQuietly(resource);
        }
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
