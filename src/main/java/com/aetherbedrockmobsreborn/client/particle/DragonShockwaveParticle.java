package com.aetherbedrockmobsreborn.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// 水平展开的冲击波圆环，xd 为最大半径，yd 为持续 tick
public class DragonShockwaveParticle extends TextureSheetParticle {
    private final float maxRadius;

    protected DragonShockwaveParticle(ClientLevel level, double x, double y, double z, double radius, double life, SpriteSet sprites) {
        super(level, x, y, z);
        // 半径与寿命由生成参数决定
        this.maxRadius = (float) Math.max(1.0D, radius);
        this.lifetime = Math.max(4, (int) life);
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.pickSprite(sprites);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // 计算相对相机的位置
        Vec3 cam = camera.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cam.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cam.y());
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cam.z());
        // 先快后慢地扩张，后半段淡出
        float progress = Mth.clamp((this.age + partialTicks) / this.lifetime, 0.0F, 1.0F);
        float inverse = 1.0F - progress;
        float radius = this.maxRadius * (1.0F - inverse * inverse * inverse);
        float alpha = 1.0F - progress * progress;
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = 15728880;
        // 朝上的一面
        buffer.vertex(px - radius, py, pz - radius).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px - radius, py, pz + radius).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px + radius, py, pz + radius).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px + radius, py, pz - radius).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        // 朝下的一面
        buffer.vertex(px + radius, py, pz - radius).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px + radius, py, pz + radius).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px - radius, py, pz + radius).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
        buffer.vertex(px - radius, py, pz - radius).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new DragonShockwaveParticle(level, x, y, z, xd, yd, this.sprites);
        }
    }
}
