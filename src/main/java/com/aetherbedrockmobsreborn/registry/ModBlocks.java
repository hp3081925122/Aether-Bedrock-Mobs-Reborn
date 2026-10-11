package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.block.CrystaliteCapstoneBlock;
import com.aetherbedrockmobsreborn.block.SpecialSpawnerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, RegistryObject<Block>> ALL = new LinkedHashMap<>();

    static {
        String[] ids = {
                "aureatebluebeam", "aureatebluebeambrick", "aureatebluehead", "aureatebluepillar", "aureatebricks",
                "aureatechiseled", "aureatecobblestone", "aureatekeystone", "aureateleaves", "aureatepillar",
                "aureatepillarbeam", "aureatepillarhead", "aureatepillarheadbrick", "aureatescript", "aureatetilebricks",
                "aureatetiles", "aurorastone", "blockofsalt", "blueglazedmosaic", "cobbleddeepslate", "crystalitebricks",
                "crystalitecapstone", "crystalitecolumnhead", "crystalitecolumnheadlit", "crystalitekeystone", "crystalitespawner",
                "crystalitestone", "crystalitestonescript", "deepdirt", "deepgrass", "dragonspawner", "drysaltstone",
                "elderspawner", "grassydeadrock", "labyrinthfloor_1", "labyrinthfloor_2", "labyrinthfloor_3", "labyrinthleaves",
                "labyrinthwall_1", "labyrinthwall_2", "labyrinthwall_3", "labyrinthwall_4", "labyrinthwall_5", "labyrinthwall_6",
                "labyrinthwall_7", "labyrintspawner", "lamp", "layeredsaltstone", "mosaicblue", "mosaicwhite",
                "mossydeadrockbrick", "redglazedmosaic", "saltspawner", "saltstonebricks", "saltstonepillarside", "saltstonesmallbricks"
        };
        for (String id : ids) {
            ALL.put(id, BLOCKS.register(id, () -> id.equals("crystalitecapstone")
                    ? new CrystaliteCapstoneBlock()
                    : isSpecialSpawner(id)
                    ? new SpecialSpawnerBlock(getSpawnerAction(id))
                    : new Block(BlockBehaviour.Properties.of().strength(1.5F))));
        }
    }

    private static boolean isSpecialSpawner(String id) {
        return id.equals("crystalitespawner") || id.equals("dragonspawner") || id.equals("elderspawner")
                || id.equals("labyrintspawner") || id.equals("saltspawner") || id.equals("lamp");
    }

    private static String getSpawnerAction(String id) {
        return switch (id) {
            case "crystalitespawner" -> "crystalite";
            case "dragonspawner" -> "dragon";
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
