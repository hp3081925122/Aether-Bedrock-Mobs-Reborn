package com.aetherbedrockmobsreborn;

import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = AetherBedrockMobsReborn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RebornGameplayEvents {
    private static final int GOLEM_MINION_COOLDOWN_TICKS = 20;
    private static final int GOLEM_MINION_LIMIT = 8;
    private static final double GOLEM_MINION_COUNT_RADIUS = 32.0D;
    private static final String GOLEM_MINION_COOLDOWN_TAG = "reborn_minion_cooldown_until";
    private static final Map<String, Consumer<RebornMobEntity>> JOIN_HANDLERS = Map.of(
            "crystalitegolem", RebornGameplayEvents::handleGolemJoin,
            "crystaliteminion", RebornGameplayEvents::handleMinionJoin
    );
    private static final Map<String, Consumer<RebornMobEntity>> DEATH_HANDLERS = Map.ofEntries(
            Map.entry("aetherdragon", RebornGameplayEvents::handleDragonDeath),
            Map.entry("aether_naga", RebornGameplayEvents::handleNagaDeath),
            Map.entry("saltcrabking", RebornGameplayEvents::handleSaltCrabKingDeath),
            Map.entry("eldershulker", RebornGameplayEvents::handleElderShulkerDeath),
            Map.entry("spirit", RebornGameplayEvents::handleSpiritDeath),
            Map.entry("crystalitegolem", RebornGameplayEvents::handleCrystaliteGolemDeath),
            Map.entry("guardianangel", RebornGameplayEvents::handleGuardianAngelDeath)
    );
    private static final Map<String, Consumer<RebornMobEntity>> HURT_HANDLERS = Map.of(
            "crystalitegolem", RebornGameplayEvents::handleCrystaliteGolemHurt,
            "saltcrab", RebornGameplayEvents::handleSaltCrabHurt
    );

    private RebornGameplayEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof RebornMobEntity entity)) {
            return;
        }
        Consumer<RebornMobEntity> handler = JOIN_HANDLERS.get(entity.getIdPath());
        if (handler != null) {
            handler.accept(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RebornMobEntity entity) || !(entity.level() instanceof ServerLevel server)) {
            return;
        }
        entity.triggerAction("death");
        Consumer<RebornMobEntity> handler = DEATH_HANDLERS.get(entity.getIdPath());
        if (handler != null) {
            handler.accept(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof RebornMobEntity entity) || entity.level().isClientSide) {
            return;
        }
        Consumer<RebornMobEntity> handler = HURT_HANDLERS.get(entity.getIdPath());
        if (handler != null) {
            handler.accept(entity);
        }
    }

    private static void handleGolemJoin(RebornMobEntity entity) {
        if (!entity.getPersistentData().getBoolean("reborn_golem_initialized")) {
            entity.getPersistentData().putBoolean("reborn_golem_initialized", true);
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 10, false, false));
            entity.invulnerableTime = 100;
            entity.triggerAction("activate");
        }
    }

    private static void handleMinionJoin(RebornMobEntity entity) {
        if (!entity.getPersistentData().getBoolean("reborn_minion_initialized") && entity.level() instanceof ServerLevel server) {
            entity.getPersistentData().putBoolean("reborn_minion_initialized", true);
            entity.triggerAction("spawn");
            sendParticles(server, ModParticles.RUNESEFFECT.get(), entity, 6);
        }
    }

    private static void handleDragonDeath(RebornMobEntity entity) {
        if (!entity.isDragonPhaseTwo() || !(entity.level() instanceof ServerLevel server)) {
            return;
        }
        drop(server, entity, ModItems.ALL.get("shaday_helmet").get());
        drop(server, entity, ModItems.ALL.get("shaday_chestplate").get());
        drop(server, entity, ModItems.ALL.get("shaday_leggings").get());
        explode(server, entity, Level.ExplosionInteraction.NONE);
        playVictory(server, entity);
        sendParticles(server, ParticleTypes.EXPLOSION, entity, 7);
        sendParticles(server, ModParticles.FIRE.get(), entity, 17);
    }

    private static void handleNagaDeath(RebornMobEntity entity) {
        playVictory((ServerLevel) entity.level(), entity);
    }

    private static void handleSaltCrabKingDeath(RebornMobEntity entity) {
        ServerLevel server = (ServerLevel) entity.level();
        playVictory(server, entity);
        explode(server, entity, Level.ExplosionInteraction.NONE);
    }

    private static void handleElderShulkerDeath(RebornMobEntity entity) {
        ServerLevel server = (ServerLevel) entity.level();
        playVictory(server, entity);
        explode(server, entity, Level.ExplosionInteraction.NONE);
        sendParticles(server, ParticleTypes.EXPLOSION, entity, 5);
        sendParticles(server, ModParticles.FIRE.get(), entity, 10);
    }

    private static void handleSpiritDeath(RebornMobEntity entity) {
        ServerLevel server = (ServerLevel) entity.level();
        playVictory(server, entity);
        sendParticles(server, ModParticles.WHITESMOKE.get(), entity, 7);
        explode(server, entity, Level.ExplosionInteraction.NONE);
    }

    private static void handleCrystaliteGolemDeath(RebornMobEntity entity) {
        ServerLevel server = (ServerLevel) entity.level();
        playVictory(server, entity);
        sendParticles(server, ModParticles.RUNESEFFECT.get(), entity, 10);
        sendParticles(server, ParticleTypes.EXPLOSION, entity, 5);
        explode(server, entity, Level.ExplosionInteraction.MOB);
    }

    private static void handleGuardianAngelDeath(RebornMobEntity entity) {
        if (entity.getRandom().nextInt(7) == 6 && entity.level() instanceof ServerLevel server) {
            ItemEntity item = new ItemEntity(server, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(ModItems.ALL.get("citykey").get()));
            item.setPickUpDelay(10);
            server.addFreshEntity(item);
        }
    }

    private static void handleCrystaliteGolemHurt(RebornMobEntity entity) {
        if (entity.level() instanceof ServerLevel server && entity.getRandom().nextBoolean() && canSummonCrystaliteMinion(entity, server)) {
            RebornMobEntity minion = ModEntities.MOBS.get("crystaliteminion").get().create(server);
            if (minion != null) {
                minion.moveTo(entity.getX() + entity.getRandom().nextDouble() * 6.0D - 3.0D, entity.getY(), entity.getZ() + entity.getRandom().nextDouble() * 6.0D - 3.0D, entity.getYRot(), entity.getXRot());
                server.addFreshEntity(minion);
                entity.getPersistentData().putLong(GOLEM_MINION_COOLDOWN_TAG, server.getGameTime() + GOLEM_MINION_COOLDOWN_TICKS);
                sendParticles(server, ModParticles.RUNESEFFECT.get(), entity, 5);
            }
            entity.triggerAction("summoning");
        } else {
            entity.triggerAction("roar");
        }
    }

    private static void handleSaltCrabHurt(RebornMobEntity entity) {
        entity.triggerAction("jump");
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide || event.getSlot() != EquipmentSlot.OFFHAND || !event.getTo().is(ModItems.ALL.get("spiritsword").get())) {
            return;
        }
        LivingEntity entity = event.getEntity();
        ItemStack spiritSword = entity.getItemBySlot(EquipmentSlot.OFFHAND).copy();
        entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        if (entity instanceof Player player) {
            if (!player.getInventory().add(spiritSword)) {
                player.drop(spiritSword, false);
            }
        } else {
            entity.spawnAtLocation(spiritSword);
        }
    }

    private static boolean canSummonCrystaliteMinion(RebornMobEntity entity, ServerLevel server) {
        long cooldownUntil = entity.getPersistentData().getLong(GOLEM_MINION_COOLDOWN_TAG);
        if (server.getGameTime() < cooldownUntil) {
            return false;
        }
        AABB range = entity.getBoundingBox().inflate(GOLEM_MINION_COUNT_RADIUS);
        int minionCount = server.getEntitiesOfClass(RebornMobEntity.class, range, candidate -> candidate.getIdPath().equals("crystaliteminion")).size();
        return minionCount < GOLEM_MINION_LIMIT;
    }

    @SubscribeEvent
    public static void onEntityStruckByLightning(EntityStruckByLightningEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof net.minecraft.world.entity.LivingEntity living && !entity.level().isClientSide && living.getMainHandItem().is(ModItems.ALL.get("crystalitesword").get())) {
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 15, 4));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1));
        }
    }

    private static void drop(ServerLevel server, RebornMobEntity entity, net.minecraft.world.level.ItemLike itemLike) {
        ItemEntity item = new ItemEntity(server, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(itemLike));
        item.setPickUpDelay(10);
        server.addFreshEntity(item);
    }

    private static void playVictory(ServerLevel server, RebornMobEntity entity) {
        server.playSound(null, BlockPos.containing(entity.position()), ModSounds.ALL.get("victorymmm").get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static void explode(ServerLevel server, RebornMobEntity entity, Level.ExplosionInteraction interaction) {
        server.explode(null, entity.getX(), entity.getY(), entity.getZ(), 0.0F, interaction);
    }

    private static void sendParticles(ServerLevel server, ParticleOptions particle, RebornMobEntity entity, int count) {
        server.sendParticles(particle, entity.getX(), entity.getY(), entity.getZ(), count, 3.0D, 3.0D, 3.0D, 1.0D);
    }
}
