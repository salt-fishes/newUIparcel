# Round 9 快照：视觉 tokens（4 主题 + ParcelPalette + 纯色 Surface 组件族）

> 日期：2026-07-24 ｜ 状态：通过（静态验证，构建由用户最终统一执行）

## 计划（改造点清单）
- [x] Color.kt：清理旧 M3 紫令牌（无外部引用），新增 v2.0 Web 对齐令牌（emerald/slate/语义强调色/4 主题面板色）
- [x] 新增 ParcelPalette.kt：语义调色板（面板/卡片/芯片/边框/文字三级/8 强调色/FAB 渐变），4 主题各一份
- [x] Theme.kt：`ParcelTheme(themeMode, isSeniorMode)` 新签名；4 套 ColorScheme；LocalParcelPalette Provider；跟随系统解析
- [x] ThemeManager：+MODE_EMERALD=3 / MODE_VIOLET=4（存量 0/1/2 兼容），标签对齐 Web 主题名
- [x] 新增 Surface.kt：ThemedPanel/ThemedCard/ThemedChip 纯色面板族（决策 D1，无玻璃）
- [x] ParcelColors.kt：签名不变，实现改读 ParcelPalette（调用方零改动）
- [x] ParcelContainers.kt：签名不变，实现改挂 Surface.kt 面板族
- [x] 新增 ThemeBackdrop.kt：4 主题对角渐变背景层
- [x] 壁纸三参：WallpaperSettings +brightness/contrast/fitContain；BackgroundManager +3 键（默认值兜底+coerce）；WallpaperBackdrop ColorMatrix 亮度/对比度 + Cover/Contain
- [x] Shape.kt：large 20→24dp（对齐 Web rounded-3xl）
- [x] Type.kt：取件码 mono/Black 令牌（20sp/关怀 24sp）
- [x] MainActivity：themeMode 直传 ParcelTheme；挂 ThemeBackdrop；HomeScreen/SettingsScreen Scaffold 透明透出渐变
- [x] SettingsScreen：主题 Chips 扩为 5 项（跟随/明亮/深色/翡翠/紫罗兰）

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/theme/Color.kt` | MODIFY | 删旧 M3 紫亮暗令牌（86 行，无引用）；+v2.0 令牌 |
| `ui/theme/ParcelPalette.kt` | NEW | 语义调色板 + 4 主题实例 + Local |
| `ui/theme/Theme.kt` | MODIFY | 新签名 + 4 ColorScheme + Provider |
| `ui/theme/Shape.kt` | MODIFY | large=24dp |
| `ui/theme/Type.kt` | MODIFY | +取件码字体令牌 |
| `ui/components/Surface.kt` | NEW | ThemedPanel/ThemedCard/ThemedChip |
| `ui/components/ThemeBackdrop.kt` | NEW | 主题渐变背景层 |
| `ui/components/ParcelColors.kt` | MODIFY | 改读 palette，签名不变 |
| `ui/components/ParcelContainers.kt` | MODIFY | 改挂 Surface.kt，签名不变 |
| `ui/components/WallpaperBackdrop.kt` | MODIFY | 亮度/对比度 ColorMatrix + fit |
| `util/ThemeManager.kt` | MODIFY | +2 模式 + 标签 |
| `util/BackgroundManager.kt` | MODIFY | +brightness/contrast/fitContain |
| `MainActivity.kt` | MODIFY | themeMode 直传 + ThemeBackdrop 层 |
| `ui/home/HomeScreen.kt` | MODIFY | Scaffold 恒透明 |
| `ui/SettingsScreen.kt` | MODIFY | Scaffold 透明 + 主题 5 Chips |

## 静态验证结果
- 残留搜索：`ui/` 下 `Color(0x` 仅存在于 `ui/theme/`（Color/ParcelPalette/Theme）✅；`darkTheme =` 旧调用 0 ✅；旧 M3 令牌外部引用 0（清理后）✅
- 签名核对：ParcelColors 6 函数签名 diff 为空 ✅；ParcelContainers 3 组件签名 diff 为空 ✅
- 路由核对：本轮无路由改动，N/A
- 代码审查：无 `!!` 新增；WallpaperSettings 新字段带默认值，WallpaperAdjustSheet 命名参数构造不受影响 ✅；`LocalIsDarkTheme` Provider 保留 ✅
- 已知告警（不阻断）：HomeScreen `wallpaperActive` 参数暂未使用（R10 重构时消化）；SettingsScreen 旧 AssistChip 样式（R12 重写）

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 静态验证 | 通过 | 见上 |
| C2 范围控制 | 通过 | 仅 R9 清单内文件 |
| C3 无越界视觉 | 通过 | 无玻璃/模糊面板；渐变仅主题背景层 |
| C4 令牌引用 | 通过 | 颜色全走 colorScheme/ParcelPalette |
| C9 功能回归 | 通过 | 数据层/解析器/小组件零改动；主题模式存量键兼容（0/1/2 原语义不变） |
| C10 编码规范 | 通过 | 无 `!!`、无保留字命名 |
| 专项：4 ColorScheme + 跟随系统 | 通过 | Theme.kt when 分支齐全 |
| 专项：存量键兼容 | 通过 | theme_mode 0/1/2 语义未变；壁纸新参默认值兜底 |

## 遗留问题 / 风险
- 浅色主题对比度：浅色卡片纯白 + 浅色渐变页底，对比依赖卡片边框与文字深化变体，R11 列表改造时重点抽查。
- HomeTopBar/AddressCard 仍引用 SuccessGreen/FailPink 旧语义色（R11 收敛为 palette 徽章色）。
- 二级页面（Add/Rules/Logs 等）仍为旧视觉，R11/R12 逐页对齐（过渡态可接受）。

## 回滚要点
- 还原 Color/Theme/Shape/Type/ParcelColors/ParcelContainers/WallpaperBackdrop/ThemeManager/BackgroundManager/MainActivity/HomeScreen/SettingsScreen 至 v1 内容；删除 ParcelPalette.kt / Surface.kt / ThemeBackdrop.kt。
