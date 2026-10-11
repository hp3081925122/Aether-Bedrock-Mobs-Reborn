package com.aetherbedrockmobsreborn.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public final class NagaEntity extends RebornMobEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Long> ANIMATION_START = SynchedEntityData.defineId(NagaEntity.class, EntityDataSerializers.LONG);
    private final NagaPart[] parts;
    private int hitboxSkill = -1;

    public NagaEntity(EntityType<? extends RebornMobEntity> type, Level level) {
        super(type, level);
        this.parts = NagaHitboxes.createParts(this);
        setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
        updateParts();
        LOGGER.debug("Naga multipart initialized: entity={}, parts={}, bodyBox={}, tailBox={}",
                getId(), this.parts.length, this.parts[0].getBoundingBox(), this.parts[this.parts.length - 1].getBoundingBox());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_START, 0L);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts != null) {
            for (int i = 0; i < this.parts.length; i++) {
                this.parts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public NagaPart[] getParts() {
        return this.parts;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (this.parts != null) {
            updateParts();
        }
    }

    @Override
    public void tick() {
        super.tick();
        int skill = getNagaSkill();
        if (!level().isClientSide && skill != this.hitboxSkill) {
            this.hitboxSkill = skill;
            this.entityData.set(ANIMATION_START, level().getGameTime() - getNagaCombat().getSkillTicks());
        }
        updateParts();
    }

    private void updateParts() {
        double time = getNagaSkill() == 0 ? this.tickCount / 20.0D
                : Math.max(0L, level().getGameTime() - this.entityData.get(ANIMATION_START)) / 20.0D;
        NagaHitboxes.update(this, this.parts, time);
    }
}
