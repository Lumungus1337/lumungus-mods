/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package de.djdaddy.railquarry.block.entity;

import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class QuarrySettingsContainer
implements Container {
    private final QuarryBlockEntity quarry;

    public QuarrySettingsContainer(QuarryBlockEntity quarryBlockEntity) {
        this.quarry = quarryBlockEntity;
    }

    public int getContainerSize() {
        return 54;
    }

    public boolean isEmpty() {
        return this.quarry.isEmpty();
    }

    public ItemStack getItem(int n) {
        return this.quarry.getMenuItem(n);
    }

    public ItemStack removeItem(int n, int n2) {
        return this.quarry.removeItem(n, n2);
    }

    public ItemStack removeItemNoUpdate(int n) {
        if (n >= 52 && n <= 53) {
            return this.quarry.removeItem(n, 64);
        }
        return this.quarry.removeItemNoUpdate(n);
    }

    public void setItem(int n, ItemStack itemStack) {
        if (n >= 52 && n <= 53) {
            return;
        }
        this.quarry.setItem(n, itemStack);
    }

    public void setChanged() {
        this.quarry.setChanged();
    }

    public boolean stillValid(Player player) {
        return this.quarry.stillValid(player);
    }

    public void clearContent() {
        this.quarry.clearContent();
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (n >= 52 && n <= 53) {
            return false;
        }
        return this.quarry.canPlaceItem(n, itemStack);
    }
}

