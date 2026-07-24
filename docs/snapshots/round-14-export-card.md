# Round 14 — Canvas 导出取件卡片

状态：已完成静态 Review，未执行 Gradle 构建（按用户约束由最终验收统一构建）。

## 交付

- 新增 `util/ExportCardRenderer.kt`：Android Canvas 绘制 1280 宽动态高度 PNG；按地址分组、取件码块、柜号与时间；深色/翡翠/紫罗兰/明亮四套配色。
- 新增 `ui/components/ExportCardDialog.kt`：地址范围选择、四配色选择、是否包含已取件、预览、复制文本清单、保存 PNG。
- 修改 `ListToolBar.kt`：增加导出图片卡片入口。
- 修改 `ParcelTabContent.kt`：接入导出弹窗，并读取当前分组数据。
- 使用 MediaStore `RELATIVE_PATH=Pictures/Parcel`；API 29+ 使用 `IS_PENDING` 两阶段提交；失败时删除半成品；未新增存储权限。

## 静态检查

- 导出入口仅出现在待取/历史共享工具条，弹窗使用当前 ViewModel 数据。
- 图片保存走 MediaStore，不直接写外部路径。
- 复制操作使用系统 ClipboardManager。

## 风险

未进行构建；最终构建需重点确认 Compose Image 对 `Bitmap.asImageBitmap()` 的依赖、Material3 下拉菜单 API，以及不同设备字体/emoji 渲染差异。
