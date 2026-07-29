# 隙光项目 Harness

本文件适用于仓库根目录及所有子目录。任何编码代理进入本项目后，必须先阅读并遵守本文件；若子目录存在更具体的 `AGENTS.md`，其规则作为补充，但不得绕过本文件的修改确认门禁。

## 1. 首要规则：修改确认门禁

### 1.1 未确认前只能调查

收到需求后，先进行只读检查、复述目标和补齐信息。用户明确确认完整修改契约前，不得：

- 新增、编辑、移动、重命名或删除源代码、测试、资源、Manifest、Gradle 配置、数据库 Schema、脚本或其他项目文件。
- 运行会改写受版本控制文件的格式化、代码生成、依赖升级或迁移命令。
- 顺手修复未纳入本次契约的问题。
- 因改动很小、显而易见或与目标相关而跳过确认。

确认前允许读取文件、搜索引用、检查目录和差异、查看 Git 状态，以及运行不会改写受版本控制文件的诊断命令。

### 1.2 确认流程

每次准备修改项目时必须：

1. **复述目标**：用可验证的语言说明要改变的用户行为、技术结果或文档结果。
2. **补齐信息**：根据任务覆盖适用项，包括当前与期望行为、修改范围、明确不做的内容、UI 状态与文案、数据和兼容规则、异常/空状态/并发/离线行为、验收标准和测试。
3. **提交修改契约**：列出拟修改或新增的文件、各文件职责、接口或数据结构变化、验证方式以及主要风险。
4. **等待明确确认**：只有用户回复“确认”“按这个方案修改”或表达同等清晰授权后，才可写入。
5. **按契约实施**：只修改已确认范围，并保持改动最小。
6. **范围变化时重新确认**：若必须增加文件、改变数据模型或接口、添加依赖、执行迁移或改变既定交互，立即暂停并重新获得确认。

沉默、模糊肯定或只回答某个澄清问题不视为对完整契约的确认。一次确认只授权当次列明的范围。

### 1.3 修改契约模板

```text
我理解的目标：
- ...

需要你确认的细节：
- ...

拟修改：
- path/to/FileA.kt：...
- path/to/FileB.kt：...

明确不改：
- ...

验收方式：
- ...

风险或取舍：
- ...

请确认上述范围；确认后我再修改。
```

## 2. 当前项目事实

代码目录和构建配置是当前实现事实，`outputs/` 中的产品与技术文档是设计背景。两者不一致时必须指出差异，不得把规划内容当成已实现能力。

### 2.1 技术基线

- 单模块 Android 应用：`:app`。
- 包名：`app.xiguang`；Debug 包名带 `.debug` 后缀。
- Android Gradle Plugin 9.2.1。
- Gradle Wrapper 9.4.1。
- Kotlin 2.3.10，Java 17 字节码目标。
- `minSdk 23`、`targetSdk 36`、`compileSdk 37`。
- Jetpack Compose、Material 3、Navigation Compose。
- Room 2.8.4、KSP、Coroutines、Flow。
- Preferences DataStore 负责设置持久化。
- `XiguangApplication` 手动组装数据库、附件存储和仓库；未引入依赖注入框架。
- 构建配置版本为 `1.0.0`（`versionCode = 1`）。

### 2.2 当前源码结构

```text
app/src/main/java/app/xiguang/
├── MainActivity.kt
├── XiguangApplication.kt
├── navigation/
│   ├── AppDestination.kt
│   └── XiguangNavGraph.kt
├── domain/
│   ├── model/CollectionModels.kt
│   └── parser/
│       ├── ShareIntentParser.kt
│       └── UrlTools.kt
├── data/
│   ├── file/AttachmentStore.kt
│   ├── local/
│   │   ├── Entities.kt
│   │   ├── Daos.kt
│   │   └── XiguangDatabase.kt
│   ├── preferences/SettingsRepository.kt
│   └── repository/
│       ├── CollectionRepository.kt
│       └── ProjectRepository.kt
├── share/
│   ├── ShareReceiverActivity.kt
│   └── ShareSaveScreen.kt
├── collection/
│   ├── CollectionScreen.kt
│   ├── CollectionViewModel.kt
│   ├── detail/
│   ├── edit/
│   ├── folder/
│   ├── list/
│   ├── reader/
│   └── search/
├── today/TodayScreen.kt
├── projects/ProjectsScreen.kt
├── settings/SettingsScreen.kt
└── ui/
    ├── folder/FolderCreationDialogs.kt
    └── theme/
```

测试已存在于：

```text
app/src/test/          JVM 单元测试
app/src/androidTest/   Compose、Navigation、WebView 和 Room Migration 仪器测试
```

### 2.3 已实现能力

- 接收系统 `SEND` 的纯文本、HTML、图片和 PDF，以及 `SEND_MULTIPLE` 图片。
- 提取、规范化 URL，移除 fragment 和常见追踪参数，并按规范化 URL 去重。
- 识别 X、微博、小红书、抖音、B站、知乎、YouTube、博客和其他平台。
- 将图片和文档附件复制到应用私有存储。
- 分享保存页支持选择一级/二级收藏夹及快速新建一级收藏夹。
- 收藏按收藏夹或平台分组；支持列表、详情、搜索、类型/阅读状态筛选。
- 支持编辑标题和备注、移动、删除、已读/未读、多选批量操作。
- 支持两级收藏夹的新建、重命名、同级排序和安全删除。
- 链接可在应用内 WebView 或外部浏览器打开；图片和 PDF 交给系统应用查看。
- 今日页展示今日新增、全部未读、阅读进度和避免连续重复的随机阅读。
- 项目与来源支持创建、编辑、删除，并保留未归属来源。
- 设置支持主题、默认打开方式、通知开关和本地收藏统计。

### 2.4 数据现状

- Room 数据库名：`xiguang.db`，当前版本为 3；KSP 已配置将 Schema 导出到 `app/schemas/`，但该目录当前尚不存在。
- v1→v2 增加收藏已读状态；v2→v3 增加项目和来源。
- 数据库实体包括收藏、收藏夹、项目和来源。
- 设置通过 Preferences DataStore 存储。
- 图片和 PDF 附件由 `LocalAttachmentStore` 保存到应用私有文件目录。

## 3. 架构边界

目标依赖方向：

```text
Android 入口 / Compose UI
          ↓
领域模型、规则与用例
          ↓
仓库门面
          ↓
Room DAO / Entity / Database、DataStore、文件存储
```

分享输入是 Android 边界适配：

```text
Intent → ShareIntentParser → SharedPayload → CollectionRepository → Room / AttachmentStore
```

### 3.1 `domain/model`

- 保存稳定的业务语言、领域模型和值类型。
- 不依赖 Android、Compose、Room、DAO、Activity 或 ViewModel。
- 新枚举或字段若影响持久化、UI 或兼容性，必须在修改契约中单独列出。

### 3.2 `domain/parser`

- `UrlExtractor`、`UrlNormalizer`、`PlatformDetector` 保持纯 Kotlin、确定性和无副作用。
- Android `Intent` 逻辑只放在 `ShareIntentParser`。
- URL 规则修改必须给出输入/输出示例和 JVM 单元测试，覆盖追踪参数、大小写、端口、末尾斜杠、fragment、无效 URL 和平台子域名。
- 解析器不得访问数据库、网络或 UI。

### 3.3 `data/local`

- 只负责 Room Entity、DAO、Database 和 Migration。
- 数据库字段或约束变化必须说明版本升级、Migration、Schema 导出及兼容风险。
- 不得使用破坏性迁移掩盖问题。
- Entity 属于持久化细节，不应成为新增 UI API。

### 3.4 `data/file` 与 `data/preferences`

- 文件存储负责附件持久化和路径生命周期，不承载收藏业务决策。
- DataStore 仓库只负责设置读写，不混入页面展示状态。
- 文件 I/O 不得阻塞主线程；不得记录完整分享内容或私有文件路径。

### 3.5 `data/repository`

- 仓库协调 DAO、映射、去重、附件保存和业务写入，不负责 UI 或 Android 生命周期。
- 新 API 优先暴露领域模型，不继续扩大 Entity Flow 的公开范围。
- 时间、网络、文件系统等易变依赖应通过窄接口或构造参数注入。
- 一个仓库方法表达一个业务动作，避免万能方法。

### 3.6 功能包与导航

- 页面按 Route/Screen、ViewModel、UiState、事件组织，文件数量服从实际复杂度。
- Route 负责依赖和生命周期连接；Screen 尽量只接收状态和回调。
- ViewModel 不得直接依赖 DAO、Database 或具体 Compose 组件。
- UI 状态保持不可变，事件单向流动。
- 导航路由集中在 `navigation/`，页面不得自行拼接散落的路由协议。

### 3.7 应用入口与主题

- `MainActivity`、`ShareReceiverActivity`、`XiguangApplication` 保持轻量。
- `ui/theme` 只保存可复用视觉 token 和主题基础。
- 业务规则不放入 Activity、Application 或主题包。
- 引入依赖注入框架属于架构级变化，必须单独确认。

## 4. 当前技术债务

以下是现状，不得未经确认顺手重构：

- 多个 ViewModel 继承 `AndroidViewModel` 并通过 `XiguangApplication` 获取仓库，尚未形成独立构造注入边界。
- `CollectionRepository.collections` 和 `folders` 仍公开 Room Entity Flow。
- 部分页面将 ViewModel、UiState 和 Screen 集中在单个文件中，代码风格和格式并不完全统一。
- `ShareSaveScreen` 仍有硬编码用户文案，未全部迁移到 string resources。
- 构建配置版本为 1.0.0，但设置页“关于”文案仍显示 0.1.0。
- 通知开关已持久化，但当前未看到通知调度或系统通知实现。
- 项目与来源已支持本地管理，但当前未看到网络订阅、抓取或同步实现。
- 仓库没有网络同步、账号体系或云端备份。

## 5. 不可破坏的业务规则

除非用户明确确认改变产品规则：

- 单条内容分享只创建收藏，不自动创建作者订阅或来源。
- 收藏夹、平台、内容类型、项目与来源是不同维度，不因切换视图改写数据。
- 平台以内容 URL 域名识别，分享来源 App 仅记录在 `shared_from_package`。
- URL 规范化移除 fragment 和常见追踪参数，同时保留决定内容身份的业务参数。
- 同一规范化 URL 不创建第二条收藏；重复分享更新允许变化的归属/时间，并返回“已经收藏”语义。
- 分享失败时尽可能保留原始文本或 URI。
- 两级收藏夹是当前上限；非空收藏夹不得直接删除。
- 不在日志中写入 Token、Cookie、完整分享原文或其他敏感内容。
- 大文件、数据库和网络工作不得阻塞主线程。

## 6. 修改策略

- 只修改解决已确认目标所必需的文件。
- 不进行无关重命名、目录整理、全项目格式化、依赖升级或架构重写。
- 已有缺陷先记录；只有纳入修改契约后才能修复。
- 跨层变更必须列出所有受影响层。
- 新业务概念先进入 `domain/model`；纯规则进入窄职责领域文件；持久化进入 `data/local`；协调进入仓库；UI 进入对应功能包。
- 遵循 Kotlin 官方风格和现有尾逗号格式。
- 新用户文案放入 string resources。
- 默认使用 `private`/`internal`，只公开调用方实际需要的表面。
- 不创建 God ViewModel、God Repository、`Utils.kt` 或无边界的 `Manager`。

## 7. 隐私与仓库安全

- 文档、日志、示例和测试数据不得包含真实邮箱、手机号、住址、账号凭据、本机用户名或个人绝对路径。
- 不得提交 `local.properties`、签名文件、私钥、Token、Cookie、`.env` 或 Secret 值。
- GitHub Actions 只能引用 Secrets 的变量名，不能将值打印到日志。
- 图片提交前检查 EXIF/GPS/作者/设备元数据及画面中的个人信息。
- 示例路径使用仓库相对路径，示例凭据使用明显的占位符。
- 修改 Git 作者身份、重写历史或强制推送属于独立且高风险的操作，必须另行确认。

当前仓库的提交作者元数据包含一个 QQ 邮箱；这是已存在的 Git 历史隐私暴露。未经用户单独确认，不得重写历史。

## 8. 验证要求

验证范围应与风险成比例，并在修改契约中提前列出。

- 纯 Kotlin 规则：JVM 单元测试。
- Repository/Room：成功、重复、空值、约束和 Migration 测试。
- ViewModel：初始、加载、成功、空、错误和重复提交状态。
- Compose UI：关键状态可渲染；重要交互增加 Compose UI 测试。
- Manifest、资源或构建配置：运行对应 Gradle 编译、测试和 Lint。

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

Release 还需要四个签名环境变量：

```text
XIGUANG_RELEASE_KEYSTORE_PATH
XIGUANG_RELEASE_STORE_PASSWORD
XIGUANG_RELEASE_KEY_ALIAS
XIGUANG_RELEASE_KEY_PASSWORD
```

当前工作机使用 Java 24，且没有 `local.properties`/完整 Android SDK。`gradlew --version` 可运行，但不能据此声称 Android 测试、Lint 或 APK 构建通过。云端工作流存在也不等于当前提交已经成功执行；必须以实际运行结果为准。

## 9. 交付与变更记录

每次实施后报告：

- 实际修改内容及是否与契约一致。
- 已运行检查及结果。
- 未运行验证及原因。
- 已知限制和需要用户决定的事项。

每次完成已确认的代码修改后，交付前必须向根目录 `CODE_CHANGELOG.md` 追加一条记录。源代码、测试、资源、Manifest、Gradle 配置、数据库 Schema 和项目脚本均计为代码修改；纯调查、答疑或单独编辑说明文档不增加次数。

记录只能追加，编号为现有最大编号加一，使用北京时间：

```text
- 第 N 次修改 | YYYY-MM-DD HH:mm（北京时间） | 一句话说明本次改了什么以及结果。
```

若测试失败或部分验证无法运行，只要代码已经写入仍须记录，并如实说明。写入后检查编号连续、时间存在且描述与实际差异一致。

## 10. 参考资料

- `README.md`：当前项目入口、功能、环境和构建说明。
- `outputs/隙光_Android技术设计文档_v2.0.md`：目标技术设计，可能落后于源码。
- `outputs/隙光_Android_APK总设计文档_v1.3.md`：产品和视觉背景，可能落后于源码。
- `.github/workflows/android-apk.yml`：Debug 测试、Lint 和 APK 工作流。
- `.github/workflows/android-release.yml`：签名 Release 工作流。
