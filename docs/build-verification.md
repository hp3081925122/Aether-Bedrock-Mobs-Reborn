# 构建与静态检查记录

## 2026-10-11

- 工程：Minecraft 1.20.1 Forge 47.4.23，Java 17，Gradle Wrapper 8.8。
- 命令：`.\gradlew.bat build`。
- 结果：`BUILD SUCCESSFUL`；无测试源，Gradle test 任务为 `NO-SOURCE`。
- 产物：`build/libs/aether_bedrock_mobs_reborn-0.1.0.jar`，SHA-256 为 `434AE6AF03136CAD012A18A9F09F4204613EA3A6EAC83DD1EDE4D4E1715DCA04`，大小 2869296 字节。
- JSON：资源目录共 351 个 JSON，使用支持空字符串键的哈希解析方式全部通过。
- 命名空间：源码和资源中没有 `aethermobs:` 或 `aethermobs.` 残留。
- Java 源码：没有块注释、行注释或 HTML 注释残留。
- 专用服务端：已接受 EULA 后运行 `.\gradlew.bat runServer`；补充 refmap 重映射、GeckoLib 解混淆依赖和延迟刷怪蛋注册后，日志完成世界出生区生成并出现 `Done (15.832s)!`。完整记录见 `docs/runserver-verification.md`。
- 注意：语言资源、刷怪蛋颜色、事件逻辑、投射物/粒子注册、动作映射和傀儡动画关键帧修复均是在上述构建记录之后完成的。根据用户要求，本轮没有自动重新执行 build/runClient，因此现有 `build/libs` 产物不包含本轮源码资源变更。
- 本轮未执行 build/runClient；仅完成 351 个 JSON 解析、36 个声音事件与 OGG 文件逐项对应、24 个 Java 文件括号/混淆名静态检查，以及 11 个实体渲染注册覆盖检查。

## 尚未覆盖

- `runClient` 已进入集成世界并完成 JEI 初始化；这只覆盖启动、注册同步和基础世界进入，不代表实体渲染、结构生成或实际战斗全部通过。详见 `docs/runclient-verification.md`。
- 已完成专用服务端基础数据加载和 Overworld 出生区生成；尚未完成 Aether 维度内的结构生成验收。
- Boss 专属攻击、3 个新移植的 Java 专用模型的客户端视觉验收和完整旧存档兼容仍在迁移范围内。




