/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.items;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.client.model.ForgingMaterialModelRegister;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry.Definition;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@ParametersAreNonnullByDefault
public class ItemExtendedHammer extends ItemHammer
{
    @Nullable
    private static ItemExtendedHammer item;

    @Nullable
    public static ItemExtendedHammer getItem()
    {
        return item;
    }

    @Nonnull
    public static ItemStack get(Definition material, int amount)
    {
        ItemExtendedHammer hammer = getItem();
        if (hammer == null)
        {
            return ItemStack.EMPTY;
        }
        return ExtendedMaterialRegistry.setMaterial(new ItemStack(hammer, amount), material);
    }

    public ItemExtendedHammer()
    {
        super(Item.ToolMaterial.IRON);
        item = this;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        Definition material = ExtendedMaterialRegistry.get(stack);
        String itemName = super.getItemStackDisplayName(stack);
        return material == null ? itemName : material.getDisplayName() + " " + itemName;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
        {
            for (Definition material : ExtendedMaterialRegistry.getAll())
            {
                items.add(get(material, 1));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel()
    {
        ForgingMaterialModelRegister.register(this, "hammer");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        Definition material = ExtendedMaterialRegistry.get(stack);
        if (material != null)
        {
            tooltip.add(TextFormatting.DARK_GREEN + I18n.format(MOD_ID + ".tooltip.material", material.getDisplayName()));
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
