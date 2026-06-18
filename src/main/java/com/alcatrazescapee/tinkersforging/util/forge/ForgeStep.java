/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util.forge;

import javax.annotation.Nullable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum ForgeStep
{
    HIT_LIGHT(-3, 53, 56, 64, 240),
    HIT_MEDIUM(-6, 71, 56, 80, 240),
    HIT_HARD(-9, 53, 74, 96, 240),
    DRAW(-15, 71, 74, 112, 240),
    PUNCH(2, 89, 56, 0, 240),
    BEND(7, 107, 56, 16, 240),
    UPSET(13, 89, 74, 32, 240),
    SHRINK(16, 107, 74, 48, 240);

    private static final ForgeStep[] values = values();

    @Nullable
    public static ForgeStep valueOf(int id)
    {
        return id >= 0 && id < values.length ? values[id] : null;
    }

    final int textureU;
    final int textureV;

    private final int stepAmount;
    private final int xPos;
    private final int yPos;

    ForgeStep(int stepAmount, int xPos, int yPos, int textureU, int textureV)
    {
        this.stepAmount = stepAmount;
        this.xPos = xPos;
        this.yPos = yPos;
        this.textureU = textureU;
        this.textureV = textureV;
    }

    public int getStepAmount()
    {
        return stepAmount;
    }

    @SideOnly(Side.CLIENT)
    public int getX()
    {
        return xPos;
    }

    @SideOnly(Side.CLIENT)
    public int getY()
    {
        return yPos;
    }

    @SideOnly(Side.CLIENT)
    public int getTexU()
    {
        return textureU;
    }

    @SideOnly(Side.CLIENT)
    public int getTexV()
    {
        return textureV;
    }
}
