package com.aetherbedrockmobsreborn.item;

import com.aetherbedrockmobsreborn.client.model.ShadayArmorModel;
import com.aetherbedrockmobsreborn.registry.ModArmorMaterials;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;


public final class ShadayArmorItem extends Item {
    private final ArmorType type;
    public ShadayArmorItem(ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterials.SHADAY, type).rarity(Rarity.RARE));
        this.type = type;
    }

    public IClientItemExtensions clientExtensions() {
        return new IClientItemExtensions() {
            @Override
            public Identifier getArmorTexture(ItemStack stack, net.minecraft.client.resources.model.EquipmentClientInfo.LayerType layerType,
                    net.minecraft.client.resources.model.EquipmentClientInfo.Layer layer, Identifier fallback) {
                return Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "textures/entities/valkyrie_armor.png");
            }
            @Override
            public net.minecraft.client.model.Model getHumanoidArmorModel(ItemStack stack, net.minecraft.client.resources.model.EquipmentClientInfo.LayerType layerType, net.minecraft.client.model.Model defaultModel) {
                ShadayArmorModel source = new ShadayArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ShadayArmorModel.LAYER_LOCATION));
                ModelPart empty = new ModelPart(Collections.emptyList(), Collections.emptyMap());
                Map<String, ModelPart> parts;
                if (type == ArmorType.HELMET) {
                    parts = Map.of(
                            "head", source.head,
                            "hat", empty,
                            "body", empty,
                            "right_arm", empty,
                            "left_arm", empty,
                            "right_leg", empty,
                            "left_leg", empty
                    );
                } else if (type == ArmorType.CHESTPLATE) {
                    parts = Map.of(
                            "head", empty,
                            "hat", empty,
                            "body", source.jacket,
                            "right_arm", source.rightArm,
                            "left_arm", source.leftArm,
                            "right_leg", empty,
                            "left_leg", empty
                    );
                } else if (type == ArmorType.LEGGINGS) {
                    parts = Map.of(
                            "head", empty,
                            "hat", empty,
                            "body", empty,
                            "right_arm", empty,
                            "left_arm", empty,
                            "right_leg", source.rightLeg,
                            "left_leg", source.leftLeg
                    );
                } else {
                    parts = Map.of(
                            "head", empty,
                            "hat", empty,
                            "body", empty,
                            "right_arm", empty,
                            "left_arm", empty,
                            "right_leg", source.rightBoots,
                            "left_leg", source.leftBoots
                    );
                }
                HumanoidModel<?> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(), parts));

                return armorModel;
            }
        };
    }


}
