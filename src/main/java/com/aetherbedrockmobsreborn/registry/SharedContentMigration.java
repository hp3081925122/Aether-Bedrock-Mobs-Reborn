package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

public final class SharedContentMigration {
    public static final List<String> BLOCK_IDS = List.of("aureatebluebeam", "aureatebluebeambrick", "aureatebluehead", "aureatebluepillar", "aureatebricks", "aureatechiseled", "aureatecobblestone", "aureatekeystone", "aureateleaves", "aureatepillar", "aureatepillarbeam", "aureatepillarhead", "aureatepillarheadbrick", "aureatescript", "aureatetilebricks", "aureatetiles", "aurorastone", "dragonspawner");
    public static final List<String> ARMOR_IDS = List.of("shaday_helmet", "shaday_chestplate", "shaday_leggings");

    private SharedContentMigration() {
    }

    public static void registerAliases() {
        for (String id : BLOCK_IDS) {
            ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).addAlias(new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, id), new ResourceLocation("aether_dragon", id));
            ((ForgeRegistry<Item>) ForgeRegistries.ITEMS).addAlias(new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, id), new ResourceLocation("aether_dragon", id));
        }
        for (String id : ARMOR_IDS) {
            ((ForgeRegistry<Item>) ForgeRegistries.ITEMS).addAlias(new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, id), new ResourceLocation("aether_dragon", id));
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(SharedContentMigration::remapMissingMappings);
        com.mojang.logging.LogUtils.getLogger().debug("Registered Aether Dragon content aliases: blocks={} items={}", BLOCK_IDS.size(), BLOCK_IDS.size() + ARMOR_IDS.size());
    }

    private static void remapMissingMappings(MissingMappingsEvent event) {
        for (var mapping : event.getMappings(ForgeRegistries.Keys.BLOCKS, AetherBedrockMobsReborn.MOD_ID)) {
            if (BLOCK_IDS.contains(mapping.getKey().getPath())) {
                Block target = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("aether_dragon", mapping.getKey().getPath()));
                if (target == null || target == net.minecraft.world.level.block.Blocks.AIR) {
                    throw new IllegalStateException("Missing Aether Dragon block migration target: " + mapping.getKey());
                }
                mapping.remap(target);
                com.mojang.logging.LogUtils.getLogger().info("Remapped legacy block {} to {}", mapping.getKey(), ForgeRegistries.BLOCKS.getKey(target));
            }
        }
        for (var mapping : event.getMappings(ForgeRegistries.Keys.ITEMS, AetherBedrockMobsReborn.MOD_ID)) {
            if (BLOCK_IDS.contains(mapping.getKey().getPath()) || ARMOR_IDS.contains(mapping.getKey().getPath())) {
                Item target = ForgeRegistries.ITEMS.getValue(new ResourceLocation("aether_dragon", mapping.getKey().getPath()));
                if (target == null || target == net.minecraft.world.item.Items.AIR) {
                    throw new IllegalStateException("Missing Aether Dragon item migration target: " + mapping.getKey());
                }
                mapping.remap(target);
                com.mojang.logging.LogUtils.getLogger().info("Remapped legacy item {} to {}", mapping.getKey(), ForgeRegistries.ITEMS.getKey(target));
            }
        }
    }

}
