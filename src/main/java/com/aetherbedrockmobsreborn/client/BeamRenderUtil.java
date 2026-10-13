package com.aetherbedrockmobsreborn.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

final class BeamRenderUtil {
    private static final int FULL_BRIGHT = 15728880;

    private BeamRenderUtil() {
    }

    static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v) {
        consumer.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
