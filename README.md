# 隙光

隙光是一款以 Android 系统分享面板为主要入口的本地内容收藏应用。

当前开发基线已经包含：

- 原生 Kotlin + Jetpack Compose 工程。
- Android 系统分享目标。
- 文字、网址、图片、PDF 分享解析。
- X、微博、小红书、抖音、B站、知乎、YouTube 与博客平台识别。
- Room 本地数据库。
- URL 规范化与去重。
- 分享保存页。
- 收藏页。
- 收藏夹分类/平台分类单按钮切换。
- 浅色和深色“隙光”主题。
- 正式 App 图标。

## 推荐方式：云端生成 APK

项目包含 `.github/workflows/android-apk.yml`。上传到 GitHub 后，GitHub Actions 会自动准备 JDK 17 和 Android SDK、运行测试并生成 APK。本地不需要安装 Android Studio。

详细步骤见：

```text
docs/云端构建APK.md
```

## 可选方式：本地开发环境

只有需要在本机运行模拟器、调试界面时，才建议安装最新版稳定版 Android Studio，并在首次启动时安装默认 Android SDK 组件。不要单独修改系统 Java。

打开本项目后确认：

1. Gradle JDK 使用 Android Studio 自带的 JDK 17 或更高兼容版本，而不是本机 Java 24。
2. Android SDK Platform 36 已安装。
3. Android SDK Build-Tools 已安装。
4. 等待 Gradle Sync 完成。

工程已经包含 Gradle Wrapper，不需要另外安装 Gradle。

## 构建

在 Android Studio 中选择：

```text
Build → Build APK(s)
```

或在兼容环境中运行：

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Debug APK 输出位置：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 当前环境说明

创建工程时检测到本机默认 Java 为 24，且没有完整 Android SDK。AGP 8.13.2 的基线环境为 JDK 17。源码已经准备好，但首次完整编译应在 Android Studio 自带的兼容 JDK 与 SDK 下完成。

## 产品与视觉文档

- `outputs/隙光_Android技术设计文档_v2.0.md`
- `outputs/隙光_Android_APK总设计文档_v1.3.md`
- `outputs/xiguang_home_light.png`
- `outputs/xiguang_home_dark.png`
- `outputs/隙光_UI_信息库.png`
- `outputs/隙光_App图标.png`
