/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.integration.jei;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.alcatrazescapee.tinkersforging.common.blocks.BlockTinkersAnvil;
import com.alcatrazescapee.tinkersforging.common.blocks.ModBlocks;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_ID;
import static com.alcatrazescapee.tinkersforging.TinkersForging.MOD_NAME;
import static com.alcatrazescapee.tinkersforging.integration.jei.JEIIntegration.ANVIL_UID;

@ParametersAreNonnullByDefault
public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipeCategory.Wrapper>
{
    private static final String TRANSLATION_KEY = MOD_ID + ".jei.category.anvil";
    private static final ResourceLocation ICONS_LOCATION = new ResourceLocation(MOD_ID, "textures/gui/jei/icons.png");
    private static final int WIDTH = 98;
    private static final int HEIGHT = 26;

    private final IDrawable background;
    private final IDrawable icon;

    private static IDrawableStatic slot;
    private static IDrawableStatic arrow;
    private static IDrawableAnimated arrowAnimated;

    AnvilRecipeCategory(IGuiHelper guiHelper)
    {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        ItemStack iconStack = getIconStack();
        icon = iconStack.isEmpty() ? guiHelper.createBlankDrawable(16, 16) : guiHelper.createDrawableIngredient(iconStack);
        slot = guiHelper.getSlotDrawable();
        arrow = guiHelper.createDrawable(ICONS_LOCATION, 0, 14, 22, 16);
        arrowAnimated = guiHelper.createAnimatedDrawable(guiHelper.createDrawable(ICONS_LOCATION, 22, 14, 22, 16), 80, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    @Nonnull
    public String getUid()
    {
        return ANVIL_UID;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    public String getTitle()
    {
        return I18n.format(TRANSLATION_KEY);
    }

    @Override
    @Nonnull
    public String getModName()
    {
        return MOD_NAME;
    }

    @Override
    @Nonnull
    public IDrawable getBackground()
    {
        return background;
    }

    @Nullable
    @Override
    public IDrawable getIcon()
    {
        return icon;
    }

    @Nonnull
    private static ItemStack getIconStack()
    {
        for (BlockTinkersAnvil block : BlockTinkersAnvil.getAll())
        {
            if (block.getMaterial().isEnabled())
            {
                return new ItemStack(block);
            }
        }
        return ModBlocks.FORGE == null ? ItemStack.EMPTY : new ItemStack(ModBlocks.FORGE);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, Wrapper recipeWrapper, IIngredients ingredients)
    {
        recipeLayout.getItemStacks().init(0, true, 6, 5);
        recipeLayout.getItemStacks().set(0, ingredients.getInputs(ItemStack.class).get(0));

        recipeLayout.getItemStacks().init(1, false, 76, 5);
        recipeLayout.getItemStacks().set(1, ingredients.getOutputs(ItemStack.class).get(0));
    }

    public static class Wrapper implements IRecipeWrapper
    {
        private final List<List<ItemStack>> inputLists;
        private final ItemStack output;

        public Wrapper(AnvilRecipe recipe)
        {
            inputLists = java.util.Collections.singletonList(recipe.getInput().getStacks());
            output = recipe.getOutput();
        }

        @Override
        public void getIngredients(@Nonnull IIngredients ingredients)
        {
            ingredients.setInputLists(ItemStack.class, inputLists);
            ingredients.setOutput(ItemStack.class, output);
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
        {
            if (slot != null)
            {
                slot.draw(minecraft, 5, 4);
                slot.draw(minecraft, 75, 4);
            }
            if (arrow != null)
            {
                arrow.draw(minecraft, 36, 5);
            }
            if (arrowAnimated != null)
            {
                arrowAnimated.draw(minecraft, 36, 5);
            }
        }
    }
}
