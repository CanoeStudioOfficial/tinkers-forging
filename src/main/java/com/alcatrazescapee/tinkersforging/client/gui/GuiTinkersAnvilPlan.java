/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.gui;

import java.io.IOException;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.client.gui.GuiContainerTileCore;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.network.PacketAnvilButton;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class GuiTinkersAnvilPlan extends GuiContainerTileCore<TileTinkersAnvil>
{
    static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/gui/anvil_plan.png");
    private static final int RECIPES_PER_PAGE = 18;

    private final List<AnvilRecipe> recipes;
    private int page;

    public GuiTinkersAnvilPlan(TileTinkersAnvil tile, String translationKey, Container container, InventoryPlayer playerInv)
    {
        super(tile, container, playerInv, BACKGROUND, translationKey);
        this.recipes = ModRecipes.ANVIL.getAllMatching(tile.getInputStack());
        this.page = 0;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        refreshButtons();
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        for (GuiButton button : buttonList)
        {
            if (button instanceof PlanButton && button.isMouseOver())
            {
                ItemStack output = ((PlanButton) button).getRecipe().getOutput();
                renderToolTip(output, mouseX, mouseY);
                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        String text = I18n.format(MOD_ID + ".tooltip.anvil_plan");
        fontRenderer.drawString(text, guiLeft + 8, guiTop + 6, 0x404040);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button instanceof PageButton)
        {
            page += ((PageButton) button).delta;
            refreshButtons();
            return;
        }
        if (button instanceof PlanButton)
        {
            TinkersForging.getNetwork().sendToServer(new PacketAnvilButton(ContainerTinkersAnvil.ACTION_PLAN_SELECT_BASE + ((PlanButton) button).recipeIndex));
            return;
        }
        super.actionPerformed(button);
    }

    private void refreshButtons()
    {
        buttonList.clear();
        int maxPage = Math.max(0, (recipes.size() - 1) / RECIPES_PER_PAGE);
        if (page < 0) page = 0;
        if (page > maxPage) page = maxPage;

        int start = page * RECIPES_PER_PAGE;
        for (int i = 0; i < RECIPES_PER_PAGE && start + i < recipes.size(); i++)
        {
            int x = guiLeft + 7 + (i % 9) * 18;
            int y = guiTop + 17 + (i / 9) * 18;
            buttonList.add(new PlanButton(ContainerTinkersAnvil.ACTION_PLAN_SELECT_BASE + start + i, x, y, start + i, recipes.get(start + i)));
        }
        if (page > 0)
        {
            buttonList.add(new PageButton(-1, guiLeft + 7, guiTop + 56, -1));
        }
        if (page < maxPage)
        {
            buttonList.add(new PageButton(-2, guiLeft + 160, guiTop + 56, 1));
        }
    }

    private class PlanButton extends GuiButton
    {
        private final int recipeIndex;
        private final AnvilRecipe recipe;

        private PlanButton(int id, int x, int y, int recipeIndex, AnvilRecipe recipe)
        {
            super(id, x, y, 18, 18, "");
            this.recipeIndex = recipeIndex;
            this.recipe = recipe;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (!visible)
                return;

            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(BACKGROUND);
            drawModalRectWithCustomSizedTexture(x, y, 176, 0, width, height, 256, 256);
            ItemStack output = recipe.getOutput();
            itemRender.renderItemAndEffectIntoGUI(output, x + 1, y + 1);
            itemRender.renderItemOverlayIntoGUI(fontRenderer, output, x + 1, y + 1, null);
        }

        private AnvilRecipe getRecipe()
        {
            return recipe;
        }
    }

    private class PageButton extends GuiButton
    {
        private final int delta;

        private PageButton(int id, int x, int y, int delta)
        {
            super(id, x, y, 9, 13, "");
            this.delta = delta;
        }

        @Override
        public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if (!visible)
                return;

            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(BACKGROUND);
            drawModalRectWithCustomSizedTexture(x, y, delta < 0 ? 201 : 212, 3, width, height, 256, 256);
        }
    }
}
