/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
class GuiButtonAnvilPlan extends GuiButton
{
    private final TileTinkersAnvil tile;

    GuiButtonAnvilPlan(TileTinkersAnvil tile, int guiLeft, int guiTop)
    {
        super(ContainerTinkersAnvil.ACTION_PLAN, guiLeft + 137, guiTop + 56, 18, 18, "");
        this.tile = tile;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(GuiTinkersAnvil.BACKGROUND);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

            ItemStack output = tile.getSelectedPlanOutput();
            boolean hasPlan = !output.isEmpty() || tile.hasSelectablePlan();
            drawModalRectWithCustomSizedTexture(x, y, 218, hasPlan ? 0 : 18, width, height, 256, 256);

            if (output.isEmpty())
            {
                drawModalRectWithCustomSizedTexture(x + 1, y + 1, 236, 0, 16, 16, 256, 256);
            }
            else
            {
                mc.getRenderItem().renderItemAndEffectIntoGUI(output, x + 1, y + 1);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, output, x + 1, y + 1, null);
            }
            mouseDragged(mc, mouseX, mouseY);
        }
    }

    String getTooltip()
    {
        return MOD_ID + ".tooltip.anvil_plan";
    }
}
