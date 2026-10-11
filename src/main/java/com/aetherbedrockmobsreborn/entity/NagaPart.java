package com.aetherbedrockmobsreborn.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.PartEntity;
import org.slf4j.Logger;

public final class NagaPart extends PartEntity<NagaEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
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
    public boolean hurt(DamageSource source, float amount) {
        if (!getParent().isAlive() || source.getEntity() == getParent()) {
            return false;
        }
        boolean accepted = getParent().hurt(source, amount);
        LOGGER.debug("Naga multipart hit: entity={}, part={}, damage={}, accepted={}", getParent().getId(), this.bone, amount, accepted);
        return accepted;
    }

    @Override
    public boolean is(Entity entity) {
        return this == entity || getParent() == entity;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return getParent().isInvulnerableTo(source);
    }

    @Override
    public boolean canBeCollidedWith() {
        return getParent().canBeCollidedWith();
    }

    @Override
    public Entity getRootVehicle() {
        return getParent().getRootVehicle();
    }

    @Override
    public void setSecondsOnFire(int seconds) {
        getParent().setSecondsOnFire(seconds);
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
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
