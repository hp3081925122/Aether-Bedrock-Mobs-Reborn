# Aether Bedrock Mobs Reborn — runServer 验证记录

## 2026-10-11

- 命令：`.\gradlew.bat runServer`
- 工作目录：项目根目录，运行目录为 `run/`。
- EULA：`run/eula.txt` 已由用户设置为 `eula=true`。
- 环境：Minecraft 1.20.1、Forge 47.4.23、Java 17、Gradle Wrapper 8.8。

## 逐层排错

1. 初次接受 EULA 后，Aether 的 `common.EntityMixin` 找不到 `Entity#m_8119_()V`。检查发现运行配置没有提供 Forge 1.20.1 开发环境所需的 refmap 重映射。
2. 加入 `mixin.env.remapRefMap` 和 `mixin.env.refMapRemappingFile` 后，Aether Mixin 成功应用；GeckoLib 随后因普通 `implementation` 保留生产混淆字段而报 `NoSuchFieldError: f_279569_`。
3. 将 GeckoLib 改为 `fg.deobf` 后，注册阶段暴露出刷怪蛋供应商提前调用实体 `RegistryObject.get()` 的时序错误。
4. 将刷怪蛋改为 `ForgeSpawnEggItem` 的实体类型供应商后，注册和世界加载完成。

## 最终启动证据

最终 `run/logs/latest.log` 包含：

```text
Remapping refMap aether.refmap.json using .../build/createSrgToMcp/output.srg
Preparing level "world"
Preparing start region for dimension minecraft:overworld
Done (15.832s)! For help, type "help"
```

本次启动没有新的 `LoadingFailedException`、`NoSuchFieldError` 或 `Registry Object not present`。Forge/Aether 的在线版本检查出现超时和连接重置警告，属于外部更新查询失败；Aether 配置和首次生成的 `server.properties`、世界服务端配置被正常写入，不影响 `Done` 结果。

## 验收边界

这次验证确认了专用服务端可启动、模组发现、Mixin 应用、注册事件和 Overworld 出生区生成。它没有验证玩家连接后的命令、Aether 维度进入、每个生物的实际生成和攻击、模型渲染、物品主动效果、掉落配方、结构生成或完整正常停服；这些仍需客户端/联机游戏内测试。测试实例已在出现 `Done` 后结束，未把强制结束当作正常停服证据。
