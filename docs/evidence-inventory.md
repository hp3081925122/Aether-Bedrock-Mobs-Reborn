# Aether Bedrock Mobs Reborn — JAR 功能清单

> 来源：`aethermobs-1.5.0-forge-1.20.1.jar` 的资源、数据文件、注册表字节码和 `mods.toml`。名称和行为在重写前仍需逐项复核。

## 模组元数据

- 原模组 ID：`aethermobs`
- 原显示名：`aethermobs`
- 原包名：`net.mcreator.aethermobs`
- Forge loader 范围：`[47,)`
- Minecraft：`[1.20.1]`
- Aether：可选；开发环境使用 CurseMaven 文件 `6134920`
- GeckoLib：原文件标为非必需，范围 `[0,)`；重写版若使用 GeckoLib，应按实际代码改为必需依赖
- 原 `mods.toml` 重复声明了 Aether；重写版不保留该错误

## 数量盘点

| 类别 | 数量 | 证据 |
|---|---:|---|
| 实体注册 | 原 JAR 28；当前 27 | `AethermobsModEntities` 字节码；当前注册表已移除未发布的 Dragon 二阶段类型 |
| 方块注册 | 56 | `AethermobsModBlocks` 字节码与 blockstates |
| 普通物品/装备 | 14 | `AethermobsModItems` 字节码 |
| 刷怪蛋 | 26 | `AethermobsModItems` 字节码 |
| GeckoLib Geo 模型 | 10 | `assets/aethermobs/geo/` |
| GeckoLib 动画文件 | 10 | `assets/aethermobs/animations/` |
| 结构 NBT | 6 | `data/aethermobs/structures/` |
| 结构定义 | 6 | `data/aethermobs/worldgen/structure/` |
| 结构集合 | 6 | `data/aethermobs/worldgen/structure_set/` |
| 模板池 | 6 | `data/aethermobs/worldgen/template_pool/` |
| 生物群系生成修饰器 | 15 | `data/aethermobs/forge/biome_modifier/` |
| 方块战利品表 | 56 | `data/aethermobs/loot_tables/blocks/` |
| 配方 | 13 | `data/aethermobs/recipes/` |
| 进度 | 6 | `data/aethermobs/advancements/` |
| 粒子类型 | 3 | `fire`、`runeseffect`、`whitesmoke` |
| 声音事件 | 38 | `assets/aethermobs/sounds.json` |
| OGG 声音文件 | 37 | `assets/aethermobs/sounds/` |
| 纹理资源 | 161 | `assets/aethermobs/textures/` |

## 实体 ID

```text
aether_naga
aetherdragon
buterfly_10
buterfly_11
buterfly_12
buterfly_13
buterfly_2
buterfly_3
buterfly_4
buterfly_5
buterfly_6
buterfly_8
buterfly_9
buterfly7
crystalitegolem
crystaliteminion
eldershulker
guardianangel
mutatedcritter
percipitus
saltcrab
saltcrabking
shalkbbbb
shimaenaga
shulkercrysta
spirit
twinkler
```

## 方块 ID

```text
aureatebluebeam
aureatebluebeambrick
aureatebluehead
aureatebluepillar
aureatebricks
aureatechiseled
aureatecobblestone
aureatekeystone
aureateleaves
aureatepillar
aureatepillarbeam
aureatepillarhead
aureatepillarheadbrick
aureatescript
aureatetilebricks
aureatetiles
aurorastone
blockofsalt
blueglazedmosaic
cobbleddeepslate
crystalitebricks
crystalitecapstone
crystalitecolumnhead
crystalitecolumnheadlit
crystalitekeystone
crystalitespawner
crystalitestone
crystalitestonescript
deepdirt
deepgrass
dragonspawner
drysaltstone
elderspawner
grassydeadrock
labyrinthfloor_1
labyrinthfloor_2
labyrinthfloor_3
labyrinthleaves
labyrinthwall_1
labyrinthwall_2
labyrinthwall_3
labyrinthwall_4
labyrinthwall_5
labyrinthwall_6
labyrinthwall_7
labyrintspawner
lamp
layeredsaltstone
mosaicblue
mosaicwhite
mossydeadrockbrick
redglazedmosaic
saltspawner
saltstonebricks
saltstonepillarside
saltstonesmallbricks
```

## 普通物品与装备 ID

```text
citykey
crabmeat
crabmeatcooked
crystalite
crystalitedust
crystalitesword
dungeondagger
finalkey
pyrespear
saltinstrument
shaday_chestplate
shaday_helmet
shaday_leggings
spiritsword
```

## 结构与世界生成

| 结构 ID | 结构集合间距 | 分离值 | 高度范围 | 生物群系 |
|---|---:|---:|---|---|
| `aethercity` | 150 | 65 | 30–128 | `aether:skyroot_woodland`、`skyroot_meadow`、`skyroot_forest`、`skyroot_grove` |
| `crystalitedungeon` | 50 | 20 | 30–128 | 同上 |
| `elderdangeon` | 60 | 28 | 30–128 | 同上 |
| `labyrinth` | 60 | 30 | 30–128 | 同上 |
| `saltdangeon` | 60 | 27 | 30–128 | 同上 |
| `spiritdange` | 60 | 25 | 30–128 | 同上 |

所有结构定义均为 `minecraft:jigsaw`；结构步骤为 `surface_structures`。其中 Crystalite、Elder、Labyrinth 使用 `beard_thin` 地形适配，Aether City、Salt、Spirit 使用 `none`。

## 生物群系生成

15 个 Forge biome modifier 全部把生物加入以下 Aether 生物群系：

- `aether:skyroot_woodland`
- `aether:skyroot_meadow`
- `aether:skyroot_forest`
- `aether:skyroot_grove`

刷怪权重：`percipitus=22`、`shimaenaga=20`，13 种蝴蝶/环境生物各为 `2`；最小和最大群体数均为 `1`。

## 已发现的资源问题

- JAR 只有 `en_us.json`，没有 `zh_cn.json`。
- 多个蝴蝶和 Twinkler 的英文显示名都错误地写成 `baterfly1`。
- 多个声音字幕键为空字符串。
- 结构、生物群系和方块资源命名存在拼写不一致，如 `labyrintspawner`、`elderdangeon`、`spiritdange`；是否保留旧 ID 需在兼容性决策后冻结。
- `data/minecraft/tags/items/` 中把 `saltinstrument` 同时加入 axes、pickaxes、swords 标签，需在重写时按实际物品用途复核。


