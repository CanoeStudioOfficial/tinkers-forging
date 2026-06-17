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

        File builtInFile = new File(dir, "builtin.json");
        for (File file : getJsonFiles(dir))
        {
            if (!loadFile(file) && normalize(file).equals(normalize(builtInFile)))
            {
                TinkersForging.getLog().warn("Falling back to internal built-in material definitions because {} could not be parsed.", file);
                loadDefinitions(file, getBuiltInDefinitions());
            }
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
        writeIfMissing(new File(dir, "builtin.json"), getBuiltInDefinitions());

        MaterialDefinition example = definition("diamond", "gemDiamond", 0x5eead4, 3, 800f, 1400f, false, false, null, false);
        example.load = false;
        example.replaceExisting = true;
        example.sourceItem = "minecraft:diamond";
        example.comment = "Example only. JSON does not support // comments, so the comments object below explains each field. The loader ignores comments.";
        example.comments = getExampleComments();
        writeIfMissing(new File(dir, "_example.json"), example);
    }

    private static List<MaterialDefinition> getBuiltInDefinitions()
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
        return builtIn;
    }

    private static Map<String, String> getExampleComments()
    {
        Map<String, String> comments = new HashMap<>();
        comments.put("comment", "Human-readable note. This field is ignored by Tinkers Forging.");
        comments.put("comments", "Human-readable field guide. This whole object is ignored by Tinkers Forging.");
        comments.put("load", "Set true to load this entry. Set false to keep it as a disabled example/template.");
        comments.put("id", "Material id used internally and in generated registry names. Example: id diamond creates tinkersforging:tinkers_anvil/diamond when anvil is true, plus material hammer/hammer-head items where applicable. Use lowercase letters, numbers, underscores, hyphens, or dots.");
        comments.put("ore", "Ore dictionary input used for normal material recipes and heat checks. If omitted, Tinkers Forging guesses ingot + material id, for example ingotCopper.");
        comments.put("color", "Fallback tint color used when no custom material texture is found. Accepts #RRGGBB, 0xRRGGBB, or decimal integer.");
        comments.put("tier", "Tool/anvil tier from 0 to 5. Higher tier anvils can work higher tier parts when Respect Tiers is enabled.");
        comments.put("workTemperature", "Temperature in Celsius where this material becomes workable on the forge/anvil.");
        comments.put("meltingTemperature", "Temperature in Celsius where this material melts or becomes too hot. It should be higher than workTemperature.");
        comments.put("replaceExisting", "If true, this entry replaces an already loaded material with the same id. If false, existing material stats stay unchanged and only compatibility flags/sourceItem are added.");
        comments.put("anvil", "If true, registers a Tinker's Anvil block for this material at tinkersforging:tinkers_anvil/<id>.");
        comments.put("enabled", "If true, forces this material to appear/register recipes even when the ore dictionary precondition is not currently found.");
        comments.put("noTreePunching", "If true and No Tree Punching compat is enabled, this material can generate No Tree Punching tool part recipes.");
        comments.put("tinkersConstruct", "If true and Tinkers Construct is installed/enabled in config, this material can generate Tinkers Construct part recipes. When Tinkers Construct compat is enabled, Tinkers Forging's own pickaxe_head/axe_head/shovel_head/hoe_head/sword_blade items are intentionally not registered; use the tconstruct part item ids instead.");
        comments.put("adventurersToolbox", "If true and Adventurer's Toolbox is installed, this material can generate Adventurer's Toolbox part recipes.");
        comments.put("requiredMod", "Optional mod id gate. If set, this material entry only loads when that mod is installed, for example tconstruct or toolbox.");
        comments.put("sourceItem", "Optional item registry name used to register extended Tinkers Forging parts and an extended hammer rendered from that item's model texture, for example minecraft:diamond.");
        comments.put("sourceMeta", "Metadata/damage value for sourceItem. Usually 0; use another value for old 1.12 metadata items.");
        return comments;
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

    private static boolean loadFile(File file)
    {
        Reader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
            JsonElement json = new JsonParser().parse(reader);
            if (json.isJsonArray())
            {
                JsonArray array = json.getAsJsonArray();
                for (int i = 0; i < array.size(); i++)
                {
                    loadElement(file, array.get(i), i);
                }
            }
            else
            {
                loadElement(file, json, -1);
            }
            return true;
        }
        catch (Exception e)
        {
            TinkersForging.getLog().warn("Unable to parse material config {}. No entries from this file were loaded.", file, e);
            return false;
        }
        finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private static void loadDefinitions(File file, List<MaterialDefinition> definitions)
    {
        for (int i = 0; i < definitions.size(); i++)
        {
            loadDefinition(file, definitions.get(i), "fallback entry " + i);
        }
    }

    private static void loadElement(File file, JsonElement element, int index)
    {
        if (!element.isJsonObject())
        {
            TinkersForging.getLog().warn("Skipping non-object material entry {} in {}", getEntryName(index), file);
            return;
        }
        MaterialDefinition definition;
        try
        {
            definition = GSON.fromJson(element, MaterialDefinition.class);
        }
        catch (RuntimeException e)
        {
            TinkersForging.getLog().warn("Skipping invalid material entry {} in {}", getEntryName(index), file, e);
            return;
        }
        loadDefinition(file, definition, getEntryName(index));
    }

    private static void loadDefinition(File file, @Nullable MaterialDefinition definition, String entryName)
    {
        if (definition == null || !definition.load)
        {
            TinkersForging.getLog().debug("Skipping disabled material entry {} in {}", entryName, file);
            return;
        }
        if (definition.requiredMod != null && !definition.requiredMod.trim().isEmpty() && !Loader.isModLoaded(definition.requiredMod.trim()))
        {
            TinkersForging.getLog().debug("Skipping material entry {} in {} because required mod '{}' is not loaded.", entryName, file, definition.requiredMod.trim());
            return;
        }
        if (definition.id == null || definition.id.trim().isEmpty())
        {
            TinkersForging.getLog().warn("Skipping material entry {} with no id in {}", entryName, file);
            return;
        }

        String id = cleanId(definition.id);
        if (!id.equals(definition.id))
        {
            TinkersForging.getLog().warn("Material id '{}' in {} {} was normalized to '{}'.", definition.id, file, entryName, id);
        }
        MaterialType existing = MaterialRegistry.getMaterial(id);
        if (existing != null && !definition.replaceExisting)
        {
            TinkersForging.getLog().debug("Material '{}' already exists; adding flags/source item from {} {} without replacing stats.", id, file, entryName);
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

    private static String getEntryName(int index)
    {
        return index < 0 ? "root object" : "at index " + index;
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
        public Map<String, String> comments;
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
