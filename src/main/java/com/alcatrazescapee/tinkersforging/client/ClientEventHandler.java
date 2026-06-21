/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.AlcatrazCore;
import com.alcatrazescapee.alcatrazcore.util.RegistryHelper;
import com.alcatrazescapee.tinkersforging.client.model.material.ForgingMaterialTextureManager;
import com.alcatrazescapee.tinkersforging.client.render.TESRCharcoalForge;
import com.alcatrazescapee.tinkersforging.client.render.TESRTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedToolHead;
import com.alcatrazescapee.tinkersforging.common.items.ItemMetalForm;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.TickTimer;
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
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START && !Minecraft.getMinecraft().isGamePaused() && Minecraft.getMinecraft().player != null)
        {
            TickTimer.update(AlcatrazCore.getProxy().getClientWorld().getTotalWorldTime());
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerModels(ModelRegistryEvent event)
    {
        RegistryHelper.get(MOD_ID).initModels(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileTinkersAnvil.class, new TESRTinkersAnvil());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCharcoalForge.class, new TESRCharcoalForge());
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerColorHandlerItems(ColorHandlerEvent.Item event)
    {
        ItemColors itemColors = event.getItemColors();
        BlockColors blockColors = event.getBlockColors();

        // Tool Heads
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (ForgingMaterialTextureManager.hasCustomTexture(stack))
            {
                return 0xffffff;
            }
            if (stack.getItem() instanceof ItemMetalForm)
            {
                MaterialType material = ((ItemMetalForm) stack.getItem()).getMaterial();
                return ForgingMaterialTextureManager.getColor(stack, material.getColor());
            }
            return 0xffffff;
        }, ItemMetalForm.getAll().toArray(new ItemMetalForm[0]));

        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (ForgingMaterialTextureManager.hasCustomTexture(stack))
            {
                return 0xffffff;
            }
            if (stack.getItem() instanceof ItemToolHead)
            {
                MaterialType material = ((ItemToolHead) stack.getItem()).getMaterial();
                return ForgingMaterialTextureManager.getColor(stack, material.getColor());
            }
            return 0xffffff;
        }, ItemToolHead.getAll().toArray(new ItemToolHead[0]));

        itemColors.registerItemColorHandler((stack, tintIndex) -> 0xffffff, ItemExtendedToolHead.getAll().toArray(new ItemExtendedToolHead[0]));
        ItemExtendedHammer extendedHammer = ItemExtendedHammer.getItem();
        if (extendedHammer != null)
        {
            itemColors.registerItemColorHandler((stack, tintIndex) -> 0xffffff, extendedHammer);
        }

        // Hammers
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 1 && ForgingMaterialTextureManager.hasCustomTexture(stack))
            {
                return 0xffffff;
            }
            if (stack.getItem() instanceof ItemHammer && tintIndex == 1)
            {
                MaterialType material = ((ItemHammer) stack.getItem()).getMaterial();
                return material != null ? ForgingMaterialTextureManager.getColor(stack, material.getColor()) : 0xffffff;
            }
            return 0xffffff;
        }, ItemHammer.getAll().toArray(new ItemHammer[0]));

        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockTinkersAnvil)
            {
                BlockTinkersAnvil block = (BlockTinkersAnvil) ((ItemBlock) stack.getItem()).getBlock();
                if (ForgingMaterialTextureManager.hasAnvilCustomTexture(block.getMaterial()))
                {
                    return 0xffffff;
                }
                return block.getMaterial().getColor();
            }
            return 0xffffff;
        }, BlockTinkersAnvil.getAll().toArray(new BlockTinkersAnvil[0]));

        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            if (state.getBlock() instanceof BlockTinkersAnvil)
            {
                BlockTinkersAnvil block = (BlockTinkersAnvil) state.getBlock();
                if (ForgingMaterialTextureManager.hasAnvilCustomTexture(block.getMaterial()))
                {
                    return 0xffffff;
                }
                return block.getMaterial().getColor();
            }
            return 0xffffff;
        }, BlockTinkersAnvil.getAll().toArray(new BlockTinkersAnvil[0]));
    }
}
