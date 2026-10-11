package com.aetherbedrockmobsreborn;

import com.aetherbedrockmobsreborn.registry.ModBlocks;
import com.aetherbedrockmobsreborn.registry.ModCreativeTabs;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AetherBedrockMobsReborn.MOD_ID)
public final class AetherBedrockMobsReborn {
    public static final String MOD_ID = "aether_bedrock_mobs_reborn";

    public AetherBedrockMobsReborn(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.register(bus);
        ModParticles.PARTICLES.register(bus);
        ModSounds.SOUNDS.register(bus);
        bus.addListener(this::registerAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (var entry : ModEntities.MOBS.entrySet()) {
            event.put(entry.getValue().get(), com.aetherbedrockmobsreborn.entity.RebornMobEntity.createAttributes(entry.getKey()).build());
        }
    }
}



