# 隙光 Android 技术设计文档

版本：v2.0  
日期：2026-07-29  
状态：开发基线  
交付形式：原生 Android APK

---

## 1. 产品基线

隙光是一款以“用户主动收藏”为核心、以“订阅更新”为辅助的本地优先内容收藏工具。

核心动作不是爬取平台账号，而是：

```text
用户在其他 App 看到内容
→ 点击系统分享
→ 选择“隙光”
→ 选择收藏位置
→ 保存并返回原 App
```

产品遵循以下原则：

1. 分享收藏是第一入口。
2. 单条内容直接进入收藏系统，不进入订阅收件箱。
3. 作者主页、博客主页、栏目页只有在用户明确选择“订阅”时才成为订阅源。
4. 收藏夹表示“用户为什么保存”，平台表示“内容来自哪里”，两者互相独立。
5. 收藏页面只使用一个分类视图按钮，在“收藏夹分类”和“平台分类”之间切换。
6. 通知和后台订阅默认关闭。
7. 默认只保存用户提供的数据、公开元数据、链接和个人备注。
8. 数据优先保存在设备本地。

---

## 2. MVP 范围

### 2.1 P0

- Android 系统分享目标。
- 接收单条文字、网址、图片和 PDF 分享。
- 从分享文本中识别第一个有效网址。
- 自动识别 X、微博、小红书、抖音、B站、知乎、YouTube、博客和其他平台。
- 保存到“未整理”或用户选择的一级/二级文件夹。
- 收藏页面。
- 收藏夹分类/平台分类单按钮切换。
- 内容去重。
- 站内 WebView 打开和外部 App/浏览器打开。
- 本地搜索。
- 创建、重命名和删除两级文件夹。
- 本地 Room 数据库。

### 2.2 P1

- 异步获取网页标题、作者、摘要、封面和 Open Graph/JSON-LD 数据。
- 分享时添加标签和个人备注。
- 分享图片及相关网址的组合收藏。
- RSS/Atom 订阅。
- 打开应用时刷新订阅。
- 导出 JSON/Markdown。

### 2.3 P2

- 公开网页来源监测。
- X、微博、小红书、抖音等平台专用连接器。
- 可选定时刷新和通知。
- 云同步。

### 2.4 第一版不做

- 登录第三方平台。
- 复用其他 App 的 Cookie。
- 无障碍服务自动操作其他 App。
- 逆向平台私有接口。
- 默认下载或搬运视频文件。
- 无限推荐流。
- 默认后台高频轮询。

---

## 3. 核心业务规则

### 3.1 分享与订阅

| 输入 | 用户动作 | 结果 |
|---|---|---|
| 单条推文、文章、笔记、视频网址 | 分享到隙光 | 创建收藏 |
| 文字、图片、PDF | 分享到隙光 | 创建文字/媒体/文件收藏 |
| 作者主页、博客首页、栏目页 | 在隙光内选择“添加订阅” | 创建订阅源 |
| 订阅发现的新内容 | 系统刷新 | 进入订阅收件箱 |
| 订阅内容点击收藏 | 用户主动操作 | 复制/关联到收藏系统 |

单条分享永远不会自动订阅作者。

### 3.2 分类

一条收藏同时拥有：

```text
folder_id       用户收藏夹，可为空；为空表示“未整理”
platform        系统识别的平台
content_type    link / text / image / document
tags            P1
```

收藏页分类视图：

- `FOLDER`：按收藏夹分组。
- `PLATFORM`：按平台分组。

切换视图只改变展示方式，不修改数据。

### 3.3 平台识别

平台以最终内容网址域名为准，分享来源 App 只记录为 `shared_from_package`。

示例：用户从微信分享一篇独立博客文章：

```text
platform = BLOG
shared_from_package = com.tencent.mm
```

首批域名规则：

| 域名 | 平台 |
|---|---|
| x.com / twitter.com | X |
| weibo.com / weibo.cn | 微博 |
| xiaohongshu.com / xhslink.com | 小红书 |
| douyin.com / iesdouyin.com | 抖音 |
| bilibili.com / b23.tv | B站 |
| zhihu.com | 知乎 |
| youtube.com / youtu.be | YouTube |
| 其他 HTTP(S) 地址 | 博客/其他网页 |

短链接解析和重定向跟随属于 P1；P0 先保存原始地址并识别已知短链域名。

### 3.4 去重

按以下顺序生成唯一性：

1. 规范化 URL。
2. 外部内容 ID（连接器可提供时）。
3. 内容指纹。

URL 规范化：

- 域名小写。
- 删除 URL fragment。
- 删除常见跟踪参数：`utm_*`、`fbclid`、`gclid` 等。
- 保留决定内容身份的业务参数。
- 规范末尾斜杠。

再次分享同一内容时：

- 不创建第二条记录。
- 更新收藏夹、备注或最近操作时间。
- 告知用户“已经收藏”。

---

## 4. Android 分享接入

应用通过专用 `ShareReceiverActivity` 注册：

- `Intent.ACTION_SEND`
- `Intent.ACTION_SEND_MULTIPLE`
- `text/plain`
- `text/html`
- `image/*`
- `application/pdf`

接收字段：

- `Intent.EXTRA_TEXT`
- `Intent.EXTRA_SUBJECT`
- `Intent.EXTRA_TITLE`
- `Intent.EXTRA_STREAM`
- `ClipData`
- 调用方包名（能够安全获取时）

安全规则：

1. 不相信发送方 MIME 类型，必须复核内容。
2. URI 只在系统授予的读取权限范围内访问。
3. 大文件和网络解析不得阻塞主线程。
4. 分享失败时至少保留原始文本或 URI。
5. 不把 Token、Cookie、分享原文写入调试日志。

分享界面字段：

- 内容预览。
- 标题。
- 平台。
- 收藏夹。
- 备注（P1）。
- 保存/取消。

默认收藏夹为最近使用位置；首次使用时为“未整理”。

---

## 5. 技术架构

### 5.1 技术栈

- Kotlin 2.3.21。
- Android Gradle Plugin 8.13.2。
- Gradle 8.13。
- Jetpack Compose + Material 3。
- Compose BOM 2026.06.00。
- Room 2.8.4。
- Coroutines + Flow。
- 单 Activity 主界面 + 独立分享接收 Activity。
- minSdk 23。
- targetSdk / compileSdk 36。

### 5.2 模块结构

第一阶段保持单模块，避免过早模块化：

```text
app
├── data
│   ├── local        Room 数据库、Entity、DAO
│   └── repository   收藏与文件夹仓库
├── domain
│   ├── model        平台、内容类型、分类模式
│   └── parser       分享解析、URL 规范化、平台识别
├── share            Android 分享接收界面
├── collection       收藏页面与 ViewModel
├── ui
│   └── theme        隙光视觉主题
└── MainActivity
```

当订阅连接器进入开发后再拆分 `connector-api` 和各平台连接器。

### 5.3 数据流

```text
Android Sharesheet
→ ShareReceiverActivity
→ ShareIntentParser
→ SharedPayload
→ CollectionRepository
→ URL 规范化/平台识别/去重
→ Room
→ Flow
→ 收藏页
```

---

## 6. 数据模型 v1

### collections

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| canonical_url | String? | 规范化网址，唯一索引 |
| original_url | String? | 用户收到的原始网址 |
| title | String | 标题 |
| shared_text | String? | 分享附带文字 |
| platform | String | 平台枚举 |
| content_type | String | 内容类型 |
| mime_type | String? | 原始 MIME |
| preview_uri | String? | 图片/PDF URI |
| folder_id | Long? | 收藏夹；空为未整理 |
| shared_from_package | String? | 分享来源 App |
| note | String? | 用户备注 |
| created_at | Long | 创建时间 |
| updated_at | Long | 更新时间 |

### folders

| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 主键 |
| parent_id | Long? | 父文件夹 |
| name | String | 名称 |
| sort_order | Int | 排序 |
| created_at | Long | 创建时间 |

约束：

- 最多两级。
- 删除文件夹不得静默删除收藏。
- 同一父节点下文件夹名不得重复。

---

## 7. 界面状态

### 7.1 收藏页

```text
顶部：收藏 / 搜索 / 分类视图按钮
内容：当前分类分组及数量
空状态：提示用户从其他 App 的分享面板添加内容
```

分类视图按钮：

- 文件夹图标：当前按收藏夹分类。
- 平台图标：当前按平台分类。
- 点击后立即切换并保存偏好。
- 无需额外平台筛选栏。

### 7.2 分享保存页

目标是在 1–2 次点击内完成：

```text
内容预览
平台与内容类型
收藏夹选择
保存
```

保存成功后关闭分享 Activity，用户回到原 App。

---

## 8. 开发阶段

### M0：工程基线

- Gradle/Compose 工程。
- 主题、图标、包名。
- Room schema v1。
- 基础单元测试。

### M1：分享收藏闭环

- 分享面板出现隙光。
- 解析文字、URL、图片和 PDF。
- 保存到未整理或指定文件夹。
- URL 去重。
- 收藏页展示。

### M2：整理与阅读

- 两级文件夹 CRUD。
- 分类视图切换。
- 搜索。
- 站内 WebView 和外部打开。

### M3：元数据

- 网页标题、作者、摘要和封面。
- 短链接还原。
- 解析失败状态和重试。

### M4：订阅

- RSS/Atom。
- 打开应用时刷新。
- 订阅收件箱。
- 收藏动作进入长期收藏。

---

## 9. MVP 验收标准

1. 隙光出现在 Android 系统分享面板。
2. 从浏览器、X、微博、小红书、抖音分享含网址的文本时能够创建收藏。
3. 无法解析页面时仍保留原始文本和网址。
4. 用户可在保存前选择收藏夹。
5. 未选择收藏夹时进入“未整理”。
6. 重复分享同一规范化网址不会产生重复记录。
7. 收藏页能够一键切换收藏夹分类和平台分类。
8. 切换分类不修改内容所在收藏夹。
9. 应用重启后收藏仍然存在。
10. 内容可以站内或外部打开。
11. 文件夹最多两级。
12. 所有分享解析与数据库操作不阻塞界面。

---

## 10. 下一开发任务

当前代码阶段结束后依次实现：

1. 文件夹创建与选择弹窗。
2. 分享图片/PDF 的持久 URI 处理。
3. 收藏详情页与 WebView。
4. URL 去重冲突反馈。
5. 网页元数据异步补全。
6. 数据库迁移测试。

---

## 11. 视觉实现基线

正式代码应持续参考以下既有视觉资产：

- `xiguang_home_light.png`：暖雾石浅色主题、宋体感大标题、细分隔信息流。
- `xiguang_home_dark.png`：石墨深色主题、低亮度层次与克制强调色。
- `隙光_UI_信息库.png`：无卡片堆叠的文件夹层级与底部导航。
- `隙光_App图标.png`：书页/书签负空间与中间光隙。

当前“信息库”页面在实现中按最新产品决策命名为“收藏”，右上角通过一个按钮在收藏夹分类和平台分类之间切换。
