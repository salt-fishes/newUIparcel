# Round 13 — AI 大模型解析模块

状态：已完成静态 Review，未执行 Gradle 构建（按用户约束由最终验收统一构建）。

## 交付

- 新增 `util/ai/AiConfig.kt`：本地序列化配置、默认系统提示词、解析结果和连通结果模型。
- 新增 `util/ai/AiProviders.kt`：14 家预设服务商、默认模型、常用模型与描述。
- 新增 `util/ai/AiClient.kt`：仅使用 `HttpURLConnection`；模型拉取 8 秒、短信解析 10 秒、连通测试 12 秒；仅允许 HTTPS；OpenAI-compatible JSON 解析；401/404/超时错误分类。
- 新增 `ui/ai/AiSettingsScreen.kt`：服务商切换、API 地址、密钥显隐、模型、图片地址、启停、拉取模型、模型选择、连通测试、系统提示词。
- 修改 `AndroidManifest.xml`：增加 `INTERNET`，未增加 `usesCleartextTraffic`。
- 修改 `SettingsScreen.kt` / `MainActivity.kt`：增加 AI 设置入口和 `ai_settings` 路由。
- 修改 `AddCustomSmsScreen.kt`：启用 AI 时显示 AI 预识别按钮、结果和失败提示；失败不阻断手动保存。
- 修改 `save_data.kt`：新增 `ai_config_json` 与最多 500 条 `ai_parse_cache` 存储函数。

## 静态检查

- `HttpURLConnection` 仅存在 `util/ai/AiClient.kt`。
- 未发现 `usesCleartextTraffic`。
- 未发现将 API Key 写入 `addLog` 的调用。
- 短信接收、通知监听与小组件刷新路径未接入 AI，自动路径保持正则。

## 风险

未进行构建；最终 Android Studio 构建需重点确认 Compose Material3 版本下 `menuAnchor()` 与 `ExposedDropdownMenu` API 签名，以及序列化插件版本兼容性。
