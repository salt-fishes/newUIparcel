# Round 8 快照：v2.0 文档交付

> 日期：2026-07-24 ｜ 状态：待用户确认

## 计划（改造点清单）
- [x] 编写 `docs/ANALYSIS.md`（四维度差异分析 + 已锁定决策）
- [x] 编写 `docs/GOAL.md`（最终目标 + G1-G6 完成标准）
- [x] 覆盖更新 `docs/PRD.md`（v2.0）
- [x] 覆盖更新 `docs/SOP.md`（v2.0，静态验证流程）
- [x] 覆盖更新 `docs/ROADMAP.md`（v2.0，R8-R15）
- [x] 更新 `README.md`（v2 改造状态 + 文档索引）

## 变更文件清单
| 文件 | 操作 | 说明 |
|------|------|------|
| `docs/ANALYSIS.md` | NEW | 视觉/导航/组件/功能四维度差异分析；决策 D1-D6 锁定记录 |
| `docs/GOAL.md` | NEW | 最终交付目标 + G1-G6 可度量完成标准 + 里程碑 M1-M6 |
| `docs/PRD.md` | MODIFY | v1.1 → v2.0：目标、范围（新增 AI/导出卡片）、设计规范（4 主题/emerald/无玻璃）、风险更新 |
| `docs/SOP.md` | MODIFY | v1.0 → v2.0：构建验证后移（用户 R15 后统一构建），静态验证流程，禁止事项扩至 8 条 |
| `docs/ROADMAP.md` | MODIFY | v1.0 → v2.0：R8-R15 轮次定义与里程碑 |
| `README.md` | MODIFY | 顶部状态由"M3 改造已完成"更新为"Web 版对齐改造进行中"，文档索引补 ANALYSIS/GOAL |

## 静态验证结果
- 残留搜索：纯文档轮，无代码改动，N/A
- 签名/路由核对：N/A
- 代码审查：N/A
- 文档一致性：ANALYSIS 差异表与已批准方案（swift-thunder-curie.md）逐项一致；决策 D1-D6 在 PRD/GOAL/SOP 中表述一致

## Review 检查记录
| 检查项 | 结果 | 说明 |
|--------|------|------|
| 四维度差异表与批准方案一致 | 通过 | ANALYSIS.md §1-§4 |
| R8-R15 轮次表完整 | 通过 | ROADMAP.md 总览 + 逐轮任务与验收 |
| 决策 D1-D6 明示 | 通过 | ANALYSIS §0 / GOAL「已锁定决策」/ PRD §3、§5 |
| v1 文档保留历史可追溯 | 通过 | v1 快照 round-0~7 保留；PRD/ROADMAP 注明历史版本 |

## 已锁定决策（用户确认记录）
| # | 决策 | 结论 |
|---|------|------|
| D1 | 面板质感 | 不做玻璃效果；各主题不透明纯色 Surface + outline 细边框；主题渐变背景保留 |
| D2 | 录入短信 | 全屏路由页 |
| D3 | AI 网络层 | HttpURLConnection + kotlinx-serialization |
| D4 | 明文流量 | 仅 https；不加 usesCleartextTraffic |
| D5 | 自动短信 | 默认正则；AI 仅手动录入默认启用；缓存读-through |
| D6 | 主题 | 4 套 + 跟随系统 |
| D7 | 身份码 | 仅真实 Scheme 跳转，不做 Web 版演示假码 |
| D8 | 构建验证 | 每轮静态验证；R15 后用户统一构建反馈 |

## 遗留问题 / 风险
- 无 VCS：建议用户每轮开始前备份 `app\src` 目录。
- R9 起进入代码改造，浅色主题对比度需在每轮 Review 重点抽查。

## 回滚要点
- 纯文档轮：删除 `docs/ANALYSIS.md`、`docs/GOAL.md`；`docs/PRD.md`、`docs/SOP.md`、`docs/ROADMAP.md`、`README.md` 恢复 v1.1/v1.0 表述即可，无代码变更。
