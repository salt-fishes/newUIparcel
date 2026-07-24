# 差异分析报告：newUIparcel（Android）vs --main（Web 参考版）

> 版本：v2.0 ｜ 日期：2026-07-24 ｜ 关联文档：[PRD.md](./PRD.md) · [GOAL.md](./GOAL.md) · [ROADMAP.md](./ROADMAP.md)
>
> 对比基准：参考项目 `D:\project\--main\--main`（React 18 + Tailwind v4 单页 Web App，包裹取件码管理器增强版）
> 被改造方：本项目（Kotlin + Jetpack Compose + Material3，刚完成 Round 0-7 M3 改造，纯 surface + 单紫色）

---

## 0. 结论摘要

两个项目是同一产品的两个端：Web 版在视觉表现（4 套主题、emerald 主色、彩色语义徽章、底部 Tab 导航、弹窗化交互）与功能（AI 大模型解析、Canvas 导出取件卡片）上领先；Android 版在系统能力（短信自动读取、通知监听、桌面小组件、真实身份码跳转）上独有且必须保留。**改造方向 = Web 版视觉与功能全量对齐 + Android 原生能力零回归。**

### 已锁定关键决策

| # | 决策点 | 结论 |
|---|--------|------|
| D1 | 面板质感 | **不做玻璃效果**：面板/卡片/芯片用各主题不透明纯色 + 主题 outline 细边框；主题对角渐变背景层保留 |
| D2 | 录入短信 | 保持全屏路由页（不做 Dialog，粘贴长短信与 AI 预览需要空间） |
| D3 | AI 网络层 | HttpURLConnection + kotlinx-serialization（零新第三方依赖） |
| D4 | 明文流量 | **仅 https**；不加 usesCleartextTraffic；http 地址在 UI 层拒绝并提示 |
| D5 | 自动短信解析 | 自动接收默认走正则；AI 仅手动录入路径默认启用；解析结果缓存读-through |
| D6 | 主题选项 | 4 套主题（经典深色/日间明亮/翡翠夜景/极光紫罗兰）+ 保留"跟随系统" |

---

## 1. 视觉体系差异

| 维度 | Web 版（对齐目标） | Android 现状（R0-R7 后） | 差异/动作 | 轮次 |
|------|------|------|------|------|
| 主色 | emerald-500 `#10b981`，FAB emerald-400→teal-500 渐变 | M3 基线紫 `#6750A4` 单强调（`ui/theme/Color.kt`） | primary 系全量换 emerald | R9 |
| 主题 | 4 套 dark/light/emerald/violet + CSS 类覆盖 | ThemeManager 仅 0/1/2（跟随系统/明/暗）+ 双 ColorScheme | 扩 4 套 ColorScheme + `ParcelPalette` 语义色 CompositionLocal；保留跟随系统 | R9 |
| 面板质感 | glass-panel/card/chip 半透明 + backdrop blur + 白边 | 不透明 surfaceContainer*（`ui/components/ParcelContainers.kt`） | **按 D1**：不透明纯色 Surface 组件族（色值取 Web 四主题 panel/card/chip 的不透明化），无模糊 | R9 |
| 页面背景 | 每主题一条对角渐变；壁纸支持 blur/opacity/brightness/contrast/fit | 纯 background；WallpaperBackdrop 仅 blur/overlay/gradient/对齐 | 渐变背景层回迁（ThemeBackdrop）；壁纸补 brightness/contrast/fit 三参 | R9 |
| 字号 | 9~24sp 层级；取件码 mono font-black 20/24sp | M3 全级距 11~57sp，老人 1.5x | 保留 typography 机制，新增 mono/black 取件码令牌；禁硬编码 sp | R9/R11 |
| 图标 | lucide 线性 + 彩色图标盒（bg-{c}-500/20） | Material Icons，无图标盒 | 彩色图标盒抽为共享件 `IconBox.kt` | R11 |
| 语义徽章 | 紫=地址、靛蓝=柜号、琥珀=AI/关怀、玫红=错误、天蓝=规则、绿=主操作 | 无统一语义徽章体系 | ParcelPalette 提供全色族，组件按语义取用 | R9-R12 |

## 2. 导航架构差异

| 维度 | Web 版 | Android 现状 | 差异/动作 | 轮次 |
|------|------|------|------|------|
| 一级导航 | 底部 4 Tab：待取包裹（角标）/历史归档/身份码/设置 | 无底部导航；单 NavHost 11 路由（home/settings/rules/...） | 新增 MainScaffold 底部 4 Tab；home 拆 Pending/Archive 两 Tab；身份码整页 | R10 |
| FAB | 全局固定（4 Tab 均显示）"录入短信" | 仅 HomeScreen 有 FAB | FAB 全局化，仅 Tab 根路由显示 | R10 |
| 二级页面 | 12 个模态弹窗 | 全屏路由 | 路由保留重绘；TimeFilter=BottomSheet；原文/导出=Dialog；录入保持全屏（D2） | R10-R14 |
| Toast | 统一绿色胶囊组件（2.4s 自动消失） | `Toast.makeText` 零散调用 | 统一 Compose Toast 层 | R10 |
| 待取/已取 | 两个独立 Tab | 单列表 + showCompleted 开关过滤 | 拆为两个 Tab；showCompleted 偏好退役（键保留不读） | R10 |

## 3. 页面与组件差异（Web → Android 映射）

| Web 组件 | Android 对应 | 差异/动作 | 轮次 |
|------|------|------|------|
| Navbar.tsx（品牌+关怀开关+主题入口+视图下拉） | HomeTopBar.kt（结构不同） | 重建为全局 AppNavbar，挂 MainScaffold；删 HomeTopBar | R11 |
| ParcelCard.tsx（折叠/一键取件/标签胶囊） | AddressCard.kt（结构近似） | 视觉全改：紫图标盒+emerald 计数+一键取件胶囊 | R11 |
| PickupCodeChip.tsx（点击完成/复制反馈） | PickupCodeChip.kt | emerald mono 码块+复制 1.8s 反馈 | R11 |
| 行内柜号徽章/时间/原文 | SmsDataRow.kt | 靛蓝柜号徽章 + mono 时间 + RawSmsDialog | R11 |
| IdentityView.tsx（整页身份码） | 无（仅设置页两个 TextButton） | **新建 IdentityScreen**：Web 布局（横幅/平台切换/跳转横幅/柜前高亮/提示卡）× Android 真实 Scheme 跳转（IdentityEntries.kt）；**不做 Web 版演示假码** | R10 |
| SettingsView.tsx（9 项彩色菜单+3 子页） | SettingsScreen.kt（分组 ListItem） | 重写为彩色图标盒 8 项菜单（AI 项 R13 加入）+ Preferences/Backup 子页 | R12/R13 |
| ThemeBgModal（主题卡+壁纸画廊+4 滑杆） | WallpaperAdjustSheet（部分参数） | 新建 ThemeBgSheet 双 Tab 弹层，吸收壁纸调节，删旧 Sheet | R12 |
| ExportCardModal（Canvas 导出 PNG） | 无 | 新建 ExportCardRenderer + ExportCardDialog | R14 |
| AddSmsModal（引擎横幅/AI 预识别/强行保存） | AddCustomSmsScreen.kt | 重绘 + AI 预识别接入 | R13 |
| RulesModal / LogsModal / FailedSmsModal / AboutModal / AddressGroupModal / TagModal / TimeFilterModal | rules / logs / fail_sms / about / address_group / TagDialog / TimeFilterSheet | 路由与结构保留，视觉逐轮重绘为主题化 Surface | R11-R12 |

## 4. 功能模块差异

| 功能 | Web 版 | Android 现状 | 结论 | 轮次 |
|------|------|------|------|------|
| AI 大模型解析 | 完整：14 家 OpenAI 兼容预设 + 连通测试 + 模型管理 + 系统提示词 + 失败降级正则 | 无 | **全新实现**（HttpURLConnection，仅 https；缓存读-through 接入 SmsProcessor；自动短信默认不走 AI） | R13 |
| 导出取件图片卡片 | Canvas 2x PNG（640 宽、4 配色、按地址分组）+ 复制文本 | 无 | **全新实现**（Bitmap 1280 宽 + MediaStore 入相册） | R14 |
| 主题换肤 | 4 主题 + 关怀模式 | 明/暗/跟随系统 + 老人模式 | 对齐扩展 | R9 |
| 自定义壁纸 | 预设/上传/URL + 模糊/遮罩/亮度/对比度 + Cover/Contain | 本地壁纸 + 模糊/遮罩/渐变/对齐 | 补 brightness/contrast/fit | R9/R12 |
| 整站备份 | settings+rules+mappings 导出 JSON | 仅规则 JSON 导入导出 | 扩展 exportFullBackupJson | R12 |
| 短信自动读取 | 无（纯手动录入） | SmsReceiver + ContentObserver + 权限引导 | **保留，逻辑不动** | — |
| 通知监听保活 | 无 | NotificationListener + WorkManager 健康检查 | **保留，界面重绘** | R12 |
| 桌面小组件 | 无 | 5 个小组件（含 MIUI 双规格） | **保留不动** | — |
| 身份码 | 演示假码（不可用） | 真实 Scheme 跳转（淘宝 tbopen / 拼多多 pinduoduo） | **融合**：Web 布局 + Android 真跳转 | R10 |
| 正则解析引擎 | smsParser.ts | SmsParser.kt（已对齐增强版能力） | 不动 | — |
| 三级解析/失败排查/日志/标签/柜号排序/时间筛选 | 有 | 有 | 逻辑不动，视觉对齐 | R11 |

---

*本报告为 R8 交付物，作为 PRD v2.0 的差异依据；具体数值以 `C:\Users\DENG\.workbuddy\plans\swift-thunder-curie.md` 附录 A（Web 视觉规范全量提取）为准。*
