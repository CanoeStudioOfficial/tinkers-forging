/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge;

import static com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge.SLOT_EXTRA_MAX;
import static com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge.SLOT_INPUT_MIN;

@SideOnly(Side.CLIENT)
public class TESRCharcoalForge extends TileEntitySpecialRenderer<TileCharcoalForge>
{
    private static final float[][] POSITIONS = new float[][] {
            {0.25f, 0.25f},
            {0.25f, 0.75f},
            {0.75f, 0.25f},
            {0.75f, 0.75f},
            {0.5f, 0.5f},
            {0.8f, 0.9f},
            {0.6f, 0.9f},
            {0.4f, 0.9f},
            {0.2f, 0.9f}
    };

    @Override
    public void render(TileCharcoalForge tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

        IItemHandler cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap == null)
            return;

        for (int slot = SLOT_INPUT_MIN; slot <= SLOT_EXTRA_MAX; slot++)
        {
            ItemStack stack = cap.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;

            float[] pos = POSITIONS[slot - SLOT_INPUT_MIN];
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + pos[0], y + 15f / 16f, z + pos[1]);
            GlStateManager.scale(0.33f, 0.33f, 0.33f);
            GlStateManager.rotate((tile.getWorld().getTotalWorldTime() + partialTicks) % 360f, 0f, 1f, 0f);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileCharcoalForge te)
    {
        return false;
    }
}
