/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util.forge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.alcatrazescapee.tinkersforging.util.forge.ForgeStep.*;

public enum ForgeRule
{
    HIT_ANY(Order.ANY, HIT_LIGHT),
    HIT_NOT_LAST(Order.NOT_LAST, HIT_LIGHT),
    HIT_LAST(Order.LAST, HIT_LIGHT),
    HIT_SECOND_LAST(Order.SECOND_LAST, HIT_LIGHT),
    HIT_THIRD_LAST(Order.THIRD_LAST, HIT_LIGHT),
    DRAW_ANY(Order.ANY, DRAW),
    DRAW_LAST(Order.LAST, DRAW),
    DRAW_NOT_LAST(Order.NOT_LAST, DRAW),
    DRAW_SECOND_LAST(Order.SECOND_LAST, DRAW),
    DRAW_THIRD_LAST(Order.THIRD_LAST, DRAW),
    PUNCH_ANY(Order.ANY, PUNCH),
    PUNCH_LAST(Order.LAST, PUNCH),
    PUNCH_NOT_LAST(Order.NOT_LAST, PUNCH),
    PUNCH_SECOND_LAST(Order.SECOND_LAST, PUNCH),
    PUNCH_THIRD_LAST(Order.THIRD_LAST, PUNCH),
    BEND_ANY(Order.ANY, BEND),
    BEND_LAST(Order.LAST, BEND),
    BEND_NOT_LAST(Order.NOT_LAST, BEND),
    BEND_SECOND_LAST(Order.SECOND_LAST, BEND),
    BEND_THIRD_LAST(Order.THIRD_LAST, BEND),
    UPSET_ANY(Order.ANY, UPSET),
    UPSET_LAST(Order.LAST, UPSET),
    UPSET_NOT_LAST(Order.NOT_LAST, UPSET),
    UPSET_SECOND_LAST(Order.SECOND_LAST, UPSET),
    UPSET_THIRD_LAST(Order.THIRD_LAST, UPSET),
    SHRINK_ANY(Order.ANY, SHRINK),
    SHRINK_LAST(Order.LAST, SHRINK),
    SHRINK_NOT_LAST(Order.NOT_LAST, SHRINK),
    SHRINK_SECOND_LAST(Order.SECOND_LAST, SHRINK),
    SHRINK_THIRD_LAST(Order.THIRD_LAST, SHRINK);

    private static final ForgeRule[] values = values();

    public static int getID(@Nullable ForgeRule rule)
    {
        return rule == null ? -1 : rule.ordinal();
    }

    @Nullable
    public static ForgeRule valueOf(int id)
    {
        return id < 0 || id >= values.length ? null : values[id];
    }

    public static boolean isConsistent(ForgeRule... rules)
    {
        if (rules == null || rules.length == 0 || rules.length > 3)
        {
            return false;
        }

        ForgeRule last = null;
        ForgeRule secondLast = null;
        ForgeRule thirdLast = null;
        ForgeRule notLast1 = null;
        ForgeRule notLast2 = null;
        for (ForgeRule rule : rules)
        {
            if (rule == null || rule == last || rule == secondLast || rule == thirdLast || rule == notLast1 || rule == notLast2)
            {
                continue;
            }
            switch (rule.order)
            {
                case THIRD_LAST:
                    if (thirdLast != null)
                    {
                        return false;
                    }
                    thirdLast = rule;
                    break;
                case SECOND_LAST:
                    if (secondLast != null)
                    {
                        return false;
                    }
                    secondLast = rule;
                    break;
                case LAST:
                    if (last != null)
                    {
                        return false;
                    }
                    last = rule;
                    break;
                case NOT_LAST:
                    if (notLast2 != null)
                    {
                        return false;
                    }
                    notLast2 = notLast1;
                    notLast1 = rule;
                    break;
                default:
                    break;
            }
        }
        return conflict3(notLast1, secondLast, thirdLast)
            && conflict3(secondLast, notLast1, notLast2)
            && conflict3(thirdLast, notLast1, notLast2);
    }

    private static boolean conflict3(@Nullable ForgeRule rule1, @Nullable ForgeRule rule2, @Nullable ForgeRule rule3)
    {
        return rule1 == null || rule2 == null || rule3 == null || rule1.type == rule2.type || rule1.type == rule3.type;
    }

    private final int iconU;
    private final int iconV;

    private final Order order;
    private final ForgeStep type;

    ForgeRule(@Nonnull Order order, @Nonnull ForgeStep type)
    {
        this.order = order;
        this.type = type;

        iconU = type.textureU;
        iconV = type.textureV;
    }

    public boolean matches(@Nonnull ForgeSteps steps)
    {
        switch (this.order)
        {
            case ANY:
                return matchesStep(steps.getLastStep()) || matchesStep(steps.getSecondLastStep()) || matchesStep(steps.getThirdLastStep());
            case NOT_LAST:
                return matchesStep(steps.getSecondLastStep()) || matchesStep(steps.getThirdLastStep());
            case LAST:
                return matchesStep(steps.getLastStep());
            case SECOND_LAST:
                return matchesStep(steps.getSecondLastStep());
            case THIRD_LAST:
                return matchesStep(steps.getThirdLastStep());
            default:
                return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public int getIconU()
    {
        return iconU;
    }

    @SideOnly(Side.CLIENT)
    public int getIconV()
    {
        return iconV;
    }

    @SideOnly(Side.CLIENT)
    public int getOutlineU()
    {
        return 198;
    }

    @SideOnly(Side.CLIENT)
    public int getOutlineV()
    {
        return order.textureV;
    }

    private boolean matchesStep(@Nullable ForgeStep step)
    {
        switch (this.type)
        {
            case HIT_LIGHT:
                return step == HIT_LIGHT || step == ForgeStep.HIT_MEDIUM || step == ForgeStep.HIT_HARD;
            default:
                return type == step;
        }
    }

    private enum Order
    {
        ANY(88),
        LAST(0),
        NOT_LAST(66),
        SECOND_LAST(22),
        THIRD_LAST(44);

        private final int textureV;

        Order(int textureV)
        {
            this.textureV = textureV;
        }
    }

}
