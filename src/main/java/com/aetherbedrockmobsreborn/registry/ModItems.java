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
import net.minecraft.core.registries.Registries;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, DeferredHolder<Item, Item>> ALL = new LinkedHashMap<>();

    static {
        for (Map.Entry<String, DeferredHolder<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block>> entry : ModBlocks.ALL.entrySet()) {
            register(entry.getKey(), properties -> new BlockItem(entry.getValue().get(), properties));
        }
        register("citykey", properties -> new Item(properties.stacksTo(64).rarity(Rarity.RARE)));
        register("crabmeat", properties -> new Item(properties.stacksTo(64).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).build())));
        register("crabmeatcooked", properties -> new Item(properties.stacksTo(64).food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build())));
        register("crystalite", properties -> new Item(properties.stacksTo(64)));
        register("crystalitedust", properties -> new Item(properties.stacksTo(64)));
        register("crystalitesword", CrystaliteSwordItem::new);
        register("dungeondagger", DungeonDaggerItem::new);
        register("finalkey", properties -> new Item(properties.stacksTo(1).rarity(Rarity.EPIC)));
        register("pyrespear", PyreSpearItem::new);
        register("saltinstrument", SaltInstrumentItem::new);
        register("spiritsword", SpiritSwordItem::new);
        String[] spawnEggs = {
                "aether_naga", "buterfly",
                "crystalitegolem", "crystaliteminion", "eldershulker", "guardianangel", "mutatedcritter", "percipitus", "saltcrab",
                "saltcrabking", "shimaenaga", "spirit", "twinkler"
        };
        for (String id : spawnEggs) {
            DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<RebornMobEntity>> entity = ModEntities.MOBS.get(id);
            if (entity != null) {
                register(id + "_spawn_egg", properties -> new net.minecraft.world.item.SpawnEggItem(properties.spawnEgg(entity.get())));
            }
        }
    }

    private static void register(String id, Function<Item.Properties, ? extends Item> factory) {
        ALL.put(id, ITEMS.register(id, () -> factory.apply(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, id))))));
    }

    private ModItems() {
    }
}

