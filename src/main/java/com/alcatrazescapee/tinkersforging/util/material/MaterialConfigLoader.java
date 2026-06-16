package com.alcatrazescapee.tinkersforging.util.material;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.integration.AdvToolboxIntegration;
import com.alcatrazescapee.tinkersforging.integration.TinkersIntegration;

import static com.alcatrazescapee.alcatrazcore.util.OreDictionaryHelper.UPPER_UNDERSCORE_TO_LOWER_CAMEL;

public final class MaterialConfigLoader
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DIRECTORY = "tinkersforging/materials";
    private static final Map<String, Integer> FALLBACK_COLORS = new HashMap<>();
    private static final List<PendingExtendedMaterial> PENDING_EXTENDED_MATERIALS = new ArrayList<>();

    static
    {
        FALLBACK_COLORS.put("aluminum", 0xa36d61);
        FALLBACK_COLORS.put("aluminium", 0xe0e0e0);
        FALLBACK_COLORS.put("ardite", 0x6d2513);
        FALLBACK_COLORS.put("bronze", 0xb87333);
        FALLBACK_COLORS.put("cobalt", 0x114ca0);
        FALLBACK_COLORS.put("constantan", 0x8c4b3e);
        FALLBACK_COLORS.put("copper", 0xcf8665);
        FALLBACK_COLORS.put("dawnstone", 0x9d6f34);
        FALLBACK_COLORS.put("electrum", 0xa89955);
        FALLBACK_COLORS.put("flint", 0x43413e);
        FALLBACK_COLORS.put("gold", 0xfff835);
        FALLBACK_COLORS.put("iron", 0xffffff);
        FALLBACK_COLORS.put("lead", 0x65527f);
        FALLBACK_COLORS.put("manyullyn", 0x693a97);
        FALLBACK_COLORS.put("nickel", 0x757c68);
        FALLBACK_COLORS.put("silver", 0xeff6ff);
        FALLBACK_COLORS.put("soulforged_steel", 0x505050);
        FALLBACK_COLORS.put("steel", 0x808080);
        FALLBACK_COLORS.put("stone", 0x6c6c6c);
        FALLBACK_COLORS.put("thaumium", 0x392f58);
        FALLBACK_COLORS.put("tin", 0x788f95);
        FALLBACK_COLORS.put("void", 0x1f0d3c);
        FALLBACK_COLORS.put("wood", 0x594319);
    }

    public static void load(File configDir)
    {
        PENDING_EXTENDED_MATERIALS.clear();
        File dir = new File(configDir, DIRECTORY);
        if (!dir.exists() && !dir.mkdirs())
        {
            TinkersForging.getLog().warn("Unable to create material config directory {}", dir);
        }

        writeDefaultFiles(dir);
        if (Loader.isModLoaded("tconstruct"))
        {
            TinkersIntegration.writeMaterialConfig(dir);
        }
        if (Loader.isModLoaded("toolbox"))
        {
            AdvToolboxIntegration.writeMaterialConfig(dir);
        }

        for (File file : getJsonFiles(dir))
        {
            loadFile(file);
        }
    }

    public static void registerExtendedMaterials()
    {
        for (PendingExtendedMaterial material : PENDING_EXTENDED_MATERIALS)
        {
            ItemStack sourceStack = material.source.get();
            if (sourceStack.isEmpty())
            {
                TinkersForging.getLog().warn("Unable to register extended parts for material '{}': source item '{}' was not found.", material.id, material.sourceName);
            }
            else
            {
                ExtendedMaterialRegistry.registerItemMaterialWithRecipes(material.id, sourceStack, material.tier, material.workTemperature, material.meltingTemperature);
            }
        }
    }

    public static MaterialDefinition definition(String id, int color, int tier, float workTemp, float meltTemp, boolean noTreePunching)
    {
        MaterialDefinition definition = new MaterialDefinition();
        definition.id = id;
        definition.color = String.format(Locale.ROOT, "#%06x", color & 0xffffff);
        definition.tier = tier;
        definition.workTemperature = workTemp;
        definition.meltingTemperature = meltTemp;
        definition.noTreePunching = noTreePunching;
        definition.replaceExisting = true;
        return definition;
    }

    public static MaterialDefinition definition(String id, String ore, int color, int tier, float workTemp, float meltTemp, boolean tinkers, boolean toolbox, @Nullable String requiredMod)
    {
        return definition(id, ore, color, tier, workTemp, meltTemp, tinkers, toolbox, requiredMod, false);
    }

    public static MaterialDefinition definition(String id, String ore, int color, int tier, float workTemp, float meltTemp, boolean tinkers, boolean toolbox, @Nullable String requiredMod, boolean noTreePunching)
    {
        MaterialDefinition definition = definition(id, color, tier, workTemp, meltTemp, false);
        definition.ore = ore;
        definition.tinkersConstruct = tinkers;
        definition.adventurersToolbox = toolbox;
        definition.requiredMod = requiredMod;
        definition.noTreePunching = noTreePunching;
        return definition;
    }

    public static void writeIfMissing(File file, Object value)
    {
        if (file.exists())
        {
            return;
        }
        Writer writer = null;
        try
        {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs())
            {
                TinkersForging.getLog().warn("Unable to create material config directory {}", parent);
                return;
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8));
            GSON.toJson(value, writer);
        }
        catch (IOException e)
        {
            TinkersForging.getLog().warn("Unable to write material config {}", file, e);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
    }

    private static void writeDefaultFiles(File dir)
    {
        List<MaterialDefinition> builtIn = new ArrayList<>();
        builtIn.add(definition("iron", "ingotIron", 0xffffff, 2, 1350f, 1600f, false, false, null, true));
        builtIn.add(definition("gold", "ingotGold", 0xfff835, 1, 700f, 1100f, false, false, null, true));
        builtIn.add(definition("copper", "ingotCopper", 0xcf8665, 1, 600f, 1000f, false, false, null, true));
        builtIn.add(definition("tin", "ingotTin", 0x788f95, 1, 150f, 300f, false, false, null, true));
        builtIn.add(definition("bronze", "ingotBronze", 0xb87333, 2, 700f, 950f, false, false, null, true));
        builtIn.add(definition("steel", "ingotSteel", 0x808080, 3, 950f, 1350f, false, false, null, true));
        builtIn.add(definition("lead", "ingotLead", 0x65527f, 1, 200f, 350f, false, false, null, false));
        builtIn.add(definition("silver", "ingotSilver", 0xeff6ff, 1, 700f, 950f, false, false, null, false));
        builtIn.add(definition("aluminium", "ingotAluminium", 0xe0e0e0, 1, 450f, 700f, false, false, null, false));
        writeIfMissing(new File(dir, "builtin.json"), builtIn);

        MaterialDefinition example = definition("diamond", "gemDiamond", 0x5eead4, 3, 800f, 1400f, false, false, null, false);
        example.load = false;
        example.replaceExisting = true;
        example.sourceItem = "minecraft:diamond";
        example.comment = "Example only. Set load=true to register this material. anvil=true creates tinkersforging:tinkers_anvil/diamond. replaceExisting=true allows this file to replace an existing material with the same id. sourceItem adds extended Tinkers Forging parts rendered from that item's texture.";
        writeIfMissing(new File(dir, "_example.json"), example);
    }

    private static List<File> getJsonFiles(File dir)
    {
        List<File> files = new ArrayList<>();
        Set<String> reserved = new HashSet<>();
        addReservedFile(dir, files, reserved, "builtin.json");
        addReservedFile(dir, files, reserved, "compat/tconstruct.json");
        addReservedFile(dir, files, reserved, "compat/adventurers_toolbox.json");

        List<File> customFiles = new ArrayList<>();
        collectJsonFiles(dir, customFiles);
        Collections.sort(customFiles, Comparator.comparing(file -> relativePath(dir, file)));
        for (File file : customFiles)
        {
            if (!reserved.contains(normalize(file)))
            {
                files.add(file);
            }
        }
        return files;
    }

    private static void addReservedFile(File dir, List<File> files, Set<String> reserved, String path)
    {
        File file = new File(dir, path);
        reserved.add(normalize(file));
        if (file.isFile())
        {
            files.add(file);
        }
    }

    private static String relativePath(File root, File file)
    {
        String rootPath = normalize(root);
        String filePath = normalize(file);
        return filePath.startsWith(rootPath) ? filePath.substring(rootPath.length()) : filePath;
    }

    private static String normalize(File file)
    {
        return file.getAbsolutePath().replace('\\', '/').toLowerCase(Locale.ROOT);
    }

    private static void collectJsonFiles(File dir, List<File> files)
    {
        File[] children = dir.listFiles();
        if (children == null)
        {
            return;
        }
        for (File child : children)
        {
            if (child.isDirectory())
            {
                collectJsonFiles(child, files);
            }
            else if (child.getName().toLowerCase(Locale.ROOT).endsWith(".json"))
            {
                files.add(child);
            }
        }
    }

    private static void loadFile(File file)
    {
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
            JsonElement json = new JsonParser().parse(reader);
            if (json.isJsonArray())
            {
                JsonArray array = json.getAsJsonArray();
                for (JsonElement element : array)
                {
                    loadElement(file, element);
                }
            }
            else
            {
                loadElement(file, json);
            }
        }
        catch (Exception e)
        {
            TinkersForging.getLog().warn("Unable to load material config {}", file, e);
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private static void loadElement(File file, JsonElement element)
    {
        if (!element.isJsonObject())
        {
            TinkersForging.getLog().warn("Skipping non-object material entry in {}", file);
            return;
        }
        MaterialDefinition definition = GSON.fromJson(element, MaterialDefinition.class);
        if (definition == null || !definition.load)
        {
            return;
        }
        if (definition.requiredMod != null && !definition.requiredMod.trim().isEmpty() && !Loader.isModLoaded(definition.requiredMod.trim()))
        {
            return;
        }
        if (definition.id == null || definition.id.trim().isEmpty())
        {
            TinkersForging.getLog().warn("Skipping material entry with no id in {}", file);
            return;
        }

        String id = cleanId(definition.id);
        MaterialType existing = MaterialRegistry.getMaterial(id);
        if (existing != null && !definition.replaceExisting)
        {
            MaterialRegistry.addMaterialFlags(existing, definition.tinkersConstruct, definition.noTreePunching, definition.adventurersToolbox);
            addPendingExtendedMaterial(definition, id);
            return;
        }

        String ore = definition.ore == null || definition.ore.trim().isEmpty() ? getDefaultOreName(id) : definition.ore.trim();
        int color = parseColor(definition.color);
        if (isLegacyWhiteToolboxMaterial(definition, color))
        {
            color = getFallbackMaterialColor(id, color);
        }
        MaterialType material = new MaterialType(id, ore, () -> OreDictionary.doesOreNameExist(ore), getToolMaterial(id), color, definition.tier, definition.workTemperature, definition.meltingTemperature, definition.anvil);
        if (definition.enabled)
        {
            material.setEnabled();
        }
        MaterialRegistry.addMaterial(material, definition.tinkersConstruct, definition.noTreePunching, definition.adventurersToolbox);
        addPendingExtendedMaterial(definition, id);
    }

    private static void addPendingExtendedMaterial(MaterialDefinition definition, String id)
    {
        if (definition.sourceItem != null && !definition.sourceItem.trim().isEmpty())
        {
            PENDING_EXTENDED_MATERIALS.add(new PendingExtendedMaterial(id, definition.sourceItem, () -> getSourceStack(definition.sourceItem, definition.sourceMeta), definition.tier, definition.workTemperature, definition.meltingTemperature));
        }
    }

    @Nullable
    private static Item.ToolMaterial getToolMaterial(String id)
    {
        if ("iron".equals(id))
        {
            return Item.ToolMaterial.IRON;
        }
        if ("gold".equals(id))
        {
            return Item.ToolMaterial.GOLD;
        }
        return null;
    }

    private static String cleanId(String value)
    {
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++)
        {
            char c = lower.charAt(i);
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

    public static String getDefaultOreName(String id)
    {
        return UPPER_UNDERSCORE_TO_LOWER_CAMEL.convert("INGOT_" + id);
    }

    public static int getFallbackMaterialColor(String id, int fallback)
    {
        Integer color = FALLBACK_COLORS.get(cleanId(id));
        return color == null ? fallback : color;
    }

    private static boolean isLegacyWhiteToolboxMaterial(MaterialDefinition definition, int color)
    {
        return color == 0xffffff && definition.adventurersToolbox && definition.requiredMod != null && "toolbox".equals(definition.requiredMod.trim());
    }

    private static int parseColor(String value)
    {
        if (value == null || value.trim().isEmpty())
        {
            return 0xffffff;
        }
        String text = value.trim().toLowerCase(Locale.ROOT);
        boolean hex = false;
        if (text.startsWith("#"))
        {
            text = text.substring(1);
            hex = true;
        }
        if (text.startsWith("0x"))
        {
            text = text.substring(2);
            hex = true;
        }
        try
        {
            return Integer.parseInt(text, hex || !text.matches("[0-9]+") ? 16 : 10);
        }
        catch (NumberFormatException e)
        {
            TinkersForging.getLog().warn("Invalid material color '{}', using white.", value);
            return 0xffffff;
        }
    }

    private static ItemStack getSourceStack(String itemName, int meta)
    {
        try
        {
            ResourceLocation location = new ResourceLocation(itemName.trim());
            Item item = ForgeRegistries.ITEMS.getValue(location);
            return item == null ? ItemStack.EMPTY : new ItemStack(item, 1, meta);
        }
        catch (RuntimeException e)
        {
            TinkersForging.getLog().warn("Invalid source item '{}' in material config.", itemName);
            return ItemStack.EMPTY;
        }
    }

    public static final class MaterialDefinition
    {
        public String comment;
        public boolean load = true;
        public String id;
        public String ore;
        public String color = "#ffffff";
        public int tier;
        public float workTemperature;
        public float meltingTemperature;
        public boolean replaceExisting = false;
        public boolean anvil = true;
        public boolean enabled = false;
        public boolean noTreePunching = false;
        public boolean tinkersConstruct = false;
        public boolean adventurersToolbox = false;
        public String requiredMod;
        public String sourceItem;
        public int sourceMeta = 0;
    }

    private static final class PendingExtendedMaterial
    {
        private final String id;
        private final String sourceName;
        private final Supplier<ItemStack> source;
        private final int tier;
        private final float workTemperature;
        private final float meltingTemperature;

        private PendingExtendedMaterial(String id, String sourceName, Supplier<ItemStack> source, int tier, float workTemperature, float meltingTemperature)
        {
            this.id = id;
            this.sourceName = sourceName;
            this.source = source;
            this.tier = tier;
            this.workTemperature = workTemperature;
            this.meltingTemperature = meltingTemperature;
        }
    }

    private MaterialConfigLoader() {}
}
