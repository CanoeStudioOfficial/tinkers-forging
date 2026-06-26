/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration.top;

import java.io.IOException;
import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.common.recipe.WeldingRecipe;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.client.ElementTextRender;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@SuppressWarnings("unused")
public final class TOPIntegration
{
    private static int textLocalizedElement;

    public static final class Callback implements Function<ITheOneProbe, Void>
    {
        @Override
        public Void apply(ITheOneProbe top)
        {
            textLocalizedElement = top.registerElementFactory(ElementTextLocalized::new);
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
            IProbeInfo row = probeInfo.horizontal().item(input);
            if (recipe.hasSecondaryInput())
            {
                row.item(secondary);
            }
            row.progress(progress, maxProgress, new ProgressStyle().height(18).width(64).showText(false))
                .item(output);
            probeInfo.element(new ElementTextLocalized(MOD_ID + ".top.anvil_recipe", output));
            probeInfo.element(new ElementTextLocalized(MOD_ID + ".top.anvil_hits", progress, maxProgress));
        }
    }

    private static final class ElementTextLocalized implements IElement
    {
        private static final byte TYPE_STRING = 0;
        private static final byte TYPE_INT = 1;
        private static final byte TYPE_ITEM_STACK = 2;

        private String translationKey;
        private Object[] args;
        private String text;

        private ElementTextLocalized(String translationKey, Object... args)
        {
            this.translationKey = translationKey;
            this.args = args;
        }

        private ElementTextLocalized(ByteBuf buffer)
        {
            fromBytes(buffer);
            text = I18n.format(translationKey, args);
        }

        @Override
        public void render(int x, int y)
        {
            ElementTextRender.render(getText(), x, y);
        }

        @Override
        public int getWidth()
        {
            return ElementTextRender.getWidth(getText());
        }

        @Override
        public int getHeight()
        {
            return 10;
        }

        @Override
        public void toBytes(ByteBuf buffer)
        {
            PacketBuffer packet = new PacketBuffer(buffer);
            packet.writeInt(translationKey.length());
            packet.writeString(translationKey);
            packet.writeInt(args == null ? 0 : args.length);
            if (args == null)
                return;

            for (Object arg : args)
            {
                if (arg instanceof String)
                {
                    String value = (String) arg;
                    packet.writeByte(TYPE_STRING);
                    packet.writeInt(value.length());
                    packet.writeString(value);
                }
                else if (arg instanceof Integer)
                {
                    packet.writeByte(TYPE_INT);
                    packet.writeInt((Integer) arg);
                }
                else if (arg instanceof ItemStack)
                {
                    packet.writeByte(TYPE_ITEM_STACK);
                    packet.writeItemStack((ItemStack) arg);
                }
                else
                {
                    throw new IllegalArgumentException("Unsupported TOP localized text arg: " + arg.getClass());
                }
            }
        }

        @Override
        public int getID()
        {
            return textLocalizedElement;
        }

        private void fromBytes(ByteBuf buffer)
        {
            PacketBuffer packet = new PacketBuffer(buffer);
            translationKey = packet.readString(packet.readInt());
            int length = packet.readInt();
            args = new Object[length];
            for (int i = 0; i < length; i++)
            {
                byte type = packet.readByte();
                if (type == TYPE_STRING)
                {
                    args[i] = packet.readString(packet.readInt());
                }
                else if (type == TYPE_INT)
                {
                    args[i] = packet.readInt();
                }
                else if (type == TYPE_ITEM_STACK)
                {
                    try
                    {
                        args[i] = packet.readItemStack().getDisplayName();
                    }
                    catch (IOException e)
                    {
                        args[i] = "ERROR";
                    }
                }
                else
                {
                    throw new IllegalArgumentException("Unsupported TOP localized text arg type: " + type);
                }
            }
        }

        private String getText()
        {
            if (text == null)
            {
                text = I18n.format(translationKey, args);
            }
            return text;
        }
    }
}
