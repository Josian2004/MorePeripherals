package com.hakimen.peripherals.peripherals;

import com.hakimen.peripherals.blocks.tile_entities.SpawnerInterfaceEntity;
import com.hakimen.peripherals.items.MobDataCardItem;
import com.hakimen.peripherals.utils.Utils;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SpawnerPeripheral implements IPeripheral {

    SpawnerInterfaceEntity entity;

    public SpawnerPeripheral(SpawnerInterfaceEntity entity) {
        this.entity = entity;
    }

    @NotNull
    @Override
    public String getType() {
        return "spawner_interface";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    @LuaFunction(mainThread = true)
    public final boolean changeSpawner(IComputerAccess computer, String inv, int slot, Optional<Boolean> force) throws LuaException {
        slot = slot-1;
        IPeripheral peripheral = computer.getAvailablePeripheral(inv);
        if (inv == null) throw new LuaException("the input " + inv + " was not found");
        IItemHandler handler = extractHandler(peripheral.getTarget());

        if(Utils.isFromMinecraft(computer,inv)){
            if(slot < 0 || slot > handler.getSlots()) throw new LuaException("Slot out of range");
            var stack = handler.getStackInSlot(slot);
            if(stack.hasTag() && stack.getItem() instanceof MobDataCardItem){
                var previousMob = entity.saveWithFullMetadata().getCompound("SpawnData").getCompound("entity").getString("id");
                entity.entity.getSpawner().setEntityId(EntityType.byString(stack.getTag().getString("mob")).get(),entity.getLevel(),entity.getLevel().getRandom(), entity.getBlockPos());
                if(force.isPresent() && force.get()){
                    stack.getTag().remove("mob");
                }else{
                    if(!previousMob.equals("minecraft:pig")){
                        stack.getTag().putString("mob",previousMob);
                    }
                }
                stack.resetHoverName();
                CompoundTag tag = new CompoundTag();
                var saved = entity.entity.getSpawner().save(tag);
                entity.entity.getSpawner().load(entity.getLevel(),entity.getBlockPos(),saved);
                entity.setChanged();
                entity.entity.getSpawner().getSpawnerEntity();
                return true;
            }
        }else{
            throw new LuaException("This block requires a vanilla inventory");
        }
        return false;
    }


    @LuaFunction(mainThread = true)
    public final String getCurrentlySpawningMob(){
        CompoundTag tag = new CompoundTag();
        tag = entity.entity.getSpawner().save(tag);
        return tag.getCompound("SpawnData").getCompound("entity").getString("id");
    }

    long lastTime;
    @LuaFunction(mainThread = true)
    public final boolean captureSpawner(IComputerAccess computer, Optional<String> inv, Optional<Integer> slot) throws LuaException {
        ItemStack spawnerBlock = new ItemStack(Items.SPAWNER);

        CompoundTag tag = new CompoundTag();
        var saved = entity.entity.getSpawner().save(tag);
        if(lastTime + 50 >= System.currentTimeMillis())
            return false;
        if(inv.isPresent()){
            IPeripheral peripheral = computer.getAvailablePeripheral(inv.get());
            if (inv.get() == null) throw new LuaException("the input " + inv.get() + " was not found");
            IItemHandler handler = extractHandler(peripheral.getTarget());
            if(Utils.isFromMinecraft(computer,inv.get())) {
                if (slot.isPresent()){
                    slot = Optional.of(slot.get()-1);
                    if (slot.get() < 0 || slot.get() > handler.getSlots()) throw new LuaException("Slot out of range");
                    var stack = handler.getStackInSlot(slot.get());
                    if(stack.getItem() instanceof MobDataCardItem){
                        stack.getOrCreateTag().putString("mob",saved.getCompound("SpawnData").getCompound("entity").getString("id"));
                        stack.setHoverName(Component.translatable("item.peripherals.mob_data_card").append(" ("+stack.getOrCreateTag().getString("mob")+")"));
                        var blockPos = entity.getBlockPos();
                        entity.getLevel().addFreshEntity(new ItemEntity(entity.getLevel(),blockPos.getX(),blockPos.getY(),blockPos.getZ(),spawnerBlock));
                        entity.getLevel().destroyBlock(blockPos,false);
                        lastTime = System.currentTimeMillis();
                        return true;
                    }
                }
            }else{
                throw new LuaException("This block requires a vanilla inventory");
            }
        }else if(saved.getCompound("SpawnData").getCompound("entity").getString("id").equals("minecraft:pig")){
                var blockPos = entity.getBlockPos();
                entity.getLevel().addFreshEntity(new ItemEntity(entity.getLevel(),blockPos.getX(),blockPos.getY(),blockPos.getZ(),spawnerBlock));
                entity.getLevel().destroyBlock(blockPos,false);
                lastTime = System.currentTimeMillis();
                return true;
        }
        return false;
    }

    @javax.annotation.Nullable
    private static IItemHandler extractHandler(@javax.annotation.Nullable Object object) {
        if (object instanceof BlockEntity blockEntity && blockEntity.isRemoved()) return null;

        if (object instanceof ICapabilityProvider provider) {
            LazyOptional<IItemHandler> cap = provider.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (cap.isPresent()) return cap.orElseThrow(NullPointerException::new);
        }

        if (object instanceof IItemHandler handler) return handler;
        if (object instanceof Container container) return new InvWrapper(container);
        return null;
    }
}
