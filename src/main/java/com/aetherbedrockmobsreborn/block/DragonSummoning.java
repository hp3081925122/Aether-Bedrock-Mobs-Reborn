package com.aetherbedrockmobsreborn.block;

import com.aetherbedrockmobsreborn.registry.ModBlocks;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class DragonSummoning {
    private static final Queue<WeakReference<LevelChunk>> PENDING_CHUNKS = new ConcurrentLinkedQueue<>();
    private static Block externalSpawner;

    private DragonSummoning() {
    }

    public static boolean isDragon(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().equals("aether_dragon:aetherdragon");
    }

    public static boolean isKeySpawner(BlockState state) {
        return state.getBlock() == ModBlocks.ALL.get("dragonspawner").get();
    }

    public static boolean isAutomaticSpawner(Level level, BlockPos pos, BlockState state) {
        if (externalSpawner == null) {
            externalSpawner = BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("aether_dragon", "dragonspawner")).orElse(null);
        }
        if (state.getBlock() == externalSpawner) {
            return true;
        }
        if (!state.is(Blocks.SPAWNER) || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return false;
        }
        CompoundTag tag = spawner.getUpdateTag(level.registryAccess());
        CompoundTag spawnData = tag.getCompound("SpawnData");
        String entityId = spawnData.getCompound("entity").getString("id");
        return entityId.equals("aether_dragon:aetherdragon");
    }

    public static BlockState convertAutomaticSpawner(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isAutomaticSpawner(level, pos, state)) {
            return state;
        }
        BlockState replacement = ModBlocks.ALL.get("dragonspawner").get().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.POWERED)) {
            replacement = replacement.setValue(BlockStateProperties.POWERED, state.getValue(BlockStateProperties.POWERED));
        }
        if (level.setBlock(pos, replacement, 3)) {
            LogUtils.getLogger().debug("Converted automatic Aether Dragon spawner to key-gated spawner at {}", pos);
            return replacement;
        }
        return state;
    }

    public static void queueChunk(LevelChunk chunk) {
        if (chunk.getLevel() instanceof ServerLevel && !chunk.getBlockEntitiesPos().isEmpty()) {
            PENDING_CHUNKS.offer(new WeakReference<>(chunk));
        }
    }

    public static void processPendingChunks(MinecraftServer server) {
        int remaining = PENDING_CHUNKS.size();
        while (remaining-- > 0) {
            WeakReference<LevelChunk> reference = PENDING_CHUNKS.poll();
            if (reference == null) {
                return;
            }
            LevelChunk chunk = reference.get();
            if (chunk == null || !(chunk.getLevel() instanceof ServerLevel level) || level.getServer() != server
                    || level.getChunkSource().getChunkNow(chunk.getPos().x, chunk.getPos().z) != chunk) {
                continue;
            }
            for (BlockPos pos : List.copyOf(chunk.getBlockEntitiesPos())) {
                BlockState state = chunk.getBlockState(pos);
                if (state.is(Blocks.SPAWNER)) {
                    convertAutomaticSpawner(level, pos, state);
                }
            }
        }
    }
}
