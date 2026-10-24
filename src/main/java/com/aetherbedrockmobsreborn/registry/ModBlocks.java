package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.block.CrystaliteCapstoneBlock;
import com.aetherbedrockmobsreborn.block.SpecialSpawnerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, DeferredHolder<Block, Block>> ALL = new LinkedHashMap<>();

    static {
        String[] ids = {
                "blockofsalt", "blueglazedmosaic", "cobbleddeepslate", "crystalitebricks", "crystalitecapstone",
                "crystalitecolumnhead", "crystalitecolumnheadlit", "crystalitekeystone", "crystalitespawner", "crystalitestone",
                "crystalitestonescript", "deepdirt", "deepgrass", "drysaltstone", "elderspawner",
                "grassydeadrock", "labyrinthfloor_1", "labyrinthfloor_2", "labyrinthfloor_3", "labyrinthleaves",
                "labyrinthwall_1", "labyrinthwall_2", "labyrinthwall_3", "labyrinthwall_4", "labyrinthwall_5",
                "labyrinthwall_6", "labyrinthwall_7", "labyrintspawner", "lamp", "layeredsaltstone",
                "mosaicblue", "mosaicwhite", "mossydeadrockbrick", "redglazedmosaic", "saltspawner",
                "saltstonebricks", "saltstonepillarside", "saltstonesmallbricks"
        };
        for (String id : ids) {
            ALL.put(id, BLOCKS.register(id, () -> id.equals("crystalitecapstone")
                    ? new CrystaliteCapstoneBlock(properties(id))
                    : isSpecialSpawner(id)
                    ? new SpecialSpawnerBlock(getSpawnerAction(id), properties(id))
                    : id.equals("aureateleaves") || id.equals("labyrinthleaves")
                    ? new Block(properties(id).mapColor(mapColor(id)).strength(1.5F).noOcclusion())
                    : new Block(properties(id).mapColor(mapColor(id)).strength(1.5F))));
        }
    }

    private static BlockBehaviour.Properties properties(String id) {
        return BlockBehaviour.Properties.of().setId(net.minecraft.resources.ResourceKey.create(Registries.BLOCK,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, id)));
    }

    private static MapColor mapColor(String id) {
        if (id.equals("aureateleaves") || id.equals("labyrinthleaves")) {
            return MapColor.PLANT;
        }
        if (id.contains("blue")) {
            return MapColor.COLOR_BLUE;
        }
        if (id.startsWith("crystalite")) {
            return MapColor.DIAMOND;
        }
        if (id.startsWith("labyrinth")) {
            return id.equals("labyrinthleaves") ? MapColor.PLANT : MapColor.DEEPSLATE;
        }
        if (id.contains("salt")) {
            return MapColor.COLOR_LIGHT_GRAY;
        }
        if (id.equals("deepdirt") || id.equals("deepgrass") || id.equals("grassydeadrock")) {
            return id.equals("deepgrass") ? MapColor.GRASS : MapColor.DIRT;
        }
        if (id.equals("redglazedmosaic")) {
            return MapColor.COLOR_RED;
        }
        if (id.startsWith("aureate") || id.equals("goldcity")) {
            return MapColor.GOLD;
        }
        return MapColor.STONE;
    }

    private static boolean isSpecialSpawner(String id) {
        return id.equals("crystalitespawner") || id.equals("elderspawner")
                || id.equals("labyrintspawner") || id.equals("saltspawner") || id.equals("lamp");
    }

    private static String getSpawnerAction(String id) {
        return switch (id) {
            case "crystalitespawner" -> "crystalite";
            case "elderspawner" -> "elder";
            case "labyrintspawner" -> "labyrinth";
            case "saltspawner" -> "salt";
            case "lamp" -> "lamp";
            default -> "";
        };
    }

    private ModBlocks() {
    }
}
