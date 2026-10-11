# Aether Bedrock Mobs Reborn — 依赖与授权矩阵

| 依赖/来源 | 原 JAR 证据 | 重写处理 | 状态 |
|---|---|---|---|
| Minecraft | `versionRange="[1.20.1]"` | 固定 Minecraft 1.20.1 | 已确认 |
| Forge | `loaderVersion="[47,)"` | 模板冻结 Forge 47.4.23 | 已确认 |
| Aether | 必需，原 JAR 范围过宽且重复声明 | 使用 CurseMaven 文件 `6134920`，声明一条必需依赖，范围 `[0.0.1,)`，开发运行启用 refmap 重映射 | 服务端启动通过 |
| GeckoLib | 原 JAR 标为非必需，实际资源使用 GeckoLib | 使用 `4.8.4`，通过 `fg.deobf` 接入，声明一条必需依赖，范围 `[0.0.1,)` | 服务端启动通过 |
| 原模组 ID | `aethermobs` | 重写版冻结为 `aether_bedrock_mobs_reborn`；默认不做旧 ID 存档兼容 | 已确认 |
| 许可证 | JAR `mods.toml` 为 `Not specified`；Modrinth 页面标注 CC0-1.0 | 发布前核对模型、纹理、音效和结构的实际来源；未核对前不复制外部资源 | 待确认 |

## 原元数据缺陷，不直接继承

- GeckoLib 的可选标记与资源实际使用冲突。
- Aether 依赖重复声明。
- 依赖范围 `[0,)` 过宽，不能作为目标兼容范围。
- JAR 元数据未声明许可证。
- MCreator 生成的显示名和作者字段不能作为 Reborn 的发布元数据。

