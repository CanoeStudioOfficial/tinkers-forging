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

import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;

import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.SLOT_CATALYST;
import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.SLOT_HAMMER;
import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.SLOT_INPUT_MAIN;
import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.SLOT_INPUT_SECOND;

@SideOnly(Side.CLIENT)
public class TESRTinkersAnvil extends TileEntitySpecialRenderer<TileTinkersAnvil>
{
    @Override
    public void render(TileTinkersAnvil tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

        IItemHandler cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap != null)
        {
            int rotation = tile.getBlockMetadata();

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.003125D + 0.6875D, z + 0.5);
            GlStateManager.scale(0.3f, 0.3f, 0.3f);
            GlStateManager.rotate(90f, 1f, 0f, 0f);
            GlStateManager.rotate(90f * (float) rotation + 270f, 0f, 0f, 1f);
            GlStateManager.translate(-0.4f, 0, 0);

            renderStack(cap.getStackInSlot(SLOT_HAMMER));

            GlStateManager.translate(1.15f, 0, 0);
            renderStack(cap.getStackInSlot(SLOT_INPUT_MAIN));

            GlStateManager.translate(0.4f, 0, -0.05f);
            renderStack(cap.getStackInSlot(SLOT_INPUT_SECOND));

            ItemStack catalyst = cap.getStackInSlot(SLOT_CATALYST);
            if (!catalyst.isEmpty())
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(-0.9f, -0.25f, 0.05f);
                GlStateManager.scale(0.6f, 0.6f, 0.6f);
                renderStack(catalyst);
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();

        }
    }

    private void renderStack(ItemStack stack)
    {
        if (!stack.isEmpty())
        {
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileTinkersAnvil te)
    {
        return false;
    }
}
