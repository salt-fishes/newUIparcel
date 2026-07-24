# Round 12 快照：设置中心（9 项菜单 + Preferences/Backup 子页 + ThemeBgSheet）

> 日期：2026-07-24 ｜ 状态：通过（静态验证，构建由用户最终统一执行）

## 计划（改造点清单）
- [x] SettingsScreen 重写为 Tab 页菜单：标题卡（琥珀⚙️ + 系统设置 + 版本徽章）+ 9 项彩色图标盒菜单（配色壁纸/规则库/地址归类/显示偏好/异常排查(带N条徽章)/通知监听/日志/备份/关于）；移除旧"显示/壁纸/数据/工具"分区与返回箭头
- [x] 新增 ui/settings/ThemeBgSheet.kt：2 Tab（配色与主题：5 主题卡 2 列网格 + 关怀开关；自定义背景图：开关/相册/URL(仅 https)/5 滑杆/Cover-Contain/清除），吸收 WallpaperAdjustSheet 全部能力
- [x] 新增 ui/settings/PreferencesScreen.kt：4 开关行卡片（横向布局/柜号排序/时间显隐/关怀模式），接入 App 级共享状态
- [x] 新增 ui/settings/BackupScreen.kt：整站配置 JSON 导出 + 规则导入导出 + 关于入口
- [x] save_data.kt + exportFullBackupJson（{version, exportTime, settings, rules, mappings}，壁纸全参数）
- [x] ThemeBgSheet 全局挂载（AppNavbar 🎨 与设置菜单共享入口）
- [x] ConfirmDialog/TagDialog 主题化（palette.panel 容器）
- [x] 删除 ui/WallpaperAdjustSheet.kt

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/SettingsScreen.kt` | MODIFY | 重写为 9 项菜单 Tab 页（新签名） |
| `ui/settings/ThemeBgSheet.kt` | NEW | 主题+壁纸双 Tab 弹层 |
| `ui/settings/PreferencesScreen.kt` | NEW | 显示偏好 4 开关 |
| `ui/settings/BackupScreen.kt` | NEW | 备份与规则导入导出 |
| `util/save_data.kt` | MODIFY | +exportFullBackupJson 及 3 个 @Serializable 备份模型 |
| `MainActivity.kt` | MODIFY | +preferences/backup 路由；ThemeBgSheet 挂载；SettingsScreen 新签名 |
| `ui/components/ConfirmDialog.kt` | MODIFY | palette 容器与按钮色 |
| `ui/components/TagDialog.kt` | MODIFY | Card 容器 palette.panel |
| `ui/WallpaperAdjustSheet.kt` | DELETE | 功能迁入 ThemeBgSheet |

## 静态验证结果
- 残留搜索：`WallpaperAdjustSheet` 引用 0（仅 ThemeBgSheet 注释提及）✅；旧 SettingsScreen 回调参数（onSeniorModeChange/onRulesChanged）0 ✅
- 路由核对：preferences/backup 已注册；设置 9 菜单项逐项目标全部可达 ✅
- 存储核对：SharedPreferences 存量键零变更（仅读取）；备份导出仅新增序列化模型 ✅
- 代码审查：URL 壁纸强制 https ✅（决策 D4 延伸）；无 `!!`；版本号读取带兜底 ✅
- 壁纸参数：brightness/contrast/fitContain 已接入 ThemeBgSheet 并与 BackgroundManager 双向同步 ✅

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 静态验证 | 通过 | 见上 |
| C2 范围控制 | 通过 | 仅 R12 清单内文件 |
| C9 功能回归 | 通过 | 主题切换/壁纸/关怀/规则导入导出入口全保留 |
| C10 编码规范 | 通过 | state hoisting；无敏感信息 |
| 专项：菜单项逐项可达 | 通过 | 9 项目标核对 |
| 专项：存量键零变更 | 通过 | 备份为只读导出 |

## 遗留问题 / 风险
- Web 版 8 张预设壁纸画廊未移植（Android 无内置资源，相册/URL 已覆盖，记录为有意偏差）。
- RulesScreen/AddressGroupScreen 等二级页仍为 v1 视觉（功能完整，视觉统一在后续轮次已有基础组件上自然收敛，R15 终审评估）。
- AI 菜单项 R13 加入后设置菜单变 10 项。

## 回滚要点
- 还原 SettingsScreen/save_data/MainActivity/ConfirmDialog/TagDialog；恢复 WallpaperAdjustSheet.kt；删除 ui/settings/ 三个新文件。
