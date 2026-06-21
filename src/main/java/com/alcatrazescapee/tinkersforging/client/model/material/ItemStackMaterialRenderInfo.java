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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final int MAX_MODEL_DEPTH = 16;
    private static final String[] TEXTURE_KEYS = {"layer0", "texture", "all", "particle", "base", "layer1", "layer2"};

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
        return new GeneratedMaterialTexture(texture, baseTexture, location, true);
    }

    @Nullable
    private static ResourceLocation resolveTexture(IResourceManager resourceManager, ItemStack stack)
    {
        ResourceLocation itemName = stack.getItem().getRegistryName();
        if (itemName == null)
        {
            return null;
        }

        for (ResourceLocation model : getCandidateModels(stack, itemName))
        {
            ResourceLocation texture = resolveModelTexture(resourceManager, model);
            if (texture != null && textureExists(resourceManager, texture))
            {
                TinkersForging.getLog().debug("Resolved material source {}:{} to item model texture {} from {}.", itemName, stack.getMetadata(), texture, model);
                return texture;
            }
        }

        for (ResourceLocation fallback : getCandidateFallbackTextures(stack, itemName))
        {
            if (textureExists(resourceManager, fallback))
            {
                TinkersForging.getLog().debug("Resolved material source {}:{} to fallback item texture {}.", itemName, stack.getMetadata(), fallback);
                return fallback;
            }
        }
        TinkersForging.getLog().warn("Unable to find item texture for material source {}:{}.", itemName, stack.getMetadata());
        return null;
    }

    @Nullable
    private static ResourceLocation resolveModelTexture(IResourceManager resourceManager, ResourceLocation model)
    {
        Map<String, String> textures = new LinkedHashMap<>();
        if (!loadModelTextures(resourceManager, model, textures, new HashSet<String>(), 0))
        {
            return null;
        }

        for (String key : TEXTURE_KEYS)
        {
            ResourceLocation texture = resolveTextureKey(resourceManager, textures, key, model.getNamespace(), new HashSet<String>());
            if (texture != null)
            {
                return texture;
            }
        }

        for (Map.Entry<String, String> entry : textures.entrySet())
        {
            ResourceLocation texture = resolveTextureValue(resourceManager, textures, entry.getValue(), model.getNamespace(), new HashSet<String>());
            if (texture != null)
            {
                return texture;
            }
        }
        return null;
    }

    private static boolean loadModelTextures(IResourceManager resourceManager, ResourceLocation model, Map<String, String> textures, Set<String> visited, int depth)
    {
        if (depth > MAX_MODEL_DEPTH || !visited.add(model.toString()))
        {
            return false;
        }

        Reader reader = null;
        try
        {
            reader = getModelReader(resourceManager, model);
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

            JsonElement parent = json.get("parent");
            if (parent != null)
            {
                String parentName = parent.getAsString();
                if (!parentName.startsWith("builtin/"))
                {
                    loadModelTextures(resourceManager, new ResourceLocation(parentName), textures, visited, depth + 1);
                }
            }

            JsonObject textureJson = json.getAsJsonObject("textures");
            if (textureJson != null)
            {
                for (Map.Entry<String, JsonElement> entry : textureJson.entrySet())
                {
                    if (entry.getValue().isJsonPrimitive())
                    {
                        textures.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private static Reader getModelReader(IResourceManager resourceManager, ResourceLocation model) throws IOException
    {
        ResourceLocation file = new ResourceLocation(model.getNamespace(), "models/" + model.getPath() + ".json");
        IResource resource = resourceManager.getResource(file);
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
    }

    @Nullable
    private static ResourceLocation resolveTextureKey(IResourceManager resourceManager, Map<String, String> textures, String key, String defaultNamespace, Set<String> resolving)
    {
        String value = textures.get(key);
        return value == null ? null : resolveTextureValue(resourceManager, textures, value, defaultNamespace, resolving);
    }

    @Nullable
    private static ResourceLocation resolveTextureValue(IResourceManager resourceManager, Map<String, String> textures, String value, String defaultNamespace, Set<String> resolving)
    {
        if (value == null || value.isEmpty())
        {
            return null;
        }
        if (value.charAt(0) == '#')
        {
            String key = value.substring(1);
            if (key.isEmpty() || !resolving.add(key))
            {
                return null;
            }
            return resolveTextureKey(resourceManager, textures, key, defaultNamespace, resolving);
        }
        try
        {
            if (value.indexOf(':') >= 0)
            {
                return new ResourceLocation(value);
            }

            ResourceLocation sameNamespace = new ResourceLocation(defaultNamespace, value);
            return textureExists(resourceManager, sameNamespace) ? sameNamespace : new ResourceLocation(value);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }

    private static List<ResourceLocation> getCandidateModels(ItemStack stack, ResourceLocation itemName)
    {
        List<ResourceLocation> models = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int meta = stack.getMetadata();
        String namespace = itemName.getNamespace();
        String path = itemName.getPath();

        if (meta != 0)
        {
            addModelCandidate(models, seen, namespace, "item/" + path + "_" + meta);
            addModelCandidate(models, seen, namespace, "item/" + path + "/" + meta);
        }
        for (String suffix : getStackModelSuffixes(stack))
        {
            addModelCandidate(models, seen, namespace, "item/" + path + "_" + suffix);
            addModelCandidate(models, seen, namespace, "item/" + path + "/" + suffix);
            addModelCandidate(models, seen, namespace, "item/" + suffix);
        }
        addModelCandidate(models, seen, namespace, "item/" + path);
        return models;
    }

    private static List<ResourceLocation> getCandidateFallbackTextures(ItemStack stack, ResourceLocation itemName)
    {
        List<ResourceLocation> textures = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int meta = stack.getMetadata();
        String namespace = itemName.getNamespace();
        String path = itemName.getPath();

        if (meta != 0)
        {
            addTextureCandidate(textures, seen, namespace, "items/" + path + "_" + meta);
            addTextureCandidate(textures, seen, namespace, "items/" + path + "/" + meta);
        }
        for (String suffix : getStackModelSuffixes(stack))
        {
            addTextureCandidate(textures, seen, namespace, "items/" + path + "_" + suffix);
            addTextureCandidate(textures, seen, namespace, "items/" + path + "/" + suffix);
            addTextureCandidate(textures, seen, namespace, "items/" + suffix);
        }
        addTextureCandidate(textures, seen, namespace, "items/" + path);
        return textures;
    }

    private static List<String> getStackModelSuffixes(ItemStack stack)
    {
        List<String> suffixes = new ArrayList<>();
        try
        {
            String baseName = stack.getItem().getTranslationKey();
            String stackName = stack.getTranslationKey();
            String suffix = null;
            if (baseName != null && stackName != null && stackName.startsWith(baseName + "."))
            {
                suffix = stackName.substring(baseName.length() + 1);
            }
            else if (stackName != null)
            {
                int index = stackName.lastIndexOf('.');
                suffix = index >= 0 && index + 1 < stackName.length() ? stackName.substring(index + 1) : stackName;
            }
            if (suffix != null)
            {
                addSuffix(suffixes, suffix.toLowerCase());
                addSuffix(suffixes, toSnakeCasePath(suffix));
            }
        }
        catch (RuntimeException e)
        {
            // Some modded items do not like being queried during model stitching.
        }
        return suffixes;
    }

    private static void addModelCandidate(List<ResourceLocation> models, Set<String> seen, String namespace, String path)
    {
        ResourceLocation location = new ResourceLocation(namespace, path);
        if (seen.add(location.toString()))
        {
            models.add(location);
        }
    }

    private static void addTextureCandidate(List<ResourceLocation> textures, Set<String> seen, String namespace, String path)
    {
        ResourceLocation location = new ResourceLocation(namespace, path);
        if (seen.add(location.toString()))
        {
            textures.add(location);
        }
    }

    private static void addSuffix(List<String> suffixes, String suffix)
    {
        if (!suffix.isEmpty() && !suffixes.contains(suffix))
        {
            suffixes.add(suffix);
        }
    }

    private static String toSnakeCasePath(String value)
    {
        StringBuilder builder = new StringBuilder(value.length());
        char previous = 0;
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            if (Character.isUpperCase(c) && previous != 0 && previous != '_' && previous != '/' && !Character.isUpperCase(previous))
            {
                builder.append('_');
            }
            c = Character.toLowerCase(c);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '/')
            {
                builder.append(c);
                previous = c;
            }
            else if (builder.length() == 0 || builder.charAt(builder.length() - 1) != '_')
            {
                builder.append('_');
                previous = '_';
            }
        }
        return builder.toString();
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
