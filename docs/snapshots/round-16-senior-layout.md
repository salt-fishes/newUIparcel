# Round 16 — 大字模式专用重排

日期：2026-07-24
状态：静态完成，未执行 Gradle。

## 交付

- 移除统一字号倍率，改用语义化关怀 Typography。
- AppNavbar、MainScaffold、ListToolBar、AddressCard、SmsDataRow 与 PickupCodeChip 增加大字专用布局。
- 底栏提升至 88dp，关键触控目标采用 48/56dp。
- 长标题、标签、计数与角标增加限宽、省略及 `99+` 保护。

## 静态 Review

- 大字模式不再依赖整体缩放，降低文字挤压与卡片溢出风险。
- Observer/Receiver 重复注册与 API 29-32 广播注册分支已修正。
- 实机字体缩放、横屏和极窄屏仍需最终真机验收。
