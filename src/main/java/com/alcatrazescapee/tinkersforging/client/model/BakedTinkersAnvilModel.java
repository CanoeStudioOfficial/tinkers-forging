/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.model;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

@SideOnly(Side.CLIENT)
public final class BakedTinkersAnvilModel implements IBakedModel
{
    private final IBakedModel base;
    private final Map<String, IBakedModel> materialModels;
    private final ItemOverrideList overrides;

    BakedTinkersAnvilModel(IBakedModel base, Map<String, IBakedModel> materialModels)
    {
        this.base = base;
        this.materialModels = materialModels;
        this.overrides = new AnvilOverrideList();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        IBakedModel model = getModel(state);
        return model.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return base.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return base.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return base.getItemCameraTransforms();
    }

    @Nonnull
    @Override
    public ItemOverrideList getOverrides()
    {
        return overrides;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType)
    {
        return base.handlePerspective(cameraTransformType);
    }

    private IBakedModel getModel(@Nullable IBlockState state)
    {
        if (state != null && state.getBlock() instanceof BlockTinkersAnvil)
        {
            MaterialType material = ((BlockTinkersAnvil) state.getBlock()).getMaterial();
            IBakedModel model = materialModels.get(material.getName());
            if (model != null)
            {
                return model;
            }
        }
        return base;
    }

    private final class AnvilOverrideList extends ItemOverrideList
    {
        private AnvilOverrideList()
        {
            super(ImmutableList.of());
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
        {
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockTinkersAnvil)
            {
                MaterialType material = ((BlockTinkersAnvil) ((ItemBlock) stack.getItem()).getBlock()).getMaterial();
                IBakedModel model = materialModels.get(material.getName());
                if (model != null)
                {
                    return model;
                }
            }
            return base.getOverrides().handleItemState(base, stack, world, entity);
        }
    }
}
