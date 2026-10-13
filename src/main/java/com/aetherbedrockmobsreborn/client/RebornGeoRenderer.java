package com.aetherbedrockmobsreborn.client;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.NagaEntity;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RebornGeoRenderer extends GeoEntityRenderer<RebornMobEntity, RebornRenderState> {
    public RebornGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new RebornGeoModel());
        shadowRadius = 0.5F;
    }
    @Override
    public RebornRenderState createRenderState(RebornMobEntity entity, Void context) { return new RebornRenderState(); }
    @Override
    public void extractRenderState(RebornMobEntity entity, RebornRenderState state, float partialTick) {
        state.id = entity.getIdPath();
        state.texture = entity.getTextureName();
        super.extractRenderState(entity, state, partialTick);
        state.tailBend = entity instanceof NagaEntity naga ? naga.getTailBend(partialTick) * Mth.DEG_TO_RAD : 0;
        state.headBone = state.id.equals("spirit") && entity.getSpiritSkill() == 0 ? "head"
                : state.id.equals("eldershulker") && entity.getShulkerSkill() == 0 ? "shulker_head" : "";
        state.headLook = entity.level().getEntity(entity.getId()) == entity;
        boolean naga = entity.isNagaBeamActive();
        state.shulkerBeam = entity.isShulkerBeamActive();
        state.beam = naga || state.shulkerBeam;
        state.beamBone = "mouth";
        String beam = state.shulkerBeam ? "shulker" : "naga";
        state.beamTexture = Identifier.fromNamespaceAndPath(AetherBedrockMobsReborn.MOD_ID,
                "textures/entity/" + beam + "_skill/" + beam + "_beam.png");
        state.beamEnd = entity.getBossBeamEnd();
        state.beamYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
    }
    @Override
    public void adjustModelBonesForRender(RenderPassInfo<RebornRenderState> pass, BoneSnapshots bones) {
        RebornRenderState state = pass.renderState();
        String[] tail = {"tail1", "tail2", "tail3"};
        float[] weights = {0.3F, 0.35F, 0.35F};
        for (int i = 0; i < tail.length; i++) {
            final float bend = state.tailBend * weights[i];
            bones.ifPresent(tail[i], bone -> bone.setRotY(bone.getRotY() + bend));
        }
        if (state.headLook && !state.headBone.isEmpty()) {
            bones.ifPresent(state.headBone, bone -> {
                bone.setRotX(bone.getRotX() + Mth.clamp(state.xRot,-60,60) * Mth.DEG_TO_RAD);
                bone.setRotY(bone.getRotY() + Mth.clamp(Mth.wrapDegrees(state.yRot),-75,75) * Mth.DEG_TO_RAD);
            });
        }
    }
    @Override
    public void submitRenderTasks(RenderPassInfo<RebornRenderState> pass,
            net.minecraft.client.renderer.OrderedSubmitNodeCollector collector,
            net.minecraft.client.renderer.rendertype.RenderType type) {
        RebornRenderState state = pass.renderState();
        if (state.beam) {
            Vec3[] start = {null};
            pass.model().getBone(state.beamBone).ifPresent(bone -> pass.renderPosed(() -> {
                PoseStack pose = pass.poseStack();
                pose.pushPose();
                com.geckolib.util.RenderUtil.transformToBone(pose, bone);
                org.joml.Matrix4f local = com.geckolib.util.RenderUtil.extractPoseFromRoot(pose.last().pose(), pass.getPreRenderMatrixState());
                start[0] = com.geckolib.util.RenderUtil.renderPoseToPosition(local, 1, 1, 1).add(state.x, state.y, state.z);
                pose.popPose();
            }));
            if (start[0] != null) {
                PoseStack pose = new PoseStack();
                pose.last().set(pass.getPreRenderMatrixPose());
                submitBeam(state, start[0], pose, collector, pass.cameraState());
            }
        }
        super.submitRenderTasks(pass, collector, type);
    }
    private static void submitBeam(RebornRenderState state, Vec3 worldStart, PoseStack pose,
            net.minecraft.client.renderer.OrderedSubmitNodeCollector collector, CameraRenderState camera) {
        Vec3 origin = new Vec3(state.x,state.y,state.z);
        Vec3 start = worldStart.subtract(origin);
        Vec3 end = state.beamEnd.subtract(origin);
        if (state.shulkerBeam) {
            Vec3 delta = state.beamEnd.subtract(worldStart);
            double horizontal = Math.sqrt(delta.x*delta.x + delta.z*delta.z);
            float yaw = state.beamYaw * Mth.DEG_TO_RAD;
            end = start.add(-Mth.sin(yaw)*horizontal, delta.y, Mth.cos(yaw)*horizontal);
        }
        Vec3 direction = end.subtract(start);
        double length = direction.length();
        if (length < 0.5) return;
        Vec3 side = direction.cross(camera.pos.subtract(origin).subtract(start)).normalize();
        float scroll = -state.ageInTicks*0.35F;
        float pulse = 1+Mth.sin(state.ageInTicks*0.9F)*0.12F;
        Vec3 endpoint = end;
        collector.submitCustomGeometry(pose, BeamRenderUtil.renderType(state.beamTexture), (entry, vertices) -> {
            for (float width : new float[]{2.4F*pulse, 1.0F*pulse}) {
                Vec3 half = side.scale(width*0.5F);
                Vec3[] corners = {start.subtract(half),start.add(half),endpoint.add(half),endpoint.subtract(half)};
                for (int i=0;i<4;i++) {
                    Vec3 point = corners[i];
                    BeamRenderUtil.vertex(vertices,entry.pose(),entry.normal(),(float)point.x,(float)point.y,(float)point.z,
                            i==0 || i==3 ? 0 : 1, scroll+(i>=2 ? (float)(length/4) : 0));
                }
            }
        });
    }
}
