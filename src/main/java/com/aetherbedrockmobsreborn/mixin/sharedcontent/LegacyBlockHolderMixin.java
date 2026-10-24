package com.aetherbedrockmobsreborn.mixin.sharedcontent;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.registry.SharedContentMigration;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper", remap = false)
public abstract class LegacyBlockHolderMixin {
    @ModifyVariable(method = {"getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", "m_203636_(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;"}, at = @At("HEAD"), argsOnly = true, remap = false)
    private ResourceKey<?> aetherBedrockMobsReborn$resolveLegacyBlock(ResourceKey<?> key) {
        ResourceLocation name = key.location();
        if (key.isFor(Registries.BLOCK) && name.getNamespace().equals(AetherBedrockMobsReborn.MOD_ID)
                && SharedContentMigration.BLOCK_IDS.contains(name.getPath())) {
            return ResourceKey.create(Registries.BLOCK, new ResourceLocation("aether_dragon", name.getPath()));
        }
        return key;
    }
}
