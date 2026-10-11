package com.aetherbedrockmobsreborn.entity;

import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RebornMobEntity extends PathfinderMob implements GeoEntity, RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DRAGON_PHASE_TWO = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String DRAGON_PHASE_TAG = "reborn_dragon_phase_two";
    // 转阶段状态：同步给客户端，期间无敌且不执行 AI
    private static final EntityDataAccessor<Boolean> DRAGON_TRANSITION = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Logger LOGGER = LogUtils.getLogger();
    // 转阶段时间轴（tick），与 phase_transition 动画一致
    private static final int TRANSITION_TICKS = 215;
    private static final int TRANSITION_DIVE_START = 35;
    private static final int TRANSITION_DIVE_END = 108;
    private int transitionTicks;
    private double transitionStartY;
    private double transitionGroundY;
    // 一阶段技能：当前技能、龙息开关与落点，同步给客户端做表现
    private static final EntityDataAccessor<Integer> DRAGON_SKILL = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRAGON_BEAM = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DRAGON_BEAM_X = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DRAGON_BEAM_Y = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DRAGON_BEAM_Z = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    private static final int SKILL_BREATH = 1;
    private static final int SKILL_FEATHER = 2;
    private static final int SKILL_METEOR = 3;
    private static final int SKILL_THUNDER = 4;
    private static final int SKILL_GALE = 5;
    // 技能时长与动画一致；冷却以 tick 计
    private static final int[] SKILL_DURATION = {0, 80, 50, 60, 60, 40};
    private static final int[] SKILL_COOLDOWN = {0, 220, 70, 280, 180, 120};
    private static final byte EVENT_FEATHER = 90;
    private static final byte EVENT_METEOR = 91;
    private static final byte EVENT_GALE = 92;
    private final int[] skillCooldowns = new int[6];
    private int skillTicks;
    private int skillGap = 40;
    private double orbitAngle;
    private Vec3 beamBase = Vec3.ZERO;
    private Vec3 beamSide = Vec3.ZERO;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossInfo = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);

    public RebornMobEntity(EntityType<? extends RebornMobEntity> type, Level level) {
        super(type, level);
        if (isFlyingEntity()) {
            this.moveControl = new FlyingMoveControl(this, 10, true);
        }
    }

    @Override
    protected void registerGoals() {
        if (getIdPath().equals("aetherdragon")) {
            this.goalSelector.addGoal(5, new DragonFlightStrollGoal(this, 0.8D, 20));
            this.goalSelector.addGoal(5, new DragonGroundStrollGoal(this, 1.0D));
        } else {
            if (isFlyingEntity()) {
                this.goalSelector.addGoal(1, new PanicGoal(this, 1.1D));
                this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D, 20));
            } else {
                this.goalSelector.addGoal(0, new FloatGoal(this));
                this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
            }
        }
        if (getIdPath().equals("aetherdragon")) {
            this.goalSelector.addGoal(7, new FloatGoal(this));
        }
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, getIdPath().equals("aetherdragon") ? 180.0F : 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (this.getType().getCategory() == MobCategory.MONSTER || getIdPath().equals("saltcrab")) {
            if (getIdPath().equals("aetherdragon")) {
                this.goalSelector.addGoal(2, new DragonSkyCombatGoal(this));
                this.goalSelector.addGoal(2, new DragonMeleeAttackGoal(this, 1.0D, true));
            } else if (getIdPath().equals("eldershulker")) {
                this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.25D, 30, 70.0F));
            } else {
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
                if (getIdPath().equals("aether_naga") || getIdPath().equals("spirit")) {
                    this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.5F));
                }
            }
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return isFlyingEntity() ? new FlyingPathNavigation(this, level) : super.createNavigation(level);
    }

    @Override
    public boolean isNoGravity() {
        return isFlyingEntity() || isDragonTransitioning() || super.isNoGravity();
    }

    @Override
    protected boolean isImmobile() {
        // 转阶段期间停止 AI 与移动输入
        return isDragonTransitioning() || super.isImmobile();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 转阶段期间无敌，/kill 之类的绕过无敌伤害除外
        if (isDragonTransitioning() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    public boolean isPushable() {
        return !getIdPath().equals("aetherdragon") && super.isPushable();
    }

    @Override
    protected void doPush(Entity entity) {
        if (!getIdPath().equals("aetherdragon")) {
            super.doPush(entity);
        }
    }

    @Override
    protected void pushEntities() {
        if (!getIdPath().equals("aetherdragon")) {
            super.pushEntities();
        }
    }

    @Override
    public boolean canChangeDimensions() {
        return getIdPath().equals("aetherdragon") ? false : super.canChangeDimensions();
    }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) {
        if (!isDragonPhaseOne()) {
            super.checkFallDamage(distance, onGround, state, pos);
        }
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(isDragonPhaseOne() || noGravity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (isDragonPhaseOne()) {
            setNoGravity(true);
            if (!level().isClientSide && isAlive() && getHealth() <= 300.0F) {
                enterDragonPhaseTwo();
            }
        }
        if (level().isClientSide && getDragonSkill() == SKILL_BREATH) {
            RandomSource random = getRandom();
            if (isDragonBeamActive()) {
                // 光束落点：碎光迸溅与小冲击环
                Vec3 end = getDragonBeamEnd();
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ModParticles.DRAGON_SHARD.get(), end.x, end.y + 0.2D, end.z, random.nextGaussian() * 0.2D, 0.3D + random.nextDouble() * 0.3D, random.nextGaussian() * 0.2D);
                }
                level().addParticle(ModParticles.WHITESMOKE.get(), end.x, end.y + 0.3D, end.z, 0.0D, 0.05D, 0.0D);
                if (this.tickCount % 5 == 0) {
                    level().addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, end.x, end.y + 0.1D, end.z, 3.0D, 8.0D, 0.0D);
                }
            } else {
                // 蓄力：碎光向口部汇聚
                Vec3 mouth = getDragonMouth();
                for (int i = 0; i < 3; i++) {
                    Vec3 from = mouth.add(random.nextGaussian() * 2.5D, random.nextGaussian() * 2.5D, random.nextGaussian() * 2.5D);
                    Vec3 velocity = mouth.subtract(from).scale(0.18D);
                    level().addParticle(ModParticles.DRAGON_SHARD.get(), from.x, from.y, from.z, velocity.x, velocity.y, velocity.z);
                }
            }
        }
        if (!level().isClientSide && isDragonTransitioning()) {
            this.transitionTicks++;
            // 俯冲段：按缓动曲线把龙从空中带到地面
            if (this.transitionTicks >= TRANSITION_DIVE_START && this.transitionTicks <= TRANSITION_DIVE_END) {
                float progress = (float) (this.transitionTicks - TRANSITION_DIVE_START) / (TRANSITION_DIVE_END - TRANSITION_DIVE_START);
                float eased = progress * progress * (3.0F - 2.0F * progress);
                double targetY = Mth.lerp(eased, this.transitionStartY, this.transitionGroundY);
                move(MoverType.SELF, new Vec3(0.0D, targetY - getY(), 0.0D));
            }
            setDeltaMovement(Vec3.ZERO);
            // 动画结束：解除无敌，恢复重力
            if (this.transitionTicks >= TRANSITION_TICKS) {
                this.entityData.set(DRAGON_TRANSITION, false);
                super.setNoGravity(false);
                LOGGER.debug("Aether dragon {} finished phase transition at y={}", getId(), getY());
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        if (!level().isClientSide && isDragonPhaseOne() && amount >= getHealth() + getAbsorptionAmount()) {
            float safeDamage = Math.max(0.001F, getHealth() - 0.001F);
            boolean hurt = super.hurt(source, safeDamage);
            if (hurt && isDragonPhaseOne()) {
                enterDragonPhaseTwo();
            }
            return hurt;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && isDragonPhaseOne()) {
            enterDragonPhaseTwo();
            return;
        }
        super.die(source);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DRAGON_PHASE_TWO, false);
        this.entityData.define(DRAGON_TRANSITION, false);
        this.entityData.define(DRAGON_SKILL, 0);
        this.entityData.define(DRAGON_BEAM, false);
        this.entityData.define(DRAGON_BEAM_X, 0.0F);
        this.entityData.define(DRAGON_BEAM_Y, 0.0F);
        this.entityData.define(DRAGON_BEAM_Z, 0.0F);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (isBossEntity()) {
            bossInfo.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (isBossEntity()) {
            bossInfo.removePlayer(player);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isBossEntity()) {
            bossInfo.setProgress(getHealth() / getMaxHealth());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getSound("ambient");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return getSound("hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getSound("death");
    }
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        ItemLike drop = switch (getIdPath()) {
            case "aether_naga" -> ModItems.ALL.get("dungeondagger").get();
            case "crystalitegolem" -> ModItems.ALL.get("crystalitesword").get();
            case "crystaliteminion" -> ModItems.ALL.get("crystalite").get();
            case "eldershulker" -> ModItems.ALL.get("pyrespear").get();
            case "mutatedcritter" -> Items.ROTTEN_FLESH;
            case "percipitus" -> Items.BONE;
            case "saltcrab" -> ModItems.ALL.get("crabmeat").get();
            case "saltcrabking" -> ModItems.ALL.get("saltinstrument").get();
            case "shimaenaga" -> Items.FEATHER;
            case "spirit" -> ModItems.ALL.get("spiritsword").get();
            default -> null;
        };
        if (drop != null) {
            this.spawnAtLocation(new ItemStack(drop));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("reborn_texture", getTextureName());
        if (getIdPath().equals("aetherdragon")) {
            tag.putBoolean(DRAGON_PHASE_TAG, isDragonPhaseTwo());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (getIdPath().equals("aetherdragon") && tag.getBoolean(DRAGON_PHASE_TAG)) {
            this.entityData.set(DRAGON_PHASE_TWO, true);
            applyDragonPhaseTwoAttributes();
            this.navigation = new GroundPathNavigation(this, level());
            this.moveControl = new MoveControl(this);
            super.setNoGravity(false);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<RebornMobEntity> movement = new AnimationController<>(this, "movement", 4, this::movementPredicate);
        movement.setParticleKeyframeHandler(this::particleKeyframe);
        movement.setSoundKeyframeHandler(this::soundKeyframe);
        controllers.add(movement);
        AnimationController<RebornMobEntity> action = new AnimationController<>(this, "action", 0, state -> PlayState.STOP);
        action.setParticleKeyframeHandler(this::particleKeyframe);
        action.setSoundKeyframeHandler(this::soundKeyframe);
        registerActionAnimations(action);
        controllers.add(action);
    }

    private void particleKeyframe(ParticleKeyframeEvent<RebornMobEntity> event) {
        String effect = event.getKeyframeData().getEffect();
        if (effect.startsWith("phase_")) {
            spawnDragonPhaseEffect(effect);
            return;
        }
        ParticleOptions particle = switch (effect) {
            case "summoning", "activate" -> ModParticles.RUNESEFFECT.get();
            case "death", "smash", "groundslam" -> ParticleTypes.EXPLOSION;
            case "hurt", "angry", "roar" -> ModParticles.WHITESMOKE.get();
            case "trail" -> ModParticles.FIRE.get();
            case "dig" -> ParticleTypes.POOF;
            default -> null;
        };
        if (particle != null) {
            level().addParticle(particle, getX(), getY() + getBbHeight() * 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnDragonPhaseEffect(String effect) {
        RandomSource random = getRandom();
        Level level = level();
        switch (effect) {
            case "phase_break" -> {
                // 受创：躯干处碎鳞四散，伴随一圈收束的冲击环
                double cy = getY() + 2.5D;
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), cy, getZ(), 7.0D, 14.0D, 0.0D);
                for (int i = 0; i < 36; i++) {
                    double yaw = random.nextDouble() * Math.PI * 2.0D;
                    double pitch = (random.nextDouble() - 0.3D) * Math.PI * 0.6D;
                    double speed = 0.35D + random.nextDouble() * 0.35D;
                    level.addParticle(ModParticles.DRAGON_SHARD.get(), getX(), cy, getZ(), Math.cos(yaw) * Math.cos(pitch) * speed, Math.sin(pitch) * speed + 0.15D, Math.sin(yaw) * Math.cos(pitch) * speed);
                }
                for (int i = 0; i < 6; i++) {
                    level.addParticle(ModParticles.WHITESMOKE.get(), getX() + random.nextGaussian(), cy + random.nextGaussian(), getZ() + random.nextGaussian(), 0.0D, 0.02D, 0.0D);
                }
            }
            case "phase_impact" -> {
                // 落地：贴地双层冲击波 + 从地面崩起的碎鳞与尘烟
                double gy = getY() + 0.15D;
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), gy, getZ(), 14.0D, 22.0D, 0.0D);
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), gy + 0.05D, getZ(), 8.0D, 14.0D, 0.0D);
                for (int i = 0; i < 40; i++) {
                    double angle = random.nextDouble() * Math.PI * 2.0D;
                    double dx = Math.cos(angle);
                    double dz = Math.sin(angle);
                    level.addParticle(ModParticles.DRAGON_SHARD.get(), getX() + dx * 2.5D, gy + 0.3D, getZ() + dz * 2.5D, dx * (0.3D + random.nextDouble() * 0.3D), 0.4D + random.nextDouble() * 0.4D, dz * (0.3D + random.nextDouble() * 0.3D));
                }
                for (int i = 0; i < 20; i++) {
                    double angle = Math.PI * 2.0D * i / 20.0D;
                    level.addParticle(ModParticles.WHITESMOKE.get(), getX() + Math.cos(angle) * 3.0D, gy + 0.4D, getZ() + Math.sin(angle) * 3.0D, Math.cos(angle) * 0.25D, 0.03D, Math.sin(angle) * 0.25D);
                }
            }
            case "phase_roar" -> {
                // 咆哮：口部高度的水平声浪环，向前喷出金色碎屑
                float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                double fx = -Mth.sin(yaw);
                double fz = Mth.cos(yaw);
                double hx = getX() + fx * 4.0D;
                double hy = getY() + 5.5D;
                double hz = getZ() + fz * 4.0D;
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, hx, hy, hz, 11.0D, 18.0D, 0.0D);
                for (int i = 0; i < 10; i++) {
                    double spread = (random.nextDouble() - 0.5D) * 0.5D;
                    level.addParticle(ModParticles.DRAGON_SHARD.get(), hx, hy, hz, (fx + spread * fz) * 0.6D, 0.1D + random.nextDouble() * 0.2D, (fz - spread * fx) * 0.6D);
                }
            }
            default -> {
            }
        }
    }

    private void soundKeyframe(SoundKeyframeEvent<RebornMobEntity> event) {
        if (!level().isClientSide) {
            return;
        }
        String soundId = switch (getIdPath()) {
            case "aetherdragon" -> switch (event.getKeyframeData().getSound()) {
                case "flying.idle" -> "drw";
                case "landing" -> "dri";
                case "attack" -> "drf";
                // 转阶段：受创惨叫与落地后咆哮
                case "phase_hurt" -> "drh";
                case "phase_roar" -> "drw";
                default -> null;
            };
            case "crystalitegolem" -> switch (event.getKeyframeData().getSound()) {
                case "groundslam" -> "cgmd";
                case "roar" -> "cgmh";
                case "swing" -> "cgmi";
                case "summoning", "activate" -> "cgmw";
                case "death" -> "cgd";
                default -> null;
            };
            case "crystaliteminion" -> event.getKeyframeData().getSound().equals("attack") ? "cgi" : null;
            case "aether_naga" -> event.getKeyframeData().getSound().equals("scream") ? "nad" : null;
            case "saltcrab" -> switch (event.getKeyframeData().getSound()) {
                case "jump" -> "sabw";
                case "ree" -> "sah";
                default -> null;
            };
            case "mutatedcritter" -> switch (event.getKeyframeData().getSound()) {
                case "pre_attack" -> "mui";
                case "angry" -> "muh";
                default -> null;
            };
            default -> null;
        };
        if (soundId != null) {
            var sound = ModSounds.ALL.get(soundId);
            if (sound != null) {
                // 咆哮压低音调、放大音量
                float volume = 1.0F;
                float pitch = 1.0F;
                if (event.getKeyframeData().getSound().equals("phase_roar")) {
                    volume = 4.0F;
                    pitch = 0.6F;
                }
                // 客户端需用本地播放，playSound(null, ...) 在客户端不会出声
                level().playLocalSound(getX(), getY(), getZ(), sound.get(), SoundSource.HOSTILE, volume, pitch, false);
            }
        }
    }

    private void registerActionAnimations(AnimationController<RebornMobEntity> controller) {
        for (String action : new String[]{"attack", "hurt", "death", "summoning", "roar", "groundslam", "activate", "jump", "spawn", "phase"}) {
            String animation = getActionAnimation(action);
            if (!animation.isEmpty()) {
                controller.triggerableAnim(action, RawAnimation.begin().thenPlay(animation));
            }
        }
    }

    private String getActionAnimation(String action) {
        return switch (getIdPath()) {
            case "crystalitegolem" -> switch (action) {
                case "attack" -> "animation.crystalite_golem.swing";
                case "death", "summoning", "roar", "groundslam", "activate" -> "animation.crystalite_golem." + action;
                default -> "";
            };
            case "crystaliteminion" -> switch (action) {
                case "spawn", "attack" -> "animation.crystalite_minion." + action;
                default -> "";
            };
            case "aetherdragon" -> switch (action) {
                // 一阶段转二阶段的完整演出
                case "phase" -> "animation.aether_dragon.phase_transition";
                default -> "";
            };
            case "aether_naga" -> switch (action) {
                case "attack", "death" -> "animation.slider." + action;
                default -> "";
            };
            case "guardianangel" -> action.equals("attack") ? "animation.guardian_angel.attack" : "";
            case "saltcrab" -> switch (action) {
                case "summon", "jump", "attack" -> "animation.salt_crab." + action;
                default -> "";
            };
            case "mutatedcritter" -> switch (action) {
                case "hurt", "angry", "attack", "jump", "death" -> "animation.mutated_critter." + action;
                default -> "";
            };
            default -> "";
        };
    }

    public void triggerAction(String action) {
        if (!getActionAnimation(action).isEmpty()) {
            triggerAnim("action", action);
        }
    }

    @Override
    public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float distanceFactor) {
        RebornProjectileEntity.shootShalk(level(), this, target);
        triggerAction("attack");
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return isDragonPhaseOne() ? false : super.causeFallDamage(distance, multiplier, source);
    }

    private PlayState movementPredicate(AnimationState<RebornMobEntity> state) {
        // 一阶段技能动画以飞行待机为底，放在移动控制器里与待机平滑过渡
        String skill = switch (getDragonSkill()) {
            case SKILL_BREATH -> "animation.aether_dragon.skill_breath";
            case SKILL_FEATHER -> "animation.aether_dragon.skill_feather";
            case SKILL_METEOR -> "animation.aether_dragon.skill_meteor";
            case SKILL_THUNDER -> "animation.aether_dragon.skill_thunder";
            case SKILL_GALE -> "animation.aether_dragon.skill_gale";
            default -> "";
        };
        if (!skill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(skill));
        }
        String animation = getMovementAnimation(state.isMoving());
        return animation.isEmpty() ? PlayState.STOP : state.setAndContinue(RawAnimation.begin().thenLoop(animation));
    }

    private String getMovementAnimation(boolean moving) {
        return switch (getIdPath()) {
            case "aetherdragon" -> isDragonPhaseTwo() ? (moving ? "animation.aether_dragon.walk" : "animation.aether_dragon.idle") : "animation.aether_dragon.idle.flying";
            case "aether_naga" -> moving ? "animation.slider.move" : "animation.slider.idle";
            case "crystalitegolem" -> moving ? "animation.crystalite_golem.walk" : "animation.crystalite_golem.idle1";
            case "crystaliteminion" -> moving ? "animation.crystalite_minion.walk" : "animation.crystalite_minion.idle";
            case "guardianangel" -> moving ? "animation.guardian_angel.move" : "animation.guardian_angel.idle";
            case "mutatedcritter" -> moving ? "animation.mutated_critter.walk" : "animation.mutated_critter.idle";
            case "percipitus" -> moving ? "animation.percipitus.fly" : "animation.percipitus.idle";
            case "saltcrab" -> moving ? "animation.salt_crab.walk" : "animation.salt_crab.idle2";
            case "shimaenaga" -> moving ? "animation.shima_enaga.walk" : "animation.shima_enaga.idle2";
            case "twinkler", "buterfly_2", "buterfly_3", "buterfly_4", "buterfly_5", "buterfly_6", "buterfly7", "buterfly_8", "buterfly_9", "buterfly_10", "buterfly_11", "buterfly_12", "buterfly_13" -> moving ? "animation.twinkler.fly" : "animation.twinkler.idle";
            default -> "";
        };
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    public String getIdPath() {
        var key = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        return key == null ? "missing" : key.getPath();
    }

    public String getTextureName() {
        return switch (getIdPath()) {
            case "aetherdragon" -> "dragon";
            case "aether_naga" -> "slider";
            case "buterfly_2" -> "black";
            case "buterfly_3" -> "blue";
            case "buterfly_4" -> "cloud";
            case "buterfly_5" -> "cyan";
            case "buterfly_6" -> "dark";
            case "buterfly7" -> "dark_blue";
            case "buterfly_8" -> "gold";
            case "buterfly_9" -> "green";
            case "buterfly_10" -> "orange";
            case "buterfly_11" -> "purple";
            case "buterfly_12" -> "red_large";
            case "buterfly_13" -> "yellow";
            case "crystalitegolem" -> "crystalite_golem";
            case "crystaliteminion" -> "crystalite_minion";
            case "eldershulker" -> "elder_shulker";
            case "guardianangel" -> "ggguar";
            case "mutatedcritter" -> "mutated_critter";
            case "percipitus" -> "percipitus";
            case "saltcrab" -> "salt_crab";
            case "saltcrabking" -> "salt_crab_king";
            case "shimaenaga" -> "shima_enaga";
            case "twinkler" -> "aurora_quartz";
            default -> getIdPath();
        };
    }

    private boolean isBossEntity() {
        return switch (getIdPath()) {
            case "aether_naga", "aetherdragon", "crystalitegolem", "eldershulker", "saltcrabking", "spirit" -> true;
            default -> false;
        };
    }

    private SoundEvent getSound(String kind) {
        String id = getIdPath();
        if (id.equals("percipitus")) {
            return switch (kind) {
                case "ambient" -> SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
                case "hurt" -> SoundEvents.ALLAY_HURT;
                default -> SoundEvents.ALLAY_DEATH;
            };
        }
        String sound = switch (id) {
            case "aether_naga" -> kind.equals("ambient") ? "naw" : kind.equals("hurt") ? "nah" : "nad";
            case "crystalitegolem" -> kind.equals("ambient") ? "cgw" : kind.equals("hurt") ? "cgh" : "cgd";
            case "guardianangel" -> kind.equals("ambient") ? "gaw" : kind.equals("hurt") ? "gah" : "gad";
            case "mutatedcritter" -> kind.equals("ambient") ? "muw" : kind.equals("hurt") ? "muh" : "mud";
            case "saltcrab" -> kind.equals("ambient") ? "saw" : "sah";
            case "spirit" -> kind.equals("ambient") ? "spw" : kind.equals("hurt") ? "sph" : "spd";
            case "aetherdragon" -> kind.equals("ambient") ? "drw" : kind.equals("hurt") ? "drh" : "drd";
            case "eldershulker" -> kind.equals("ambient") ? "shi" : kind.equals("hurt") ? "shh" : "shd";
            default -> null;
        };
        return sound == null ? null : ModSounds.ALL.get(sound).get();
    }
    private boolean isFlyingEntity() {
        String id = getIdPath();
        return isDragonPhaseOne() || id.equals("percipitus") || id.equals("shimaenaga") || id.equals("twinkler") || id.startsWith("buterfly_") || id.equals("buterfly7");
    }

    public static AttributeSupplier.Builder createAttributes(String id) {
        double speed = 0.3D;
        double health = 10.0D;
        double armor = 0.0D;
        double knockbackResistance = 0.0D;
        double attackKnockback = 0.0D;
        double damage = 3.0D;
        double followRange = 16.0D;
        double flyingSpeed = 0.0D;
        if (id.equals("aetherdragon")) {
            speed = 0.0D;
            health = 600.0D;
            armor = 0.2D;
            damage = 3.0D;
            followRange = 50.0D;
            knockbackResistance = 2.5D;
        } else if (id.equals("aether_naga")) {
            health = 350.0D;
            armor = 0.1D;
            damage = 6.0D;
            followRange = 20.0D;
            attackKnockback = 0.1D;
        } else if (id.equals("spirit")) {
            speed = 0.4D;
            health = 300.0D;
            armor = 0.3D;
            knockbackResistance = 0.1D;
            damage = 11.0D;
            followRange = 30.0D;
            attackKnockback = 0.2D;
        } else if (id.equals("saltcrabking")) {
            speed = 0.23D;
            health = 250.0D;
            armor = 0.2D;
            damage = 15.0D;
            followRange = 35.0D;
            attackKnockback = 0.2D;
            knockbackResistance = 1.0D;
        } else if (id.equals("eldershulker")) {
            speed = 0.0D;
            health = 400.0D;
            armor = 1.5D;
            knockbackResistance = 1.0D;
            damage = 5.0D;
            followRange = 40.0D;
            attackKnockback = 0.2D;
        } else if (id.equals("crystalitegolem")) {
            speed = 0.2D;
            health = 300.0D;
            armor = 0.2D;
            damage = 8.0D;
            followRange = 12.0D;
            attackKnockback = 0.3D;
        } else if (id.equals("crystaliteminion")) {
            speed = 0.2D;
            health = 20.0D;
            damage = 5.0D;
        } else if (id.equals("mutatedcritter")) {
            health = 12.0D;
            damage = 5.0D;
        } else if (id.equals("saltcrab")) {
            health = 6.0D;
            damage = 3.0D;
            knockbackResistance = 2.0D;
        } else if (id.equals("guardianangel")) {
            health = 7.0D;
        } else if (id.equals("percipitus") || id.equals("shimaenaga")) {
            health = 10.0D;
            flyingSpeed = 0.3D;
        } else if (id.startsWith("buterfly") || id.equals("twinkler")) {
            health = 1.0D;
            flyingSpeed = 0.3D;
        }
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ATTACK_KNOCKBACK, attackKnockback)
                .add(Attributes.FOLLOW_RANGE, followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.FLYING_SPEED, flyingSpeed);
    }

    public boolean isDragonPhaseTwo() {
        return getIdPath().equals("aetherdragon") && this.entityData.get(DRAGON_PHASE_TWO);
    }

    private boolean isDragonPhaseOne() {
        return getIdPath().equals("aetherdragon") && !isDragonPhaseTwo();
    }

    private void enterDragonPhaseTwo() {
        if (!getIdPath().equals("aetherdragon") || isDragonPhaseTwo()) {
            return;
        }
        this.entityData.set(DRAGON_PHASE_TWO, true);
        stopDragonSkill();
        applyDragonPhaseTwoAttributes();
        setHealth(300.0F);
        setTarget(null);
        this.navigation.stop();
        this.navigation = new GroundPathNavigation(this, level());
        this.moveControl = new MoveControl(this);
        // 进入转阶段：记录俯冲起点与下方地面，保持无重力直到动画结束
        this.entityData.set(DRAGON_TRANSITION, true);
        this.transitionTicks = 0;
        this.transitionStartY = getY();
        this.transitionGroundY = getY() - 64.0D;
        Vec3 from = position();
        BlockHitResult ground = level().clip(new ClipContext(from, from.subtract(0.0D, 64.0D, 0.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (ground.getType() == HitResult.Type.BLOCK) {
            this.transitionGroundY = ground.getLocation().y;
        }
        setDeltaMovement(Vec3.ZERO);
        super.setNoGravity(true);
        triggerAction("phase");
        LOGGER.debug("Aether dragon {} started phase transition: startY={}, groundY={}, groundHit={}", getId(), this.transitionStartY, this.transitionGroundY, ground.getType());
    }

    public boolean isDragonTransitioning() {
        return getIdPath().equals("aetherdragon") && this.entityData.get(DRAGON_TRANSITION);
    }

    private void applyDragonPhaseTwoAttributes() {
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        }
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        }
        if (getAttribute(Attributes.KNOCKBACK_RESISTANCE) != null) {
            getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.2D);
        }
    }

    public int getDragonSkill() {
        if (!getIdPath().equals("aetherdragon")) {
            return 0;
        }
        return this.entityData.get(DRAGON_SKILL);
    }

    public boolean isDragonBeamActive() {
        return getIdPath().equals("aetherdragon") && this.entityData.get(DRAGON_BEAM);
    }

    public Vec3 getDragonBeamEnd() {
        return new Vec3(this.entityData.get(DRAGON_BEAM_X), this.entityData.get(DRAGON_BEAM_Y), this.entityData.get(DRAGON_BEAM_Z));
    }

    // 口部大致位置：前 5 格、上 3.2 格（来自 BB 中龙息姿态的下颚坐标）
    private Vec3 getDragonMouth() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return position().add(-Mth.sin(yaw) * 5.0D, 3.2D, Mth.cos(yaw) * 5.0D);
    }

    private void faceDragonTowards(double x, double z, float maxStep) {
        float yaw = (float) (Mth.atan2(z - getZ(), x - getX()) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(Mth.approachDegrees(getYRot(), yaw, maxStep));
        this.yBodyRot = getYRot();
        this.yHeadRot = getYRot();
    }

    // 向下寻找地面高度，找不到时返回 fallback
    private double findGroundY(double x, double fromY, double z, double depth, double fallback) {
        Vec3 from = new Vec3(x, fromY, z);
        BlockHitResult hit = level().clip(new ClipContext(from, from.subtract(0.0D, depth, 0.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation().y;
        }
        return fallback;
    }

    private void stopDragonSkill() {
        this.skillTicks = 0;
        this.entityData.set(DRAGON_SKILL, 0);
        this.entityData.set(DRAGON_BEAM, false);
    }

    private boolean canDragonSkillHit(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof RebornMobEntity);
    }

    // 一阶段空战：绕目标盘旋，按冷却挑选技能并执行时间轴
    private void tickSkyCombat(LivingEntity target) {
        for (int i = 1; i < this.skillCooldowns.length; i++) {
            if (this.skillCooldowns[i] > 0) {
                this.skillCooldowns[i]--;
            }
        }
        int skill = getDragonSkill();
        if (skill != SKILL_BREATH) {
            faceDragonTowards(target.getX(), target.getZ(), 8.0F);
        }
        if (skill == 0) {
            // 盘旋：目标上方 10 格、半径 16 格
            this.orbitAngle += 0.012D;
            Vec3 hover = new Vec3(target.getX() + Math.cos(this.orbitAngle) * 16.0D, target.getY() + 10.0D, target.getZ() + Math.sin(this.orbitAngle) * 16.0D);
            Vec3 want = hover.subtract(position());
            double distance = want.length();
            Vec3 velocity = Vec3.ZERO;
            if (distance > 0.5D) {
                velocity = want.scale(Math.min(0.45D, distance * 0.05D) / distance);
            }
            setDeltaMovement(getDeltaMovement().lerp(velocity, 0.12D));
            if (this.skillGap > 0) {
                this.skillGap--;
                return;
            }
            // 选技能：贴脸优先飓风，其余在就绪技能中随机
            int chosen = 0;
            if (distanceTo(target) < 10.0D && this.skillCooldowns[SKILL_GALE] <= 0) {
                chosen = SKILL_GALE;
            } else {
                List<Integer> ready = new ArrayList<>();
                for (int i = SKILL_BREATH; i <= SKILL_THUNDER; i++) {
                    if (this.skillCooldowns[i] <= 0) {
                        ready.add(i);
                    }
                }
                if (!ready.isEmpty()) {
                    chosen = ready.get(getRandom().nextInt(ready.size()));
                }
            }
            if (chosen == 0) {
                return;
            }
            this.skillCooldowns[chosen] = SKILL_COOLDOWN[chosen];
            this.skillTicks = 0;
            this.entityData.set(DRAGON_SKILL, chosen);
            if (chosen == SKILL_BREATH) {
                // 龙息扫射基准：目标脚下，横扫方向垂直于龙到目标的连线
                this.beamBase = target.position().add(0.0D, 0.5D, 0.0D);
                Vec3 toward = target.position().subtract(position());
                this.beamSide = new Vec3(-toward.z, 0.0D, toward.x).normalize();
            }
            LOGGER.debug("Aether dragon {} casts skill {} at distance {}", getId(), chosen, distanceTo(target));
            return;
        }
        // 施法时减速悬停
        setDeltaMovement(getDeltaMovement().scale(0.8D));
        this.skillTicks++;
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        switch (skill) {
            case SKILL_BREATH -> {
                // 天穹龙息：0-20 蓄力，20-68 横扫光束
                if (this.skillTicks == 1) {
                    server.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 3.0F, 0.5F);
                }
                if (this.skillTicks < 20) {
                    faceDragonTowards(target.getX(), target.getZ(), 8.0F);
                }
                if (this.skillTicks == 20) {
                    this.entityData.set(DRAGON_BEAM, true);
                    server.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.HOSTILE, 4.0F, 0.7F);
                }
                if (this.skillTicks >= 20 && this.skillTicks <= 68) {
                    this.beamBase = this.beamBase.lerp(target.position().add(0.0D, 0.5D, 0.0D), 0.06D);
                    double sweep = (this.skillTicks - 20) / 48.0D * 2.0D - 1.0D;
                    Vec3 aim = this.beamBase.add(this.beamSide.scale(sweep * 7.0D));
                    faceDragonTowards(aim.x, aim.z, 4.0F);
                    Vec3 mouth = getDragonMouth();
                    Vec3 far = mouth.add(aim.subtract(mouth).scale(1.6D));
                    BlockHitResult hit = level().clip(new ClipContext(mouth, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    Vec3 end = far;
                    if (hit.getType() != HitResult.Type.MISS) {
                        end = hit.getLocation();
                    }
                    this.entityData.set(DRAGON_BEAM_X, (float) end.x);
                    this.entityData.set(DRAGON_BEAM_Y, (float) end.y);
                    this.entityData.set(DRAGON_BEAM_Z, (float) end.z);
                    // 每 4 tick 结算一次光束沿线伤害
                    if (this.skillTicks % 4 == 0) {
                        Vec3 segment = end.subtract(mouth);
                        double lengthSqr = Math.max(1.0E-4D, segment.lengthSqr());
                        for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, new AABB(mouth, end).inflate(1.6D), this::canDragonSkillHit)) {
                            Vec3 center = living.getBoundingBox().getCenter();
                            double t = Mth.clamp(center.subtract(mouth).dot(segment) / lengthSqr, 0.0D, 1.0D);
                            if (mouth.add(segment.scale(t)).distanceToSqr(center) < 2.6D) {
                                living.hurt(damageSources().indirectMagic(this, this), 5.0F);
                            }
                        }
                    }
                    if (this.skillTicks % 12 == 0) {
                        server.playSound(null, end.x, end.y, end.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.0F, 0.7F);
                    }
                }
                if (this.skillTicks == 69) {
                    this.entityData.set(DRAGON_BEAM, false);
                }
            }
            case SKILL_FEATHER -> {
                // 风暴羽刃：双翼前扫时射出 9 片扇形羽刃
                if (this.skillTicks == 18) {
                    Vec3 origin = position().add(0.0D, 2.5D, 0.0D).add(forward.scale(2.5D));
                    Vec3 aim = target.getEyePosition().subtract(origin).normalize();
                    for (int i = -4; i <= 4; i++) {
                        Vec3 velocity = aim.yRot(i * 8.0F * Mth.DEG_TO_RAD).scale(1.3D);
                        DragonSkillEntity.spawn(server, this, DragonSkillEntity.FEATHER, origin, velocity, 0.0D, target);
                    }
                    server.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 4.0F, 0.8F);
                    level().broadcastEntityEvent(this, EVENT_FEATHER);
                }
            }
            case SKILL_METEOR -> {
                // 苍穹陨晶：咆哮后在目标周围依次落下 7 根晶矛
                if (this.skillTicks == 12) {
                    var roar = ModSounds.ALL.get("drw");
                    if (roar != null) {
                        server.playSound(null, getX(), getY(), getZ(), roar.get(), SoundSource.HOSTILE, 4.0F, 0.7F);
                    }
                    level().broadcastEntityEvent(this, EVENT_METEOR);
                }
                if (this.skillTicks >= 20 && this.skillTicks <= 56 && (this.skillTicks - 20) % 6 == 0) {
                    double x = target.getX() + target.getDeltaMovement().x * 10.0D;
                    double z = target.getZ() + target.getDeltaMovement().z * 10.0D;
                    if (this.skillTicks > 20) {
                        x = target.getX() + (getRandom().nextDouble() * 2.0D - 1.0D) * 7.0D;
                        z = target.getZ() + (getRandom().nextDouble() * 2.0D - 1.0D) * 7.0D;
                    }
                    double groundY = findGroundY(x, target.getY() + 6.0D, z, 30.0D, target.getY());
                    DragonSkillEntity.spawn(server, this, DragonSkillEntity.CRYSTAL, new Vec3(x, groundY + 26.0D, z), Vec3.ZERO, groundY, target);
                }
            }
            case SKILL_THUNDER -> {
                // 天雷审判：连续三次标记目标当前位置
                if (this.skillTicks == 12 || this.skillTicks == 24 || this.skillTicks == 36) {
                    double groundY = findGroundY(target.getX(), target.getY() + 1.0D, target.getZ(), 12.0D, target.getY());
                    DragonSkillEntity.spawn(server, this, DragonSkillEntity.THUNDER, new Vec3(target.getX(), groundY, target.getZ()), Vec3.ZERO, groundY, target);
                    server.playSound(null, target.getX(), groundY, target.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.8F);
                }
            }
            case SKILL_GALE -> {
                // 龙翼飓风：下拍瞬间击退并造成少量伤害
                if (this.skillTicks == 14) {
                    for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(12.0D), this::canDragonSkillHit)) {
                        Vec3 push = living.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
                        if (push.lengthSqr() < 1.0E-4D || living.distanceToSqr(this) > 144.0D) {
                            continue;
                        }
                        push = push.normalize();
                        living.hurt(damageSources().mobAttack(this), 4.0F);
                        living.setDeltaMovement(living.getDeltaMovement().add(push.x * 2.2D, 0.7D, push.z * 2.2D));
                        living.hurtMarked = true;
                    }
                    server.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 5.0F, 0.5F);
                    level().broadcastEntityEvent(this, EVENT_GALE);
                }
            }
            default -> {
            }
        }
        if (this.skillTicks >= SKILL_DURATION[skill]) {
            stopDragonSkill();
            this.skillGap = 25;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id != EVENT_FEATHER && id != EVENT_METEOR && id != EVENT_GALE) {
            super.handleEntityEvent(id);
            return;
        }
        RandomSource random = getRandom();
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        double fx = -Mth.sin(yaw);
        double fz = Mth.cos(yaw);
        Level level = level();
        switch (id) {
            case EVENT_FEATHER -> {
                // 羽刃齐射：胸前冲击环 + 向前喷散的碎鳞
                double cx = getX() + fx * 2.5D;
                double cy = getY() + 2.5D;
                double cz = getZ() + fz * 2.5D;
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, cx, cy, cz, 5.0D, 10.0D, 0.0D);
                for (int i = 0; i < 20; i++) {
                    level.addParticle(ModParticles.DRAGON_SHARD.get(), cx, cy, cz, fx * 0.6D + random.nextGaussian() * 0.2D, 0.1D + random.nextDouble() * 0.2D, fz * 0.6D + random.nextGaussian() * 0.2D);
                }
            }
            case EVENT_METEOR -> {
                // 陨晶召唤：头顶天空展开三层金环，碎光冲天
                for (int i = 0; i < 3; i++) {
                    level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), getY() + 10.0D + i * 5.0D, getZ(), 8.0D + i * 4.0D, 20.0D + i * 6.0D, 0.0D);
                }
                for (int i = 0; i < 24; i++) {
                    level.addParticle(ModParticles.DRAGON_SHARD.get(), getX() + random.nextGaussian(), getY() + 6.0D, getZ() + random.nextGaussian(), random.nextGaussian() * 0.15D, 0.9D + random.nextDouble() * 0.5D, random.nextGaussian() * 0.15D);
                }
            }
            default -> {
                // 龙翼飓风：三层冲击波 + 径向风痕
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), getY() - 1.5D, getZ(), 14.0D, 16.0D, 0.0D);
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), getY() + 1.0D, getZ(), 12.0D, 14.0D, 0.0D);
                level.addAlwaysVisibleParticle(ModParticles.DRAGON_SHOCKWAVE.get(), true, getX(), getY() + 3.5D, getZ(), 10.0D, 12.0D, 0.0D);
                for (int i = 0; i < 40; i++) {
                    double angle = Math.PI * 2.0D * i / 40.0D;
                    double dx = Math.cos(angle);
                    double dz = Math.sin(angle);
                    level.addParticle(ModParticles.WHITESMOKE.get(), getX() + dx * 2.0D, getY() + 1.0D + random.nextDouble() * 2.0D, getZ() + dz * 2.0D, dx * 0.9D, 0.0D, dz * 0.9D);
                }
            }
        }
    }

    private static final class DragonSkyCombatGoal extends Goal {
        private final RebornMobEntity dragon;

        private DragonSkyCombatGoal(RebornMobEntity dragon) {
            this.dragon = dragon;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = dragon.getTarget();
            return dragon.isDragonPhaseOne() && !dragon.isDragonTransitioning() && target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            dragon.stopDragonSkill();
        }

        @Override
        public void tick() {
            dragon.tickSkyCombat(dragon.getTarget());
        }
    }

    private static final class DragonMeleeAttackGoal extends MeleeAttackGoal {
        private final RebornMobEntity dragon;

        private DragonMeleeAttackGoal(RebornMobEntity dragon, double speed, boolean followTarget) {
            super(dragon, speed, followTarget);
            this.dragon = dragon;
        }

        @Override
        public boolean canUse() {
            return dragon.isDragonPhaseTwo() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return dragon.isDragonPhaseTwo() && super.canContinueToUse();
        }

        @Override
        protected double getAttackReachSqr(net.minecraft.world.entity.LivingEntity target) {
            return Math.max(9.0D, dragon.getBbWidth() * dragon.getBbWidth() + target.getBbWidth());
        }
    }

    private static final class DragonFlightStrollGoal extends RandomStrollGoal {
        private final RebornMobEntity dragon;

        private DragonFlightStrollGoal(RebornMobEntity dragon, double speed, int interval) {
            super(dragon, speed, interval);
            this.dragon = dragon;
        }

        @Override
        public boolean canUse() {
            return dragon.isDragonPhaseOne() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return dragon.isDragonPhaseOne() && super.canContinueToUse();
        }

        @Override
        protected Vec3 getPosition() {
            RandomSource random = dragon.getRandom();
            double x = dragon.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            double y = dragon.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            double z = dragon.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0D;
            return new Vec3(x, y, z);
        }
    }

    private static final class DragonGroundStrollGoal extends RandomStrollGoal {
        private final RebornMobEntity dragon;

        private DragonGroundStrollGoal(RebornMobEntity dragon, double speed) {
            super(dragon, speed);
            this.dragon = dragon;
        }

        @Override
        public boolean canUse() {
            return dragon.isDragonPhaseTwo() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return dragon.isDragonPhaseTwo() && super.canContinueToUse();
        }
    }
}





