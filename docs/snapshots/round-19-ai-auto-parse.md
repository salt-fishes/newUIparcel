# Round 19 — AI 自动解析策略

日期：2026-07-24
状态：静态完成，未执行 Gradle。

## 交付

- 主解析链支持 REGEX_ONLY、AI_PREFERRED、AI_ONLY。
- `autoParseSince` 只允许启用后新消息自动入队，历史短信不回灌。
- WorkManager 以正文指纹创建唯一任务，支持 CONNECTED / UNMETERED 网络约束。
- AI 成功结果写入账本，广播刷新 UI；AI 优先模式可配置正则降级。

## 静态 Review

- API 仅允许 HTTPS，网络层仍为 HttpURLConnection。
- 自动任务最多尝试 3 次；认证、额度和最终失败不会无限重试。
- 失败短信页提供单条强制重试，并以 REPLACE 替换旧唯一任务。
