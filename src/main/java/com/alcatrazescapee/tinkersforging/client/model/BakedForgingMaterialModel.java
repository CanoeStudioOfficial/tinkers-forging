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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

@SideOnly(Side.CLIENT)
public final class BakedForgingMaterialModel implements IBakedModel
{
    private final IBakedModel base;
    private final Map<String, IBakedModel> materialModels;
    private final ItemOverrideList overrides;

    BakedForgingMaterialModel(IBakedModel base, Map<String, IBakedModel> materialModels)
    {
        this.base = base;
        this.materialModels = materialModels;
        this.overrides = new MaterialOverrideList();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        return base.getQuads(state, side, rand);
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

    private final class MaterialOverrideList extends ItemOverrideList
    {
        private MaterialOverrideList()
        {
            super(ImmutableList.of());
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity)
        {
            String material = getMaterial(stack);
            IBakedModel model = material == null ? null : materialModels.get(material);
            return model != null ? model : base.getOverrides().handleItemState(base, stack, world, entity);
        }

        @Nullable
        private String getMaterial(ItemStack stack)
        {
            if (stack.getItem() instanceof ItemToolHead)
            {
                return ((ItemToolHead) stack.getItem()).getMaterial().getName();
            }
            if (stack.getItem() instanceof ItemHammer)
            {
                MaterialType material = ((ItemHammer) stack.getItem()).getMaterial();
                return material == null ? null : material.getName();
            }
            return null;
        }
    }
}
