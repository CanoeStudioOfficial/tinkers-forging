/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model.material;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
public final class ForgingMaterialRenderInfoLoader
{
    private static final Map<String, ForgingMaterialRenderInfo> RENDER_INFOS = new HashMap<>();

    public static void load(IResourceManager resourceManager)
    {
        RENDER_INFOS.clear();
        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            ForgingMaterialRenderInfo renderInfo = load(resourceManager, material);
            if (renderInfo != null)
            {
                RENDER_INFOS.put(material.getName(), renderInfo);
            }
        }
    }

    public static ForgingMaterialRenderInfo get(MaterialType material)
    {
        return RENDER_INFOS.get(material.getName());
    }

    private static ForgingMaterialRenderInfo load(IResourceManager resourceManager, MaterialType material)
    {
        ResourceLocation location = new ResourceLocation(MOD_ID, "materials/" + material.getName() + ".json");
        Reader reader = null;
        try
        {
            reader = getReader(resourceManager, location);
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            String type = json.get("type").getAsString();
            JsonObject parameters = json.getAsJsonObject("parameters");

            if ("block".equals(type))
            {
                return new TextureMaterialRenderInfo(new ResourceLocation(parameters.get("texture").getAsString()));
            }
            if ("colored".equals(type))
            {
                return new ColorMaterialRenderInfo(fromHex(parameters.get("color").getAsString()));
            }

            TinkersForging.getLog().warn("Unknown material render info type '{}' in {}", type, location);
        }
        catch (FileNotFoundException e)
        {
            // Optional resource, ignore.
        }
        catch (Exception e)
        {
            TinkersForging.getLog().error("Unable to load material render info {}", location, e);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
        return null;
    }

    private static Reader getReader(IResourceManager resourceManager, ResourceLocation location) throws IOException
    {
        ResourceLocation file = new ResourceLocation(location.getNamespace(), location.getPath());
        IResource resource = resourceManager.getResource(file);
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
    }

    private static int fromHex(String hex)
    {
        return Integer.parseInt(hex, 16);
    }

    private ForgingMaterialRenderInfoLoader() {}
}
