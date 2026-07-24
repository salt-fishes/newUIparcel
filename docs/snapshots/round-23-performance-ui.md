# Round 23 — 性能节流与 UI 打磨

日期：2026-07-24
状态：静态完成，未执行 Gradle 构建。

## 交付

- `ParcelViewModel` 新增 `FailedMessageUiModel`，在后台线程预计算失败短信的 AI 状态、错误与可重试条件。
- `FailSmsScreen` 不再在列表项重组时读取 `ParseLedger` 或逐条读取 AI 配置；重试与删除操作图标化，保留 48dp 触控目标。
- 新增 `AiRequestThrottle`，后台 AI 请求全局最小间隔 900ms；批量新消息入队增加 0-1s 错峰。
- `AiSettingsScreen` 增加隐私说明：短信正文会发送到用户配置的服务商，可随时切换仅正则，并支持 Wi-Fi 与每日额度限制。
- `AddCustomSmsScreen` 进入页面时刷新 AI 配置，保留预识别防重复点击与每日额度控制。

## 静态检查

- `ui/` 下已无 `ParseLedger.get` / `ParseLedger.fingerprint` 调用。
- AI 请求节流已接入 `AiParseWorker`，批量入队使用 `setInitialDelay` 错峰。
- 失败列表、设置页徽标和工具栏计数仍使用同一 StateFlow，只读 size，无类型适配残留。
- 未执行 Gradle；Compose 重组性能、Room 查询耗时和 WorkManager 节流效果需真机/Profile 验证。
