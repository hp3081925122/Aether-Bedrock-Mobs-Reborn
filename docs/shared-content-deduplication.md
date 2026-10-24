# 天境巨龙共享内容去重与旧存档兼容

## 范围

将以下 18 种方块及其方块物品、3 件盔甲从 `aether_bedrock_mobs_reborn` 迁移到已安装前置的 `aether_dragon` 同名注册项；不修改前置模组源码，不变更其他装备、材料或五个 Boss。

方块：`aureatebluebeam`、`aureatebluebeambrick`、`aureatebluehead`、`aureatebluepillar`、`aureatebricks`、`aureatechiseled`、`aureatecobblestone`、`aureatekeystone`、`aureateleaves`、`aureatepillar`、`aureatepillarbeam`、`aureatepillarhead`、`aureatepillarheadbrick`、`aureatescript`、`aureatetilebricks`、`aureatetiles`、`aurorastone`、`dragonspawner`。

盔甲：`shaday_helmet`、`shaday_chestplate`、`shaday_leggings`。

## 兼容约束

- 本模组不再注册这 18 种方块、18 个方块物品及 3 件盔甲的独立实例。
- 按当前加载器源码支持的旧映射重定向或注册表别名，兼容旧物品堆栈、容器、掉落实体、区块方块调色板及结构中的旧 ID；不扫描或批量改写用户存档。
- 更新本模组配方、掉落、标签、世界生成和结构资源，使新内容直接引用前置 ID。
- 保留旧物品堆栈的数量、损耗、附魔及自定义数据；方块重定向保留可用方块状态。
- 巨龙刷怪器统一使用前置方块。安装本模组时禁止靠近自动召唤，仅允许非旁观玩家主手持有终末之钥右键召唤；保持原有钥匙不消耗、成功后刷怪器消失及保护插件取消交互行为。
- 旧原版刷怪笼中召唤前置巨龙的入口继续受钥匙规则约束。
- 不提高版本号，不上传，不改动先前其他任务未提交的兼容代码。

## 验证

1. 三版本独立构建并检查最终 JAR 的注册与资源引用。
2. 检查旧盔甲和方块物品堆栈反序列化、旧区块调色板读取及再次保存，确认实际对象为前置注册项。
3. 验证前置刷怪器的靠近、空手、错误物品、副手、旁观者、主手终末之钥及重复激活路径。
4. 区分静态验证、构建和实际服务端验证，按真实结果记录。
## 2026-10-16 验证结果

- 当前注册表中本模组方块从 56 种减至 38 种。18 个旧方块 ID、21 个旧物品 ID 不再拥有独立注册实例，查询旧 ID 时得到前置对象。
- Forge 通过启动别名和 MissingMappingsEvent 重映射兼容旧注册表快照；额外对 NamespacedWrapper 的 ResourceKey holder 查询补充同范围重定向，覆盖 NbtUtils 与区块调色板 codec 读取。Mixin 同时声明开发官方名 getHolder 和正式 SRG 名 m_203636_，不依赖缺失的 refmap。两个 NeoForge 分支使用当前加载器 DeferredRegister.addAlias。
- 三版本均完成独立服务端测试，结果都是 `DEDUP_VERIFICATION_PASS checks=215`，测试服完成正常保存并停止。
- 实测 18 种旧方块的 NbtUtils 读取、BlockState.CODEC 调色板解码及规范 ID 保存；巨龙刷怪器 powered=true 保留。
- 实测 21 种旧物品堆栈的读取与规范 ID 保存，数量及自定义数据标记保留；三件盔甲同时验证损耗和附魔，保存的完整 NBT 与预期相等。
- 实测前置刷怪器空手或持钥匙靠近均不自动召唤；空手、错误物品、只有副手钥匙和旁观者均不能激活；主手终末之钥成功召唤且不消耗钥匙，刷怪器移除，只产生一个巨龙，重复调用失败。
- 服务端验证使用独立的 migrationVerification source set，源码及运行数据在 `D:/AI临时文件/aether-shared-content-20261016/1201`、`1211`、`2612`，最终日志为各目录 `server-final.log`，结果文件为 `result.txt`。最终运行目录为各目录的 run，世界根为 run/worlds；测试断言检查了实际运行目录。
- 未运行客户端，因此不将这些检查表述为客户端渲染或人工交互测试。兼容范围是注册 ID 及相应 NBT/codec 读取，不改写第三方自定义数据中任意字符串。
- 前置天境巨龙自身仍有未注册条目对应的旧掉落表/进度资源报错，本次不修改前置；所迁移的目标注册项均由服务端实际验证存在。

## 测试目录失误记录

首次 Forge 验证的 JavaExec workingDir 被运行插件覆盖，打开并保存了项目开发服 `120 forge Aether Bedrock Mobs Reborn/run/world`，未打开客户端 saves。旧 ID 映射确已在该世界应用，但同时进行了刷怪器测试，不应视为无修改读取。该次日志保存于 `D:/AI临时文件/aether-shared-content-20261016/1201/server.log`，开发服在 10:20:31 前正常保存并停止。后续已通过加载器 run 配置和显式 universe 子目录修正隔离，并重新通过全部检查。

已把该开发服的测试后状态复制到 `D:/AI临时文件/aether-shared-content-20261016/forge-development-world-after-first-test` 留存；这不是修改前备份。窄范围查找未发现可确认属于该开发服的原始备份，因此没有尝试猜测恢复。客户端 saves 未被打开。

## 最终构建与归档检查

三版本 `gradlew --offline build` 均成功。最终 JAR 不含已移除的 18 套方块状态/模型、21 个物品模型、26.1.2 的 21 个物品定义、旧 Shaday 实现类及测试 mod；新映射类和 Mixin 配置均在产物中。Forge 产物的双方法选择器和 MC 调用生产 SRG 重映射另经 javap 静态核对。

本分支最终 JAR SHA256：`8E6B6DDB60AC4D67B0D1BDF9F0B0228968DEE4384F5264C7F3A7399889B86341`。

## 后续1.6发布阶段

用户随后要求版本+0.1并上传CF。本阶段已把版本从1.5提高为1.6，三版build通过并提交，作者后台状态为Processing。已核对前置源码及实际依赖JAR：前置在Reborn存在时隐藏自身创造栏，通过构建事件将18种方块和三件Shaday盔甲分别加入Reborn方块栏、物品栏，故无需在本模组重复追加。完整发布元数据、文件ID、SHA256及状态证明见 `D:/AI临时文件/aether-reborn-release-1.6-20261016/release-status.md`。
