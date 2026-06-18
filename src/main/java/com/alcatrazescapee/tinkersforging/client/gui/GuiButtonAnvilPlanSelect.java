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

import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;

@SideOnly(Side.CLIENT)
class GuiButtonAnvilPlanSelect extends GuiButton
{
    private final int recipeIndex;
    private final int page;
    private final AnvilRecipe recipe;

    GuiButtonAnvilPlanSelect(int id, int x, int y, int recipeIndex, int page, AnvilRecipe recipe)
    {
        super(id, x, y, 18, 18, "");
        this.recipeIndex = recipeIndex;
        this.page = page;
        this.recipe = recipe;
    }

    void setCurrentPage(int currentPage)
    {
        visible = enabled = currentPage == page;
    }

    int getRecipeIndex()
    {
        return recipeIndex;
    }

    AnvilRecipe getRecipe()
    {
        return recipe;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (visible)
        {
            GlStateManager.color(1, 1, 1, 1);
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(GuiTinkersAnvilPlan.BACKGROUND);
            drawModalRectWithCustomSizedTexture(x, y, 176, 0, width, height, 256, 256);

            ItemStack output = recipe.getOutput();
            mc.getRenderItem().renderItemAndEffectIntoGUI(output, x + 1, y + 1);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, output, x + 1, y + 1, null);
            mouseDragged(mc, mouseX, mouseY);
        }
    }
}
