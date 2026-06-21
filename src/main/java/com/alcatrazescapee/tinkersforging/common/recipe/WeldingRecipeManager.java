/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
public class WeldingRecipeManager
{
    private final List<WeldingRecipe> recipes;

    WeldingRecipeManager()
    {
        recipes = new ArrayList<>();
    }

    public void add(WeldingRecipe recipe)
    {
        recipes.add(recipe);
    }

    @Nullable
    public WeldingRecipe get(ItemStack first, ItemStack second, int tier)
    {
        return recipes.stream().filter(recipe -> recipe.test(first, second, tier)).findFirst().orElse(null);
    }
}
