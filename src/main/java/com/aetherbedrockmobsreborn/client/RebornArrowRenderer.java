package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.entity.RebornProjectileEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public final class RebornArrowRenderer extends ArrowRenderer<RebornProjectileEntity, ArrowRenderState> {
    public RebornArrowRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override
    public ArrowRenderState createRenderState() { return new ArrowRenderState(); }
    @Override
    public Identifier getTextureLocation(ArrowRenderState state) {
        return Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    }
}
