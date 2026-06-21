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
    private static final int PAGE_LEFT = -1;
    private static final int PAGE_RIGHT = -2;

    private final List<AnvilRecipe> recipes;
    private final java.util.List<GuiButtonAnvilPlanSelect> recipeButtons;
    private GuiButtonAnvilPage leftButton;
    private GuiButtonAnvilPage rightButton;
    private int maxPageInclusive;
    private int page;

    public GuiTinkersAnvilPlan(TileTinkersAnvil tile, String translationKey, Container container, InventoryPlayer playerInv)
    {
        super(tile, container, playerInv, BACKGROUND, translationKey);
        this.recipes = ModRecipes.ANVIL.getAllMatching(tile.getInputStack(), tile.getTier());
        this.recipeButtons = new java.util.ArrayList<>();
        this.maxPageInclusive = 0;
        this.page = 0;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        recipeButtons.clear();

        for (int i = 0; i < recipes.size(); i++)
        {
            int buttonPage = i / RECIPES_PER_PAGE;
            int index = i % RECIPES_PER_PAGE;
            int x = guiLeft + 7 + (index % 9) * 18;
            int y = guiTop + 17 + (index / 9) * 18;
            GuiButtonAnvilPlanSelect button = new GuiButtonAnvilPlanSelect(ContainerTinkersAnvil.ACTION_PLAN_SELECT_BASE + i, x, y, i, buttonPage, recipes.get(i));
            recipeButtons.add(button);
            buttonList.add(button);
        }

        maxPageInclusive = Math.max(0, (recipes.size() - 1) / RECIPES_PER_PAGE);
        buttonList.add(leftButton = new GuiButtonAnvilPage(PAGE_LEFT, guiLeft + 7, guiTop + 56, -1));
        buttonList.add(rightButton = new GuiButtonAnvilPage(PAGE_RIGHT, guiLeft + 160, guiTop + 56, 1));
        updateCurrentPage();
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        for (GuiButton button : buttonList)
        {
            if (button instanceof GuiButtonAnvilPlanSelect && button.isMouseOver())
            {
                ItemStack output = ((GuiButtonAnvilPlanSelect) button).getRecipe().getOutput();
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
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button instanceof GuiButtonAnvilPage)
        {
            page += ((GuiButtonAnvilPage) button).getDelta();
            updateCurrentPage();
            return;
        }
        if (button instanceof GuiButtonAnvilPlanSelect)
        {
            TinkersForging.getNetwork().sendToServer(new PacketAnvilButton(((GuiButtonAnvilPlanSelect) button).getRecipe().getName()));
            return;
        }
        super.actionPerformed(button);
    }

    private void updateCurrentPage()
    {
        if (page < 0) page = 0;
        if (page > maxPageInclusive) page = maxPageInclusive;

        for (GuiButtonAnvilPlanSelect button : recipeButtons)
        {
            button.setCurrentPage(page);
        }
        if (leftButton != null)
        {
            leftButton.visible = leftButton.enabled = page > 0;
        }
        if (rightButton != null)
        {
            rightButton.visible = rightButton.enabled = page < maxPageInclusive;
        }
    }
}
