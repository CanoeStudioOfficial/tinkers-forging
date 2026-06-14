package com.alcatrazescapee.tinkersforging.client.material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

public class BakedMaterialOverrideModel implements IBakedModel
{
    private final IBakedModel parent;
    private final MaterialType material;
    private final ResourceLocation template;
    private final String key;
    private final ItemOverrideList overrides;

    public BakedMaterialOverrideModel(IBakedModel parent, @Nullable MaterialType material, ResourceLocation template, String key)
    {
        this.parent = parent;
        this.material = material;
        this.template = template;
        this.key = key;
        this.overrides = new Overrides(this);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
    {
        MaterialType renderMaterial = material;
        if (renderMaterial == null && state != null && state.getBlock() instanceof BlockTinkersAnvil)
        {
            renderMaterial = ((BlockTinkersAnvil) state.getBlock()).getMaterial();
        }
        if (renderMaterial == null)
        {
            return parent.getQuads(state, side, rand);
        }

        TextureAtlasSprite materialSprite = MaterialRenderRegistry.getGeneratedTexture(renderMaterial, template, key);
        if (materialSprite == null)
        {
            return parent.getQuads(state, side, rand);
        }

        List<BakedQuad> quads = parent.getQuads(state, side, rand);
        if (quads.isEmpty())
        {
            return quads;
        }

        String templateName = template.toString();
        List<BakedQuad> retouched = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads)
        {
            if (quad.getSprite().getIconName().equals(templateName))
            {
                retouched.add(new BakedQuadRetextured(quad, materialSprite));
            }
            else
            {
                retouched.add(quad);
            }
        }
        return retouched;
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return parent.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        if (material == null)
        {
            return parent.getParticleTexture();
        }
        TextureAtlasSprite materialSprite = MaterialRenderRegistry.getGeneratedTexture(material, template, key);
        return materialSprite == null ? parent.getParticleTexture() : materialSprite;
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return parent.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return overrides;
    }

    @Override
    public boolean isAmbientOcclusion(IBlockState state)
    {
        return parent.isAmbientOcclusion(state);
    }

    @Override
    public Pair<? extends IBakedModel, javax.vecmath.Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType)
    {
        Pair<? extends IBakedModel, javax.vecmath.Matrix4f> pair = parent.handlePerspective(cameraTransformType);
        return Pair.of(this, pair.getRight());
    }

    public IBakedModel getParent()
    {
        return parent;
    }

    @Nullable
    public static BakedMaterialOverrideModel forStack(IBakedModel parent, ItemStack stack)
    {
        if (stack.getItem() instanceof ItemToolHead)
        {
            ItemToolHead item = (ItemToolHead) stack.getItem();
            ItemType type = item.getType();
            ResourceLocation template = MaterialRenderRegistry.getTemplate(type);
            String key = "item_" + type.name().toLowerCase();
            // Only wrap if a texture was actually generated for THIS item+material combination,
            // otherwise the item would render as a blank texture. Falling through (return null) lets
            // it keep its normal model + vertex coloring.
            if (MaterialRenderRegistry.hasGeneratedTexture(item.getMaterial(), template, key))
            {
                return new BakedMaterialOverrideModel(parent, item.getMaterial(), template, key);
            }
        }
        if (stack.getItem() instanceof ItemHammer)
        {
            MaterialType material = ((ItemHammer) stack.getItem()).getMaterial();
            ResourceLocation template = MaterialRenderRegistry.getHammerMetalTemplate();
            String key = "item_hammer";
            if (material != null && MaterialRenderRegistry.hasGeneratedTexture(material, template, key))
            {
                return new BakedMaterialOverrideModel(parent, material, template, key);
            }
        }
        if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockTinkersAnvil)
        {
            BlockTinkersAnvil block = (BlockTinkersAnvil) ((ItemBlock) stack.getItem()).getBlock();
            ResourceLocation template = MaterialRenderRegistry.getAnvilTemplate();
            String key = "block_tinkers_anvil";
            if (MaterialRenderRegistry.hasGeneratedTexture(block.getMaterial(), template, key))
            {
                return new BakedMaterialOverrideModel(parent, block.getMaterial(), template, key);
            }
        }
        return null;
    }

    public static BakedMaterialOverrideModel dispatch(IBakedModel parent, ResourceLocation template, String key)
    {
        return new BakedMaterialOverrideModel(parent, null, template, key);
    }

    private static class Overrides extends ItemOverrideList
    {
        private final BakedMaterialOverrideModel model;

        Overrides(BakedMaterialOverrideModel model)
        {
            super(Collections.emptyList());
            this.model = model;
        }

        @Nonnull
        @Override
        public IBakedModel handleItemState(@Nonnull IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity)
        {
            IBakedModel parentModel = model.getParent().getOverrides().handleItemState(model.getParent(), stack, world, entity);
            BakedMaterialOverrideModel materialModel = BakedMaterialOverrideModel.forStack(parentModel, stack);
            return materialModel == null ? parentModel : materialModel;
        }
    }
}
