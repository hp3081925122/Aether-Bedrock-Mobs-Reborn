package com.aetherbedrockmobsreborn.registry;

import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class ModArmorMaterials {
    public static final ArmorMaterial SHADAY = new ArmorMaterial(50,
            Map.of(ArmorType.HELMET, 4, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 9, ArmorType.BOOTS, 4),
            30, SoundEvents.ARMOR_EQUIP_GENERIC, 4.0F, 0.1F,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "no_repair")),
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "shaday")));
    private ModArmorMaterials() {}
}
