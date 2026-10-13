package com.aetherbedrockmobsreborn.entity;

import com.aetherbedrockmobsreborn.config.BossSkillConfig;
import com.aetherbedrockmobsreborn.config.BossSkillConfig.Skill;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class BossSkillDamage {
    private BossSkillDamage() {
    }

    public static double attackDamage(LivingEntity owner) {
        return owner.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public static float calculate(LivingEntity owner, Skill skill) {
        return calculate(attackDamage(owner), skill);
    }

    public static float calculate(Entity owner, double fallbackAttackDamage, Skill skill) {
        return calculate(owner instanceof LivingEntity living ? attackDamage(living) : fallbackAttackDamage, skill);
    }

    public static float calculate(double attackDamage, Skill skill) {
        if (!Double.isFinite(attackDamage) || attackDamage <= 0.0D) {
            return 0.0F;
        }
        return (float) Math.min(Float.MAX_VALUE, attackDamage * BossSkillConfig.multiplier(skill));
    }
}
