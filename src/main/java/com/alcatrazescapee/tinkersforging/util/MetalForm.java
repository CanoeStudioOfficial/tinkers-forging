/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.alcatrazcore.util.OreDictionaryHelper.UPPER_UNDERSCORE_TO_LOWER_CAMEL;

public enum MetalForm
{
    DOUBLE_INGOT("double_ingot", "DOUBLE_INGOT_"),
    SHEET("sheet", "SHEET_"),
    DOUBLE_SHEET("double_sheet", "DOUBLE_SHEET_");

    private final String registryName;
    private final String orePrefix;

    MetalForm(String registryName, String orePrefix)
    {
        this.registryName = registryName;
        this.orePrefix = orePrefix;
    }

    @Nonnull
    public String getRegistryName()
    {
        return registryName;
    }

    @Nonnull
    public String getTranslationKey()
    {
        return registryName;
    }

    @Nullable
    public String getOreName(MaterialType material)
    {
        return UPPER_UNDERSCORE_TO_LOWER_CAMEL.convert(orePrefix + material.getName());
    }
}
