package com.aetherbedrockmobsreborn.entity;

import com.aetherbedrockmobsreborn.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public final class GroundQuakeEffects {
    private GroundQuakeEffects() {
    }

    public static void spawnQuakeRing(Level level, double cx, double cy, double cz, double inner, double outer, double lift, RandomSource random) {
        int range = Mth.ceil(outer);
        int baseY = Mth.floor(cy - 0.5D);
        int bx0 = Mth.floor(cx);
        int bz0 = Mth.floor(cz);
        for (int bx = bx0 - range; bx <= bx0 + range; bx++) {
            for (int bz = bz0 - range; bz <= bz0 + range; bz++) {
                double dx = bx + 0.5D - cx;
                double dz = bz + 0.5D - cz;
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance < inner || distance > outer) {
                    continue;
                }
                quakeColumn(level, bx, baseY, bz, lift, random);
            }
        }
    }

    public static void spawnQuakeLine(Level level, double x0, double y, double z0, double x1, double z1, double halfWidth, double lift, RandomSource random) {
        int baseY = Mth.floor(y - 0.5D);
        int minX = Mth.floor(Math.min(x0, x1) - halfWidth);
        int maxX = Mth.floor(Math.max(x0, x1) + halfWidth);
        int minZ = Mth.floor(Math.min(z0, z1) - halfWidth);
        int maxZ = Mth.floor(Math.max(z0, z1) + halfWidth);
        double sx = x1 - x0;
        double sz = z1 - z0;
        double lengthSqr = Math.max(1.0E-4D, sx * sx + sz * sz);
        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                double px = bx + 0.5D - x0;
                double pz = bz + 0.5D - z0;
                double t = Mth.clamp((px * sx + pz * sz) / lengthSqr, 0.0D, 1.0D);
                double dx = px - sx * t;
                double dz = pz - sz * t;
                if (dx * dx + dz * dz > halfWidth * halfWidth) {
                    continue;
                }
                quakeColumn(level, bx, baseY, bz, lift * (0.7D + 0.5D * t), random);
            }
        }
    }

    private static void quakeColumn(Level level, int bx, int baseY, int bz, double lift, RandomSource random) {
        for (int y = baseY + 1; y >= baseY - 2; y--) {
            BlockPos pos = new BlockPos(bx, y, bz);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.isCollisionShapeFullBlock(level, pos)) {
                continue;
            }
            if (level.getBlockState(pos.above()).isCollisionShapeFullBlock(level, pos.above())) {
                return;
            }
            level.addParticle(new BlockParticleOption(ModParticles.QUAKE_BLOCK.get(), state), bx, y, bz, 0.0D, lift * (0.8D + random.nextDouble() * 0.4D), 0.0D);
            return;
        }
    }

}
