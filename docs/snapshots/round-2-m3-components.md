# Round 2 快照：M3 组件层

> 日期：2026-07-23 ｜ 状态：通过（构建验证由用户取消，以静态检查替代）

## 计划（改造点清单）
- [x] 新建 `ui/components/` 设计系统层：ParcelCard / ParcelSurface / ParcelChip / 取色函数 / SectionHeader / EmptyState / ConfirmDialog
- [x] 全项目替换 10 个界面文件中的全部 Glass* 引用（84 处）
- [x] 删除 `ui/theme/GlassComponents.kt`

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| ui/components/ParcelContainers.kt | NEW | ParcelCard（surfaceContainerLow + 1dp 海拔，内边距沿用 16h/14v）、ParcelSurface（surfaceVariant 默认，borderAlpha>0 时用 outline 细描边）、ParcelChip |
| ui/components/ParcelColors.kt | NEW | parcelBaseColor/parcelOnCardColor/parcelOnCardVariantColor/parcelSheetColor/parcelButtonColor/parcelChipColor → 全部映射 colorScheme |
| ui/components/SectionHeader.kt | NEW | 分组标题（titleSmall + onSurfaceVariant，供 R3/R4 使用） |
| ui/components/EmptyState.kt | NEW | 空状态（主文案 + 引导说明 + 可选动作） |
| ui/components/ConfirmDialog.kt | NEW | 标准确认对话框（destructive 时 error 色） |
| ui/theme/GlassComponents.kt | DELETE | 玻璃拟态体系移除 |
| ui/HomeScreen.kt | MODIFY | Glass×3 导入 + 7 类调用点替换（23 处） |
| ui/UseNotificationScreen.kt | MODIFY | 19 处替换 |
| ui/AddressGroupScreen.kt | MODIFY | 13 处替换 |
| ui/SuccessSmsScreen.kt | MODIFY | 9 处替换 |
| ui/LogScreen.kt | MODIFY | 8 处替换 |
| ui/FailSmsScreen.kt | MODIFY | 6 处替换 |
| ui/AddCustomSmsScreen.kt | MODIFY | 5 处替换 |
| ui/AddRuleScreen.kt / RulesScreen.kt / AboutScreen.kt | MODIFY | 各 4/4/4 处替换（glassBaseColor/glassOnCardColor） |
| docs/snapshots/round-2-m3-components.md | NEW | 本快照 |

## 构建结果
- 命令：`.\gradlew.bat assembleDebug` —— 未执行（用户此前取消，本轮沿用静态验证）
- 静态验证：read_lints 对 ui/ 全目录诊断 0 错误；Glass* 全局搜索 0 残留

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| R2 专项：Glass* 全局搜索 | ✅ | `GlassCard/GlassSurface/GlassChip/GlassStyles/glassBase/glassOnCard/glassSheet/glassButton/glassChip` 0 结果 |
| R2 专项：GlassComponents.kt 删除 | ✅ | 已删除且 lints 无断裂 |
| C2 范围控制 | ✅ | 仅组件层新增 + 10 界面机械替换 + 1 文件删除 |
| C3 无 Glass/渐变新增 | ✅ | 新组件无渐变/描边/高光层，单一容器色 |
| C4 令牌引用 | ✅ | 组件层全部走 colorScheme + ParcelSpacing |
| C9 功能回归 | ⏳ | 待用户构建后确认（布局结构未变，仅容器样式与取色来源变化） |

## 遗留问题 / 风险
- AddRule/Rules/About 的 `parcelBaseColor().copy(alpha = 0.65f)` 为半透明 surface 叠加，视觉上偏灰——R4 迁移时改为不透明容器
- HomeScreen 中 ParcelSurface 的 `borderAlpha` 选中态为兼容保留，R3 重构时改为标准 FilterChip/选中样式
- 界面仍为旧布局结构（间距硬编码、菜单未精简），属预期，R3/R4 处理

## 回滚要点
- 恢复 GlassComponents.kt；10 个界面文件的组件层导入改回 `ui.theme.Glass*/glass*`；删除 ui/components/ 下 5 个新文件
