package com.aetherbedrockmobsreborn;

import com.aetherbedrockmobsreborn.registry.ModBlocks;
import com.aetherbedrockmobsreborn.registry.ModCreativeTabs;
import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import com.aetherbedrockmobsreborn.registry.ModSounds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;

@Mod(AetherBedrockMobsReborn.MOD_ID)
public final class AetherBedrockMobsReborn {
    public static final String MOD_ID = "aether_bedrock_mobs_reborn";

    public AetherBedrockMobsReborn(IEventBus bus, net.neoforged.fml.ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,
                com.aetherbedrockmobsreborn.config.BossSkillConfig.SPEC, MOD_ID + "-boss-skills.toml");
        com.aetherbedrockmobsreborn.registry.SharedContentMigration.registerAliases();
        ModBlocks.BLOCKS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.register(bus);
        ModParticles.PARTICLES.register(bus);
        ModSounds.SOUNDS.register(bus);
        bus.addListener(this::registerAttributes);
        bus.addListener(this::registerSpawnPlacements);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (var entry : ModEntities.MOBS.entrySet()) {
            event.put(entry.getValue().get(), com.aetherbedrockmobsreborn.entity.RebornMobEntity.createAttributes(entry.getKey()).build());
        }
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (String id : new String[]{"shimaenaga", "twinkler", "buterfly", "percipitus"}) {
            event.register(ModEntities.MOBS.get(id).get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }
}



