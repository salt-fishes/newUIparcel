# Parcel 取件码 App — UI/功能全量对齐 Web 版改造 PRD

> 版本：v2.0 ｜ 状态：进行中（R8-R15）｜ 关联文档：[ANALYSIS.md](./ANALYSIS.md) · [GOAL.md](./GOAL.md) · [SOP.md](./SOP.md) · [ROADMAP.md](./ROADMAP.md)
>
> 历史版本：v1.1（M3 改造，R0-R7 已完结，见 docs/snapshots/round-0~7）

---

## 1. 背景与问题

Parcel 已完成 Round 0-7 的 M3 规范化改造（纯 surface、单紫色、无底栏）。参考项目 `--main` 是同一产品的 Web 增强复刻版，在视觉与功能上定义了新的产品标准：**emerald 主色、4 套主题、底部 4 Tab、FAB、弹窗化交互、AI 大模型解析、Canvas 导出取件卡片**。

当前 Android 端与 Web 版标准的差距（详见 [ANALYSIS.md](./ANALYSIS.md)）：

| # | 问题 | 现状 | 影响 |
|---|------|------|------|
| P1 | 视觉体系不一致 | 单紫色 + 仅明暗主题；无彩色语义徽章/图标盒 | 与 Web 版产品形象割裂，语义信息（地址/柜号/AI/错误）无色彩区分 |
| P2 | 导航架构落后 | 无底部 Tab；待取/已取混在一个列表靠开关过滤；身份码仅为设置页两个 TextButton | 高频操作（看待取/翻历史/亮身份码）路径深 |
| P3 | 功能缺失 | 无 AI 解析、无导出取件卡片、壁纸参数少于 Web 版、无整站备份 | Web 版核心卖点在 Android 端不可用 |
| P4 | 交互不统一 | Toast 零散、弹窗/路由风格混杂 | 体验不一致 |

## 2. 改造目标

**一句话目标**：让 Android 端的界面与功能体验与 Web 参考版（--main）一致，且原生能力（短信/通知/小组件/真实身份码）零回归。

| 目标 | 衡量标准 |
|------|----------|
| G1 视觉对齐 | emerald 主色；4 主题 + 跟随系统即时生效；面板纯色组件族唯一出处；无硬编码 Color/sp/dp |
| G2 导航对齐 | 底部 4 Tab（待取角标/历史/身份码/设置）+ 全局 FAB + 统一 Toast 层 |
| G3 功能对齐 | Web 操作清单 100% 有入口；AI 全链路可用；导出 PNG 入相册 |
| G4 原生零回归 | 短信/通知监听/小组件/MIUI/身份码真跳转手动验收全过 |
| G5 文档齐全 | 五件套 + 快照 round-8~15 |
| G6 质量收口 | 终审 grep 残留 0；read_lints 0 错误；用户最终构建通过 |

## 3. 范围

### 3.1 范围内（In Scope）

- 主题系统扩展：4 套 ColorScheme + `ParcelPalette` 语义色 + 纯色 Surface 组件族（无玻璃，决策 D1）+ 主题渐变背景层
- 导航重构：底部 4 Tab（tab_pending/tab_archive/tab_identity/tab_settings）+ MainScaffold + 全局 FAB/Toast；home 拆 Pending/Archive；身份码整页（真实 Scheme 跳转）
- 列表与卡片视觉对齐：AppNavbar、ListToolBar、AddressCard、PickupCodeChip、SmsDataRow、IconBox、RawSmsDialog、EmptyState
- 设置中心：8 项彩色菜单 + Preferences/Backup 子页 + ThemeBgSheet（主题卡 + 壁纸控制，补 brightness/contrast/fit）；整站 JSON 备份
- **新功能 1 — AI 大模型解析**：14 家 OpenAI 兼容服务商预设、连通测试、模型拉取/搜索/增删、系统提示词编辑、录入页 AI 预识别、失败降级正则、解析缓存读-through（HttpURLConnection + kotlinx-serialization，仅 https，决策 D3/D4/D5）
- **新功能 2 — 导出取件图片卡片**：Canvas 绘制 Bitmap（1280 宽、4 配色、按地址分组）+ MediaStore 入相册 + 复制文本清单
- 文档交付：ANALYSIS / GOAL / PRD / SOP / ROADMAP v2.0 + README + 每轮快照

### 3.2 范围外（Out of Scope）

- 短信解析逻辑（SmsParser / SmsProcessor 主流程）的修改（仅允许 R13 增加可选缓存参数）
- 数据层结构变更（SharedPreferences 双文件维持现状；仅新增键）
- 桌面小组件（widget/）布局重设计（仅保证不崩）
- Web 版演示假身份码的移植（身份码只做真实跳转，决策 D7）
- 构建验证（由用户在 R15 后统一执行，决策 D8）
- 真正的 git push（沿用 docs/snapshots 文件快照）

## 4. 用户场景与信息架构

### 4.1 核心用户场景（按频率排序）

1. **看码取件**（最高频）：打开 App → 待取 Tab 一眼看到取件码/地址/柜号 → 复制或凭码取件
2. **亮身份码**：底部 Tab 一键直达 → 柜前高亮 → 跳转淘宝/拼多多原生 App
3. **找历史**：历史归档 Tab + 时间筛选 / 柜号排序 / 横向标签
4. **手动录入**：FAB → 粘贴短信 → 正则实时预览 / AI 预识别
5. **维护解析**：规则中心、失败排查、日志
6. **个性化**：4 主题、壁纸微调、关怀模式
7. **分享取件**：导出图片卡片发家人群

### 4.2 导航结构（改造后）

```
MainScaffold（底栏 4 Tab + 全局 FAB + Toast 层）
├── tab_pending    待取包裹（角标 = 待取总数）
├── tab_archive    历史归档
├── tab_identity   身份码（真实 Scheme 跳转，Web 版布局）
└── tab_settings   设置与功能（8+1 项彩色菜单）

二级路由（底栏/FAB 隐藏，全部保留可达）：
add_custom_sms / add_rule / address_group / rules / fail_sms / success_sms
about / use_notification / logs / ai_settings（新）/ preferences（新）/ backup（新）
```

### 4.3 主页（待取/历史）信息层级

```
AppNavbar：品牌（图标盒+标题）+ 关怀开关 + 主题入口 + 视图偏好下拉（排序/布局/时间显隐）
ListToolBar：时间筛选按钮 + 导出图片卡片 + 排查(N) 徽章
地址分组卡片列表（核心内容区）
├── 卡片头：紫图标盒 + 地址 + 标签胶囊 + "N 件待取"(emerald) + 一键取件 + 折叠箭头
└── 取件码行：emerald mono 码块（点击完成/复制）+ 靛蓝柜号徽章 + mono 时间 + 原文
FAB：录入短信
```

## 5. 设计规范（验收依据）

### 5.1 色彩令牌

主色族（对齐 Web Tailwind 色板）：

| 语义 | 色值 | 用法 |
|------|------|------|
| emerald-500 `#10b981` / emerald-400 `#34d399` | 主操作、选中态、取件码、FAB（渐变至 teal-500 `#14b8a6`） |
| purple-500 `#a855f7` / purple-300 `#d8b4fe` | 地址图标盒、标签 |
| indigo-500 `#6366f1` / indigo-300 `#a5b4fc` | 柜号徽章 |
| amber-500 `#f59e0b` / amber-600 `#d97706` | AI 相关、关怀模式 |
| rose-500 `#f43f5e` / rose-300 `#fda4af` | 错误、排查、删除 |
| sky-500 `#0ea5e9` / blue-500 `#3b82f6` | 规则、日志 |
| orange-500 `#f97316` | 身份码 App 跳转 |

4 套主题面板色（不透明化，决策 D1）：

| 主题 | 页底/渐变 | 卡片（不透明） | 文字 |
|------|-----------|----------------|------|
| 经典深色 | slate-950 `#020617`，渐变 slate-900→slate-950→indigo-950 | `#1e293b` 系 | slate-100 `#f1f5f9` / 次要 slate-400 |
| 日间明亮 | slate-100，渐变 slate-100→emerald-50→teal-50 | 白 `#FFFFFF`（≥0.96 等效） | slate-900 `#0f172a` / 次要 `#475569` |
| 翡翠夜景 | slate-950，渐变 slate-950→emerald-950→teal-950 | 深绿 `#064e3b` 系 | `#ecfdf5` / `#a7f3d0` |
| 极光紫罗兰 | indigo-950，渐变 slate-950→indigo-950→purple-950 | 深紫 `#3a0ca3` 系 | `#faf5ff` / `#e9d5ff` |

- 面板/卡片/芯片唯一出处 `ui/components/Surface.kt`；禁止半透明模糊（无玻璃，D1）
- 浅色主题卡片强制白底保证对比度 ≥ 4.5:1

### 5.2 字体

| 用途 | 规格 |
|------|------|
| 弹窗/页面标题 | 18-20sp / Bold |
| 卡片地址标题 | 16sp / Bold（关怀 20sp） |
| 取件码 | 20sp / Black / Monospace（关怀 24sp） |
| 正文/按钮 | 14sp |
| 辅助/列表项 | 12sp |
| 徽章/时间 | 9-11sp / Monospace（时间戳） |

老人（关怀）模式：整体 1.5x 机制保留；**禁止硬编码 sp**，全部走 typography/令牌。

### 5.3 间距 / 形状 / 触控

- 间距令牌：沿用 `ParcelSpacing`（4/8/16/24/32/48）
- 圆角：徽章 8 / 按钮与输入框 12 / 卡片与行项与图标盒 16 / 大卡片与弹层 24 / FAB 与 Toast 与开关全圆
- 触控目标：≥ 48×48dp；老人模式 ≥ 56dp；取件码芯片 ≥ 52dp 高

### 5.4 动效与无障碍

- 状态切换 100-200ms；展开/收起 200-300ms；标准缓动
- 可交互图标必须有 contentDescription（描述动作，不含"按钮"）
- 文字对比度 ≥ 4.5:1（浅色主题与彩色徽章重点抽查）

## 6. 功能需求（分轮映射详见 ROADMAP）

| ID | 需求 | 优先级 | 轮次 |
|----|------|--------|------|
| F1 | 4 主题 ColorScheme + ParcelPalette + Surface 组件族 + 壁纸三参 + 渐变背景 | P0 | R9 |
| F2 | 底部 4 Tab + MainScaffold + 全局 FAB/Toast + home 拆分 + 身份码整页 | P0 | R10 |
| F3 | 列表卡片视觉对齐（AppNavbar/ListToolBar/AddressCard/PickupCodeChip/IconBox 等） | P0 | R11 |
| F4 | 设置中心 8 项菜单 + Preferences/Backup 子页 + ThemeBgSheet + 整站备份 | P0 | R12 |
| F5 | AI 模块（预设/连通测试/模型管理/系统提示词/录入预识别/缓存读-through） | P0 | R13 |
| F6 | Canvas 导出取件卡片（4 配色/范围选择/含已取/入相册/复制文本） | P1 | R14 |
| F7 | 终审 + 文档收官 | P0 | R15 |

## 7. 验收标准（DoD）

每轮必须同时满足：

1. 静态验证通过：残留 grep 归零 + 代码审查 + read_lints 类检查 0 错误（本轮不构建，决策 D8）
2. 该轮 Review 检查清单（见 SOP.md §4）全部通过并记录
3. `docs/snapshots/round-N-*.md` 快照已生成（变更清单 + Review 结果 + 回滚要点）
4. 用户对快照确认通过

整体验收（R15 终审）：GOAL.md 中 G1-G6 全部勾选；用户本机 `gradlew.bat assembleDebug` 构建通过并反馈。

## 8. 风险与对策

| 风险 | 等级 | 对策 |
|------|------|------|
| Tab 化影响小组件刷新/数据链路 | 高 | 数据层不动；R10 静态核对 updateAllWidget/readAndParseSms 调用点全保留 |
| 浅色主题对比度不达标 | 中 | 浅色卡片强制白底（≥0.96 等效）；壁纸遮罩下限保留 |
| AI 接入破坏 SmsProcessor 同步性 | 高 | 仅加可选缓存读-through 参数（默认空，向后兼容）；AI 失败降级正则 + warn 日志；自动短信默认不走 AI |
| 密钥泄露 | 中 | 密钥不入日志（grep 验证）；备份导出含 apiKey 时 Toast 明文提示 |
| 老人模式被硬编码字号破坏 | 中 | 静态审查禁硬编码 sp/dp/Color（ui/theme 除外） |
| 底栏/FAB 显隐遗漏新路由 | 低 | R10/R12/R13 各做一次路由表静态核对 |
| 壁纸新参旧存储不兼容 | 低 | 默认值兜底 + coerce 范围 |
| MediaStore 保存失败 | 低 | Toast + 日志，不崩溃 |
| 无 VCS 无法回滚 | 中 | 每轮快照记录精确变更文件清单与回滚要点；建议用户每轮前备份 app\src |

---

*本文档为 R8 交付物，后续轮次以 ROADMAP.md 为准执行。*
