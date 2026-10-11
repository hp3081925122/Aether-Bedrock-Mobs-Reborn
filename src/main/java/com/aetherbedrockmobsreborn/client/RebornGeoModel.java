package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class RebornGeoModel extends GeoModel<RebornMobEntity> {
    public static boolean supports(String id) {
        return switch (id) {
            case "aetherdragon", "aether_naga", "buterfly_2", "buterfly_3", "buterfly_4", "buterfly_5", "buterfly_6", "buterfly7", "buterfly_8", "buterfly_9", "buterfly_10", "buterfly_11", "buterfly_12", "buterfly_13", "crystalitegolem", "crystaliteminion", "guardianangel", "mutatedcritter", "percipitus", "saltcrab", "shimaenaga", "twinkler" -> true;
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
            default -> "twinkler";
        };
        return new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, "animations/" + animation + ".animation.json");
    }
}
