# 云端构建 APK

本项目使用 GitHub Actions 生成 APK，本地不需要安装 Android Studio、Android SDK 或 Gradle。

## 第一次启用

1. 在 GitHub 创建一个空仓库。
2. 将本项目代码上传到仓库。
3. 打开仓库的 **Actions** 页面。
4. 选择 **Build Android APK**。
5. 点击 **Run workflow**。

推送到 `main` 或 `develop` 分支时也会自动构建。

## 下载 APK

构建完成后：

1. 打开对应的 Actions 运行记录。
2. 找到页面底部的 **Artifacts**。
3. 下载 `xiguang-debug-apk`。
4. 解压后得到 `app-debug.apk`。

Debug APK 适合当前开发测试，可以直接安装到 Android 手机。正式发布前需要增加独立签名和 Release 构建。

## 云端自动准备的环境

工作流会自动完成：

- 安装 JDK 17。
- 准备 Gradle 8.13。
- 安装 Android SDK Platform 36。
- 运行单元测试。
- 构建 Debug APK。
- 保存 APK 和测试报告。

## 后续正式签名

发布版不能把签名文件直接提交到仓库。后续应将以下内容保存为 GitHub Actions Secrets：

- 签名文件的 Base64 内容。
- Key alias。
- Key password。
- Store password。

当前阶段只生成不含正式发布签名的 Debug APK。
