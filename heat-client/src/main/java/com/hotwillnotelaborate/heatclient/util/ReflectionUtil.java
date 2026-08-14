package com.hotwillnotelaborate.heatclient.util;

import java.lang.reflect.Field;

/**
 * Small reflection helper to access Minecraft fields by their
 * SRG names (runtime) with MCP-name fallback (dev environment).
 */
public class ReflectionUtil {

    /**
     * Get a declared field from a class, trying SRG name first,
     * then MCP name.
     */
    public static Field getField(Class<?> clazz, String srgName, String mcpName) {
        try {
            return clazz.getDeclaredField(srgName);
        } catch (NoSuchFieldException e) {
            try {
                return clazz.getDeclaredField(mcpName);
            } catch (NoSuchFieldException e2) {
                throw new RuntimeException(
                    "Cannot find field " + srgName + " or " + mcpName
                    + " on " + clazz.getName(), e2);
            }
        }
    }

    public static double getDouble(Object obj, Field f) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { return f.getDouble(obj); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static float getFloat(Object obj, Field f) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { return f.getFloat(obj); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static void setFloat(Object obj, Field f, float val) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { f.setFloat(obj, val); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static void setBoolean(Object obj, Field f, boolean val) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { f.setBoolean(obj, val); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }
}
