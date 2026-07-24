# Round 11 快照：列表与卡片视觉对齐（emerald 化 + 图标盒 + 全局 AppNavbar）

> 日期：2026-07-24 ｜ 状态：通过（静态验证，构建由用户最终统一执行）

## 计划（改造点清单）
- [x] 新增 ui/main/AppNavbar.kt：品牌（emerald 图标盒📦+标题+关怀徽章）+ 关怀开关 + 主题入口（🎨，暂跳设置 Tab，R12 换 ThemeBgSheet）+ 视图偏好下拉（排序/布局/时间/外观）
- [x] 新增 ui/home/components/ListToolBar.kt：时间筛选（日历 emerald）+ 成功/排查计数徽章
- [x] 新增 ui/components/IconBox.kt：彩色图标盒共享件（badgeBg/badgeBorder + 16dp 圆角）
- [x] 新增 ui/components/RawSmsDialog.kt：原文弹窗（mono 滚动文本 + emerald 关闭）
- [x] AddressCard 重写：紫图标盒 + 标签胶囊（未设显示"设标签"）+ "N 件待取"emerald/"全部已取完" + 一键取件徽章 + 折叠箭头
- [x] PickupCodeChip 重写：emerald 徽章底 + mono Black 20/24sp 令牌 + 📋复制（LocalAppToast 反馈）
- [x] SmsDataRow 重写：16dp 芯片行 + 靛蓝柜号徽章 + mono 时间（M/d HH:mm）+ 原文入口
- [x] HorizontalParcelList 重写：横向胶囊 Tab 条（选中实心 emerald，"地址 (N)"）
- [x] TimeFilterSheet/EmptyState 主题化（palette + 选中 emerald 徽章底 + 空态大图标盒）
- [x] 视图偏好（排序/布局/时间显隐）上提 App 级，AppNavbar 与列表共享
- [x] 删除 HomeTopBar.kt；清理 Color.kt 旧语义色令牌（SuccessGreen/FailPink/WarningAmber，已零引用）

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/main/AppNavbar.kt` | NEW | 全局顶部导航栏 |
| `ui/main/MainScaffold.kt` | MODIFY | +topBar 槽位（仅 Tab 根显示） |
| `ui/home/components/ListToolBar.kt` | NEW | 列表工具条 |
| `ui/components/IconBox.kt` | NEW | 彩色图标盒 |
| `ui/components/RawSmsDialog.kt` | NEW | 原文弹窗 |
| `ui/components/EmptyState.kt` | MODIFY | palette + 可选 emoji 图标盒（签名兼容） |
| `ui/home/components/AddressCard.kt` | MODIFY | Web ParcelCard 结构重写 |
| `ui/home/components/PickupCodeChip.kt` | MODIFY | emerald 码块 + 复制 |
| `ui/home/components/SmsDataRow.kt` | MODIFY | 靛蓝徽章 + mono 时间 |
| `ui/home/components/HorizontalParcelList.kt` | MODIFY | 胶囊 Tab 条 |
| `ui/home/components/TimeFilterSheet.kt` | MODIFY | palette + 选中徽章底 |
| `ui/home/ParcelTabContent.kt` | MODIFY | HomeTopBar→ListToolBar；接收上提偏好 |
| `ui/home/PendingTab.kt` / `ArchiveTab.kt` | MODIFY | 透传视图偏好 |
| `MainActivity.kt` | MODIFY | 偏好状态上提 + AppNavbar 接入 |
| `ui/home/components/HomeTopBar.kt` | DELETE | 职责并入 AppNavbar + ListToolBar |
| `ui/theme/Color.kt` | MODIFY | 删旧语义色令牌（零引用） |

## 静态验证结果
- 残留搜索：`HomeTopBar` 0 ✅；`SuccessGreen|FailPink|WarningAmber` 引用 0（定义已删）✅；`Color(0x` 新增 0（仅主题文件）✅
- 签名核对：EmptyState 新增 emoji 参数带默认值，旧调用方兼容 ✅；AddressCard/ParcelList 签名变更已同步全部调用点（ParcelList/HorizontalParcelList）✅
- 路由核对：无路由增删；success_sms/fail_sms 入口由 ListToolBar 徽章承接 ✅
- 代码审查：字号走 typography/取件码令牌（关怀 1.5x 不失效）✅；复制走 ClipboardManager + 全局 Toast ✅；无 `!!` ✅
- 触控抽查：取件码芯片 52dp（关怀 56）✅；原文按钮 36/48dp ✅；一键取件/标签胶囊内边距满足 40dp+ ✅

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 静态验证 | 通过 | 见上 |
| C2 范围控制 | 通过 | 仅 R11 清单内文件 |
| C4 令牌引用 | 通过 | 颜色全走 palette/colorScheme；取件码字号走令牌 |
| C5 触控目标 | 通过 | 抽查见上 |
| C7 无障碍 | 通过 | 新增 IconButton 均有 contentDescription（描述动作） |
| C9 功能回归 | 通过 | 完成/批量/标签/复制/筛选/排序/布局切换链路未变 |
| 专项：老人模式字号 | 通过 | 无硬编码 sp；令牌化 |
| 专项：4 主题可读 | 待真机 | 浅色主题文字深化变体已配置（palette） |

## 遗留问题 / 风险
- AppNavbar 主题按钮暂跳设置 Tab（R12 改为 ThemeBgSheet 弹层）。
- 二级页面（Add/Rules/Logs/About 等）仍为 v1 视觉（R12 对齐设置中心时统一处理）。
- Tab 条胶囊长地址可能过宽（已用 LazyRow 横向滚动兜底）。

## 回滚要点
- 还原 AddressCard/PickupCodeChip/SmsDataRow/HorizontalParcelList/TimeFilterSheet/ParcelTabContent/PendingTab/ArchiveTab/MainActivity/MainScaffold/EmptyState/Color.kt；恢复 HomeTopBar.kt；删除 AppNavbar/ListToolBar/IconBox/RawSmsDialog。
