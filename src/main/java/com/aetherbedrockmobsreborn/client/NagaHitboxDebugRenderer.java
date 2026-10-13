package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.entity.NagaPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.Vec3;

final class NagaHitboxDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    NagaHitboxDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        if (minecraft.level == null) {
            return;
        }
        for (var part : minecraft.level.dragonParts()) {
            if (part instanceof NagaPart && part.getParent().isAlive() && !part.getParent().isInvisible()) {
                float tick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(!minecraft.level.tickRateManager().isEntityFrozen(part.getParent()));
                Vec3 offset = part.getPosition(tick).subtract(part.position());
                var box = part.getBoundingBox().move(offset);
                if (frustum.isVisible(box)) {
                    Gizmos.cuboid(box, GizmoStyle.stroke(0xFF40FF00));
                }
            }
        }
    }
}
