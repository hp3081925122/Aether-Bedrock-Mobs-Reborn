package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.entity.RebornProjectileEntity;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class PyreSpearItem extends Item {
    public PyreSpearItem(Item.Properties properties) {
        super(properties.durability(400).rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.aether_bedrock_mobs_reborn.pyrespear").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 60;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 1.3F;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getAbilities().instabuild || !findAmmo(player).isEmpty()) {
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        ItemStack ammo = findAmmo(player);
        if (!player.getAbilities().instabuild && ammo.isEmpty()) {
            player.stopUsingItem();
            return;
        }
        RebornProjectileEntity projectile = RebornProjectileEntity.shootShulker(level, player, 3.0F);
        if (projectile == null) {
            return;
        }
        if (player.getAbilities().instabuild) {
            projectile.pickup = net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        if (!player.getAbilities().instabuild) {
            ammo.shrink(1);
        }
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        if (level instanceof ServerLevel server) {
            server.playSound(null, player.blockPosition(), ModSounds.ALL.get("sword").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            player.getCooldowns().addCooldown(stack, 60);
        }
        player.stopUsingItem();
    }

    private ItemStack findAmmo(Player player) {
        ItemStack held = ProjectileWeaponItem.getHeldProjectile(player, stack -> stack.is(Items.SHULKER_SHELL));
        if (!held.isEmpty()) {
            return held;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.SHULKER_SHELL)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}






