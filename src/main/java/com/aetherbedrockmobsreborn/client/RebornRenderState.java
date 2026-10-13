package com.aetherbedrockmobsreborn.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;

public final class RebornRenderState extends LivingEntityRenderState implements GeoRenderState {
    private final Map<DataTicket<?>, Object> data = new HashMap<>();
    public String id, texture;
    public float tailBend;
    public boolean headLook, beam, shulkerBeam;
    public String headBone, beamBone;
    public net.minecraft.resources.Identifier beamTexture;
    public Vec3 beamEnd;
    public float beamYaw;
    @Override
    public <D> void addGeckolibData(DataTicket<D> ticket, D value) { data.put(ticket,value); }
    @Override
    public Map<DataTicket<?>, Object> getDataMap() { return data; }
}
