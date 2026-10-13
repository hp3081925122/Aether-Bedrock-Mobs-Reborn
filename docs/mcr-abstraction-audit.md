> 历史记录说明（2026-10-13）：本文中的巨龙实现和“当前 27”统计属于早期移植快照。现版本模组注册 13 种生物、5 种其他 Boss 技能实体和 2 种投射物（共 20 个实体类型）；巨龙专用 AI、技能、模型与动画已移除，天境巨龙由独立 Aether Dragon 模组提供。原 JAR 的 ID 与行为清单作为来源证据保留。

# MCR 抽象代码与迁移实现审查

## 审查范围

本次审查不是只检查“无敌效果、双阶段实体、禁用副手”三个例子，而是把当前迁移源码按实体属性、AI、阶段生命周期、Forge 事件、动画关键帧、渲染、声音、掉落、物品范围扫描和特殊方块交互逐项对照原模组 1.20.1 JAR 的字节码与资源。

结论分为三类：

- **P0：已确认的功能错误或明显行为偏差**，应优先修复。
- **P1：保留了 MCR 生成式实现，功能可能还能运行，但存在错误语义、性能浪费或维护风险**。
- **P2：资源和验证缺口**，不一定是迁移逻辑错误，但会直接影响运行时表现。

## P0：已确认的功能错误

| 优先级 | 位置 | 发现 | 原因与影响 | 建议方向 |
| --- | --- | --- | --- | --- |
| P0 | `RebornMobEntity.createAttributes` | Aether Dragon 一阶段属性字段错位 | 原字节码对应的是 `ATTACK_DAMAGE=3`、`FOLLOW_RANGE=50`、`KNOCKBACK_RESISTANCE=2.5`；当前源码写成攻击伤害 50、跟随距离 2.5，并没有写入击退抗性。龙会从“远距离飞行 Boss”变成近距离高伤害实体。 | 逐个使用明确的属性字段恢复原值，不能把攻击击退、击退抗性和跟随距离混用。 |
| P0 | `RebornMobEntity.createAttributes` | Aether Dragon 二阶段属性字段错位 | 原字节码对应的是攻击伤害 10、跟随距离 50、击退抗性 0.2；当前源码写成攻击伤害 50，并把 0.2 写到了 `ATTACK_KNOCKBACK`，跟随距离仍是默认 16。 | 按官方 1.20.1 属性键逐项恢复；这类错误不能靠运行时效果补偿。 |
| P0 | `RebornMobEntity.createAttributes` | Salt Crab King、Elder Shulker、Salt Crab 的击退相关属性不一致 | 原字节码中 Salt Crab King 和 Elder Shulker 的击退抗性为 1.0；Salt Crab 为 2.0。当前分别缺失、写成 0.2、完全缺失。Elder Shulker 还缺少原来的攻击击退 0.2。 | 用 `KNOCKBACK_RESISTANCE` 和 `ATTACK_KNOCKBACK` 分开恢复，不要只看数值相近就认为等价。 |
| P0 | `RebornMobEntity.createAttributes` | 飞行速度属性没有注册 | Percipitus、Twinkler 和蝴蝶原字节码有 `FLYING_SPEED=0.3`，当前属性构造器完全没有加入 `Attributes.FLYING_SPEED`。 | 给需要飞行速度的实体显式注册飞行速度，并与导航控制器一起验证。 |
| P0 | `RebornMobEntity.registerGoals` + `ModEntities` | Salt Crab 没有攻击目标和近战目标 | Salt Crab 注册为 `MobCategory.CREATURE`，但通用 AI 只在 `MobCategory.MONSTER` 下添加 `MeleeAttackGoal`、受伤目标和玩家目标。因此它实际没有原模组的攻击 AI。 | 目标注册应按实体行为能力或实体 ID 判断，不能把 `MONSTER` 分类当作 AI 开关。 |
| P0 | `RebornMobEntity.isFlyingEntity` | Shimaenaga 漏掉飞行与无重力 | 原实体覆盖了无重力逻辑；当前 `isFlyingEntity()` 没有包含 `shimaenaga`，它使用普通地面导航并受重力影响。 | 将飞行能力作为实体行为配置，补上 Shimaenaga 的飞行导航、移动控制和无重力。 |
| P0 | `RebornMobEntity.registerGoals` | Dragon、Elder Shulker 的远程 AI 参数被泛化 | 原 Dragon 的远程攻击间隔/范围约为 20 tick/150 格，Elder Shulker 为 30 tick/70 格；当前两者共用 40 tick/20 格。Dragon 原本还拥有更远的观察范围和自定义飞行/跳跃目标。 | 为每种实体恢复自己的远程目标参数和特殊目标，不要让一个通用构造器覆盖所有 Boss。 |
| P0 | `RebornMobEntity.registerGoals` | Naga、Spirit 等实体的自定义跳跃/近战目标被删除 | 原 Naga、Spirit 等有自定义近战目标和 `LeapAtTargetGoal`；当前只剩普通 `MeleeAttackGoal`，移动和攻击节奏会改变。 | 保留自定义目标类的行为，通用目标只作为没有特殊 AI 的兜底。 |

## P1：明确属于 MCR 生成式实现的代码

| 优先级 | 位置 | 当前写法 | 问题 | 更合适的实现方向 |
| --- | --- | --- | --- | --- |
| P1 | `RebornGameplayEvents.onEntityJoin` | Golem 通过生成时的高等级效果组合模拟保护窗口 | 原过程本身已有 100 tick 无敌帧，额外效果会污染药水状态、粒子和实体数据，不能把效果等级当作无敌机制。 | 使用实体无敌帧，保留必要的再生效果；当前已移除生成时的持续负面/伪无敌效果。 |
| P1 | `RebornMobEntity` Dragon 阶段逻辑 | 原 MCR 通过死亡后生成第二个 EntityType 表示阶段 | 两个实体会分裂 UUID、目标、Boss 条、掉落和存档状态；本项目尚未发布，不需要保留旧 phase2 ID。 | 使用同步阶段字段、生命阈值和动态属性/导航切换；第一阶段死亡被拦截为同一实体的第二阶段。 |
| 已处理 | `RebornGameplayEvents` 全文件 | 原先一个全局事件类按 `getIdPath()` 大型 `switch` 分发死亡、受伤和加入世界行为 | 这是 MCR Procedure 集中搬运后的调度器，继续堆分支会扩大服务端边界和维护风险。 | 已改为按实体 ID 注册的加入、受伤、死亡行为表；事件方法只做事件过滤和分发，保留原参数与服务端边界。 |
| 已处理 | `RebornGameplayEvents` 原副手 tick 处理 | 副手每 tick 施加减速、饥饿、中毒，并每 tick 播放声音 | 这是用负面效果和声音模拟“禁止副手”的生成式方案，会造成持续音效刷屏和效果刷新；用户示例中的“神秘音效+一堆 buff”确实属于此类。 | 已删除副手 tick 处理，改为装备变更时移回背包或掉落；使用入口也直接拒绝副手使用。 |
| P1 | `CrystaliteSwordItem.use` | 对 5 格范围内所有 `Entity` 生成闪电，并在循环内重复发粒子 | 谓词永远为 true，会把物品、投射物和施法者附近的非生物实体也算进去；实体数量越多，闪电和粒子数量线性增加，可能自伤、误伤并造成服务端压力。 | 只筛选符合规则的 `LivingEntity`，明确排除玩家自身；粒子移到循环外按一次技能播放。 |
| P1 | `RebornMobEntity.particleKeyframe` | 未知关键帧统一映射为 FIRE | `trail`、`dig` 等关键帧会被错误显示成火焰，资源一旦新增关键帧也会静默得到错误效果。 | 为每个关键帧建立显式映射；未知值记录英文调试日志并跳过，不要使用有游戏语义的默认粒子。 |
| P1 | `RebornMobEntity.registerControllers` | 只注册粒子关键帧处理，没有声音关键帧处理 | 原动画包含飞行、落地、攻击、咆哮、跳跃等声音关键帧；当前没有对应 handler，动画播放时声音行为会丢失。 | 注册声音关键帧处理器，按实体和关键帧 ID 映射 `ModSounds`。 |
| P1 | `RebornMobEntity.registerActionAnimations` | 动作表只对少数四类实体真正提供动画，Dragon/Elder 的 `attack` 触发可能为空 | `performRangedAttack()` 会统一调用 `triggerAction("attack")`，但没有动画定义的实体触发后没有可播放动作。 | 让每个实体声明自己的动作集合；没有动作时不要调用无效触发器，并补齐远程攻击动作。 |
| P1 | `RebornClientEvents` + `RebornGeoModel` | Elder Shulker、Salt Crab King、Spirit 不在 Geo 支持表，最终回退到僵尸 Humanoid 模型 | 源码已有这些实体的纹理/资源线索，但渲染器选择直接落入 `HumanoidModel`，外观与碰撞尺寸都会显得不对。 | 补齐专用 Geo 模型与动画映射；没有专用资源时也应使用明确的占位模型，而不是静默回退僵尸模型。 |
| P1 | `SpecialSpawnerBlock.neighborChanged` | 只要红石信号大于 0，每次邻居更新都调用一次激活 | 同一持续信号在活塞、方块更新或红石变化时可能重复生成，直到方块被移除；这是事件边沿没有被记录的典型生成式写法。 | 只响应从无信号到有信号的上升沿，或激活前锁定/移除方块并增加服务端保护。 |
| P1 | `RebornGameplayEvents` 的粒子、爆炸和直接掉落 | 大量固定半径、固定数量和零半径爆炸直接写在事件 switch 中 | 这些行为保留了 MCR Procedure 的硬编码参数，难以按实体、难度、战利品上下文和客户端表现调整。原模组确实有部分直接掉落，因此“直接掉落”本身不是确定缺口，但仍是维护候选。 | 先把参数收敛为实体行为配置；掉落优先迁移到实体战利品表或 `dropCustomDeathLoot` 上下文中，保留原概率和数量。 |

## 本轮已修复

- 修正 Dragon 阶段属性、Salt Crab King、Elder Shulker、Salt Crab 的属性字段，并补上飞行速度属性。
- Salt Crab 不再因为 `CREATURE` 分类被跳过攻击 AI；恢复 Naga、Spirit 的跳跃目标，分别恢复 Dragon/Elder Shulker 的远程攻击范围和间隔。
- Shimaenaga 接入飞行导航和无重力处理。
- Golem 初始化改用实体无敌帧，不再使用 10 级抗性效果模拟无敌。
- Crystalite Sword 只处理范围内其他生物，粒子从实体循环中移出，避免把物品和投射物也变成闪电目标。
- Spirit Sword 的副手使用直接失败，删除副手每 tick 负面效果和循环音效。
- 动画关键帧增加显式 `trail`、`dig` 映射，未知关键帧跳过；补充已有动画声音关键帧处理和 Dragon、Naga、Guardian Angel 的动作注册。
- 特殊刷怪器增加 `POWERED` 状态，只响应红石上升沿，避免持续信号的邻居更新重复生成；六个对应方块状态文件已补齐 `powered=true/false` 模型变体。
- Dragon 已合并为单一 `aetherdragon` EntityType；生命降至 300 或第一阶段受到致命伤害时切换阶段，阶段状态通过实体数据和 NBT 保存，不保留旧 phase2 ID、刷怪蛋或语言键。
- Dragon 第二阶段切换为地面导航、重力和近战目标，第二阶段死亡仍保留原 Shaday 套装掉落、爆炸、胜利音效和粒子。
- Spirit Sword 通过 `canEquip` 和装备变更事件禁止进入副手，已移除副手每 tick 的负面效果与刷音效；Pyre Spear 恢复原有弹药查找、即时发射、1.3 破坏速度、耐久和弹药消耗流程。
- 特殊刷怪器在白天激活失败时不锁定红石状态，夜间仍可由同一持续信号正常触发。

龙的精确碰撞、专用模型和缺失原始音频由当前工程已有改动处理；本轮没有覆盖另一对话已经完成的资源和渲染修改。

## P2：资源与验证缺口

- 原先缺失的 `death1.ogg`、`idle1.ogg`、`hurt2.ogg` 引用已删除，并改用原 JAR 对应的原版 Allay 音效；本轮没有重新运行客户端确认日志。
- 上一次客户端日志还出现 `crystalite_golem` 的 `groundslam` 和 `death` 动画解析错误。当前源码在那次运行后没有重新做客户端验证，不能把资源修复当成已验证。
- 当前实体尺寸与原注册表字节码一致：环境实体 0.6×1.8、Golem 4×2、Naga 3×2.5、Spirit 1×3、Dragon 5×4 等。尺寸不是本轮需要改的缺陷。
- `SpiritSwordItem` 继承 `ShieldItem` 与原模组一致，真正的问题是副手限制的实现方式，不是继承关系本身。

## 推荐修复顺序

1. 先修复属性字段、Salt Crab AI、Shimaenaga 飞行和各 Boss 的远程参数；这些会直接改变战斗行为。
2. 复核 Golem 无敌、Dragon 阶段合并、Spirit Sword 副手限制和 Pyre Spear 弹药流程。
3. 复核 Geo 渲染、声音关键帧和关键帧显式映射，确认已知 GeckoLib 解析错误不再出现。
4. 最后复核雷击范围、红石昼夜重试、粒子/爆炸/掉落配置，并补测试矩阵。

本轮已修改 Java、注册表、资源和审计文档；只完成了源码与 JSON 静态检查，没有自动执行 `build` 或 `runClient`。原始字节码对照材料保存在本地取证缓存目录。
