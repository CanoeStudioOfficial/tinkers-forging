/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model.material;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.TinkersForging;

@SideOnly(Side.CLIENT)
public final class ItemStackMaterialRenderInfo implements ForgingMaterialRenderInfo
{
    private final ResourceLocation texture;

    private ItemStackMaterialRenderInfo(ResourceLocation texture)
    {
        this.texture = texture;
    }

    @Nullable
    public static ItemStackMaterialRenderInfo of(IResourceManager resourceManager, ItemStack stack)
    {
        ResourceLocation texture = resolveTexture(resourceManager, stack);
        return texture == null ? null : new ItemStackMaterialRenderInfo(texture);
    }

    @Override
    public TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location)
    {
        return new GeneratedMaterialTexture(texture, baseTexture, location);
    }

    @Nullable
    private static ResourceLocation resolveTexture(IResourceManager resourceManager, ItemStack stack)
    {
        ResourceLocation itemName = stack.getItem().getRegistryName();
        if (itemName == null)
        {
            return null;
        }

        ResourceLocation model = new ResourceLocation(itemName.getNamespace(), "models/item/" + itemName.getPath() + ".json");
        Reader reader = null;
        try
        {
            IResource resource = resourceManager.getResource(model);
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
            ResourceLocation texture = readLayer0Texture(new JsonParser().parse(reader).getAsJsonObject());
            if (texture != null && textureExists(resourceManager, texture))
            {
                TinkersForging.getLog().debug("Resolved material source {} to item model texture {}.", itemName, texture);
                return texture;
            }
        }
        catch (Exception e)
        {
            // Some items use custom models or generated paths. Fall through to the conventional item texture.
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
        ResourceLocation fallback = new ResourceLocation(itemName.getNamespace(), "items/" + itemName.getPath());
        if (textureExists(resourceManager, fallback))
        {
            TinkersForging.getLog().debug("Resolved material source {} to fallback item texture {}.", itemName, fallback);
            return fallback;
        }
        TinkersForging.getLog().warn("Unable to find item texture for material source {}.", itemName);
        return null;
    }

    @Nullable
    private static ResourceLocation readLayer0Texture(JsonObject json)
    {
        JsonObject textures = json.getAsJsonObject("textures");
        if (textures == null || !textures.has("layer0"))
        {
            return null;
        }
        JsonElement layer = textures.get("layer0");
        return layer == null ? null : new ResourceLocation(layer.getAsString());
    }

    private static boolean textureExists(IResourceManager resourceManager, ResourceLocation texture)
    {
        ResourceLocation file = new ResourceLocation(texture.getNamespace(), "textures/" + texture.getPath() + ".png");
        try
        {
            IResource resource = resourceManager.getResource(file);
            IOUtils.closeQuietly(resource);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
