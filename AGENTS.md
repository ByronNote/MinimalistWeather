# Codex 项目规则

## 当前架构状态

本项目 active app 已迁移为 Compose-first Clean Architecture Android 应用。

当前事实：

- 包名和 applicationId：`cn.byronlab.weather`
- UI：Jetpack Compose + Material 3
- 状态：ViewModel + 不可变 `StateFlow<HomeUiState>` + `HomeUiEvent` + `HomeUiEffect` 单向数据流
- DI：Hilt + KSP
- 异步：domain repository/use case 使用 `suspend`/`Flow` 契约，表现层使用 Kotlin Coroutines
- 模块：`:app`、`:presentation`、`:data`、`:domain`
- 依赖方向：`:app -> :presentation`、`:app -> :data`、`:app -> :domain`、`:presentation -> :domain`、`:data -> :domain`
- 依赖、插件及 Android SDK/应用版本统一声明在 `gradle/libs.versions.toml`
- 已移除旧 XML/MVP UI、旧 Presenter/Fragment active 路径、旧 Dagger component、ButterKnife、SmartRefresh 和 `:widget` 模块
- `:data` active 路径使用 Open-Meteo：城市搜索为内置全球热门城市列表 + Open-Meteo Geocoding，天气为 Open-Meteo Forecast，空气质量为 Open-Meteo Air Quality
- 已移除旧 MI/环境云 API、Retrofit/RxJava、ORMLite、FastJson、旧 `city.db` 和相关 legacy repository/mapper
- 已移除仅剩旧工具类的 `:library` 模块；共享能力必须有明确归属后再引入 `core` 模块
- 已移除 Stetho、按 build type 注入对象的旧 `BuildConfig` 调试链和 Java Application；`:app` 启动入口已迁移为 Kotlin
- active app、data、domain、presentation 生产与测试源码已全部迁移为 Kotlin；domain model 使用不可变 `data class`，结果契约使用 sealed `DomainResult`
- UseCase 和 data repository 使用 Dagger 构造注入；`:app` 的 Hilt 装配按 Coroutine、Network、DataStore、Repository 四类模块拆分
- 网络层使用 OkHttp 5.3.0；Open-Meteo JSON 使用 Kotlinx Serialization 1.11.0 解析为强类型 response DTO，再由 mapper 映射为 domain model
- 已移除手写 `org.json` parser；remote DTO 通过默认值、nullable 字段和忽略未知字段处理 Open-Meteo 响应演进
- 当前城市偏好使用 DataStore Preferences 保存，并通过 `SharedPreferencesMigration` 兼容迁移旧数据

目标架构：

- `domain`：业务模型、Repository 契约、UseCase、纯业务规则
- `data`：Open-Meteo 网络访问、本地热门城市库、偏好设置、Mapper、Repository 实现
- `presentation`：独立 Android library，持有 ViewModel、不可变 UI 状态、UI 事件、Compose 页面和 UI mapper
- `app`：依赖装配、导航宿主、Android 入口

目标技术栈：

- 新 UI 和新表现层代码使用 Kotlin
- Jetpack Compose + Material 3 用于 UI
- ViewModel + `StateFlow` 管理表现层状态
- Kotlin Coroutines + Flow 处理异步任务和可观察数据
- Hilt 作为依赖注入框架
- DataStore Preferences 管理偏好设置
- OkHttp 5.3.0 处理 Open-Meteo HTTPS API
- Kotlinx Serialization 1.11.0 负责将 Open-Meteo JSON 解析为 `@Serializable` response DTO，再通过 mapper 映射为 domain model
- Room 仅在后续需要天气离线缓存、历史数据或更大本地城市库时引入
- Compose-first 页面按需使用 Navigation
- 按需使用 JUnit、Kotlin coroutines test、Turbine、MockK 或同类测试工具

## 迁移原则

- 每次有意义的改动后都要保持应用可运行。
- 除非任务明确要求改变行为，否则保持现有行为不变。
- 不要重新引入旧 MI/环境云 API、旧 `city.db`、ORMLite、RxJava 或 FastJson。
- 引入新依赖必须有明确的迁移目的。
- 不要把领域逻辑写进 Android 类、ViewModel、remote DTO、Room Entity 或 Compose UI。
- 使用不可变模型和单向数据流。
- 网络数据的空值、不稳定字段和容错逻辑放在 data 层 mapper 中，不要散落到 UI。
- `domain` 层不得依赖 Android framework。

## 包结构方向

新代码或迁移后的代码优先使用以下包结构：

```text
cn.byronlab.weather
  app/
  core/
    common/
    network/
    database/
    datastore/
    designsystem/
  domain/
    model/
    repository/
    usecase/
  data/
    remote/
    local/
    mapper/
    repository/
  presentation/
    home/
    citysearch/
    savedcities/
```

功能包可以拥有自己的 presentation 代码。共享的 domain/data 契约先放在对应层级包中，只有当功能边界足够大时再考虑 feature module。

## 编码规则

- 所有新文件优先使用 Kotlin。
- 优先使用 Hilt 构造函数注入。
- ViewModel 对外暴露不可变 `StateFlow<UiState>`。
- UI 操作用明确的 event/intent 表达；一次性消息、导航等副作用用 effect 表达。
- 可恢复的 domain/data 失败使用 sealed result/error 类型表达。
- remote DTO、JSON 数据和持久化 Entity 必须通过专门的 mapper 映射为 domain model。
- 不要把 DTO、Room Entity、`Context`、`Resources` 或 Android View 传入 domain 代码。
- Compose 函数尽量保持无状态；状态持有者放在 ViewModel。
- 不要使用 `GlobalScope`、裸线程，或在主线程执行阻塞调用。
- 新 repository/use case 契约必须使用 `suspend` 或 `Flow`。
- 逻辑变更必须新增或调整测试。

## 验证命令

开发时运行相关子集；较大改动交付前运行完整验证：

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

当架构或依赖装配发生变化时，先运行受影响模块的最小测试集，再运行完整验证。

## 测试要求

- Domain use case：使用确定性输入做纯单元测试，不依赖 Android 运行时。
- Mapper：覆盖正常数据、缺失字段、格式异常和部分字段为空的 API 数据。
- Repository：覆盖缓存、网络、错误和降级行为。
- ViewModel：用 coroutine test 覆盖加载、刷新、空状态和错误状态。
- Compose UI：只有在布局或交互风险较高时才补截图测试或仪器测试。

## 遗留清理优先级

下一阶段优先清理：

- 删除未被 active app 使用的旧资源、工具类和构建声明。
- 为 Open-Meteo HTTP 边界补充 MockWebServer 测试，覆盖非 2xx、空响应和畸形响应。

## 文档约定

`docs/modern-clean-architecture-plan.md` 是迁移路线图。每当阶段完成、拆分或范围发生实质变化时，都要同步更新该文档。
