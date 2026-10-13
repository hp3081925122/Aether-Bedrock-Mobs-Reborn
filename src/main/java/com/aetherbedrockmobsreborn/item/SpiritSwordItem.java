package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class SpiritSwordItem extends ShieldItem {
    public SpiritSwordItem(Item.Properties properties) {
        super(properties.durability(800)
                .delayedComponent(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS,
                        context -> new net.minecraft.world.item.component.BlocksAttacks(0.25F, 1.0F,
                                java.util.List.of(new net.minecraft.world.item.component.BlocksAttacks.DamageReduction(90.0F, java.util.Optional.empty(), 0.0F, 1.0F)),
                                new net.minecraft.world.item.component.BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                java.util.Optional.of(context.getOrThrow(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD)),
                                java.util.Optional.of(net.minecraft.sounds.SoundEvents.SHIELD_BLOCK),
                                java.util.Optional.of(net.minecraft.sounds.SoundEvents.SHIELD_BREAK)))
                .component(net.minecraft.core.component.DataComponents.BREAK_SOUND, net.minecraft.sounds.SoundEvents.SHIELD_BREAK));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.aether_bedrock_mobs_reborn.spiritsword").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return slot != EquipmentSlot.OFFHAND && super.canEquip(stack, slot, entity);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.FAIL;
        }
        InteractionResult result = super.use(level, player, hand);
        if (!player.getMainHandItem().is(this)) {
            return result;
        }
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 40);
        if (level instanceof ServerLevel server) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 50, 2, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 50, 2, false, false));
            server.sendParticles(ModParticles.WHITESMOKE.get(), player.getX(), player.getY(), player.getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
        }
        return result;
    }
}

