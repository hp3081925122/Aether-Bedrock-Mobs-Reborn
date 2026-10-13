# Aether Bedrock Mobs Reborn

面向 Minecraft 26.1.2 NeoForge 的天境生物扩展，分支为 `2612neoforge`，基于 [Aether bedrock mobs](https://modrinth.com/mod/aether-bedrock-mobs) 的发布资源和可验证行为重建与移植。此版本依赖 The Aether II，天境巨龙由独立的 Aether Dragon 模组提供。

本项目为独立维护的 Reborn 版本。

## 内容

- 天境娜迦、水晶石傀儡、远古潜影贝、盐蟹王、灵魂等首领，以及其他生物。
- 地牢、建筑方块、武器、盔甲、刷怪蛋和进度。
- 简体中文与英文语言资源，中文统一使用“天境”译名。

## 开发环境

| 项目 | 版本 |
|---|---|
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.99 |
| Java | 25 |
| Gradle Wrapper | 9.4.0 |
| 必需模组 | The Aether II、Aether Dragon、GeckoLib |
| Aether II 开发依赖 | 26.1.2-alpha.4.1，CurseForge 文件 8502033 |
| GeckoLib 开发依赖 | 5.5.1 |
| JEI 开发运行时依赖 | 29.34.0.90，仅用于开发运行环境 |
| 当前项目版本 | 1.2 |

在仓库根目录运行：

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\jdk-25.0.4+7'
.\gradlew.bat build
```

构建产物为 `build/libs/aether_bedrock_mobs_reborn-26.1.2-neoforge-1.2.jar`。开发运行时默认读取同级 `天境巨龙/2612-neoforge-天境巨龙-worktree/build/libs/aether_dragon-1.0.0.jar`，可通过 `-Paether_dragon_jar=<JAR路径>` 覆盖。依赖 JAR 不会打包进本模组。需要手动启动开发客户端时运行：

```powershell
.\gradlew.bat runClient
```

## 验证状态

已通过 Java 编译、全部 JSON 解析、GeckoLib 5 的 12 个模型和 12 个动画文件解析、注册 ID 与资源完整性核对、专用服务端创建世界、13 种生物及天境巨龙召唤、伤害处理、装备写入、数据重载和存档重启读取。验证脚本、日志和临时世界位于 `D:\AI临时文件\aether-reborn-2612`，原版本工作区保留。

Aether II 没有四个旧 Aether 群系的逐项对应 ID，本分支使用官方 `#aether_ii:the_aether` 标签承接自然生成和结构选址。原生成权重、组数、结构布局及间距不变；选址范围会随 Aether II 的群系体系变化。

客户端未启动，模型显示、Boss 技能特效、盔甲及刷怪蛋外观、自然生成和地牢探索仍需游戏内验收。旧 `docs/` 中的历史记录不能作为此分支的客户端验收依据。

## 来源与许可证

项目采用 [CC0-1.0](LICENSE.txt)。原模组 Modrinth 页面声明 CC0-1.0，本地原版 JAR 的元数据则写为 `Not specified`；来源版本和文件哈希记录在 [原始发布证据索引](evidence/source-index.md)。原模组资源、移植模型及其他第三方内容的权利仍归对应权利人，第三方权利不因本项目声明而改变。

刷怪蛋兼容纹理 `textures/item/spawn_egg.png` 和 `spawn_egg_overlay.png` 来自 Mojang 的 Minecraft 1.21.1 原版资源，用于替代新版移除的通用模板。Forge 模板的原许可证说明保留在 [LICENSE-Forge.txt](LICENSE-Forge.txt)。The Aether II、Aether Dragon、GeckoLib 与其他依赖按各自许可证使用。

本模组内的巨龙 AI、技能实体、模型、动画及专用音效已移除；天境巨龙由独立的 Aether Dragon 模组提供。共享地面震起与光束渲染保留为通用实现，兼容刷怪器与地牢结构继续引用独立模组。
