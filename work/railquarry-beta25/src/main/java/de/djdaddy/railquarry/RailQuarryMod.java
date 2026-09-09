/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ModInitializer
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 */
package de.djdaddy.railquarry;

import de.djdaddy.railquarry.SilkTouchCompat;
import de.djdaddy.railquarry.block.QuarryBlock;
import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class RailQuarryMod
implements ModInitializer {
    public static final String MOD_ID = "railquarry";
    public static final ResourceKey<Block> QUARRY_KEY = ResourceKey.create((ResourceKey)Registries.BLOCK, (Identifier)RailQuarryMod.id("quarry"));
    public static final Block QUARRY = new QuarryBlock(RailQuarryMod.copyBlockProperties(Blocks.IRON_BLOCK).setId(QUARRY_KEY));
    public static final ResourceKey<Item> QUARRY_ITEM_KEY = ResourceKey.create((ResourceKey)Registries.ITEM, (Identifier)RailQuarryMod.id("quarry"));
    public static final Item QUARRY_ITEM = new BlockItem(QUARRY, new Item.Properties().setId(QUARRY_ITEM_KEY));
    public static final ResourceKey<BlockEntityType<?>> QUARRY_BLOCK_ENTITY_KEY = ResourceKey.create((ResourceKey)Registries.BLOCK_ENTITY_TYPE, (Identifier)RailQuarryMod.id("quarry"));
    public static final BlockEntityType<QuarryBlockEntity> QUARRY_BLOCK_ENTITY = new BlockEntityType(QuarryBlockEntity::new, Set.of(QUARRY));

    public static Identifier id(String string) {
        return Identifier.fromNamespaceAndPath((String)MOD_ID, (String)string);
    }

    public static Component literalText(String string) {
        return RailQuarryMod.createText("literal", string);
    }

    public static Component translatableText(String string) {
        return RailQuarryMod.createText("translatable", string);
    }

    private static Component createText(String string, String string2) {
        try {
            for (Method method : Component.class.getMethods()) {
                Object object;
                Class<?>[] classArray;
                if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(string) || (classArray = method.getParameterTypes()).length != 1 || classArray[0] != String.class || !((object = method.invoke(null, string2)) instanceof Component)) continue;
                Component component = (Component)object;
                return component;
            }
            throw new IllegalStateException("Could not find compatible Component." + string + " overload");
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwable throwable = invocationTargetException.getCause();
            throw new IllegalStateException("Minecraft rejected text creation via Component." + string, throwable != null ? throwable : invocationTargetException);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Failed to create Minecraft text via Component." + string, reflectiveOperationException);
        }
    }

    public void onInitialize() {
        de.djdaddy.railquarry.menu.QuarryMenu.register();
        SilkTouchCompat.init();
        RailQuarryMod.register("BLOCK", QUARRY_KEY, QUARRY);
        RailQuarryMod.register("ITEM", QUARRY_ITEM_KEY, QUARRY_ITEM);
        RailQuarryMod.register("BLOCK_ENTITY_TYPE", QUARRY_BLOCK_ENTITY_KEY, QUARRY_BLOCK_ENTITY);
        RailQuarryMod.addToCreativeMenu();
    }

    private static void addToCreativeMenu() {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.item.CreativeModeTabs");
            Object object2 = clazz.getField("BUILDING_BLOCKS").get(null);
            Class<?> clazz2 = Class.forName("net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents");
            Object object3 = null;
            for (Method method2 : clazz2.getMethods()) {
                Class<?>[] classArray;
                if (!Modifier.isStatic(method2.getModifiers()) || !method2.getName().equals("modifyOutputEvent") || (classArray = method2.getParameterTypes()).length != 1 || !classArray[0].isInstance(object2)) continue;
                object3 = method2.invoke(null, object2);
                break;
            }
            if (object3 == null) {
                return;
            }
            Class<?> clazz3 = Class.forName("net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents$ModifyOutput");
            Object object4 = Proxy.newProxyInstance(RailQuarryMod.class.getClassLoader(), new Class[]{clazz3}, (object, method, objectArray) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "toString" -> "RailQuarryCreativeTabCallback";
                        case "hashCode" -> System.identityHashCode(object);
                        case "equals" -> object == (objectArray == null ? null : objectArray[0]);
                        default -> null;
                    };
                }
                boolean bl = false;
                if (objectArray != null) {
                    for (Object callbackArg : objectArray) {
                        if (callbackArg == null) continue;
                        for (Method method2 : callbackArg.getClass().getMethods()) {
                            Class<?>[] classArray;
                            if (!method2.getName().equals("accept") || (classArray = method2.getParameterTypes()).length != 1 || !classArray[0].isInstance(QUARRY_ITEM)) continue;
                            method2.invoke(callbackArg, QUARRY_ITEM);
                            bl = true;
                            break;
                        }
                        if (bl) break;
                    }
                }
                return RailQuarryMod.defaultValue(method.getReturnType());
            });
            for (Method method3 : object3.getClass().getMethods()) {
                if (!method3.getName().equals("register") || method3.getParameterCount() != 1) continue;
                try {
                    method3.invoke(object3, object4);
                    return;
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
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

    private static BlockBehaviour.Properties copyBlockProperties(Block block) {
        try {
            for (Method method : BlockBehaviour.Properties.class.getMethods()) {
                Object object;
                Class<?>[] classArray;
                if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals("ofFullCopy") || (classArray = method.getParameterTypes()).length != 1 || !classArray[0].isInstance(block) || !((object = method.invoke(null, block)) instanceof BlockBehaviour.Properties)) continue;
                BlockBehaviour.Properties properties = (BlockBehaviour.Properties)object;
                return properties;
            }
            throw new IllegalStateException("Could not find compatible BlockBehaviour.Properties.ofFullCopy overload");
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwable throwable = invocationTargetException.getCause();
            throw new IllegalStateException("Minecraft rejected Rail Quarry block property copy", throwable != null ? throwable : invocationTargetException);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Failed to create Rail Quarry block properties", reflectiveOperationException);
        }
    }

    private static void register(String string, ResourceKey<?> resourceKey, Object object) {
        try {
            Field field = BuiltInRegistries.class.getField(string);
            Object object2 = field.get(null);
            for (Method method : Registry.class.getMethods()) {
                Class<?>[] classArray;
                if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals("register") || (classArray = method.getParameterTypes()).length != 3 || !classArray[0].isInstance(object2) || !ResourceKey.class.isAssignableFrom(classArray[1]) || !classArray[2].isInstance(object) && classArray[2] != Object.class) continue;
                try {
                    method.invoke(null, object2, resourceKey, object);
                    return;
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            throw new IllegalStateException("Could not find compatible Registry.register overload for " + string);
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwable throwable = invocationTargetException.getCause();
            throw new IllegalStateException("Minecraft rejected Rail Quarry registration for " + string, throwable != null ? throwable : invocationTargetException);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Failed to register Rail Quarry content for " + string, reflectiveOperationException);
        }
    }
}
