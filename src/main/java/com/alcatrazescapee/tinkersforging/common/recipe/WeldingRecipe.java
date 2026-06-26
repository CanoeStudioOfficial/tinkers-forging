/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;

import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;

@ParametersAreNonnullByDefault
public class WeldingRecipe
{
    private final IRecipeIngredient firstInput;
    private final IRecipeIngredient secondInput;
    private final ItemStack outputStack;
    private final int minTier;
    private final boolean mergeInputs;

    public WeldingRecipe(IRecipeIngredient firstInput, IRecipeIngredient secondInput, int minTier)
    {
        this(firstInput, secondInput, ItemStack.EMPTY, minTier, true);
    }

    public WeldingRecipe(IRecipeIngredient firstInput, IRecipeIngredient secondInput, ItemStack outputStack, int minTier)
    {
        this(firstInput, secondInput, outputStack, minTier, false);
    }

    private WeldingRecipe(IRecipeIngredient firstInput, IRecipeIngredient secondInput, ItemStack outputStack, int minTier, boolean mergeInputs)
    {
        this.firstInput = firstInput;
        this.secondInput = secondInput;
        this.outputStack = outputStack.copy();
        this.minTier = minTier;
        this.mergeInputs = mergeInputs;
    }

    public boolean test(ItemStack first, ItemStack second, int tier)
    {
        return tier >= minTier && matchesInputs(first, second);
    }

    public boolean matchesInputs(ItemStack first, ItemStack second)
    {
        return (firstInput.test(first) && secondInput.test(second)) || (firstInput.test(second) && secondInput.test(first));
    }

    public boolean matchesInputIgnoreCount(ItemStack stack)
    {
        return firstInput.testIgnoreCount(stack) || secondInput.testIgnoreCount(stack);
    }

    @Nonnull
    public ItemStack getOutput(ItemStack main, ItemStack secondary)
    {
        if (!outputStack.isEmpty())
        {
            return outputStack.copy();
        }
        if (!mergeInputs)
        {
            return ItemStack.EMPTY;
        }
        ItemStack output = main.copy();
        output.setCount(Math.min(main.getMaxStackSize(), main.getCount() + secondary.getCount()));
        return output;
    }

    public IRecipeIngredient getFirstInput()
    {
        return firstInput;
    }

    public IRecipeIngredient getSecondInput()
    {
        return secondInput;
    }

    @Nonnull
    public ItemStack getOutput()
    {
        return outputStack.copy();
    }

    @Nonnull
    public ItemStack getDisplayOutput()
    {
        if (!outputStack.isEmpty())
        {
            return outputStack.copy();
        }
        if (mergeInputs)
        {
            List<ItemStack> stacks = firstInput.getStacks();
            if (!stacks.isEmpty())
            {
                ItemStack output = stacks.get(0).copy();
                output.setCount(Math.min(output.getMaxStackSize(), 2));
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public int getTier()
    {
        return minTier;
    }

    public boolean usesInputMergeFallback()
    {
        return mergeInputs;
    }
}
