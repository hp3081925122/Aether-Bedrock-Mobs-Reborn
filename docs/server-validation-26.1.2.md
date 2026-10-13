# 26.1.2 专用服务端验收记录

验收日期：2026-10-13。目标分支：`2612neoforge`。本体扩展使用 NeoForge 26.1.2.99，依赖独立 Aether Dragon、Aether II 26.1.2 alpha.4.1 与 GeckoLib 5.5.1。

羽刃和陨晶已恢复真实技能实体，远程攻击通过目标实际掉血判定。技能时间轴完成与命中结果分别记录。

## 激光黑边修复与最新产物

娜迦与远古潜影贝激光改用专用 ONE/ONE 加法混合管线，恢复旧版黑色贴图边缘不遮盖背景的效果。原有双层宽度、UV、口部起点与伤害逻辑保持一致，保留自发光、无叠加层、深度测试及不写深度和 CULL 设置。

无窗口检查验证了管线注册、加法混合、深度与剔除、自发光标记、渲染类型缓存，以及两张光束贴图的尺寸和不透明黑色边缘。build 通过，正式包无临时测试类；未运行客户端画面或重跑服务端完整检查。日志为 `beam-geometry-validation.log`，位于隔离测试目录。

最新包为 `aether_bedrock_mobs_reborn-26.1.2-neoforge-1.3.jar`，SHA-256 为 `525746E2EF63C4C19398F0644029E208D5463E3EBC203AEE66FD15D3F20BD519`。它包含下节的粒子颜色与娜迦分节显示修复，并替代下节的前次构建。客户端验证应观察两个 Boss 的激光黑边是否消失，原有外层发光和内层核心是否正常。

## 粒子颜色与娜迦分节显示回归

当前版本为 1.3。本次修复五类自定义粒子的共用实体红染和表面光照路径，恢复原生粒子管线及碎片的不透明模式，新增仅 F3+B 开启时注册的娜迦分节调试显示。

- 娜迦专项：26 项通过，包括每节 ID 查询、空间查询、射线命中和伤害转发、主碰撞箱外尾段命中、位置与转向跟随、父实体无敌保护及部位清理。
- 无窗口粒子几何回放：10 项通过，验证原生管线、混合模式、RGBA、透明度、默认 NO_OVERLAY 和显式叠加层保留。
- build 通过；JAR 内包含 NagaHitboxDebugRenderer，不包含 ServerValidation、ToastDiagnostic 或 ClientGeometryValidation。

前次粒子与娜迦修复包为 `aether_bedrock_mobs_reborn-26.1.2-neoforge-1.3.jar`，SHA-256 为 `9D19BB7F77E4E397031178B3F2678F45769E30C340BFACC845653A0372F269B5`。日志为 `naga-regression.log` 和 `client-geometry-validation.log`，原始服务端断言为 `results-naga-regression.json`。这些文件均在前述隔离测试目录中。

无窗口回放验证没有启动 Minecraft 客户端，不能代替实际游戏画面验收。客户端需要观察各 Boss 的冲击环、预警、爪痕及碎片颜色，打开 F3+B 检查娜迦五节绿色碰撞箱随转向移动，并实际攻击尾段确认反馈。

下表和旧版本 JAR 校验值属于此前 1.2 的完整迁移验收。本轮对已定位的问题做针对性回归，没有重跑完整 535 项检查。

## 检查结果

| 检查 | 结果 | 主要范围 |
|---|---|---|
| 本体完整功能 | 535 项通过 | 13 种生物、5 个 Boss 的 35 个技能时间轴、自然 AI、保护机制、技能实体、投射物、56 个方块、83 个物品、13 个配方、6 个进度、装备和巨龙联动 |
| 巨龙专项 | 34 项通过 | 远程命中、坠落、预警、技能清理、倍率、属性变化、伤害快照、墙体阻挡、百分比转阶段、击退抗性 |
| 天境维度 | 70 项通过 | 实际 Aether II 维度内的 16 个群系刷新权重与数量、6 个结构的选址与真实区块放置、击退缩放 |
| 存档与重载 | 37 项通过 | 14 个生物 UUID 和生命值、装备、无敌、NoAI、狂暴与巨龙二阶段，以及完整重载后配方和进度重新加载 |
| 正式 JAR | 通过 | 日志确认两个模组均从 build/libs 中的 JAR 加载；启动、资源重载、14 个存档实体生命值读取、保存及正常停服 |
| 发布包检查 | 通过 | 两个包均不包含 ServerValidation 或 ToastDiagnostic 临时类；独立巨龙包包含真实 DragonSkillEntity |

各检查存在覆盖交集，表中数量不代表互不重复的功能数量。测试在隔离存档中使用真实专用服务端，并以 100 tick/s 加速技能验证；没有测量 20 TPS 下多人联机的性能表现。

## 远程技能结果

羽刃命中 12 格外的目标，默认造成 6 点伤害；陨晶从 26 格高处坠落，落地造成 10 点伤害。四类技能在攻击属性 10/20 时的伤害分别为：羽刃 6/12、陨晶 10/20、落雷 12/24、晶棘 9/18。

羽刃被墙体阻挡时不穿墙伤害；落雷前 23 tick 不扣血；晶棘对满击退抗性目标不上抛。发射后修改攻击属性与移除发射者分别验证了实时属性计算和快照回退。1000 最大生命值场景中，501 生命值不转阶段，500 生命值开始转阶段。

## 产物与日志

正式包校验值取自正式 JAR 加载验收完成时；其他聊天后续重新构建独立巨龙时，应重新核对对应包。

| JAR | SHA-256 |
|---|---|
| aether_bedrock_mobs_reborn-26.1.2-neoforge-1.2.jar | CF6306344BCCF1F12B67D748F0AD25B9C3BFA92A6E82FB80A70FAB1C76455440 |
| aether_dragon-1.0.0.jar | 8600A350875D882B5F93A38BD0BD2E50CAF952FD396D8A70C65A289289446C93 |

临时测试源码、隔离世界、原始 JSON 断言和日志都位于 `D:/AI临时文件/aether-reborn-2612/full-server-tests`。使用 `validation.init.gradle` 按 full、dragon-regression、aether-worldgen、persistence 模式运行 runServer；`packaged.init.gradle` 清除开发模组源集，改用正式 JAR 运行。

对应日志：`full-current-artifacts.log`、`dragon-regression.log`、`aether-worldgen-final.log`、`persistence-final.log`、`packaged-server-final.log`。原始结果为对应模式的 `results-*.json` 和 `finished-*.txt`。

## 验收边界

未启动客户端，因此模型、粒子和光束的游戏内视觉尚未验收。源代码迁移、编译与服务端命中均已检查，视觉效果仍须客户端观察。长期自然分布和探索体验也不属于这次自动化检查。

Aether II 注册 GuidebookToastPacket 时输出两条 Toast 客户端类加载错误；临时诊断捕获的调用栈定位至 Aether II 的 GuidebookToast 类型初始化。它们未阻断专用服务端启动、技能命中、世界生成、重载或停服。正式 JAR 验收没有启用该临时诊断类。
