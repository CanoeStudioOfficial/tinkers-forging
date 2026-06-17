/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.client.model.material.ForgingMaterialTextureManager;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
public final class TinkersAnvilModel implements IModel
{
    private static final ResourceLocation ANVIL_TEXTURE = new ResourceLocation(MOD_ID, "blocks/metal_block");

    private final IModel baseModel;

    TinkersAnvilModel(IModel baseModel)
    {
        this.baseModel = baseModel;
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return baseModel.getDependencies();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        List<ResourceLocation> textures = new ArrayList<>(baseModel.getTextures());
        textures.add(ANVIL_TEXTURE);
        return textures;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        IBakedModel base = baseModel.bake(state, format, bakedTextureGetter);
        Map<String, IBakedModel> materialModels = new HashMap<>();

        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            TextureAtlasSprite sprite = ForgingMaterialTextureManager.getAnvilSprite(material);
            if (sprite != null)
            {
                Map<String, String> textures = new HashMap<>();
                textures.put("all", sprite.getIconName());
                textures.put("particle", sprite.getIconName());
                IModel retextured = baseModel.retexture(ImmutableMap.copyOf(textures));
                materialModels.put(material.getName(), retextured.bake(state, format, bakedTextureGetter));
            }
        }

        return new BakedTinkersAnvilModel(base, materialModels);
    }

    @Override
    public IModelState getDefaultState()
    {
        return baseModel.getDefaultState();
    }
}
