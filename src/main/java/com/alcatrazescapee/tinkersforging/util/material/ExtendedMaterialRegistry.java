/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.util.material;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;

import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.items.ItemExtendedToolHead;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.common.recipe.ModRecipes;
import com.alcatrazescapee.tinkersforging.util.ItemType;

@ParametersAreNonnullByDefault
public final class ExtendedMaterialRegistry
{
    public static final String TAG_MATERIAL = "TFExtendedMaterial";

    private static final Map<String, Definition> MATERIALS = new LinkedHashMap<>();

    @Nullable
    public static Definition registerItemMaterial(String name, ItemStack sourceStack, int tier, float workTemp, float meltTemp)
    {
        if (sourceStack.isEmpty() || sourceStack.getItem().getRegistryName() == null)
        {
            TinkersForging.getLog().warn("Unable to register extended material '{}' from an empty or unregistered stack.", name);
            return null;
        }

        String id = cleanName(name);
        if (id.isEmpty())
        {
            id = getDefaultName(sourceStack);
        }

        Definition definition = new Definition(id, sourceStack, tier, workTemp, meltTemp);
        if (MATERIALS.containsKey(id))
        {
            TinkersForging.getLog().debug("Extended material {} was overridden!", id);
        }
        MATERIALS.put(id, definition);
        return definition;
    }

    public static void registerItemMaterialWithRecipes(String name, ItemStack sourceStack, int tier, float workTemp, float meltTemp)
    {
        Definition material = registerItemMaterial(name, sourceStack, tier, workTemp, meltTemp);
        if (material != null)
        {
            CapabilityForgeItem.registerStackCapability(IRecipeIngredient.of(material.getSourceStack()), material.getWorkTemp(), material.getMeltTemp());
            ModRecipes.addRecipeAction(() -> addExtendedMaterialRecipes(material));
            TinkersForging.getProxy().onExtendedMaterialsChanged();
        }
    }

    @Nonnull
    public static String getDefaultName(ItemStack sourceStack)
    {
        ResourceLocation name = sourceStack.getItem().getRegistryName();
        if (name == null)
        {
            return "unknown";
        }
        String value = name.getNamespace() + "_" + name.getPath();
        if (sourceStack.getMetadata() != 0)
        {
            value += "_" + sourceStack.getMetadata();
        }
        return cleanName(value);
    }

    @Nonnull
    public static Collection<Definition> getAll()
    {
        return Collections.unmodifiableCollection(MATERIALS.values());
    }

    @Nullable
    public static Definition get(String id)
    {
        return MATERIALS.get(id);
    }

    @Nullable
    public static Definition get(ItemStack stack)
    {
        String id = getMaterialId(stack);
        return id == null ? null : get(id);
    }

    @Nullable
    public static String getMaterialId(ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTagCompound())
        {
            return null;
        }
        String id = stack.getTagCompound().getString(TAG_MATERIAL);
        return id.isEmpty() ? null : id;
    }

    @Nonnull
    public static ItemStack setMaterial(ItemStack stack, Definition material)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
        {
            tag = new NBTTagCompound();
        }
        tag.setString(TAG_MATERIAL, material.getId());
        stack.setTagCompound(tag);
        return stack;
    }

    @Nonnull
    private static String cleanName(String name)
    {
        String value = name.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.')
            {
                builder.append(c);
            }
            else
            {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private static void addExtendedMaterialRecipes(Definition material)
    {
        addExtendedMaterialRecipe(ItemType.HAMMER_HEAD, material);
        for (ItemType type : ItemType.tools())
        {
            addExtendedMaterialRecipe(type, material);
        }
        if (Loader.isModLoaded("notreepunching") && ModConfig.GENERAL.enableNoTreePunchingCompat)
        {
            for (ItemType type : ItemType.ntpTools())
            {
                addExtendedMaterialRecipe(type, material);
            }
        }
    }

    private static void addExtendedMaterialRecipe(ItemType type, Definition material)
    {
        ItemStack output = ItemExtendedToolHead.get(type, material, 1);
        ItemStack input = material.getSourceStack(type.getAmount());
        if (!output.isEmpty() && !input.isEmpty())
        {
            ModRecipes.ANVIL.add(new AnvilRecipe(output, input, material.getTier(), type.getRules()));
        }
    }

    @ParametersAreNonnullByDefault
    public static final class Definition
    {
        private final String id;
        private final ItemStack sourceStack;
        private final String displayName;
        private final int tier;
        private final float workTemp;
        private final float meltTemp;

        private Definition(String id, ItemStack sourceStack, int tier, float workTemp, float meltTemp)
        {
            this.id = id;
            this.sourceStack = sourceStack.copy();
            this.sourceStack.setCount(1);
            this.displayName = sourceStack.getDisplayName();
            this.tier = MathHelper.clamp(tier, 0, 5);
            this.workTemp = MathHelper.clamp(workTemp, 100f, 1400f);
            this.meltTemp = Math.max(this.workTemp + 100f, meltTemp);
        }

        @Nonnull
        public String getId()
        {
            return id;
        }

        @Nonnull
        public String getDisplayName()
        {
            return displayName;
        }

        @Nonnull
        public ItemStack getSourceStack()
        {
            return getSourceStack(1);
        }

        @Nonnull
        public ItemStack getSourceStack(int amount)
        {
            ItemStack stack = sourceStack.copy();
            stack.setCount(amount);
            return stack;
        }

        public int getTier()
        {
            return tier;
        }

        public float getWorkTemp()
        {
            return workTemp;
        }

        public float getMeltTemp()
        {
            return meltTemp;
        }
    }

    private ExtendedMaterialRegistry() {}
}
