/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry.block.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class VisualCompat {
    private static final Set<Object> PURGED_LEVELS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap()));

    private VisualCompat() {
    }

    public static void tick(Object object, Object object2, Object object3) {
        if (object == null) {
            return;
        }
        if (PURGED_LEVELS.add(object)) {
            VisualCompat.purgeLegacyVisuals(object);
        }
    }

    public static void removeAt(Object object, Object object2) {
    }

    private static void purgeLegacyVisuals(Object object) {
        try {
            Iterable<?> iterable = VisualCompat.allEntities(object);
            if (iterable == null) {
                return;
            }
            ArrayList arrayList = new ArrayList();
            for (Object object2 : iterable) {
                arrayList.add(object2);
            }
            for (Object object2 : arrayList) {
                if (object2 == null || !VisualCompat.hasRailQuarryVisualTag(object2)) continue;
                VisualCompat.discard(object2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Iterable<?> allEntities(Object object) {
        for (String string : new String[]{"getAllEntities", "getEntities"}) {
            try {
                Object object2;
                Method method = VisualCompat.findNoArg(object.getClass(), string);
                if (method == null || !((object2 = method.invoke(object, new Object[0])) instanceof Iterable)) continue;
                Iterable iterable = (Iterable)object2;
                return iterable;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private static boolean hasRailQuarryVisualTag(Object object) {
        try {
            Method method = VisualCompat.findNoArg(object.getClass(), "getTags");
            if (method == null) {
                return false;
            }
            Object object2 = method.invoke(object, new Object[0]);
            if (!(object2 instanceof Iterable)) {
                return false;
            }
            Iterable iterable = (Iterable)object2;
            for (Object t : iterable) {
                if (t == null || !String.valueOf(t).startsWith("rqv_")) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static void discard(Object object) {
        try {
            Method method = VisualCompat.findNoArg(object.getClass(), "discard");
            if (method != null) {
                method.invoke(object, new Object[0]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Method findNoArg(Class<?> clazz, String string) {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            Method[] methodArray = clazz2.getDeclaredMethods();
            int n = methodArray.length;
            for (int i = 0; i < n; ++i) {
                Method method = methodArray[i];
                if (!method.getName().equals(string) || method.getParameterCount() != 0) continue;
                try {
                    method.setAccessible(true);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
                return method;
            }
        }
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 0) continue;
            return method;
        }
        return null;
    }
}

