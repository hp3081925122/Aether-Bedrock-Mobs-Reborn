package com.aetherbedrockmobsreborn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public final class SaltInstrumentItem extends Item {
    public SaltInstrumentItem(Item.Properties properties) {
        super(properties.durability(300).enchantable(2).attributes(createAttributes()));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.aether_bedrock_mobs_reborn.saltinstrument").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.MINEABLE_WITH_HOE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL) || state.is(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return 4.0F;
    }

    @Override
    public boolean canPerformAction(net.minecraft.world.item.ItemInstance stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
                || itemAbility == ItemAbilities.SWORD_SWEEP;
    }

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.EMPTY
                .withModifierAdded(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "saltinstrument_attack_damage"), 3.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Identifier.fromNamespaceAndPath("aether_bedrock_mobs_reborn", "saltinstrument_attack_speed"), -3.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
        if (stack.isEmpty()) {
            com.mojang.logging.LogUtils.getLogger().debug("Salt instrument broke for attacker {}", attacker.getUUID());
        }
    }
}


