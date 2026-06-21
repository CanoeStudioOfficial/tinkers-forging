/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util.forge;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import static com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil.*;

@ParametersAreNonnullByDefault
public class ForgeSteps implements INBTSerializable<NBTTagCompound>
{
    private final List<ForgeStep> steps;
    private int total;

    public ForgeSteps()
    {
        steps = new ArrayList<>(3);
        reset();
    }

    public void reset()
    {
        steps.clear();
        total = 0;
    }

    public boolean isEmpty()
    {
        return !isWorked();
    }

    public boolean isWorked()
    {
        return total > 0;
    }

    public int getTotal()
    {
        return total;
    }

    public void addStep(@Nullable ForgeStep step)
    {
        if (step == null)
            return;

        if (steps.size() == 3)
        {
            steps.remove(0);
        }
        steps.add(step);
        total++;
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
        int index;
        switch (position)
        {
            case FIELD_LAST_STEP:
                index = 0;
                break;
            case FIELD_SECOND_STEP:
                index = 1;
                break;
            case FIELD_THIRD_STEP:
                index = 2;
                break;
            default:
                return;
        }

        ForgeStep value = ForgeStep.valueOf(step);
        if (value == null)
        {
            while (steps.size() > index)
            {
                steps.remove(steps.size() - 1);
            }
            total = steps.isEmpty() ? 0 : Math.max(total, steps.size());
        }
        else if (index < steps.size())
        {
            steps.set(index, value);
            total = Math.max(total, steps.size());
        }
        else if (index == steps.size())
        {
            steps.add(value);
            total = Math.max(total, steps.size());
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
        nbt.setInteger("total", total);
        return nbt;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound nbt)
    {
        reset();
        if (nbt != null && (nbt.hasKey("last") || nbt.hasKey("second") || nbt.hasKey("third") || nbt.hasKey("total")))
        {
            addDeserializedStep(nbt, "last");
            addDeserializedStep(nbt, "second");
            addDeserializedStep(nbt, "third");
            total = nbt.hasKey("total") ? Math.max(nbt.getInteger("total"), steps.size()) : steps.size();
        }
    }

    @Nullable
    ForgeStep getStep(int idx)
    {
        return idx < 0 || idx >= steps.size() ? null : steps.get(idx);
    }

    @Nullable
    ForgeStep getLastStep()
    {
        return getStepFromEnd(0);
    }

    @Nullable
    ForgeStep getSecondLastStep()
    {
        return getStepFromEnd(1);
    }

    @Nullable
    ForgeStep getThirdLastStep()
    {
        return getStepFromEnd(2);
    }

    private void addDeserializedStep(NBTTagCompound nbt, String key)
    {
        if (!nbt.hasKey(key))
            return;

        ForgeStep step = ForgeStep.valueOf(nbt.getInteger(key));
        if (step != null)
        {
            addStepWithoutCounting(step);
        }
    }

    private void addStepWithoutCounting(ForgeStep step)
    {
        if (steps.size() == 3)
        {
            steps.remove(0);
        }
        steps.add(step);
    }

    @Nullable
    private ForgeStep getStepFromEnd(int offset)
    {
        return getStep(steps.size() - 1 - offset);
    }

    private int getStepInt(int idx)
    {
        ForgeStep step = getStep(idx);
        return step == null ? -1 : step.ordinal();
    }

    public ForgeSteps copy()
    {
        ForgeSteps newSteps = new ForgeSteps();
        newSteps.steps.addAll(this.steps);
        newSteps.total = this.total;
        return newSteps;
    }
}
