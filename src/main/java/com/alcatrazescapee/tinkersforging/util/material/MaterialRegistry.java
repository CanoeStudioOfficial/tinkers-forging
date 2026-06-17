package com.alcatrazescapee.tinkersforging.util.material;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.alcatrazescapee.tinkersforging.TinkersForging;

@ParametersAreNonnullByDefault
public final class MaterialRegistry
{
    private static final Map<String, MaterialType> MATERIALS = new LinkedHashMap<>();
    private static final Set<String> TINKERS_MATERIALS = new HashSet<>();
    private static final Set<String> NTP_MATERIALS = new HashSet<>();
    private static final Set<String> TOOLBOX_MATERIALS = new HashSet<>();

    public static void preInit(File configDir)
    {
        MaterialConfigLoader.load(configDir);
    }

    public static void addMaterial(MaterialType material)
    {
        addMaterial(material, false, false, false);
    }

    public static void addMaterial(MaterialType material, boolean tinkers, boolean noTreePunching, boolean toolbox)
    {
        if (MATERIALS.containsKey(material.getName()))
        {
            TinkersForging.getLog().debug("Material {} was overriden!", material.getName());
            clearMaterialFlags(material);
        }
        MATERIALS.put(material.getName(), material);
        addMaterialFlags(material, tinkers, noTreePunching, toolbox);
    }

    public static void addMaterialFlags(MaterialType material, boolean tinkers, boolean noTreePunching, boolean toolbox)
    {
        if (tinkers)
        {
            TINKERS_MATERIALS.add(material.getName());
        }
        if (noTreePunching)
        {
            NTP_MATERIALS.add(material.getName());
        }
        if (toolbox)
        {
            TOOLBOX_MATERIALS.add(material.getName());
        }
    }

    public static void addTinkersMaterial(MaterialType material)
    {
        if (MATERIALS.containsKey(material.getName()))
        {
            TINKERS_MATERIALS.add(material.getName());
        }
    }

    public static void addToolboxMaterial(MaterialType material)
    {
        if (MATERIALS.containsKey(material.getName()))
        {
            TOOLBOX_MATERIALS.add(material.getName());
        }
    }

    @Nonnull
    public static Collection<MaterialType> getAllMaterials()
    {
        return MATERIALS.values();
    }

    @Nullable
    public static MaterialType getMaterial(String name)
    {
        return MATERIALS.get(name);
    }

    public static boolean isTinkersMaterial(MaterialType material)
    {
        return TINKERS_MATERIALS.contains(material.getName());
    }

    public static boolean isNTPMaterial(MaterialType material)
    {
        return NTP_MATERIALS.contains(material.getName());
    }

    public static boolean isToolboxMaterial(MaterialType material)
    {
        return TOOLBOX_MATERIALS.contains(material.getName());
    }

    private static void clearMaterialFlags(MaterialType material)
    {
        TINKERS_MATERIALS.remove(material.getName());
        NTP_MATERIALS.remove(material.getName());
        TOOLBOX_MATERIALS.remove(material.getName());
    }
}
