package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, RegistryObject<SoundEvent>> ALL = new LinkedHashMap<>();

    static {
        String[] ids = {
                "sword", "victorymmm", "cgd", "cgh", "cgi", "cgmd", "cgmh", "cgmi", "cgmw", "cgw",
                "mud", "muh", "mui", "muw", "nad", "nah", "naw", "sabw", "sah", "saw", "spd", "sph", "spi", "spw", "shh", "shi", "shd",
                "gad", "gah", "gai", "gaw", "drd", "drf", "drh", "dri", "drw"
        };
        for (String id : ids) {
            ALL.put(id, SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AetherBedrockMobsReborn.MOD_ID, id))));
        }
    }

    private ModSounds() {
    }
}
