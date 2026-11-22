package com.aetherbedrockmobsreborn.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BossSkillConfig {
    public record MobAttributeValues(double maxHealth, double attackDamage, double armor) {
    }

    private record MobAttributeConfig(ModConfigSpec.DoubleValue maxHealth,
                                      ModConfigSpec.DoubleValue attackDamage,
                                      ModConfigSpec.DoubleValue armor) {
    }

    public enum MobAttributePreset {
        AETHER_NAGA("aether_naga", "天境娜迦", 350.0D, 6.0D, 0.1D),
        SPIRIT("spirit", "灵魂", 300.0D, 11.0D, 0.3D),
        SALT_CRAB_KING("saltcrabking", "盐蟹王", 250.0D, 15.0D, 0.2D),
        ELDER_SHULKER("eldershulker", "远古潜影贝", 400.0D, 5.0D, 1.5D),
        CRYSTALITE_GOLEM("crystalitegolem", "水晶石傀儡", 300.0D, 8.0D, 0.2D),
        CRYSTALITE_MINION("crystaliteminion", "水晶石仆从", 20.0D, 5.0D, 0.0D),
        MUTATED_CRITTER("mutatedcritter", "变异生物", 12.0D, 5.0D, 0.0D),
        SALT_CRAB("saltcrab", "盐蟹", 6.0D, 3.0D, 0.0D),
        GUARDIAN_ANGEL("guardianangel", "守护天使", 7.0D, 3.0D, 0.0D),
        PERCIPITUS("percipitus", "悬崖鸟", 10.0D, 3.0D, 0.0D),
        SHIMAENAGA("shimaenaga", "银喉长尾山雀", 10.0D, 3.0D, 0.0D),
        BUTTERFLY("buterfly", "蝴蝶", 1.0D, 3.0D, 0.0D),
        TWINKLER("twinkler", "闪光精灵", 1.0D, 3.0D, 0.0D);

        private final String entityId;
        private final String displayName;
        private final double defaultMaxHealth;
        private final double defaultAttackDamage;
        private final double defaultArmor;

        MobAttributePreset(String entityId, String displayName, double defaultMaxHealth, double defaultAttackDamage, double defaultArmor) {
            this.entityId = entityId;
            this.displayName = displayName;
            this.defaultMaxHealth = defaultMaxHealth;
            this.defaultAttackDamage = defaultAttackDamage;
            this.defaultArmor = defaultArmor;
        }
    }
    public enum Skill {
        CRAB_PINCER("saltcrabking", "pincer", 12.0D / 15.0D, "蟹钳夹击"),
        CRAB_SLAM("saltcrabking", "slam", 15.0D / 15.0D, "重砸"),
        CRAB_RUSH("saltcrabking", "rush", 13.0D / 15.0D, "冲撞"),
        CRAB_BUBBLE("saltcrabking", "bubble", 6.0D / 15.0D, "泡泡弹"),
        CRAB_GEYSER("saltcrabking", "geyser", 9.0D / 15.0D, "盐泉喷发"),
        CRAB_WAVE("saltcrabking", "wave", 10.0D / 15.0D, "潮汐浪墙"),
        CRAB_SALT("saltcrabking", "salt", 9.0D / 15.0D, "盐晶破土"),
        GOLEM_PUNCH_RIGHT("crystalitegolem", "punch_right", 11.0D / 8.0D, "右拳"),
        GOLEM_PUNCH_LEFT("crystalitegolem", "punch_left", 13.0D / 8.0D, "左拳"),
        GOLEM_SLAM("crystalitegolem", "slam", 16.0D / 8.0D, "重锤"),
        GOLEM_CHARGE("crystalitegolem", "charge", 14.0D / 8.0D, "冲锋"),
        GOLEM_CAGE_CONTACT("crystalitegolem", "cage_contact", 4.0D / 8.0D, "晶笼持续接触"),
        GOLEM_CORE("crystalitegolem", "core", 14.0D / 8.0D, "晶核爆发"),
        GOLEM_CAGE_ERUPT("crystalitegolem", "cage_erupt", 6.0D / 8.0D, "晶笼升起"),
        GOLEM_SPIKE("crystalitegolem", "spike", 10.0D / 8.0D, "晶刺突起"),
        NAGA_BITE("aether_naga", "bite", 12.0D / 6.0D, "噬咬"),
        NAGA_SWEEP("aether_naga", "sweep", 12.0D / 6.0D, "尾扫"),
        NAGA_BURROW("aether_naga", "burrow", 15.0D / 6.0D, "潜地突袭"),
        NAGA_BEAM("aether_naga", "beam", 4.0D / 6.0D, "光束每次伤害"),
        NAGA_PILLAR("aether_naga", "pillar", 10.0D / 6.0D, "石柱升起"),
        NAGA_BOULDER("aether_naga", "boulder", 10.0D / 6.0D, "巨石撞击"),
        SHULKER_BEAM("eldershulker", "beam", 6.0D / 5.0D, "光束每次伤害"),
        SHULKER_PHASE("eldershulker", "phase", 14.0D / 5.0D, "相位砸落"),
        SHULKER_FIELD("eldershulker", "field", 4.0D / 5.0D, "浮空力场脉冲"),
        SHULKER_ORB("eldershulker", "orb", 5.0D / 5.0D, "潜影弹"),
        SHULKER_SHARD("eldershulker", "shard", 6.0D / 5.0D, "碎刃"),
        SHULKER_VINE("eldershulker", "vine", 7.0D / 5.0D, "缠藤破土"),
        SPIRIT_SLASH("spirit", "slash", 11.0D / 11.0D, "灵刃第一斩"),
        SPIRIT_REVERSE_SLASH("spirit", "reverse_slash", 1.15D, "灵刃反斩"),
        SPIRIT_CLEAVE("spirit", "cleave", 1.3D, "裂地重斩"),
        SPIRIT_CLEAVE_FIRE("spirit", "cleave_fire", 0.9D, "裂地魂焰"),
        SPIRIT_DASH("spirit", "dash", 1.2D, "灵影突进"),
        SPIRIT_DASH_BURST("spirit", "dash_burst", 0.9D, "突进路径爆发"),
        SPIRIT_NOVA("spirit", "nova", 0.8D, "灵焰轮环"),
        SPIRIT_RUNE("spirit", "rune", 7.0D / 11.0D, "圣环符弹"),
        SPIRIT_SWORD("spirit", "sword", 12.0D / 11.0D, "天剑落地");

        private final String boss;
        private final String key;
        private final double defaultMultiplier;
        private final String displayName;

        Skill(String boss, String key, double defaultMultiplier, String displayName) {
            this.boss = boss;
            this.key = key;
            this.defaultMultiplier = defaultMultiplier;
            this.displayName = displayName;
        }
    }

    private static final Map<Skill, ModConfigSpec.DoubleValue> MULTIPLIERS = new EnumMap<>(Skill.class);
    private static final Map<String, MobAttributeConfig> MOB_ATTRIBUTES = new LinkedHashMap<>();
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("自定义生物基础属性配置；仅在实体首次应用配置时生效一次，后续加载不再覆盖，避免冲掉指令、KubeJS 或 buff 的调整。",
                "Base attribute settings for custom mobs; applied only once per entity via NBT flag so reloads never overwrite commands, KubeJS, or buffs.").push("mob_attributes");
        for (MobAttributePreset preset : MobAttributePreset.values()) {
            builder.push(preset.entityId);
            ModConfigSpec.DoubleValue maxHealth = builder.comment(preset.displayName + "最大生命值。",
                    "Max health for " + preset.entityId + ".")
                    .defineInRange("max_health", preset.defaultMaxHealth, 1.0D, Double.MAX_VALUE);
            ModConfigSpec.DoubleValue attackDamage = builder.comment(preset.displayName + "基础攻击力；Boss 技能伤害也会以此为基数乘以倍率。",
                    "Base attack damage for " + preset.entityId + "; boss skill damage also scales from this value.")
                    .defineInRange("attack_damage", preset.defaultAttackDamage, 0.0D, Double.MAX_VALUE);
            ModConfigSpec.DoubleValue armor = builder.comment(preset.displayName + "护甲值。",
                    "Armor value for " + preset.entityId + ".")
                    .defineInRange("armor", preset.defaultArmor, 0.0D, Double.MAX_VALUE);
            MOB_ATTRIBUTES.put(preset.entityId, new MobAttributeConfig(maxHealth, attackDamage, armor));
            builder.pop();
        }
        builder.pop();
        builder.comment("Boss 技能直接伤害 = 当前攻击力 × 对应技能倍率。",
                "Boss direct skill damage = current attack damage multiplied by the corresponding skill multiplier.").push("boss_skill_multipliers");
        for (Skill skill : Skill.values()) {
            builder.push(skill.boss);
            MULTIPLIERS.put(skill, builder.comment(skill.displayName + "伤害倍率；0 仅关闭直接伤害，状态与特效保留。",
                    "Damage multiplier for " + skill.key + "; 0 disables direct damage while preserving effects.")
                    .defineInRange(skill.key, skill.defaultMultiplier, 0.0D, Double.MAX_VALUE));
            builder.pop();
        }
        builder.pop();
        SPEC = builder.build();
    }

    private BossSkillConfig() {
    }

    public static double multiplier(Skill skill) {
        return MULTIPLIERS.get(skill).get();
    }

    public static MobAttributeValues mobAttributes(String entityId) {
        if (!SPEC.isLoaded()) {
            return null;
        }
        MobAttributeConfig config = MOB_ATTRIBUTES.get(entityId);
        if (config == null) {
            return null;
        }
        return new MobAttributeValues(config.maxHealth.get(), config.attackDamage.get(), config.armor.get());
    }
}
