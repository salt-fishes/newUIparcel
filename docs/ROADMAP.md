# Parcel Web 版对齐改造 — 分轮路线图（ROADMAP）

> 版本：v2.0 ｜ 关联文档：[PRD.md](./PRD.md) · [SOP.md](./SOP.md) · [GOAL.md](./GOAL.md) · [ANALYSIS.md](./ANALYSIS.md)
>
> 执行规则：严格按轮次顺序执行；每轮遵循 SOP 的"勘察→实施→静态验证→Review→快照"循环；用户确认通过后方可进入下一轮。
> 路径别名：`K` = `app\src\main\java\com\chenran\parcel`
> 历史：v1（M3 改造 R0-R7）已完结；v2 主体改造 R8-R15 已静态交付；R16-R21 为大字模式与 AI 自动解析补强。

---

## 总览

| 轮次 | 主题 | 关键产出 | 依赖 |
|------|------|----------|------|
| R8 | 文档交付 | ANALYSIS / GOAL / PRD / SOP / ROADMAP v2.0 + README | — |
| R9 | 视觉 tokens | 4 主题 + ParcelPalette + Surface 组件族 + 壁纸三参 | R8 |
| R10 | 导航架构 | 底部 4 Tab + MainScaffold + 身份码整页 | R9 |
| R11 | 列表视觉对齐 | AppNavbar / ListToolBar / 卡片组件 emerald 化 | R10 |
| R12 | 设置中心 | 8 项菜单 + Preferences/Backup 子页 + ThemeBgSheet | R11 |
| R13 | AI 模块 | 14 预设 / 连通测试 / 模型管理 / 录入预识别 | R12 |
| R14 | 导出卡片 | Canvas PNG + MediaStore + 复制文本 | R13 |
| R15 | 终审收官 | G1-G6 终审 + 文档勾选 + 交付用户构建 | R14 |
| R16 | 大字模式重排 | 语义字号 + 专用响应式布局 + 触控目标加固 | R15 |
| R17 | 幂等解析账本 | 正文级 SHA-256 指纹 + 解析状态机 + 结果复用 | R16 |
| R18 | 统一消息摄取 | 系统短信 / 通知 / 手动录入统一仓库与去重 | R17 |
| R19 | AI 自动解析 | 新消息门槛 + WorkManager 唯一任务 + 三策略解析 | R18 |
| R20 | 地址自动归类 | 用户映射优先 + 本地标准化 + AI 高置信建议 | R19 |
| R21 | 隐私可靠性 | Keystore + 脱敏 + 原子额度 + 有限重试 + 单条重试 | R20 |
| R22 | Release/Room 基座 | arm64-only Release + R8 fullMode + Room 账本 | R21 |
| R23 | 性能与 UI 打磨 | 状态模型 + AI 节流 + 隐私提示 + 失败页优化 | R22 |

---

## Round 8 — 文档交付 ✅

**目标**：建立改造依据与追溯机制（零代码改动）。

**任务**：
- [x] 编写 `docs/ANALYSIS.md`（四维度差异分析 + 已锁定决策）
- [x] 编写 `docs/GOAL.md`（最终目标 + G1-G6 完成标准）
- [x] 覆盖更新 `docs/PRD.md`（v2.0）
- [x] 覆盖更新 `docs/SOP.md`（v2.0，静态验证流程）
- [x] 覆盖更新 `docs/ROADMAP.md`（本文档）
- [x] 更新 `README.md`（v2 改造状态 + 文档索引）
- [x] 生成 `docs/snapshots/round-8-docs-v2.md`

**验收**：五份文档齐全且决策 D1-D8 明示；README 含文档索引。

---

## Round 9 — 视觉 tokens 先行

**目标**：颜色/字号/面板来源唯一；4 主题就绪；调用方零改动。

**任务**：
- 修改 `$K\ui\theme\Color.kt`：新增 emerald/teal/slate 及 purple/indigo/amber/rose/sky/orange 色族令牌
- 新建 `$K\ui\theme\ParcelPalette.kt`：语义色 CompositionLocal（panel/card/chip/border/emerald/amber/purple/indigo/rose/sky/orange 及 on 色），4 主题各一份
- 修改 `$K\ui\theme\Theme.kt`：`ParcelTheme(themeMode: Int, isSeniorMode)`；新增 Dark/Light/Emerald/Violet 四套 ColorScheme + Provider
- 新建 `$K\ui\components\Surface.kt`：纯色面板组件族（ParcelPanel/ParcelCardSurface/ParcelChipSurface，不透明 + outline 细边框，**无玻璃**）
- 新建 `$K\ui\components\ThemeBackdrop.kt`：每主题对角渐变全屏层
- 修改 `$K\ui\components\ParcelColors.kt`：签名不变，实现改读 ParcelPalette
- 修改 `$K\ui\components\ParcelContainers.kt`：迁移至 Surface.kt 组件族
- 修改 `$K\ui\components\WallpaperBackdrop.kt` + `$K\util\BackgroundManager.kt`：+brightness(50-150)/contrast(50-150)/fit(cover/contain)，ColorMatrix 实现
- 修改 `$K\util\ThemeManager.kt`：+MODE_EMERALD=3 / MODE_VIOLET=4；`$K\MainActivity.kt` themeMode 直传；`$K\ui\SettingsScreen.kt` 主题段最小适配 5 选项
- 修改 `$K\ui\theme\Type.kt` / `Shape.kt`：mono/black 取件码令牌、24dp 圆角

**验收（专项清单）**：
- `ui/` 下 `Color(0x` 仅存在于 `ui/theme/`
- ParcelColors 公开签名 diff 为空；`LocalIsDarkTheme` 引用完整
- ThemeManager 存量 0/1/2 值兼容

---

## Round 10 — 导航架构改造

**目标**：底部 4 Tab 上线；身份码整页；旧路由全保留。只做结构，不做视觉精修。

**任务**：
- 新建 `$K\ui\main\MainScaffold.kt`：底部 NavigationBar（4 项，待取角标）+ 全局 FAB + 统一 Toast 层；仅 Tab 根路由显示
- 新建 `$K\ui\main\MainTabs.kt`：Tab 定义（待取包裹/历史归档/身份码/设置与功能）
- 新建 `$K\ui\home\ParcelTabContent.kt`：共享工具条 + 列表 + 空态 + TimeFilterSheet；`PendingTab.kt` / `ArchiveTab.kt` 仅传完成态过滤；showCompleted 偏好退役
- 新建 `$K\ui\identity\IdentityScreen.kt`：顶部横幅 / 平台切换（淘宝·菜鸟 / 拼多多）/ App 跳转横幅（调 IdentityEntries 真实 Scheme）/ 柜前高亮 / 提示卡；不做演示假码
- 修改 `$K\MainActivity.kt`：NavHost 重组，startDestination=tab_pending；二级路由全保留；旧 `home` 路由废弃
- `$K\ui\home\HomeScreen.kt`：内容迁出后转薄壳或删除

**验收（专项清单）**：
- 旧 11 路由全部可达；grep `"home"` 旧路由 0 残留
- updateAllWidget / readAndParseSms 调用链未动
- 底栏/FAB 仅 4 个 Tab 根路由显示

---

## Round 11 — 列表与卡片视觉对齐

**目标**：待取/历史两 Tab 视觉与 Web 版一致（emerald 化 + 彩色图标盒）。

**任务**：
- 新建 `$K\ui\main\AppNavbar.kt`：品牌（emerald 图标盒 + 标题 + 关怀胶囊）+ 关怀开关 + 主题入口 + 视图偏好下拉（柜号排序/横纵布局/时间显隐/外观入口）
- 新建 `$K\ui\home\components\ListToolBar.kt`：时间筛选 + 排查(N) 玫红徽章（导出按钮占位，R14 接入）
- 新建 `$K\ui\components\IconBox.kt`（彩色图标盒共享件）、`$K\ui\components\RawSmsDialog.kt`（短信原文弹窗）
- 修改 `$K\ui\home\components\AddressCard.kt`：纯色大卡 + 紫图标盒 + 标签胶囊 + emerald 计数 + 一键取件 + 折叠
- 修改 `$K\ui\home\components\PickupCodeChip.kt`：emerald mono 码块 + 点击完成 + 复制 1.8s 反馈；完成态灰底删除线
- 修改 `$K\ui\home\components\SmsDataRow.kt`：靛蓝柜号徽章 + mono 时间 + 原文入口
- 修改 `ParcelList.kt` / `HorizontalParcelList.kt`（横向胶囊 Tab 条）/ `TimeFilterSheet.kt` / `$K\ui\components\EmptyState.kt`
- 删除 `HomeTopBar.kt`（职责并入 AppNavbar + ListToolBar）

**验收（专项清单）**：
- grep HomeTopBar 0 残留
- 字号全走 typography 令牌；触控目标 ≥48dp 抽查
- 4 主题下列表可读性抽查

---

## Round 12 — 设置中心 + 主题壁纸弹层

**目标**：设置页对齐 Web 9 项菜单结构（AI 项除外，R13 加入）；ThemeBgSheet 吸收壁纸调节。

**任务**：
- 重写 `$K\ui\SettingsScreen.kt`：标题卡（琥珀图标盒 + 版本徽章）+ 8 项彩色图标盒菜单（主题壁纸/规则库/地址归类/显示偏好/失败排查/日志/备份导出/关于）
- 新建 `$K\ui\settings\PreferencesScreen.kt`：4 开关（横向 Tab 视图/柜号排序/时间显隐/关怀模式）
- 新建 `$K\ui\settings\BackupScreen.kt`：整站 JSON 导出（settings+rules+mappings）
- 新建 `$K\ui\settings\ThemeBgSheet.kt`：ModalBottomSheet 双 Tab（主题卡网格 5 选项 + 壁纸控制含 brightness/contrast/fit）
- 修改 `$K\util\save_data.kt`：+exportFullBackupJson()
- `$K\ui\components\TagDialog.kt` / `ConfirmDialog.kt` 主题化；`$K\MainActivity.kt` +preferences/backup 路由
- 删除 `WallpaperAdjustSheet.kt`（功能迁入 ThemeBgSheet）

**验收（专项清单）**：
- 8 菜单项逐项可达；grep WallpaperAdjustSheet 0 残留
- SharedPreferences 存量键零变更；备份 JSON 结构正确

---

## Round 13 — AI 大模型解析模块 ✅

**目标**：AI 解析全链路上线（仅 https，零新第三方依赖）。

**任务**：
- 新建 `$K\util\ai\AiConfig.kt`（@Serializable 配置 + 默认系统提示词）、`AiProviders.kt`（14 家预设，直译 Web aiParser.ts）、`AiClient.kt`（HttpURLConnection：GET /models 8s、POST 连通测试 12s、POST 解析 10s；`\{[\s\S]*\}` 抽 JSON；http 地址拒绝）
- 新建 `$K\ui\ai\AiSettingsScreen.kt`：服务商下拉（自动填充）/ API 地址（重置）/ 密钥（显隐）/ 图片地址 / 启用开关 / 模型拉取·搜索·增删 / 连通测试（延迟+错误分类）/ 系统提示词编辑 + 4 个二级 Dialog
- 修改 `$K\util\save_data.kt`：+ai_config_json + ai_parse_cache（LRU 500）
- 修改 `$K\util\SmsProcessor.kt`：process() 增加可选缓存读-through 参数（默认空，向后兼容）
- 修改 `$K\ui\AddCustomSmsScreen.kt`：引擎状态横幅 + AI 预识别 + 结果卡 + 强行保存；失败降级正则 + warn 日志
- 修改 `$K\ui\SettingsScreen.kt`：+AI 助手配置菜单项（琥珀，状态胶囊）
- 修改 `$K\MainActivity.kt`：+ai_settings 路由；`app\src\main\AndroidManifest.xml`：+INTERNET（**不加** usesCleartextTraffic）

**验收（专项清单）**：
- HttpURLConnection 仅出现在 AiClient；无 usesCleartextTraffic
- `addLog.*[aA]piKey` 为 0；超时常量 8/10/12s
- SmsReceiver/通知服务解析路径不变（仅正则）

---

## Round 14 — Canvas 导出取件卡片 ✅

**目标**：导出 2x 高清取件卡片 PNG 并可保存相册/复制文本。

**任务**：
- 新建 `$K\util\ExportCardRenderer.kt`：Bitmap(1280, 动态高) + Canvas；布局参数对齐 Web（标题 bold、码块 190x48@2x 圆角、行高/组头比例）；4 配色（深色/翡翠/紫罗兰/明亮）
- 新建 `$K\ui\components\ExportCardDialog.kt`：范围下拉（全部/各地址）+ 配色 4 选 + 含已取勾选 + 预览 + 保存相册（MediaStore，IS_PENDING 两段提交）+ 复制文本清单
- 修改 `ListToolBar.kt`：+导出图片卡片按钮；`ParcelTabContent.kt`：弹窗状态接入

**验收（专项清单）**：
- MediaStore API 29 分支正确；无新增存储权限；Bitmap 回收
- 4 配色色值与 PRD §5.1 一致

---

## Round 15 — 终审 + 文档收官 ✅（静态）

**目标**：GOAL.md G1-G6 全过，交付用户统一构建。

**任务**：
- 终审 grep：旧符号/硬编码色/旧路由/TODO 全 0
- Web 版用户操作清单逐项对照入口存在（对照表入快照）
- PRD/ROADMAP 勾选收官；README 终版；生成 `docs/snapshots/round-15-final.md`
- 交付用户：本机 `.\gradlew.bat assembleDebug` 构建并反馈；构建问题修复走补丁快照

**验收**：GOAL.md G1-G6 全部勾选。

---

## Round 16 — 大字模式专用重排 ✅

- [x] 语义化关怀模式 Typography，移除统一 1.5 倍缩放。
- [x] AppNavbar、底部导航、工具栏、地址卡和短信行采用大字专用布局。
- [x] 长标题、标签、角标和取件码增加省略、限宽及 `99+` 保护。
- [x] 关键触控目标提升至 48/56dp。

## Round 17 — 幂等解析账本 ✅

- [x] 新增 `ParseRecord`、解析状态机和持久化账本。
- [x] 使用标准化正文 SHA-256 作为跨来源解析指纹，同文案复用结果。
- [x] 记录 AI 引擎、模型、错误、重试次数和下一次重试时间。

## Round 18 — 统一消息摄取 ✅

- [x] `ParcelMessageRepository` 统一系统短信、通知和手动录入读取。
- [x] 通知与系统短信按正文和 12 小时时间窗跨来源去重。
- [x] 自定义消息入库后直接触发新消息 AI 调度。

## Round 19 — AI 自动解析 ✅

- [x] `REGEX_ONLY` / `AI_PREFERRED` / `AI_ONLY` 三策略接入主解析链。
- [x] `autoParseSince` 只允许启用后新消息自动入队，旧短信不回灌。
- [x] WorkManager 按指纹创建唯一任务，并支持联网或仅 Wi-Fi 约束。
- [x] AI 成功结果写入账本并广播刷新主界面。

## Round 20 — 地址自动标准化归类 ✅

- [x] 用户地址映射优先级最高。
- [x] 无用户映射时执行本地标准化。
- [x] AI 建议仅在开启自动归类且置信度达到阈值时生效。
- [x] 主界面与桌面小组件统一读取排序和归类配置。

## Round 21 — 隐私可靠性与静态收官 ✅（未构建）

- [x] API Key 迁移至 Android Keystore AES/GCM；配置 JSON 与整站备份不含密钥。
- [x] 日志隐藏 API Key、Authorization、Bearer 和通知/短信正文。
- [x] 每日请求额度使用进程内原子计数，0 表示不限。
- [x] 自动失败最多尝试 3 次；认证、额度等不可恢复错误不自动重试。
- [x] 失败短信页展示 AI 状态与错误，支持单条“重新解析”。
- [x] 完成调用签名、依赖、广播、Manifest 和 Compose API 静态核对。

> 验收边界：按用户约束，R16-R21 未执行 Gradle。实际 Kotlin/Compose 编译、设备网络、系统权限、小组件、MIUI 和 Scheme 仍需统一构建与真机验收。

## Round 22 — Release/arm64 与 Room 基座 ✅（静态）

- [x] Release ABI split 只保留 `arm64-v8a`，不生成 universal APK。
- [x] 开启 R8 fullMode、资源压缩与 `zh/en` 资源配置。
- [x] ProGuard 从过宽包级 keep 收紧为模型、AI、Room 和系统组件定向 keep。
- [x] 新增 Room Entity/DAO/Database，替代 SharedPreferences 整表账本。
- [x] 新增旧账本和 AI 使用计数一次性迁移器。
- [x] `ParseLedger` / `AiUsagePolicy` 保持对外 API，内部改走 Room。
- [x] `parcel.db` 与 WAL 文件排除系统备份和设备迁移。

## Round 23 — 性能节流与 UI 打磨 ✅（静态）

- [x] Compose 失败列表改由 ViewModel 输出不可变 UI 状态，列表项不再同步查账本。
- [x] AI 请求增加全局 900ms 节流和批量入队错峰，避免瞬时并发。
- [x] AI 设置补充隐私说明，明确正文会发送到用户配置的服务商。
- [x] 失败页按钮层级弱化，重试/删除改为图标化操作，保留 48dp 触控目标。
- [x] 手动录入页 AI 配置进入页面时刷新，并保留防重复点击与额度控制。

## 里程碑

| 里程碑 | 包含轮次 | 标志 |
|--------|----------|------|
| M1 依据就绪 | R8 | 文档五件套交付 |
| M2 视觉底座 | R9 | 4 主题 + Surface 组件族 |
| M3 架构重塑 | R10-R11 | 4 Tab + 列表视觉对齐 |
| M4 体验收口 | R12 | 设置中心 + ThemeBgSheet |
| M5 功能补齐 | R13-R14 | AI + 导出卡片 |
| M6 主体静态交付 | R15 | G1-G6 静态终审，等待用户构建 |
| M7 关怀与 AI 自动化补强 | R16-R20 | 大字布局、幂等摄取、自动解析、地址归类 |
| M8 隐私可靠性收官 | R21 | 密钥、脱敏、额度、有限重试、单条重试静态完成 |
| M9 Release 与数据层基座 | R22 | arm64-only、R8 fullMode、Room 账本 |
| M10 性能与体验打磨 | R23 | 状态模型、AI 节流、隐私提示、UI 细节 |
