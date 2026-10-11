package com.aetherbedrockmobsreborn.block;

import com.aetherbedrockmobsreborn.registry.ModEntities;
import com.aetherbedrockmobsreborn.registry.ModItems;
import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SpecialSpawnerBlock extends Block {
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private final String action;

    public SpecialSpawnerBlock(String action) {
        super(action.equals("lamp")
                ? BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(state -> 4).noOcclusion()
                : BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(state -> 4));
        this.action = action;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    public void checkPlayerProximity(ServerLevel level, BlockPos pos, Player player) {
        if (!isProximityAction() || player.isSpectator()) {
            return;
        }
        double dx = player.getX() - (pos.getX() + 0.5D);
        double dy = player.getY() - (pos.getY() + 0.5D);
        double dz = player.getZ() - (pos.getZ() + 0.5D);
        if (dx * dx + dy * dy + dz * dz <= 64.0D) {
            activate(level, pos, player);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || (!action.equals("dragon") && !action.equals("lamp"))) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        activate(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private boolean isProximityAction() {
        return action.equals("crystalite") || action.equals("elder") || action.equals("salt") || action.equals("labyrinth");
    }

    private boolean activate(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return false;
        }
        if (action.equals("dragon") && player != null) {
            DeferredHolder<net.minecraft.world.item.Item, net.minecraft.world.item.Item> key = ModItems.ALL.get("finalkey");
            if (key == null || !player.getMainHandItem().is(key.get())) {
                player.displayClientMessage(Component.translatable("message.aether_bedrock_mobs_reborn.key_required"), true);
                return false;
            }
        }
        if ((action.equals("elder") || action.equals("crystalite") || action.equals("labyrinth")) && level.isDay()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.aether_bedrock_mobs_reborn.night_only"), true);
            }
            return false;
        }
        String entityId = switch (action) {
            case "dragon" -> "aetherdragon";
            case "elder" -> "eldershulker";
            case "crystalite" -> "crystalitegolem";
            case "salt" -> "saltcrabking";
            case "labyrinth" -> "aether_naga";
            case "lamp" -> "spirit";
            default -> null;
        };
        if (entityId == null) {
            return false;
        }
        DeferredHolder<EntityType<?>, EntityType<RebornMobEntity>> type = ModEntities.MOBS.get(entityId);
        if (type == null) {
            return false;
        }
        RebornMobEntity entity = type.get().create(level);
        if (entity == null) {
            return false;
        }
        double y = pos.getY() + (action.equals("dragon") ? 10.0D : 1.0D);
        entity.moveTo(pos.getX() + 0.5D, y, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity);
        if (level instanceof ServerLevel server) {
            double particleY = pos.getY() + (action.equals("dragon") ? 10.0D : 1.0D);
            server.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5D, particleY, pos.getZ() + 0.5D, 5, 3.0D, 3.0D, 3.0D, 1.0D);
            server.sendParticles(ModParticles.FIRE.get(), pos.getX() + 0.5D, particleY, pos.getZ() + 0.5D, 10, 3.0D, 3.0D, 3.0D, 1.0D);
            server.sendParticles(ModParticles.RUNESEFFECT.get(), pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 16, 0.5D, 0.5D, 0.5D, 0.05D);
        }
        if (action.equals("dragon")) {
            level.explode(null, pos.getX() + 0.5D, y, pos.getZ() + 0.5D, 0.0F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }
}




