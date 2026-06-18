/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge;

import static com.alcatrazescapee.tinkersforging.common.tile.TileCharcoalForge.*;

public class ContainerCharcoalForge extends ContainerTileInventory<TileCharcoalForge>
{
    public ContainerCharcoalForge(InventoryPlayer playerInv, TileCharcoalForge tile)
    {
        super(playerInv, tile, 0, 20);
    }

    @Override
    protected void addContainerSlots()
    {
        IItemHandler cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap != null)
        {
            int index = SLOT_FUEL_MIN;
            addSlotToContainer(new SlotTileCore(cap, index++, 80, 70, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 98, 52, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 62, 52, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 116, 34, tile));
            addSlotToContainer(new SlotTileCore(cap, index, 44, 34, tile));

            index = SLOT_INPUT_MIN;
            addSlotToContainer(new SlotTileCore(cap, index++, 80, 52, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 98, 34, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 62, 34, tile));
            addSlotToContainer(new SlotTileCore(cap, index++, 116, 16, tile));
            addSlotToContainer(new SlotTileCore(cap, index, 44, 16, tile));

            for (int i = SLOT_EXTRA_MIN; i <= SLOT_EXTRA_MAX; i++)
            {
                addSlotToContainer(new SlotTileCore(cap, i, 152, 16 + 18 * (i - SLOT_EXTRA_MIN), tile));
            }
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        detectAndSendAllChanges();
        detectAndSendFieldChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        if (tile == null || tile.getWorld() == null || tile.isInvalid())
            return false;

        BlockPos pos = tile.getPos();
        return entityPlayer.world == tile.getWorld()
                && tile.getWorld().getTileEntity(pos) == tile
                && entityPlayer.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }
}
