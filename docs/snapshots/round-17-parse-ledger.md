# Round 17 — 幂等解析账本

日期：2026-07-24
状态：静态完成，未执行 Gradle。

## 交付

- 新增 `ParseRecord`、`ParseStatus`、`ParseEngine` 与 SharedPreferences 解析账本。
- 记录 PENDING / PROCESSING / SUCCESS / FAILED / RETRY_WAIT、模型、错误、次数与重试时间。
- 标准化正文后计算 SHA-256；短信、通知和手动录入的相同正文共享解析结果。
- 账本最多保留最近 1500 条，并提供单条 reset。

## 静态 Review

- 成功记录可复用，失败和处理中记录不会被普通调度重复入队。
- 解析账本已排除系统云备份和设备迁移。
- SharedPreferences 为临时实现；高并发或多进程长期方案仍建议迁移 Room。
