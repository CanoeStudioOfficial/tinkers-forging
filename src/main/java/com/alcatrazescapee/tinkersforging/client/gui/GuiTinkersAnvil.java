/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.gui;

import java.io.IOException;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.client.gui.GuiContainerTileCore;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.network.PacketAnvilButton;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeRule;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeSteps;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;
import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.*;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
public class GuiTinkersAnvil extends GuiContainerTileCore<TileTinkersAnvil>
{
    static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/gui/tinkers_anvil.png");

    private static final int WORK_BAR_X = 13;
    private static final int WORK_BAR_WIDTH = 145;
    private static final int WORK_SCALE_Y = 94;
    private static final int WORK_SCALE_MAX_LABEL = 14;
    private static final int WORK_SCALE_COLOR = 0x006CFF;

    private static final boolean isJEIEnabled = Loader.isModLoaded("jei");

    public GuiTinkersAnvil(TileTinkersAnvil tile, String translationKey, Container container, InventoryPlayer playerInv)
    {
        super(tile, container, playerInv, BACKGROUND, translationKey);

        this.ySize = 207;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        int id = -1;
        // Draw buttons here
        for (ForgeStep step : ForgeStep.values())
        {
            ++id;
            addButton(new GuiButtonAnvilStep(step, guiLeft, guiTop));
        }

        addButton(new GuiButtonAnvilPlan(tile, guiLeft, guiTop));
        addButton(new GuiButtonAnvilWeld(tile, guiLeft, guiTop));
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY)
    {
        // Rule tooltips
        int x = guiLeft + 59;
        int y = guiTop + 13;

        for (int i = FIELD_FIRST_RULE; i <= FIELD_THIRD_RULE; i++)
        {
            if (mouseX >= x && mouseY >= y && mouseX < x + 20 && mouseY < y + 22)
            {
                ForgeRule rule = ForgeRule.valueOf(tile.getField(i));
                if (rule != null)
                {
                    drawHoveringText(I18n.format(MOD_ID + ".tooltip." + rule.name().toLowerCase()), mouseX, mouseY);
                }
            }
            x += 19;
        }

        // Step Button Tooltips
        for (GuiButton button : buttonList)
        {
            if (button instanceof GuiButtonAnvilStep && button.isMouseOver())
            {
                drawHoveringText(I18n.format(((GuiButtonAnvilStep) button).getTooltip()), mouseX, mouseY);
            }
            if (button instanceof GuiButtonAnvilPlan && button.isMouseOver())
            {
                drawHoveringText(I18n.format(((GuiButtonAnvilPlan) button).getTooltip()), mouseX, mouseY);
            }
            if (button instanceof GuiButtonAnvilWeld && button.isMouseOver())
            {
                drawHoveringText(I18n.format(((GuiButtonAnvilWeld) button).getTooltip()), mouseX, mouseY);
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawWorkScaleLabels();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // JEI Question Mark Icon
        if (isJEIEnabled)
            drawTexturedModalRect(guiLeft + 141, guiTop + 40, 0, 207, 9, 14);

        AnvilRecipe recipe = tile.getRecipe();
        if (recipe != null)
        {
            // Progress + Target
            int progress = tile.getField(TileTinkersAnvil.FIELD_PROGRESS);
            drawTexturedModalRect(guiLeft + WORK_BAR_X + scaleWork(progress), guiTop + 104, 176, 0, 5, 5);

            int target = tile.getField(TileTinkersAnvil.FIELD_TARGET);
            int range = ModConfig.BALANCE.forgeTargetRange + (5 - recipe.getTier()) * ModConfig.BALANCE.forgeTierRangeMod;
            drawTarget(target, range);
        }

        // Last Three Steps
        for (int i = FIELD_LAST_STEP; i <= FIELD_THIRD_STEP; i++)
        {
            ForgeStep step = ForgeStep.valueOf(tile.getField(i));
            if (step != null)
            {
                int xOffset = 19 * (i - FIELD_LAST_STEP);
                drawTexturedModalRect(guiLeft + 99 - xOffset, guiTop + 34, step.getTexU(), step.getTexV(), 16, 16);
            }
        }

        ForgeSteps steps = tile.getSteps();

        // Rules
        if (recipe != null)
        {
            for (int i = FIELD_FIRST_RULE; i <= FIELD_THIRD_RULE; i++)
            {
                ForgeRule rule = ForgeRule.valueOf(tile.getField(i));
                if (rule != null)
                {
                    int xOffset = 19 * (i - FIELD_FIRST_RULE);
                    // The rule icon
                    drawTexturedModalRect(guiLeft + 61 + xOffset, guiTop + 13, rule.getIconU(), rule.getIconV(), 16, 16);
                    if (rule.matches(steps))
                    {
                        GlStateManager.color(0f, 0.6f, 0.2f, 1f);
                    }
                    else
                    {
                        GlStateManager.color(1f, 0.4f, 0f, 1f);
                    }
                    drawTexturedModalRect(guiLeft + 59 + xOffset, guiTop + 13, rule.getOutlineU(), rule.getOutlineV(), 20, 22);
                    GlStateManager.color(1f, 1f, 1f, 1f);
                }
            }
        }

    }

    private void drawWorkScaleLabels()
    {
        for (int i = 0; i <= WORK_SCALE_MAX_LABEL; i++)
        {
            String label = Integer.toString(i);
            int x = WORK_BAR_X + (i * WORK_BAR_WIDTH) / WORK_SCALE_MAX_LABEL - fontRenderer.getStringWidth(label) / 2;
            fontRenderer.drawString(label, x, WORK_SCALE_Y, WORK_SCALE_COLOR);
        }
    }

    private void drawTarget(int target, int range)
    {
        if (range < 2)
        {
            drawTexturedModalRect(guiLeft + WORK_BAR_X + scaleWork(target), guiTop + 98, 181, 0, 5, 5);
        }
        else
        {
            int leftLimit = scaleWork(Math.max(IForgeItem.MIN_WORK, target - range));
            int rightLimit = scaleWork(Math.min(IForgeItem.MAX_WORK - 1, target + range));

            drawTexturedModalRect(guiLeft + WORK_BAR_X + leftLimit, guiTop + 96, 176, 7, 5, 7);
            drawTexturedModalRect(guiLeft + WORK_BAR_X + rightLimit, guiTop + 96, 186, 7, 5, 7);
            for (int i = leftLimit + 2; i < rightLimit - 1; i++)
            {
                drawTexturedModalRect(guiLeft + WORK_BAR_X + 2 + i, guiTop + 94, 192, 5, 1, 5);
            }
            if (range > 2)
            {
                drawTexturedModalRect(guiLeft + WORK_BAR_X + (rightLimit + leftLimit) / 2, guiTop + 94, 181, 5, 5, 5);
            }
        }
    }

    private static int scaleWork(int work)
    {
        int clamped = Math.max(IForgeItem.MIN_WORK, Math.min(IForgeItem.MAX_WORK - 1, work));
        return Math.round((float) clamped * WORK_BAR_WIDTH / (IForgeItem.MAX_WORK - 1));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        // Handle gui buttons being clicked here
        if (button instanceof GuiButtonAnvilStep || button instanceof GuiButtonAnvilPlan || button instanceof GuiButtonAnvilWeld)
        {
            TinkersForging.getNetwork().sendToServer(new PacketAnvilButton(button.id));
        }
        super.actionPerformed(button);
    }
}
