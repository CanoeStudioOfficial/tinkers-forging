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

import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
class GuiButtonAnvilStep extends GuiButton
{
    private final ForgeStep step;

    GuiButtonAnvilStep(ForgeStep step, int guiLeft, int guiTop)
    {
        super(step.ordinal(), guiLeft + step.getX(), guiTop + step.getY(), 16, 16, "");
        this.step = step;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(GuiTinkersAnvil.BACKGROUND);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            drawModalRectWithCustomSizedTexture(x, y, step.getTexU(), step.getTexV(), width, height, 256, 256);
            mouseDragged(mc, mouseX, mouseY);
        }
    }

    String getTooltip()
    {
        return MOD_ID + ".tooltip." + step.name().toLowerCase();
    }
}
