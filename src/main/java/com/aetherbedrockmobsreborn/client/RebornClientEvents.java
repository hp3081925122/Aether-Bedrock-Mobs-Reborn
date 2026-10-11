package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.client.model.ElderShulkerModel;
import com.aetherbedrockmobsreborn.client.model.SaltCrabKingModel;
import com.aetherbedrockmobsreborn.client.model.ShadayArmorModel;
import com.aetherbedrockmobsreborn.client.model.SpiritModel;
import com.aetherbedrockmobsreborn.client.particle.DragonShardParticle;
import com.aetherbedrockmobsreborn.client.particle.DragonShockwaveParticle;
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
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = AetherBedrockMobsReborn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RebornClientEvents {
    private RebornClientEvents() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var entry : ModEntities.MOBS.entrySet()) {
            if (entry.getKey().equals("eldershulker")) {
                event.registerEntityRenderer(entry.getValue().get(), RebornElderShulkerRenderer::new);
            } else if (entry.getKey().equals("saltcrabking")) {
                event.registerEntityRenderer(entry.getValue().get(), RebornSaltCrabKingRenderer::new);
            } else if (entry.getKey().equals("spirit")) {
                event.registerEntityRenderer(entry.getValue().get(), RebornSpiritRenderer::new);
            } else if (RebornGeoModel.supports(entry.getKey())) {
                event.registerEntityRenderer(entry.getValue().get(), RebornGeoRenderer::new);
            } else {
                event.registerEntityRenderer(entry.getValue().get(), RebornMobRenderer::new);
            }
        }
        for (var entry : ModEntities.PROJECTILES.entrySet()) {
            event.registerEntityRenderer(entry.getValue().get(), RebornProjectileRenderer::new);
        }
        // 艾瑟龙技能实体
        event.registerEntityRenderer(ModEntities.DRAGON_SKILL.get(), DragonSkillRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ElderShulkerModel.LAYER_LOCATION, ElderShulkerModel::createBodyLayer);
        event.registerLayerDefinition(SaltCrabKingModel.LAYER_LOCATION, SaltCrabKingModel::createBodyLayer);
        event.registerLayerDefinition(ShadayArmorModel.LAYER_LOCATION, ShadayArmorModel::createBodyLayer);
        event.registerLayerDefinition(SpiritModel.LAYER_LOCATION, SpiritModel::createBodyLayer);
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
    }

    private static final class RebornGeoRenderer extends GeoEntityRenderer<RebornMobEntity> {
        private static final ResourceLocation DRAGON_BEAM = new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entity/dragon_skill/dragon_beam.png");

        private RebornGeoRenderer(EntityRendererProvider.Context context) {
            super(context, new RebornGeoModel());
            this.shadowRadius = 0.5F;
        }

        @Override
        public void render(RebornMobEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
            boolean beam = entity.isDragonBeamActive();
            // 龙息期间追踪下颚骨骼的世界坐标
            if (beam) {
                getGeoModel().getBone("jaw").ifPresent(bone -> bone.setTrackingMatrices(true));
            }
            super.render(entity, yaw, partialTick, poseStack, buffers, light);
            if (beam) {
                getGeoModel().getBone("jaw").ifPresent(bone -> renderDragonBeam(entity, partialTick, poseStack, buffers, bone.getWorldPosition()));
            }
        }

        private void renderDragonBeam(RebornMobEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffers, Vector3d jaw) {
            // 以实体插值位置为原点，计算光束起止点
            double ox = Mth.lerp(partialTick, entity.xo, entity.getX());
            double oy = Mth.lerp(partialTick, entity.yo, entity.getY());
            double oz = Mth.lerp(partialTick, entity.zo, entity.getZ());
            // GeckoLib 的骨骼世界坐标基于未插值的实体位置
            Vec3 start = new Vec3(jaw.x - entity.getX(), jaw.y - entity.getY(), jaw.z - entity.getZ());
            Vec3 end = entity.getDragonBeamEnd().subtract(ox, oy, oz);
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            if (length < 0.5D) {
                return;
            }
            // 面向相机展开光束宽度
            Vec3 cam = this.entityRenderDispatcher.camera.getPosition().subtract(ox, oy, oz);
            Vec3 side = dir.cross(cam.subtract(start)).normalize();
            float time = entity.tickCount + partialTick;
            float scroll = -time * 0.35F;
            float pulse = 1.0F + Mth.sin(time * 0.9F) * 0.12F;
            float vLength = (float) (length / 4.0D);
            VertexConsumer consumer = buffers.getBuffer(RenderType.eyes(DRAGON_BEAM));
            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            // 外层光晕与内层核心
            for (float width : new float[]{2.4F * pulse, 1.0F * pulse}) {
                Vec3 s = side.scale(width * 0.5F);
                DragonSkillRenderer.vertex(consumer, pose, normal, (float) (start.x - s.x), (float) (start.y - s.y), (float) (start.z - s.z), 0.0F, scroll);
                DragonSkillRenderer.vertex(consumer, pose, normal, (float) (start.x + s.x), (float) (start.y + s.y), (float) (start.z + s.z), 1.0F, scroll);
                DragonSkillRenderer.vertex(consumer, pose, normal, (float) (end.x + s.x), (float) (end.y + s.y), (float) (end.z + s.z), 1.0F, scroll + vLength);
                DragonSkillRenderer.vertex(consumer, pose, normal, (float) (end.x - s.x), (float) (end.y - s.y), (float) (end.z - s.z), 0.0F, scroll + vLength);
            }
        }
    }

    private static final class RebornMobRenderer extends MobRenderer<RebornMobEntity, HumanoidModel<RebornMobEntity>> {
        private RebornMobRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
        }

        @Override
        public ResourceLocation getTextureLocation(RebornMobEntity entity) {
            return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entities/" + entity.getTextureName() + ".png");
        }
    }

    private static final class RebornElderShulkerRenderer extends MobRenderer<RebornMobEntity, ElderShulkerModel<RebornMobEntity>> {
        private RebornElderShulkerRenderer(EntityRendererProvider.Context context) {
            super(context, new ElderShulkerModel<>(context.bakeLayer(ElderShulkerModel.LAYER_LOCATION)), 4.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(RebornMobEntity entity) {
            return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entities/elder_shulker.png");
        }
    }

    private static final class RebornSaltCrabKingRenderer extends MobRenderer<RebornMobEntity, SaltCrabKingModel<RebornMobEntity>> {
        private RebornSaltCrabKingRenderer(EntityRendererProvider.Context context) {
            super(context, new SaltCrabKingModel<>(context.bakeLayer(SaltCrabKingModel.LAYER_LOCATION)), 3.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(RebornMobEntity entity) {
            return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entities/salt_crab_king.png");
        }
    }

    private static final class RebornSpiritRenderer extends MobRenderer<RebornMobEntity, SpiritModel<RebornMobEntity>> {
        private RebornSpiritRenderer(EntityRendererProvider.Context context) {
            super(context, new SpiritModel<>(context.bakeLayer(SpiritModel.LAYER_LOCATION)), 2.0F);
        }

        @Override
        public ResourceLocation getTextureLocation(RebornMobEntity entity) {
            return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entities/spirit.png");
        }
    }

    private static final class RebornProjectileRenderer extends ArrowRenderer<RebornProjectileEntity> {
        private RebornProjectileRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(RebornProjectileEntity entity) {
            return new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");
        }
    }
}

