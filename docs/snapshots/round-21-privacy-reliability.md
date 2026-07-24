# Round 21 — 隐私、成本与可靠性收官

日期：2026-07-24
状态：静态终审完成，未执行 Gradle。

## 交付

- API Key 使用 Android Keystore AES/GCM；旧明文仅在迁移成功后擦除。
- 普通配置与整站 JSON 备份不包含 API Key；密文与解析账本排除系统备份/设备迁移。
- 日志隐藏 API Key、Authorization、Bearer、短信与通知正文。
- 每日额度使用原子 `tryConsume`，覆盖后台自动解析和手动 AI 预识别；0 表示不限。
- Worker 使用指数退避并最多尝试 3 次，所有成功/失败状态均广播刷新。
- 失败短信页展示等待、处理中、重试等待、失败原因和单条重新解析入口。

## 静态终审

- WorkManager、Kotlin serialization、Compose Material3 与 INTERNET 依赖声明存在。
- `SmsProcessor.process/loadAndProcess` 调用签名已核对。
- `getAiConfig/saveAiConfig` 迁移无递归；Keystore 失败不会清除旧明文配置。
- 仍未运行 Gradle；Kotlin/Compose 实际编译、网络服务、权限、小组件、MIUI 与 Scheme 需用户统一构建和真机验收。
