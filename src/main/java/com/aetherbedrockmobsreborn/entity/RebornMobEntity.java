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
import net.minecraft.server.level.ServerEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RebornMobEntity extends PathfinderMob implements GeoEntity {
    static void applyBossKnockback(LivingEntity target, Vec3 impulse) {
        var resistance = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        double scale = resistance == null ? 1.0D : 1.0D - Mth.clamp(resistance.getValue(), 0.0D, 1.0D);
        if (scale <= 0.0D) {
            return;
        }
        target.setDeltaMovement(target.getDeltaMovement().add(impulse.scale(scale)));
        target.hurtMarked = true;
    }

    private static final String BUTTERFLY_VARIANT_TAG = "reborn_butterfly_variant";
    private static final String[] BUTTERFLY_TEXTURES = {"black", "blue", "cloud", "cyan", "dark", "dark_blue", "gold", "green", "orange", "purple", "red_large", "yellow"};
    private static final EntityDataAccessor<Integer> BUTTERFLY_VARIANT = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BOSS_ENRAGED = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.BOOLEAN);
    // 盐蟹受伤音效冷却，避免连续受伤时反复播放跳跃音效
    private int saltCrabHurtSoundCooldown;
    private static final EntityDataAccessor<Integer> BOSS_SKILL = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BOSS_BEAM = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BOSS_BEAM_X = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BOSS_BEAM_Y = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BOSS_BEAM_Z = SynchedEntityData.defineId(RebornMobEntity.class, EntityDataSerializers.FLOAT);
    // 灵魂战斗状态，首次使用时创建
    private SpiritCombat spiritCombat;
    // 天境娜迦战斗状态，首次使用时创建
    private NagaCombat nagaCombat;
    // 水晶石傀儡战斗状态，首次使用时创建
    private GolemCombat golemCombat;
    // 盐蟹王战斗状态，首次使用时创建
    private CrabCombat crabCombat;
    // 远古潜影贝战斗状态，首次使用时创建
    private ShulkerCombat shulkerCombat;
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossInfo = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);
    private float lastBossBarProgress = -1.0F;

    public RebornMobEntity(EntityType<? extends RebornMobEntity> type, Level level) {
        super(type, level);
        if (!level.isClientSide && isBossEntity()) {
            com.mojang.logging.LogUtils.getLogger().debug("Boss distance despawn protection enabled: type={} uuid={}", getIdPath(), getUUID());
        }
        if (getIdPath().equals("buterfly") && !level.isClientSide) {
            this.entityData.set(BUTTERFLY_VARIANT, this.random.nextInt(BUTTERFLY_TEXTURES.length));
        }
        if (getIdPath().equals("crystalitegolem")) {
        }
        if (isFlyingEntity()) {
            this.moveControl = new FlyingMoveControl(this, 10, true);
        }
    }

    @Override
    protected void registerGoals() {
        if (isFlyingEntity()) {
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.1D));
            this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.8D));
        } else {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        }
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (this.getType().getCategory() == MobCategory.MONSTER || getIdPath().equals("saltcrab")) {
            if (getIdPath().equals("eldershulker")) {
                // 远古潜影贝：技能战斗，取代原有远程射击
                this.goalSelector.addGoal(2, new ShulkerCombat.CombatGoal(this));
            } else if (getIdPath().equals("spirit")) {
                // 灵魂：技能战斗，取代原有普攻与扑跳
                this.goalSelector.addGoal(2, new SpiritCombat.CombatGoal(this));
            } else if (getIdPath().equals("aether_naga")) {
                // 天境娜迦：技能战斗，取代原有普攻与扑跳
                this.goalSelector.addGoal(2, new NagaCombat.CombatGoal(this));
            } else if (getIdPath().equals("crystalitegolem")) {
                // 水晶石傀儡：技能战斗，取代原有普攻
                this.goalSelector.addGoal(2, new GolemCombat.CombatGoal(this));
            } else if (getIdPath().equals("saltcrabking")) {
                // 盐蟹王：技能战斗，取代原有普攻
                this.goalSelector.addGoal(2, new CrabCombat.CombatGoal(this));
            } else {
                this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
            }
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (!isFlyingEntity()) {
            return super.createNavigation(level);
        }
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public boolean isNoGravity() {
        return isFlyingEntity() || super.isNoGravity();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 远古潜影贝相位跃迁期间无敌
        if (getIdPath().equals("eldershulker") && !level().isClientSide && getShulkerCombat().isPhased() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        // 天境娜迦潜地期间无敌
        if (getIdPath().equals("aether_naga") && !level().isClientSide && getNagaCombat().isBurrowed() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) {
        if (!isFlyingEntity()) {
            super.checkFallDamage(distance, onGround, state, pos);
        }
    }

    @Override
    public void aiStep() {
        if (this.saltCrabHurtSoundCooldown > 0) {
            this.saltCrabHurtSoundCooldown--;
        }
        if (!level().isClientSide && getIdPath().equals("buterfly") && this.entityData.get(BUTTERFLY_VARIANT) < 0) {
            this.entityData.set(BUTTERFLY_VARIANT, this.random.nextInt(BUTTERFLY_TEXTURES.length));
        }
        super.aiStep();

        if (level().isClientSide && getIdPath().equals("spirit")) {
            getSpiritCombat().clientTick();
        }
        if (level().isClientSide && getIdPath().equals("aether_naga")) {
            getNagaCombat().clientTick();
        }
        if (level().isClientSide && getIdPath().equals("crystalitegolem")) {
            getGolemCombat().clientTick();
        }
        if (level().isClientSide && getIdPath().equals("saltcrabking")) {
            getCrabCombat().clientTick();
        }
        if (level().isClientSide && getIdPath().equals("eldershulker")) {
            getShulkerCombat().clientTick();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }

        // 远古潜影贝：记录受击时间，闭壳期间伤害降为两成
        if (!level().isClientSide && getIdPath().equals("eldershulker")) {
            getShulkerCombat().onHurt();
            if (getShulkerCombat().isGuarding()) {
                amount *= 0.2F;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BOSS_ENRAGED, false);
        builder.define(BOSS_SKILL, 0);
        builder.define(BOSS_BEAM, false);
        builder.define(BOSS_BEAM_X, 0.0F);
        builder.define(BOSS_BEAM_Y, 0.0F);
        builder.define(BOSS_BEAM_Z, 0.0F);
        builder.define(BUTTERFLY_VARIANT, -1);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (isBossEntity()) {
            updateBossBarProgress();
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
        updateBossBarProgress();
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(health);
        updateBossBarProgress();
    }

    private void updateBossBarProgress() {
        if (bossInfo == null || level().isClientSide || !isBossEntity()) {
            return;
        }
        float maxHealth = getMaxHealth();
        float progress = maxHealth > 0.0F ? Mth.clamp(getHealth() / maxHealth, 0.0F, 1.0F) : 0.0F;
        if (Math.abs(progress - lastBossBarProgress) > 1.0E-6F) {
            bossInfo.setProgress(progress);
            lastBossBarProgress = progress;
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

    @Override
    public int getAmbientSoundInterval() {
        return getIdPath().equals("saltcrab") ? 600 : super.getAmbientSoundInterval();
    }

    public void triggerSaltCrabHurtSound() {
        if (this.saltCrabHurtSoundCooldown > 0) {
            return;
        }
        this.saltCrabHurtSoundCooldown = 40;
        triggerAction("jump");
    }
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel server, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(server, source, recentlyHit);
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
        if (getIdPath().equals("buterfly")) {
            tag.putInt(BUTTERFLY_VARIANT_TAG, this.entityData.get(BUTTERFLY_VARIANT));
        }

        if (getIdPath().equals("spirit")) {
            getSpiritCombat().save(tag);
        }
        if (getIdPath().equals("aether_naga")) {
            getNagaCombat().save(tag);
        }
        if (getIdPath().equals("crystalitegolem")) {
            getGolemCombat().save(tag);
        }
        if (getIdPath().equals("saltcrabking")) {
            getCrabCombat().save(tag);
        }
        if (getIdPath().equals("eldershulker")) {
            getShulkerCombat().save(tag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (getIdPath().equals("buterfly") && tag.contains(BUTTERFLY_VARIANT_TAG)) {
            this.entityData.set(BUTTERFLY_VARIANT, Mth.clamp(tag.getInt(BUTTERFLY_VARIANT_TAG), 0, BUTTERFLY_TEXTURES.length - 1));
        }
        if (getIdPath().equals("spirit")) {
            getSpiritCombat().load(tag);
        }
        if (getIdPath().equals("aether_naga")) {
            getNagaCombat().load(tag);
        }
        if (getIdPath().equals("crystalitegolem")) {
            getGolemCombat().load(tag);
        }
        if (getIdPath().equals("saltcrabking")) {
            getCrabCombat().load(tag);
        }
        if (getIdPath().equals("eldershulker")) {
            getShulkerCombat().load(tag);
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

    private void soundKeyframe(SoundKeyframeEvent<RebornMobEntity> event) {
        if (!level().isClientSide) {
            return;
        }
        String soundId = switch (getIdPath()) {
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
                // 客户端需用本地播放，playSound(null, ...) 在客户端不会出声
                level().playLocalSound(getX(), getY(), getZ(), sound.get(), SoundSource.HOSTILE, 1.0F, 1.0F, false);
            }
        }
    }

    private void registerActionAnimations(AnimationController<RebornMobEntity> controller) {
        for (String action : new String[]{"attack", "hurt", "death", "summoning", "roar", "groundslam", "activate", "jump", "spawn"}) {
            String animation = getActionAnimation(action);
            if (!animation.isEmpty()) {
                controller.triggerableAnim(action, RawAnimation.begin().thenPlay(animation));
            }
        }
    }

    private String getActionAnimation(String action) {
        return switch (getIdPath()) {
            case "crystalitegolem" -> switch (action) {
                case "death", "activate" -> "animation.crystalite_golem." + action;
                default -> "";
            };
            case "crystaliteminion" -> switch (action) {
                case "spawn", "attack" -> "animation.crystalite_minion." + action;
                default -> "";
            };
            case "aether_naga" -> switch (action) {
                case "death" -> "animation.slider." + action;
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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (isFlyingEntity()) {
            if (!level().isClientSide && getIdPath().equals("buterfly") && distance > 3.0F) {
            }
            return false;
        }
        return super.causeFallDamage(distance, multiplier, source);
    }

    private PlayState movementPredicate(AnimationState<RebornMobEntity> state) {
        // 灵魂技能动画同样以待机为底，放在移动控制器里
        String spiritSkill = SpiritCombat.getAnimation(getSpiritSkill());
        if (!spiritSkill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(spiritSkill));
        }
        // 天境娜迦技能动画
        String nagaSkill = NagaCombat.getAnimation(getNagaSkill());
        if (!nagaSkill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(nagaSkill));
        }
        // 水晶石傀儡技能动画
        String golemSkill = GolemCombat.getAnimation(getGolemSkill());
        if (!golemSkill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(golemSkill));
        }
        // 盐蟹王技能动画
        String crabSkill = CrabCombat.getAnimation(getCrabSkill());
        if (!crabSkill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(crabSkill));
        }
        // 远古潜影贝技能动画
        String shulkerSkill = ShulkerCombat.getAnimation(getShulkerSkill());
        if (!shulkerSkill.isEmpty()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay(shulkerSkill));
        }
        String animation = getMovementAnimation(state.isMoving());
        return animation.isEmpty() ? PlayState.STOP : state.setAndContinue(RawAnimation.begin().thenLoop(animation));
    }

    private String getMovementAnimation(boolean moving) {
        return switch (getIdPath()) {
            case "aether_naga" -> moving ? "animation.slider.move" : "animation.slider.idle";
            case "saltcrabking" -> moving ? "animation.salt_crab_king.walk" : "animation.salt_crab_king.idle";
            case "eldershulker" -> "animation.elder_shulker.idle";
            case "spirit" -> moving ? "animation.spirit.move" : "animation.spirit.idle";
            case "crystalitegolem" -> moving ? "animation.crystalite_golem.walk" : "animation.crystalite_golem.idle1";
            case "crystaliteminion" -> moving ? "animation.crystalite_minion.walk" : "animation.crystalite_minion.idle";
            case "guardianangel" -> moving ? "animation.guardian_angel.move" : "animation.guardian_angel.idle";
            case "mutatedcritter" -> moving ? "animation.mutated_critter.walk" : "animation.mutated_critter.idle";
            case "percipitus" -> moving ? "animation.percipitus.fly" : "animation.percipitus.idle";
            case "saltcrab" -> moving ? "animation.salt_crab.walk" : "animation.salt_crab.idle2";
            case "shimaenaga" -> moving ? "animation.shima_enaga.walk" : "animation.shima_enaga.idle2";
            case "buterfly", "twinkler" -> moving ? "animation.twinkler.fly" : "animation.twinkler.idle";
            default -> "";
        };
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    public String getIdPath() {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType());
        return key == null ? "missing" : key.getPath();
    }

    public String getTextureName() {
        return switch (getIdPath()) {
            case "aether_naga" -> "slider";
            case "buterfly" -> BUTTERFLY_TEXTURES[Mth.clamp(this.entityData.get(BUTTERFLY_VARIANT), 0, BUTTERFLY_TEXTURES.length - 1)];
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

    @Override
    public boolean requiresCustomPersistence() {
        return isBossEntity() || super.requiresCustomPersistence();
    }

    private boolean isBossEntity() {
        return switch (getIdPath()) {
            case "aether_naga", "crystalitegolem", "eldershulker", "saltcrabking", "spirit" -> true;
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
            case "mutatedcritter" -> kind.equals("ambient") ? null : kind.equals("hurt") ? "muh" : "mud";
            case "saltcrab" -> kind.equals("ambient") ? "saw" : "sah";
            case "spirit" -> kind.equals("ambient") ? "spw" : kind.equals("hurt") ? "sph" : "spd";
            case "eldershulker" -> kind.equals("ambient") ? "shi" : kind.equals("hurt") ? "shh" : "shd";
            default -> null;
        };
        return sound == null ? null : ModSounds.ALL.get(sound).get();
    }
    private boolean isFlyingEntity() {
        String id = getIdPath();
        return id.equals("percipitus") || id.equals("shimaenaga") || id.equals("twinkler") || id.equals("buterfly");
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
        if (id.equals("aether_naga")) {
            health = 350.0D;
            armor = 0.1D;
            damage = 6.0D;
            followRange = 32.0D;
            knockbackResistance = 0.8D;
            attackKnockback = 0.1D;
        } else if (id.equals("spirit")) {
            speed = 0.4D;
            health = 300.0D;
            armor = 0.3D;
            knockbackResistance = 0.8D;
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
            followRange = 32.0D;
            attackKnockback = 0.3D;
            knockbackResistance = 1.0D;
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
        } else if (id.equals("buterfly") || id.equals("twinkler")) {
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

    // 灵魂技能复用技能同步字段
    public int getSpiritSkill() {
        if (!getIdPath().equals("spirit")) {
            return 0;
        }
        return this.entityData.get(BOSS_SKILL);
    }

    void setBossSkill(int skill) {
        this.entityData.set(BOSS_SKILL, skill);
    }

    void setBossSkillPoint(Vec3 point) {
        this.entityData.set(BOSS_BEAM_X, (float) point.x);
        this.entityData.set(BOSS_BEAM_Y, (float) point.y);
        this.entityData.set(BOSS_BEAM_Z, (float) point.z);
    }

    Vec3 getBossSkillPoint() {
        return getBossBeamEnd();
    }

    // 天境娜迦技能复用技能同步字段
    public int getNagaSkill() {
        if (!getIdPath().equals("aether_naga")) {
            return 0;
        }
        return this.entityData.get(BOSS_SKILL);
    }

    public boolean isNagaBeamActive() {
        return getIdPath().equals("aether_naga") && this.entityData.get(BOSS_BEAM);
    }

    void setBossBeam(boolean active) {
        this.entityData.set(BOSS_BEAM, active);
    }

    // 水晶石傀儡技能复用技能同步字段
    public int getGolemSkill() {
        if (!getIdPath().equals("crystalitegolem")) {
            return 0;
        }
        return this.entityData.get(BOSS_SKILL);
    }

    public boolean isGolemEnraged() {
        return getIdPath().equals("crystalitegolem") && this.entityData.get(BOSS_ENRAGED);
    }

    void setBossEnraged(boolean enraged) {
        this.entityData.set(BOSS_ENRAGED, enraged);
    }

    // 盐蟹王技能复用技能同步字段
    public int getCrabSkill() {
        if (!getIdPath().equals("saltcrabking")) {
            return 0;
        }
        return this.entityData.get(BOSS_SKILL);
    }

    public boolean isCrabEnraged() {
        return getIdPath().equals("saltcrabking") && this.entityData.get(BOSS_ENRAGED);
    }

    // 远古潜影贝技能复用技能同步字段
    public int getShulkerSkill() {
        if (!getIdPath().equals("eldershulker")) {
            return 0;
        }
        return this.entityData.get(BOSS_SKILL);
    }

    public boolean isShulkerEnraged() {
        return getIdPath().equals("eldershulker") && this.entityData.get(BOSS_ENRAGED);
    }

    public boolean isShulkerBeamActive() {
        return getIdPath().equals("eldershulker") && this.entityData.get(BOSS_BEAM);
    }

    ShulkerCombat getShulkerCombat() {
        if (this.shulkerCombat == null) {
            this.shulkerCombat = new ShulkerCombat(this);
        }
        return this.shulkerCombat;
    }

    CrabCombat getCrabCombat() {
        if (this.crabCombat == null) {
            this.crabCombat = new CrabCombat(this);
        }
        return this.crabCombat;
    }

    GolemCombat getGolemCombat() {
        if (this.golemCombat == null) {
            this.golemCombat = new GolemCombat(this);
        }
        return this.golemCombat;
    }

    NagaCombat getNagaCombat() {
        if (this.nagaCombat == null) {
            this.nagaCombat = new NagaCombat(this);
        }
        return this.nagaCombat;
    }

    SpiritCombat getSpiritCombat() {
        if (this.spiritCombat == null) {
            this.spiritCombat = new SpiritCombat(this);
        }
        return this.spiritCombat;
    }

    public Vec3 getBossBeamEnd() {
        return new Vec3(this.entityData.get(BOSS_BEAM_X), this.entityData.get(BOSS_BEAM_Y), this.entityData.get(BOSS_BEAM_Z));
    }

    void faceBossTowards(double x, double z, float maxStep) {
        float yaw = (float) (Mth.atan2(z - getZ(), x - getX()) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(Mth.approachDegrees(getYRot(), yaw, maxStep));
        this.yBodyRot = getYRot();
        this.yHeadRot = getYRot();
    }

    // 向下寻找地面高度，找不到时返回 fallback
    double findGroundY(double x, double fromY, double z, double depth, double fallback) {
        Vec3 from = new Vec3(x, fromY, z);
        BlockHitResult hit = level().clip(new ClipContext(from, from.subtract(0.0D, depth, 0.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation().y;
        }
        return fallback;
    }

    // 远古潜影贝不会被玩家或其他生物挤动，自身仍会把贴进来的实体推开
    @Override
    public boolean isPushable() {
        if (getIdPath().equals("eldershulker")) {
            return false;
        }
        return super.isPushable();
    }

    boolean canBossSkillHit(LivingEntity entity) {
        return entity != this && entity.isAlive() && !(entity instanceof RebornMobEntity);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (getIdPath().equals("spirit") && SpiritCombat.isEvent(id)) {
            getSpiritCombat().handleEvent(id);
            return;
        }
        if (getIdPath().equals("aether_naga") && NagaCombat.isEvent(id)) {
            getNagaCombat().handleEvent(id);
            return;
        }
        if (getIdPath().equals("crystalitegolem") && GolemCombat.isEvent(id)) {
            getGolemCombat().handleEvent(id);
            return;
        }
        if (getIdPath().equals("saltcrabking") && CrabCombat.isEvent(id)) {
            getCrabCombat().handleEvent(id);
            return;
        }
        if (getIdPath().equals("eldershulker") && ShulkerCombat.isEvent(id)) {
            getShulkerCombat().handleEvent(id);
            return;
        }
        super.handleEntityEvent(id);
    }
}
