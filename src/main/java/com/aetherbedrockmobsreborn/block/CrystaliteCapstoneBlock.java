package com.aetherbedrockmobsreborn.block;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.BlockPos;

public final class CrystaliteCapstoneBlock extends RotatedPillarBlock {
    public CrystaliteCapstoneBlock(BlockBehaviour.Properties properties) {
        super(properties
                .mapColor(MapColor.DIAMOND)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .sound(SoundType.STONE)
                .strength(3.0F, 10.0F)
                .lightLevel(state -> 15)
                .noOcclusion()
                .isValidSpawn((state, getter, pos, entityType) -> false));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

}
