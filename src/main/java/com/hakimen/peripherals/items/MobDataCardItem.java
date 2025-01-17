package com.hakimen.peripherals.items;

import com.hakimen.peripherals.config.Config;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Random;

public class MobDataCardItem extends Item {
    public MobDataCardItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if(entity instanceof LivingEntity livingEntity && !(entity instanceof Player) ){
            Random r = new Random();
            if (r.nextFloat() < Config.mobDataCaptureChance.get()) {
                stack.setHoverName(null);
                stack.getOrCreateTag().putString("mob", livingEntity.getEncodeId());
                stack.setHoverName(Component.translatable("item.peripherals.spawner_card").append(" ("+livingEntity.getEncodeId()+")"));
                return false;
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }



    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        stack.getOrCreateTag();
        if (stack.getTag() != null){
            components.add(Component.literal((stack.getTag().get("mob") != null ? stack.getTag().getString("mob") : "Empty")).setStyle(Style.EMPTY.withColor(0x838383)));
        }
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

}
