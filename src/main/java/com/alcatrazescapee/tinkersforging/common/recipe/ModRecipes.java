/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import com.alcatrazescapee.alcatrazcore.inventory.crafting.InventoryCraftingEmpty;
import com.alcatrazescapee.alcatrazcore.inventory.ingredient.IRecipeIngredient;
import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.alcatrazcore.util.collections.ImmutablePair;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.items.ItemHammer;
import com.alcatrazescapee.tinkersforging.common.items.ItemMetalForm;
import com.alcatrazescapee.tinkersforging.common.items.ItemToolHead;
import com.alcatrazescapee.tinkersforging.integration.AdvToolboxIntegration;
import com.alcatrazescapee.tinkersforging.integration.ModLoaderCompat;
import com.alcatrazescapee.tinkersforging.integration.PatchouliIntegration;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.MetalForm;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeRule;
import com.alcatrazescapee.tinkersforging.util.material.MaterialRegistry;
import com.alcatrazescapee.tinkersforging.util.material.MaterialType;

import static com.alcatrazescapee.alcatrazcore.util.OreDictionaryHelper.UPPER_UNDERSCORE_TO_LOWER_CAMEL;
import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;

@ParametersAreNonnullByDefault
public final class ModRecipes
{
    public static final AnvilRecipeManager ANVIL = new AnvilRecipeManager();
    public static final WeldingRecipeManager WELDING = new WeldingRecipeManager();
    private static final List<Runnable> CT_ACTIONS = new ArrayList<>();

    public static void init()
    {
        // Hammer Head Recipes
        if (ModConfig.isBuiltInToolPartEnabled(ItemType.HAMMER_HEAD))
        {
            for (MaterialType material : MaterialRegistry.getAllMaterials())
            {
                if (material.isEnabled() && ModConfig.isBuiltInToolPartEnabled(ItemType.HAMMER_HEAD, material))
                {
                    ItemStack output = ItemToolHead.get(ItemType.HAMMER_HEAD, material, 1);
                    String inputOre = material.getOreName();

                    if (!output.isEmpty())
                        ANVIL.add(new AnvilRecipe(output, inputOre, ItemType.HAMMER_HEAD.getAmount(), material.getTier(), ItemType.HAMMER_HEAD.getRules()));
                }
            }
        }

        // Other tool part recipes
        if (ModLoaderCompat.shouldRegisterBuiltInToolParts())
        {
            for (ItemType type : ItemType.tools())
            {
                if (!ModConfig.isBuiltInToolPartEnabled(type)) continue;

                for (MaterialType material : MaterialRegistry.getAllMaterials())
                {
                    if (material.isEnabled() && ModConfig.isBuiltInToolPartEnabled(type, material))
                    {
                        // This will always register the default tools anvil recipes, even though the actual tools for modded materials might not exist.
                        final String metalIngotName = material.getOreName();
                        ItemStack output = ItemToolHead.get(type, material, 1);
                        ANVIL.add(new AnvilRecipe(output.copy(), metalIngotName, type.getAmount(), material.getTier(), type.getRules()));
                    }
                }
            }
        }

        // Tinker's Construct Tool Parts
        if (ModLoaderCompat.shouldRegisterTinkersConstructParts())
        {
            for (ItemType type : ItemType.tinkersParts())
            {
                for (MaterialType material : MaterialRegistry.getAllMaterials())
                {
                    if (!MaterialRegistry.isTinkersMaterial(material) || !material.isEnabled()) continue;
                    ItemStack output = getTinkersPartFor(type, material);

                    String inputOre = material.getOreName();

                    if (!output.isEmpty())
                        ANVIL.add(new AnvilRecipe(output, inputOre, type.getAmount(), material.getTier(), type.getRules()));
                }
            }
        }

        // Construct Armory's Armor Parts
        if (Loader.isModLoaded("conarm") && ModConfig.GENERAL.useConstructsArmory)
        {
            for (ItemType type : ItemType.constructArmors())
            {
                for (MaterialType material : MaterialRegistry.getAllMaterials())
                {
                    if (!MaterialRegistry.isTinkersMaterial(material) || !material.isEnabled())
                        continue;

                    ItemStack output = getConstructsArmorFor(type, material);

                    String inputOre = material.getOreName();

                    if (!output.isEmpty())
                        ANVIL.add(new AnvilRecipe(output, inputOre, type.getAmount(), material.getTier(), type.getRules()));
                }
            }
        }

        // Adventurer's Toolbox Tool Parts
        if (Loader.isModLoaded("toolbox"))
        {
            AdvToolboxIntegration.addRecipes();
        }

        // TFC-style welding recipes. These replace the old unrestricted same-NBT merge path.
        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            if (material.isEnabled())
            {
                addTfcMetalFormRecipes(material);

                IRecipeIngredient input = IRecipeIngredient.of(material.getOreName());
                ItemStack output = getDefaultWeldingOutput(material);
                WELDING.add(output.isEmpty() ? new WeldingRecipe(input, input, material.getTier()) : new WeldingRecipe(input, input, output, material.getTier()));
            }
        }
    }

    public static void postInit()
    {
        CT_ACTIONS.forEach(Runnable::run);
    }

    public static void addRecipeAction(Runnable action)
    {
        CT_ACTIONS.add(action);
    }

    private static void addTfcMetalFormRecipes(MaterialType material)
    {
        ItemStack sheet = ItemMetalForm.get(MetalForm.SHEET, material, 1);
        String doubleIngotOre = MetalForm.DOUBLE_INGOT.getOreName(material);
        if (!sheet.isEmpty() && doubleIngotOre != null)
        {
            ANVIL.add(new AnvilRecipe(sheet, doubleIngotOre, 1, material.getTier(), ForgeRule.HIT_THIRD_LAST, ForgeRule.HIT_SECOND_LAST, ForgeRule.HIT_LAST));
        }

        ItemStack doubleSheet = ItemMetalForm.get(MetalForm.DOUBLE_SHEET, material, 1);
        String sheetOre = MetalForm.SHEET.getOreName(material);
        if (!doubleSheet.isEmpty() && sheetOre != null)
        {
            IRecipeIngredient sheetInput = IRecipeIngredient.of(sheetOre);
            WELDING.add(new WeldingRecipe(sheetInput, sheetInput, doubleSheet, material.getTier()));
        }
    }

    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        final IForgeRegistryModifiable<IRecipe> r = (IForgeRegistryModifiable<IRecipe>) event.getRegistry();
        final Collection<IRecipe> recipes = r.getValuesCollection();
        ResourceLocation extendedHammerRecipe = new ResourceLocation(MOD_ID, "extended_hammer");
        r.register(new RecipeExtendedHammer().setRegistryName(extendedHammerRecipe));

        for (MaterialType material : MaterialRegistry.getAllMaterials())
        {
            if (material.isEnabled())
            {
                // Setup
                final String metalIngotName = material.getOreName();
                final String metalBlockName = UPPER_UNDERSCORE_TO_LOWER_CAMEL.convert("BLOCK_" + material.getName());
                if (metalBlockName == null) continue;

                final NonNullList<ItemStack> ingots = OreDictionary.getOres(metalIngotName, false);
                // this is meant to stop things from not registering if the compat exists via other means
                //if (ingots.isEmpty()) continue;

                // Vanilla Armors
                for (ItemType type : ItemType.armors())
                {
                    ImmutablePair<IRecipe, ItemStack> result = getToolRecipeFor(recipes, type, false, ingots);
                    if (result != null)
                    {
                        ItemStack output = result.getValue();

                        // register the anvil recipe
                        ANVIL.add(new AnvilRecipe(output.copy(), metalIngotName, type.getAmount(), material.getTier(), type.getRules()));

                        // un-register the old recipe
                        if (ModConfig.GENERAL.removeCraftingRecipes)
                            r.remove(result.getKey().getRegistryName());
                    }
                }

                // Vanilla Tools
                if (ModLoaderCompat.shouldRegisterBuiltInToolParts())
                {
                    for (ItemType type : ItemType.tools())
                    {
                        if (!ModConfig.isBuiltInToolPartEnabled(type)) continue;
                        if (!ModConfig.isBuiltInToolPartEnabled(type, material)) continue;

                        ImmutablePair<IRecipe, ItemStack> result = getToolRecipeFor(recipes, type, true, ingots);
                        if (result != null)
                        {
                            ItemStack output = ItemToolHead.get(type, material, 1);
                            ResourceLocation loc = new ResourceLocation(MOD_ID, (material.getName() + "_" + type.name()).toLowerCase());

                            // register the tool part recipe
                            r.register(new ShapedOreRecipe(loc, result.getValue(), "H", "S", 'S', "stickWood", 'H', output).setRegistryName(loc));

                            // un-register the old recipe
                            if (ModConfig.GENERAL.removeCraftingRecipes)
                                r.remove(result.getKey().getRegistryName());
                        }
                    }
                }

                // Hammer Head
                final ItemStack hammer = ItemHammer.get(material, 1);
                final ItemStack hammerHead = ItemToolHead.get(ItemType.HAMMER_HEAD, material, 1);
                if (ModConfig.isBuiltInToolPartEnabled(ItemType.HAMMER_HEAD, material) && !hammer.isEmpty() && !hammerHead.isEmpty())
                {
                    ResourceLocation loc = new ResourceLocation(MOD_ID, "hammer_" + material.getName().toLowerCase());
                    r.register(new ShapedOreRecipe(loc, hammer, "H", "S", 'S', "stickWood", 'H', hammerHead).setRegistryName(loc));
                }

                // Anvil recipes
                final ItemStack anvil = BlockTinkersAnvil.get(material, 1);
                if (!anvil.isEmpty())
                {
                    ResourceLocation loc = new ResourceLocation(MOD_ID, "anvil_" + material.getName().toLowerCase());
                    r.register(new ShapedOreRecipe(loc, anvil, "IBI", " I ", "III", 'B', metalBlockName, 'I', metalIngotName).setRegistryName(loc));
                }

                // No Tree Punching Tools
                if (Loader.isModLoaded("notreepunching") && ModConfig.GENERAL.enableNoTreePunchingCompat && MaterialRegistry.isNTPMaterial(material))
                {
                    for (ItemType type : ItemType.ntpTools())
                    {
                        if (!ModConfig.isBuiltInToolPartEnabled(type)) continue;
                        if (!ModConfig.isBuiltInToolPartEnabled(type, material)) continue;

                        // Anvil Recipe
                        ItemStack result = ItemToolHead.get(type, material, 1);
                        ANVIL.add(new AnvilRecipe(result, metalIngotName, type.getAmount(), material.getTier(), type.getRules()));

                        // Crafting Recipe
                        ItemStack tool = getNTPToolFor(type, material);
                        ResourceLocation loc = new ResourceLocation(MOD_ID, "ntp_tool_" + (type.name() + "_" + material.getName()).toLowerCase());
                        r.register(new ShapedOreRecipe(loc, tool, "T", "S", 'T', result, 'S', "stickWood").setRegistryName(loc));
                    }
                }
            }
        }

        // Patchouli Book
        if (Loader.isModLoaded("patchouli"))
        {
            PatchouliIntegration.registerRecipes(event);
        }
    }

    @Nullable
    private static ImmutablePair<IRecipe, ItemStack> getToolRecipeFor(Collection<IRecipe> recipes, ItemType type, boolean isTool, NonNullList<ItemStack> ingots)
    {
        final ItemStack stick = new ItemStack(Items.STICK);
        final InventoryCraftingEmpty tempCrafting = new InventoryCraftingEmpty(3, 3);
        for (ItemStack ingot : ingots)
        {
            if (isTool)
            {
                tempCrafting.setInventorySlotContents(4, stick);
                tempCrafting.setInventorySlotContents(7, stick);
            }
            for (int pos : getIngotPositionsFor(type))
                tempCrafting.setInventorySlotContents(pos, ingot);

            //noinspection ConstantConditions
            final IRecipe recipe = recipes.stream().filter(x -> x.matches(tempCrafting, null)).findFirst().orElse(null);
            if (recipe != null)
            {
                return ImmutablePair.of(recipe, recipe.getCraftingResult(tempCrafting));
            }
        }
        return null;
    }

    @Nonnull
    private static ItemStack getDefaultWeldingOutput(MaterialType material)
    {
        String doubleIngot = getDoubleIngotOreName(material.getOreName());
        if (doubleIngot == null)
        {
            return ItemStack.EMPTY;
        }

        NonNullList<ItemStack> outputs = OreDictionary.getOres(doubleIngot, false);
        if (outputs.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        ItemStack output = outputs.get(0).copy();
        output.setCount(1);
        return output;
    }

    @Nullable
    private static String getDoubleIngotOreName(String oreName)
    {
        return oreName.startsWith("ingot") && oreName.length() > "ingot".length() ? "doubleIngot" + oreName.substring("ingot".length()) : null;
    }

    @Nonnull
    private static ItemStack getTinkersPartFor(ItemType type, MaterialType material)
    {
        final String toolName = type.name().toLowerCase().substring(3);

        final ItemStack stack = CoreHelpers.getStackByRegistryName("tconstruct:" + toolName);
        if (!stack.isEmpty())
        {
            final NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Material", material.getName());
            stack.setTagCompound(nbt);
        }
        return stack;
    }

    @Nonnull
    private static ItemStack getConstructsArmorFor(ItemType type, MaterialType material)
    {
        final String armorName = type.name().toLowerCase().substring(3);

        final ItemStack stack = CoreHelpers.getStackByRegistryName("conarm:" + armorName);
        if (!stack.isEmpty())
        {
            final NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("Material", material.getName());
            stack.setTagCompound(nbt);
        }
        return stack;
    }

    private static ItemStack getNTPToolFor(ItemType type, MaterialType material)
    {
        final String toolName = type.name().substring(4);
        return CoreHelpers.getStackByRegistryName(("notreepunching:" + toolName + "/" + material.getName()).toLowerCase());
    }

    @Nonnull
    private static int[] getIngotPositionsFor(ItemType type)
    {
        /*
         *  Slots:
         *  0 1 2
         *  3 4 5
         *  6 7 8
         */
        switch (type)
        {
            case PICKAXE_HEAD:
                return new int[] {0, 1, 2};
            case SHOVEL_HEAD:
                return new int[] {1};
            case HOE_HEAD:
                return new int[] {1, 2};
            case SWORD_BLADE:
                return new int[] {1, 4};
            case AXE_HEAD:
                return new int[] {1, 2, 5};
            case HELMET:
            case HAMMER_HEAD:
                return new int[] {0, 1, 2, 3, 5};
            case CHESTPLATE:
                return new int[] {0, 2, 3, 4, 5, 6, 7, 8};
            case BOOTS:
                return new int[] {3, 5, 6, 8};
            case LEGGINGS:
                return new int[] {0, 1, 2, 3, 5, 6, 8};
            default:
                return new int[0];
        }
    }
}
