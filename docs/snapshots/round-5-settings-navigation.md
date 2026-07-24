# Round 5 快照：设置页与导航收口

> 日期：2026-07-24 ｜ 状态：部分通过

## 计划（改造点清单）
- [x] 新增设置页，承接 R3 暂存的低频入口
- [x] 在 MainActivity 注册 `settings` 路由
- [x] 在首页更多菜单恢复 `设置` 入口
- [x] 将老人模式偏好抽为共享工具，供 MainActivity 与设置页共用
- [x] 设置页接入主题模式、规则导入导出、通知监听、日志、身份码、壁纸选择/清除/调节、关于
- [x] 避开当前依赖中不可用的扩展 Material Icons，使用项目已有基础图标

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `app/src/main/java/com/chenran/parcel/ui/SettingsScreen.kt` | NEW | 新增分组设置页，承接低频入口与显示/数据/工具设置 |
| `app/src/main/java/com/chenran/parcel/MainActivity.kt` | MODIFY | 注册 `settings` 路由，接入主题/老人模式/规则刷新回调 |
| `app/src/main/java/com/chenran/parcel/ui/home/components/HomeTopBar.kt` | MODIFY | 首页更多菜单新增 `设置` 入口 |
| `app/src/main/java/com/chenran/parcel/util/SeniorModeManager.kt` | NEW | 抽出老人模式 SharedPreferences 读写工具 |
| `app/src/main/java/com/chenran/parcel/ui/WallpaperAdjustSheet.kt` | MODIFY | 设置页壁纸调节入口复用该弹层 |

## 构建结果
- 命令：`./gradlew.bat :app:compileDebugKotlin`
- 结果：成功，`BUILD SUCCESSFUL in 22s`
- 警告：仅有既有 deprecated/delicate API 警告，集中在 service/widget/UseNotificationScreen，非本轮新增阻断。
- 说明：`assembleDebug` 被当前执行环境跳过，未取得完整 APK 打包结果。

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 编译通过 | 部分通过 | `compileDebugKotlin` 成功；完整打包待本机复跑 |
| C2 范围控制 | 通过 | 变更集中于 R5 设置页、导航入口和老人模式状态共享 |
| C3 无 Glass/渐变新增 | 通过 | 设置页未引入 Glass 或渐变背景 |
| C4 令牌引用 | 通过 | 设置页使用 `colorScheme`、typography、`ParcelSpacing` |
| C5 触控目标 | 通过 | 设置页操作使用 ListItem、Switch、TextButton、IconButton 默认触控目标 |
| C7 无障碍 | 通过 | 返回图标有 `返回` contentDescription；装饰性列表图标 contentDescription 为 null |
| R5 专项 | 部分通过 | 设置页上线、路由和首页入口完成；导航转场统一时长尚未专项改造 |

## 原主页菜单归属记录
| 项目 | 归属 |
|------|------|
| 刷新、排序、筛选、布局、显示开关、规则列表 | 保留在主页高频入口 |
| 主题模式、老人模式 | 迁移到设置页 `显示` |
| 导入/导出规则 | 迁移到设置页 `数据` |
| 通知监听、日志、身份码、关于 | 迁移到设置页 `工具` |
| 壁纸选择、清除、调节 | 迁移到设置页 `壁纸` |

## 遗留问题 / 风险
- ROADMAP R5 的导航转场时长统一尚未单独实现；当前以功能入口收口为主。
- 设置页当前文件位于既有 `ui/` 平铺结构下，而非新建 `ui/settings/` 包，符合项目现有 Screen 文件布局。
- `assembleDebug` 仍需本机复跑确认完整 APK 打包。

## 回滚要点
- 删除 `SettingsScreen.kt` 和 `SeniorModeManager.kt`。
- 移除 MainActivity 中 `settings` 路由与 `SeniorModeManager` 引用，恢复原老人模式私有读写函数。
- 删除 HomeTopBar 中 `设置` 菜单项。
