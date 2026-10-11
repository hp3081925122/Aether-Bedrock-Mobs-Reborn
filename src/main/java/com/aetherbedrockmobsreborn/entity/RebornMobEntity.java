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
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.1D));
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
                this.goalSelector.addGoal(2, new DragonRangedAttackGoal(this, 1.25D, 25, 150.0F));
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
                case "attack" -> "animation.aether_dragon.attack.air";
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

    private static final class DragonRangedAttackGoal extends RangedAttackGoal {
        private final RebornMobEntity dragon;

        private DragonRangedAttackGoal(RebornMobEntity dragon, double speed, int interval, float range) {
            super(dragon, speed, interval, range);
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





