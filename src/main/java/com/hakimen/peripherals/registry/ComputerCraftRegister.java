package com.hakimen.peripherals.registry;

import com.hakimen.peripherals.peripherals.*;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.Capabilities;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;


public class ComputerCraftRegister {


    public static void registerTurtleUpgrades(){
    }



    public static void registerPeripheralProvider(){
        ForgeComputerCraftAPI.registerPeripheralProvider(new AnvilPeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new BeehiveInterfacePeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new CrafterPeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new EnchantingTablePeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new GrindstonePeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new LoomInterfacePeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(new SpawnerPeripheral());
        ForgeComputerCraftAPI.registerPeripheralProvider(((world, blockPos, direction) -> {
            BlockEntity te = world.getBlockEntity(blockPos);
            if(te == null) {
                return LazyOptional.empty();
            }
            LazyOptional<IPeripheral> capabilityLazyOptional = te.getCapability(Capabilities.CAPABILITY_PERIPHERAL);
            if(capabilityLazyOptional.isPresent()){
                return capabilityLazyOptional;
            }
            return LazyOptional.empty();
        }));
    }
}
