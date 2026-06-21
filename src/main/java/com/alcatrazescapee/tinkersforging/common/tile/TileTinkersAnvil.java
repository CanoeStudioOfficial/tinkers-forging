/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.tile;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.tile.ITileFields;
import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;
import com.alcatrazescapee.tinkersforging.common.network.PacketAnvilRecipeUpdate;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.recipe.WeldingRecipe;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeRule;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeSteps;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@ParametersAreNonnullByDefault
public class TileTinkersAnvil extends TileInventory implements ITileFields
{
    public static final int FIELD_PROGRESS = 0;
    public static final int FIELD_TARGET = 1;
    public static final int FIELD_LAST_STEP = 2;
    public static final int FIELD_SECOND_STEP = 3;
    public static final int FIELD_THIRD_STEP = 4;
    public static final int FIELD_FIRST_RULE = 5;
    public static final int FIELD_SECOND_RULE = 6;
    public static final int FIELD_THIRD_RULE = 7;

    public static final int SLOT_INPUT_MAIN = 0;
    public static final int SLOT_INPUT_SECOND = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_HAMMER = 3;
    public static final int SLOT_CATALYST = 4;
    public static final int SLOT_DISPLAY = 5;

    @Deprecated
    public static final int SLOT_INPUT = SLOT_INPUT_MAIN;

    private static final int DIRECT_BASE_HITS = 8;
    private static final int DIRECT_HITS_PER_TIER = 4;
    private static final int DIRECT_HITS_PER_RULE = 2;

    private AnvilRecipe cachedAnvilRecipe = null;
    private String lastRecipeName = null;
    private EntityPlayer currentPlayer = null;
    private ForgeSteps steps;
    private ForgeRule[] rules;
    private int workingProgress = 0; // Min = 0, Max = 150. If it goes over / under you lose the input
    private int workingTarget = 0;
    private int directProgress = 0;

    public TileTinkersAnvil()
    {
        super(6);

        steps = new ForgeSteps();
        rules = new ForgeRule[3];
    }

    public AnvilRecipe getRecipe()
    {
        // Called on server
        return cachedAnvilRecipe;
    }

    public ItemStack getInputStack()
    {
        return inventory.getStackInSlot(SLOT_INPUT_MAIN);
    }

    public ItemStack getSecondaryInputStack()
    {
        return inventory.getStackInSlot(SLOT_INPUT_SECOND);
    }

    public ItemStack getFluxStack()
    {
        return inventory.getStackInSlot(SLOT_CATALYST);
    }

    @Nullable
    private IForgeItem getInputForgeItem()
    {
        return getInputStack().getCapability(CapabilityForgeItem.CAPABILITY, null);
    }

    public ItemStack getSelectedPlanOutput()
    {
        return inventory.getStackInSlot(SLOT_DISPLAY);
    }

    public boolean hasSelectablePlan()
    {
        return !ModRecipes.ANVIL.getAllMatching(inventory.getStackInSlot(SLOT_INPUT_MAIN), getTier()).isEmpty();
    }

    @Nullable
    public AnvilRecipe getDirectRecipeForDisplay()
    {
        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap == null)
            return null;

        AnvilRecipe recipe = getSelectedRecipe(stack, cap, true);
        if (recipe != null)
            return recipe;

        List<AnvilRecipe> matches = ModRecipes.ANVIL.getAllMatching(stack, getTier());
        return matches.isEmpty() ? null : matches.get(0);
    }

    public int getDirectProgress()
    {
        return directProgress;
    }

    public int getDirectMaxProgress()
    {
        AnvilRecipe recipe = getDirectRecipeForDisplay();
        return recipe == null ? 1 : getRequiredDirectHits(recipe);
    }

    public boolean canWeldNow()
    {
        return getWeldStatus() == WeldStatus.WELDABLE;
    }

    public boolean canWeldNow(@Nullable EntityPlayer player)
    {
        return getWeldStatus(player) == WeldStatus.WELDABLE;
    }

    public WeldStatus getWeldStatus()
    {
        return getWeldStatus(null);
    }

    public WeldStatus getWeldStatus(@Nullable EntityPlayer player)
    {
        ItemStack main = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        ItemStack secondary = inventory.getStackInSlot(SLOT_INPUT_SECOND);
        ItemStack hammer = getHammer(player).stack;
        ItemStack flux = inventory.getStackInSlot(SLOT_CATALYST);

        if (main.isEmpty() || secondary.isEmpty())
            return WeldStatus.PROBLEM_INPUTS;

        WeldingRecipe recipe = ModRecipes.WELDING.getForInputs(main, secondary);
        if (recipe == null)
            return WeldStatus.PROBLEM_RECIPE;
        if (getTier() < recipe.getTier())
            return WeldStatus.PROBLEM_TIER;

        IForgeItem mainHeat = main.getCapability(CapabilityForgeItem.CAPABILITY, null);
        IForgeItem secondHeat = secondary.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (mainHeat == null || secondHeat == null || !mainHeat.isWeldable() || !secondHeat.isWeldable())
            return WeldStatus.PROBLEM_HEAT;

        if (flux.isEmpty() || !isFlux(flux))
            return WeldStatus.PROBLEM_FLUX;
        if (hammer.isEmpty() || !isHammer(hammer))
            return WeldStatus.PROBLEM_HAMMER;

        return recipe.getOutput(main, secondary).isEmpty() ? WeldStatus.PROBLEM_RECIPE : WeldStatus.WELDABLE;
    }

    public void setRecipe(@Nullable AnvilRecipe recipe)
    {
        cachedAnvilRecipe = recipe;

        // update recipe-based fields (both sides)
        // note on client the recipe is only a shallow copy of the actual recipe (it has no input paramaters)
        if (recipe != null)
        {
            ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
            IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
            if (cap != null && (world == null || !world.isRemote))
            {
                cap.setRecipe(cachedAnvilRecipe);
            }

            inventory.setStackInSlot(SLOT_DISPLAY, cachedAnvilRecipe.getOutput().copy());
        }
        else
        {
            inventory.setStackInSlot(SLOT_DISPLAY, ItemStack.EMPTY);
        }
    }

    @Override
    public void setAndUpdateSlots(int slot)
    {
        super.setAndUpdateSlots(slot);

        if (world.isRemote)
            return;

        if (slot == SLOT_INPUT_MAIN)
        {
            directProgress = 0;
        }

        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);

        if (cap == null)
        {
            // cap was null, most likely if the slot was empty
            resetFields();
            updateRecipe(null);
            inventory.setStackInSlot(SLOT_DISPLAY, ItemStack.EMPTY);
            return;
        }

        AnvilRecipe recipe = getSelectedRecipe(stack, cap, true);
        if (recipe == null)
        {
            resetFields();
            cap.setRecipe(null);
            updateRecipe(null);
            inventory.setStackInSlot(SLOT_DISPLAY, ItemStack.EMPTY);
            return;
        }

        applyRecipeState(stack, cap, recipe);
    }

    public boolean handleDirectInteraction(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ)
    {
        if (world == null)
            return false;

        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty())
        {
            if (!world.isRemote)
            {
                extractDirect(player, player.isSneaking());
            }
            return true;
        }

        if (isHammer(held))
        {
            if (!world.isRemote)
            {
                if (player.isSneaking())
                {
                    cycleDirectRecipe(player);
                }
                else if (canWeldNow(player))
                {
                    tryWeld(player);
                }
                else
                {
                    directWork(player);
                }
            }
            return true;
        }

        if (player.isSneaking())
        {
            if (isFlux(held))
            {
                return world.isRemote ? canInsertHeldStack(player, hand, SLOT_CATALYST) : insertHeldStack(player, hand, SLOT_CATALYST);
            }
            if (held.hasCapability(CapabilityForgeItem.CAPABILITY, null))
            {
                if (inventory.getStackInSlot(SLOT_INPUT_MAIN).isEmpty())
                {
                    return world.isRemote ? canInsertHeldStack(player, hand, SLOT_INPUT_MAIN) : insertHeldStack(player, hand, SLOT_INPUT_MAIN);
                }
                return world.isRemote ? canInsertHeldStack(player, hand, SLOT_INPUT_SECOND) : insertHeldStack(player, hand, SLOT_INPUT_SECOND);
            }
        }
        return false;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        switch (slot)
        {
            case SLOT_INPUT:
            case SLOT_INPUT_SECOND:
                return stack.hasCapability(CapabilityForgeItem.CAPABILITY, null);
            case SLOT_HAMMER:
                return isHammer(stack);
            case SLOT_CATALYST:
                return isFlux(stack);
            default:
                return false;
        }
    }

    @Override
    public void onBreakBlock()
    {
        // -1 is to skip the display slot
        for (int i = 0; i < inventory.getSlots() - 1; ++i)
        {
            if (i == SLOT_INPUT_MAIN || i == SLOT_INPUT_SECOND)
            {
                ItemStack input = inventory.getStackInSlot(i);
                CapabilityForgeItem.clearStackCheckRecipe(input);
                CoreHelpers.dropItemInWorld(world, pos, input);
            }
            else
            {
                CoreHelpers.dropItemInWorld(world, pos, inventory.getStackInSlot(i));
            }
        }
    }

    public void setCurrentPlayer(EntityPlayer player)
    {
        this.currentPlayer = player;
    }

    public int getTier()
    {
        return ((BlockTinkersAnvil) world.getBlockState(pos).getBlock()).getTier();
    }

    public void work(EntityPlayer player, ForgeStep step)
    {
        if (world == null || world.isRemote)
            return;

        ItemStack input = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        IForgeItem cap = input.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap == null)
            return;

        HammerStack hammer = getHammer(player);
        if (hammer.stack.isEmpty())
        {
            sendProblem(player, "no_hammer");
            return;
        }

        AnvilRecipe recipe = getRecipeForWork(input, cap);
        if (recipe == null)
            return;

        WorkFailure failure = validateWork(cap, recipe, step);
        if (failure != null)
        {
            failure.send(player);
            return;
        }

        performWork(player, input, cap, recipe, hammer, step);
    }

    @Nullable
    private AnvilRecipe getRecipeForWork(ItemStack input, IForgeItem cap)
    {
        AnvilRecipe recipe = getSelectedRecipe(input, cap, true);
        if (recipe == null)
            return null;

        if (!recipe.test(input))
        {
            setAndUpdateSlots(SLOT_INPUT_MAIN);
            return null;
        }
        return recipe;
    }

    @Nullable
    private WorkFailure validateWork(IForgeItem cap, AnvilRecipe recipe, ForgeStep step)
    {
        if (getTier() < recipe.getTier())
        {
            return WorkFailure.TIER_TOO_LOW;
        }
        if (!cap.isWorkable())
        {
            return WorkFailure.TOO_COLD;
        }
        if (isInitialNegativeStep(cap, step))
        {
            return WorkFailure.CANNOT_MOVE_LOWER;
        }
        return null;
    }

    private void performWork(EntityPlayer player, ItemStack input, IForgeItem cap, AnvilRecipe recipe, HammerStack hammer, ForgeStep step)
    {
        applyRecipeState(input, cap, recipe);
        applyForgeStep(cap, step);
        damageHammer(hammer, player);
        if (isOverworked(cap.getWork()))
        {
            overworkInput();
            setAndUpdateSlots(SLOT_INPUT_MAIN);
            syncWorkingState(player);
            return;
        }

        createForgingEffects();
        if (isRecipeComplete(recipe, cap))
        {
            completeRecipe(recipe, input, player);
            setAndUpdateSlots(SLOT_INPUT_MAIN);
            syncWorkingState(player);
            return;
        }

        setAndUpdateSlots(SLOT_INPUT_MAIN);
        syncWorkingState(player);
    }

    public void directWork(EntityPlayer player)
    {
        if (world == null || world.isRemote)
            return;

        ItemStack input = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        IForgeItem cap = input.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap == null)
            return;

        HammerStack hammer = getHammer(player);
        if (hammer.stack.isEmpty())
        {
            sendProblem(player, "no_hammer");
            return;
        }

        AnvilRecipe recipe = getDirectRecipe(input, cap);
        if (recipe == null)
        {
            sendProblem(player, "weld_mismatch");
            return;
        }

        WorkFailure failure = validateDirectWork(cap, recipe);
        if (failure != null)
        {
            failure.send(player);
            return;
        }

        damageHammer(hammer, player);
        directProgress++;
        createForgingEffects();

        if (directProgress >= getRequiredDirectHits(recipe))
        {
            completeRecipe(recipe, input, player);
        }
        else
        {
            markDirectDirty();
        }
    }

    @Nullable
    private AnvilRecipe getDirectRecipe(ItemStack input, IForgeItem cap)
    {
        AnvilRecipe recipe = getDirectRecipeForDisplay();
        if (recipe == null || !recipe.test(input))
            return null;

        boolean recipeChanged = !recipe.getName().equals(cap.getRecipeName());
        if (recipeChanged)
        {
            directProgress = 0;
        }
        lastRecipeName = recipe.getName();
        applyRecipeState(input, cap, recipe);
        return recipe;
    }

    @Nullable
    private WorkFailure validateDirectWork(IForgeItem cap, AnvilRecipe recipe)
    {
        if (getTier() < recipe.getTier())
        {
            return WorkFailure.TIER_TOO_LOW;
        }
        if (!cap.isWorkable())
        {
            return WorkFailure.TOO_COLD;
        }
        return null;
    }

    public boolean tryWeld(EntityPlayer player)
    {
        if (world == null || world.isRemote)
            return false;

        ItemStack main = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        ItemStack secondary = inventory.getStackInSlot(SLOT_INPUT_SECOND);
        ItemStack flux = inventory.getStackInSlot(SLOT_CATALYST);
        HammerStack hammer = getHammer(player);

        WeldStatus status = getWeldStatus(player);
        if (status != WeldStatus.WELDABLE)
        {
            sendProblem(player, status.tooltip);
            return false;
        }

        WeldingRecipe recipe = ModRecipes.WELDING.get(main, secondary, getTier());
        if (recipe == null)
        {
            sendProblem(player, "weld_mismatch");
            return false;
        }
        ItemStack result = recipe.getOutput(main, secondary);
        if (result.isEmpty())
        {
            sendProblem(player, "weld_mismatch");
            return false;
        }

        float weldTemperature = Math.max(getForgeTemperature(main), getForgeTemperature(secondary));
        resetForgeData(result);
        preserveForgeTemperature(result, weldTemperature);
        flux.shrink(1);
        damageHammer(hammer, player);

        inventory.setStackInSlot(SLOT_INPUT_MAIN, result);
        inventory.setStackInSlot(SLOT_INPUT_SECOND, ItemStack.EMPTY);
        inventory.setStackInSlot(SLOT_CATALYST, flux.isEmpty() ? ItemStack.EMPTY : flux);
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        directProgress = 0;
        setAndUpdateSlots(SLOT_INPUT_MAIN);
        return true;
    }

    @Override
    public void onLoad()
    {
        setAndUpdateSlots(0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        lastRecipeName = nbt.hasKey("lastRecipe") ? nbt.getString("lastRecipe") : null;
        directProgress = nbt.getInteger("directProgress");
        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (lastRecipeName != null)
        {
            nbt.setString("lastRecipe", lastRecipeName);
        }
        nbt.setInteger("directProgress", directProgress);
        return super.writeToNBT(nbt);
    }

    @SideOnly(Side.CLIENT)
    public ForgeSteps getSteps()
    {
        return steps;
    }

    @Override
    public int getFieldCount()
    {
        return 8;
    }

    @Override
    public int getField(int ID)
    {
        switch (ID)
        {
            case FIELD_PROGRESS:
            {
                IForgeItem cap = getInputForgeItem();
                return cap == null ? workingProgress : cap.getWork();
            }
            case FIELD_TARGET:
            {
                IForgeItem cap = getInputForgeItem();
                return cap == null || cap.getTarget() < 0 ? workingTarget : cap.getTarget();
            }
            case FIELD_LAST_STEP:
            case FIELD_SECOND_STEP:
            case FIELD_THIRD_STEP:
                return steps.getStepByID(ID);
            case FIELD_FIRST_RULE:
            case FIELD_SECOND_RULE:
            case FIELD_THIRD_RULE:
                if (ID - FIELD_FIRST_RULE >= rules.length)
                    return -1;
                return ForgeRule.getID(rules[ID - FIELD_FIRST_RULE]);
            default:
                TinkersForging.getLog().warn("Invalid field id {}", ID);
                return 0;
        }
    }

    @Override
    public void setField(int ID, int value)
    {
        switch (ID)
        {
            case FIELD_PROGRESS:
                workingProgress = value;
                break;
            case FIELD_TARGET:
                workingTarget = value;
                break;
            case FIELD_LAST_STEP:
            case FIELD_SECOND_STEP:
            case FIELD_THIRD_STEP:
                steps.setStep(ID, value);
                break;
            case FIELD_FIRST_RULE:
            case FIELD_SECOND_RULE:
            case FIELD_THIRD_RULE:
                rules[ID - FIELD_FIRST_RULE] = ForgeRule.valueOf(value);
                break;
            default:
                TinkersForging.getLog().warn("Invalid field id {}", ID);
        }
    }

    @Nullable
    private AnvilRecipe getSelectedRecipe(ItemStack stack, IForgeItem cap, boolean allowDefault)
    {
        AnvilRecipe recipe = ModRecipes.ANVIL.getByName(cap.getRecipeName());
        if (isRecipeValid(recipe, stack))
            return recipe;

        if (isRecipeValid(cachedAnvilRecipe, stack))
            return cachedAnvilRecipe;

        recipe = ModRecipes.ANVIL.getByName(lastRecipeName);
        if (isRecipeValid(recipe, stack) && recipe.getTier() <= getTier())
            return recipe;

        if (allowDefault)
        {
            List<AnvilRecipe> matches = ModRecipes.ANVIL.getAllMatching(stack, getTier());
            if (matches.size() == 1)
                return matches.get(0);
        }
        return null;
    }

    private boolean isRecipeValid(@Nullable AnvilRecipe recipe, ItemStack stack)
    {
        return recipe != null && recipe.test(stack);
    }

    private void applyRecipeState(ItemStack stack, IForgeItem cap, AnvilRecipe recipe)
    {
        boolean forgeRecipeChanged = !recipe.getName().equals(cap.getRecipeName());
        if (!isSameRecipe(cachedAnvilRecipe, recipe))
        {
            updateRecipe(recipe);
        }
        else
        {
            setRecipe(recipe);
        }

        workingProgress = cap.getWork();
        steps = cap.getSteps().copy();
        if (!steps.isWorked() && workingProgress == IForgeItem.MAX_WORK / 2)
        {
            cap.setWork(IForgeItem.DEFAULT_WORK);
            workingProgress = IForgeItem.DEFAULT_WORK;
        }
        if (forgeRecipeChanged || cap.getTarget() < 0)
        {
            cap.setRecipe(recipe);
            cap.setTarget(recipe.getWorkingTarget(world.getSeed()));
        }
        workingTarget = cap.getTarget();
        rules = recipe.getRules();
        inventory.setStackInSlot(SLOT_DISPLAY, recipe.getOutput().copy());
    }

    private boolean isSameRecipe(@Nullable AnvilRecipe first, @Nullable AnvilRecipe second)
    {
        if (first == second)
            return true;
        return first != null && second != null && first.getName().equals(second.getName());
    }

    private boolean isRecipeComplete(AnvilRecipe recipe, IForgeItem cap)
    {
        int targetRange = ModConfig.BALANCE.forgeTargetRange + (5 - recipe.getTier()) * ModConfig.BALANCE.forgeTierRangeMod;
        return Math.abs(cap.getWork() - cap.getTarget()) <= targetRange && recipe.stepsMatch(cap.getSteps());
    }

    private boolean isInitialNegativeStep(IForgeItem cap, ForgeStep step)
    {
        return !cap.getSteps().isWorked() && cap.getWork() <= IForgeItem.MIN_WORK && step.getStepAmount() < 0;
    }

    private void applyForgeStep(IForgeItem cap, ForgeStep step)
    {
        cap.addStep(step);
        steps = cap.getSteps().copy();
        workingProgress = cap.getWork();
    }

    private boolean isOverworked(int work)
    {
        return work < IForgeItem.MIN_WORK || work > IForgeItem.MAX_WORK;
    }

    private void completeRecipe(AnvilRecipe recipe, ItemStack input, EntityPlayer player)
    {
        float inputTemperature = getForgeTemperature(input);
        ItemStack remainingInput = recipe.consumeInput(input);
        resetForgeData(remainingInput);
        preserveForgeTemperature(remainingInput, inputTemperature);
        if (!remainingInput.isEmpty())
        {
            CoreHelpers.dropItemInWorld(world, pos, remainingInput);
        }

        ItemStack output = recipe.getOutput().copy();
        preserveForgeTemperature(output, inputTemperature);
        inventory.setStackInSlot(SLOT_INPUT_MAIN, output);
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        grantForgeExperience(recipe, player);

        resetFields();
        directProgress = 0;
        updateRecipe(null);
        inventory.setStackInSlot(SLOT_DISPLAY, ItemStack.EMPTY);
    }

    private void overworkInput()
    {
        inventory.setStackInSlot(SLOT_INPUT_MAIN, ItemStack.EMPTY);
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.PLAYERS, 0.4f, 1.0f);
        directProgress = 0;
        resetFields();
    }

    private float getForgeTemperature(ItemStack stack)
    {
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        return cap == null ? 0f : cap.getTemperature();
    }

    private void preserveForgeTemperature(ItemStack stack, float temperature)
    {
        if (stack.isEmpty() || temperature <= 0f)
            return;

        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap != null && temperature > cap.getTemperature())
        {
            cap.setTemperature(Math.min(temperature, cap.getMeltingTemperature() - 1f));
        }
    }

    private void resetForgeData(ItemStack stack)
    {
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap != null)
        {
            cap.reset();
        }
    }

    private void grantForgeExperience(AnvilRecipe recipe, EntityPlayer player)
    {
        if (!ModConfig.BALANCE.forgeExperienceEnabled)
            return;

        int xp = (int) (ModConfig.BALANCE.forgeExperienceModifier * (2 + recipe.getTier()));
        while (xp > 0)
        {
            int split = EntityXPOrb.getXPSplit(xp);
            xp -= split;
            world.spawnEntity(new EntityXPOrb(world, player.posX + 0.5d, player.posY, player.posZ + 0.5d, split));
        }
    }

    private void createForgingEffects()
    {
        world.playSound(null, pos, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.4f, 1.0f);
        if (world instanceof WorldServer)
        {
            ((WorldServer) world).spawnParticle(
                EnumParticleTypes.CRIT,
                pos.getX() + 0.2d + world.rand.nextDouble() * 0.6d,
                pos.getY() + 0.8d + world.rand.nextDouble() * 0.2d,
                pos.getZ() + 0.2d + world.rand.nextDouble() * 0.6d,
                5,
                0.0d, 0.0d, 0.0d,
                0.2d
            );
        }
    }

    private void updateRecipe(@Nullable AnvilRecipe recipe)
    {
        // Called on server
        setRecipe(recipe);
        TinkersForging.getNetwork().sendToDimension(new PacketAnvilRecipeUpdate(this), world.provider.getDimension());
    }

    private void syncWorkingState(EntityPlayer player)
    {
        markDirectDirty();
    }

    private void resetFields()
    {
        if (!world.isRemote)
        {
            workingProgress = 0;
            workingTarget = 0;
            steps.reset();
            rules = new ForgeRule[3];
        }
    }

    private void cycleDirectRecipe(EntityPlayer player)
    {
        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT_MAIN);
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap == null)
            return;

        List<AnvilRecipe> matches = ModRecipes.ANVIL.getAllMatching(stack, getTier());
        if (matches.isEmpty())
            return;

        AnvilRecipe current = getDirectRecipeForDisplay();
        int index = current == null ? -1 : matches.indexOf(current);
        AnvilRecipe next = matches.get(index < 0 || index + 1 >= matches.size() ? 0 : index + 1);
        lastRecipeName = next.getName();
        directProgress = 0;
        applyRecipeState(stack, cap, next);
        markDirectDirty();
        player.sendMessage(new TextComponentString("" + TextFormatting.GREEN).appendSibling(new TextComponentTranslation(MOD_ID + ".tooltip.anvil_selected", next.getOutput().getDisplayName())));
    }

    private boolean insertHeldStack(EntityPlayer player, EnumHand hand, int slot)
    {
        ItemStack held = player.getHeldItem(hand);
        ItemStack inSlot = inventory.getStackInSlot(slot);
        if (!canInsertHeldStack(player, hand, slot))
            return false;

        if (inSlot.isEmpty())
        {
            inventory.setStackInSlot(slot, held.copy());
            player.setHeldItem(hand, ItemStack.EMPTY);
            setAndUpdateSlots(slot);
            markDirectDirty();
            return true;
        }

        if (!ItemHandlerHelper.canItemStacksStack(inSlot, held))
            return false;

        int limit = Math.min(inSlot.getMaxStackSize(), inventory.getSlotLimit(slot));
        int move = Math.min(held.getCount(), limit - inSlot.getCount());
        if (move <= 0)
            return false;

        inSlot.grow(move);
        held.shrink(move);
        inventory.setStackInSlot(slot, inSlot);
        player.setHeldItem(hand, held.isEmpty() ? ItemStack.EMPTY : held);
        setAndUpdateSlots(slot);
        markDirectDirty();
        return true;
    }

    private boolean canInsertHeldStack(EntityPlayer player, EnumHand hand, int slot)
    {
        ItemStack held = player.getHeldItem(hand);
        ItemStack inSlot = inventory.getStackInSlot(slot);
        if (!isItemValid(slot, held))
            return false;

        if (inSlot.isEmpty())
            return true;

        if (!ItemHandlerHelper.canItemStacksStack(inSlot, held))
            return false;

        int limit = Math.min(inSlot.getMaxStackSize(), inventory.getSlotLimit(slot));
        return inSlot.getCount() < limit;
    }

    private void extractDirect(EntityPlayer player, boolean secondaryFirst)
    {
        int[] order = secondaryFirst ? new int[] {SLOT_INPUT_SECOND, SLOT_CATALYST, SLOT_INPUT_MAIN, SLOT_HAMMER} : new int[] {SLOT_INPUT_MAIN, SLOT_INPUT_SECOND, SLOT_CATALYST, SLOT_HAMMER};
        for (int slot : order)
        {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty())
            {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                if (!player.inventory.addItemStackToInventory(stack))
                {
                    CoreHelpers.dropItemInWorld(world, pos, stack);
                }
                setAndUpdateSlots(slot);
                markDirectDirty();
                return;
            }
        }
    }

    private int getRequiredDirectHits(AnvilRecipe recipe)
    {
        return Math.max(1, DIRECT_BASE_HITS + DIRECT_HITS_PER_TIER * Math.max(0, recipe.getTier()) + DIRECT_HITS_PER_RULE * recipe.getRules().length);
    }

    private void markDirectDirty()
    {
        if (world == null || world.isRemote)
            return;

        markDirty();
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    private void sendProblem(EntityPlayer player, String translationKey)
    {
        player.sendMessage(new TextComponentString("" + TextFormatting.RED).appendSibling(new TextComponentTranslation(MOD_ID + ".tooltip." + translationKey)));
    }

    private enum WorkFailure
    {
        TIER_TOO_LOW("tier_too_low", false),
        TOO_COLD("too_cold", false),
        CANNOT_MOVE_LOWER("The work cursor cannot move lower.", true);

        private final String message;
        private final boolean literal;

        WorkFailure(String message, boolean literal)
        {
            this.message = message;
            this.literal = literal;
        }

        private void send(EntityPlayer player)
        {
            if (literal)
            {
                player.sendMessage(new TextComponentString("" + TextFormatting.RED + message));
            }
            else
            {
                player.sendMessage(new TextComponentString("" + TextFormatting.RED).appendSibling(new TextComponentTranslation(MOD_ID + ".tooltip." + message)));
            }
        }
    }

    public enum WeldStatus
    {
        PROBLEM_TIER("tier_too_low"),
        PROBLEM_HEAT("too_cold"),
        PROBLEM_FLUX("no_flux"),
        PROBLEM_HAMMER("no_hammer"),
        PROBLEM_INPUTS("weld_no_inputs"),
        PROBLEM_RECIPE("weld_mismatch"),
        WELDABLE("anvil_weld");

        private final String tooltip;

        WeldStatus(String tooltip)
        {
            this.tooltip = tooltip;
        }

        public String getTranslationKey()
        {
            return MOD_ID + ".tooltip." + tooltip;
        }
    }

    private boolean isFlux(ItemStack stack)
    {
        return CoreHelpers.doesStackMatchOre(stack, "flux") || CoreHelpers.doesStackMatchOre(stack, "dustFlux") || CoreHelpers.doesStackMatchOre(stack, "gemBorax");
    }

    private boolean isHammer(ItemStack stack)
    {
        if (stack.isEmpty())
            return false;
        if (CoreHelpers.doesStackMatchOre(stack, "hammer"))
            return true;
        if (stack.getItem().getToolClasses(stack).contains("hammer"))
            return true;

        ResourceLocation name = stack.getItem().getRegistryName();
        if (name == null)
            return false;

        String path = name.getPath();
        return "hammer".equals(path) || path.startsWith("hammer/") || path.endsWith("/hammer") || path.endsWith("_hammer");
    }

    private HammerStack getHammer(@Nullable EntityPlayer player)
    {
        ItemStack anvilHammer = inventory.getStackInSlot(SLOT_HAMMER);
        if (isHammer(anvilHammer))
        {
            return new HammerStack(anvilHammer, true, null);
        }

        if (player != null)
        {
            ItemStack mainHand = player.getHeldItemMainhand();
            if (isHammer(mainHand))
            {
                return new HammerStack(mainHand, false, EnumHand.MAIN_HAND);
            }

            ItemStack offHand = player.getHeldItemOffhand();
            if (isHammer(offHand))
            {
                return new HammerStack(offHand, false, EnumHand.OFF_HAND);
            }
        }
        return new HammerStack(ItemStack.EMPTY, false, null);
    }

    private void damageHammer(HammerStack hammer, EntityPlayer player)
    {
        hammer.stack.damageItem(1, player);
        if (hammer.inAnvil)
        {
            inventory.setStackInSlot(SLOT_HAMMER, hammer.stack.isEmpty() || hammer.stack.getCount() <= 0 ? ItemStack.EMPTY : hammer.stack);
        }
        else if (hammer.hand != null && hammer.stack.getCount() <= 0)
        {
            player.setHeldItem(hammer.hand, ItemStack.EMPTY);
        }
    }

    private static final class HammerStack
    {
        private final ItemStack stack;
        private final boolean inAnvil;
        private final EnumHand hand;

        private HammerStack(ItemStack stack, boolean inAnvil, @Nullable EnumHand hand)
        {
            this.stack = stack;
            this.inAnvil = inAnvil;
            this.hand = hand;
        }
    }

}
