package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.DragonSkillEntity;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.aetherbedrockmobsreborn.entity.RebornProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AetherBedrockMobsReborn.MOD_ID);
    public static final Map<String, RegistryObject<EntityType<RebornMobEntity>>> MOBS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<EntityType<RebornProjectileEntity>>> PROJECTILES = new LinkedHashMap<>();
    // 艾瑟龙技能实体：羽刃、陨晶、雷击标记
    public static final RegistryObject<EntityType<DragonSkillEntity>> DRAGON_SKILL = ENTITIES.register("dragon_skill", () -> EntityType.Builder.<DragonSkillEntity>of(DragonSkillEntity::new, MobCategory.MISC)
            .sized(0.8F, 0.8F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("dragon_skill"));

    static {
        String[] ambient = {
                "shimaenaga", "twinkler", "buterfly_2", "buterfly_3", "buterfly_4", "buterfly_5", "buterfly_6",
                "buterfly7", "buterfly_8", "buterfly_9", "buterfly_10", "buterfly_11", "buterfly_12", "buterfly_13", "percipitus"
        };
        for (String id : ambient) {
            registerMob(id, MobCategory.AMBIENT, 0.6F, 1.8F, 3);
        }
        registerMob("saltcrab", MobCategory.CREATURE, 0.6F, 1.8F, 3);
        registerMob("crystalitegolem", MobCategory.MONSTER, 4.0F, 2.0F, 3);
        registerMob("crystaliteminion", MobCategory.MONSTER, 0.6F, 1.8F, 3);
        registerMob("mutatedcritter", MobCategory.MONSTER, 0.6F, 1.8F, 3);
        registerMob("aether_naga", MobCategory.MONSTER, 3.0F, 2.5F, 3);
        registerMob("spirit", MobCategory.MONSTER, 1.0F, 3.0F, 3);
        registerMob("saltcrabking", MobCategory.MONSTER, 2.6F, 2.6F, 3);
        registerMob("eldershulker", MobCategory.MONSTER, 4.0F, 4.0F, 3);
        registerMob("guardianangel", MobCategory.MONSTER, 0.6F, 2.7F, 3);
        registerMob("aetherdragon", MobCategory.MONSTER, 5.0F, 4.0F, 3);
        registerProjectile("shalkbbbb");
        registerProjectile("shulkercrysta");
    }

    private static void registerMob(String id, MobCategory category, float width, float height, int updateInterval) {
        MOBS.put(id, ENTITIES.register(id, () -> EntityType.Builder.of(RebornMobEntity::new, category)
                .sized(width, height)
                .clientTrackingRange(64)
                .updateInterval(updateInterval)
                .build(id)));
    }

    private static void registerProjectile(String id) {
        PROJECTILES.put(id, ENTITIES.register(id, () -> EntityType.Builder.of(RebornProjectileEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(64)
                .updateInterval(1)
                .build(id)));
    }

    private ModEntities() {
    }
}
