package com.aetherbedrockmobsreborn.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 迷宫树叶方块：继承原版树叶以兼容树叶剔除模组，并禁用自然枯萎。
 */
public final class LabyrinthLeavesBlock extends LeavesBlock {
    public LabyrinthLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        // 默认标记为持久化，兼容结构 NBT 与旧存档且避免自然腐烂
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, DECAY_DISTANCE)
                .setValue(PERSISTENT, Boolean.TRUE)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return false;
    }

    @Override
    protected boolean decaying(BlockState state) {
        return false;
    }
}
