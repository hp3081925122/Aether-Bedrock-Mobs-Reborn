package com.aetherbedrockmobsreborn.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.neoforge.entity.PartEntity;

public final class NagaPart extends PartEntity<NagaEntity> {
    private final String bone;

    NagaPart(NagaEntity parent, String bone) {
        super(parent);
        this.bone = bone;
        this.noPhysics = true;
    }

    void moveToBox(AABB box) {
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();
        this.xOld = getX();
        this.yOld = getY();
        this.zOld = getZ();
        setPos((box.minX + box.maxX) * 0.5D, box.minY, (box.minZ + box.maxZ) * 0.5D);
        setBoundingBox(box);
    }

    @Override
    public boolean isPickable() {
        return getParent().isAlive() && getParent().isPickable();
    }

    @Override
    public boolean hurtServer(ServerLevel server, DamageSource source, float amount) {
        if (!getParent().isAlive() || source.getEntity() == getParent()) {
            return false;
        }
        boolean accepted = getParent().hurtOrSimulate(source, amount);
        return accepted;
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    public boolean isInvulnerableTo(ServerLevel server, DamageSource source) {
        return getParent().isInvulnerableTo(server, source);
    }

    @Override
    public boolean canBeCollidedWith(Entity other) {
        return getParent().canBeCollidedWith(other);
    }

    @Override
    public Entity getRootVehicle() {
        return getParent().getRootVehicle();
    }

    @Override
    public ItemStack getPickResult() {
        return getParent().getPickResult();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.0F, 2.0F);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput tag) {
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput tag) {
    }
}

