package com.aetherbedrockmobsreborn.item;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ToolMaterial;

public final class AetherTier {
    public static ToolMaterial create(int uses, float speed, float damage, int enchantmentValue) {
        return new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, uses, speed, damage, enchantmentValue,
                TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "no_repair")));
    }
    private AetherTier() {}
}
