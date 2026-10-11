package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
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

    private ModParticles() {
    }
}
