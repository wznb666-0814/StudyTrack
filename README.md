# 学迹 (StudyTrack)

学迹是一款面向学生的 Android 成绩管理与分析应用，强调沉浸式视觉与高效的数据记录体验。项目以 Kotlin + Jetpack Compose 构建，采用 MVVM 架构与本地离线存储，支持壁纸、主题色与液态玻璃风格的深度个性化。

## 功能概览
- 科目管理：创建/维护学科与满分设置
- 成绩记录：录入单次成绩、班排/年排/区排及反思
- 数据分析：统计与趋势展示，支持图表可视化
- Excel 导入/导出：成绩批量备份与迁移
- 个性化设置：壁纸、主题色、字体与图标颜色、液态玻璃参数
- 沉浸式 UI：全屏壁纸背景与玻璃拟态卡片

## 技术栈
- 语言：Kotlin
- UI：Jetpack Compose (Material 3)
- 架构：MVVM
- 依赖注入：Hilt
- 数据库：Room (SQLite)
- 本地配置：DataStore
- 图表：自定义 Canvas 图表
- Excel：Apache POI

## 运行环境
- 最低系统：Android 10 (API 29)
- 目标/编译版本：Android 16（当前编译 SDK 为 36）

## 快速开始
### 导入与同步
1. 打开 Android Studio
2. 选择 Open，定位到项目根目录（包含 `gradlew` / `gradlew.bat`）
3. 等待 Gradle 同步完成

### 运行调试
1. 连接真机或启动模拟器
2. 点击 Run (▶️)

### 命令行构建
```bash
./gradlew :app:assembleDebug
```

### 打包 APK
```bash
./gradlew :app:assembleRelease
```
构建产物位置：
- Debug：`app/build/outputs/apk/debug/app-debug.apk`
- Release：`app/build/outputs/apk/release/app-release.apk`

## 个性化与主题说明
- 默认壁纸：项目根目录 `bizhi.png`（打包到 `app/src/main/res/drawable/bizhi.png`）
- 主题颜色：默认淡红色，支持按壁纸自动取色
- 前景色策略：支持自动/强制亮色/强制暗色（自动算法已优化）
- 深色模式：不跟随系统暗色模式，强制亮色显示
- 液态玻璃：可调节模糊、折射、染色、边框等参数

## 商业化发布说明
- 版权与商用：允许商业使用与发布，需保留版权声明与作者信息
- 资源归属：默认壁纸与 Logo 为项目资源，二次分发需保留来源
- 第三方依赖：发布前需核对依赖许可并遵循其条款
- 隐私与数据：当前版本不包含服务器上传逻辑，数据仅存本地

## 项目结构
- `app/src/main/java/com/repea/studytrack/`
  - `data/`：数据库实体与 DAO
  - `di/`：Hilt 依赖注入
  - `repository/`：数据仓库与偏好配置
  - `ui/`：Compose 组件与页面
  - `viewmodel/`：ViewModel 与状态管理
  - `utils/`：工具类（Excel 导入导出等）

## 许可与声明
© 2026 StudyTrack Project
