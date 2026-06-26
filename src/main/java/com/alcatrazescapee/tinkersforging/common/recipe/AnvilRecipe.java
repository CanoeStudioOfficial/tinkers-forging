/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import java.util.Random;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;
import com.alcatrazescapee.alcatrazcore.inventory.recipe.RecipeCore;
import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeRule;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeSteps;
import io.netty.buffer.ByteBuf;

@ParametersAreNonnullByDefault
public class AnvilRecipe extends RecipeCore
{
    static boolean assertValid(AnvilRecipe recipe)
    {
        if (StringUtils.isNullOrEmpty(recipe.recipeName))
        {
            TinkersForging.getLog().warn("Recipe is invalid with empty name");
            return false;
        }
        if (recipe.outputStack.isEmpty())
        {
            TinkersForging.getLog().warn("Output is empty!");
            return false;
        }
        if (recipe.rules.length > 3)
        {
            TinkersForging.getLog().warn("Rules are invalid length!");
            return false;
        }
        if (recipe.rules.length > 0 && !ForgeRule.isConsistent(recipe.rules))
        {
            TinkersForging.getLog().warn("Rules cannot be satisfied for recipe {}", recipe.recipeName);
            return false;
        }
        return true;
    }

    public static AnvilRecipe fromSerialized(ByteBuf buffer)
    {
        int minTier = buffer.readInt();
        int seed = buffer.readInt();
        int hammerHits = buffer.readInt();
        boolean requiresHeat = buffer.readBoolean();
        int secondaryInputAmount = buffer.readBoolean() ? buffer.readInt() : 0;

        ItemStack output = ByteBufUtils.readItemStack(buffer);

        int numRules = buffer.readInt();
        ForgeRule[] rules = new ForgeRule[numRules];
        for (int i = 0; i < numRules; i++)
        {
            rules[i] = ForgeRule.valueOf(buffer.readInt());
        }

        return new AnvilRecipe(output, minTier, hammerHits, requiresHeat, secondaryInputAmount, rules).withSeed(seed);
    }

    private static final Random RANDOM = new Random();
    private static final int DIRECT_BASE_HITS = 8;
    private static final int DIRECT_HITS_PER_TIER = 4;
    private static final int DIRECT_HITS_PER_RULE = 2;

    private final ForgeRule[] rules;
    @Nullable private final IRecipeIngredient secondaryIngredient;
    private final int minTier;
    private final int hammerHits;
    private final boolean requiresHeat;
    private final int secondaryInputAmount;
    private final String recipeName;

    private int workingSeed = 0;

    public AnvilRecipe(ItemStack outputStack, String inputOre, int inputAmount, int minTier, ForgeRule... rules)
    {
        this(outputStack, inputOre, inputAmount, minTier, defaultHammerHits(minTier, rules), rules);
    }

    public AnvilRecipe(ItemStack outputStack, String inputOre, int inputAmount, int minTier, int hammerHits, ForgeRule... rules)
    {
        this(outputStack, inputOre, inputAmount, minTier, hammerHits, true, rules);
    }

    public AnvilRecipe(ItemStack outputStack, String inputOre, int inputAmount, int minTier, int hammerHits, boolean requiresHeat, ForgeRule... rules)
    {
        this(outputStack, inputOre, inputAmount, null, 0, minTier, hammerHits, requiresHeat, rules);
    }

    public AnvilRecipe(ItemStack outputStack, String inputOre, int inputAmount, @Nullable IRecipeIngredient secondaryIngredient, int secondaryInputAmount, int minTier, int hammerHits, boolean requiresHeat, ForgeRule... rules)
    {
        super(outputStack, inputOre, inputAmount);

        this.rules = rules;
        this.secondaryIngredient = secondaryIngredient;
        this.minTier = ModConfig.GENERAL.respectTiers ? minTier : Integer.MIN_VALUE;
        this.hammerHits = sanitizeHammerHits(hammerHits);
        this.requiresHeat = requiresHeat;
        this.secondaryInputAmount = sanitizeSecondaryInputAmount(secondaryIngredient, secondaryInputAmount);
        this.recipeName = outputStack.serializeNBT().toString();
    }

    public AnvilRecipe(ItemStack outputStack, ItemStack inputStack, int minTier, ForgeRule... rules)
    {
        this(outputStack, inputStack, minTier, defaultHammerHits(minTier, rules), rules);
    }

    public AnvilRecipe(ItemStack outputStack, ItemStack inputStack, int minTier, int hammerHits, ForgeRule... rules)
    {
        this(outputStack, inputStack, minTier, hammerHits, true, rules);
    }

    public AnvilRecipe(ItemStack outputStack, ItemStack inputStack, int minTier, int hammerHits, boolean requiresHeat, ForgeRule... rules)
    {
        this(outputStack, inputStack, null, 0, minTier, hammerHits, requiresHeat, rules);
    }

    public AnvilRecipe(ItemStack outputStack, ItemStack inputStack, @Nullable IRecipeIngredient secondaryIngredient, int secondaryInputAmount, int minTier, int hammerHits, boolean requiresHeat, ForgeRule... rules)
    {
        super(outputStack, inputStack);

        this.rules = rules;
        this.secondaryIngredient = secondaryIngredient;
        this.minTier = ModConfig.GENERAL.respectTiers ? minTier : Integer.MIN_VALUE;
        this.hammerHits = sanitizeHammerHits(hammerHits);
        this.requiresHeat = requiresHeat;
        this.secondaryInputAmount = sanitizeSecondaryInputAmount(secondaryIngredient, secondaryInputAmount);
        this.recipeName = outputStack.serializeNBT().toString();
    }

    private AnvilRecipe(ItemStack outputStack, int minTier, int hammerHits, boolean requiresHeat, int secondaryInputAmount, ForgeRule... rules)
    {
        // Only created on client
        super(outputStack, ItemStack.EMPTY);

        this.minTier = ModConfig.GENERAL.respectTiers ? minTier : Integer.MIN_VALUE;
        this.rules = rules;
        this.secondaryIngredient = null;
        this.hammerHits = sanitizeHammerHits(hammerHits);
        this.requiresHeat = requiresHeat;
        this.secondaryInputAmount = secondaryInputAmount;
        this.recipeName = "client:" + outputStack.serializeNBT().toString();
    }

    public static int defaultHammerHits(int minTier, ForgeRule... rules)
    {
        int ruleCount = rules == null ? 0 : rules.length;
        int effectiveTier = ModConfig.GENERAL.respectTiers ? minTier : Integer.MIN_VALUE;
        return Math.max(1, DIRECT_BASE_HITS + DIRECT_HITS_PER_TIER * Math.max(0, effectiveTier) + DIRECT_HITS_PER_RULE * ruleCount);
    }

    private static int sanitizeHammerHits(int hammerHits)
    {
        return Math.max(1, hammerHits);
    }

    private static int sanitizeSecondaryInputAmount(@Nullable IRecipeIngredient secondaryIngredient, int secondaryInputAmount)
    {
        return secondaryIngredient == null ? 0 : Math.max(1, secondaryInputAmount);
    }

    @Override
    @Nonnull
    public String getName()
    {
        return recipeName;
    }

    @Nonnull
    public ForgeRule[] getRules()
    {
        return rules;
    }

    public int getTier()
    {
        return minTier;
    }

    public int getHammerHits()
    {
        return hammerHits;
    }

    public int getInputAmount()
    {
        return inputAmount;
    }

    public boolean hasSecondaryInput()
    {
        return secondaryInputAmount > 0;
    }

    public int getSecondaryInputAmount()
    {
        return secondaryInputAmount;
    }

    @Nonnull
    public List<ItemStack> getSecondaryInputStacks()
    {
        return secondaryIngredient == null ? Collections.emptyList() : secondaryIngredient.getStacks();
    }

    public boolean requiresHeat()
    {
        return requiresHeat;
    }

    public int getWorkingTarget(long seed)
    {
        RANDOM.setSeed(seed + workingSeed);
        return 40 + RANDOM.nextInt(IForgeItem.MAX_WORK + 4 - 2 * 40);
    }

    public boolean stepsMatch(ForgeSteps steps)
    {
        for (ForgeRule rule : rules)
        {
            if (!rule.matches(steps))
                return false;
        }
        return true;
    }

    public boolean matchesInputIgnoreCount(Object input)
    {
        return ingredient.testIgnoreCount(input);
    }

    public boolean matchesSecondaryInputIgnoreCount(Object input)
    {
        return secondaryIngredient != null && secondaryIngredient.testIgnoreCount(input);
    }

    public boolean matchesInputs(ItemStack input, ItemStack secondaryInput)
    {
        if (!test(input))
            return false;
        if (!hasSecondaryInput())
            return secondaryInput.isEmpty();
        return secondaryIngredient != null && secondaryIngredient.test(secondaryInput);
    }

    public boolean matchesInputsExact(ItemStack input, ItemStack secondaryInput)
    {
        if (!matchesInputs(input, secondaryInput) || input.getCount() != getInputAmount())
            return false;
        return hasSecondaryInput() ? secondaryInput.getCount() == getSecondaryInputAmount() : secondaryInput.isEmpty();
    }

    public ItemStack consumeSecondaryInput(ItemStack secondaryInput)
    {
        return hasSecondaryInput() ? CoreHelpers.consumeItem(secondaryInput, secondaryInputAmount) : secondaryInput;
    }

    public void serialize(ByteBuf buffer)
    {
        // Numbers
        buffer.writeInt(minTier);
        buffer.writeInt(workingSeed);
        buffer.writeInt(hammerHits);
        buffer.writeBoolean(requiresHeat);
        buffer.writeBoolean(hasSecondaryInput());
        if (hasSecondaryInput())
        {
            buffer.writeInt(secondaryInputAmount);
        }

        // Output
        ByteBufUtils.writeItemStack(buffer, outputStack);

        // Rules
        buffer.writeInt(rules.length);
        for (ForgeRule rule : rules)
            buffer.writeInt(ForgeRule.getID(rule));
    }

    public boolean matchesOutput(ItemStack output)
    {
        return CoreHelpers.doStacksMatch(outputStack, output);
    }

    @Nonnull
    AnvilRecipe withSeed(int seed)
    {
        this.workingSeed = seed;
        return this;
    }

}
