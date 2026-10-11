package com.aetherbedrockmobsreborn.entity;

import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class RebornProjectileEntity extends AbstractArrow {
    private int lifetime;

    public RebornProjectileEntity(EntityType<? extends RebornProjectileEntity> type, Level level) {
        super(type, level);
    }

    public static void shootShalk(Level level, LivingEntity shooter, LivingEntity target) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        EntityType<RebornProjectileEntity> type = ModEntities.PROJECTILES.get("shalkbbbb").get();
        RebornProjectileEntity projectile = type.create(server);
        if (projectile == null) {
            return;
        }
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        var direction = target.getEyePosition().subtract(projectile.position());
        projectile.shoot(direction.x, direction.y, direction.z, 1.6F, 0.0F);
        projectile.setNoGravity(true);
        projectile.setCritArrow(true);
        projectile.setPierceLevel((byte) 1);
        projectile.pickup = AbstractArrow.Pickup.DISALLOWED;
        server.addFreshEntity(projectile);
        server.playSound(null, shooter.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    public static RebornProjectileEntity shootShulker(Level level, LivingEntity shooter, float power) {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        EntityType<RebornProjectileEntity> type = ModEntities.PROJECTILES.get("shulkercrysta").get();
        RebornProjectileEntity projectile = type.create(server);
        if (projectile == null) {
            return null;
        }
        projectile.setOwner(shooter);
        projectile.setPos(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        var direction = shooter.getViewVector(1.0F);
        projectile.shoot(direction.x, direction.y, direction.z, power * 2.0F, 0.0F);
        projectile.setNoGravity(true);
        projectile.setCritArrow(true);
        projectile.setBaseDamage(1.0D);
        projectile.setKnockback(1);
        projectile.pickup = AbstractArrow.Pickup.ALLOWED;
        server.addFreshEntity(projectile);
        server.playSound(null, shooter.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return projectile;
    }

    @Override
    public void tick() {
        super.tick();
        if (++lifetime >= 100) {
            discard();
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return getIdPath().equals("shulkercrysta") ? new ItemStack(Items.SHULKER_SHELL) : new ItemStack(ModItems.ALL.get("pyrespear").get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        triggerImpact();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        triggerImpact();
    }

    private void triggerImpact() {
        if ((!getIdPath().equals("shulkercrysta") && !getIdPath().equals("shalkbbbb")) || !(level() instanceof ServerLevel server)) {
            return;
        }
        server.explode(null, getX(), getY(), getZ(), 0.0F, Level.ExplosionInteraction.NONE);
        server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 3.0D, 3.0D, 3.0D, 1.0D);
        server.sendParticles(ModParticles.FIRE.get(), getX(), getY(), getZ(), 5, 3.0D, 3.0D, 3.0D, 1.0D);
    }

    private String getIdPath() {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(getType());
        return key == null ? "missing" : key.getPath();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
