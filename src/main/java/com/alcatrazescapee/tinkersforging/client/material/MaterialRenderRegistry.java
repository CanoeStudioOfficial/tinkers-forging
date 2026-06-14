package com.alcatrazescapee.tinkersforging.client.material;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

public final class MaterialRenderRegistry
{
    private static final Map<String, ResourceLocation> MATERIAL_TEXTURES = new HashMap<>();
    private static final Map<String, TextureAtlasSprite> GENERATED_TEXTURES = new HashMap<>();

    private static final ResourceLocation HAMMER_METAL_TEMPLATE = new ResourceLocation(MOD_ID, "items/hammer/metal");
    private static final ResourceLocation ANVIL_TEMPLATE = new ResourceLocation(MOD_ID, "blocks/metal_block");

    public static void onTextureStitch(TextureStitchEvent.Pre event)
    {
        MATERIAL_TEXTURES.clear();
        GENERATED_TEXTURES.clear();

        IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            ResourceLocation materialTexture = readMaterialTexture(manager, material);
            if (materialTexture != null)
            {
                MATERIAL_TEXTURES.put(material.getName(), materialTexture);
            }
        }

        TextureMap map = event.getMap();
        for (ItemToolHead item : ItemToolHead.getAll())
        {
            if (hasMaterialTexture(item.getMaterial()))
            {
                registerGeneratedTexture(map, item.getMaterial(), getTemplate(item.getType()), "item_" + item.getType().name().toLowerCase());
            }
        }
        for (ItemHammer item : ItemHammer.getAll())
        {
            MaterialType material = item.getMaterial();
            if (material != null && hasMaterialTexture(material))
            {
                registerGeneratedTexture(map, material, HAMMER_METAL_TEMPLATE, "item_hammer");
            }
        }
        for (BlockTinkersAnvil block : BlockTinkersAnvil.getAll())
        {
            if (hasMaterialTexture(block.getMaterial()))
            {
                registerGeneratedTexture(map, block.getMaterial(), ANVIL_TEMPLATE, "block_tinkers_anvil");
            }
        }
    }

    public static boolean hasMaterialTexture(MaterialType material)
    {
        return material != null && MATERIAL_TEXTURES.containsKey(material.getName());
    }

    @Nullable
    public static TextureAtlasSprite getGeneratedTexture(MaterialType material, ResourceLocation template, String key)
    {
        return material == null ? null : GENERATED_TEXTURES.get(getKey(material, template, key));
    }

    public static ResourceLocation getHammerMetalTemplate()
    {
        return HAMMER_METAL_TEMPLATE;
    }

    public static ResourceLocation getAnvilTemplate()
    {
        return ANVIL_TEMPLATE;
    }

    public static ResourceLocation getTemplate(ItemType type)
    {
        switch (type)
        {
            case AXE_HEAD:
                return new ResourceLocation(MOD_ID, "items/axe_head");
            case HAMMER_HEAD:
                return new ResourceLocation(MOD_ID, "items/hammer_head");
            case HOE_HEAD:
                return new ResourceLocation(MOD_ID, "items/hoe_head");
            case NTP_KNIFE:
                return new ResourceLocation(MOD_ID, "items/knife_head");
            case NTP_MATTOCK:
                return new ResourceLocation(MOD_ID, "items/mattock_head");
            case NTP_SAW:
                return new ResourceLocation(MOD_ID, "items/saw_head");
            case PICKAXE_HEAD:
                return new ResourceLocation(MOD_ID, "items/pickaxe_head");
            case SHOVEL_HEAD:
                return new ResourceLocation(MOD_ID, "items/shovel_head");
            case SWORD_BLADE:
                return new ResourceLocation(MOD_ID, "items/sword_blade");
            default:
                return new ResourceLocation(MOD_ID, "items/" + type.name().toLowerCase());
        }
    }

    private static void registerGeneratedTexture(TextureMap map, MaterialType material, ResourceLocation template, String key)
    {
        ResourceLocation materialTexture = MATERIAL_TEXTURES.get(material.getName());
        if (materialTexture == null)
        {
            return;
        }

        String generatedName = MOD_ID + ":generated/materials/" + key + "_" + material.getName();
        TextureAtlasSprite sprite = new MaterialTextureSprite(generatedName, template, materialTexture);
        map.setTextureEntry(sprite);
        GENERATED_TEXTURES.put(getKey(material, template, key), sprite);
    }

    @Nullable
    private static ResourceLocation readMaterialTexture(IResourceManager manager, MaterialType material)
    {
        ResourceLocation texture = readMaterialTexture(manager, material, new ResourceLocation("tconstruct", "materials/" + material.getName() + ".json"));
        return texture == null ? readMaterialTexture(manager, material, new ResourceLocation(MOD_ID, "materials/" + material.getName() + ".json")) : texture;
    }

    @Nullable
    private static ResourceLocation readMaterialTexture(IResourceManager manager, MaterialType material, ResourceLocation infoLocation)
    {
        try
        {
            IResource resource = manager.getResource(infoLocation);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
            {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                if (!json.has("type") || !"block".equals(json.get("type").getAsString()))
                {
                    return null;
                }
                JsonElement parameters = json.get("parameters");
                if (parameters == null || !parameters.isJsonObject() || !parameters.getAsJsonObject().has("texture"))
                {
                    return null;
                }
                return new ResourceLocation(parameters.getAsJsonObject().get("texture").getAsString());
            }
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        catch (IOException | RuntimeException e)
        {
            TinkersForging.getLog().warn("Failed to load material render info for {}", material.getName(), e);
            return null;
        }
    }

    private static String getKey(MaterialType material, ResourceLocation template, String key)
    {
        return key + "|" + material.getName() + "|" + template.toString();
    }

    private MaterialRenderRegistry() {}

    private static class MaterialTextureSprite extends TextureAtlasSprite
    {
        private final ResourceLocation template;
        private final ResourceLocation materialTexture;

        MaterialTextureSprite(String spriteName, ResourceLocation template, ResourceLocation materialTexture)
        {
            super(spriteName);
            this.template = template;
            this.materialTexture = materialTexture;
        }

        @Override
        public Collection<ResourceLocation> getDependencies()
        {
            return Arrays.asList(template, materialTexture);
        }

        @Override
        public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location)
        {
            return true;
        }

        @Override
        public boolean load(IResourceManager manager, ResourceLocation location, java.util.function.Function<ResourceLocation, TextureAtlasSprite> textureGetter)
        {
            TextureAtlasSprite templateSprite = textureGetter.apply(template);
            TextureAtlasSprite materialSprite = textureGetter.apply(materialTexture);
            if (templateSprite == null || templateSprite.getFrameCount() <= 0 || materialSprite == null || materialSprite.getFrameCount() <= 0)
            {
                return false;
            }

            copyFrom(templateSprite);
            int[][] original = templateSprite.getFrameTextureData(0);
            int[] pixels = Arrays.copyOf(original[0], original[0].length);
            int[] materialPixels = materialSprite.getFrameTextureData(0)[0];
            int materialWidth = materialSprite.getIconWidth();
            int materialHeight = materialSprite.getIconHeight();

            for (int i = 0; i < pixels.length; i++)
            {
                int pixel = pixels[i];
                int alpha = alpha(pixel);
                if (alpha == 0)
                {
                    continue;
                }

                int x = i % width;
                int y = i / width;
                int materialPixel = materialPixels[(y % materialHeight) * materialWidth + (x % materialWidth)];

                int r = multiply(multiply(red(materialPixel), red(pixel)), red(pixel));
                int g = multiply(multiply(green(materialPixel), green(pixel)), green(pixel));
                int b = multiply(multiply(blue(materialPixel), blue(pixel)), blue(pixel));

                pixels[i] = compose(r, g, b, alpha);
            }

            framesTextureData = new java.util.ArrayList<>();
            framesTextureData.add(new int[][] {pixels});
            return false;
        }

        private static int alpha(int color)
        {
            return color >>> 24 & 255;
        }

        private static int red(int color)
        {
            return color >> 16 & 255;
        }

        private static int green(int color)
        {
            return color >> 8 & 255;
        }

        private static int blue(int color)
        {
            return color & 255;
        }

        private static int multiply(int first, int second)
        {
            return (int) (first * (second / 255f));
        }

        private static int compose(int r, int g, int b, int a)
        {
            return a << 24 | r << 16 | g << 8 | b;
        }
    }
}
