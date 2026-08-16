# 现代化 Clean Architecture 迁移计划

最后更新：2026-08-14

## 目标

将 MinimalistWeather 现代化为一个可维护的 Kotlin-first Android 应用，采用 Clean Architecture、单向数据流和当前主流 Jetpack 模式，并在迁移过程中始终保持应用可构建、可运行、可发布。

目标最终状态：

- Kotlin-first 代码库
- 清晰的 `domain`、`data`、`presentation` 分层
- Jetpack Compose + Material 3 UI
- ViewModel + 不可变 `StateFlow` UI 状态
- 使用 Coroutines + Flow 替代 RxJava
- 使用 Hilt 进行依赖注入
- 使用 Open-Meteo 作为天气、城市搜索和空气质量数据源
- 使用内置全球热门城市列表作为本地城市库
- 使用 DataStore 偏好设置；Room 仅在需要离线缓存或更大本地城市库时引入
- Mapper、UseCase、Repository、ViewModel 具备有效测试覆盖

## 当前状态概览

active app 当前使用：

- AGP 9.2.0、Gradle 9.4.1、Kotlin 2.3.21
- Jetpack Compose + Material 3
- Compose-first 单 Activity 结构：首页、选择城市、城市搜索、设置
- 天气状态驱动动态背景、Hero 首屏、今日摘要、24 小时预报、7 日预报、AQI 和可展开详情
- `:presentation` 内以 `presentation.weatherui` 包级引擎集中管理天气场景协议、视觉 Token、图层、特效和图标
- Hilt + KSP
- ViewModel + `StateFlow<HomeUiState>` + `HomeUiEvent` + `HomeUiEffect`
- Kotlin Coroutines
- `:domain` 独立纯 Kotlin/JVM 模块
- `:data` 独立 Android library 模块
- `:presentation` 独立 Android library 模块
- `:baselineprofile` 独立性能测试模块，生成 Baseline Profile / Startup Profile 并提供冷启动 Macrobenchmark
- `:app -> :presentation -> :domain`、`:app -> :data -> :domain` 与 `:app -> :domain` 的 active 依赖方向
- `:app` 仅保留 Application、Activity、依赖装配和 Android 资源入口
- 插件、依赖和 Android SDK/应用版本已集中到 Gradle Version Catalog
- OkHttp 5.3.0 + Kotlinx Serialization 1.11.0
- Open-Meteo Geocoding + Forecast + Air Quality
- 内置 Kotlin 全球热门城市列表
- 旧 XML/MVP 用户界面、旧 Dagger component、ButterKnife、SmartRefresh、旧 RecyclerView helper、旧状态栏 helper、`widget` 模块已移除

2026-08-14 启动性能优化结果：

- 移除启动闪屏额外的 280ms 退出缩放/淡出动画，保留 Android 12+ SplashScreen 的系统退出行为。
- `OpenMeteoWeatherRepository` 的缓存读取、Kotlinx Serialization 解析和天气映射统一切到 IO dispatcher，避免在主线程解析天气 JSON。
- 热门城市和最近城市与首屏天气并发读取；后台刷新调度与已添加城市天气等待首屏天气结束，其中当前城市复用首屏结果，避免缓存重复解析。
- WorkManager 改为按需初始化，Hilt 构造后台刷新调度器时不再提前创建 WorkManager。
- 新增 `:baselineprofile` 模块，通过 `BaselineProfileRule` 采集真实启动路径，并用 `StartupTimingMetric` 对比无编译和 Baseline Profile 冷启动。
- release 开启 R8 压缩优化，生成的 Startup Profile 可参与 DEX 布局，Baseline Profile 以 `assets/dexopt/baseline.prof` 打包。
- Pixel 10 Pro XL API 37 模拟器的 Macrobenchmark 波动较大，当前结果不足以证明 Baseline Profile 的相对收益；发布前应在固定状态的真机上复测，不使用模拟器结果外推用户设备。

当前 data 状态：

- 城市搜索：先查内置全球热门城市列表，再查 Open-Meteo Geocoding。
- 天气数据：Open-Meteo Forecast。
- 空气质量：Open-Meteo Air Quality。
- 当前城市偏好使用 DataStore Preferences，并通过 `SharedPreferencesMigration` 兼容旧 SharedPreferences 数据。
- “已添加城市”使用独立 DataStore 记录持久化稳定城市唯一标识与天气定位载荷，并与当前城市、最近访问城市分开管理；浏览城市不会自动加入。
- 当前定位通过 app 层系统 `LocationManager` / `Geocoder` 实现，经 domain 契约映射为 Open-Meteo 可用城市，不与当前选择城市状态混用。
- 旧 MI/环境云 API、Retrofit/RxJava、ORMLite、FastJson、旧 `city.db` 和 legacy data adapter 已移除。

Phase 0 已处理的基线风险：

- 启动、城市选择和抽屉城市列表流程中的 eager `Observable.just(...)` 已改为延迟执行。
- 天气数据映射已补充缺失字段、格式异常和空列表防御。
- 日期转换已移除已废弃且结果错误的 `Date#getMonth()` / `Date#getDay()` 用法。
- library/widget manifest 与 namespace 问题已修复，lint 不再通过 `abortOnError false` 吞掉真实错误。
- 明文网络曾从全局允许收敛到必要 legacy API 域名；后续 active 数据源已迁移到 HTTPS Open-Meteo。

Phase 1 已建立的基线能力：

- 已新增独立 `:domain` Kotlin/JVM library 模块，`app` 已依赖该模块。
- 已新增不可变 domain model、Repository 契约、UseCase、`DomainResult` / `DomainError`。
- Domain 层不依赖 Android framework、RxJava、Retrofit、ORMLite 或 UI 类型。
- Repository / UseCase active 契约已迁移为 Kotlin `suspend` / `Flow`。

2026-08-01 一次性重构结果：

- 新 launcher 为 `cn.byronlab.weather.app.MainActivity`，使用 Compose `setContent`。
- 首页、城市列表、设置页和城市搜索合并为 Compose-first 单 Activity 体验。
- `HomeViewModel` 通过 `HomeUiEvent` 接收用户操作，通过不可变 `HomeUiState` 驱动 UI。
- UseCase 和 data repository 使用 Dagger 构造注入；`:app` 的 Hilt module 只装配 repository 契约、OkHttp、DataStore 和 dispatcher。
- data 层将 Open-Meteo JSON 解析为 response DTO，再通过 mapper 映射到 domain model；presentation mapper 将 domain model 映射到 Compose UI model。
- `:widget` 模块已从 Gradle 工程中移除。

2026-08-02 UDF / suspend 契约收紧结果：

- `:domain` 启用 Kotlin/JVM，Repository / UseCase 改为 `suspend` / `Flow` 契约。
- Repository / UseCase 已形成 Kotlin `suspend` / `Flow` 契约，并在 repository 内部切换 IO dispatcher。
- `HomeViewModel` 不再手动包同步 UseCase；改用可取消 job、搜索 debounce、sealed load state 和一次性 `HomeUiEffect`。
- 已补充 Kotlin coroutine test：domain use case 测试和 `HomeViewModel` UDF 测试。

2026-08-02 Open-Meteo 数据源切换结果：

- 新增 `data/openmeteo`：Open-Meteo client、城市编码、本地热门城市库、天气码映射和 JSON 边界处理。
- `WeatherRepository` active 实现切到 Open-Meteo Forecast + Air Quality。
- `CityRepository` active 实现切到本地热门城市列表 + Open-Meteo Geocoding。
- 启动逻辑会把空城市或旧城市 id 自动替换为 Open-Meteo 默认城市。
- 已删除旧 ORMLite 城市/天气库、旧 `city.db`、旧 MI/环境云 HTTP、FastJson DTO、RxJava bridge 和 legacy mapper。
- 已删除 app 中 RxJava、Retrofit、ORMLite、FastJson、旧 converter 依赖。

2026-08-02 DataStore 设置迁移结果：

- `SettingsRepository` active 实现切到 DataStore Preferences。
- DataStore 初始化包含旧 SharedPreferences 文件迁移，保留 `current_city_id`。
- 已删除 `PreferenceHelper`、`WeatherSettings` 和旧 settings repository。
- 已补充 DataStore settings repository 单元测试。

2026-08-03 Open-Meteo DTO / Mapper 拆分结果：

- 已新增 Open-Meteo response DTO、JSON parser、city mapper 和 weather mapper。
- `OpenMeteoCityRepository` 只负责本地/远程搜索编排、合并、去重和错误归类。
- `OpenMeteoWeatherRepository` 只负责城市解析、调用 Forecast/Air Quality API、解析 DTO 和映射结果。
- 已补充 Open-Meteo parser/mapper 单元测试，覆盖空 geocoding 响应、无效城市过滤、完整天气/AQI 样例和缺失字段容错。

2026-08-03 Data module 拆分结果：

- 已新增独立 `:data` Android library 模块。
- Open-Meteo client、DTO、JSON parser、mapper、本地热门城市库和 repository 实现已从 `:app` 迁移到 `:data`。
- DataStore settings repository 及 data 层单元测试已迁移到 `:data`。
- `:data` 只依赖 `:domain`、OkHttp、DataStore、Coroutines 和测试依赖，不依赖 `:app`、Compose、Hilt 或 presentation。
- `:app` 保留 Hilt 装配、DataStore 初始化、OkHttp client 创建和 Android 入口。

2026-08-03 Presentation module 拆分结果：

- 已新增独立 `:presentation` Android library 模块。
- Compose 页面、Material 3 主题、`HomeViewModel`、不可变 UI state/event/effect 和 UI mapper 已从 `:app` 迁移到 `:presentation`。
- ViewModel 与 UI mapper 单元测试已同步迁移到 `:presentation`，测试所有权跟随生产代码。
- `:presentation` 只依赖 `:domain` 和表现层所需框架，不依赖 `:app` 或 `:data`。
- `:app` 通过 `:presentation` 使用 UI 和 ViewModel，只保留 Application、Activity、Hilt 装配及应用资源。

2026-08-03 Library module 移除结果：

- 已确认 `:library` 只剩 `DateConvertUtils` 和未使用的 `NetworkUtils`，不再具备独立模块职责。
- 未被 UI 消费的 `WeatherUiModel.publishTime` 及其日期格式化依赖已删除。
- 已从 Gradle 工程移除 `:library`，当前模块收敛为 `:app`、`:presentation`、`:data`、`:domain`。
- 后续只有出现被多个模块稳定复用且职责清晰的能力时，才建立对应 `core` 模块，不再使用泛化 `library` 容器。

2026-08-03 Version Catalog 迁移结果：

- 已新增 `gradle/libs.versions.toml`，统一管理 AGP、Kotlin、KSP、Hilt、Compose BOM、AndroidX、Coroutines、OkHttp 和测试依赖版本。
- Android SDK、applicationId、versionCode 和 versionName 也通过 catalog 统一读取。
- 根工程和四个模块已迁移到插件 DSL 与类型安全 catalog accessor。
- 已删除旧 `dependencies.gradle`、`rootProject.ext` 依赖映射和未使用的 JitPack 仓库。
- `settings.gradle` 统一管理 Google Maven 与 Maven Central，并禁止模块私自声明仓库。

2026-08-03 App 启动与调试链清理结果：

- `WeatherApplication` 已从 Java 迁移为 Kotlin，启动逻辑只保留 debug build 的 StrictMode 检查。
- 已移除 Stetho 初始化、OkHttp Stetho interceptor、debug/release helper、依赖和 ProGuard 规则。
- 已删除未使用的 `LOG_DEBUG` / `STETHO` BuildConfig 字段和 `ACCESS_NETWORK_STATE` 权限。
- `:app` production source 已不再包含 Java 文件；网络调试使用 Android Studio Network Inspector，无需注入运行时调试库。

2026-08-03 Domain Kotlin-first 收口结果：

- City、Weather、CurrentWeather、ForecastDay、AirQuality 和 LifeIndex 已迁移为不可变 Kotlin `data class`。
- `Weather` 的可选块显式使用 nullable 类型，forecast/life index 列表收紧为非空只读列表。
- `DomainResult` 已从 nullable data/error Java 容器迁移为可穷举的 sealed `Success` / `Failure` 契约。
- 已删除仅用于 Java 兼容的 domain `Unit.INSTANCE`，repository/use case 统一使用 Kotlin `Unit`。
- app、data、domain、presentation 的手写生产与测试源码均已不再包含 Java 文件。
- 已补充 `DomainResult` 分支单元测试，并将 use case、repository、ViewModel 测试改为验证具体结果分支。

2026-08-03 Hilt 装配拆分结果：

- 已删除同时承担网络、存储、Repository、UseCase 和 dispatcher 装配的 `CleanArchitectureModule`。
- 8 个 UseCase 与 4 个 data repository 已改为 Dagger `@Inject constructor`，不再维护重复的 `@Provides` 构造代码。
- `:domain` / `:data` 使用纯 JVM Dagger + KSP 生成构造工厂，不依赖 Android framework 或 Hilt Android API。
- `:app` 装配已拆分为 `CoroutineModule`、`NetworkModule`、`DataStoreModule` 和 `RepositoryModule`。
- Repository 接口与实现通过 `@Binds` 声明，singleton scope 由 composition root 统一控制。
- `IoDispatcher` qualifier 归属 `:data` 的注入边界，data 不再引用 app 内部类型。

2026-08-04 网络与 JSON 边界现代化结果：

- OkHttp 已从 3.12.13 升级到 5.3.0，并使用 OkHttp 5 的 URL、响应码、响应体和 Kotlin Duration API。
- 已引入 Kotlinx Serialization 1.11.0 及 Kotlin serialization 编译插件。
- Open-Meteo response DTO 已改为 `@Serializable` 强类型模型，snake_case 字段使用 `@SerialName` 映射。
- JSON parser 现在只负责原始字符串到 response DTO 的反序列化，并通过默认值、nullable 字段、`ignoreUnknownKeys` 和 `coerceInputValues` 兼容缺失或新增字段。
- 已删除手写 `JSONObject` / `JSONArray` 取值工具和 `org.json` 测试依赖；repository 将反序列化异常统一映射为 domain error。
- parser/mapper 测试已覆盖未知字段、缺失字段、畸形数值、无效城市和完整天气/AQI 样例。
- 已通过 `./gradlew test`、`./gradlew lint`、Debug/Release 构建和模拟器天气首页冷启动回归；Forecast 与 Air Quality 实际响应可正常解析并渲染。

2026-08-04 天气 UI 包级引擎化结果：

- 已在 `:presentation` 内建立 `presentation.weatherui` 包级天气视觉引擎，暂不增加新的 Gradle 模块。
- `WeatherSceneSpec` 统一表达昼夜、云量、降水类型、降水强度、雾和雷暴，首页不再直接持有基础视觉分类或昼夜布尔值。
- 场景协议已进一步扩展为云量层级、持续/阵性/飘雪模式、冻结降水、雷电强度和风级；当前风速只影响首页主场景运动，不改变领域天气类型。
- domain 新增稳定的 `WeatherCondition` 语义枚举；Open-Meteo WMO 数字码由 data 层直接映射为 domain 条件，presentation 再穷举转换为展示文案和 `WeatherSceneSpec`。
- 当前天气、逐小时预报和每日预报已移除字符串天气条件契约，生产代码不再通过中文天气名称反向判断天气类型。
- 天气背景、图片图层、Canvas 降水/雾/雷暴特效、热门城市缩略场景、视觉 Token 和天气图标已从 `HomeScreen` 迁入引擎包。
- 大部晴朗与少云、持续雨与阵雨、普通雨与冻雨、持续雪与阵雪/雪粒、普通雷暴与冰雹雷暴现在使用不同的场景组合；全部主要天气家族均具有独立昼夜配色。
- 引擎新增薄云、阴云和积雨云三张透明素材；场景切换采用淡入过渡，云层、雾带、雨雪和闪电使用低幅度循环动画，避免抢夺天气数据的视觉层级。
- 小时预报和每日预报 UI model 现在携带稳定场景协议，Compose 页面不再根据中文天气文案决定图标。
- 天气条件到场景协议的转换仍由 presentation mapper 负责，引擎不依赖 domain、data 或 Open-Meteo 类型。
- 场景图层和视觉 Token 测试已迁入引擎测试包，继续覆盖昼夜、云量、持续/阵性降水、冻结降水、雨雪、雾、雷电和降水强度。
- 雾景从规则等高横向椭圆带升级为前、中、后景不等尺度雾团：每个雾团采用单次连续径向羽化，避免半透明叠层产生格纹；雾团具有独立漂移方向和相位，并分别提供日间散射光与夜间冷灰空气透视。
- 雨景已移除固定步长、等长等粗的规则雨线，统一改为远、中、近三层非规则雨丝；强度控制密度、长度、透明度与空气雨幕，中到大雨逐级增强近景存在感，风级控制倾角，阵雨使用强弱脉冲，冻雨和昼夜场景分别使用独立色调。
- 强阵雨与暴雨进一步按阵雨等级拆分：强阵雨提升近景密度、脉冲峰值和下落速度，暴雨使用更高密度、更长雨丝、更厚空气雨幕与更深背景压暗，避免两者仅依靠倾角区分。
- 冰雪场景同步完成分级重构：小雪、中雪和大雪使用远中近三层随机雪场，并统一提高雪花数量、尺寸可见度和空气雪幕；强阵雪进一步增加横向漂移与脉冲峰值，雪粒改为更高密度的细小高速粒子，雨夹雪与冰雹分别增强冰粒密度、尺寸层次和高光，昼夜与阵性冰雪使用独立冷色背景。
- 雷暴闪电已移除静态常驻图形，普通雷暴约每 5.8 秒触发一次双脉冲，强对流场景约每 3.6 秒触发一次并带较弱余闪；六套不同落点、长度、方向和分叉数量的冷白电弧按周期轮换，只在闪电期间出现，并通过局部雷云照明和轻微环境提亮替代生硬的整屏闪白。

2026-08-05 自动与后台天气刷新结果：

- 设置页新增 15 分钟、30 分钟、1 小时和 3 小时四档自动刷新间隔，默认 30 分钟；设置通过 DataStore Preferences 持久化。
- 前台刷新由页面生命周期驱动：应用重新进入前台时立即静默刷新，持续停留时按所选间隔刷新，进入后台后停止前台循环任务。
- 后台刷新使用 WorkManager 2.11.2 的唯一周期任务和联网约束；修改间隔时通过 `ExistingPeriodicWorkPolicy.UPDATE` 更新原任务，避免重复调度。
- Open-Meteo Forecast 与 Air Quality 原始响应写入独立 DataStore 缓存；后台刷新成功后更新缓存，应用冷启动可优先渲染缓存并在后续刷新时更新。
- WorkManager 的周期表示允许执行的最短间隔，实际执行时刻仍由 Android 的联网、Doze 和电量策略决定，不作为精确定时器使用。
- 自动刷新间隔的 domain 契约、设置存储、调度 use case、前台生命周期行为和天气缓存均已补充单元测试。

2026-08-05 选址流程修复结果：

- “当前定位”卡片改用设备定位和反向地理编码结果，不再复用首页当前选择城市的天气状态。
- app 层负责运行时定位权限和 Android 系统定位实现；domain 只持有设备位置、当前位置 repository 和 use case 契约。
- 当前城市与收藏城市完成状态拆分；收藏列表使用独立 DataStore 顺序列表持久化，启动默认城市和后续选择城市都会自动加入收藏。
- 热门城市、收藏城市和搜索结果点击后会立即切回天气页并显示加载状态，天气网络请求在页面切换后继续执行。
- 已补充 ViewModel 时序测试、选城自动收藏 use case 测试、收藏持久化测试和当前位置映射测试。

2026-08-03 v2 Compose UI 设计落地结果：

- 已按 `docs/design.md` 和 `docs/img.png` 重做 Compose UI。
- 已从旧 TopAppBar + 抽屉列表改为 Compose-first 首页、城市、设置基础页面。
- 首页已实现天气动态背景、大号天气 Hero、Today Summary、Hourly Forecast、Canvas 温度曲线、7-Day Forecast、AQI 和默认折叠 More Details。
- 首页背景不使用固定图片资源，改为由 Compose Canvas 按天气状态动态绘制太阳、云、雨、雪等天气相关视觉层。
- Cities Tab 已展示收藏城市列表、当前城市天气摘要和添加城市入口。
- Settings Tab 已展示主题、温度单位、风速单位、通知开关和版本信息，并通过 `HomeUiEvent` 走单向数据流。
- `MainActivity` 已启用 edge-to-edge。

2026-08-03 v3 Compose UI 设计落地结果：

- 已按最新截图重做首页顶部和位置选择路径：顶部菜单、城市下拉、搜索入口、居中大号温度和半透明信息面板。
- 首页移除可见底部导航，改为通过顶部城市下拉进入“选择位置”，通过菜单进入设置。
- 选择位置页已实现当前定位、我的收藏、添加城市、横向热门城市卡片和“全部”入口。
- 热门城市页已按大洲分组展示本地全球热门城市，默认展开亚洲和欧洲，其余大洲折叠。
- 搜索页保持全屏 Compose 实现，使用本地城市库 + Open-Meteo Geocoding 的 UDF 搜索状态。
- 新增 `GetPopularCitiesUseCase` 和 `CityRepository.getPopularCities()`，让 presentation 通过 domain 契约消费本地热门城市库，不直接依赖 data internal catalog。
- 待补：热门城市卡片/列表中的小天气摘要目前是设计稿样式预览；如需真实实时温度，应新增批量城市天气摘要 UseCase 和缓存策略。

2026-08-05 v4 城市选择与添加流程：

- 首页右上角入口改为带加号角标的搜索图标，点击进入“选择城市”。
- “我的收藏”统一改名为“已添加城市”；选择城市页只展示当前定位和已添加城市，不再重复展示热门城市。
- 搜索城市页在空查询时按“搜索框、搜索历史、全球热门城市”展示；搜索历史保存最近访问城市，按最近优先去重并限制为 9 个，新增记录超过上限时淘汰最早一条。
- 点击已添加城市会立即回到天气页并在页面切换后加载；点击未添加城市则进入不落盘的天气预览。
- 天气预览隐藏城市下拉和普通右上角操作，提供关闭按钮与底部“添加城市”按钮；关闭会恢复原城市和来源页面，确认添加后才写入已添加城市并设为当前城市。
- `SetCurrentCityUseCase` 不再隐式添加城市；新增 `AddCityUseCase`，并将 CityRepository 契约拆为当前城市、已添加城市和最近访问城市三类职责。
- 已补充预览、关闭、确认添加、已添加城市直达、最近城市持久化和 use case 时序测试。

2026-08-05 v5 天气信息与城市网格精细化结果：

- 首页搜索加号角标改为透明背景；日出和日落使用带方向语义的独立 Canvas 图标，不再复用同一太阳图标。
- 每小时温度走势使用三次贝塞尔曲线连接，并用至少 8°C 的视觉温区约束小温差的纵向振幅，避免折线生硬或轻微温差被放大。
- 详细信息卡片统一为图标、类别、数值的纵向居中层级；气压展示不再附加单位。
- 全球热门城市页标题完成统一，六个大洲默认展开；大洲标题保留吸附和折叠动画，城市改为三列地标缩略图网格且不展示模拟天气。
- 搜索历史改为三列状态卡片，展示城市、国家和是否已添加；九个精选热门城市改为三列地标天气卡片，柏林位于巴黎之后、纽约之前。
- 天气原始响应缓存由单城市全局键升级为按 city id 分区，保留旧单城市缓存读取兼容；已添加城市启动时并发读取各自缓存，缺失时请求网络并持久化，选择城市页可展示每座城市自己的天气摘要。
- 选择城市页的已添加城市由多张独立卡片收敛为单一列表容器；每行统一展示城市、地区、天气、当前温度和最高/最低温，当前城市使用轻量底色标识，非当前城市保留对齐的删除入口。
- 多城市缓存隔离、单城市清理、已添加城市天气状态和热门城市默认展开规则已补充单元测试。

2026-08-06 v6 城市唯一性修复：

- 城市模型新增稳定 `uniqueId`，基于规范化后的国家、行政区和城市名识别同一城市；`cityId` 继续保留完整坐标、时区和显示载荷供天气请求使用。
- 已添加城市和搜索历史的 DataStore 从 ID 字符串列表升级为 `uniqueId + cityId` 记录，添加、删除、最近访问和旧数据读取均按 `uniqueId` 去重。
- 保留旧 `saved_city_ids`、`recent_city_ids` 兼容读取；下次写入时自动迁移到 v2 记录，旧数据中的语义重复城市会被归并。
- 搜索合并、已添加状态标记和已添加城市直达判断统一使用 `uniqueId`，避免定位、本地热门城市和 Open-Meteo 搜索载荷差异导致重复添加。

2026-08-05 Debug 天气 UI 测试台结果：

- Debug 包设置页新增“显示天气测试入口”开关，开关状态保存在独立调试偏好中；Release 包固定关闭且不展示开发与测试区域。
- 开启后，天气页右下角显示紧凑悬浮入口；入口可展开场景选择器，选中场景后仍可再次点击切换，并可随时恢复实时天气。
- 测试目录覆盖 domain 中全部 23 种已识别天气条件，并增加独立大风场景；24 种视觉类型均提供白天和夜间版本，共 48 个确定性场景。
- 场景预览使用本地合成的完整天气 UI 数据，不请求网络，也不写入真实城市、天气缓存或 ViewModel 状态；主背景、当前天气、24 小时预报、7 日预报、空气质量和详细信息可一起验收。
- 场景目录测试覆盖天气条件完整性、昼夜成对关系、id 唯一性和离线预览数据完整性。

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
    addedcities
```

## 目标框架选择

除非后续技术 spike 发现明确阻碍，否则默认采用：

- UI：Jetpack Compose、Material 3
- 状态：ViewModel、`StateFlow`、不可变 UI state data class
- 异步：Kotlin Coroutines、Flow
- DI：Hilt
- 本地城市库：内置全球热门城市 Kotlin catalog
- 偏好设置：DataStore Preferences
- 网络：OkHttp 5.3.0 + Open-Meteo HTTPS API
- JSON：Kotlinx Serialization 1.11.0 将原始响应解析为 Open-Meteo `@Serializable` response DTO，再通过 mapper 输出 domain model
- 持久化：Room 仅在需要天气离线缓存、历史数据或大型本地城市库时引入
- 导航：Compose Navigation；新 Compose-first 流程可评估 Navigation 3
- 后台任务：只有重新引入周期刷新时才使用 WorkManager

版本策略：

- 依赖版本已集中到 `gradle/libs.versions.toml`。
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
- 待补充：手动 smoke test：启动应用、加载默认城市、搜索城市、预览并添加城市、切换城市、删除已添加城市。

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
  - `ObserveAddedCitiesUseCase`
  - `SearchCitiesUseCase`
  - `SetCurrentCityUseCase`
  - `AddCityUseCase`
  - `RemoveAddedCityUseCase`
- 定义 domain error/result 类型。

验证：

- 已通过：`:domain:test`
- 已通过：`./gradlew test`
- 已通过：`./gradlew lint`
- 已通过：`./gradlew assembleDebug`
- 已补充：`SearchCitiesUseCase`、`GetCurrentWeatherUseCase`、`AddCityUseCase`、`RemoveAddedCityUseCase` 单元测试。
- 已检查：`domain/src/main/java` 没有 Android、RxJava、Retrofit 或 ORMLite import。
- 暂未改变现有 UI 行为。

退出标准：

- Presenter 可以通过兼容适配层调用 UseCase；实际 data 适配层在 Phase 2 接入。
- Domain 层没有 Android import。

## Phase 2：建立 Open-Meteo Data 层

状态：已完成，2026-08-02。

目标：通过 clean repository 契约接入 Open-Meteo，并用内置热门城市列表替换旧城市库。

任务：

- 已创建满足 domain repository 契约的 Open-Meteo repository 实现。
- 已新增内置全球热门城市 catalog。
- 已新增 Open-Meteo city id 编码/解码，确保当前城市设置可以携带坐标和展示信息。
- 已新增 Open-Meteo Forecast、Geocoding、Air Quality 请求封装。
- 已新增 Open-Meteo `@Serializable` response DTO、Kotlinx Serialization parser 和 mapper，repository 不直接做字段级映射。
- 已删除旧 ORMLite 城市库和天气缓存。
- 已删除旧 MI/环境云 HTTP、RxJava bridge、FastJson DTO 和 legacy mapper。
- 当前城市偏好通过 DataStore settings repository 管理。

验证：

- 已通过：Open-Meteo city codec/local catalog 单元测试。
- 已通过：Open-Meteo parser/mapper 边界样例单元测试。
- 已通过：Open-Meteo startup repository 单元测试。
- 已通过：`./gradlew :app:testDebugUnitTest`

退出标准：

- UI/presentation 层不 import Open-Meteo client、JSON、DAO、API DTO 或 `PreferenceHelper`。
- Repository 行为可以脱离 Android UI 测试。

## Phase 3：Kotlin、Coroutines 和 Flow

状态：active app 已完成。

目标：用结构化并发替代天气和城市流程中的 RxJava。

任务：

- 已在 `app` 启用 Kotlin。
- 已在 `:domain` 启用 Kotlin/JVM。
- 已添加 coroutines 和 Flow 依赖。
- `HomeViewModel` 对外暴露 `StateFlow<HomeUiState>`。
- Repository / UseCase 对 ViewModel 暴露 `suspend` / `Flow` API。
- active data repository 内部不再使用 RxJava。

推荐顺序：

- 天气加载/刷新流程
- 已保存城市列表流程
- 城市搜索流程
- 设置/当前城市流程

退出标准：

- 已迁移的 presentation/domain 路径不再使用 RxJava。
- 面向 View 的 API 基于 suspend/Flow。

## Phase 4：Hilt 依赖注入

状态：active app 已完成。

目标：用应用级依赖装配替代手写和散落的 Dagger component 创建。

任务：

- 已为 Application 添加 `@HiltAndroidApp`。
- 已为 Compose launcher Activity 添加 `@AndroidEntryPoint`。
- 已添加 repository、use case、dispatcher 的 Hilt module。
- 已移除旧 Presenter 内部创建 Dagger component 的 active 路径。

验证：

- debug 和 release 编译通过。
- ViewModel/Repository 使用 fake binding 或等价方案进行测试。

退出标准：

- presentation 类中不再手动创建 Dagger component。
- 生产依赖全部通过构造函数注入或 module 提供。

## Phase 5：ViewModel 和 StateFlow 表现层

状态：active app 已完成。

目标：用生命周期感知的状态持有者替换 MVP Presenter。

任务：

- 已新增 `HomeViewModel`。
- `HomeViewModel`、UI state/event/effect 和 UI mapper 已迁入独立 `:presentation` 模块。
- 已定义不可变 `HomeUiState` 和 `WeatherLoadState`，覆盖初始化、加载、刷新、内容、空状态和搜索。
- 已显式建模 `HomeUiEvent`。
- 已显式建模一次性 `HomeUiEffect`，用于 Snackbar 等副作用。
- 搜索事件已加入可取消 debounce，天气加载/刷新使用可取消 job 管理并发。
- 已移除旧 MVP Presenter/Fragment active 路径。

验证：

- 已补充 ViewModel coroutine 单元测试覆盖初始化加载和搜索 debounce。
- 待补充：刷新、错误、城市选择和删除测试。
- 每个已迁移页面完成手动 smoke test。

退出标准：

- 已迁移页面不再保留 Presenter。
- Fragment/Activity 只负责绑定 UI 和转发用户事件。

## Phase 6：Room 和 DataStore

状态：DataStore 已完成，Room 按需保留。

目标：用 DataStore 替换旧偏好设置；仅在存在明确离线缓存或大型本地数据需求时引入 Room。

任务：

- 已用 DataStore Preferences 替换 SharedPreferences/`PreferenceHelper`。
- 已配置旧 SharedPreferences 自动迁移。
- 如果需要离线天气缓存，再设计天气、预报、空气质量、生活指数 Room entity 和 DAO。
- 如果热门城市列表膨胀为大型离线库，再评估 Room 或打包 JSON 资源。
- 已添加 DataStore repository 测试；如引入 Room，再添加 schema migration 测试。

验证：

- 已通过 Settings repository 测试。
- 可选 Room DAO/Migration 测试。

退出标准：

- 旧 preference helper 不再出现在 active code 中。
- 需要观察变化的数据源对外暴露 Flow。

## Phase 7：Compose UI 和 Material 3

状态：active app 已完成。

目标：在保留 ViewModel 契约的前提下，将 XML UI 迁移到 Compose。

任务：

- 已建立 Compose Material 3 theme。
- Compose 页面和主题已迁入独立 `:presentation` 模块，`:app` 只负责承载和装配。
- 已实现 Compose 三 Tab：Home、Cities、Settings。
- 已实现天气状态驱动背景、Weather Hero、Today Summary、Hourly Forecast、Canvas 温度曲线、7-Day Forecast、AQI 和 More Details。
- 已实现城市列表 Tab、设置 Tab 和城市搜索对话框。
- 已用 Compose host 替换 Activity/Fragment XML。
- 已补充主要按钮 content description、加载状态和错误状态。

验证：

- 已通过 emulator smoke test：启动应用、Home Hero 首屏渲染、Today/Hourly/7-Day/AQI 可见、Cities Tab 可见、Settings Tab 可见且 crash buffer 为空。
- 已通过 lint content description 相关检查。
- 待补充：高价值页面截图测试。

退出标准：

- 新的用户可见页面使用 Compose。
- active app 不再使用 XML 页面布局。

## Phase 8：依赖和模块清理

状态：主要完成。

目标：移除过时库，简化构建结构。

任务：

- 已移除 ButterKnife、SmartRefresh、RxBinding、旧 Dagger active 路径和 `widget` 模块。
- 已移除旧 View helper、状态栏 helper、Fragment/RecyclerView helper 和未引用的 app 级遗留模型文件。
- 已移除 RxJava、Retrofit、ORMLite、FastJson、旧 converter、旧 `city.db` 和旧 data adapter。
- 已移除旧 SharedPreferences helper，settings repository 已迁移到 DataStore。
- 已拆分独立 `:presentation` 模块，Compose、ViewModel 和表现层测试不再由 `:app` 持有。
- 已移除只剩旧工具类的 `library` 模块及其过时网络状态检查实现。
- 已将依赖、插件和 Android 配置版本迁入 version catalog。
- 已移除 Stetho、旧 build type 调试对象注入和 Java Application 启动代码。
- 已删除 Compose active 路径不再引用的 XML/MVP 时代 drawable、dimension、string、style、天气背景资源和遗留 Fabric 配置。
- 已删除旧 `widget` module 残留目录、空 source set、空 package 和空资源目录，工程目录树只保留 active module 路径。
- 已将已保存城市流改为跟随 DataStore 当前城市变化的真实响应式 Flow，并移除 ViewModel 的重复快照查询。
- 已加固协程取消传播和损坏 city id 解码边界，避免取消被包装为业务失败或旧偏好数据导致启动崩溃。
- `Weather` 现在聚合通用 domain `City`，presentation 不再解析 Open-Meteo city id 来获取地区和坐标。
- 已将 OkHttp 升级到 5.3.0，并使用 Kotlinx Serialization 1.11.0 替代手写 `org.json` parser。
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
10. 天气、城市搜索和空气质量迁移到 Open-Meteo。
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
- Open-Meteo Forecast API: https://open-meteo.com/en/docs
- Open-Meteo Geocoding API: https://open-meteo.com/en/docs/geocoding-api
- Open-Meteo Air Quality API: https://open-meteo.com/en/docs/air-quality-api
