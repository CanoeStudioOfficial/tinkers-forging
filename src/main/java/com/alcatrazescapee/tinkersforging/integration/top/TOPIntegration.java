/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration.top;

import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.recipe.WeldingRecipe;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SuppressWarnings("unused")
public final class TOPIntegration
{
    public static final class Callback implements Function<ITheOneProbe, Void>
    {
        @Override
        public Void apply(ITheOneProbe top)
        {
            top.registerProvider(new TinkersAnvilProvider());
            return null;
        }
    }

    private static final class TinkersAnvilProvider implements IProbeInfoProvider
    {
        @Override
        public String getID()
        {
            return MOD_ID + ":tinkers_anvil";
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data)
        {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof TileTinkersAnvil))
                return;

            TileTinkersAnvil tile = (TileTinkersAnvil) tileEntity;
            ItemStack input = tile.getInputStack();
            if (input.isEmpty())
                return;

            ItemStack secondary = tile.getSecondaryInputStack();
            if (!secondary.isEmpty())
            {
                WeldingRecipe weldingRecipe = ModRecipes.WELDING.getForInputs(input, secondary);
                if (weldingRecipe != null)
                {
                    ItemStack output = weldingRecipe.getDisplayOutput();
                    if (!output.isEmpty())
                    {
                        probeInfo.horizontal().item(input).item(secondary).item(output);
                        return;
                    }
                }
            }

            AnvilRecipe recipe = tile.getDirectRecipeForDisplay();
            if (recipe == null)
            {
                probeInfo.item(input);
                return;
            }

            ItemStack output = recipe.getOutput();
            int maxProgress = tile.getDirectMaxProgress();
            int progress = Math.min(tile.getDirectProgress(), maxProgress);
            probeInfo.horizontal()
                .item(input)
                .progress(progress, maxProgress, new ProgressStyle().height(18).width(64).showText(false))
                .item(output);
        }
    }
}
