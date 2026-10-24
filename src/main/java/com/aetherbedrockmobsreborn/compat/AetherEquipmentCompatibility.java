package com.aetherbedrockmobsreborn.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AetherEquipmentCompatibility {
    private static final AtomicBoolean REPORTED = new AtomicBoolean();
    private static final TagKey<Item> AETHER_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("aether", "treated_as_aether_item"));
    private static final TagKey<EntityType<?>> AETHER_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("aether", "treated_as_aether_entity"));
    private static final TagKey<EntityType<?>> VANILLA_ENTITIES = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("aether", "treated_as_vanilla_entity"));

    private AetherEquipmentCompatibility() {
    }

    public static boolean isExempt(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String namespace = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
        return namespace.equals("aether_bedrock_mobs_reborn") || namespace.equals("aether_dragon");
    }

    public static double exemptArmorPenalty(LivingEntity target, Entity source) {
        if (source == null || !(source.getType().getDescriptionId().startsWith("entity.aether")
                || source.getType().is(AETHER_ENTITIES) && !source.getType().is(VANILLA_ENTITIES))) return 0.0;
        double[] penalty = {0.0};
        for (ItemStack stack : target.getArmorSlots()) {
            if (!isExempt(stack) || stack.getItem().getDescriptionId().startsWith("item.aether.") || stack.is(AETHER_ITEMS)) continue;
            if (!(stack.getItem() instanceof ArmorItem armor)) continue;
            stack.forEachModifier(armor.getEquipmentSlot(), (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) penalty[0] += modifier.amount() / 15.0;
            });
        }
        return penalty[0];
    }

    public static void reportActive() {
        if (REPORTED.compareAndSet(false, true)) {
            LogUtils.getLogger().debug("Bypassed Aether equipment penalties for Reborn and Aether Dragon equipment");
        }
    }
}
