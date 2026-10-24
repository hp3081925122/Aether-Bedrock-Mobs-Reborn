# 天境武器与护甲削弱兼容

## 源码依据

当前 The Aether 依赖：Forge file 6134920、NeoForge file 6172950。实际 JAR 的 `com.aetherteam.aether.event.hooks.AbilityHooks$WeaponHooks` 包含两个静态函数：

- `reduceWeaponEffectiveness(LivingEntity, Entity, float)`：符合天境实体/标签条件的目标受到外部武器或弹射物攻击时，伤害被改为 `max(pow(damage, damage > 1 ? 0.6 : 1.6), 1)`。武器分支检查 `tools_debuff` 配置。
- `reduceArmorEffectiveness(LivingEntity, Entity, float)`：天境实体攻击穿外部护甲的目标时，按各护甲提供的护甲值除以 15 追加伤害。该函数不检查 `tools_debuff` 配置。

实体判断使用 `entity.aether` 前缀或相关天境标签，也会匹配本模组的 `entity.aether_bedrock_mobs_reborn.*`。

Forge 从 `WeaponAbilityListener.onEntityDamage(LivingDamageEvent)` 调用，NeoForge 1.21.1 从 `LivingDamageEvent.Pre` 调用。这里改变的是结算伤害，并非直接修改物品的面板属性。

## 实现

仅豁免物品注册 ID 命名空间为 `aether_bedrock_mobs_reborn` 或 `aether_dragon` 的装备。武器在命中前直接保持输入伤害；护甲在原函数结算后只扣除受保护护甲贡献的额外惩罚。混穿时其他模组/原版护甲的惩罚仍保留；已被天境标签豁免的装备不会重复扣除。原版及其他模组武器继续使用天境原有规则。不改天境配置或依赖 JAR。

1.20.1 和 1.21.1 分支使用 `@Pseudo`：目标天境类不存在时跳过兼容。26.1.2 当前依赖 Aether II file 8502033，没有旧版这两个削弱函数，无需加入这套兼容。未把可选的旧天境加入模组依赖声明。

Aether II 的 `AetherIIEventListeners.onLivingPreDamaged` 调用 `DamageSystemAttachment.getDamageTypeModifiedValue`，使用斩击、冲击、穿刺伤害与抗性结算，不按装备的模组命名空间判断。这套伤害类型机制保留。

首次实际拦截输出一次英文 debug 日志：`Bypassed Aether equipment penalties for Reborn and Aether Dragon equipment`。
