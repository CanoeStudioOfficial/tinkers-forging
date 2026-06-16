/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
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

@SideOnly(Side.CLIENT)
public final class ForgingMaterialModel implements IModel
{
    private final IModel baseModel;
    private final ImmutableList<ResourceLocation> textures;
    private final int materialLayer;

    ForgingMaterialModel(IModel baseModel, ImmutableList<ResourceLocation> textures, int materialLayer)
    {
        this.baseModel = baseModel;
        this.textures = textures;
        this.materialLayer = materialLayer;
    }

    @Override
    public Collection<ResourceLocation> getDependencies()
    {
        return baseModel.getDependencies();
    }

    @Override
    public Collection<ResourceLocation> getTextures()
    {
        return textures;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
    {
        IBakedModel base = baseModel.bake(state, format, bakedTextureGetter);
        ResourceLocation baseTexture = textures.get(materialLayer);
        Map<String, IBakedModel> materialModels = new HashMap<>();

        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            TextureAtlasSprite sprite = ForgingMaterialTextureManager.getSprite(baseTexture, material);
            if (sprite != null)
            {
                IModel retextured = baseModel.retexture(ImmutableMap.of("layer" + materialLayer, sprite.getIconName()));
                materialModels.put(material.getName(), retextured.bake(state, format, bakedTextureGetter));
            }
        }

        return new BakedForgingMaterialModel(base, materialModels);
    }

    @Override
    public IModelState getDefaultState()
    {
        return baseModel.getDefaultState();
    }
}
