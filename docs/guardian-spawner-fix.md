# 黄金城守护天使刷怪笼修复

## 原因

黄金城模板池的钥匙召唤规则将全部 minecraft:spawner 转为 aether_dragon:dragonspawner。前置提供的 goldcity_aether 模板中六个原版刷怪笼的 SpawnData 也误指向了 aether_dragon:aetherdragon。因此原先的六个守护天使刷怪点被转成了钥匙方块。

## 修复范围

修改本模组天境黄金城模板池规则，并通过数据资源覆盖主世界黄金城模板池的处理器。覆盖资源仅在安装本模组时生效，不修改前置模板文件。真正的 aether_dragon:dragonspawner 保留钥匙召唤规则；六个原版刷怪笼保持 minecraft:spawner，并用原版 append_static 方块实体修改器将 SpawnData.entity.id 设置为 aether_bedrock_mobs_reborn:guardianangel，清空错误候选列表。其他延迟、数量、范围等参数沿用模板，不改前置源码或已有存档。

先前钥匙测试将七处都转换成功当作正确断言，未核对六个守卫点的用途，该断言不足以验证原设计。

## 已有存档限制

新生成地牢使用修复规则。已被替换并保存的六处方块丢失原刷怪笼 NBT，本次不会自动识别或批量还原；需要已知地牢位置及方向后，按六个局部坐标精确恢复。不能把所有巨龙刷怪器都改成守护天使，否则会破坏真正的 Boss 召唤入口。

## 验证

检查实际依赖模板及 Minecraft 原版处理器编解码；验证规则输出六个守护天使刷怪笼和一个巨龙钥匙刷怪器，并核对原参数。各版验证结果以实际服务端日志和构建结果为准。

## 本次验证结果

- 独立服务端运行通过：`GUARDIAN_SPAWNER_PASS checks=50 templates=2 guards=12 bosses=2`。读取实际资源管理器的两个模板池及实际前置模板，确认每个地牢六个守护天使刷怪笼和一个巨龙钥匙入口；原有刷怪参数及位置保持。
- `gradlew build --offline` 成功；正式 JAR 内含两处修复规则，不含临时验证类。
- 没有运行客户端；没有自动恢复已生成地牢，也没有提高版本或重新上传。
