/*
 * Decompiled with CFR 0.152.
 */
package de.djdaddy.railquarry;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class SilkTouchCompat {
    private static volatile boolean initialized;

    private SilkTouchCompat() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Class<?> clazz = Class.forName("net.fabricmc.fabric.api.event.player.UseBlockCallback");
            Object object = clazz.getField("EVENT").get(null);
            Object object2 = Proxy.newProxyInstance(SilkTouchCompat.class.getClassLoader(), new Class[]{clazz}, SilkTouchCompat::invokeCallback);
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals("register") || method.getParameterCount() != 1) continue;
                method.invoke(object, object2);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Object invokeCallback(Object object, Method method, Object[] objectArray) throws Throwable {
        String string = method.getName();
        if (string.equals("toString")) {
            return "RailQuarrySilkTouchCallback";
        }
        if (string.equals("hashCode")) {
            return System.identityHashCode(object);
        }
        if (string.equals("equals")) {
            return objectArray != null && objectArray.length == 1 && object == objectArray[0];
        }
        if (!string.equals("interact") || objectArray == null || objectArray.length < 4) {
            return SilkTouchCompat.defaultValue(method.getReturnType());
        }
        Object object2 = objectArray[0];
        Object object3 = objectArray[1];
        Object object4 = objectArray[3];
        Object object5 = SilkTouchCompat.interactionResult("PASS");
        if (!SilkTouchCompat.boolCall(object2, "isShiftKeyDown")) {
            return object5;
        }
        Object object6 = SilkTouchCompat.callNoArgs(object4, "getBlockPos");
        if (object6 == null) {
            return object5;
        }
        Object object7 = SilkTouchCompat.callOne(object3, "getBlockEntity", object6);
        if (object7 == null || !object7.getClass().getName().equals("de.djdaddy.railquarry.block.entity.QuarryBlockEntity")) {
            return object5;
        }
        if (!SilkTouchCompat.boolCall(object3, "isClientSide")) {
            boolean bl = Boolean.TRUE.equals(SilkTouchCompat.callNoArgs(object7, "toggleSilkTouch"));
            try {
                Class<?> clazz = Class.forName("de.djdaddy.railquarry.RailQuarryMod");
                Method method2 = clazz.getMethod("literalText", String.class);
                Object object8 = method2.invoke(null, "Rail Quarry Silk Touch: " + (bl ? "ON" : "OFF"));
                for (Method method3 : object2.getClass().getMethods()) {
                    if (!method3.getName().equals("sendSystemMessage") || method3.getParameterCount() != 1 || !method3.getParameterTypes()[0].isInstance(object8)) continue;
                    method3.invoke(object2, object8);
                    break;
                }
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return SilkTouchCompat.interactionResult("SUCCESS");
    }

    private static Object interactionResult(String string) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.InteractionResult");
            return clazz.getField(string).get(null);
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    private static boolean boolCall(Object object, String string) {
        Object object2 = SilkTouchCompat.callNoArgs(object, string);
        return Boolean.TRUE.equals(object2);
    }

    private static Object callNoArgs(Object object, String string) {
        if (object == null) {
            return null;
        }
        try {
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != 0) continue;
                return method.invoke(object, new Object[0]);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static Object callOne(Object object, String string, Object object2) {
        if (object == null) {
            return null;
        }
        try {
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != 1) continue;
                Class<?> clazz = method.getParameterTypes()[0];
                if (object2 != null && !clazz.isInstance(object2)) continue;
                return method.invoke(object, object2);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static Object defaultValue(Class<?> clazz) {
        if (clazz == Void.TYPE || !clazz.isPrimitive()) {
            return null;
        }
        if (clazz == Boolean.TYPE) {
            return false;
        }
        if (clazz == Byte.TYPE) {
            return (byte)0;
        }
        if (clazz == Short.TYPE) {
            return (short)0;
        }
        if (clazz == Integer.TYPE) {
            return 0;
        }
        if (clazz == Long.TYPE) {
            return 0L;
        }
        if (clazz == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (clazz == Double.TYPE) {
            return 0.0;
        }
        if (clazz == Character.TYPE) {
            return Character.valueOf('\u0000');
        }
        return null;
    }
}

