package com.aetherbedrockmobsreborn.mixin.sharedcontent;

import com.aetherbedrockmobsreborn.block.DragonSummoning;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.aetherdragon.block.SpecialSpawnerBlock", remap = false)
public abstract class DragonSpawnerMixin {
    @Inject(method = "checkPlayerProximity", at = @At("HEAD"), cancellable = true, remap = false)
    private void aetherBedrockMobsReborn$preventAutomaticDragon(ServerLevel level, BlockPos pos, Player player, CallbackInfo callback) {
        if (DragonSummoning.isKeySpawner(((Block) (Object) this).defaultBlockState())) {
            callback.cancel();
        }
    }

    @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
    private void aetherBedrockMobsReborn$requireFinalKey(Level level, BlockPos pos, Player player, CallbackInfoReturnable<Boolean> callback) {
        if (DragonSummoning.isKeySpawner(((Block) (Object) this).defaultBlockState())) {
            callback.setReturnValue(DragonSummoning.activateDragon(level, pos, player));
        }
    }
}
