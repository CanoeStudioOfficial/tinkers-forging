/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.util.RegistryHelper;
import com.alcatrazescapee.tinkersforging.client.material.BakedMaterialOverrideModel;
import com.alcatrazescapee.tinkersforging.client.material.MaterialRenderRegistry;
import com.alcatrazescapee.tinkersforging.client.render.TESRTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;
import static net.minecraft.util.text.TextFormatting.GREEN;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
@Mod.EventBusSubscriber(Side.CLIENT)
public final class ClientEventHandler
{
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onTextureStitchPre(TextureStitchEvent.Pre event)
    {
        MaterialRenderRegistry.onTextureStitch(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onItemTooltipEvent(ItemTooltipEvent event)
    {
        IForgeItem cap = event.getItemStack().getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap != null)
        {
            if (cap.getWork() != IForgeItem.DEFAULT_WORK || cap.getRecipeName() != null || cap.getTemperature() >= 1f)
            {
                event.getToolTip().add(GREEN + I18n.format(MOD_ID + ".tooltip.has_been_worked"));
                cap.addTooltipInfo(event.getToolTip());
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event)
    {
        RegistryHelper.get(MOD_ID).initModels(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileTinkersAnvil.class, new TESRTinkersAnvil());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelBakeEvent(ModelBakeEvent event)
    {
        for (ItemType type : ItemType.values())
        {
            if (ItemType.isBuiltInToolPart(type))
            {
                replaceModel(event, new ModelResourceLocation(MOD_ID + ":" + type.name().toLowerCase(), "inventory"), MaterialRenderRegistry.getTemplate(type), "item_" + type.name().toLowerCase());
            }
        }

        replaceModel(event, new ModelResourceLocation(MOD_ID + ":hammer", "inventory"), MaterialRenderRegistry.getHammerMetalTemplate(), "item_hammer");

        IBakedModel inventoryAnvil = event.getModelRegistry().getObject(new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "inventory"));
        if (inventoryAnvil != null)
        {
            event.getModelRegistry().putObject(new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "inventory"), BakedMaterialOverrideModel.dispatch(inventoryAnvil, MaterialRenderRegistry.getAnvilTemplate(), "block_tinkers_anvil"));
        }
        replaceModel(event, new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "facing=north"), MaterialRenderRegistry.getAnvilTemplate(), "block_tinkers_anvil");
        replaceModel(event, new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "facing=east"), MaterialRenderRegistry.getAnvilTemplate(), "block_tinkers_anvil");
        replaceModel(event, new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "facing=south"), MaterialRenderRegistry.getAnvilTemplate(), "block_tinkers_anvil");
        replaceModel(event, new ModelResourceLocation(MOD_ID + ":tinkers_anvil", "facing=west"), MaterialRenderRegistry.getAnvilTemplate(), "block_tinkers_anvil");
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerColorHandlerItems(ColorHandlerEvent.Item event)
    {
        ItemColors itemColors = event.getItemColors();
        BlockColors blockColors = event.getBlockColors();

        // Tool Heads
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (stack.getItem() instanceof ItemToolHead)
            {
                MaterialType material = ((ItemToolHead) stack.getItem()).getMaterial();
                return MaterialRenderRegistry.hasMaterialTexture(material) ? 0xffffff : material.getColor();
            }
            return 0xffffff;
        }, ItemToolHead.getAll().toArray(new ItemToolHead[0]));

        // Hammers
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (stack.getItem() instanceof ItemHammer && tintIndex == 1)
            {
                MaterialType material = ((ItemHammer) stack.getItem()).getMaterial();
                return material != null && !MaterialRenderRegistry.hasMaterialTexture(material) ? material.getColor() : 0xffffff;
            }
            return 0xffffff;
        }, ItemHammer.getAll().toArray(new ItemHammer[0]));

        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockTinkersAnvil)
            {
                BlockTinkersAnvil block = (BlockTinkersAnvil) ((ItemBlock) stack.getItem()).getBlock();
                return MaterialRenderRegistry.hasMaterialTexture(block.getMaterial()) ? 0xffffff : block.getMaterial().getColor();
            }
            return 0xffffff;
        }, BlockTinkersAnvil.getAll().toArray(new BlockTinkersAnvil[0]));

        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (state.getBlock() instanceof BlockTinkersAnvil)
            {
                MaterialType material = ((BlockTinkersAnvil) state.getBlock()).getMaterial();
                return MaterialRenderRegistry.hasMaterialTexture(material) ? 0xffffff : material.getColor();
            }
            return 0xffffff;
        }, BlockTinkersAnvil.getAll().toArray(new BlockTinkersAnvil[0]));
    }

    private static void replaceModel(ModelBakeEvent event, ModelResourceLocation location, ResourceLocation template, String key)
    {
        IBakedModel model = event.getModelRegistry().getObject(location);
        if (model != null)
        {
            event.getModelRegistry().putObject(location, BakedMaterialOverrideModel.dispatch(model, template, key));
        }
    }
}
