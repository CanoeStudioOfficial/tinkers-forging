/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedToolHead;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry.Definition;

@ParametersAreNonnullByDefault
public class RecipeExtendedHammer extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, @Nullable World worldIn)
    {
        return findMaterial(inv) != null;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        Definition material = findMaterial(inv);
        return material == null ? ItemStack.EMPTY : ItemExtendedHammer.get(material, 1);
    }

    @Override
    public boolean canFit(int width, int height)
    {
        return width >= 1 && height >= 2;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput()
    {
        ItemExtendedHammer item = ItemExtendedHammer.getItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Nullable
    private static Definition findMaterial(InventoryCrafting inv)
    {
        Definition material = null;
        int hammerHeadSlot = -1;
        int stickSlot = -1;

        for (int slot = 0; slot < inv.getSizeInventory(); slot++)
        {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack.isEmpty())
            {
                continue;
            }
            if (isHammerHead(stack))
            {
                if (hammerHeadSlot != -1)
                {
                    return null;
                }
                material = ExtendedMaterialRegistry.get(stack);
                if (material == null)
                {
                    return null;
                }
                hammerHeadSlot = slot;
            }
            else if (CoreHelpers.doesStackMatchOre(stack, "stickWood"))
            {
                if (stickSlot != -1)
                {
                    return null;
                }
                stickSlot = slot;
            }
            else
            {
                return null;
            }
        }

        if (hammerHeadSlot == -1 || stickSlot == -1)
        {
            return null;
        }

        int width = inv.getWidth();
        return stickSlot == hammerHeadSlot + width ? material : null;
    }

    private static boolean isHammerHead(ItemStack stack)
    {
        return stack.getItem() instanceof ItemExtendedToolHead && ((ItemExtendedToolHead) stack.getItem()).getType() == ItemType.HAMMER_HEAD;
    }
}
