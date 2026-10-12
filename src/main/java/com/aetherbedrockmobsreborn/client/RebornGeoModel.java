package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.NagaEntity;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import org.slf4j.Logger;

public final class RebornGeoModel extends GeoModel<RebornMobEntity> {
    // 调试日志：展示用实体跳过头部旋转时只输出一次
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean loggedDetachedHead = false;

    public static boolean supports(String id) {
        return switch (id) {
            case "aetherdragon", "aether_naga", "buterfly", "crystalitegolem", "crystaliteminion", "guardianangel", "mutatedcritter", "percipitus", "saltcrab", "eldershulker", "saltcrabking", "shimaenaga", "spirit", "twinkler" -> true;
            default -> false;
        };
    }

    @Override
    public ResourceLocation getModelResource(RebornMobEntity entity) {
        String model = switch (entity.getIdPath()) {
            case "aetherdragon" -> "aether_dragon";
            case "aether_naga" -> "slider";
            case "crystalitegolem" -> "crystalite_golem";
            case "crystaliteminion" -> "crystalite_minion";
            case "guardianangel" -> "guardian_angel_m";
            case "mutatedcritter" -> "mutated_critter";
            case "percipitus" -> "percipitus";
            case "saltcrab" -> "salt_crab";
            case "shimaenaga" -> "shima_enaga";
            case "spirit" -> "spirit";
            case "saltcrabking" -> "salt_crab_king";
            case "eldershulker" -> "elder_shulker";
            default -> "twinkler";
        };
        return ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, "geo/" + model + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RebornMobEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, "textures/entities/" + entity.getTextureName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(RebornMobEntity entity) {
        String animation = switch (entity.getIdPath()) {
            case "aetherdragon" -> "aether_dragon";
            case "aether_naga" -> "slider";
            case "crystalitegolem" -> "crystalite_golem";
            case "crystaliteminion" -> "crystalite_minion";
            case "guardianangel" -> "guardian_angel_m";
            case "mutatedcritter" -> "mutated_critter";
            case "percipitus" -> "percipitus";
            case "saltcrab" -> "salt_crab";
            case "shimaenaga" -> "shima_enaga";
            case "spirit" -> "spirit";
            case "saltcrabking" -> "salt_crab_king";
            case "eldershulker" -> "elder_shulker";
            default -> "twinkler";
        };
        return ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, "animations/" + animation + ".animation.json");
    }

    @Override
    public void setCustomAnimations(RebornMobEntity entity, long instanceId, AnimationState<RebornMobEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
        if (entity instanceof NagaEntity naga) {
            applyNagaTailBend(naga, state.getPartialTick());
        }
        // 灵魂与远古潜影贝：不放技能时头部看向目标，叠加在动画旋转之上
        String headBone = "";
        if (entity.getIdPath().equals("spirit") && entity.getSpiritSkill() == 0) {
            headBone = "head";
        } else if (entity.getIdPath().equals("eldershulker") && entity.getShulkerSkill() == 0) {
            headBone = "shulker_head";
        }
        if (headBone.isEmpty()) {
            return;
        }
        GeoBone head = getAnimationProcessor().getBone(headBone);
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        if (head == null || data == null) {
            return;
        }
        // 未加入世界的展示用实体（JEI 掉落物界面等）不叠加看向旋转，避免插值字段未同步导致头部乱飞
        if (entity.level().getEntity(entity.getId()) != entity) {
            if (!loggedDetachedHead) {
                loggedDetachedHead = true;
                LOGGER.debug("Skipping head look for detached {} (id {}), netHeadYaw={}, headPitch={}", entity.getIdPath(), entity.getId(), data.netHeadYaw(), data.headPitch());
            }
            return;
        }
        // 偏航归一化后限幅，俯仰限幅
        float yaw = Mth.clamp(Mth.wrapDegrees(data.netHeadYaw()), -75.0F, 75.0F);
        float pitch = Mth.clamp(data.headPitch(), -60.0F, 60.0F);
        head.setRotX(head.getRotX() + pitch * Mth.DEG_TO_RAD);
        head.setRotY(head.getRotY() + yaw * Mth.DEG_TO_RAD);
    }

    // 天境娜迦：转身时尾部逐节滞后弯曲，叠加在动画旋转之上
    private void applyNagaTailBend(NagaEntity naga, float partialTicks) {
        float bend = naga.getTailBend(partialTicks) * Mth.DEG_TO_RAD;
        if (Math.abs(bend) < 1.0E-4F) {
            return;
        }
        String[] bones = {"tail1", "tail2", "tail3"};
        float[] weights = {0.3F, 0.35F, 0.35F};
        for (int i = 0; i < bones.length; i++) {
            GeoBone bone = getAnimationProcessor().getBone(bones[i]);
            if (bone != null) {
                bone.setRotY(bone.getRotY() + bend * weights[i]);
            }
        }
    }
}

