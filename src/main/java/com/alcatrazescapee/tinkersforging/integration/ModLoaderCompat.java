/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration;

import net.minecraftforge.fml.common.Loader;

import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.ModConfig.TinkersConstructCompatMode;

public final class ModLoaderCompat
{
    public static boolean isTinkersConstructLoaded()
    {
        return Loader.isModLoaded("tconstruct");
    }

    public static boolean shouldRegisterBuiltInToolParts()
    {
        return !isTinkersConstructLoaded() || ModConfig.GENERAL.tinkersConstructCompatMode == TinkersConstructCompatMode.BOTH;
    }

    public static boolean shouldRegisterTinkersConstructParts()
    {
        return isTinkersConstructLoaded();
    }

    public static boolean shouldUseTinkersConstructMaterialRenderInfo()
    {
        return isTinkersConstructLoaded() && ModConfig.GENERAL.useTinkersConstructMaterialRenderInfo;
    }

    private ModLoaderCompat() {}
}
