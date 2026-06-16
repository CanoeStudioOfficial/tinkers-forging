/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model.material;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedToolHead;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.integration.TinkersClientIntegration;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry.Definition;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
public enum ForgingMaterialTextureManager
{
    INSTANCE;

    private static final Set<ResourceLocation> BASE_TEXTURES = new HashSet<>();
    private static final Map<String, Map<String, TextureAtlasSprite>> SPRITES = new HashMap<>();

    public static void registerBaseTexture(ResourceLocation baseTexture)
    {
        BASE_TEXTURES.add(baseTexture);
    }

    public static void registerDefaultBaseTextures()
    {
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/hammer/metal"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/axe_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/pickaxe_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/shovel_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/hoe_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/sword_blade"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/hammer_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/knife_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/mattock_head"));
        registerBaseTexture(new ResourceLocation(MOD_ID, "items/saw_head"));
    }

    public static TextureAtlasSprite getSprite(ResourceLocation baseTexture, MaterialType material)
    {
        Map<String, TextureAtlasSprite> materialSprites = SPRITES.get(baseTexture.toString());
        return materialSprites == null ? null : materialSprites.get(material.getName());
    }

    public static TextureAtlasSprite getSprite(ResourceLocation baseTexture, Definition material)
    {
        Map<String, TextureAtlasSprite> materialSprites = SPRITES.get(baseTexture.toString());
        return materialSprites == null ? null : materialSprites.get(material.getId());
    }

    public static boolean hasCustomTexture(ItemStack stack)
    {
        MaterialType material = getMaterial(stack);
        ResourceLocation baseTexture = getMaterialTexture(stack);
        return material != null && baseTexture != null && getSprite(baseTexture, material) != null;
    }

    public static int getColor(ItemStack stack, int fallback)
    {
        MaterialType material = getMaterial(stack);
        if (material == null)
        {
            return fallback;
        }
        ForgingMaterialRenderInfo renderInfo = getRenderInfo(material);
        return renderInfo != null && renderInfo.useVertexColoring() ? renderInfo.getVertexColor() : fallback;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void createMaterialTextures(TextureStitchEvent.Pre event)
    {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        if (Loader.isModLoaded("tconstruct"))
        {
            TinkersClientIntegration.reloadMaterialRenderInfo(resourceManager);
        }
        ForgingMaterialRenderInfoLoader.load(resourceManager);

        SPRITES.clear();
        for (ResourceLocation baseTexture : BASE_TEXTURES)
        {
            Map<String, TextureAtlasSprite> materialSprites = new HashMap<>();
            for (MaterialType material : MaterialRegistry.getAllMaterials())
            {
                TextureAtlasSprite sprite = createTexture(event.getMap(), baseTexture, material);
                if (sprite != null)
                {
                    materialSprites.put(material.getName(), sprite);
                }
            }
            for (Definition material : ExtendedMaterialRegistry.getAll())
            {
                TextureAtlasSprite sprite = createTexture(event.getMap(), resourceManager, baseTexture, material);
                if (sprite != null)
                {
                    materialSprites.put(material.getId(), sprite);
                }
            }
            SPRITES.put(baseTexture.toString(), materialSprites);
        }
    }

    private static TextureAtlasSprite createTexture(TextureMap textureMap, ResourceLocation baseTexture, MaterialType material)
    {
        ResourceLocation customTexture = new ResourceLocation(baseTexture.getNamespace(), baseTexture.getPath() + "_" + material.getName());
        ForgingMaterialRenderInfo renderInfo = getRenderInfo(material);
        if (renderInfo == null)
        {
            return exists(customTexture) ? textureMap.registerSprite(customTexture) : null;
        }
        if (renderInfo.useVertexColoring())
        {
            return null;
        }

        TextureAtlasSprite sprite = renderInfo.getTexture(baseTexture, customTexture.toString());
        if (sprite != null && renderInfo.isStitched())
        {
            textureMap.setTextureEntry(sprite);
        }
        return sprite;
    }

    private static TextureAtlasSprite createTexture(TextureMap textureMap, IResourceManager resourceManager, ResourceLocation baseTexture, Definition material)
    {
        ForgingMaterialRenderInfo renderInfo = ItemStackMaterialRenderInfo.of(resourceManager, material.getSourceStack());
        if (renderInfo == null)
        {
            return null;
        }
        TextureAtlasSprite sprite = renderInfo.getTexture(baseTexture, baseTexture + "_extended_" + material.getId());
        if (sprite != null && renderInfo.isStitched())
        {
            textureMap.setTextureEntry(sprite);
        }
        return sprite;
    }

    private static ForgingMaterialRenderInfo getRenderInfo(MaterialType material)
    {
        if (Loader.isModLoaded("tconstruct"))
        {
            ForgingMaterialRenderInfo tinkersRenderInfo = TinkersClientIntegration.getRenderInfo(material);
            if (tinkersRenderInfo != null)
            {
                return tinkersRenderInfo;
            }
        }
        return ForgingMaterialRenderInfoLoader.get(material);
    }

    private static boolean exists(ResourceLocation sprite)
    {
        List<IResource> resources = null;
        try
        {
            ResourceLocation file = new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png");
            resources = Minecraft.getMinecraft().getResourceManager().getAllResources(file);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            if (resources != null)
            {
                for (IResource resource : resources)
                {
                    IOUtils.closeQuietly(resource);
                }
            }
        }
    }

    private static ResourceLocation getMaterialTexture(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemHammer && ((ItemHammer) stack.getItem()).getMaterial() != null)
        {
            return new ResourceLocation(MOD_ID, "items/hammer/metal");
        }
        if (stack.getItem() instanceof ItemToolHead)
        {
            return getMaterialTexture(((ItemToolHead) stack.getItem()).getType());
        }
        if (stack.getItem() instanceof ItemExtendedToolHead)
        {
            return getMaterialTexture(((ItemExtendedToolHead) stack.getItem()).getType());
        }
        return null;
    }

    private static ResourceLocation getMaterialTexture(ItemType type)
    {
        switch (type)
        {
            case AXE_HEAD:
                return new ResourceLocation(MOD_ID, "items/axe_head");
            case PICKAXE_HEAD:
                return new ResourceLocation(MOD_ID, "items/pickaxe_head");
            case SHOVEL_HEAD:
                return new ResourceLocation(MOD_ID, "items/shovel_head");
            case HOE_HEAD:
                return new ResourceLocation(MOD_ID, "items/hoe_head");
            case SWORD_BLADE:
                return new ResourceLocation(MOD_ID, "items/sword_blade");
            case HAMMER_HEAD:
                return new ResourceLocation(MOD_ID, "items/hammer_head");
            case NTP_KNIFE:
                return new ResourceLocation(MOD_ID, "items/knife_head");
            case NTP_MATTOCK:
                return new ResourceLocation(MOD_ID, "items/mattock_head");
            case NTP_SAW:
                return new ResourceLocation(MOD_ID, "items/saw_head");
            default:
                return null;
        }
    }

    private static MaterialType getMaterial(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemToolHead)
        {
            return ((ItemToolHead) stack.getItem()).getMaterial();
        }
        if (stack.getItem() instanceof ItemHammer)
        {
            return ((ItemHammer) stack.getItem()).getMaterial();
        }
        return null;
    }
}
