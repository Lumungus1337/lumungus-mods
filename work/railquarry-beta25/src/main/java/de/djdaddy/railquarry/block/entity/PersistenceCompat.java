/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry.block.entity;

import java.lang.reflect.Method;

public final class PersistenceCompat {
    private PersistenceCompat() {
    }

    public static void markBlockEntityDirty(Object object) {
        if (object == null) {
            return;
        }
        try {
            Object object2 = PersistenceCompat.invokeNoArg(object, "getLevel");
            Object object3 = PersistenceCompat.invokeNoArg(object, "getBlockPos");
            PersistenceCompat.markChunk(object2, object3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void markMoveDirty(Object object, Object object2, Object object3) {
        try {
            PersistenceCompat.markChunk(object, object2);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            PersistenceCompat.markChunk(object, object3);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void markChunk(Object object, Object object2) throws Exception {
        if (object == null || object2 == null) {
            return;
        }
        Method method = PersistenceCompat.findCompatible(object.getClass(), "getChunkAt", object2);
        if (method == null) {
            return;
        }
        Object object3 = method.invoke(object, object2);
        if (object3 == null) {
            return;
        }
        Method method2 = PersistenceCompat.findBooleanMethod(object3.getClass(), "setUnsaved");
        if (method2 != null) {
            method2.invoke(object3, true);
        }
    }

    private static Object invokeNoArg(Object object, String string) throws Exception {
        Method method = null;
        Class<?> clazz = object.getClass();
        while (clazz != null && method == null) {
            try {
                method = clazz.getDeclaredMethod(string, new Class[0]);
            }
            catch (NoSuchMethodException noSuchMethodException) {
                clazz = clazz.getSuperclass();
            }
        }
        if (method == null) {
            return null;
        }
        method.setAccessible(true);
        return method.invoke(object, new Object[0]);
    }

    private static Method findCompatible(Class<?> clazz, String string, Object object) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 1 || object != null && !method.getParameterTypes()[0].isInstance(object)) continue;
            return method;
        }
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != 1 || object != null && !method.getParameterTypes()[0].isInstance(object)) continue;
                try {
                    method.setAccessible(true);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return method;
            }
        }
        return null;
    }

    private static Method findBooleanMethod(Class<?> clazz, String string) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 1 || method.getParameterTypes()[0] != Boolean.TYPE && method.getParameterTypes()[0] != Boolean.class) continue;
            return method;
        }
        return null;
    }
}

