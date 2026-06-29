# 现代化 Clean Architecture 迁移计划

最后更新：2026-06-26

## 目标

将 MinimalistWeather 逐步现代化为一个可维护的 Kotlin-first Android 应用，采用 Clean Architecture 和当前主流 Jetpack 模式，并在迁移过程中始终保持应用可构建、可运行、可发布。

目标最终状态：

- Kotlin-first 代码库
- 清晰的 `domain`、`data`、`presentation` 分层
- Jetpack Compose + Material 3 UI
- ViewModel + 不可变 `StateFlow` UI 状态
- 使用 Coroutines + Flow 替代 RxJava
- 使用 Hilt 进行依赖注入
- 使用 Room 数据库和 DataStore 偏好设置
- Mapper、UseCase、Repository、ViewModel 具备有效测试覆盖

## 当前状态概览

应用当前使用：

- Java
- XML 布局
- MVP Presenter
- Dagger，且部分组件在 Presenter 内部手动创建
- RxJava 1
- Retrofit + OkHttp + FastJson converter
- ORMLite
- SharedPreferences
- 偏旧的 AndroidX/support 时代依赖

Phase 0 已处理的基线风险：

- 启动、城市选择和抽屉城市列表流程中的 eager `Observable.just(...)` 已改为延迟执行。
- 天气数据映射已补充缺失字段、格式异常和空列表防御。
- 日期转换已移除已废弃且结果错误的 `Date#getMonth()` / `Date#getDay()` 用法。
- library/widget manifest 与 namespace 问题已修复，lint 不再通过 `abortOnError false` 吞掉真实错误。
- 明文网络已从全局允许收敛到必要 legacy API 域名。

Phase 1 已建立的基线能力：

- 已新增独立 `:domain` Java library 模块，`app` 已依赖该模块。
- 已新增不可变 domain model、Repository 契约、UseCase、`DomainResult` / `DomainError`。
- Domain 层不依赖 Android framework、RxJava、Retrofit、ORMLite 或 UI 类型。
- 迁移期 UseCase 暂时保持同步 Java API，旧 Presenter 后续可通过 `Observable.fromCallable(...)` 包装调用；实际 data 适配层在 Phase 2 接入。

## 目标架构

```text
presentation -> domain <- data
       app 负责依赖装配和导航
```

依赖方向：

- `presentation` 依赖 `domain`。
- `data` 依赖 `domain`。
- `domain` 只依赖 Kotlin/JDK。
- `app` 只在装配时依赖各实现层。

建议包结构：

```text
cn.byronlab.weather
  app
  core
    common
    network
    database
    datastore
    designsystem
  domain
    model
    repository
    usecase
  data
    remote
    local
    mapper
    repository
  presentation
    home
    citysearch
    savedcities
```

## 目标框架选择

除非后续技术 spike 发现明确阻碍，否则默认采用：

- UI：Jetpack Compose、Material 3
- 状态：ViewModel、`StateFlow`、不可变 UI state data class
- 异步：Kotlin Coroutines、Flow
- DI：Hilt
- 持久化：Room 管理天气和城市等关系型数据
- 偏好设置：DataStore Preferences
- 网络：Retrofit、OkHttp，debug 构建使用 logging interceptor
- JSON：Kotlin serialization 或 Moshi，在 DTO 迁移阶段最终选择
- 导航：Compose Navigation；新 Compose-first 流程可评估 Navigation 3
- 后台任务：只有重新引入周期刷新时才使用 WorkManager

版本策略：

- 依赖版本集中到 version catalog。
- 每个依赖迁移阶段开始前，先从 Android/Kotlin 官方文档确认当前稳定版本。
- 同一职责不要让旧框架和新框架并行超过一个迁移阶段。

## Phase 0：稳定遗留应用

状态：已完成，2026-06-26。

目标：在架构迁移前先移除明显运行时风险。

任务：

- 将 eager `Observable.just(expensiveCall())` 改为延迟执行。
- 天气 mapper 增加对缺失字段和格式异常的防御处理。
- 修复日期解析和格式化；优先使用支持 desugaring 的 `java.time`，否则使用安全 fallback。
- 移除 library manifest 中无效的 activity 声明。
- 当前 lint 错误修复后，让 lint 对真实错误失败。
- 将明文网络限制到必要 legacy API 域名，或在可行时迁移到 HTTPS。

验证：

- 已通过：`./gradlew test`
- 已通过：`./gradlew lint`
- 已通过：`./gradlew assembleDebug`
- 待补充：手动 smoke test：启动应用、加载默认城市、搜索城市、切换城市、删除已保存城市。

退出标准：

- 启动和城市列表流程中没有已知主线程数据库/文件 IO。
- 天气 mapper 遇到异常数据时返回可展示的错误状态，而不是崩溃。
- lint 没有被 `abortOnError false` 隐藏的错误。

## Phase 1：建立 Domain 层

状态：已完成，2026-06-26。

目标：先建立纯业务契约，再替换具体实现。

任务：

- 新增 domain model，例如 `Weather`、`CurrentWeather`、`ForecastDay`、`AirQuality`、`LifeIndex`、`City`。
- 新增 Repository 契约：
  - `WeatherRepository`
  - `CityRepository`
  - `SettingsRepository`
- 新增 UseCase：
  - `GetCurrentWeatherUseCase`
  - `RefreshWeatherUseCase`
  - `ObserveSavedCitiesUseCase`
  - `SearchCitiesUseCase`
  - `SetCurrentCityUseCase`
  - `DeleteSavedCityUseCase`
- 定义 domain error/result 类型。

验证：

- 已通过：`:domain:test`
- 已通过：`./gradlew test`
- 已通过：`./gradlew lint`
- 已通过：`./gradlew assembleDebug`
- 已补充：`SearchCitiesUseCase`、`GetCurrentWeatherUseCase`、`DeleteSavedCityUseCase` 单元测试。
- 已检查：`domain/src/main/java` 没有 Android、RxJava、Retrofit 或 ORMLite import。
- 暂未改变现有 UI 行为。

退出标准：

- Presenter 可以通过兼容适配层调用 UseCase；实际 data 适配层在 Phase 2 接入。
- Domain 层没有 Android import。

## Phase 2：在旧存储/网络外建立 Data 层门面

目标：把现有 Retrofit、ORMLite、SharedPreferences 隔离到 clean 接口之后。

任务：

- 创建满足 domain repository 契约的 data repository 实现。
- 将 API 响应映射移动到专门的 mapper 类或函数。
- 将 ORMLite 访问隐藏在 local data source 后。
- 将偏好设置访问隐藏在 settings data source 后。
- 为 API 边界场景补 mapper 测试。
- 用 UseCase 替换 Presenter 对 DAO 和 preference helper 的直接访问。

验证：

- Mapper 单元测试。
- 使用 fake remote/local source 测试 Repository。
- 现有应用 smoke test。

退出标准：

- UI/presentation 层不再 import DAO、Retrofit service、API DTO 或 `PreferenceHelper`。
- Repository 行为可以脱离 Android UI 测试。

## Phase 3：Kotlin、Coroutines 和 Flow

目标：用结构化并发替代天气和城市流程中的 RxJava。

任务：

- 在项目中启用 Kotlin。
- 添加 coroutines 和 Flow 依赖。
- 将 domain use case 转为合适的 `suspend` 函数或 `Flow`。
- 按垂直切片逐步转换 repository 实现。
- 从已迁移流程中移除 RxJava。
- 使用 test dispatcher 补 coroutine 测试。

推荐顺序：

- 天气加载/刷新流程
- 已保存城市列表流程
- 城市搜索流程
- 设置/当前城市流程

退出标准：

- 已迁移的 presentation/data 路径不再使用 RxJava。
- 面向 View 的 API 基于 suspend/Flow。

## Phase 4：Hilt 依赖注入

目标：用应用级依赖装配替代手写和散落的 Dagger component 创建。

任务：

- 为 Application 添加 `@HiltAndroidApp`。
- 为 Android 入口添加 `@AndroidEntryPoint`。
- 将构造函数改为 `@Inject` 注入。
- 添加网络、数据库、data source、repository、dispatcher 的 Hilt module。
- 移除 Presenter 内部创建 Dagger component 的代码。

验证：

- debug 和 release 编译通过。
- ViewModel/Repository 使用 fake binding 或等价方案进行测试。

退出标准：

- presentation 类中不再手动创建 Dagger component。
- 生产依赖全部通过构造函数注入或 module 提供。

## Phase 5：ViewModel 和 StateFlow 表现层

目标：用生命周期感知的状态持有者替换 MVP Presenter。

任务：

- 新增 `HomeViewModel`、`CitySearchViewModel`、`SavedCitiesViewModel`。
- 定义不可变 UI state：
  - loading
  - content
  - empty
  - refreshing
  - error
- 显式建模 UI event。
- 暂时保留 XML 页面，从 Fragment 中观察 ViewModel 状态。
- 每个页面完全迁移后移除对应 Presenter。

验证：

- ViewModel 单元测试覆盖加载、刷新、错误、城市选择和删除。
- 每个已迁移页面完成手动 smoke test。

退出标准：

- 已迁移页面不再保留 Presenter。
- Fragment/Activity 只负责绑定 UI 和转发用户事件。

## Phase 6：Room 和 DataStore

目标：用现代 Jetpack 持久化替换旧存储。

任务：

- 设计天气、预报、空气质量、生活指数、城市数据的 Room entity 和 DAO。
- 为内置 `city.db` 制定迁移或导入策略。
- 用 Room data source 替换 ORMLite data source。
- 用 DataStore 替换 SharedPreferences。
- 添加数据库 schema 迁移测试。

验证：

- DAO 测试。
- Migration 测试。
- Settings repository 测试。

退出标准：

- ORMLite 和旧 preference helper 不再出现在 active code 中。
- 需要观察变化的数据源对外暴露 Flow。

## Phase 7：Compose UI 和 Material 3

目标：在保留 ViewModel 契约的前提下，将 XML UI 迁移到 Compose。

任务：

- 建立小型 design system：颜色、字体、间距、天气业务组件。
- 基于现有 ViewModel 构建 Compose 页面。
- 按页面逐步迁移：
  - 城市搜索
  - 已保存城市/抽屉替代页面
  - 首页天气页面
- 用 Compose host 替换 Activity/Fragment XML。
- 补充无障碍标签、加载状态和错误状态。

验证：

- 手动响应式检查。
- 高价值页面补截图测试。
- 检查无障碍 lint 和 content description。

退出标准：

- 新的用户可见页面使用 Compose。
- XML 只保留在计划删除的遗留代码中。

## Phase 8：依赖和模块清理

目标：移除过时库，简化构建结构。

任务：

- 移除 RxJava、ButterKnife、ORMLite、旧 support 时代依赖和未使用资源。
- 移除未使用的 `library`/`widget` 代码，或将仍有价值的部分迁入 `core` 包/模块。
- 将依赖声明迁入 version catalog。
- 在可行范围内启用更严格的 lint 和编译告警。
- 只有当 clean 包边界稳定后，才考虑进一步模块化。

验证：

- 完整验证命令集。
- Release build。
- 依赖报告审查。

退出标准：

- active code 中没有未使用的遗留框架。
- 构建文件能清晰表达现代化技术栈。

## 建议 PR 拆分

1. 稳定遗留代码并清理 lint。
2. 新增 domain models、repository contracts 和 use cases。
3. 在现有 network/db/preferences 外建立 data repository 门面。
4. 加固天气 mapper 并补 mapper 测试。
5. 启用 Kotlin 和 coroutines。
6. 将天气流程迁移到 suspend/Flow。
7. 接入 Hilt app setup 和 repository binding。
8. Home ViewModel + XML 绑定。
9. City search ViewModel + XML 绑定。
10. 天气和城市数据迁移到 Room。
11. 设置迁移到 DataStore。
12. Compose 城市搜索页。
13. Compose 已保存城市页。
14. Compose 首页天气页。
15. 移除遗留框架、未使用模块和资源。

## 每个阶段的完成定义

- 应用可以构建并启动。
- 受影响行为有单元测试，或明确记录暂缓测试的原因。
- 没有新增主线程 IO。
- Domain 代码不依赖 Android framework。
- 新改代码 lint 干净。
- 当范围变化时同步更新迁移计划。

## 参考文档

- Android app architecture: https://developer.android.com/topic/architecture
- Android architecture recommendations: https://developer.android.com/topic/architecture/recommendations
- Jetpack Compose: https://developer.android.com/develop/ui/compose
- Hilt on Android: https://developer.android.com/training/dependency-injection/hilt-android
- Room: https://developer.android.com/training/data-storage/room
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
