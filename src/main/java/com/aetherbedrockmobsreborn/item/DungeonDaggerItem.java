package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;

public final class DungeonDaggerItem extends SwordItem {
    public DungeonDaggerItem() {
        super(new AetherTier(500, 2.0F, 2.0F, 3, 20), 3, -1.5F, new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        player.getCooldowns().addCooldown(this, 60);
        if (level instanceof ServerLevel server) {
            level.playSound(null, player.blockPosition(), ModSounds.ALL.get("sword").get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
            server.sendParticles(ParticleTypes.ASH, player.getX(), player.getY(), player.getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
            server.sendParticles(ParticleTypes.DOLPHIN, player.getX(), player.getY(), player.getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
        }
        player.push(player.getLookAngle().x * 2.0D, player.getLookAngle().y * 2.0D, player.getLookAngle().z * 2.0D);
        return result;
    }
}
