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
    private static final float ITEM_SCALE = 0.3f;

    @Override
    public void render(TileTinkersAnvil tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

        IItemHandler cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap != null)
        {
            int rotation = tile.getBlockMetadata();
            float yOffset = tile.getTier() == 0 ? 0.875f : 0.6875f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 0.003125D + yOffset, z + 0.5);
            GlStateManager.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            GlStateManager.rotate(90f, 1f, 0f, 0f);
            GlStateManager.rotate(90f * (float) rotation + 180f, 0f, 0f, 1f);

            renderStack(cap.getStackInSlot(SLOT_HAMMER), -0.4f, 0f, 0f, 1f);
            renderStack(cap.getStackInSlot(SLOT_INPUT_MAIN), 0.75f, 0f, 0f, 1f);
            renderStack(cap.getStackInSlot(SLOT_INPUT_SECOND), 1.15f, 0f, -0.05f, 1f);

            renderStack(cap.getStackInSlot(SLOT_CATALYST), 0.25f, -0.25f, 0f, 0.6f);

            GlStateManager.popMatrix();

        }
    }

    private void renderStack(ItemStack stack, float x, float y, float z, float scale)
    {
        if (!stack.isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.scale(scale, scale, scale);
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileTinkersAnvil te)
    {
        return false;
    }
}
