/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry.block.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class FluidDrainCompat {
    private static final int RADIUS = 12;
    private static final int MAX_FLUID_BLOCKS_PER_PASS = 2048;

    private FluidDrainCompat() {
    }

    public static Object mine(Object object, Object object2, Object object3, Object object4) {
        try {
            Class<?> clazz = object.getClass();
            Class<?> clazz2 = object3.getClass();
            Field field = FluidDrainCompat.findField(clazz, "collectFluids");
            field.setAccessible(true);
            boolean bl = field.getBoolean(object);
            Method method = FluidDrainCompat.findMethod(object2.getClass(), "getBlockState", 1);
            Object object6 = method.invoke(object2, object3);
            Method method2 = FluidDrainCompat.findMethod(object6.getClass(), "getBlock", 0);
            Object object7 = method2.invoke(object6, new Object[0]);
            Method method3 = FluidDrainCompat.findMethod(clazz2, "getX", 0);
            Method method4 = FluidDrainCompat.findMethod(clazz2, "getY", 0);
            Method method5 = FluidDrainCompat.findMethod(clazz2, "getZ", 0);
            int n = ((Number)method3.invoke(object3, new Object[0])).intValue();
            int n2 = ((Number)method4.invoke(object3, new Object[0])).intValue();
            int n3 = ((Number)method5.invoke(object3, new Object[0])).intValue();
            Constructor<?> constructor2 = null;
            for (Constructor<?> constructor : clazz2.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 3 || parameterTypes[0] != Integer.TYPE || parameterTypes[1] != Integer.TYPE || parameterTypes[2] != Integer.TYPE) continue;
                constructor2 = constructor;
                break;
            }
            if (constructor2 == null) {
                throw new NoSuchMethodException("BlockPos(int,int,int)");
            }
            Class<?> clazz3 = Class.forName("net.minecraft.world.level.block.Blocks");
            Object object8 = clazz3.getField("AIR").get(null);
            Method method6 = FluidDrainCompat.findMethod(object8.getClass(), "defaultBlockState", 0);
            Object airState = method6.invoke(object8, new Object[0]);
            Method setBlockMethod = FluidDrainCompat.findSetBlock(object2.getClass(), clazz2);
            Method method7 = FluidDrainCompat.findMethod(clazz, "setChanged", 0);
            Method method8 = FluidDrainCompat.findDeclared(clazz, "hasEmptyBucket", 0);
            Method method9 = FluidDrainCompat.findDeclared(clazz, "consumeEmptyBucket", 0);
            Method method10 = FluidDrainCompat.findDeclared(clazz, "prepareSpaceForDrops", 1);
            Method method11 = FluidDrainCompat.findDeclared(clazz, "insertDropsIntoActiveShulker", 1);
            Method method12 = FluidDrainCompat.findDeclared(clazz, "finishBoxIfFull", 0);
            method8.setAccessible(true);
            method9.setAccessible(true);
            method10.setAccessible(true);
            method11.setAccessible(true);
            method12.setAccessible(true);
            Method method13 = null;
            try {
                method13 = FluidDrainCompat.findMethod(object4.getClass(), "copy", 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            ArrayDeque<int[]> arrayDeque = new ArrayDeque<int[]>();
            HashSet<Long> hashSet = new HashSet<Long>();
            arrayDeque.add(new int[]{n, n2, n3});
            int n4 = 0;
            Object object9 = null;
            while (!arrayDeque.isEmpty() && n4 < 2048) {
                long l;
                int[] nArray = (int[])arrayDeque.removeFirst();
                int n5 = nArray[0];
                int n6 = nArray[1];
                int n7 = nArray[2];
                if (Math.abs(n5 - n) > 12 || Math.abs(n6 - n2) > 12 || Math.abs(n7 - n3) > 12 || !hashSet.add(l = FluidDrainCompat.pack(n5, n6, n7))) continue;
                Object t = constructor2.newInstance(n5, n6, n7);
                Object object10 = method.invoke(object2, t);
                Object object11 = method2.invoke(object10, new Object[0]);
                if (object11 != object7 && !Objects.equals(object11, object7)) continue;
                Method method14 = FluidDrainCompat.findMethod(object10.getClass(), "getFluidState", 0);
                Object object12 = method14.invoke(object10, new Object[0]);
                Method method15 = FluidDrainCompat.findMethod(object12.getClass(), "isSource", 0);
                boolean bl2 = Boolean.TRUE.equals(method15.invoke(object12, new Object[0]));
                if (bl && bl2) {
                    boolean bl3 = Boolean.TRUE.equals(method8.invoke(object, new Object[0]));
                    if (!bl3) {
                        object9 = FluidDrainCompat.enumConstant(clazz, "MiningResult", "NO_BUCKET");
                        break;
                    }
                    Object object13 = method13 != null ? method13.invoke(object4, new Object[0]) : object4;
                    List<Object> list = Collections.singletonList(object13);
                    Object object14 = method10.invoke(object, list);
                    String string = FluidDrainCompat.enumName(object14);
                    if ("NO_SHULKER".equals(string)) {
                        object9 = FluidDrainCompat.enumConstant(clazz, "MiningResult", "NO_SHULKER");
                        break;
                    }
                    if ("NO_OUTPUT_SPACE".equals(string)) {
                        object9 = FluidDrainCompat.enumConstant(clazz, "MiningResult", "STORAGE_FULL");
                        break;
                    }
                    method9.invoke(object, new Object[0]);
                    setBlockMethod.invoke(object2, t, airState, 2);
                    method11.invoke(object, list);
                    method12.invoke(object, new Object[0]);
                } else {
                    setBlockMethod.invoke(object2, t, airState, 2);
                }
                ++n4;
                arrayDeque.add(new int[]{n5 + 1, n6, n7});
                arrayDeque.add(new int[]{n5 - 1, n6, n7});
                arrayDeque.add(new int[]{n5, n6 + 1, n7});
                arrayDeque.add(new int[]{n5, n6 - 1, n7});
                arrayDeque.add(new int[]{n5, n6, n7 + 1});
                arrayDeque.add(new int[]{n5, n6, n7 - 1});
            }
            if (n4 > 0) {
                method7.invoke(object, new Object[0]);
                return FluidDrainCompat.enumConstant(clazz, "MiningResult", "MINED");
            }
            if (object9 != null) {
                return object9;
            }
            return FluidDrainCompat.enumConstant(clazz, "MiningResult", "MINED");
        }
        catch (Throwable throwable) {
            try {
                return FluidDrainCompat.enumConstant(object.getClass(), "MiningResult", "MINED");
            }
            catch (Throwable throwable2) {
                throw new RuntimeException("RailQuarry fluid drain failed", throwable);
            }
        }
    }

    private static long pack(int n, int n2, int n3) {
        return ((long)n & 0x3FFFFFFL) << 38 | ((long)n3 & 0x3FFFFFFL) << 12 | (long)n2 & 0xFFFL;
    }

    private static String enumName(Object object) {
        String string;
        if (object instanceof Enum) {
            Enum enum_ = (Enum)object;
            string = enum_.name();
        } else {
            string = String.valueOf(object);
        }
        return string;
    }

    private static Object enumConstant(Class<?> clazz, String string, String string2) throws Exception {
        Class<?> clazz2 = Class.forName(clazz.getName() + "$" + string);
        return Enum.valueOf(clazz2.asSubclass(Enum.class), string2);
    }

    private static Field findField(Class<?> clazz, String string) throws NoSuchFieldException {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            try {
                return clazz2.getDeclaredField(string);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        throw new NoSuchFieldException(string);
    }

    private static Method findDeclared(Class<?> clazz, String string, int n) throws NoSuchMethodException {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            for (Method method : clazz2.getDeclaredMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != n) continue;
                return method;
            }
        }
        throw new NoSuchMethodException(string + "/" + n);
    }

    private static Method findMethod(Class<?> clazz, String string, int n) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(string) || method.getParameterCount() != n) continue;
            return method;
        }
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != n) continue;
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(clazz.getName() + "." + string + "/" + n);
    }

    private static Method findSetBlock(Class<?> clazz, Class<?> clazz2) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            Class<?>[] classArray;
            if (!method.getName().equals("setBlock") || method.getParameterCount() != 3 || !(classArray = method.getParameterTypes())[0].isAssignableFrom(clazz2) && !clazz2.isAssignableFrom(classArray[0]) || classArray[2] != Integer.TYPE) continue;
            return method;
        }
        throw new NoSuchMethodException("setBlock(BlockPos,BlockState,int)");
    }
}

