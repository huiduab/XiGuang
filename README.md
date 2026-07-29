# 隙光

隙光是一款以 Android 系统分享面板为主要入口的本地内容收藏应用。它把散落在不同平台的链接、文字、图片和 PDF 收进一个本地资料库，提供整理、搜索、阅读和回顾能力。

项目目前处于持续开发阶段。源码实现是当前事实，`outputs/` 中的产品和技术文档用于保留设计背景。

## 当前功能

### 分享收藏

- 接收纯文本、HTML、图片和 PDF 分享。
- 支持一次分享多张图片。
- 提取并规范化 URL，移除 fragment 和常见追踪参数。
- 按规范化 URL 去重，重复分享不会创建第二条收藏。
- 识别 X、微博、小红书、抖音、B站、知乎、YouTube、博客及其他平台。
- 保存时可选择收藏夹，也可快速新建一级收藏夹。
- 图片和 PDF 会复制到应用私有存储，避免长期依赖临时分享权限。

### 收藏管理

- 按收藏夹或平台分组浏览。
- 查看收藏列表和详情。
- 搜索标题、网址、分享文字和备注。
- 按内容类型、已读/未读状态筛选。
- 编辑标题、备注和所属收藏夹。
- 标记已读/未读，支持多选后的批量移动、删除和状态修改。
- 管理两级收藏夹：新建、重命名、同级排序和安全删除。

### 阅读与回顾

- 链接可在应用内 WebView 或外部浏览器打开。
- 图片和 PDF 可交给系统应用查看。
- 今日页展示今日新增、全部未读和今日阅读进度。
- 随机阅读会在存在多个候选时避免连续抽到同一条收藏。

### 项目与设置

- 创建、编辑和删除项目与来源。
- 来源可以归属项目，也可以保持未归属。
- 主题支持跟随系统、浅色和深色。
- 可设置默认使用应用内阅读或外部浏览器。
- 本地保存通知开关，并展示收藏总数和未读数。

> 通知开关目前只完成偏好持久化，尚未看到通知调度实现；项目与来源目前也是本地管理能力，不包含网络订阅、抓取或云同步。

## 技术栈

| 项目 | 当前配置 |
| --- | --- |
| 应用模块 | `:app` |
| 包名 | `app.xiguang` |
| Android Gradle Plugin | 9.2.1 |
| Gradle Wrapper | 9.4.1 |
| Kotlin | 2.3.10 |
| Java 目标 | 17 |
| Android SDK | min 23 / target 36 / compile 37 |
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation Compose |
| 数据库 | Room 2.8.4，当前 Schema v3 |
| 设置 | Preferences DataStore |
| 异步 | Coroutines + Flow |

项目没有使用依赖注入框架。数据库、附件存储和仓库由 `XiguangApplication` 手动组装。

## 项目结构

```text
app/src/main/java/app/xiguang/
├── MainActivity.kt            主应用入口
├── XiguangApplication.kt      数据库与仓库组装
├── navigation/                Compose 导航与路由
├── domain/
│   ├── model/                 领域模型
│   └── parser/                分享解析、URL 规范化和平台识别
├── data/
│   ├── file/                  私有附件存储
│   ├── local/                 Room Entity、DAO、Database、Migration
│   ├── preferences/           DataStore 设置
│   └── repository/            收藏、项目与来源仓库
├── share/                     系统分享接收和保存页面
├── collection/                收藏分组、列表、详情、阅读、编辑和搜索
├── today/                     今日与随机阅读
├── projects/                  项目和来源管理
├── settings/                  设置与统计
└── ui/                        主题和通用 UI
```

测试目录：

```text
app/src/test/          JVM 单元测试
app/src/androidTest/   Compose、Navigation、WebView 和 Room Migration 测试
```

## 数据与隐私

- 收藏、收藏夹、项目和来源保存在本地 Room 数据库 `xiguang.db`。
- 主题、默认打开方式和通知开关保存在本地 DataStore。
- 分享的图片和 PDF 保存在应用私有目录。
- 当前没有账号体系、网络同步或云端备份。
- URL 去重依据规范化 URL；分享来源应用包名只作为来源信息保存。

仓库不应包含真实邮箱、个人路径、签名文件或 Secret 值。Release 签名材料必须通过本机环境变量或 GitHub Actions Secrets 提供。

## 本地开发

建议使用支持当前 Android Gradle Plugin 的稳定版 Android Studio，并使用 JDK 17。

需要安装：

- Android SDK Platform 37 预览平台包 `android-37.0`。
- Android SDK Build-Tools 37.0.0。
- Android Emulator 或连接的 Android 设备（仅仪器测试需要）。

项目已包含 Gradle Wrapper，无需单独安装 Gradle。

Windows 常用命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

连接设备或启动模拟器后：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Debug APK 输出位置：

```text
app\build\outputs\apk\debug\app-debug.apk
```

当前仓库所在工作机使用 Java 24，且没有配置本地 Android SDK，因此只能确认 Gradle Wrapper 可启动，不能宣称本地单元测试、Lint 或 APK 构建已经通过。

## GitHub Actions

### Debug

`.github/workflows/android-apk.yml` 会在推送到 `main`、`develop`、提交 Pull Request 或手动触发时：

1. 配置 JDK 17、Gradle 缓存和 Android SDK。
2. 运行 `testDebugUnitTest`、`lintDebug` 和 `assembleDebug`。
3. 上传 `xiguang-debug-apk` 和可用的测试报告。

工作流配置存在不代表每个提交已经验证成功，请以对应 Actions 运行结果为准。

### 签名 Release

`.github/workflows/android-release.yml` 仅支持手动触发。它会运行测试与 Release Lint、构建签名 APK，并使用 `apksigner` 校验签名。

GitHub 仓库需要配置：

```text
XIGUANG_RELEASE_KEYSTORE_BASE64
XIGUANG_RELEASE_STORE_PASSWORD
XIGUANG_RELEASE_KEY_ALIAS
XIGUANG_RELEASE_KEY_PASSWORD
```

本地构建 Release 时使用：

```text
XIGUANG_RELEASE_KEYSTORE_PATH
XIGUANG_RELEASE_STORE_PASSWORD
XIGUANG_RELEASE_KEY_ALIAS
XIGUANG_RELEASE_KEY_PASSWORD
```

签名文件、密码和 Secret 值不得提交到仓库或打印到构建日志。

## 当前已知限制

- 设置页“关于”文案仍显示 0.1.0，而 Gradle 构建配置版本为 1.0.0。
- 通知开关尚未连接实际通知调度。
- 项目和来源没有网络抓取或同步能力。
- 当前没有账号、跨设备同步、云备份或数据导出。
- 部分 ViewModel 仍通过 `XiguangApplication` 获取依赖，仓库也保留少量 Entity Flow 暴露，后续应按独立契约逐步收敛。

## 相关文档与资源

- `AGENTS.md`：项目协作、修改确认、架构与验证规则。
- `outputs/隙光_Android技术设计文档_v2.0.md`：目标技术设计。
- `outputs/隙光_Android_APK总设计文档_v1.3.md`：产品与视觉设计背景。
- `outputs/xiguang_home_light.png`、`outputs/xiguang_home_dark.png`：主页视觉稿。
- `outputs/隙光_UI_信息库.png`：信息库视觉稿。
- `outputs/隙光_App图标.png`：应用图标源图。

旧设计文档和 `docs/云端构建APK.md` 可能落后于当前构建配置；涉及版本、SDK 和工作流时，以 Gradle 配置及 `.github/workflows/` 为准。
