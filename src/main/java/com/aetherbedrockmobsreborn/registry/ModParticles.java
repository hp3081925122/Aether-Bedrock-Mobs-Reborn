package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AetherBedrockMobsReborn.MOD_ID);
    public static final RegistryObject<SimpleParticleType> RUNESEFFECT = PARTICLES.register("runeseffect", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> WHITESMOKE = PARTICLES.register("whitesmoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FIRE = PARTICLES.register("fire", () -> new SimpleParticleType(false));
    // 艾瑟龙转阶段冲击波与碎鳞
    public static final RegistryObject<SimpleParticleType> DRAGON_SHOCKWAVE = PARTICLES.register("dragon_shockwave", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> DRAGON_SHARD = PARTICLES.register("dragon_shard", () -> new SimpleParticleType(true));
    // 艾瑟龙技能落点预警环
    public static final RegistryObject<SimpleParticleType> DRAGON_WARNING = PARTICLES.register("dragon_warning", () -> new SimpleParticleType(true));
    // 艾瑟龙二阶段：爪痕刀光、被震起的地面方块
    public static final RegistryObject<SimpleParticleType> DRAGON_SLASH = PARTICLES.register("dragon_slash", () -> new SimpleParticleType(true));
    public static final RegistryObject<ParticleType<BlockParticleOption>> DRAGON_QUAKE_BLOCK = PARTICLES.register("dragon_quake_block", () -> new ParticleType<BlockParticleOption>(true, BlockParticleOption.DESERIALIZER) {
        @Override
        public Codec<BlockParticleOption> codec() {
            return BlockParticleOption.codec(this);
        }
    });

    private ModParticles() {
    }
}
