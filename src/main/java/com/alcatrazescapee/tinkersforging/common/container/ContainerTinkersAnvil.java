package com.alcatrazescapee.tinkersforging.common.container;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.tinkersforging.common.slot.SlotForgeInput;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;

import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.*;

@ParametersAreNonnullByDefault
public class ContainerTinkersAnvil extends ContainerTileInventory<TileTinkersAnvil>
{
    public static final int ACTION_PLAN = 8;
    public static final int ACTION_WELD = 9;
    public static final int ACTION_PLAN_SELECT_BASE = 100;
    public static final int ACTION_PLAN_SELECT_MAX = ACTION_PLAN_SELECT_BASE + 4095;

    private final EntityPlayer player;

    public ContainerTinkersAnvil(EntityPlayer player, TileTinkersAnvil tile)
    {
        super(player.inventory, tile, 0, 41);
        this.player = player;
        tile.setCurrentPlayer(player);
    }

    public void onReceiveAction(int actionId)
    {
        if (actionId == ACTION_PLAN)
        {
            tile.openPlanGui(player);
            return;
        }
        if (actionId == ACTION_WELD)
        {
            tile.tryWeld(player);
            return;
        }
        if (actionId >= ACTION_PLAN_SELECT_BASE && actionId <= ACTION_PLAN_SELECT_MAX)
        {
            tile.selectPlan(actionId - ACTION_PLAN_SELECT_BASE);
            return;
        }
        if (actionId >= 0 && actionId < ForgeStep.values().length)
        {
            ForgeStep step = ForgeStep.valueOf(actionId);
            if (step != null)
                tile.work(player, step);
        }
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getStack().copy();
        ItemStack stackCopy = stack.copy();
        int containerSlots = inventorySlots.size() - player.inventory.mainInventory.size();

        if (index < containerSlots)
        {
            stack = slot.onTake(player, stack);
            if (!this.mergeItemStack(stack, containerSlots, inventorySlots.size(), true))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            for (int i = 0; i < containerSlots; i++)
            {
                if (inventorySlots.get(i).isItemValid(stack))
                {
                    if (this.mergeItemStack(stack, i, i + 1, false))
                    {
                        tile.setAndUpdateSlots(i);
                    }
                }
            }
        }

        if (stack.getCount() == 0)
        {
            slot.putStack(ItemStack.EMPTY);
        }
        else
        {
            slot.putStack(stack);
            slot.onSlotChanged();
        }
        if (stack.getCount() == stackCopy.getCount())
        {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return stackCopy;
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

    @Override
    protected void addContainerSlots()
    {
        IItemHandler cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (cap != null)
        {
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_MAIN, 22, 76, tile));
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_SECOND, 22, 20, tile));
            addSlotToContainer(new SlotTileCore(cap, SLOT_HAMMER, 138, 76, tile));
            addSlotToContainer(new SlotTileCore(cap, SLOT_CATALYST, 22, 38, tile));
        }
    }

}
