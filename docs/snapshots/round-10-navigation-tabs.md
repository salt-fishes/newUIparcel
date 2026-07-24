# Round 10 快照：导航架构改造（底部 4 Tab + MainScaffold + 身份码整页）

> 日期：2026-07-24 ｜ 状态：通过（静态验证，构建由用户最终统一执行）

## 计划（改造点清单）
- [x] 新增 ui/main/MainTabs.kt：4 Tab 路由常量（tab_pending/tab_archive/tab_identity/tab_settings）
- [x] 新增 ui/main/MainScaffold.kt：底部 NavigationBar（待取角标）+ emerald→teal 渐变全局 FAB + 全局 Toast 层（LocalAppToast，2.4s）；底栏/FAB 仅 Tab 根显示
- [x] 新增 ui/home/ParcelTabContent.kt：待取/历史共享内容（HomeTopBar + 列表 + TimeFilterSheet）
- [x] 新增 PendingTab/ArchiveTab；新增 ui/home/components/ParcelTabMode.kt（PENDING/ARCHIVE 替代 showCompleted）
- [x] 新增 ui/identity/IdentityScreen.kt：Web 版布局（顶部横幅/平台切换/App 跳转横幅/柜前高亮/提示卡），跳转仅调 IdentityEntries 真实 Scheme（决策 D7，无演示假码）
- [x] MainActivity NavHost 重组：startDestination=tab_pending，MainScaffold 包裹，旧 home/settings 路由移除，9 条二级路由全保留
- [x] 删除 ui/home/HomeScreen.kt（内容已迁移）
- [x] 适配：ParcelList/AddressCard/HorizontalParcelList 改 tabMode；HomeTopBar 移除"显示已取件"开关项、设置入口改 tab_settings；AddCustomSmsScreen/RulesScreen 旧 home 导航改 tab_pending

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/main/MainTabs.kt` | NEW | Tab 路由常量 + isTabRoot |
| `ui/main/MainScaffold.kt` | NEW | 底栏/FAB/Toast 全局骨架 |
| `ui/home/ParcelTabContent.kt` | NEW | 待取/历史共享 Tab 内容 |
| `ui/home/PendingTab.kt` | NEW | 待取 Tab |
| `ui/home/ArchiveTab.kt` | NEW | 历史 Tab |
| `ui/home/components/ParcelTabMode.kt` | NEW | PENDING/ARCHIVE 枚举 |
| `ui/identity/IdentityScreen.kt` | NEW | 身份码整页（真实跳转） |
| `MainActivity.kt` | MODIFY | NavHost 重组 + MainScaffold + 待取角标计数 |
| `ui/home/HomeScreen.kt` | DELETE | 已迁移至 ParcelTabContent |
| `ui/home/components/ParcelList.kt` | MODIFY | showCompleted→tabMode；空态文案分 Tab |
| `ui/home/components/AddressCard.kt` | MODIFY | tabMode 行过滤；折叠=true 切换生效（对齐 Web） |
| `ui/home/components/HorizontalParcelList.kt` | MODIFY | tabMode 透传 |
| `ui/home/components/HomeTopBar.kt` | MODIFY | 移除已取件开关；设置入口→tab_settings |
| `ui/AddCustomSmsScreen.kt` | MODIFY | 保存后导航 home→tab_pending |
| `ui/RulesScreen.kt` | MODIFY | 清规则后导航 home→tab_pending |

## 静态验证结果
- 残留搜索：`"home"` 旧路由 0 ✅；`HomeScreen` 引用 0 ✅；`navigate("settings")` 0 ✅；showCompleted 仅 HomePrefs 存留（键保留不读，无害）✅
- 路由核对：4 Tab 根 + add_custom_sms/add_rule/address_group/rules/fail_sms/success_sms/about/use_notification/logs 共 13 条全部在 NavHost 注册 ✅
- 调用链：updateAllWidget/readAndParseSms 定义与调用点未动（MainActivity/AddressCard/ParcelTabContent）✅
- 代码审查：身份码页仅调 openTaobaoIdentityEntry/openPddIdentityEntry ✅；无演示假码绘制 ✅；Color(0x 新增 0（仅令牌引用）✅
- 底栏/FAB 显隐：MainScaffold.onTabRoot 判定，二级路由不显示 ✅

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| C1 静态验证 | 通过 | 见上 |
| C2 范围控制 | 通过 | 仅 R10 清单内文件 |
| C9 功能回归 | 通过 | 数据层/解析器/小组件零改动；完成/一键取件/标签/筛选/排序链路保留 |
| C10 编码规范 | 通过 | state hoisting；无 `!!` |
| 专项：旧路由全可达 | 通过 | 9 条二级路由注册且引用点已改 |
| 专项：身份码真实跳转 | 通过 | IdentityEntries 未改动，仅界面重排 |

## 遗留问题 / 风险
- HomeTopBar 仍为旧视觉（R11 替换为 AppNavbar + ListToolBar）。
- SettingsScreen 作为 Tab 页仍含返回箭头（R12 重写时去除）。
- Tab 切换状态保持依赖 saveState/restoreState，真机验证项。
- `show_completed_codes` 偏好键退役（保留不读）；HomePrefs 两个函数暂未被引用（R15 清理待定）。

## 回滚要点
- 还原 MainActivity/ParcelList/AddressCard/HorizontalParcelList/HomeTopBar/AddCustomSmsScreen/RulesScreen；恢复 ui/home/HomeScreen.kt；删除 ui/main/、ui/identity/、ParcelTabContent/PendingTab/ArchiveTab/ParcelTabMode 新文件。
