# Boss 技能伤害倍率

五个本模组 Boss 的直接技能伤害统一为 `施放者当前 ATTACK_DAMAGE 属性 × 对应技能倍率`。攻击属性包含属性修饰器；倍率不影响攻击范围、时间轴、命中次数、击退或特效。燃烧与摔落等附带的原版环境伤害保持原机制。

本次检查发现三个分支此前没有技能伤害配置类，因此新增服务端倍率配置，并非沿用已有固定伤害配置。文件名为 `aether_bedrock_mobs_reborn-boss-skills.toml`，1.20.1 Forge 位于对应世界的 `serverconfig` 目录，可在 `defaultconfigs` 放置模板用于新世界；1.21.1 和 26.1.2 NeoForge 默认位于实例的 `config` 目录。三个分支的键与默认倍率一致。

技能实体优先在命中时读取施放者当前攻击力；施放者无法从当前维度解析时，使用施放时捕获的攻击力。没有合法施放者的手动召唤技能实体不产生直接伤害。配置值为 0 时仍保留状态效果、燃烧、击退和动画。配置使用 Forge/NeoForge 的服务端同步与重载机制；修改后也可重启服务器生效。

默认倍率按旧伤害除以旧攻击力换算，保持原默认伤害水平；灵魂已有倍率保留原数值。光束、晶笼等倍率表示每次原有伤害结算的倍率，并非整个技能合计伤害。独立 Aether Dragon 模组未修改。

| Boss 分组 | 配置键 | 技能 | 默认倍率 |
|---|---|---|---|
| saltcrabking | pincer | 蟹钳夹击 | 0.8 |
| saltcrabking | slam | 重砸 | 1 |
| saltcrabking | rush | 冲撞 | 0.866666666667 |
| saltcrabking | bubble | 泡泡弹 | 0.4 |
| saltcrabking | geyser | 盐泉喷发 | 0.6 |
| saltcrabking | wave | 潮汐浪墙 | 0.666666666667 |
| saltcrabking | salt | 盐晶破土 | 0.6 |
| crystalitegolem | punch_right | 右拳 | 1.375 |
| crystalitegolem | punch_left | 左拳 | 1.625 |
| crystalitegolem | slam | 重锤 | 2 |
| crystalitegolem | charge | 冲锋 | 1.75 |
| crystalitegolem | cage_contact | 晶笼持续接触 | 0.5 |
| crystalitegolem | core | 晶核爆发 | 1.75 |
| crystalitegolem | cage_erupt | 晶笼升起 | 0.75 |
| crystalitegolem | spike | 晶刺突起 | 1.25 |
| aether_naga | bite | 噬咬 | 2 |
| aether_naga | sweep | 尾扫 | 2 |
| aether_naga | burrow | 潜地突袭 | 2.5 |
| aether_naga | beam | 光束每次伤害 | 0.666666666667 |
| aether_naga | pillar | 石柱升起 | 1.66666666667 |
| aether_naga | boulder | 巨石撞击 | 1.66666666667 |
| eldershulker | beam | 光束每次伤害 | 1.2 |
| eldershulker | phase | 相位砸落 | 2.8 |
| eldershulker | field | 浮空力场脉冲 | 0.8 |
| eldershulker | orb | 潜影弹 | 1 |
| eldershulker | shard | 碎刃 | 1.2 |
| eldershulker | vine | 缠藤破土 | 1.4 |
| spirit | slash | 灵刃第一斩 | 1 |
| spirit | reverse_slash | 灵刃反斩 | 1.15 |
| spirit | cleave | 裂地重斩 | 1.3 |
| spirit | cleave_fire | 裂地魂焰 | 0.9 |
| spirit | dash | 灵影突进 | 1.2 |
| spirit | dash_burst | 突进路径爆发 | 0.9 |
| spirit | nova | 灵焰轮环 | 0.8 |
| spirit | rune | 圣环符弹 | 0.636363636364 |
| spirit | sword | 天剑落地 | 1.09090909091 |

例如将盐蟹王钳击设为攻击力的 1.5 倍：

```toml
[boss_skill_multipliers.saltcrabking]
pincer = 1.5
```
