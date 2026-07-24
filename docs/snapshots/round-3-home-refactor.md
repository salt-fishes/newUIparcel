# Round 3 快照：主页重构（拆分 + 菜单精简）

> 日期：2026-07-23 ｜ 状态：通过（构建验证由用户取消，以静态检查替代）

## 计划（改造点清单）
- [x] code-explorer 勘察 HomeScreen（1277 行）：组件树、状态点、18 项菜单调用链
- [x] 拆分为 `ui/home/` 包：壳 + 7 个组件 + HomePrefs
- [x] 溢出菜单 18 → 6 项（仅高频）
- [x] 主页 M3 化：TopAppBar 标准色、FAB 替代悬浮圆钮、EmptyState、TabRow 选中态、48dp 触控修正

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| ui/HomeScreen.kt（旧，1277 行） | DELETE | 全部职责迁出 |
| ui/home/HomeScreen.kt（174 行） | NEW | 壳：状态持有 + Scaffold + FAB + 时间筛选弹层；签名收敛（移除 onSeniorModeChanged/onWallpaperChanged/themeMode/onThemeModeChanged） |
| ui/home/HomePrefs.kt（60 行） | NEW | show_completed_codes / show_code_time / horizontal_layout 读写（从旧文件 private 函数迁出） |
| ui/home/components/HomeTopBar.kt（198 行） | NEW | 标准 TopAppBar + AssistChip 时间筛选 + 成功/失败计数徽标（语义色 + 暗色变体）+ 6 项菜单 |
| ui/home/components/TimeFilterSheet.kt（92 行） | NEW | 标准 BottomSheet：48dp 行 + primary 选中 + Check 图标 |
| ui/home/components/PickupCodeChip.kt（106 行） | NEW | primary 强调取件码；已取件降级 surfaceVariant + 删除线；去除白色描边 |
| ui/home/components/AddressCard.kt（262 行） | NEW | 地址分组卡片；标签/全取按钮外层 48dp 触控 Box（原 24/36dp 违规）；完整短信改标准 AlertDialog；移除双层 padding |
| ui/home/components/SmsDataRow.kt（149 行） | NEW | 原文摘要/取件码双形态；surfaceVariant 容器替代白色半透明 |
| ui/home/components/HorizontalParcelList.kt（121 行） | NEW | TabRow 标准 surface/primary 选中（原白色药丸）；移除未用的 navController/sortByLocker 参数 |
| ui/home/components/ParcelList.kt（119 行） | NEW | 空状态用 EmptyState + Button；FAB 上移至 Scaffold；移除未用参数 |
| util/IdentityEntries.kt | NEW | 淘宝/拼多多身份码入口（原 private，改 public 供 R5 设置页使用） |
| MainActivity.kt | MODIFY | HomeScreen 导入与调用适配新签名；移除 BackgroundManager 导入 |
| docs/snapshots/round-3-home-refactor.md | NEW | 本快照 |

## 菜单项归属核对（原 18 项全覆盖）
| 原序号 | 菜单项 | 归属 |
|--------|--------|------|
| 0 按柜号排序 | 保留主页菜单 |
| 1 横向/纵向地址 | 保留主页菜单 |
| 2 显示/隐藏已取件 | 保留主页菜单 |
| 3 显示/隐藏时间 | 保留主页菜单 |
| 4 添加自定义取件短信 | 已由 FAB + 空状态按钮覆盖（菜单去重） |
| 5 地址归类 | 保留主页菜单 |
| 6 规则列表 | 保留主页菜单 |
| 7 导出规则 | → R5 设置页（代码暂存本快照附录 A） |
| 8 导入规则 | → R5 设置页（附录 A） |
| 9 查看日志 | → R5 设置页（路由 logs 已存在） |
| 10 监听第三方app通知 | → R5 设置页（路由 use_notification 已存在） |
| 11 淘宝身份码 | → R5 设置页（util/IdentityEntries.kt） |
| 12 拼多多身份码 | → R5 设置页（util/IdentityEntries.kt） |
| 13 老人模式开关 | → R5 设置页（MainActivity.saveSeniorMode 保留） |
| 14 主题模式 | → R5 设置页（ThemeManager 保留；MainActivity App 内 themeMode 状态保留） |
| 15/16 设置/清除背景图 | → R6 壁纸回迁时入设置页（附录 A 选图 launcher） |
| 17 关于 | → R5 设置页（路由 about 已存在） |

## 构建结果
- 未执行 gradle（用户取消）；read_lints 全包 0 错误；旧组件无外部引用（已全局搜索验证）

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| R3 专项：壳 ≤300 行 | ✅ | 174 行；组件 92-262 行各司一职 |
| R3 专项：菜单 ≤6 项 | ✅ | 恰好 6 项，18 项原菜单归属全覆盖 |
| C4 令牌引用 | ✅ | 间距 ParcelSpacing；颜色 colorScheme/语义令牌 |
| C5 触控目标 | ✅ | 标签/全取/筛选行均 ≥48dp（原 24/36dp 违规已修） |
| C7 无障碍 | ✅ | FAB/菜单/计数徽标/全取均有 contentDescription |
| C3 无 Glass/渐变新增 | ✅ | |
| C9 功能回归 | ⏳ | 待构建确认：状态流、排序、横竖布局、标签对话框、标记取件、时间筛选路径逻辑未变 |

## 遗留问题 / 风险
- R3→R5 期间：导出/导入规则、日志、通知监听、身份码、老人模式、主题、壁纸、关于**暂无 UI 入口**（功能代码保留，R5 设置页恢复，见附录 A）
- WallpaperAdjustSheet 暂无任何调用方，R6 回迁
- HorizontalParcelList 的 TabRow divider 置空（沿用原样式），R7 终审可再评估

## 附录 A：暂存代码（R5 设置页重建入口时复用）

### A.1 导入/导出规则 launcher（原旧 HomeScreen 168-197 行）
```kotlin
val importRulesLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    if (uri != null) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
            importRulesFromJson(context, viewModel, json)
            (context as MainActivity).readAndParseSms()
            addLog(context, "规则导入成功")
        } catch (e: Exception) {
            addLog(context, "规则导入失败: ${e.message}")
        }
    }
}

val exportRulesLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
) { uri: Uri? ->
    if (uri != null) {
        try {
            val json = exportRulesToJson(context)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(json.toByteArray(Charsets.UTF_8))
            }
            addLog(context, "规则导出成功")
        } catch (e: Exception) {
            addLog(context, "规则导出失败: ${e.message}")
        }
    }
}
// 依赖 import：com.chenran.parcel.util.importRulesFromJson / exportRulesToJson / addLog
```

### A.2 壁纸选择 launcher（原旧 HomeScreen 153-166 行，R6 复用）
```kotlin
val wallpaperPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
) { uri: Uri? ->
    if (uri != null) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: Exception) {
        }
        tempWallpaperUri = uri.toString()
        showWallpaperAdjust = true
    }
}
```

## 回滚要点
- 恢复旧 ui/HomeScreen.kt（1277 行版本）；删除 ui/home/ 包与 util/IdentityEntries.kt；MainActivity 还原 HomeScreen 导入与完整参数调用
