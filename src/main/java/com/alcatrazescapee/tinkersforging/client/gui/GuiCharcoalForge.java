/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.client.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.client.gui.GuiContainerTileCore;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;
import static com.alcatrazescapee.tinkersforging.common.blocks.BlockCharcoalForge.LIT;
import static com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem.MAX_TEMPERATURE;
import static com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge.*;

@SideOnly(Side.CLIENT)
public class GuiCharcoalForge extends GuiContainerTileCore<TileCharcoalForge>
{
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/gui/charcoal_forge.png");

    public GuiCharcoalForge(TileCharcoalForge tile, Container container, InventoryPlayer playerInv, String titleKey)
    {
        super(tile, container, playerInv, BACKGROUND, titleKey);
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int fuelTicksRemaining = tile.getField(FIELD_FUEL);
        int fuelTicksMax = tile.getField(FIELD_FUEL_MAX);
        if (fuelTicksRemaining > 0 && tile.getWorld().getBlockState(tile.getPos()).getValue(LIT))
        {
            // Draw burn time
            if (fuelTicksMax > 0)
            {
                int burnTime = Math.round(14 * fuelTicksRemaining / (float) fuelTicksMax);
                drawTexturedModalRect(guiLeft + 8, guiTop + 76 - burnTime, 176, 14 - burnTime, 15, burnTime);
            }
        }

        int temperature = tile.getField(FIELD_TEMPERATURE);
        if (temperature > 0)
        {
            int scaledTemp = Math.round(51 * temperature / MAX_TEMPERATURE);
            drawTexturedModalRect(guiLeft + 8, guiTop + 76 - Math.min(51, scaledTemp), 176, 0, 15, Math.min(51, scaledTemp));
        }
    }
}
