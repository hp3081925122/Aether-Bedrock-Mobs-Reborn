package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class CrystaliteSwordItem extends Item {
    public CrystaliteSwordItem(Item.Properties properties) {
        super(properties.sword(AetherTier.create(500, 2.0F, 6.0F, 20), 3.0F, -2.4F));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.aether_bedrock_mobs_reborn.crystalitesword").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionResult result = super.use(level, player, hand);
        if (player.getMainHandItem().is(this)) {
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 280);
        }
        if (player.getMainHandItem().is(this) && level instanceof ServerLevel server) {
            Vec3 center = player.position();
            AABB range = new AABB(center, center).inflate(5.0D);
            for (Entity target : level.getEntitiesOfClass(Entity.class, range, entity -> true)) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(server, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                if (lightning != null) {
                    BlockPos pos = target.blockPosition();
                    lightning.snapTo(pos.getX(), pos.getY(), pos.getZ());
                    server.addFreshEntity(lightning);
                }
                server.sendParticles(ModParticles.RUNESEFFECT.get(), center.x, center.y, center.z, 7, 3.0D, 3.0D, 3.0D, 1.0D);
            }
        }
        return result;
    }
}


