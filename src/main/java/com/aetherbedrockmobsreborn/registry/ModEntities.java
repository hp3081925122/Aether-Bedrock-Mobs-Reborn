package com.aetherbedrockmobsreborn.registry;

import com.aetherbedrockmobsreborn.AetherBedrockMobsReborn;
import com.aetherbedrockmobsreborn.entity.DragonSkillEntity;
import com.aetherbedrockmobsreborn.entity.RebornMobEntity;
import com.aetherbedrockmobsreborn.entity.CrabSkillEntity;
import com.aetherbedrockmobsreborn.entity.GolemSkillEntity;
import com.aetherbedrockmobsreborn.entity.NagaSkillEntity;
import com.aetherbedrockmobsreborn.entity.NagaEntity;
import com.aetherbedrockmobsreborn.entity.RebornProjectileEntity;
import com.aetherbedrockmobsreborn.entity.ShulkerSkillEntity;
import com.aetherbedrockmobsreborn.entity.SpiritSkillEntity;
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
    // 灵魂技能实体：圣环符文、天剑
    public static final RegistryObject<EntityType<SpiritSkillEntity>> SPIRIT_SKILL = ENTITIES.register("spirit_skill", () -> EntityType.Builder.<SpiritSkillEntity>of(SpiritSkillEntity::new, MobCategory.MISC)
            .sized(0.8F, 0.8F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("spirit_skill"));
    // 天境娜迦技能实体：石柱、巨石
    public static final RegistryObject<EntityType<NagaSkillEntity>> NAGA_SKILL = ENTITIES.register("naga_skill", () -> EntityType.Builder.<NagaSkillEntity>of(NagaSkillEntity::new, MobCategory.MISC)
            .sized(1.0F, 1.0F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("naga_skill"));
    // 水晶石傀儡技能实体：晶刺、晶笼、晶核
    public static final RegistryObject<EntityType<GolemSkillEntity>> GOLEM_SKILL = ENTITIES.register("golem_skill", () -> EntityType.Builder.<GolemSkillEntity>of(GolemSkillEntity::new, MobCategory.MISC)
            .sized(1.0F, 1.0F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("golem_skill"));
    // 盐蟹王技能实体：泡泡、盐泉、浪墙、盐晶
    public static final RegistryObject<EntityType<CrabSkillEntity>> CRAB_SKILL = ENTITIES.register("crab_skill", () -> EntityType.Builder.<CrabSkillEntity>of(CrabSkillEntity::new, MobCategory.MISC)
            .sized(0.8F, 0.8F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("crab_skill"));
    // 远古潜影贝技能实体：潜影弹、碎刃、缠藤
    public static final RegistryObject<EntityType<ShulkerSkillEntity>> SHULKER_SKILL = ENTITIES.register("shulker_skill", () -> EntityType.Builder.<ShulkerSkillEntity>of(ShulkerSkillEntity::new, MobCategory.MISC)
            .sized(0.6F, 0.6F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build("shulker_skill"));

    static {
        String[] ambient = {
                "shimaenaga", "twinkler", "buterfly", "percipitus"
        };
        for (String id : ambient) {
            if (id.equals("buterfly")) {
                registerMob(id, MobCategory.AMBIENT, 1.4F, 0.4F, 3);
            } else {
                registerMob(id, MobCategory.AMBIENT, 0.6F, 1.8F, 3);
            }
        }
        registerMob("saltcrab", MobCategory.CREATURE, 0.6F, 1.8F, 3);
        registerMob("crystalitegolem", MobCategory.MONSTER, 3.0F, 4.0F, 3);
        registerMob("crystaliteminion", MobCategory.MONSTER, 0.6F, 1.8F, 3);
        registerMob("mutatedcritter", MobCategory.MONSTER, 0.6F, 1.8F, 3);
        registerMob("aether_naga", MobCategory.MONSTER, 3.0F, 2.5F, 3);
        registerMob("spirit", MobCategory.MONSTER, 1.0F, 3.0F, 3);
        registerMob("saltcrabking", MobCategory.MONSTER, 2.6F, 2.6F, 3);
        registerMob("eldershulker", MobCategory.MONSTER, 4.0F, 4.0F, 3);
        registerMob("guardianangel", MobCategory.MONSTER, 0.6F, 2.7F, 3);
        registerProjectile("shalkbbbb");
        registerProjectile("shulkercrysta");
    }

    private static void registerMob(String id, MobCategory category, float width, float height, int updateInterval) {
        EntityType.EntityFactory<RebornMobEntity> factory = id.equals("aether_naga") ? NagaEntity::new : RebornMobEntity::new;
        MOBS.put(id, ENTITIES.register(id, () -> EntityType.Builder.of(factory, category)
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
