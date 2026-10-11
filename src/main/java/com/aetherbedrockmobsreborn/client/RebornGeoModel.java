package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public final class RebornGeoModel extends GeoModel<RebornMobEntity> {
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
        return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "geo/" + model + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RebornMobEntity entity) {
        return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "textures/entities/" + entity.getTextureName() + ".png");
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
        return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "animations/" + animation + ".animation.json");
    }

    @Override
    public void setCustomAnimations(RebornMobEntity entity, long instanceId, AnimationState<RebornMobEntity> state) {
        super.setCustomAnimations(entity, instanceId, state);
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
        CoreGeoBone head = getAnimationProcessor().getBone(headBone);
        EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
        if (head == null || data == null) {
            return;
        }
        head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
        head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
