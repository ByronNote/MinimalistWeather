# Codex 项目规则

## 当前架构状态

本项目 active app 已迁移为 Compose-first Clean Architecture Android 应用。

当前事实：

- 包名和 applicationId：`cn.byronlab.weather`
- UI：Jetpack Compose + Material 3
- 状态：ViewModel + 不可变 `StateFlow<HomeUiState>` + 单向数据流
- DI：Hilt + KSP
- 异步：表现层使用 Kotlin Coroutines
- 模块：`:app`、`:domain`、`:library`
- 已移除旧 XML/MVP UI、旧 Presenter/Fragment active 路径、旧 Dagger component、ButterKnife、SmartRefresh 和 `:widget` 模块
- data 底层仍通过 legacy adapter 复用 Retrofit/RxJava、ORMLite、FastJson 和 SharedPreferences

目标架构：

- `domain`：业务模型、Repository 契约、UseCase、纯业务规则
- `data`：网络、本地存储、偏好设置、DTO、实体、Mapper、Repository 实现
- `presentation`：ViewModel、不可变 UI 状态、UI 事件、Compose 页面
- `app`：依赖装配、导航宿主、Android 入口

目标技术栈：

- 新 UI 和新表现层代码使用 Kotlin
- Jetpack Compose + Material 3 用于 UI
- ViewModel + `StateFlow` 管理表现层状态
- Kotlin Coroutines + Flow 处理异步任务
- Hilt 作为依赖注入框架
- Room 作为下一阶段本地关系型数据存储目标
- DataStore 作为下一阶段偏好设置目标
- Retrofit + OkHttp 处理 HTTP API；下一阶段将 legacy Rx API 改为 suspend API
- JSON 方案在 API 模型迁移时从 Kotlin serialization 或 Moshi 中选定
- Compose-first 页面按需使用 Navigation
- 按需使用 JUnit、Kotlin coroutines test、Turbine、MockK 或同类测试工具

## 迁移原则

- 每次有意义的改动后都要保持应用可运行。
- 除非任务明确要求改变行为，否则保持现有行为不变。
- legacy data 实现必须继续隔离在 repository adapter 和 mapper 后面。
- 引入新依赖必须有明确的迁移目的。
- 不要把领域逻辑写进 Android 类、ViewModel、Retrofit DTO、Room Entity 或 Compose UI。
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
- UI 操作用明确的 event/intent 表达。
- 可恢复的 domain/data 失败使用 sealed result/error 类型表达。
- remote DTO 和 Room Entity 必须通过专门的 mapper 映射为 domain model。
- 不要把 DTO、Room Entity、`Context`、`Resources` 或 Android View 传入 domain 代码。
- Compose 函数尽量保持无状态；状态持有者放在 ViewModel。
- 不要使用 `GlobalScope`、裸线程，或在主线程执行阻塞调用。
- RxJava 只能在明确的迁移范围内替换；同一条业务流不要长期并存两套异步实现。
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

- 用 Room 替换 ORMLite 天气缓存和城市库访问。
- 用 DataStore 替换 SharedPreferences/`PreferenceHelper`。
- 用 Retrofit suspend API 替换 RxJava 1 service 和 blocking bridge。
- 将 FastJson DTO 迁移到 Kotlin serialization 或 Moshi。
- 将依赖声明迁移到 version catalog。
- 删除未被 active app 使用的旧资源、工具类和构建声明。

## 文档约定

`docs/modern-clean-architecture-plan.md` 是迁移路线图。每当阶段完成、拆分或范围发生实质变化时，都要同步更新该文档。
