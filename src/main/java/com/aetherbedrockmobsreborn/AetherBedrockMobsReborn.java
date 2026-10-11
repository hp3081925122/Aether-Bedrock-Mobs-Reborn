package com.aetherbedrockmobsreborn;

import com.aetherbedrockmobsreborn.registry.ModBlocks;
import com.aetherbedrockmobsreborn.registry.ModCreativeTabs;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModArmorMaterials;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.Mod;

@Mod(AetherBedrockMobsReborn.MOD_ID)
public final class AetherBedrockMobsReborn {
    public static final String MOD_ID = "aether_bedrock_mobs_reborn";

    public AetherBedrockMobsReborn(IEventBus bus) {
        ModArmorMaterials.ARMOR_MATERIALS.register(bus);
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



