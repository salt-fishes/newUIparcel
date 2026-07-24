# Round 4 快照：次级界面迁移

> 日期：2026-07-24 ｜ 状态：部分通过

## 计划（改造点清单）
- [x] 勘察次级界面与旧视觉残留：TopAppBarDefaults、Color.White/Red、SuccessGreen/FailPink、Color.Transparent、Glass/gradient 相关引用
- [x] 迁移 AddressGroupScreen、RulesScreen、SuccessSmsScreen、AddCustomSmsScreen、FailSmsScreen 的旧透明顶栏与硬编码颜色
- [x] 将 WallpaperAdjustSheet 从深色硬编码底部弹层改为 Material colorScheme 驱动
- [x] 修复本轮暴露出的编译阻断项：Gradle wrapper checksum、缺少 key.properties 时 debug 构建失败、缺少 local.properties SDK 路径

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `app/src/main/java/com/chenran/parcel/ui/AddressGroupScreen.kt` | MODIFY | 顶栏、分组头、标签和危险操作改为 M3 语义色 |
| `app/src/main/java/com/chenran/parcel/ui/RulesScreen.kt` | MODIFY | 透明顶栏/FAB 迁移为主题化 M3 样式 |
| `app/src/main/java/com/chenran/parcel/ui/SuccessSmsScreen.kt` | MODIFY | 顶栏与地址色迁移到 colorScheme |
| `app/src/main/java/com/chenran/parcel/ui/AddCustomSmsScreen.kt` | MODIFY | 清理旧色导入，预览文本改为主题主色 |
| `app/src/main/java/com/chenran/parcel/ui/FailSmsScreen.kt` | MODIFY | 清理旧失败色和无效导入 |
| `app/src/main/java/com/chenran/parcel/ui/WallpaperAdjustSheet.kt` | MODIFY | 底部弹层、Slider、按钮迁移为主题化样式，并修复被截断的 SliderRow |
| `gradle/wrapper/gradle-wrapper.properties` | MODIFY | 修正 Gradle 8.13 官方 binary-only 发行包 SHA-256 |
| `app/build.gradle.kts` | MODIFY | 缺少本地签名文件时不再阻塞 debug 编译 |
| `local.properties` | NEW | 配置本机 Android SDK 路径用于构建验证 |

## 构建结果
- 命令：`./gradlew.bat :app:compileDebugKotlin`
- 结果：成功，`BUILD SUCCESSFUL in 22s`
- 说明：SOP 推荐的 `assembleDebug` 在当前执行环境中被跳过，未得到完整打包结果；Kotlin/资源处理链路已通过到 `compileDebugKotlin`。

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 编译通过 | 部分通过 | `compileDebugKotlin` 成功；`assembleDebug` 未能由执行环境运行 |
| C2 范围控制 | 通过 | 变更集中在 R4 次级页、构建阻断配置与必要本机配置 |
| C3 无 Glass/渐变新增 | 通过 | 未新增 Glass/渐变背景；残留搜索仅剩主页业务状态色令牌定义/使用 |
| C4 令牌引用 | 通过 | 新改 UI 颜色使用 `MaterialTheme.colorScheme`，字体使用 typography |
| C5 触控目标 | 通过 | 保留 Material Button/IconButton/Switch 默认触控目标 |
| C7 无障碍 | 通过 | 新改 IconButton 均保留 contentDescription |
| R4 专项 | 部分通过 | 主要次级页和 WallpaperAdjustSheet 已迁移；完整 APK 打包待本机手动执行 `assembleDebug` 确认 |

## 遗留问题 / 风险
- `HomeTopBar.kt`、`AddressCard.kt` 仍使用 `SuccessGreen/FailPink` 作为业务状态色，按 ROADMAP 属于 R6 取件码/状态色收敛范围。
- `assembleDebug` 被执行环境跳过，需要在本机 PowerShell 手动复跑。

## 回滚要点
- 恢复上述 UI 文件中的顶栏/颜色修改。
- 恢复 `gradle-wrapper.properties` 的 checksum 和 `app/build.gradle.kts` 的签名配置逻辑。
- 删除 `local.properties`（如不需要本机 SDK 配置）。
