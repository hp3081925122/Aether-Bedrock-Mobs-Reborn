package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.aetherbedrockmobsreborn.item.CrystaliteSwordItem;
import com.aetherbedrockmobsreborn.item.DungeonDaggerItem;
import com.aetherbedrockmobsreborn.item.PyreSpearItem;
import com.aetherbedrockmobsreborn.item.SaltInstrumentItem;
import com.aetherbedrockmobsreborn.item.SpiritSwordItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, RegistryObject<Item>> ALL = new LinkedHashMap<>();

    static {
        for (Map.Entry<String, RegistryObject<net.minecraft.world.level.block.Block>> entry : ModBlocks.ALL.entrySet()) {
            register(entry.getKey(), () -> new BlockItem(entry.getValue().get(), new Item.Properties()));
        }
        register("citykey", () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE)));
        register("crabmeat", () -> new Item(new Item.Properties().stacksTo(64).food(new FoodProperties.Builder().nutrition(4).saturationMod(0.3F).build())));
        register("crabmeatcooked", () -> new Item(new Item.Properties().stacksTo(64).food(new FoodProperties.Builder().nutrition(8).saturationMod(0.6F).build())));
        register("crystalite", () -> new Item(new Item.Properties().stacksTo(64)));
        register("crystalitedust", () -> new Item(new Item.Properties().stacksTo(64)));
        register("crystalitesword", CrystaliteSwordItem::new);
        register("dungeondagger", DungeonDaggerItem::new);
        register("finalkey", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        register("pyrespear", PyreSpearItem::new);
        register("saltinstrument", SaltInstrumentItem::new);
        register("spiritsword", SpiritSwordItem::new);
        String[] spawnEggs = {
                "aether_naga", "buterfly",
                "crystalitegolem", "crystaliteminion", "eldershulker", "guardianangel", "mutatedcritter", "percipitus", "saltcrab",
                "saltcrabking", "shimaenaga", "spirit", "twinkler"
        };
        for (String id : spawnEggs) {
            RegistryObject<net.minecraft.world.entity.EntityType<RebornMobEntity>> entity = ModEntities.MOBS.get(id);
            if (entity != null) {
                int[] colors = spawnEggColors(id);
                register(id + "_spawn_egg", () -> new ForgeSpawnEggItem(entity::get, colors[0], colors[1], new Item.Properties()));
            }
        }
    }

    private static int[] spawnEggColors(String id) {
        return switch (id) {
            case "shimaenaga" -> new int[]{-1908244, -8350532};
            case "twinkler", "buterfly" -> new int[]{-1252895, -4423552};
            case "crystalitegolem" -> new int[]{-14755911, -1450953};
            case "percipitus" -> new int[]{-8482125, -2164745};
            case "crystaliteminion" -> new int[]{-15532088, -14887803};
            case "mutatedcritter" -> new int[]{-4234544, -11778986};
            case "aether_naga" -> new int[]{-11778986, -4234544};
            case "spirit" -> new int[]{-9858064, -1586885};
            case "saltcrab" -> new int[]{-527894, -2829364};
            case "saltcrabking" -> new int[]{-527894, -1256960};
            case "eldershulker" -> new int[]{-1727951, -12985922};
            case "guardianangel" -> new int[]{-394759, -6018};
            default -> new int[]{-1, -1};
        };
    }

    private static void register(String id, Supplier<? extends Item> supplier) {
        ALL.put(id, ITEMS.register(id, supplier));
    }

    private ModItems() {
    }
}

