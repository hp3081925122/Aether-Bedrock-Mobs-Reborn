# Aether Bedrock Mobs Reborn

面向 Minecraft 1.20.1 Forge 的天境生物扩展，基于 [Aether bedrock mobs](https://modrinth.com/mod/aether-bedrock-mobs) 的发布资源和可验证行为重建与移植。

本项目为独立维护的 Reborn 版本。

## 内容

- 天境娜迦、水晶石傀儡、远古潜影贝、盐蟹王、灵魂等首领，以及其他生物。
- 地牢、建筑方块、武器、盔甲、刷怪蛋和进度。
- 简体中文与英文语言资源，中文统一使用“天境”译名。

## 开发环境

| 项目 | 版本 |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Java | 17 |
| Gradle Wrapper | 8.8 |
| 必需模组 | The Aether、GeckoLib |
| GeckoLib 开发依赖 | 4.8.4 |
| JEI 开发运行时依赖 | 15.20.0.130 |
| 当前项目版本 | 0.1.0 |

在仓库根目录运行：

```powershell
.\gradlew.bat build
```

构建产物位于 `build/libs/`。需要手动启动开发客户端时运行：

```powershell
.\gradlew.bat runClient
```

## 验证状态

语言修订已通过 JSON 校验、资源处理和 Java 编译。历史客户端与服务端启动记录位于 `docs/`；启动通过不代表所有实体模型、结构生成与首领战斗都完成游戏内验收，详见其中的迁移审计和测试矩阵。

## 来源与许可证

项目采用 [CC0-1.0](LICENSE.txt)。原模组 Modrinth 页面声明 CC0-1.0，本地原版 JAR 的元数据则写为 `Not specified`；来源版本和文件哈希记录在 [原始发布证据索引](evidence/source-index.md)。原模组资源、移植模型及其他第三方内容的权利仍归对应权利人，第三方权利不因本项目声明而改变。

Forge 模板的原许可证说明保留在 [LICENSE-Forge.txt](LICENSE-Forge.txt)。The Aether、GeckoLib 与其他依赖按各自许可证使用。

本模组内的巨龙 AI、技能实体、模型、动画及专用音效已移除；天境巨龙由独立的 Aether Dragon 模组提供。共享地面震起与光束渲染保留为通用实现，兼容刷怪器与地牢结构继续引用独立模组。

## Boss 技能伤害配置

五个本模组 Boss 的 36 个直接伤害结算项使用当前攻击力乘技能倍率。配置文件为 `aether_bedrock_mobs_reborn-boss-skills.toml`，默认位于实例的 `config/`。默认倍率保持原伤害水平，详细键、默认值和示例见 [技能倍率说明](docs/boss-skill-damage-multipliers.md)。

天境巨龙地牢必须主手持终末之钥右键刷怪器召唤，靠近不再自动召唤。新旧地牢兼容路径及钥匙获取见 [钥匙召唤说明](docs/dragon-key-summoning.md)。
