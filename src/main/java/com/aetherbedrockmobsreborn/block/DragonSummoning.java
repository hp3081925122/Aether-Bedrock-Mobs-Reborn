package com.aetherbedrockmobsreborn.block;

import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
        return state.getBlock() == dragonSpawner();
    }

    public static boolean isAutomaticSpawner(Level level, BlockPos pos, BlockState state) {
        if (!state.is(Blocks.SPAWNER) || !(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            return false;
        }
        CompoundTag tag = spawner.getUpdateTag();
        CompoundTag spawnData = tag.getCompound("SpawnData");
        String entityId = spawnData.getCompound("entity").getString("id");
        return entityId.equals("aether_dragon:aetherdragon");
    }

    public static BlockState convertAutomaticSpawner(ServerLevel level, BlockPos pos, BlockState state) {
        if (!isAutomaticSpawner(level, pos, state)) {
            return state;
        }
        BlockState replacement = dragonSpawner().defaultBlockState();
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

    private static Block dragonSpawner() {
        if (externalSpawner == null) {
            externalSpawner = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation("aether_dragon", "dragonspawner")).orElseThrow();
        }
        return externalSpawner;
    }

    public static boolean activateDragon(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level instanceof ServerLevel server) || player == null || player.isSpectator() || !isKeySpawner(level.getBlockState(pos))) {
            return false;
        }
        if (!player.getMainHandItem().is(ModItems.ALL.get("finalkey").get())) {
            player.displayClientMessage(Component.translatable("message.aether_bedrock_mobs_reborn.key_required"), true);
            return false;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(new ResourceLocation("aether_dragon", "aetherdragon")).orElseThrow();
        Entity entity = type.create(level);
        if (entity == null) {
            return false;
        }
        double y = pos.getY() + 10.0D;
        entity.moveTo(pos.getX() + 0.5D, y, pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        if (!server.addFreshEntity(entity)) {
            LogUtils.getLogger().warn("Spawner activation failed: could not add Aether Dragon at {}", pos);
            return false;
        }
        server.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5D, y, pos.getZ() + 0.5D, 5, 3.0D, 3.0D, 3.0D, 1.0D);
        server.sendParticles(ModParticles.FIRE.get(), pos.getX() + 0.5D, y, pos.getZ() + 0.5D, 10, 3.0D, 3.0D, 3.0D, 1.0D);
        server.sendParticles(ModParticles.RUNESEFFECT.get(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 16, 0.5D, 0.5D, 0.5D, 0.05D);
        server.explode(null, pos.getX() + 0.5D, y, pos.getZ() + 0.5D, 0.0F, Level.ExplosionInteraction.NONE);
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        LogUtils.getLogger().debug("Activated shared Aether Dragon spawner using the Final Key at {}", pos);
        return true;
    }
}
