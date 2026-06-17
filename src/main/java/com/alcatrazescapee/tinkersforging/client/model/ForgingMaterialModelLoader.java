/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.client.model.material.ForgingMaterialTextureManager;

@SideOnly(Side.CLIENT)
public enum ForgingMaterialModelLoader implements ICustomModelLoader
{
    INSTANCE;

    public static final String EXTENSION = ".tfmat";
    public static final String ANVIL_EXTENSION = ".tfanvil";

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        String path = modelLocation.getPath();
        return path.endsWith(EXTENSION) || path.endsWith(ANVIL_EXTENSION);
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception
    {
        if (modelLocation.getPath().endsWith(ANVIL_EXTENSION))
        {
            ResourceLocation baseModelLocation = getAnvilBaseModelLocation(modelLocation);
            IModel baseModel = ModelLoaderRegistry.getModel(baseModelLocation);
            ForgingMaterialTextureManager.registerAnvilBaseTexture();
            return new TinkersAnvilModel(baseModel);
        }

        ResourceLocation baseModelLocation = getBaseModelLocation(modelLocation);
        IModel baseModel = ModelLoaderRegistry.getModel(baseModelLocation);
        ImmutableList<ResourceLocation> textures = loadTextures(baseModelLocation);
        int materialLayer = getMaterialLayer(baseModelLocation);

        if (textures.size() <= materialLayer)
        {
            TinkersForging.getLog().error("Material model {} has no layer {} to recolor.", modelLocation, materialLayer);
            return baseModel;
        }

        ForgingMaterialTextureManager.registerBaseTexture(textures.get(materialLayer));
        return new ForgingMaterialModel(baseModel, textures, materialLayer);
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {}

    private static ResourceLocation getBaseModelLocation(ResourceLocation modelLocation)
    {
        return getBaseModelLocation(modelLocation, EXTENSION);
    }

    private static ResourceLocation getAnvilBaseModelLocation(ResourceLocation modelLocation)
    {
        String path = modelLocation.getPath();
        path = path.substring(0, path.length() - ANVIL_EXTENSION.length());
        if (path.startsWith("models/"))
        {
            path = path.substring("models/".length());
        }
        if (!path.contains("/"))
        {
            path = "block/" + path;
        }
        return new ResourceLocation(modelLocation.getNamespace(), path);
    }

    private static ResourceLocation getBaseModelLocation(ResourceLocation modelLocation, String extension)
    {
        String path = modelLocation.getPath();
        path = path.substring(0, path.length() - extension.length());
        if (path.startsWith("models/"))
        {
            path = path.substring("models/".length());
        }
        if (!path.contains("/"))
        {
            path = "item/" + path;
        }
        return new ResourceLocation(modelLocation.getNamespace(), path);
    }

    private static int getMaterialLayer(ResourceLocation baseModelLocation)
    {
        String path = baseModelLocation.getPath();
        if (path.startsWith("models/item/"))
        {
            path = path.substring("models/item/".length());
        }
        if (path.startsWith("item/"))
        {
            path = path.substring("item/".length());
        }
        return "hammer".equals(path) ? 1 : 0;
    }

    private static ImmutableList<ResourceLocation> loadTextures(ResourceLocation modelLocation) throws IOException
    {
        List<ResourceLocation> textures = new ArrayList<>();
        Reader reader = getReader(modelLocation);
        try
        {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            JsonObject textureJson = json.getAsJsonObject("textures");
            if (textureJson != null)
            {
                for (int i = 0; textureJson.has("layer" + i); i++)
                {
                    JsonElement texture = textureJson.get("layer" + i);
                    textures.add(new ResourceLocation(texture.getAsString()));
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
        return ImmutableList.copyOf(textures);
    }

    private static Reader getReader(ResourceLocation location) throws IOException
    {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        String path = location.getPath();
        if (!path.startsWith("models/"))
        {
            path = "models/" + path;
        }
        ResourceLocation file = new ResourceLocation(location.getNamespace(), path + ".json");
        IResource resource = resourceManager.getResource(file);
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
    }
}
