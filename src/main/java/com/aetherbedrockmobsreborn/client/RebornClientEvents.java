package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.client.model.ShadayArmorModel;
import com.aetherbedrockmobsreborn.client.particle.DragonDecalParticle;
import com.aetherbedrockmobsreborn.client.particle.DragonQuakeBlockParticle;
import com.aetherbedrockmobsreborn.client.particle.DragonShardParticle;
import com.aetherbedrockmobsreborn.client.particle.DragonShockwaveParticle;
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
        // 艾瑟龙技能实体
        event.registerEntityRenderer(ModEntities.DRAGON_SKILL.get(), DragonSkillRenderer::new);
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
        // 艾瑟龙转阶段特效
        event.registerSpriteSet(ModParticles.DRAGON_SHOCKWAVE.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.DRAGON_SHARD.get(), DragonShardParticle.Provider::new);
        event.registerSpriteSet(ModParticles.DRAGON_WARNING.get(), DragonShockwaveParticle.WarningProvider::new);
        event.registerSpriteSet(ModParticles.DRAGON_SLASH.get(), DragonDecalParticle.SlashProvider::new);
        event.registerSpecial(ModParticles.DRAGON_QUAKE_BLOCK.get(), new DragonQuakeBlockParticle.Provider());
        // 灵魂技能特效
        event.registerSpriteSet(ModParticles.SPIRIT_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_WARNING.get(), DragonShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_SLASH.get(), DragonDecalParticle.SlashProvider::new);
        event.registerSpriteSet(ModParticles.SPIRIT_SHARD.get(), DragonShardParticle.Provider::new);
        // 水晶石傀儡专属粒子
        event.registerSpriteSet(ModParticles.GOLEM_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GOLEM_WARNING.get(), DragonShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.GOLEM_SHARD.get(), DragonShardParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GOLEM_PUNCH.get(), PunchRingParticle.Provider::new);
        // 天境娜迦技能特效
        event.registerSpriteSet(ModParticles.NAGA_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.NAGA_WARNING.get(), DragonShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.NAGA_SLASH.get(), DragonDecalParticle.SlashProvider::new);
        event.registerSpriteSet(ModParticles.NAGA_BITE.get(), JawParticle.BiteProvider::new);
        // 盐蟹王技能特效
        event.registerSpriteSet(ModParticles.SALT_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SALT_WARNING.get(), DragonShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SALT_SHARD.get(), DragonShardParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SALT_SPRAY.get(), DragonShardParticle.ShortProvider::new);
        event.registerSpriteSet(ModParticles.CRAB_PINCH.get(), JawParticle.PinchProvider::new);
        // 远古潜影贝技能特效
        event.registerSpriteSet(ModParticles.SHULKER_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SHULKER_WARNING.get(), DragonShockwaveParticle.PlainWarningProvider::new);
        event.registerSpriteSet(ModParticles.SHULKER_SPARK.get(), DragonShardParticle.Provider::new);
        // 燔祭长矛投射物：熔火冲击环、火星
        event.registerSpriteSet(ModParticles.PYRE_RING.get(), DragonShockwaveParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PYRE_EMBER.get(), DragonShardParticle.Provider::new);
    }

}
