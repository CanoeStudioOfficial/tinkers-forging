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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class GuiButtonAnvilPage extends GuiButton
{
    private final int delta;

    GuiButtonAnvilPage(int id, int x, int y, int delta)
    {
        super(id, x, y, 9, 13, "");
        this.delta = delta;
    }

    int getDelta()
    {
        return delta;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            GlStateManager.color(1, 1, 1, 1);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(GuiTinkersAnvilPlan.BACKGROUND);
            drawModalRectWithCustomSizedTexture(x, y, delta < 0 ? 176 : 185, 0, width, height, 256, 256);
            mouseDragged(mc, mouseX, mouseY);
        }
    }
}
