package com.aetherbedrockmobsreborn.client.particle;

import com.aetherbedrockmobsreborn.client.GeometryCapture;
import net.minecraft.client.Camera;

public interface CapturedParticle {
    void extractGeometry(GeometryCapture geometry, Camera camera, float partialTick);
}
