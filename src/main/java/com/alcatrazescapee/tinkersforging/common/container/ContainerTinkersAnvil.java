package com.alcatrazescapee.tinkersforging.common.container;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.alcatrazcore.inventory.slot.SlotTileCore;
import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
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
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_MAIN, 22, 76, tile));
            addSlotToContainer(new SlotForgeInput(cap, SLOT_INPUT_SECOND, 22, 20, tile));
            addSlotToContainer(new SlotTileCore(cap, SLOT_HAMMER, 138, 76, tile));
            addSlotToContainer(new SlotTileCore(cap, SLOT_CATALYST, 22, 38, tile));
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
        ForgeStep step = ForgeStep.valueOf(amount);
        if (step == null)
        {
            return false;
        }
        if (cap.getSteps().isEmpty() && cap.getWork() == IForgeItem.MIN_WORK && step.getStepAmount() < 0)
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

        HammerStack hammer = getHammer();
        if (hammer.stack.isEmpty())
        {
            sendProblem("no_hammer");
            return false;
        }

        hammer.stack.damageItem(amount, player);
        if (hammer.slot != null)
        {
            if (hammer.stack.getCount() <= 0)
            {
                hammer.slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                hammer.slot.putStack(hammer.stack);
            }
        }
        else if (hammer.hand != null && hammer.stack.getCount() <= 0)
        {
            player.setHeldItem(hammer.hand, ItemStack.EMPTY);
        }
        return true;
    }

    private HammerStack getHammer()
    {
        Slot slot = inventorySlots.get(SLOT_HAMMER);
        if (slot != null)
        {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && CoreHelpers.doesStackMatchOre(stack, "hammer"))
            {
                return new HammerStack(stack, slot, null);
            }
        }

        ItemStack mainHand = player.getHeldItemMainhand();
        if (!mainHand.isEmpty() && CoreHelpers.doesStackMatchOre(mainHand, "hammer"))
        {
            return new HammerStack(mainHand, null, EnumHand.MAIN_HAND);
        }

        ItemStack offHand = player.getHeldItemOffhand();
        if (!offHand.isEmpty() && CoreHelpers.doesStackMatchOre(offHand, "hammer"))
        {
            return new HammerStack(offHand, null, EnumHand.OFF_HAND);
        }

        return new HammerStack(ItemStack.EMPTY, null, null);
    }

    private void sendProblem(String translationKey)
    {
        player.sendMessage(new TextComponentString("" + TextFormatting.RED).appendSibling(new TextComponentTranslation(MOD_ID + ".tooltip." + translationKey)));
    }

    private static final class HammerStack
    {
        private final ItemStack stack;
        private final Slot slot;
        private final EnumHand hand;

        private HammerStack(ItemStack stack, Slot slot, EnumHand hand)
        {
            this.stack = stack;
            this.slot = slot;
            this.hand = hand;
        }
    }
}
