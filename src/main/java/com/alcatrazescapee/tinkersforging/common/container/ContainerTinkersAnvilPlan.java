/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.container;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import com.alcatrazescapee.alcatrazcore.inventory.container.ContainerTileInventory;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.gui.ModGuiHandler;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;

@ParametersAreNonnullByDefault
public class ContainerTinkersAnvilPlan extends ContainerTileInventory<TileTinkersAnvil>
{
    private final EntityPlayer player;

    public ContainerTinkersAnvilPlan(EntityPlayer player, TileTinkersAnvil tile)
    {
        super(player.inventory, tile, 0, 0);
        this.player = player;
        tile.setCurrentPlayer(player);
    }

    public void onReceiveAction(int actionId)
    {
        // Recipe selection is sent by recipe name, mirroring the newer anvil's recipe-id path.
    }

    public void onSelectRecipe(String recipeName)
    {
        tile.selectPlan(recipeName);
        openAnvilGui();
    }

    private void openAnvilGui()
    {
        player.openGui(TinkersForging.getInstance(), ModGuiHandler.TINKERS_ANVIL, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
    }

    @Override
    protected void addContainerSlots()
    {
        // Plan screen only shows buttons; it has no tile slots of its own.
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        if (tile == null || tile.getWorld() == null || tile.isInvalid())
            return false;

        BlockPos pos = tile.getPos();
        return entityPlayer.world == tile.getWorld()
                && tile.getWorld().getTileEntity(pos) == tile
                && entityPlayer.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }
}
