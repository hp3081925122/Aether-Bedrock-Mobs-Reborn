package com.aetherbedrockmobsreborn.entity;

import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

// 艾瑟龙技能实体：羽刃、陨晶、雷击标记共用
public class DragonSkillEntity extends Entity {
    public static final int FEATHER = 1;
    public static final int CRYSTAL = 2;
    public static final int THUNDER = 3;
    // 雷击：预警时长与雷柱持续时间
    public static final int THUNDER_DELAY = 24;
    public static final int THUNDER_FLASH = 8;
    public static final double CRYSTAL_FALL_SPEED = 1.0D;
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(DragonSkillEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> GROUND_Y = SynchedEntityData.defineId(DragonSkillEntity.class, EntityDataSerializers.FLOAT);
    private int ownerId = -1;
    private int targetId = -1;

    public DragonSkillEntity(EntityType<? extends DragonSkillEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 生成技能实体并加入世界
    public static DragonSkillEntity spawn(ServerLevel level, LivingEntity owner, int kind, Vec3 pos, Vec3 velocity, double groundY, LivingEntity target) {
        DragonSkillEntity entity = ModEntities.DRAGON_SKILL.get().create(level);
        if (entity == null) {
            return null;
        }
        entity.entityData.set(KIND, kind);
        entity.entityData.set(GROUND_Y, (float) groundY);
        entity.ownerId = owner.getId();
        if (target != null) {
            entity.targetId = target.getId();
        }
        entity.setPos(pos.x, pos.y, pos.z);
        entity.setDeltaMovement(velocity);
        entity.faceVelocity();
        level.addFreshEntity(entity);
        return entity;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(KIND, FEATHER);
        this.entityData.define(GROUND_Y, 0.0F);
    }

    public int getKind() {
        return this.entityData.get(KIND);
    }

    public float getGroundY() {
        return this.entityData.get(GROUND_Y);
    }

    @Override
    public void tick() {
        super.tick();
        switch (getKind()) {
            case FEATHER -> tickFeather();
            case CRYSTAL -> tickCrystal();
            default -> tickThunder();
        }
    }

    private void tickFeather() {
        Vec3 velocity = getDeltaMovement();
        if (!level().isClientSide) {
            // 前 15 tick 轻微追踪目标
            Entity target = level().getEntity(this.targetId);
            if (this.tickCount < 15 && target instanceof LivingEntity living && living.isAlive()) {
                Vec3 want = living.getEyePosition().subtract(position()).normalize().scale(velocity.length());
                velocity = velocity.lerp(want, 0.08D);
                setDeltaMovement(velocity);
            }
            Vec3 start = position();
            Vec3 end = start.add(velocity);
            // 先判定方块，再判定实体
            BlockHitResult block = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (block.getType() != HitResult.Type.MISS) {
                end = block.getLocation();
            }
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(level(), this, start, end, getBoundingBox().expandTowards(velocity).inflate(0.6D), this::canHit);
            if (hit != null) {
                hit.getEntity().hurt(skillDamage(), 6.0F);
                burst(hit.getLocation());
                return;
            }
            if (block.getType() != HitResult.Type.MISS || this.tickCount > 80) {
                burst(end);
                return;
            }
        } else if (this.tickCount % 2 == 0) {
            // 客户端拖尾
            level().addParticle(ModParticles.DRAGON_SHARD.get(), getX(), getY(), getZ(), velocity.x * -0.05D, 0.02D, velocity.z * -0.05D);
        }
        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);
        faceVelocity();
    }

    private void tickCrystal() {
        if (level().isClientSide) {
            // 落点收缩预警环，寿命等于下落时间
            if (this.tickCount == 1) {
                int fallTicks = Math.max(4, Mth.ceil((getY() - getGroundY()) / CRYSTAL_FALL_SPEED));
                level().addAlwaysVisibleParticle(ModParticles.DRAGON_WARNING.get(), true, getX(), getGroundY() + 0.1D, getZ(), 3.5D, fallTicks, 0.0D);
            }
            level().addParticle(ModParticles.DRAGON_SHARD.get(), getX() + (this.random.nextDouble() - 0.5D), getY() + 2.5D, getZ() + (this.random.nextDouble() - 0.5D), 0.0D, 0.15D, 0.0D);
        }
        setPos(getX(), getY() - CRYSTAL_FALL_SPEED, getZ());
        if (!level().isClientSide && getY() <= getGroundY()) {
            // 落地爆裂
            damageArea(new Vec3(getX(), getGroundY(), getZ()), 3.0D, 10.0F);
            level().playSound(null, getX(), getGroundY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 2.5F, 0.6F);
            level().playSound(null, getX(), getGroundY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0F, 1.4F);
            level().broadcastEntityEvent(this, (byte) 1);
            discard();
        }
    }

    private void tickThunder() {
        if (level().isClientSide) {
            // 预警：收缩金环 + 上升符文
            if (this.tickCount == 1) {
                level().addAlwaysVisibleParticle(ModParticles.DRAGON_WARNING.get(), true, getX(), getY() + 0.1D, getZ(), 3.0D, THUNDER_DELAY, 1.0D);
            }
            if (this.tickCount < THUNDER_DELAY && this.tickCount % 3 == 0) {
                double angle = this.random.nextDouble() * Math.PI * 2.0D;
                level().addParticle(ModParticles.RUNESEFFECT.get(), getX() + Math.cos(angle) * 2.5D, getY() + 0.2D, getZ() + Math.sin(angle) * 2.5D, 0.0D, 0.06D, 0.0D);
            }
            return;
        }
        if (this.tickCount == THUNDER_DELAY) {
            // 雷击落下
            damageArea(position(), 2.5D, 12.0F);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0F, 1.5F);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 2.0F, 1.0F);
            level().broadcastEntityEvent(this, (byte) 2);
        }
        if (this.tickCount > THUNDER_DELAY + THUNDER_FLASH + 2) {
            discard();
        }
    }

    // 雷柱渲染进度：0 未落下，1 刚落下，逐渐衰减
    public float getThunderFlash(float partialTicks) {
        float age = this.tickCount + partialTicks - THUNDER_DELAY;
        if (age < 0.0F || age > THUNDER_FLASH) {
            return 0.0F;
        }
        return 1.0F - age / THUNDER_FLASH;
    }

    private void burst(Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 1.0F, 1.3F);
        level().broadcastEntityEvent(this, (byte) 3);
        discard();
    }

    private void damageArea(Vec3 center, double radius, float amount) {
        AABB box = new AABB(center, center).inflate(radius, radius + 1.0D, radius);
        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, box, this::canHit)) {
            if (living.position().distanceToSqr(center) <= (radius + 0.5D) * (radius + 0.5D)) {
                living.hurt(skillDamage(), amount);
            }
        }
    }

    private boolean canHit(Entity entity) {
        return entity.isAlive() && entity.isPickable() && entity.getId() != this.ownerId && !(entity instanceof RebornMobEntity);
    }

    private DamageSource skillDamage() {
        return damageSources().indirectMagic(this, level().getEntity(this.ownerId));
    }

    private void faceVelocity() {
        Vec3 v = getDeltaMovement();
        if (v.lengthSqr() < 1.0E-6D) {
            return;
        }
        setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(v.y, v.horizontalDistance()) * Mth.RAD_TO_DEG));
        this.yRotO = getYRot();
        this.xRotO = getXRot();
    }

    @Override
    public void handleEntityEvent(byte id) {
        RandomSource random = this.random;
        switch (id) {
            case 1 -> {
                // 陨晶落地：双层冲击波 + 碎晶
                double gy = getGroundY() + 0.15D;
                level().addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), gy, getZ(), 4.5D, 12.0D, 0.0D);
                level().addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), gy + 0.05D, getZ(), 2.5D, 8.0D, 0.0D);
                for (int i = 0; i < 24; i++) {
                    double angle = random.nextDouble() * Math.PI * 2.0D;
                    level().addParticle(ModParticles.DRAGON_SHARD.get(), getX(), gy + 0.3D, getZ(), Math.cos(angle) * 0.35D, 0.35D + random.nextDouble() * 0.45D, Math.sin(angle) * 0.35D);
                }
                level().addParticle(ParticleTypes.EXPLOSION, getX(), gy + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
            }
            case 2 -> {
                // 雷击：冲击波 + 向上溅射的金屑
                level().addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), getY() + 0.15D, getZ(), 4.0D, 10.0D, 0.0D);
                for (int i = 0; i < 18; i++) {
                    double angle = random.nextDouble() * Math.PI * 2.0D;
                    level().addParticle(ModParticles.DRAGON_SHARD.get(), getX(), getY() + 0.3D, getZ(), Math.cos(angle) * 0.25D, 0.5D + random.nextDouble() * 0.4D, Math.sin(angle) * 0.25D);
                }
            }
            case 3 -> {
                // 羽刃命中：小范围碎鳞
                for (int i = 0; i < 8; i++) {
                    level().addParticle(ModParticles.DRAGON_SHARD.get(), getX(), getY(), getZ(), random.nextGaussian() * 0.15D, 0.1D + random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D);
                }
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
