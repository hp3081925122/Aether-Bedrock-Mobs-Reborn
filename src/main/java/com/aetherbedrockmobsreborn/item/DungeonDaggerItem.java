package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class DungeonDaggerItem extends Item {
    public DungeonDaggerItem(Item.Properties properties) {
        super(properties.sword(AetherTier.create(500, 2.0F, 2.0F, 20), 3.0F, -1.5F));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.aether_bedrock_mobs_reborn.dungeondagger").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = super.use(level, player, hand);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 60);
        if (level instanceof ServerLevel server) {
            level.playSound(null, player.blockPosition(), ModSounds.ALL.get("sword").get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            server.sendParticles(ParticleTypes.ASH, player.getX(), player.getY(), player.getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
            server.sendParticles(ParticleTypes.DOLPHIN, player.getX(), player.getY(), player.getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
        }
        player.push(player.getLookAngle().x * 2.0D, player.getLookAngle().y * 2.0D, player.getLookAngle().z * 2.0D);
        return result;
    }
}

