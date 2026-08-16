# MinimalistWeather 项目协作指南

本文件适用于仓库根目录及所有子目录。修改代码前先阅读相关实现；优先完成能够解决根因的最小改动，并保持当前架构、命名和行为一致。

## 项目概览

MinimalistWeather 是 Kotlin-first、Compose-first 的 Android 天气应用。

- 包名与 `applicationId`：`cn.byronlab.weather`
- Android：`minSdk 23`、`targetSdk 37`、`compileSdk 37`
- 构建：Gradle 9.4.1、AGP 9.2.0、JDK 17、Kotlin 2.3.21
- UI：Jetpack Compose + Material 3，单 Activity、edge-to-edge
- 架构：Clean Architecture + 单向数据流
- 状态：`ViewModel` + 不可变 `StateFlow<HomeUiState>` + `HomeUiEvent` + `HomeUiEffect`
- DI：`:app` / `:presentation` 使用 Hilt，`:data` / `:domain` 使用 Dagger 构造注入，统一通过 KSP 生成代码
- 异步：Kotlin Coroutines、`suspend`、`Flow`
- 数据源：Open-Meteo Geocoding、Forecast、Air Quality
- 本地状态：DataStore Preferences；后台刷新使用 WorkManager
- 性能：Baseline Profile、Startup Profile、Macrobenchmark

依赖、插件、Android SDK 与应用版本的唯一版本源是 `gradle/libs.versions.toml`。迁移状态和阶段记录见 `docs/modern-clean-architecture-plan.md`。

## 模块与依赖边界

| 模块 | 职责 | 允许的项目依赖 |
| --- | --- | --- |
| `:domain` | 不可变业务模型、Repository 契约、UseCase、`DomainResult` / `DomainError`、纯业务规则 | 无 |
| `:data` | Open-Meteo client/DTO/parser/mapper、本地城市目录、DataStore、Repository 实现 | `:domain` |
| `:presentation` | Compose 页面、ViewModel、UDF 状态、UI mapper、天气视觉引擎 | `:domain` |
| `:app` | `Application`、`Activity`、Hilt composition root、Android 定位、WorkManager、应用资源 | `:presentation`、`:data`、`:domain` |
| `:baselineprofile` | Baseline/Startup Profile 生成和冷启动 Macrobenchmark | 以 `:app` 为测试目标 |

必须保持以下方向：

```text
:baselineprofile --test target--> :app
:app ---------------------------> :presentation ---> :domain
:app ---------------------------> :data ---------> :domain
:app ---------------------------> :domain
```

边界规则：

- `:domain` 不得依赖 Android framework、Compose、Hilt Android、DTO、DataStore 或其他模块。
- `:data` 不得依赖 `:app` 或 `:presentation`；网络响应和持久化数据必须经 mapper 转换为 domain model。
- `:presentation` 不得依赖 `:data` 或 `:app`；只通过 domain 契约访问业务能力。
- `:app` 是依赖装配边界，不承载领域逻辑或网络数据映射。
- 只有出现被多个模块稳定复用且职责明确的能力时，才考虑新增 `core` 模块；不要恢复泛化的 `:library` 模块。

## 实现约定

- 所有新生产与测试代码使用 Kotlin。
- ViewModel 只暴露不可变 `StateFlow<UiState>`；用户操作使用明确的 event，消息等一次性行为使用 effect。
- Compose 页面尽量无状态；不要在 Composable 中访问 Repository、执行阻塞 I/O 或承载领域规则。
- Repository / UseCase 新契约使用 `suspend` 或 `Flow`，禁止 `GlobalScope`、裸线程和主线程阻塞调用。
- 可恢复失败使用 `DomainResult` / `DomainError` 表达，不使用 `null`、异常文本或 UI 文案充当跨层错误协议。
- 优先使用构造函数注入。Hilt module 只负责 composition root 无法直接构造的对象和接口绑定。
- 引入新依赖必须服务于当前明确需求；只有需要离线天气缓存、历史数据或更大本地城市库时才考虑 Room。
- remote DTO 使用默认值、nullable 字段和忽略未知字段兼容 API 演进；异常字段、缺失字段及降级规则集中在 data mapper。
- Open-Meteo 的 WMO code 在 data 层映射为稳定的 domain `WeatherCondition`；UI 与天气视觉不得通过中文/英文天气文案反推天气类型。
- 天气场景、图层、动画、图标和视觉 token 归属 `presentation.weatherui`；新增天气视觉能力优先扩展该包的既有协议。
- 当前城市、最近城市、已添加城市是不同状态；修改选城流程时不得把“浏览/预览城市”隐式等同于“添加城市”。
- 设备定位实现属于 `:app`，domain 只保留位置模型和契约；不要把 `Context`、`Location`、`Resources` 或 Android View 传入 domain。
- 前台自动刷新由页面生命周期驱动，后台刷新由唯一 WorkManager 周期任务驱动；WorkManager 间隔不是精确定时器。
- `app/src/main/generated/baselineProfiles/` 是生成产物，不手工维护。

## 禁止恢复的遗留实现

除非任务明确要求重新评估并获得确认，不得引入：

- 旧 XML/MVP 页面、Presenter/Fragment active 路径；
- 旧 MI/环境云接口、旧 `city.db`；
- RxJava、Retrofit、ORMLite、FastJson、ButterKnife、SmartRefresh；
- 旧 Dagger component、Stetho、`:widget` 或 `:library` 模块；
- 以字符串天气描述驱动业务或视觉分支的做法。

## 测试要求

逻辑变更必须新增或调整测试，并优先运行受影响模块的最小测试集。

- Domain UseCase：使用确定性输入做纯 JVM 单元测试。
- Data mapper/parser：覆盖正常响应、缺失字段、空列表、畸形响应和未知字段。
- Repository：覆盖本地/网络组合、缓存、持久化、错误分类和降级行为。
- ViewModel：使用 coroutine test 覆盖加载、刷新、搜索、空状态、错误状态和取消/时序行为。
- 天气视觉协议：覆盖天气条件、昼夜、云量、降水、雾、雷暴和图层选择；避免仅依赖肉眼回归。
- Compose 仪器或截图测试仅在布局/交互风险较高且单元测试不足时增加。
- 启动链路或性能配置变更时，按需在连接的设备上重新生成 Baseline Profile，并在稳定真机环境复测；不从单次模拟器结果外推真实收益。

常用验证命令：

```bash
./gradlew :domain:test
./gradlew :data:test
./gradlew :presentation:test
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

完整交付前运行：

```bash
./gradlew test lint assembleDebug
```

生成 Baseline Profile 需要已连接且可用的测试设备：

```bash
./gradlew :app:generateBaselineProfile
```

不要声称未执行的构建、测试、lint、设备验证或性能结果已经通过。若无法验证，交付时说明未验证项、原因和风险。

## 文档同步

- 架构阶段、模块边界、迁移范围或核心技术方案发生实质变化时，更新 `docs/modern-clean-architecture-plan.md`。
- 用户可见能力、运行要求、模块列表或主要数据源变化时，更新 `README.md`。
- 保持文档描述与 active source/Gradle 配置一致；历史实现只能作为背景，不得写成当前能力。

## 完成标准

- 目标行为已实现，修改范围聚焦，无无关重构。
- 分层和依赖方向未被破坏。
- 相关测试已补充并通过，或已明确未验证原因。
- 文档与代码事实一致。
- 未主动执行 commit、push、merge、rebase、release 或 deploy。
