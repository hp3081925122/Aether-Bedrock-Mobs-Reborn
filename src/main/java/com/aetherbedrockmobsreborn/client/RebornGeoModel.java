package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;

public final class RebornGeoModel extends GeoModel<RebornMobEntity> {
    public static boolean supports(String id) {
        return switch (id) {
            case "aether_naga", "buterfly", "crystalitegolem", "crystaliteminion", "guardianangel", "mutatedcritter", "percipitus", "saltcrab", "eldershulker", "saltcrabking", "shimaenaga", "spirit", "twinkler" -> true;
            default -> false;
        };
    }

    @Override
    public Identifier getModelResource(com.geckolib.renderer.base.GeoRenderState state) {
        String model = switch (((RebornRenderState) state).id) {
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
        return Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, model);
    }

    @Override
    public Identifier getTextureResource(com.geckolib.renderer.base.GeoRenderState state) {
        return Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, "textures/entities/" + ((RebornRenderState) state).texture + ".png");
    }

    @Override
    public Identifier getAnimationResource(RebornMobEntity entity) {
        String animation = switch (entity.getIdPath()) {
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
        return Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, animation);
    }

}
