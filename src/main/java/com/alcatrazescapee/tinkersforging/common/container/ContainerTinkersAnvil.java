package com.alcatrazescapee.tinkersforging.common.container;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotOutput;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.slot.SlotDisplay;
import com.alcatrazescapee.tinkersforging.common.slot.SlotForgeInput;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;
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
        super(player.inventory, tile, 0, 56);
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
            if (attemptWork(actionId % 4))
                tile.addStep(ForgeStep.valueOf(actionId));
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
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_MAIN, 22, 21, tile));
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_SECOND, 22, 39, tile));
            addSlotToContainer(new SlotOutput(cap, SLOT_OUTPUT, 140, 21));
            addSlotToContainer(new SlotTileCore(cap, SLOT_HAMMER, 140, 39, tile));
            addSlotToContainer(new SlotTileCore(cap, SLOT_CATALYST, 22, 57, tile));
            addSlotToContainer(new SlotDisplay(cap, SLOT_DISPLAY, 80, 21));
        }
    }

    private boolean attemptWork(int amount)
    {
        Slot slotInput = inventorySlots.get(SLOT_INPUT_MAIN);
        if (slotInput == null)
            return false;

        ItemStack stack = slotInput.getStack();
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap == null)
            return false;

        AnvilRecipe recipe = ModRecipes.ANVIL.getByName(cap.getRecipeName());
        if (recipe == null)
        {
            return false;
        }
        if (tile.getTier() < recipe.getTier())
        {
            sendProblem("tier_too_low");
            return false;
        }
        if (!cap.isWorkable())
        {
            sendProblem("too_cold");
            return false;
        }

        Slot slot = inventorySlots.get(SLOT_HAMMER);
        if (slot == null)
            return false;

        stack = slot.getStack();
        if (!stack.isEmpty())
        {
            stack.damageItem(amount, player);
            if (stack.getCount() <= 0)
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.putStack(stack);
            }
            return true;
        }
        else
        {
            sendProblem("no_hammer");
            return false;
        }
    }

    private void sendProblem(String translationKey)
    {
        player.sendMessage(new TextComponentString("" + TextFormatting.RED).appendSibling(new TextComponentTranslation(MOD_ID + ".tooltip." + translationKey)));
    }
}
