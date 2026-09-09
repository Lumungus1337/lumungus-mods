/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry.block.entity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class ChunkLoaderCompat {
    private static final List<Record> RECORDS = new ArrayList<Record>();
    private static Method setChunkForcedMethod;
    private static Class<?> setChunkForcedOwner;

    private ChunkLoaderCompat() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(Object object, Object object2, Object object3) {
        if (object == null || object2 == null || object3 == null) {
            return;
        }
        try {
            List<Record> list;
            int n = ChunkLoaderCompat.callInt(object2, "getX");
            int n2 = ChunkLoaderCompat.callInt(object2, "getZ");
            boolean bl = ChunkLoaderCompat.readBoolean(object3, "directionVerified", false);
            int n3 = ChunkLoaderCompat.readInt(object3, "railOffset", 0);
            Object object4 = ChunkLoaderCompat.readField(object3, "facing");
            String string = object4 == null ? "?" : String.valueOf(object4);
            Set<Long> set = ChunkLoaderCompat.computeDesiredChunks(n, n2, bl, n3, object4);
            List<Record> list2 = list = RECORDS;
            synchronized (list2) {
                ChunkLoaderCompat.pruneDeadRecords();
                Record record = ChunkLoaderCompat.findMovingRecord(object, n, n2, string, n3);
                if (record == null) {
                    record = new Record(object, n, n2, string, n3);
                    RECORDS.add(record);
                }
                for (long l : set) {
                    if (record.forced.contains(l)) continue;
                    ChunkLoaderCompat.setForced(object, ChunkLoaderCompat.chunkX(l), ChunkLoaderCompat.chunkZ(l), true);
                }
                for (long l : new HashSet<Long>(record.forced)) {
                    if (set.contains(l)) continue;
                    ChunkLoaderCompat.setForced(object, ChunkLoaderCompat.chunkX(l), ChunkLoaderCompat.chunkZ(l), false);
                }
                record.forced.clear();
                record.forced.addAll(set);
                record.lastX = n;
                record.lastZ = n2;
                record.facing = string;
                record.railOffset = n3;
                record.level = new WeakReference<Object>(object);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void onRemoved(Object object) {
        if (object == null) {
            return;
        }
        try {
            List<Record> list;
            if (ChunkLoaderCompat.readBoolean(object, "beingMoved", false)) {
                return;
            }
            Object object2 = ChunkLoaderCompat.callNoArg(object, "getLevel");
            Object object3 = ChunkLoaderCompat.callNoArg(object, "getBlockPos");
            if (object2 == null || object3 == null) {
                return;
            }
            if (ChunkLoaderCompat.isQuarryStillAt(object2, object3)) {
                return;
            }
            int n = ChunkLoaderCompat.callInt(object3, "getX");
            int n2 = ChunkLoaderCompat.callInt(object3, "getZ");
            List<Record> list2 = list = RECORDS;
            synchronized (list2) {
                Iterator<Record> iterator = RECORDS.iterator();
                while (iterator.hasNext()) {
                    Record record = iterator.next();
                    if (record.level.get() != object2 || record.lastX != n || record.lastZ != n2) continue;
                    for (long l : record.forced) {
                        ChunkLoaderCompat.setForced(object2, ChunkLoaderCompat.chunkX(l), ChunkLoaderCompat.chunkZ(l), false);
                    }
                    iterator.remove();
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Set<Long> computeDesiredChunks(int n, int n2, boolean bl, int n3, Object object) throws Exception {
        int n4;
        HashSet<Long> hashSet = new HashSet<Long>();
        int n5 = Math.floorDiv(n, 16);
        int n6 = Math.floorDiv(n2, 16);
        if (!bl || n3 == 0 || object == null) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    hashSet.add(ChunkLoaderCompat.pack(n5 + i, n6 + j));
                }
            }
            return hashSet;
        }
        int n7 = ChunkLoaderCompat.callInt(object, "getStepX");
        int n8 = ChunkLoaderCompat.callInt(object, "getStepZ");
        Object object2 = ChunkLoaderCompat.callNoArg(object, "getClockWise");
        int n9 = ChunkLoaderCompat.callInt(object2, "getStepX");
        int n10 = ChunkLoaderCompat.callInt(object2, "getStepZ");
        int n11 = n + n9 * n3;
        int n12 = n2 + n10 * n3;
        int n13 = 16;
        int n14 = 32;
        int[] nArray = new int[]{n - n7 * n13, n + n7 * n14, n11 - n7 * n13, n11 + n7 * n14};
        int[] nArray2 = new int[]{n2 - n8 * n13, n2 + n8 * n14, n12 - n8 * n13, n12 + n8 * n14};
        int n15 = nArray[0];
        int n16 = nArray[0];
        int n17 = nArray2[0];
        int n18 = nArray2[0];
        for (n4 = 1; n4 < 4; ++n4) {
            n15 = Math.min(n15, nArray[n4]);
            n16 = Math.max(n16, nArray[n4]);
            n17 = Math.min(n17, nArray2[n4]);
            n18 = Math.max(n18, nArray2[n4]);
        }
        n4 = Math.floorDiv(n15, 16);
        int n19 = Math.floorDiv(n16, 16);
        int n20 = Math.floorDiv(n17, 16);
        int n21 = Math.floorDiv(n18, 16);
        for (int i = n4; i <= n19; ++i) {
            for (int j = n20; j <= n21; ++j) {
                hashSet.add(ChunkLoaderCompat.pack(i, j));
            }
        }
        return hashSet;
    }

    private static Record findMovingRecord(Object object, int n, int n2, String string, int n3) {
        Record record = null;
        int n4 = Integer.MAX_VALUE;
        for (Record record2 : RECORDS) {
            int n5;
            if (record2.level.get() != object || !record2.facing.equals(string) || record2.railOffset != n3 || (n5 = Math.abs(record2.lastX - n) + Math.abs(record2.lastZ - n2)) > 2 || n5 >= n4) continue;
            record = record2;
            n4 = n5;
        }
        return record;
    }

    private static void pruneDeadRecords() {
        RECORDS.removeIf(record -> record.level.get() == null);
    }

    private static boolean isQuarryStillAt(Object object, Object object2) {
        if (object instanceof net.minecraft.server.level.ServerLevel level
                && object2 instanceof net.minecraft.core.BlockPos pos) {
            // Removal also runs during chunk unload. Never request that chunk again here.
            var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            return chunk == null || chunk.getBlockState(pos).is(de.djdaddy.railquarry.RailQuarryMod.QUARRY);
        }
        try {
            Object object3 = ChunkLoaderCompat.invokeCompatible(object, "getBlockState", object2);
            Object object4 = ChunkLoaderCompat.callNoArg(object3, "getBlock");
            Class<?> clazz = Class.forName("de.djdaddy.railquarry.RailQuarryMod");
            Field field = clazz.getField("QUARRY_BLOCK");
            return object4 == field.get(null);
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static boolean setForced(Object object, int n, int n2, boolean bl) {
        try {
            Method method = setChunkForcedMethod;
            if (method == null || setChunkForcedOwner == null || !setChunkForcedOwner.isInstance(object)) {
                method = null;
                for (Method method2 : object.getClass().getMethods()) {
                    Class<?>[] classArray = method2.getParameterTypes();
                    if (!method2.getName().equals("setChunkForced") || classArray.length != 3 || classArray[0] != Integer.TYPE || classArray[1] != Integer.TYPE || classArray[2] != Boolean.TYPE) continue;
                    method = method2;
                    break;
                }
                if (method == null) {
                    return false;
                }
                method.setAccessible(true);
                setChunkForcedMethod = method;
                setChunkForcedOwner = object.getClass();
            }
            method.invoke(object, n, n2, bl);
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static Object readField(Object object, String string) throws Exception {
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(string);
                field.setAccessible(true);
                return field.get(object);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        throw new NoSuchFieldException(string);
    }

    private static int readInt(Object object, String string, int n) {
        try {
            return ((Number)ChunkLoaderCompat.readField(object, string)).intValue();
        }
        catch (Throwable throwable) {
            return n;
        }
    }

    private static boolean readBoolean(Object object, String string, boolean bl) {
        try {
            return (Boolean)ChunkLoaderCompat.readField(object, string);
        }
        catch (Throwable throwable) {
            return bl;
        }
    }

    private static Object callNoArg(Object object, String string) throws Exception {
        Method method = object.getClass().getMethod(string, new Class[0]);
        method.setAccessible(true);
        return method.invoke(object, new Object[0]);
    }

    private static int callInt(Object object, String string) throws Exception {
        return ((Number)ChunkLoaderCompat.callNoArg(object, string)).intValue();
    }

    private static Object invokeCompatible(Object object, String string, Object object2) throws Exception {
        for (Method method : object.getClass().getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != 1 || !method.getParameterTypes()[0].isInstance(object2)) continue;
            method.setAccessible(true);
            return method.invoke(object, object2);
        }
        throw new NoSuchMethodException(string);
    }

    private static long pack(int n, int n2) {
        return (long)n << 32 ^ (long)n2 & 0xFFFFFFFFL;
    }

    private static int chunkX(long l) {
        return (int)(l >> 32);
    }

    private static int chunkZ(long l) {
        return (int)l;
    }

    private static final class Record {
        WeakReference<Object> level;
        int lastX;
        int lastZ;
        String facing;
        int railOffset;
        final Set<Long> forced = new HashSet<Long>();

        Record(Object object, int n, int n2, String string, int n3) {
            this.level = new WeakReference<Object>(object);
            this.lastX = n;
            this.lastZ = n2;
            this.facing = string;
            this.railOffset = n3;
        }
    }
}
