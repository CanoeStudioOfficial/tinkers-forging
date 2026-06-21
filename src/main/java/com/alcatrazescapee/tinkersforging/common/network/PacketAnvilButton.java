/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.network;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.alcatrazescapee.alcatrazcore.AlcatrazCore;
import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.container.ContainerTinkersAnvilPlan;
import io.netty.buffer.ByteBuf;

public class PacketAnvilButton implements IMessage
{
    private int buttonId;
    @Nullable private String recipeName;

    @SuppressWarnings("unused")
    public PacketAnvilButton() {}

    public PacketAnvilButton(int buttonId)
    {
        this.buttonId = buttonId;
        this.recipeName = null;
    }

    public PacketAnvilButton(String recipeName)
    {
        this.buttonId = ContainerTinkersAnvil.ACTION_PLAN_SELECT_BASE;
        this.recipeName = recipeName;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        buttonId = buf.readInt();
        recipeName = buf.readBoolean() ? ByteBufUtils.readUTF8String(buf) : null;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(buttonId);
        buf.writeBoolean(recipeName != null);
        if (recipeName != null)
        {
            ByteBufUtils.writeUTF8String(buf, recipeName);
        }
    }

    public static class Handler implements IMessageHandler<PacketAnvilButton, IMessage>
    {
        @Override
        public IMessage onMessage(PacketAnvilButton message, MessageContext ctx)
        {
            EntityPlayer player = AlcatrazCore.getProxy().getPlayer(ctx);
            AlcatrazCore.getProxy().getThreadListener(ctx).addScheduledTask(() -> {
                Container container = player.openContainer;
                if (container instanceof ContainerTinkersAnvilPlan && message.recipeName != null)
                {
                    ((ContainerTinkersAnvilPlan) container).onSelectRecipe(message.recipeName);
                }
                else if (container instanceof ContainerTinkersAnvil)
                {
                    ((ContainerTinkersAnvil) container).onReceiveAction(message.buttonId);
                }
                else if (container instanceof ContainerTinkersAnvilPlan)
                {
                    ((ContainerTinkersAnvilPlan) container).onReceiveAction(message.buttonId);
                }
            });
            return null;
        }
    }
}
