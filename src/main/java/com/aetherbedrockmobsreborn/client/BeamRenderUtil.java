package com.aetherbedrockmobsreborn.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

final class BeamRenderUtil {
    private static final int FULL_BRIGHT = 15728880;
    static final RenderPipeline PIPELINE = RenderPipelines.EYES.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID, "pipeline/boss_beam"))
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();
    private static final Map<Identifier, RenderType> BEAM_TYPES = new HashMap<>();

    private BeamRenderUtil() {
    }

    static RenderType renderType(Identifier texture) {
        return BEAM_TYPES.computeIfAbsent(texture, id -> RenderType.create("reborn_additive_beam",
                RenderSetup.builder(PIPELINE).withTexture("Sampler0", id).sortOnUpload().createRenderSetup()));
    }

    static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }
}
