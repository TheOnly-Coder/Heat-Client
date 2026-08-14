package com.hotwillnotelaborate.heatclient.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Small reflection helper to access Minecraft fields and methods by their
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

    /**
     * Get a declared method from a class, trying SRG name first,
     * then MCP name.
     */
    public static Method getMethod(Class<?> clazz, String srgName, String mcpName,
                                     Class<?>... paramTypes) {
        try {
            Method m = clazz.getDeclaredMethod(srgName, paramTypes);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            try {
                Method m = clazz.getDeclaredMethod(mcpName, paramTypes);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException(
                    "Cannot find method " + srgName + " or " + mcpName
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

    public static boolean getBoolean(Object obj, Field f) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { return f.getBoolean(obj); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static int getInt(Object obj, Field f) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { return f.getInt(obj); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    public static void setInt(Object obj, Field f, int val) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { f.setInt(obj, val); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }
    public static void setBoolean(Object obj, Field f, boolean val) {
        if (!f.isAccessible()) f.setAccessible(true);
        try { f.setBoolean(obj, val); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, Method m, Object... args) {
        try { return (T) m.invoke(obj, args); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
