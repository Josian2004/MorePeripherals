package com.hakimen.peripherals.utils;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static boolean isFromMinecraft(IComputerAccess computer,String from){
        IPeripheral inputLocation = computer.getAvailablePeripheral(from);
        return inputLocation.getType().contains("minecraft");
    }

    public static boolean canMergeItems(ItemStack stack1, ItemStack stack2) {
        if (stack1.getItem() != stack2.getItem()) return false;
        if (stack1.getDamageValue() != stack2.getDamageValue()) return false;
        if (stack1.getCount() > stack1.getMaxStackSize()) return false;
        return ItemStack.isSameItemSameTags(stack1, stack2);
    }
}
