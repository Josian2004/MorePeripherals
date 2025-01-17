package com.hakimen.peripherals.peripherals;

import com.hakimen.peripherals.utils.EnchantUtils;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantingTablePeripheral implements IPeripheral, IPeripheralProvider {

    public Level level;

    @NotNull
    @Override
    public String getType() {
        return "enchanting_interface";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    @LuaFunction(mainThread = true)
    public MethodResult getEnchantsFor(IComputerAccess computer, String from, int slot){
        slot -=1;
        List<String> enchants = new ArrayList<>();
        IPeripheral input = computer.getAvailablePeripheral(from);
        if (input == null)
            return MethodResult.of(false,"the input " + from + " was not found");
        IItemHandler inputHandler = extractHandler(input.getTarget());
        if (slot < 0 || slot > inputHandler.getSlots())
            return MethodResult.of(false,"slot out of range");
        var item = inputHandler.getStackInSlot(slot);
        if (item.isEnchantable()) {
            if (!item.isEnchanted()) {
                for (EnchantmentCategory category : EnchantmentCategory.values()) {
                    for (Enchantment e : ForgeRegistries.ENCHANTMENTS) {
                        if (e.category == category && e.canApplyAtEnchantingTable(item) && !e.isTreasureOnly()) {
                            enchants.add(ForgeRegistries.ENCHANTMENTS.getKey(e).toString());
                        }
                    }
                }
            } else {
                return MethodResult.of(false,"this item is already enchanted");
            }
        } else {
            return MethodResult.of(false,"this item can not be enchanted");
        }
        return MethodResult.of(true,enchants);
    }

    @LuaFunction(mainThread = true)
    public MethodResult enchant(IComputerAccess computer, String from, int slot, String resources) {
        var bottlesNeeded = 8;

        IPeripheral input = computer.getAvailablePeripheral(from);
        if (input == null)
            return MethodResult.of(false,"the input " + from + " was not found");
        IItemHandler inputHandler = extractHandler(input.getTarget());
        if (slot < 0 || slot > inputHandler.getSlots())
            return MethodResult.of(false,"slot out of range");

        IPeripheral resourcesInput = computer.getAvailablePeripheral(resources);
        if (resourcesInput == null)
            return MethodResult.of(false,"the resources input " + resources + " was not found");
        IItemHandler resourcesInputHandler = extractHandler(resourcesInput.getTarget());

        boolean foundLapis = false, foundEXP = false;
        int lapisSlot = -1, expSlot = -1;
        for (int i = 0; i < resourcesInputHandler.getSlots(); i++) {
            if (!foundLapis) {
                if (resourcesInputHandler.getStackInSlot(i).is(Items.LAPIS_LAZULI) && resourcesInputHandler.getStackInSlot(i).getCount() >= 1) {
                    lapisSlot = i;
                    foundLapis = true;
                }
            }
            if (!foundEXP) {
                if (resourcesInputHandler.getStackInSlot(i).is(Items.EXPERIENCE_BOTTLE) && resourcesInputHandler.getStackInSlot(i).getCount() >= bottlesNeeded) {
                    expSlot = i;
                    foundEXP = true;
                }
            }
        }
        if (!foundEXP) {
            return MethodResult.of(false,"not found the required " + bottlesNeeded + " xp bottles");
        }
        if (!foundLapis) {
            return MethodResult.of(false,"not found the required lapis lazuli");
        }
        if (foundEXP && foundLapis) {
            resourcesInputHandler.extractItem(lapisSlot, 1, false);
            resourcesInputHandler.extractItem(expSlot, bottlesNeeded, false);
            var item = inputHandler.getStackInSlot(slot);
            boolean isBook = item.getItem() == Items.BOOK;
            var bookItem = item.copy();
            if (isBook) {
                var enchant = ForgeRegistries.ENCHANTMENTS.getValues().stream().toList().get(new Random().nextInt(0, ForgeRegistries.ENCHANTMENTS.getValues().size()));
                var value = level.random.nextInt(enchant.getMinLevel(), enchant.getMaxLevel() + 1);
                bookItem = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchant, value));
                if (item.getCount() > 1) {
                    boolean putBook = false;
                    for (int i = 0; i < inputHandler.getSlots(); i++) {
                        if (inputHandler.getStackInSlot(i) == ItemStack.EMPTY) {
                            inputHandler.insertItem(i, bookItem, false);
                            putBook = true;
                            break;
                        }
                    }
                    if (!putBook) {
                        return MethodResult.of(false);
                    }
                }else if (!item.isEnchanted()) {
                    if (item.isEnchantable()) {
                        EnchantUtils.addRandomEnchant(new Random(), item);
                        return MethodResult.of(true);

                    } else {
                        return MethodResult.of(false,"this item is not enchatable");
                    }
                } else {
                    inputHandler.insertItem(slot, bookItem, false);
                }
                inputHandler.extractItem(slot, 1, false);
            }
            return MethodResult.of(true);
        } else {
            return MethodResult.of(false,"this item is already enchanted");
        }
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

    private static int moveItem(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, final int limit) {
        // See how much we can get out of this slot
        ItemStack extracted = from.extractItem(fromSlot, limit, true);
        if (extracted.isEmpty()) return 0;

        // Limit the amount to extract
        int extractCount = Math.min(extracted.getCount(), limit);
        extracted.setCount(extractCount);

        ItemStack remainder = toSlot < 0 ? ItemHandlerHelper.insertItem(to, extracted, false) : to.insertItem(toSlot, extracted, false);
        int inserted = remainder.isEmpty() ? extractCount : extractCount - remainder.getCount();
        if (inserted <= 0) return 0;

        // Remove the item from the original inventory. Technically this could fail, but there's little we can do
        // about that.
        from.extractItem(fromSlot, inserted, false);
        return inserted;
    }

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        level = world;
        if (world.getBlockState(pos).getBlock().equals(Blocks.ENCHANTING_TABLE)) {

            return LazyOptional.of(() -> this);
        }
        return LazyOptional.empty();
    }
}
