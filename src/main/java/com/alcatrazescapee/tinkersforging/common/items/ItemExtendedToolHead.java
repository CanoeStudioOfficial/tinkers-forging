/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.items;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.item.ItemCore;
import com.alcatrazescapee.tinkersforging.client.model.ForgingMaterialModelRegister;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.ExtendedMaterialRegistry.Definition;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@ParametersAreNonnullByDefault
public class ItemExtendedToolHead extends ItemCore
{
    private static final Map<ItemType, ItemExtendedToolHead> TABLE = new EnumMap<>(ItemType.class);

    @Nullable
    public static ItemExtendedToolHead get(ItemType type)
    {
        return TABLE.get(type);
    }

    @Nonnull
    public static Collection<ItemExtendedToolHead> getAll()
    {
        return TABLE.values();
    }

    @Nonnull
    public static ItemStack get(ItemType type, Definition material, int amount)
    {
        ItemExtendedToolHead item = get(type);
        if (item == null)
        {
            return ItemStack.EMPTY;
        }
        return ExtendedMaterialRegistry.setMaterial(new ItemStack(item, amount), material);
    }

    private final ItemType type;

    public ItemExtendedToolHead(ItemType type)
    {
        this.type = type;
        TABLE.put(type, this);
    }

    @Nonnull
    public ItemType getType()
    {
        return type;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        Definition material = ExtendedMaterialRegistry.get(stack);
        String partName = super.getItemStackDisplayName(stack);
        return material == null ? partName : material.getDisplayName() + " " + partName;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (isInCreativeTab(tab))
        {
            for (Definition material : ExtendedMaterialRegistry.getAll())
            {
                items.add(get(type, material, 1));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel()
    {
        ForgingMaterialModelRegister.register(this, type.name().toLowerCase());
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
