package com.alcatrazescapee.tinkersforging.client.material;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.client.material.MaterialRenderInfoLoader;
import slimeknights.tconstruct.library.client.texture.TinkerTexture;
import slimeknights.tconstruct.library.materials.Material;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

/**
 * Renders tool parts / hammers / anvils by delegating to Tinkers Construct's native
 * {@link MaterialRenderInfo} pipeline. This means the textures are generated using the exact
 * same logic (metal, metal_textured, multicolor, inverse_multicolor, block, colored, ...)
 * that Tinkers uses for its own tools, reading from {@code tconstruct:materials/<name>.json}
 * (or any other mod-provided {@code materials/<name>.json}).
 *
 * For non-Tinkers materials (the built-in iron/gold/copper/...) there is no render info,
 * so {@link #hasMaterialTexture(MaterialType)} returns false and the items fall back to
 * vertex coloring via the ItemColorHandler in {@code ClientEventHandler}.
 */
public final class MaterialRenderRegistry
{
    private static final Map<String, MaterialRenderInfo> MATERIAL_RENDER_INFO = new HashMap<>();
    private static final Map<String, TextureAtlasSprite> GENERATED_TEXTURES = new HashMap<>();
    /** Names of materials for which at least one stitched texture was generated. */
    private static final java.util.Set<String> GENERATED_MATERIALS = new HashSet<>();

    private static final ResourceLocation HAMMER_METAL_TEMPLATE = new ResourceLocation(MOD_ID, "items/hammer/metal");
    private static final ResourceLocation ANVIL_TEMPLATE = new ResourceLocation(MOD_ID, "blocks/metal_block");

    @SideOnly(Side.CLIENT)
    public static void onTextureStitch(TextureStitchEvent.Pre event)
    {
        MATERIAL_RENDER_INFO.clear();
        GENERATED_TEXTURES.clear();
        GENERATED_MATERIALS.clear();

        if (!Loader.isModLoaded("tconstruct"))
        {
            return;
        }

        // Make sure Tinkers has parsed all materials/<name>.json files into Material.renderInfo.
        // This is idempotent and mirrors what CustomTextureCreator.createCustomTextures does.
        try
        {
            MaterialRenderInfoLoader.INSTANCE.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
        }
        catch (Throwable e)
        {
            TinkersForging.getLog().warn("Failed to load Tinkers material render info", e);
        }

        // Cache the render info for every Tinkers-backed MaterialType we know about.
        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            if (!MaterialRegistry.isTinkersMaterial(material))
            {
                continue;
            }
            MaterialRenderInfo info = getTinkersRenderInfo(material);
            if (info != null)
            {
                MATERIAL_RENDER_INFO.put(material.getName(), info);
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

    /**
     * True for any material that Tinkers Construct knows how to render (i.e. it has a
     * {@link MaterialRenderInfo}). This covers BOTH stitched (metal/multicolor/block/...) and
     * vertex-colored (Default / "colored") materials. Use this to decide whether Tinkers owns the
     * rendering of a material at all.
     */
    public static boolean hasMaterialTexture(MaterialType material)
    {
        return material != null && MATERIAL_RENDER_INFO.containsKey(material.getName());
    }

    /**
     * True only if a per-material texture was actually generated and stitched for the given
     * template. Vertex-colored materials (type "colored") never produce a texture, so this returns
     * false for them and the caller must fall back to vertex coloring instead.
     */
    public static boolean hasGeneratedTexture(MaterialType material)
    {
        return material != null && GENERATED_MATERIALS.contains(material.getName());
    }

    /**
     * Returns the Tinkers vertex color for a material (used by {@code MaterialRenderInfo.Default}
     * / "colored" materials). For non-Tinkers materials returns the material's own color so callers
     * can use it as a unified color source.
     */
    public static int getMaterialColor(MaterialType material)
    {
        if (material == null)
        {
            return 0xffffff;
        }
        MaterialRenderInfo info = MATERIAL_RENDER_INFO.get(material.getName());
        if (info != null)
        {
            try
            {
                return info.getVertexColor();
            }
            catch (Throwable ignored) {}
        }
        return material.getColor();
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

    /**
     * Generates a single per-material texture by asking the Tinkers render info to produce it
     * for the given template (base) texture. This mirrors the logic in Tinkers'
     * {@code CustomTextureCreator.createTexture}, including the {@code suffix} (e.g. metal_base)
     * fallback that allows the material JSON to reference an alternate base texture.
     */
    @SideOnly(Side.CLIENT)
    private static void registerGeneratedTexture(TextureMap map, MaterialType material, ResourceLocation template, String key)
    {
        MaterialRenderInfo info = MATERIAL_RENDER_INFO.get(material.getName());
        if (info == null)
        {
            return;
        }

        // Vertex-colored materials (Default / "colored" type) don't generate a texture.
        if (!info.isStitched())
        {
            return;
        }

        ResourceLocation baseTexture = template;
        String location = template.toString() + "_" + material.getName();

        // If the render info declares a suffix (e.g. "metal_base"), look for an alternate
        // base texture "<template>_<suffix>" and use that as the base if it exists.
        String suffix = info.getTextureSuffix();
        if (suffix != null && !suffix.isEmpty())
        {
            String altLocation = template.toString() + "_" + suffix;
            TextureAtlasSprite altBase = map.getTextureExtry(altLocation);
            if (altBase == null && exists(altLocation))
            {
                altBase = TinkerTexture.loadManually(new ResourceLocation(altLocation));
                if (altBase != null)
                {
                    map.setTextureEntry(altBase);
                }
            }
            if (altBase != null)
            {
                baseTexture = new ResourceLocation(altBase.getIconName());
            }
        }

        TextureAtlasSprite sprite;
        try
        {
            sprite = info.getTexture(baseTexture, location);
        }
        catch (Throwable e)
        {
            TinkersForging.getLog().warn("Failed to generate material texture for {} on {}", material.getName(), template, e);
            return;
        }

        if (sprite != null)
        {
            map.setTextureEntry(sprite);
            GENERATED_TEXTURES.put(getKey(material, template, key), sprite);
            GENERATED_MATERIALS.add(material.getName());
        }
    }

    /**
     * Pulls the (already-loaded) {@link MaterialRenderInfo} off the matching Tinkers {@link Material}.
     * Returns null if the material isn't a Tinkers material or has no render info set.
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    private static MaterialRenderInfo getTinkersRenderInfo(MaterialType material)
    {
        try
        {
            Material tinkersMaterial = TinkerRegistry.getMaterial(material.getName());
            // TinkerRegistry.getMaterial returns Material.UNKNOWN when not found
            if (tinkersMaterial == null || tinkersMaterial == Material.UNKNOWN)
            {
                return null;
            }
            return tinkersMaterial.renderInfo;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    /** Mirrors CustomTextureCreator.exists: checks if a png exists on disk for the given sprite name. */
    private static boolean exists(String res)
    {
        try
        {
            ResourceLocation loc = new ResourceLocation(res);
            loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
            return Minecraft.getMinecraft().getResourceManager().getAllResources(loc) != null;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    private static String getKey(MaterialType material, ResourceLocation template, String key)
    {
        return key + "|" + material.getName() + "|" + template.toString();
    }

    private MaterialRenderRegistry() {}
}
