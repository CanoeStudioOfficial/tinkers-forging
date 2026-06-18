/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util.forge;

import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.*;

@ParametersAreNonnullByDefault
public class ForgeSteps implements INBTSerializable<NBTTagCompound>
{
    private final LinkedList<ForgeStep> steps;

    public ForgeSteps()
    {
        steps = new LinkedList<>();
        reset();
    }

    public void reset()
    {
        for (int i = 0; i < 3; i++) addStep(null);
    }

    public boolean isEmpty()
    {
        return steps.get(0) == null && steps.get(1) == null && steps.get(2) == null;
    }

    public void addStep(@Nullable ForgeStep step)
    {
        steps.add(step);
        while (steps.size() > 3)
        {
            steps.remove();
        }
    }

    public int getStepByID(int id)
    {
        switch (id)
        {
            case FIELD_LAST_STEP:
                return getStepInt(0);
            case FIELD_SECOND_STEP:
                return getStepInt(1);
            case FIELD_THIRD_STEP:
                return getStepInt(2);
            default:
                return -1;
        }
    }

    public void setStep(int position, int step)
    {
        switch (position)
        {
            case FIELD_LAST_STEP:
                steps.set(0, ForgeStep.valueOf(step));
                break;
            case FIELD_SECOND_STEP:
                steps.set(1, ForgeStep.valueOf(step));
                break;
            case FIELD_THIRD_STEP:
                steps.set(2, ForgeStep.valueOf(step));
                break;
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("last", getStepInt(0));
        nbt.setInteger("second", getStepInt(1));
        nbt.setInteger("third", getStepInt(2));
        return nbt;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound nbt)
    {
        reset();
        if (nbt != null && (nbt.hasKey("last") || nbt.hasKey("second") || nbt.hasKey("third")))
        {
            setStep(FIELD_LAST_STEP, nbt.hasKey("last") ? nbt.getInteger("last") : -1);
            setStep(FIELD_SECOND_STEP, nbt.hasKey("second") ? nbt.getInteger("second") : -1);
            setStep(FIELD_THIRD_STEP, nbt.hasKey("third") ? nbt.getInteger("third") : -1);
        }
    }

    @Nullable
    ForgeStep getStep(int idx)
    {
        return steps.get(idx);
    }

    private int getStepInt(int idx)
    {
        ForgeStep step = steps.get(idx);
        return step == null ? -1 : step.ordinal();
    }

    public ForgeSteps copy()
    {
        ForgeSteps newSteps = new ForgeSteps();
        for (ForgeStep step : this.steps)
            newSteps.addStep(step);
        return newSteps;
    }
}
