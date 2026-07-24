# Round 6 快照：特色功能回迁

> 日期：2026-07-24 ｜ 状态：通过（构建由用户本机执行）

## 计划（改造点清单）
- [x] 老人模式：首页提示弹窗去除硬编码 32/26sp，改用 MaterialTheme.typography
- [x] 老人模式：触控目标提升至 ≥56dp（标签入口、一键全取、计数徽标、取件码胶囊、原文按钮）
- [x] 壁纸：BackgroundManager 新增启用开关（默认关闭）；设置页新增"主页壁纸"开关
- [x] 壁纸：新增 WallpaperBackdrop（低透明度呈现 + 自动压暗双层遮罩，scrim alpha 下限 0.55 保证对比度）
- [x] 壁纸：主页 Scaffold 透明透出壁纸层，其余界面保持不透明背景
- [x] 取件码颜色：定夺为纯 primary 单色方案，删除 pickupCode* 兼容函数，组件直引 colorScheme
- [x] 深色打磨：补齐 M3 surface 层次令牌（surfaceDim/Bright/Container 五级）并接入亮暗 colorScheme

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/theme/Color.kt` | MODIFY | 新增 surface 层次令牌 14 个；删除 pickupCode* 兼容函数 |
| `ui/theme/Theme.kt` | MODIFY | 亮/暗 colorScheme 接入 surfaceContainer 系列令牌 |
| `ui/components/WallpaperBackdrop.kt` | NEW | 主页壁纸背景层：模糊/偏移 + 黑色压暗 + 背景色 scrim |
| `util/BackgroundManager.kt` | MODIFY | 新增 `wp_enabled` 开关（默认 false）；选图自动启用、清除自动关闭 |
| `ui/SettingsScreen.kt` | MODIFY | 壁纸分组新增"主页壁纸"开关；选图/清除与开关状态联动 |
| `ui/home/HomeScreen.kt` | MODIFY | 新增 `wallpaperActive` 参数，开启时 Scaffold 透明 |
| `MainActivity.kt` | MODIFY | App 层渲染 WallpaperBackdrop（remember(wallpaperRevision)）；老人模式弹窗 typography 化 |
| `ui/home/components/PickupCodeChip.kt` | MODIFY | 纯 primary 单色（primary 文字 + primaryContainer 底）；移除 isDarkTheme 参数；触控 ≥48/56dp |
| `ui/home/components/SmsDataRow.kt` | MODIFY | 同步 PickupCodeChip 签名；"原文"按钮触控 ≥48/56dp |
| `ui/home/components/AddressCard.kt` | MODIFY | 标签/一键全取触控目标老人模式 56dp |
| `ui/home/components/HomeTopBar.kt` | MODIFY | 计数徽标触控 ≥48/56dp 且内容居中 |
| `ui/SuccessSmsScreen.kt` | MODIFY | 取件码颜色改用 colorScheme.primary，删除 pickupCodeColor 引用 |

## 构建结果
- 命令：`.\gradlew.bat assembleDebug`（由用户本机执行，本轮按约定跳过）
- 静态验证：read_lints 全部修改文件 0 错误；全局搜索 `pickupCodeColor|pickupCodeBackgroundColor` 无残留

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C2 范围控制 | 通过 | 仅改 R6 范围：老人模式/壁纸/取件码色/surface 层次 |
| C3 无 Glass/渐变新增 | 通过 | 壁纸为 AsyncImage + 纯色遮罩，无渐变 Brush |
| C4 令牌引用 | 通过 | 新增颜色全部来自 colorScheme；弹窗字号改 typography |
| C5 触控目标 | 通过 | 修改点默认 48dp、老人模式 56dp |
| C7 无障碍 | 通过 | 壁纸背景 contentDescription = null（纯装饰） |
| C8 深色模式 | 通过 | surface 层次令牌亮暗双套；壁纸 scrim 随主题背景色 |
| R6 专项 | 通过 | 老人模式 1.5x 生效且触控 ≥56dp；壁纸默认关闭；取件码收敛为单色；深色打磨完成 |

## 遗留问题 / 风险
- 壁纸对比度依赖 scrim alpha 下限 0.55 的静态保证，极端高亮壁纸建议真机抽查。
- SuccessGreen/FailPink 作为业务语义色保留（PRD §5.1 允许 success/error 语义色）。
- `assembleDebug` 完整打包由用户本机复跑确认。

## 回滚要点
- 删除 `ui/components/WallpaperBackdrop.kt`；BackgroundManager 移除 `wp_enabled` 相关方法。
- SettingsScreen 移除壁纸开关项；HomeScreen 移除 `wallpaperActive`；MainActivity 移除壁纸层渲染。
- Color.kt 恢复 pickupCode* 函数，PickupCodeChip/SuccessSmsScreen 回退引用。
- Theme.kt 移除 surfaceContainer 系列参数（回退默认计算值）。
