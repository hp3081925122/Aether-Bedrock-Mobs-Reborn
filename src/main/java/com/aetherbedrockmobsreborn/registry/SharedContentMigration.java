package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public final class SharedContentMigration {
    public static final List<String> BLOCK_IDS = List.of("aureatebluebeam", "aureatebluebeambrick", "aureatebluehead", "aureatebluepillar", "aureatebricks", "aureatechiseled", "aureatecobblestone", "aureatekeystone", "aureateleaves", "aureatepillar", "aureatepillarbeam", "aureatepillarhead", "aureatepillarheadbrick", "aureatescript", "aureatetilebricks", "aureatetiles", "aurorastone", "dragonspawner");
    public static final List<String> ARMOR_IDS = List.of("shaday_helmet", "shaday_chestplate", "shaday_leggings");

    private SharedContentMigration() {
    }

    public static void registerAliases() {
        for (String id : BLOCK_IDS) {
            ModBlocks.BLOCKS.addAlias(ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, id), ResourceLocation.fromNamespaceAndPath("aether_dragon", id));
            ModItems.ITEMS.addAlias(ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, id), ResourceLocation.fromNamespaceAndPath("aether_dragon", id));
        }
        for (String id : ARMOR_IDS) {
            ModItems.ITEMS.addAlias(ResourceLocation.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, id), ResourceLocation.fromNamespaceAndPath("aether_dragon", id));
        }
        com.mojang.logging.LogUtils.getLogger().debug("Registered Aether Dragon content aliases: blocks={} items={}", BLOCK_IDS.size(), BLOCK_IDS.size() + ARMOR_IDS.size());
    }

}
