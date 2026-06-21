/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;

import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;

@ParametersAreNonnullByDefault
public class WeldingRecipe
{
    private final IRecipeIngredient firstInput;
    private final IRecipeIngredient secondInput;
    private final int minTier;

    public WeldingRecipe(IRecipeIngredient firstInput, IRecipeIngredient secondInput, int minTier)
    {
        this.firstInput = firstInput;
        this.secondInput = secondInput;
        this.minTier = minTier;
    }

    public boolean test(ItemStack first, ItemStack second, int tier)
    {
        return tier >= minTier && ((firstInput.test(first) && secondInput.test(second)) || (firstInput.test(second) && secondInput.test(first)));
    }

    @Nonnull
    public ItemStack getOutput(ItemStack main, ItemStack secondary)
    {
        ItemStack output = main.copy();
        output.setCount(Math.min(main.getMaxStackSize(), main.getCount() + secondary.getCount()));
        return output;
    }
}
