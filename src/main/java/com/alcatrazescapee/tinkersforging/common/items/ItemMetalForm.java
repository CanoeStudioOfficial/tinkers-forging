/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.items;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.alcatrazcore.item.ItemCore;
import com.alcatrazescapee.tinkersforging.client.model.ForgingMaterialModelRegister;
import com.alcatrazescapee.tinkersforging.util.MetalForm;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@ParametersAreNonnullByDefault
public class ItemMetalForm extends ItemCore
{
    private static final Map<MetalForm, Map<MaterialType, ItemMetalForm>> TABLE = new EnumMap<>(MetalForm.class);

    @Nullable
    public static ItemMetalForm get(MetalForm form, MaterialType material)
    {
        return TABLE.containsKey(form) ? TABLE.get(form).get(material) : null;
    }

    @Nonnull
    public static Collection<ItemMetalForm> getAll()
    {
        return TABLE.values().stream().map(Map::values).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Nonnull
    public static ItemStack get(MetalForm form, MaterialType material, int amount)
    {
        ItemMetalForm item = get(form, material);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, amount);
    }

    private final MetalForm form;
    private final MaterialType material;

    public ItemMetalForm(MetalForm form, MaterialType material)
    {
        this.form = form;
        this.material = material;

        if (!TABLE.containsKey(form))
        {
            TABLE.put(form, new HashMap<>());
        }
        TABLE.get(form).put(material, this);
    }

    @Nonnull
    public MetalForm getForm()
    {
        return form;
    }

    @Nonnull
    public MaterialType getMaterial()
    {
        return material;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel()
    {
        ForgingMaterialModelRegister.register(this, form.getRegistryName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add(TextFormatting.DARK_GREEN + I18n.format(MOD_ID + ".tooltip.material", I18n.format("material." + material.getName() + ".name")));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
