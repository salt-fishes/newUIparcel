# Round 18 — 统一消息摄取

日期：2026-07-24
状态：静态完成，未执行 Gradle。

## 交付

- 新增 `ParcelMessageRepository`，统一读取系统短信与自定义消息。
- 通知正文若在 12 小时窗口内已存在于短信箱则跳过。
- 自定义消息在 12 小时窗口内按正文指纹去重；更晚的同文案仍允许作为新包裹入库。
- 通知和手动录入成功后直接调度 AI，新消息无需等待 App 再次打开。

## 静态 Review

- MainActivity、SmsSyncService、SmsProcessor 与 Widget 统一走 Repository。
- `SmsModel` 增加兼容旧 JSON 的 source/sender/packageName 默认字段。
- 系统短信读取与通知监听权限路径保持原有实现。
