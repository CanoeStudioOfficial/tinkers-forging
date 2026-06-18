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

import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
class GuiButtonAnvilWeld extends GuiButton
{
    private final TileTinkersAnvil tile;

    GuiButtonAnvilWeld(TileTinkersAnvil tile, int guiLeft, int guiTop)
    {
        super(ContainerTinkersAnvil.ACTION_WELD, guiLeft + 21, guiTop + 56, 18, 18, "");
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
            drawModalRectWithCustomSizedTexture(x, y, 218, tile.canWeldNow(mc.player) ? 0 : 18, width, height, 256, 256);
            drawModalRectWithCustomSizedTexture(x + 1, y + 1, 236, 16, 16, 16, 256, 256);
            mouseDragged(mc, mouseX, mouseY);
        }
    }

    String getTooltip()
    {
        return MOD_ID + ".tooltip.anvil_weld";
    }
}
