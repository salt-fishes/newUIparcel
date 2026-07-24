# Round 22 — Release/arm64 与 Room 数据层基座

日期：2026-07-24
状态：静态完成，未执行 Gradle 构建。

## 交付

- Release ABI split 只保留 `arm64-v8a`，`isUniversalApk=false`。
- 开启 `android.enableR8.fullMode=true`，保留 release minify 与 resource shrinking。
- `resourceConfigurations` 收窄为 `zh/en`。
- ProGuard 由过宽包级 keep 收紧为模型、AI、Room、Worker、Service、Receiver、Widget 与 MainActivity 定向 keep。
- 新增 `util/db/ParseRecordEntity.kt`、`ParseRecordDao.kt`、`AiUsageEntity.kt`、`AiUsageDao.kt`、`ParcelDatabase.kt`、`LegacyStoreMigrator.kt`。
- `ParseLedger` 与 `AiUsagePolicy` 保持现有 API，内部迁移至 Room。
- 旧 `parcel_parse_ledger` 与 `parcel_ai_usage` 在首次打开数据库时一次性迁移。
- `parcel.db` 及 WAL 文件已排除系统云备份与设备迁移，避免解析结果和取件码跨设备残留。

## 静态检查

- `parcel_parse_ledger` / `records_v2` / `parcel_ai_usage` 仅保留在迁移器中。
- ABI 配置仅包含 `arm64-v8a`，未启用 universal APK。
- Room KSP、runtime、ktx 依赖已声明。
- 未执行 Gradle；KSP 生成、R8 fullMode 和 Room 迁移仍需最终 Release 构建验证。

## 风险

- R8 fullMode 可能暴露 serialization/Compose/WorkManager keep 不足问题。
- Room 目前通过兼容门面保留同步 API，UI 直接读取账本将在 R23 移到 ViewModel 后台状态。
