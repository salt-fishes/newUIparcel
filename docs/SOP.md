# Parcel Web 版对齐改造 — 标准作业流程（SOP）

> 版本：v2.0 ｜ 关联文档：[PRD.md](./PRD.md) · [GOAL.md](./GOAL.md) · [ROADMAP.md](./ROADMAP.md) · [ANALYSIS.md](./ANALYSIS.md)
>
> 本文档定义 R8-R15 每一轮改造的**固定执行循环**。所有轮次必须严格遵循，不得跳步。
> 与 v1 的关键差异：**构建验证后移**——每轮做静态验证，完整构建由用户在 R15 后统一执行并反馈（决策 D8）。

---

## 1. 每轮执行循环

```
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│ 1.勘察对齐 │ → │ 2.实施改造 │ → │ 3.静态验证 │ → │ 4.Review │ → │ 5.快照归档 │
└──────────┘   └──────────┘   └──────────┘   └────┬─────┘   └────┬─────┘
                                                  │不通过          │通过
                                                  ▼               ▼
                                            返回步骤 2 修复    用户确认后进下一轮
```

### 步骤 1：勘察对齐

- 明确本轮目标（以 ROADMAP.md 为准）
- 大范围改造（R10/R11/R12）前，先用探索代理勘察：组件树、引用点、调用链，产出精确到文件的改造点清单
- 视觉数值一律以参考项目提取报告（`C:\Users\DENG\.workbuddy\plans\swift-thunder-curie.md` 附录 A）为准，禁止凭印象写色值

**产出**：改造点清单（写入当轮快照的"计划"节）

### 步骤 2：实施改造

- 严格限定在本轮范围内，不顺手改其他轮次的内容
- 遵循 Compose 编码规范：
  - state hoisting 单向数据流，不在 Composable 内直接读写 SharedPreferences（上提到 ViewModel/remember）
  - 不使用 `!!`；suspend 函数保证 main-safe；网络请求一律 Dispatchers.IO
  - 资源/变量命名避开 Android 保留字（background、icon、view 等），统一加前缀
- 遵循 PRD §5 设计规范：颜色只走 `MaterialTheme.colorScheme` / `LocalParcelPalette`；间距只走 `ParcelSpacing`；字号只走 typography/令牌；面板只走 `ui/components/Surface.kt`

### 步骤 3：静态验证（准入门槛，替代 v1 的构建验证）

- **残留搜索**：本轮应消除的旧符号全局 grep 为 0；`ui/` 下新增硬编码 `Color(0x`/`.sp`/`.dp` 为 0（`ui/theme/` 除外）
- **签名核对**：被约束的公开 API（如 ParcelColors 各函数）签名 diff 为空
- **路由核对**：NavHost 路由表与预期清单一致，旧路由全部可达
- **代码审查**：无 `!!`、无 Composable 内直读 SharedPreferences、无保留字命名、密钥等敏感信息不入日志
- **read_lints 类检查**：可用静态检查全项目 0 错误
- 任一项不通过 = 本轮不成立，返回步骤 2 修复

### 步骤 4：Review（对照检查清单）

逐项核对 §4 检查清单，结果（通过/不通过 + 说明）记录到快照中。
发现不通过项 → 返回步骤 2 修复 → 重新静态验证 → 重新 Review。

### 步骤 5：快照归档

在 `docs/snapshots/` 下创建 `round-N-<简述>.md`，格式见 §3。
快照完成 + **用户确认通过** = 本轮结束，方可进入下一轮。

---

## 2. 角色与职责

| 角色 | 职责 |
|------|------|
| 执行者（AI） | 勘察、实施、静态验证、自 Review、写快照 |
| 确认者（用户） | 每轮快照生成后确认结果；对不通过项提出异议；R15 后统一执行 `.\gradlew.bat assembleDebug` 构建并反馈结果；建议每轮前自行备份 `app\src` |

---

## 3. 快照文件格式

文件命名：`docs/snapshots/round-N-<kebab-case-简述>.md`

```markdown
# Round N 快照：<标题>

> 日期：YYYY-MM-DD ｜ 状态：通过 / 部分通过

## 计划（改造点清单）
- [ ] ...

## 变更文件清单
| 文件 | 操作(NEW/MODIFY/DELETE) | 说明 |
|------|------|------|

## 静态验证结果
- 残留搜索：...
- 签名/路由核对：...
- 代码审查：...

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|

## 遗留问题 / 风险
- ...

## 回滚要点
- 恢复 <文件> 中 <内容>；删除 <新文件>
```

---

## 4. Review 检查清单

### 4.1 通用清单（每轮必查）

| # | 检查项 | 标准 |
|---|--------|------|
| C1 | 静态验证 | §1 步骤 3 全部通过 |
| C2 | 范围控制 | 只改了本轮该改的文件，无越界改动 |
| C3 | 无越界视觉 | 不新增半透明模糊/玻璃实现（决策 D1）；不新增渐变背景以外的装饰层 |
| C4 | 令牌引用 | 颜色走 colorScheme/ParcelPalette，间距走 ParcelSpacing，字号走 typography；面板走 Surface.kt |
| C5 | 触控目标 | 新增/修改的可点击元素 ≥ 48×48dp（老人模式 ≥ 56dp） |
| C6 | 间距网格 | 间距为 4 的倍数，优先 8dp 网格 |
| C7 | 无障碍 | 图标按钮有 contentDescription（描述动作，不含"按钮"） |
| C8 | 多主题可读 | 改动在 4 主题下均不破坏可读性（浅色主题重点抽查对比度 ≥ 4.5:1） |
| C9 | 功能回归 | 本轮涉及的功能路径逻辑不变（数据层/解析器/小组件调用链） |
| C10 | 编码规范 | 无 `!!`、无 Composable 内直读 SharedPreferences、无保留字命名、敏感信息不入日志 |

### 4.2 轮次专项清单

- **R8 文档**：四维度差异表与已批准方案一致；R8-R15 轮次表完整；决策 D1-D8 在 PRD/GOAL 中明示
- **R9 视觉 tokens**：4 ColorScheme + 跟随系统齐备；ParcelPalette 语义色 4 主题各一份；ParcelColors 公开签名 diff 为空；`ui/` 下 `Color(0x` 仅存在于 `ui/theme/`；ThemeManager 存量 0/1/2 兼容
- **R10 导航**：旧 11 路由全部可达；`"home"` 旧路由 0 残留；底栏/FAB 仅 Tab 根显示；updateAllWidget/readAndParseSms 调用链未动；身份码页仅调 IdentityEntries 真实跳转
- **R11 列表**：HomeTopBar 0 残留；字号全走令牌（老人 1.5x 不失效）；触控目标抽查通过；4 主题下列表可读
- **R12 设置**：8 菜单项逐项可达；WallpaperAdjustSheet 0 残留；SharedPreferences 存量键零变更；备份 JSON 含 settings+rules+mappings
- **R13 AI**：HttpURLConnection 仅出现在 AiClient；无 usesCleartextTraffic（决策 D4）；超时常量 8/10/12s；`addLog.*[aA]piKey` 为 0；SmsReceiver/通知服务解析路径不变（仅正则）
- **R14 导出**：MediaStore API 29 分支（IS_PENDING）正确；无新增存储权限；Bitmap 及时回收；4 配色色值与规范一致
- **R15 终审**：GOAL.md G1-G6 全过；终审 grep 残留 0；文档勾选收官

---

## 5. 禁止事项

1. 禁止跳过静态验证直接 Review
2. 禁止一轮内混入其他轮次的改造内容
3. 禁止修改短信解析、数据层、小组件逻辑（R13 允许的 SmsProcessor 可选缓存参数除外，且必须记录在快照中）
4. 禁止引入新第三方依赖（AI 用 HttpURLConnection；不引 haze/OkHttp/ZXing）
5. 禁止在未通过 Review 的情况下生成"通过"快照
6. 禁止实现 Web 版演示假身份码（条码/二维码假码）；身份码仅真实 Scheme 跳转
7. 禁止添加 usesCleartextTraffic（决策 D4，仅 https）
8. 禁止在本流程中执行全量构建充当验证（构建统一由用户在 R15 后执行）

---

*每轮快照即本地版本记录，等效于 git commit + push 的可追溯性。*
