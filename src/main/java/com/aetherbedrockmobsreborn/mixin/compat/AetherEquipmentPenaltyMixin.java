package com.aetherbedrockmobsreborn.mixin.compat;

import com.aetherbedrockmobsreborn.compat.AetherEquipmentCompatibility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.aetherteam.aether.event.hooks.AbilityHooks$WeaponHooks", remap = false)
public abstract class AetherEquipmentPenaltyMixin {
    @Inject(method = "reduceWeaponEffectiveness", at = @At("HEAD"), cancellable = true, remap = false)
    private static void aetherBedrockMobsReborn$keepWeaponDamage(LivingEntity target, Entity source, float damage, CallbackInfoReturnable<Float> callback) {
        if (source instanceof LivingEntity attacker && AetherEquipmentCompatibility.isExempt(attacker.getMainHandItem())) {
            AetherEquipmentCompatibility.reportActive();
            callback.setReturnValue(damage);
        }
    }

    @Inject(method = "reduceArmorEffectiveness", at = @At("RETURN"), cancellable = true, remap = false)
    private static void aetherBedrockMobsReborn$keepArmorProtection(LivingEntity target, Entity source, float damage, CallbackInfoReturnable<Float> callback) {
        double penalty = AetherEquipmentCompatibility.exemptArmorPenalty(target, source);
        if (penalty != 0.0) {
            AetherEquipmentCompatibility.reportActive();
            callback.setReturnValue((float)(callback.getReturnValue() - penalty));
        }
    }
}
