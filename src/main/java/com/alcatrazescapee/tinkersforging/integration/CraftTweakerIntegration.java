/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeRule;
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
@ZenClass("mods.TinkersForging.Anvil")
public final class CraftTweakerIntegration
{
    @ZenMethod
    public static void addRecipe(final IIngredient input, final IItemStack output, final int tier)
    {
        addRecipeInternal(input, output, tier, AnvilRecipe.defaultHammerHits(tier));
    }

    @ZenMethod
    public static void addRecipe(final IIngredient input, final IItemStack output, final int tier, final int hammerHits)
    {
        addRecipeInternal(input, output, tier, hammerHits);
    }

    @ZenMethod
    public static void addRecipe(final IIngredient input, final IItemStack output, final int tier, final String... ruleNames)
    {
        addRecipeInternal(input, output, tier, defaultHammerHits(tier, ruleNames), ruleNames);
    }

    @ZenMethod
    public static void addRecipe(final IIngredient input, final IItemStack output, final int tier, final int hammerHits, final String... ruleNames)
    {
        addRecipeInternal(input, output, tier, hammerHits, ruleNames);
    }

    private static void addRecipeInternal(final IIngredient input, final IItemStack output, final int tier, final int hammerHits, final String... ruleNames)
    {
        final AnvilRecipe recipe;
        final ItemStack outputStack = toStack(output);
        if (outputStack.isEmpty())
        {
            TinkersForging.getLog().warn("Invalid CraftTweaker anvil recipe. Output must be an item stack.");
            return;
        }

        final List<ForgeRule> rules = parseRules(ruleNames);
        if (rules == null)
        {
            return;
        }
        if (rules.size() > 3)
        {
            TinkersForging.getLog().warn("Illegal number of rules {} specified in craft tweaker recipe!", rules.size());
            return;
        }
        if (hammerHits < 1)
        {
            TinkersForging.getLog().warn("Invalid CraftTweaker anvil recipe. Hammer hits must be at least 1.");
            return;
        }
        if (input instanceof IOreDictEntry)
        {
            final IOreDictEntry ore = (IOreDictEntry) input;
            recipe = new AnvilRecipe(outputStack, ore.getName(), ore.getAmount(), tier, hammerHits, rules.toArray(new ForgeRule[0]));
        }
        else
        {
            ItemStack inputStack = toStack(input);
            if (inputStack.isEmpty())
            {
                TinkersForging.getLog().warn("Invalid CraftTweaker anvil recipe. Input must be an item stack or ore dictionary entry.");
                return;
            }
            recipe = new AnvilRecipe(outputStack, inputStack, tier, hammerHits, rules.toArray(new ForgeRule[0]));
        }
        CraftTweakerAPI.apply(new IAction()
        {
            @Override
            public void apply()
            {
                ModRecipes.addRecipeAction(() -> ModRecipes.ANVIL.add(recipe));
            }

            @Override
            public String describe()
            {
                return "Adding Anvil recipe for " + recipe.getName() + "\n";
            }
        });
    }

    @ZenMethod
    public static void removeRecipe(final IItemStack output)
    {
        final ItemStack stack = toStack(output);
        CraftTweakerAPI.apply(new IAction()
        {
            @Override
            public void apply()
            {
                ModRecipes.addRecipeAction(() -> ModRecipes.ANVIL.remove(stack));
            }

            @Override
            public String describe()
            {
                return "Removing Anvil recipe for " + stack.getDisplayName() + "\n";
            }
        });
    }

    @ZenMethod
    public static void addItemHeat(final IIngredient input, final int workingTemperature, final int meltingTemperature)
    {
        final IRecipeIngredient ingredient;
        if (input instanceof IOreDictEntry)
        {
            final IOreDictEntry ore = (IOreDictEntry) input;
            ingredient = IRecipeIngredient.of(ore.getName());
        }
        else
        {
            ingredient = IRecipeIngredient.of(toStack(input));
        }
        CraftTweakerAPI.apply(new IAction()
        {
            @Override
            public void apply()
            {
                CapabilityForgeItem.registerStackCapability(ingredient, (float) workingTemperature, (float) meltingTemperature);
            }

            @Override
            public String describe()
            {
                return "Adding heat registry for " + ingredient.getName() + "\n";
            }
        });
    }

    private static List<ForgeRule> parseRules(final String... ruleNames)
    {
        final int ruleCount = ruleNames == null ? 0 : ruleNames.length;
        final List<ForgeRule> rules = new ArrayList<>(ruleCount);
        if (ruleNames != null)
        {
            for (String ruleName : ruleNames)
            {
                try
                {
                    final ForgeRule rule = ForgeRule.valueOf(ruleName.toUpperCase());
                    rules.add(rule);
                }
                catch (IllegalArgumentException e)
                {
                    TinkersForging.getLog().warn("Illegal rule name {} specified in craft tweaker recipe!", ruleName);
                    return null;
                }
            }
        }
        return rules;
    }

    private static int defaultHammerHits(final int tier, final String... ruleNames)
    {
        ForgeRule[] rules = new ForgeRule[ruleNames == null ? 0 : ruleNames.length];
        return AnvilRecipe.defaultHammerHits(tier, rules);
    }

    @Nonnull
    private static ItemStack toStack(final IIngredient ingredient)
    {
        if (!(ingredient instanceof IItemStack))
            return ItemStack.EMPTY;
        final Object obj = ingredient.getInternal();
        return obj instanceof ItemStack ? ((ItemStack) obj).copy() : ItemStack.EMPTY;
    }
}
