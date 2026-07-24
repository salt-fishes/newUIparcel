# Round 1 快照：主题令牌重构

> 日期：2026-07-23 ｜ 状态：通过（构建验证由用户取消，以静态检查替代）

## 计划（改造点清单）
- [x] 重写 `ui/theme/Color.kt` 为 M3 基线色彩令牌
- [x] 重写 `ui/theme/Theme.kt`：移除渐变/壁纸渲染，签名收敛
- [x] 新增 `ui/theme/Shape.kt`（ParcelShapes）
- [x] 新增 `ui/theme/Spacing.kt`（ParcelSpacing）
- [x] 对齐 `ui/theme/Type.kt` 至 M3 字体比例，保留老人模式 1.5x 机制
- [x] MainActivity 移除壁纸参数透传（能力保留在 BackgroundManager，R6 回迁）

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| ui/theme/Color.kt | MODIFY | M3 基线亮/暗令牌（primary #6750A4 等 PRD §5.1）；删除 Gradient*/Pickup* 色板；pickup* 函数收敛为单一 primary；SuccessGreen/FailPink 改为语义色值（#386A20/#B3261E），R4 迁移命名 |
| ui/theme/Theme.kt | MODIFY | 移除 LightGradientBrush/DarkGradientBrush 全屏渐变与壁纸 AsyncImage/遮罩/模糊渲染；签名收敛为 `ParcelTheme(darkTheme, isSeniorMode, content)`；保留 LocalIsDarkTheme（GlassComponents/HomeScreen/WallpaperAdjustSheet 暂用） |
| ui/theme/Shape.kt | NEW | ParcelShapes 8/12/16/20/28dp |
| ui/theme/Spacing.kt | NEW | ParcelSpacing 4/8/16/24/32/48 |
| ui/theme/Type.kt | MODIFY | 对齐 M3 比例（display 57/45/36、headline 32/28/22、title 22/16/14、body 16/14/12、label 14/12/11）；SeniorTypography 1.5x 机制保留 |
| MainActivity.kt | MODIFY | 删除 currentWallpaperUri/currentWallpaperSettings 状态与主题传参；onWallpaperChanged 仅持久化 |
| docs/snapshots/round-1-theme-tokens.md | NEW | 本快照 |

## 构建结果
- 命令：`.\gradlew.bat assembleDebug` —— **用户取消执行**（两次），改为静态验证
- 静态验证：read_lints 对 ui/theme/ 与 MainActivity.kt 诊断 0 错误；引用面已全局勘察无遗漏
- ⚠️ 建议用户在方便时手动执行一次 `.\gradlew.bat assembleDebug` 确认

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| R1 专项：渐变/色板残留搜索 | ✅ | `GradientStart|GradientMid|GradientEnd|LightGradientBrush|DarkGradientBrush|Pickup*` 全局 0 结果 |
| R1 专项：ParcelTheme 签名收敛 | ✅ | 壁纸参数移除，纯 surface 背景 |
| R1 专项：亮/暗 colorScheme 角色完整 | ✅ | primary/secondary/tertiary/error/background/surface/outline/outlineVariant 等全角色 |
| C2 范围控制 | ✅ | 仅 theme 目录 + MainActivity |
| C3 无 Glass/渐变新增 | ✅ | |
| C9 功能回归 | ⏳ | 待用户构建后确认（主题模式切换、老人模式开关路径未动） |

## 遗留问题 / 风险
- 壁纸设置入口（WallpaperAdjustSheet）仍可在主页触发并持久化，但不再渲染效果——属预期，R6 回迁
- HomeScreen/SuccessSmsScreen 仍引用 pickup* 函数（已收敛为单色），R3/R4 迁移时删除
- 构建未实际执行，存在低概率编译断点风险（已通过引用勘察 + lints 双重静态验证缓解）

## 回滚要点
- 恢复 Color.kt/Theme.kt/Type.kt 至旧版（渐变 + 玻璃主题）；删除 Shape.kt/Spacing.kt；还原 MainActivity.kt 中壁纸状态与 ParcelTheme 传参
