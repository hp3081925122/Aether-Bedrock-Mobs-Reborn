package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AetherBedrockMobsReborn.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEM_TAB = TABS.register("aetherbossitem", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("item_group.aether_bedrock_mobs_reborn.aetherbossitem"))
                    .icon(() -> ModItems.ALL.get("spiritsword").get().getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.ALL.entrySet().stream()
                            .filter(entry -> !(entry.getValue().get() instanceof BlockItem))
                            .map(Map.Entry::getValue)
                            .map(DeferredHolder::get)
                            .forEach(item -> output.accept(new ItemStack(item))))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCK_TAB = TABS.register("aetherboss", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("item_group.aether_bedrock_mobs_reborn.aetherboss"))
                    .icon(() -> ModItems.ALL.get("crystalitebricks").get().getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.ALL.entrySet().stream()
                            .filter(entry -> entry.getValue().get() instanceof BlockItem)
                            .map(Map.Entry::getValue)
                            .map(DeferredHolder::get)
                            .forEach(item -> output.accept(new ItemStack(item))))
                    .build());

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }

    private ModCreativeTabs() {
    }
}
