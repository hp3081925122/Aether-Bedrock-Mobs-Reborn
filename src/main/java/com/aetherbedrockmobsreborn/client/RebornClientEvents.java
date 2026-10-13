package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.client.model.ShadayArmorModel;
import com.aetherbedrockmobsreborn.client.particle.DecalParticle;
import com.aetherbedrockmobsreborn.client.particle.QuakeBlockParticle;
import com.aetherbedrockmobsreborn.client.particle.ShardParticle;
import com.aetherbedrockmobsreborn.client.particle.ShockwaveParticle;
import com.aetherbedrockmobsreborn.client.particle.JawParticle;
import com.aetherbedrockmobsreborn.client.particle.PunchRingParticle;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.aetherbedrockmobsreborn.entity.RebornProjectileEntity;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import com.geckolib.renderer.GeoEntityRenderer;

@EventBusSubscriber(modid = AetherBedrockMobsReborn.MOD_ID, value = Dist.CLIENT)
public final class RebornClientEvents {
    private RebornClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderPipelines(net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
        event.registerPipeline(BeamRenderUtil.PIPELINE);
        com.mojang.logging.LogUtils.getLogger().debug("Registered additive boss beam pipeline: source=ONE destination=ONE depthWrite=false");
    }

    @SubscribeEvent
    public static void registerDebugRenderers(net.neoforged.neoforge.client.event.RegisterDebugRenderersEvent event) {
        if (net.minecraft.client.Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES)) {
            event.register(NagaHitboxDebugRenderer::new);
            com.mojang.logging.LogUtils.getLogger().debug("Registered Naga multipart hitbox debug renderer");
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var entry : ModEntities.MOBS.entrySet()) {
            if (RebornGeoModel.supports(entry.getKey())) {
                event.registerEntityRenderer(entry.getValue().get(), RebornGeoRenderer::new);
            } else {
                event.registerEntityRenderer(entry.getValue().get(), RebornGeoRenderer::new);
            }
        }
        for (var entry : ModEntities.PROJECTILES.entrySet()) {
            if (entry.getKey().equals("shulkercrysta")) {
                event.registerEntityRenderer(entry.getValue().get(), PyreSpearProjectileRenderer::new);
            } else {
                event.registerEntityRenderer(entry.getValue().get(), RebornArrowRenderer::new);
            }
        }
        // 灵魂技能实体
        event.registerEntityRenderer(ModEntities.SPIRIT_SKILL.get(), SpiritSkillRenderer::new);
        // 天境娜迦技能实体
        event.registerEntityRenderer(ModEntities.NAGA_SKILL.get(), NagaSkillRenderer::new);
        // 水晶石傀儡技能实体
        event.registerEntityRenderer(ModEntities.GOLEM_SKILL.get(), GolemSkillRenderer::new);
        // 盐蟹王技能实体
        event.registerEntityRenderer(ModEntities.CRAB_SKILL.get(), CrabSkillRenderer::new);
        // 远古潜影贝技能实体
        event.registerEntityRenderer(ModEntities.SHULKER_SKILL.get(), ShulkerSkillRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShadayArmorModel.LAYER_LOCATION, ShadayArmorModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticleGroups(net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent event) {
        event.register(com.aetherbedrockmobsreborn.client.particle.RebornParticleGroup.TYPE,
                com.aetherbedrockmobsreborn.client.particle.RebornParticleGroup::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        for (var entry : com.aetherbedrockmobsreborn.registry.ModItems.ALL.values()) {
            if (entry.get() instanceof com.aetherbedrockmobsreborn.item.ShadayArmorItem armor)
                event.registerItem(armor.clientExtensions(), armor);
        }
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FIRE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.RUNESEFFECT.get(), SmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.WHITESMOKE.get(), SmokeParticle.Provider::new);
        event.registerSpecial(ModParticles.QUAKE_BLOCK.get(), new QuakeBlockParticle.Provider());
        // 灵魂技能特效
        event.registerSpriteSet(ModParticles.SPIRIT_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_WARNING.get(), ShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_SLASH.get(), DecalParticle.SlashProvider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_SHARD.get(), ShardParticle.Provider::new);
        // 水晶石傀儡专属粒子
        event.registerSpriteSet(ModParticles.GOLEM_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GOLEM_WARNING.get(), ShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.GOLEM_SHARD.get(), ShardParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GOLEM_PUNCH.get(), PunchRingParticle.Provider::new);
        // 天境娜迦技能特效
        event.registerSpriteSet(ModParticles.NAGA_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.NAGA_WARNING.get(), ShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.NAGA_SLASH.get(), DecalParticle.SlashProvider::new);
        event.registerSpriteSet(ModParticles.NAGA_BITE.get(), JawParticle.BiteProvider::new);
        // 盐蟹王技能特效
        event.registerSpriteSet(ModParticles.SALT_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SALT_WARNING.get(), ShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SALT_SHARD.get(), ShardParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SALT_SPRAY.get(), ShardParticle.ShortProvider::new);
        event.registerSpriteSet(ModParticles.CRAB_PINCH.get(), JawParticle.PinchProvider::new);
        // 远古潜影贝技能特效
        event.registerSpriteSet(ModParticles.SHULKER_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SHULKER_WARNING.get(), ShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SHULKER_SPARK.get(), ShardParticle.Provider::new);
        // 燔祭长矛投射物：熔火冲击环、火星
        event.registerSpriteSet(ModParticles.PYRE_RING.get(), ShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PYRE_EMBER.get(), ShardParticle.Provider::new);
    }

}
