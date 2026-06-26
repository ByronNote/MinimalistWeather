# Codex 项目规则

## 现代化改造目标

本项目将从旧版 Java/XML/MVP Android 应用，逐步迁移为现代化 Clean Architecture Android 应用。

目标架构：

- `domain`：业务模型、Repository 契约、UseCase、纯业务规则
- `data`：网络、本地存储、偏好设置、DTO、实体、Mapper、Repository 实现
- `presentation`：ViewModel、不可变 UI 状态、UI 事件、Compose 页面
- `app`：依赖装配、导航宿主、Android 入口

目标技术栈：

- Kotlin 优先；迁移期间未触碰的旧代码可以暂时保留 Java
- Jetpack Compose + Material 3 用于新 UI
- ViewModel + StateFlow 管理表现层状态
- Kotlin Coroutines + Flow 处理异步任务
- Hilt 作为依赖注入框架
- Room 作为本地关系型数据存储
- DataStore 管理偏好设置和配置
- Retrofit + OkHttp 处理 HTTP API
- JSON 方案在 API 模型迁移时从 Kotlin serialization 或 Moshi 中选定
- Compose-first 页面使用 Navigation
- 按需使用 JUnit、Kotlin coroutines test、Turbine、MockK 或同类测试工具

## 迁移原则

- 渐进式迁移，避免一次性大重写。
- 每次有意义的改动后都要保持应用可运行。
- 除非任务明确要求改变行为，否则保持现有行为不变。
- 优先在旧代码外包一层适配器或门面，再逐步替换内部实现。
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

第一阶段迁移前或迁移过程中优先修复：

- 由 eager `Observable.just(...)` 导致的主线程数据库/文件 IO。
- 天气 mapper 对缺失或异常字段过于脆弱，容易崩溃。
- 使用已废弃且语义错误的 `Date#getMonth()` 和 `Date#getDay()`。
- library manifest 中注册 app 层类的问题。
- 全局允许明文网络；应收敛到必要域名或迁移到 HTTPS。
- lint 配置吞掉真实错误的问题。

## 文档约定

`docs/modern-clean-architecture-plan.md` 是迁移路线图。每当阶段完成、拆分或范围发生实质变化时，都要同步更新该文档。
