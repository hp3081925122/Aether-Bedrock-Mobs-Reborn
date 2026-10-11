# 迁移缺口审计

本审计由多个只读子代理对照原 JAR `aethermobs-1.5.0-forge-1.20.1.jar`、解压资源和当前 Java 源码完成。数据资源先做了规范化比对，再检查实体字节码与当前实现；结果区分“已确认缺失”和“原资源本身的问题”。

## 注册名审计

- 56 个方块、96 个物品、27 个当前实体类型、3 个粒子注册名均唯一，且与 blockstates、模型、战利品表、刷怪蛋模型和粒子资源逐项对应；原 JAR 的 28 个实体类型中包含未发布的 Dragon 二阶段类型，已按合并方案移除。
- 36 个声音注册名与 `sounds.json` 对应；命名空间统一为 `aether_bedrock_mobs_reborn`。
- 未发现 `aureat`、`crystalit`、大写路径或旧命名空间残留；`aureate` 与 `crystalite` 注册 ID 保持原资源拼写。
- 语言值中的 `Aureat keystone` 已修正为 `Aureate keystone`，进度文本中的 `Crystallite` 已统一为 `Crystalite`。

## 仍待处理的缺口

- Aether Dragon 已按原过程的 300 生命阈值合并为单一实体阶段切换；不保留未发布的旧 phase2 EntityType。
- 上一次 `runClient` 日志中的 `crystalite_golem` 动画解析错误已定位并改写：三个未被其他关键帧读取的 MCreator 赋值表达式已替换为等值数值；尚未用新源码重新运行客户端确认。
- 新增的三个专用 Java 模型尚未在新构建产物中做客户端视觉复验。

## 本轮已补齐

- 实体死亡、实体加入世界和被雷击事件已由 `RebornGameplayEvents` 订阅，接入阶段初始伤害、Boss 死亡效果、Golem 召唤、Salt Crab 跳跃、Guardian Angel 城市钥匙概率掉落和 Crystalite Sword 雷击效果。
- Aether Dragon 与 Elder Shulker 已接入远程攻击目标；`shalkbbbb` 已有生成入口、100 tick 生命周期、ArrowRenderer 和命中粒子/爆炸效果。
- 自定义粒子 provider 已注册；通用实体控制器已接入动作触发和粒子关键帧映射。
- `RebornMobEntity` 的 ARMOR、ATTACK_KNOCKBACK、KNOCKBACK_RESISTANCE 属性已按原实体数据重新分开。
- Crystalite Golem 受伤召唤保留 50% 概率，并加入每个傀儡 20 tick 冷却；以傀儡周围 32 格为统计范围，仆从上限为 8 个。
- Aether Dragon 已补齐原 JAR 的非推动、不可推动实体、不可改变维度、无跌落伤害、墙体伤害免疫和持续无重力行为；远程攻击参数同步为速度 1.25、间隔 25 tick、射程 150 格。
- Elder Shulker、Salt Crab King、Spirit 已注册原 JAR 对应的专用模型、模型层和渲染器，不再落到僵尸人形回退渲染器。
- Percipitus 的环境、受伤和死亡声音已改为原 JAR 实际使用的原版 Allay 音效，移除 `death1.ogg`、`idle1.ogg`、`hurt2.ogg` 的失效资源引用。
- 结构 NBT 内残留的 `aethermobs:` 方块命名空间已全部改为 `aether_bedrock_mobs_reborn:`；6 个结构的尺寸、调色板和方块数量保持原 JAR 数据。
- Crystalite Sword、Dungeon Dagger、Pyre Spear 和 Spirit Sword 已按原 JAR 补齐触发条件、冷却、声音、粒子、闪电范围、投射物发射和 Spirit Sword 副手持续效果。
- Shaday 盔甲已接入原 JAR 的 128×128 专用模型层，头盔、胸甲和护腿分别绑定对应模型部件，避免把专用贴图错误套在默认盔甲模型上。

## 已确认等价

- 56 个方块掉落表、13 个配方、6 个进度、15 个 biome modifier、6 个结构、6 个结构集、6 个模板池和 6 个 NBT 结构与原 JAR 逐项等价，只做了命名空间替换。
- 原 JAR 的 `death1.ogg`、`idle1.ogg`、`hurt2.ogg` 同样不存在；当前通过复原原实体字节码中的 Allay 音效引用消除了失效资源引用，并未伪造不存在的上游音频。

## 本轮修复

- 语言文件中 56 个方块名称已改为中文，解决创造栏悬浮提示显示英文短语的问题。
- 26 组刷怪蛋的背景色和图案色已按原 JAR 恢复；原模组的蝴蝶/闪烁者使用同一组颜色，但 Boss 和其他生物各自不同。

本轮遵循用户要求，没有在上述静态修复后自动执行 `build` 或 `runClient`；颜色修复需要下一次用户明确授权后重新编译并在客户端复验。
