/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.recipe.WeldingRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.oredict.IOreDictEntry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@SuppressWarnings("unused")
@ZenClass("mods.TinkersForging.Welding")
public final class CraftTweakerWeldingIntegration
{
    @ZenMethod
    public static void addRecipe(final IIngredient firstInput, final IIngredient secondInput, final IItemStack output, final int tier)
    {
        final IRecipeIngredient first = toIngredient(firstInput);
        final IRecipeIngredient second = toIngredient(secondInput);
        final ItemStack outputStack = toStack(output);
        if (first == null || second == null || outputStack.isEmpty())
        {
            TinkersForging.getLog().warn("Invalid CraftTweaker welding recipe. Both inputs and output must be item or ore ingredients.");
            return;
        }

        final WeldingRecipe recipe = new WeldingRecipe(first, second, outputStack, tier);
        CraftTweakerAPI.apply(new IAction()
        {
            @Override
            public void apply()
            {
                ModRecipes.addRecipeAction(() -> ModRecipes.WELDING.add(recipe));
            }

            @Override
            public String describe()
            {
                return "Adding Welding recipe for " + outputStack.getDisplayName() + "\n";
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(final IItemStack output)
    {
        final ItemStack outputStack = toStack(output);
        if (outputStack.isEmpty())
        {
            TinkersForging.getLog().warn("Invalid CraftTweaker welding removal. Output must be an item stack.");
            return;
        }

        CraftTweakerAPI.apply(new IAction()
        {
            @Override
            public void apply()
            {
                ModRecipes.addRecipeAction(() -> ModRecipes.WELDING.remove(outputStack));
            }

            @Override
            public String describe()
            {
                return "Removing Welding recipe for " + outputStack.getDisplayName() + "\n";
            }
        });
    }

    private static IRecipeIngredient toIngredient(final IIngredient input)
    {
        if (input instanceof IOreDictEntry)
        {
            return IRecipeIngredient.of(((IOreDictEntry) input).getName());
        }
        ItemStack stack = toStack(input);
        return stack.isEmpty() ? null : IRecipeIngredient.of(stack);
    }

    @Nonnull
    private static ItemStack toStack(final IIngredient ingredient)
    {
        if (!(ingredient instanceof IItemStack))
            return ItemStack.EMPTY;
        final Object obj = ingredient.getInternal();
        return obj instanceof ItemStack ? (ItemStack) obj : ItemStack.EMPTY;
    }
}
