/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry.block.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class VisualCleanupCompat {
    private static final Map<Integer, Long> LAST_SWEEP = new ConcurrentHashMap<Integer, Long>();

    private VisualCleanupCompat() {
    }

    public static void removeAt(Object object, Object object2) {
        if (object == null || object2 == null) {
            return;
        }
        try {
            int n = VisualCleanupCompat.coord(object2, "getX");
            int n2 = VisualCleanupCompat.coord(object2, "getY");
            int n3 = VisualCleanupCompat.coord(object2, "getZ");
            String string = VisualCleanupCompat.tagFor(n, n2, n3);
            if (VisualCleanupCompat.removeFromAllEntities(object, string)) {
                return;
            }
            VisualCleanupCompat.removeFromArea(object, string, n, n2, n3, 80);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void sweepOrphans(Object object) {
        if (object == null) {
            return;
        }
        try {
            long l = VisualCleanupCompat.gameTime(object);
            int n = System.identityHashCode(object);
            Long l2 = LAST_SWEEP.get(n);
            if (l2 != null && l - l2 < 100L) {
                return;
            }
            LAST_SWEEP.put(n, l);
            Iterable<?> iterable = VisualCleanupCompat.allEntities(object);
            if (iterable == null) {
                return;
            }
            ArrayList arrayList = new ArrayList();
            for (Object entity : iterable) {
                arrayList.add(entity);
            }
            HashSet<String> hashSet = new HashSet<String>();
            HashSet<String> orphanTags = new HashSet<String>();
            for (Object e : arrayList) {
                for (String string : VisualCleanupCompat.tagsOf(e)) {
                    int[] nArray;
                    if (!string.startsWith("rqv_") || !hashSet.add(string) || (nArray = VisualCleanupCompat.parseTag(string)) == null || VisualCleanupCompat.hasQuarryAt(object, nArray[0], nArray[1], nArray[2])) continue;
                    orphanTags.add(string);
                }
            }
            if (orphanTags.isEmpty()) {
                return;
            }
            block5: for (Object e : arrayList) {
                for (String string : VisualCleanupCompat.tagsOf(e)) {
                    if (!orphanTags.contains(string)) continue;
                    VisualCleanupCompat.discard(e);
                    continue block5;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean hasQuarryAt(Object object, int n, int n2, int n3) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.core.BlockPos");
            Object obj = clazz.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(n, n2, n3);
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals("getBlockEntity") || method.getParameterCount() != 1 || !method.getParameterTypes()[0].isInstance(obj)) continue;
                Object object2 = method.invoke(object, obj);
                return object2 != null && object2.getClass().getName().equals("de.djdaddy.railquarry.block.entity.QuarryBlockEntity");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static long gameTime(Object object) {
        try {
            long l;
            Method method = VisualCleanupCompat.findNoArg(object.getClass(), "getGameTime");
            Object object2 = method == null ? null : method.invoke(object, new Object[0]);
            Object object3 = object2;
            if (object2 instanceof Number) {
                Number number = (Number)object2;
                l = number.longValue();
            } else {
                l = System.currentTimeMillis() / 50L;
            }
            return l;
        }
        catch (Throwable throwable) {
            return System.currentTimeMillis() / 50L;
        }
    }

    private static Iterable<?> allEntities(Object object) {
        for (String string : new String[]{"getAllEntities", "getEntities"}) {
            try {
                Object object2;
                Method method = VisualCleanupCompat.findNoArg(object.getClass(), string);
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

    private static boolean removeFromAllEntities(Object object, String string) {
        Iterable<?> iterable = VisualCleanupCompat.allEntities(object);
        if (iterable == null) {
            return false;
        }
        return VisualCleanupCompat.removeTagged(iterable, string) > 0;
    }

    private static void removeFromArea(Object object2, String string, int n, int n2, int n3, int n4) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.entity.Display$BlockDisplay");
            Class<?> clazz2 = Class.forName("net.minecraft.world.phys.AABB");
            Constructor<?> constructor = clazz2.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE, Double.TYPE);
            Object obj = constructor.newInstance((double)n - (double)n4, (double)n2 - 8.0, (double)n3 - (double)n4, (double)n + (double)n4 + 1.0, (double)n2 + 16.0, (double)n3 + (double)n4 + 1.0);
            for (Method method : object2.getClass().getMethods()) {
                if (!method.getName().equals("getEntitiesOfClass")) continue;
                Class<?>[] classArray = method.getParameterTypes();
                Object object3 = null;
                try {
                    if (classArray.length == 2 && classArray[0] == Class.class && classArray[1].isInstance(obj)) {
                        object3 = method.invoke(object2, clazz, obj);
                    } else if (classArray.length == 3 && classArray[0] == Class.class && classArray[1].isInstance(obj) && Predicate.class.isAssignableFrom(classArray[2])) {
                        Predicate<Object> predicate = value -> true;
                        object3 = method.invoke(object2, clazz, obj, predicate);
                    }
                }
                catch (Throwable predicate) {
                    // empty catch block
                }
                if (!(object3 instanceof Iterable)) continue;
                Iterable iterable = (Iterable)object3;
                VisualCleanupCompat.removeTagged(iterable, string);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static int removeTagged(Iterable<?> iterable, String string) {
        ArrayList arrayList = new ArrayList();
        for (Object object : iterable) {
            arrayList.add(object);
        }
        int n = 0;
        for (Object e : arrayList) {
            try {
                if (!VisualCleanupCompat.tagsOf(e).contains(string) || !VisualCleanupCompat.discard(e)) continue;
                ++n;
            }
            catch (Throwable throwable) {}
        }
        return n;
    }

    private static Set<String> tagsOf(Object object) {
        HashSet<String> hashSet = new HashSet<String>();
        try {
            Object object2 = VisualCleanupCompat.callNoArgs(object, "getTags");
            if (object2 instanceof Iterable) {
                Iterable iterable = (Iterable)object2;
                for (Object t : iterable) {
                    hashSet.add(String.valueOf(t));
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return hashSet;
    }

    private static boolean discard(Object object) {
        if (object == null) {
            return false;
        }
        try {
            Method method = VisualCleanupCompat.findNoArg(object.getClass(), "discard");
            if (method != null) {
                method.invoke(object, new Object[0]);
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static Object callNoArgs(Object object, String string) throws Exception {
        Method method = VisualCleanupCompat.findNoArg(object.getClass(), string);
        return method == null ? null : method.invoke(object, new Object[0]);
    }

    private static Method findNoArg(Class<?> clazz, String string) {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 0) continue;
            return method;
        }
        return null;
    }

    private static int coord(Object object, String string) throws Exception {
        return ((Number)VisualCleanupCompat.callNoArgs(object, string)).intValue();
    }

    private static String enc(int n) {
        return n < 0 ? "m" + -n : "p" + n;
    }

    private static String tagFor(int n, int n2, int n3) {
        return "rqv_" + VisualCleanupCompat.enc(n) + "_" + VisualCleanupCompat.enc(n2) + "_" + VisualCleanupCompat.enc(n3);
    }

    private static int dec(String string) {
        return string.charAt(0) == 'm' ? -Integer.parseInt(string.substring(1)) : Integer.parseInt(string.substring(1));
    }

    private static int[] parseTag(String string) {
        try {
            String[] stringArray = string.split("_");
            if (stringArray.length != 4 || !stringArray[0].equals("rqv")) {
                return null;
            }
            return new int[]{VisualCleanupCompat.dec(stringArray[1]), VisualCleanupCompat.dec(stringArray[2]), VisualCleanupCompat.dec(stringArray[3])};
        }
        catch (Throwable throwable) {
            return null;
        }
    }
}

