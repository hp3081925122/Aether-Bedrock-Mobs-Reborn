package com.aetherbedrockmobsreborn.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SpiritModel<T extends Entity>
extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("aether_bedrock_mobs_reborn", "modelspirit"), "main");
    public final ModelPart body;
    public final ModelPart backhalo;
    public final ModelPart backsymbols;
    public final ModelPart symbol1;
    public final ModelPart symbol2;
    public final ModelPart symbol3;
    public final ModelPart symbol4;
    public final ModelPart symbol5;
    public final ModelPart symbol6;
    public final ModelPart symbol7;
    public final ModelPart head;
    public final ModelPart hair;
    public final ModelPart arm_L;
    public final ModelPart arm_R2;
    public final ModelPart weaponpivotground2;
    public final ModelPart weapon2;
    public final ModelPart arm_R;
    public final ModelPart wisppivot;
    public final ModelPart wisp1;
    public final ModelPart wisp2;
    public final ModelPart fire;
    public final ModelPart fire2;
    public final ModelPart fire3;

    public SpiritModel(ModelPart root) {
        this.body = root.getChild("body");
        this.backhalo = this.body.getChild("backhalo");
        this.backsymbols = this.backhalo.getChild("backsymbols");
        this.symbol1 = this.backsymbols.getChild("symbol1");
        this.symbol2 = this.backsymbols.getChild("symbol2");
        this.symbol3 = this.backsymbols.getChild("symbol3");
        this.symbol4 = this.backsymbols.getChild("symbol4");
        this.symbol5 = this.backsymbols.getChild("symbol5");
        this.symbol6 = this.backsymbols.getChild("symbol6");
        this.symbol7 = this.backsymbols.getChild("symbol7");
        this.head = this.body.getChild("head");
        this.hair = this.head.getChild("hair");
        this.arm_L = this.body.getChild("arm_L");
        this.arm_R2 = this.body.getChild("arm_R2");
        this.weaponpivotground2 = this.arm_R2.getChild("weaponpivotground2");
        this.weapon2 = this.weaponpivotground2.getChild("weapon2");
        this.arm_R = this.body.getChild("arm_R");
        this.wisppivot = this.body.getChild("wisppivot");
        this.wisp1 = this.wisppivot.getChild("wisp1");
        this.wisp2 = this.wisp1.getChild("wisp2");
        this.fire = this.body.getChild("fire");
        this.fire2 = this.body.getChild("fire2");
        this.fire3 = this.body.getChild("fire3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0f, -36.0f, -9.0f, 20.0f, 9.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(50, 109).addBox(-9.0f, -27.0f, -9.0f, 18.0f, 5.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(0, 41).addBox(-8.0f, -27.0f, -8.5f, 16.0f, 6.0f, 13.0f, new CubeDeformation(0.0f)).texOffs(0, 23).addBox(-8.5f, -21.0f, -9.0f, 17.0f, 4.0f, 14.0f, new CubeDeformation(0.0f)).texOffs(0, 30).addBox(-2.5f, -21.5f, -10.0f, 5.0f, 5.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(54, 0).addBox(-4.0f, -35.0f, -11.0f, 8.0f, 7.0f, 2.0f, new CubeDeformation(0.0f)).texOffs(84, 35).mirror().addBox(7.5f, -17.0f, -6.5f, 1.0f, 7.0f, 10.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(84, 35).addBox(-8.5f, -17.0f, -6.5f, 1.0f, 7.0f, 10.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)24.0f, (float)1.0f));
        PartDefinition backhalo = body.addOrReplaceChild("backhalo", CubeListBuilder.create().texOffs(0, 104).addBox(-12.0f, -18.0f, 0.5f, 24.0f, 24.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)-32.0f, (float)9.5f));
        PartDefinition backsymbols = backhalo.addOrReplaceChild("backsymbols", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-6.0f, (float)0.5f));
        PartDefinition symbol1 = backsymbols.addOrReplaceChild("symbol1", CubeListBuilder.create().texOffs(114, 121).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 121).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol2 = backsymbols.addOrReplaceChild("symbol2", CubeListBuilder.create().texOffs(114, 114).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 114).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol3 = backsymbols.addOrReplaceChild("symbol3", CubeListBuilder.create().texOffs(114, 107).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 107).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol4 = backsymbols.addOrReplaceChild("symbol4", CubeListBuilder.create().texOffs(114, 100).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 100).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol5 = backsymbols.addOrReplaceChild("symbol5", CubeListBuilder.create().texOffs(114, 93).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 93).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol6 = backsymbols.addOrReplaceChild("symbol6", CubeListBuilder.create().texOffs(114, 86).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 86).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition symbol7 = backsymbols.addOrReplaceChild("symbol7", CubeListBuilder.create().texOffs(114, 79).addBox(-3.5f, -3.5f, -0.8f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)).texOffs(114, 79).addBox(-3.5f, -3.5f, -1.15f, 7.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)0.5f, (float)1.0f));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(33, 67).addBox(-5.0f, -9.0f, -4.5f, 10.0f, 9.0f, 9.0f, new CubeDeformation(0.0f)).texOffs(33, 85).addBox(-5.0f, -9.0f, -4.5f, 10.0f, 9.0f, 9.0f, new CubeDeformation(0.25f)), PartPose.offset((float)0.0f, (float)-36.0f, (float)-3.0f));
        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(0.0f, -2.5f, 0.0f, 0.0f, 5.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-5.0f, (float)-4.5f, (float)-1.5f, (float)0.0f, (float)-0.3927f, (float)0.0f));
        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(10, 10).mirror().addBox(0.0f, -1.0f, -1.0f, 0.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-6.8598f, (float)-3.5f, (float)3.3548f, (float)0.0f, (float)1.5882f, (float)0.0f));
        PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(10, 10).mirror().addBox(0.0f, 0.0f, -1.0f, 0.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-5.7654f, (float)-3.0f, (float)0.3478f, (float)0.0f, (float)1.2217f, (float)0.0f));
        PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(10, 10).addBox(0.0f, 0.0f, -1.0f, 0.0f, 2.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)5.7654f, (float)-3.0f, (float)0.3478f, (float)0.0f, (float)-1.2217f, (float)0.0f));
        PartDefinition head_r5 = head.addOrReplaceChild("head_r5", CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, -2.5f, 0.0f, 0.0f, 5.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)5.0f, (float)-4.5f, (float)-1.5f, (float)0.0f, (float)0.3927f, (float)0.0f));
        PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-8.9796f, (float)-1.3716f));
        PartDefinition hair_r1 = hair.addOrReplaceChild("hair_r1", CubeListBuilder.create().texOffs(75, 5).addBox(-2.0f, -2.8204f, -2.4284f, 4.0f, 3.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)0.6f, (float)2.3f, (float)-0.3316f, (float)0.0f, (float)0.0f));
        PartDefinition hair_r2 = hair.addOrReplaceChild("hair_r2", CubeListBuilder.create().texOffs(109, 13).addBox(-2.0f, -4.5236f, -2.8559f, 4.0f, 2.0f, 4.0f, new CubeDeformation(0.25f)), PartPose.offsetAndRotation((float)0.0f, (float)0.6f, (float)2.3f, (float)-0.4712f, (float)0.0f, (float)0.0f));
        PartDefinition hair_r3 = hair.addOrReplaceChild("hair_r3", CubeListBuilder.create().texOffs(91, 2).addBox(-3.0f, -9.9134f, -4.6802f, 6.0f, 6.0f, 6.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)0.6f, (float)2.3f, (float)-0.6807f, (float)0.0f, (float)0.0f));
        PartDefinition arm_L = body.addOrReplaceChild("arm_L", CubeListBuilder.create().texOffs(57, 12).addBox(0.0f, -5.0f, -5.5f, 9.0f, 8.0f, 11.0f, new CubeDeformation(0.0f)).texOffs(62, 31).addBox(1.0f, 14.0f, -3.5f, 6.0f, 7.0f, 7.0f, new CubeDeformation(0.0f)).texOffs(71, 67).addBox(0.0f, -3.0f, -4.0f, 7.0f, 17.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset((float)9.0f, (float)-32.0f, (float)-2.0f));
        PartDefinition arm_L_r1 = arm_L.addOrReplaceChild("arm_L_r1", CubeListBuilder.create().texOffs(4, 8).addBox(0.0f, 0.0f, -2.0f, 3.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)9.0f, (float)2.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.9425f));
        PartDefinition arm_R2 = body.addOrReplaceChild("arm_R2", CubeListBuilder.create(), PartPose.offset((float)-9.0f, (float)-32.0f, (float)-2.0f));
        PartDefinition weaponpivotground2 = arm_R2.addOrReplaceChild("weaponpivotground2", CubeListBuilder.create(), PartPose.offset((float)-5.25f, (float)32.0f, (float)-38.0f));
        PartDefinition weapon2 = weaponpivotground2.addOrReplaceChild("weapon2", CubeListBuilder.create().texOffs(156, 50).addBox(-2.0f, -3.75f, -9.0f, 5.0f, 5.0f, 13.0f, new CubeDeformation(0.0f)).texOffs(130, 54).addBox(-2.0f, -3.75f, 4.0f, 5.0f, 5.0f, 4.0f, new CubeDeformation(0.5f)).texOffs(111, 19).addBox(-1.5f, -9.5f, -12.0f, 4.0f, 17.0f, 3.0f, new CubeDeformation(0.0f)).texOffs(106, 4).addBox(-0.5f, -6.5f, -47.0f, 2.0f, 11.0f, 35.0f, new CubeDeformation(0.0f)).texOffs(145, 0).mirror().addBox(0.0f, -14.5f, -16.0f, 0.0f, 27.0f, 12.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)1.0f, (float)-13.0f, (float)40.0f));
        PartDefinition arm_R = body.addOrReplaceChild("arm_R", CubeListBuilder.create().texOffs(57, 12).mirror().addBox(-9.0f, -5.0f, -5.5f, 9.0f, 8.0f, 11.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(62, 31).mirror().addBox(-7.0f, 14.0f, -3.5f, 6.0f, 7.0f, 7.0f, new CubeDeformation(0.0f)).mirror(false).texOffs(71, 67).mirror().addBox(-7.0f, -3.0f, -4.0f, 7.0f, 17.0f, 8.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offset((float)-9.0f, (float)-32.0f, (float)-2.0f));
        PartDefinition arm_R_r1 = arm_R.addOrReplaceChild("arm_R_r1", CubeListBuilder.create().texOffs(4, 8).mirror().addBox(-3.0f, 0.0f, -2.0f, 3.0f, 0.0f, 4.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)-9.0f, (float)2.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.9425f));
        PartDefinition wisppivot = body.addOrReplaceChild("wisppivot", CubeListBuilder.create(), PartPose.offset((float)0.0f, (float)-19.0f, (float)-8.5f));
        PartDefinition wisp1 = wisppivot.addOrReplaceChild("wisp1", CubeListBuilder.create().texOffs(45, 47).addBox(-6.5f, -2.0f, -7.0f, 13.0f, 7.0f, 13.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)2.0f, (float)7.0f));
        PartDefinition wisp1_r1 = wisp1.addOrReplaceChild("wisp1_r1", CubeListBuilder.create().texOffs(84, 53).addBox(-6.5f, 0.0f, 0.0f, 13.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)6.0f, (float)-0.0436f, (float)0.0f, (float)0.0f));
        PartDefinition wisp1_r2 = wisp1.addOrReplaceChild("wisp1_r2", CubeListBuilder.create().texOffs(84, 53).addBox(-6.5f, 0.0f, 0.0f, 13.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)-7.0f, (float)0.3054f, (float)0.0f, (float)0.0f));
        PartDefinition wisp1_r3 = wisp1.addOrReplaceChild("wisp1_r3", CubeListBuilder.create().texOffs(84, 40).mirror().addBox(0.0f, 0.0f, -6.5f, 0.0f, 7.0f, 13.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)6.5f, (float)5.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)0.0873f));
        PartDefinition wisp1_r4 = wisp1.addOrReplaceChild("wisp1_r4", CubeListBuilder.create().texOffs(84, 40).addBox(0.0f, 0.0f, -6.5f, 0.0f, 7.0f, 13.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-6.5f, (float)5.0f, (float)-0.5f, (float)0.0f, (float)0.0f, (float)-0.0873f));
        PartDefinition wisp2 = wisp1.addOrReplaceChild("wisp2", CubeListBuilder.create().texOffs(0, 60).addBox(-6.0f, -2.0f, -4.75f, 12.0f, 7.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offset((float)0.0f, (float)5.0f, (float)0.0f));
        PartDefinition wisp2_r1 = wisp2.addOrReplaceChild("wisp2_r1", CubeListBuilder.create().texOffs(101, 51).mirror().addBox(0.0f, 0.0f, -4.75f, 0.0f, 7.0f, 9.0f, new CubeDeformation(0.0f)).mirror(false), PartPose.offsetAndRotation((float)6.0f, (float)5.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)0.6981f));
        PartDefinition wisp2_r2 = wisp2.addOrReplaceChild("wisp2_r2", CubeListBuilder.create().texOffs(101, 51).addBox(0.0f, 0.0f, -4.75f, 0.0f, 7.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)-6.0f, (float)5.0f, (float)0.0f, (float)0.0f, (float)0.0f, (float)-0.6981f));
        PartDefinition wisp2_r3 = wisp2.addOrReplaceChild("wisp2_r3", CubeListBuilder.create().texOffs(97, 60).addBox(-6.0f, 0.0f, 0.25f, 12.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.1f, (float)-5.0f, (float)0.6109f, (float)0.0f, (float)0.0f));
        PartDefinition wisp2_r4 = wisp2.addOrReplaceChild("wisp2_r4", CubeListBuilder.create().texOffs(98, 60).addBox(-6.0f, 0.0f, 0.0f, 12.0f, 7.0f, 0.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation((float)0.0f, (float)5.0f, (float)4.25f, (float)-0.6981f, (float)0.0f, (float)0.0f));
        PartDefinition fire = body.addOrReplaceChild("fire", CubeListBuilder.create().texOffs(34, 153).addBox(-19.5f, 0.0f, -19.5f, 39.0f, 0.0f, 39.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-0.5f, (float)0.0f, (float)-2.5f));
        PartDefinition fire2 = body.addOrReplaceChild("fire2", CubeListBuilder.create().texOffs(34, 153).addBox(-19.5f, 0.0f, -19.5f, 39.0f, 0.0f, 39.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-0.5f, (float)-3.0f, (float)-2.5f));
        PartDefinition fire3 = body.addOrReplaceChild("fire3", CubeListBuilder.create().texOffs(34, 153).addBox(-19.5f, 0.0f, -19.5f, 39.0f, 0.0f, 39.0f, new CubeDeformation(0.0f)), PartPose.offset((float)-0.5f, (float)6.0f, (float)-2.5f));
        return LayerDefinition.create((MeshDefinition)meshdefinition, (int)192, (int)192);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.arm_R.xRot = Mth.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * limbSwingAmount;
        this.arm_L.xRot = Mth.cos((float)(limbSwing * 0.6662f)) * limbSwingAmount;
        this.head.yRot = netHeadYaw / 57.295776f;
        this.head.xRot = headPitch / 57.295776f;
        this.hair.yRot = netHeadYaw / 57.295776f;
        this.hair.xRot = headPitch / 57.295776f;
        this.arm_R2.xRot = Mth.cos((float)(limbSwing * 0.6662f + (float)Math.PI)) * limbSwingAmount;
    }
}

