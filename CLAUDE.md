# CLAUDE.md

## Project Overview

SmartAdmin v3.0.0 — an enterprise-grade admin platform by [1024 Innovation Lab](https://www.1024lab.net). It is a monorepo containing:

| Directory | Description |
|-----------|-------------|
| `smart-admin-web-javascript/` | Frontend: Vue 3 + Vite + Ant Design Vue (JavaScript) |
| `smart-admin-api-java8-springboot2/` | Backend: Java 8 + Spring Boot 2.7 + MyBatis-Plus |
| `数据库SQL脚本/` | Database SQL scripts (MySQL) |

**Philosophy:** "High-quality code" is the core principle — clean naming, clear structure, no magic numbers, strong conventions.

---

## Frontend: `smart-admin-web-javascript/`

### Tech Stack
- **Vue 3.4** (Composition API, `<script setup>`)
- **Vite 5.2** (build tool)
- **Ant Design Vue 4.2** (UI library)
- **Pinia 2.1** (state management)
- **Vue Router 4.3** (hash mode)
- **vue-i18n 9** (internationalization)
- **Axios 1.6** (HTTP client)
- **Less** (CSS preprocessor)
- **Day.js** (date handling)
- **ECharts 5** (charts)
- **Crypto-js / sm-crypto** (encryption)

### Path Alias
- `/@/` maps to `src/` (configured in `vite.config.js` and `jsconfig.json`)

### Project Structure
```
src/
├── api/           # API modules (mirrors backend modules)
│   ├── system/    # login, employee, menu, role, department, position
│   ├── support/   # dict, file, config, code-generator, datatracer, etc.
│   └── business/  # oa/, goods/, category/
├── components/    # Shared components
│   ├── framework/ # Generic: enum-select, icon-select, area-cascader, etc.
│   ├── support/   # Dict, file-upload, data-tracer, table-operator
│   ├── system/    # Dept-tree-select, employee-select, menu-tree-select
│   └── business/  # Business-specific selectors
├── config/        # App default configuration
├── constants/     # All enums & magic-value constants
├── directives/    # Custom Vue directives (v-privilege)
├── i18n/          # zh-CN and en-US translations
├── layout/        # 5 layout variants + shared components
├── lib/           # Core libraries (axios, encrypt, watermark, sentry)
├── plugins/       # Vue plugins (dict, privilege, enum)
├── router/        # Route definitions + dynamic route builder
├── store/         # Pinia stores
├── theme/         # Less variables, color palettes, global styles
├── utils/         # Utility functions (localStorage, string)
├── views/         # Page components (matched by dynamic routes)
├── App.vue        # Root component with a-config-provider
└── main.js        # Entry point — async init flow
```

### Application Initialization (`main.js`)
The bootstrap flow is intentionally different from most Vue apps:

1. Check localStorage for `USER_TOKEN`
2. **No token** → `initVue()` immediately (router has login page only)
3. **Has token** → `getLoginInfo()` calls `loginApi.getLoginInfo()` to fetch user info + menus, then `buildRoutes(menuRouterList)` registers dynamic routes, then `initVue()`

This avoids the common anti-pattern of mounting routes before backend data arrives.

### Routing (`src/router/index.js`)
- **Hash mode** (`createWebHashHistory`)
- **Static routes:** login, home, help-doc, 404, 403
- **Dynamic routes:** built from backend menu data via `buildRoutes(menuRouterList)`
  - Route `name` = `menuId.toString()` (NOT page name)
  - Route `meta.componentName` = `menuId.toString()` (used for keep-alive)
  - Components lazy-loaded via `import.meta.glob('../views/**/**.vue')`
  - External links render via `iframe-index.vue`
- All dynamic routes are children of `SmartLayout` (path `/`)
- **Router guard:** validates token, manages tagNav, handles keep-alive inclusion, NProgress loading bar

### Layout System (`src/layout/`)
5 layout modes, selectable via app config (`layout` setting):
- `side` — classic sidebar + header
- `side-expand` — grouped sidebar (二级弹出)
- `top` — horizontal top menu
- `top-expand` — grouped top menu
- `help-doc` — dedicated help documentation layout

Each layout shares key components: page-tag (tab navigation, 3 styles), breadcrumb, header-user-space (avatar, messages, settings drawer), side-help-doc, footer, watermark.

### API Layer (`src/lib/axios.js`)
- Creates `smartAxios` instance with `baseURL` from env
- **Request interceptor:** injects `Authorization: Bearer <token>` header
- **Response interceptor:** checks `code !== 1` for errors; handles token expiry (30007/30008), session timeout (30012), security warnings (30010/30011)
- Exports: `getRequest()`, `postRequest()`, `postEncryptRequest()`, `postDownload()`, `getDownload()`
- API files export named objects: `export const loginApi = { ... }`
- URL pattern: `/module/action` for system, `/support/module/action` for support, `/businessType/module/action` for business

### State Management (Pinia)
- `app-config` — UI preferences (layout, theme, language, tag style). Persisted to localStorage.
- `user` — Login state, menus, permissions, tagNav tabs, keep-alive list, unread messages. Builds menu tree from flat backend list.
- `dict` — Data dictionary cache. `Map<dictCode, dataArray[]>`.
- `role` — Role permission tree selection state.
- `spin` — Global loading spinner toggle.

### SmartEnum Pattern (Constants)
Centralized enum constants in `src/constants/`. Each enum object has `{ value, desc }` pairs.
- `src/constants/index.js` aggregates all enums into one default export for `smart-enums-plugin`
- Components use `$smartEnumPlugin.getDescByValue(name, value)` or the `<SmartEnumSelect>` component
- **No magic numbers in templates** — always reference the constant

### Plugin System
- `smart-enums-plugin` — provides `$smartEnumPlugin` global for enum lookups
- `dict-plugin` — provides `$dictPlugin` for dictionary value display
- `privilege-plugin` — provides `$privilege(value)` for conditional rendering by permission

### Permission Control
- `v-privilege` directive: removes DOM element if user lacks the required permission point
- `$privilege(value)`: returns boolean for template conditionals
- Super admin bypasses all checks

### Internationalization
- `zh_CN` (default) and `en_US`
- Each locale exports `antdLocale`, `dayjsLocale`, and translation keys
- Language persisted in app-config localStorage

### Theme System
- `src/theme/color.js` — 10 color palettes with primary/active/hover variants
- `src/theme/custom-variables.js` — Less variables
- `src/theme/index.less` — Global styles, Ant Design overrides
- Theme applied via `<a-config-provider>` in `App.vue`

### Environments (5 modes)
| Mode | API Target |
|------|-----------|
| `localhost` | `http://127.0.0.1:1024` |
| `dev` | `http://127.0.0.1:1024` |
| `test` | (local) |
| `pre` | `https://preview.smartadmin.vip/smart-admin-api` |
| `prod` | (production) |

### Code Conventions (Frontend)
- **`<script setup>`** only — no Options API
- **Single quotes**, semicolons, 150 char line width (Prettier config)
- Component files: `kebab-case.vue`
- Import paths: absolute `/@/` alias always
- API module exports: named export object (e.g., `export const employeeApi = { ... }`)
- `defineProps` / `defineEmits` with `v-model:value` + `update:value` event pattern
- `onMounted` for data fetching in components
- Error handling: wrap with `smartSentry.captureError()`
- **Vue SFC 缩进规则**：`<script setup>` 和 `<style>` 内的内容必须缩进 2 空格（对齐项目 Prettier 配置），`<template>` 内的内容也缩进 2 空格

### Scripts
```bash
npm run localhost   # vite --mode localhost
npm run dev         # vite (development)
npm run build:test  # vite build --base=/admin/ --mode test
npm run build:pre   # vite build --mode pre
npm run build:prod  # vite build --mode production
```

---

## Backend: `smart-admin-api-java8-springboot2/`

### Tech Stack
- **Java 8**
- **Spring Boot 2.7.18**
- **MyBatis-Plus 3.5.12** (ORM)
- **Sa-Token 1.44** (auth framework)
- **MySQL** (primary, with p6spy SQL logging)
- **Redis** (cache, session, distributed locks via Redisson 3.25)
- **Druid 1.2.25** (connection pool)
- **Knife4j 4.5 + SpringDoc OpenAPI 1.7** (API docs)
- **FastExcel 1.2** (Excel import/export)
- **Hutool 5.8** (utilities)
- **Velocity** (code generation templates)
- **AWS SDK S3** (cloud file storage, optional)

### Maven Structure
```
smart-admin-api-java8-springboot2/
├── pom.xml              # Parent POM (sa-parent:3.0.0), 4 profiles (dev/test/pre/prod)
├── sa-base/             # Shared infrastructure module
│   ├── pom.xml          # All core dependencies
│   └── src/main/
│       ├── java/net/lab1024/sa/base/
│       │   ├── common/  # Domain, enums, exceptions, annotations, validators
│       │   ├── config/  # Auto-configuration (MybatisPlus, Redis, Cache, Swagger, etc.)
│       │   ├── handler/ # GlobalExceptionHandler, MybatisPlusFillHandler
│       │   ├── constant/ # Shared constants, Redis keys, Swagger tags
│       │   └── module/support/ # 20+ support subsystems
│       └── resources/   # Environment-specific sa-base.yaml
└── sa-admin/            # Business module
    ├── pom.xml          # Depends on sa-base
    └── src/main/
        ├── java/net/lab1024/sa/admin/
        │   ├── config/  # MvcConfig, OperateLogAspectConfig
        │   ├── constant/ # Admin-specific cache keys, Redis keys, Swagger tags
        │   ├── interceptor/ # AdminInterceptor (auth + permission)
        │   ├── module/
        │   │   ├── system/   # login, employee, department, role, menu, position, datascope
        │   │   └── business/ # category, goods, oa(enterprise, bank, invoice, notice)
        │   └── AdminApplication.java
        └── resources/
            ├── dev/test/pre/prod/application.yaml
            └── mapper/  # MyBatis XML mapper files
```

### Four-Layer Architecture
Every business module follows this strict layering:

```
Controller (@RestController)
  ↓ calls
Service (@Service) — business logic, @Transactional
  ↓ calls
Manager (@Service) — transaction boundary, often extends ServiceImpl<Dao, Entity>, caching layer
  ↓ calls
DAO (@Mapper extends BaseMapper<Entity>) — MyBatis-Plus BaseMapper
```

**Why Manager layer?** Separates business logic (Service) from data access orchestration (Manager). The Manager layer handles cross-DAO transactions and serves as the `@Cacheable`/`@CacheEvict` boundary.

### Request/Response Conventions

#### Response Wrapper
`ResponseDTO<T>` — every API response uses this wrapper.
- `code` (Integer), `ok` (Boolean, `false` on error), `msg` (String), `data` (T)
- Factory: `ResponseDTO.ok()`, `ResponseDTO.ok(data)`, `ResponseDTO.error(ErrorCode)`

#### Pagination
- Request: `PageParam` (`pageNum`, `pageSize`, `sortItemList`, `searchCount`)
  - Extend it for query forms: `*QueryForm extends PageParam`
- Response: `PageResult<T>` (`pageNum`, `pageSize`, `total`, `pages`, `list`, `emptyFlag`)

#### DTO Naming Convention
| Suffix | Purpose | Examples |
|--------|---------|----------|
| `*Entity` | Database entity, `@TableName` | `EmployeeEntity` |
| `*AddForm` | Create request body, `@Valid` | `EmployeeAddForm` |
| `*UpdateForm` | Update request body | `EmployeeUpdateForm` |
| `*QueryForm` | Search/pagination request, extends `PageParam` | `EmployeeQueryForm` |
| `*VO` | Response view object | `EmployeeVO` |
| `*ExcelVO` | Excel export row | `GoodsExcelVO` |
| `*BO` | Internal business object | `DataTracerContentBO` |

All DTOs use Lombok `@Data`. Forms use `@Schema` (Swagger) + `@NotBlank`/`@NotNull`/`@Length` (validation).

### Security Architecture

#### Authentication (Sa-Token)
- Token stored in `Authorization` header with `Bearer ` prefix
- 30-day timeout, auto-renew enabled, single concurrent session
- `LoginService` implements `StpInterface` for permission/role retrieval
- Password: salted + BCrypt hashed
- Three-level security protection (Level 3): email 2FA, password complexity, reuse prevention, account lockout

#### Authorization (`AdminInterceptor`)
1. Bypass OPTIONS requests (CORS preflight)
2. Extract token → validate → load `RequestEmployee`
3. Check `@NoNeedLogin` annotation → allow anonymous
4. Check super admin → allow all
5. Check `@SaCheckPermission` on method → enforce permission string
6. `ThreadLocal<RequestUser>` via `SmartRequestUtil` (set in preHandle, cleared in afterCompletion)

#### Custom Security Annotations
| Annotation | Level | Purpose |
|-----------|-------|---------|
| `@NoNeedLogin` | Method | Skip authentication (login, captcha) |
| `@SaCheckPermission("system:employee:add")` | Method | Permission check |
| `@OperateLog` | Type/Method | Auto-record operation log |
| `@RepeatSubmit` | Method | Prevent duplicate form submission |
| `@DataScope` | Mapper Method | Row-level data filtering (MyBatis plugin) |
| `@ApiEncrypt` / `@ApiDecrypt` | Method | API payload encryption (AES/SM4) |

### Enum Convention
All enums implement `BaseEnum`:
```java
@AllArgsConstructor
@Getter
public enum GenderEnum implements BaseEnum {
    UNKNOWN(0, "unknown"),
    MAN(1, "male"),
    WOMAN(2, "female");
    private final Integer value;
    private final String desc;
}
```
- `BaseEnum.getValue()` / `BaseEnum.getDesc()` — standardized access
- `SmartEnumUtil` — lookup by value
- `@CheckEnum(EnumClass.class)` — Bean Validation integration
- `@SchemaEnum(EnumClass.class)` — Swagger docs integration

### Caching Pattern
- Spring Cache abstraction with Redis (primary) or Caffeine (fallback)
- Cache key prefix: `cache`
- Manager layer is the caching boundary with `@Cacheable`/`@CacheEvict`
- Redis key format: `{projectName}:{env}:{prefix}{key}`
- `RedisService` provides `getLock`/`unLock` for distributed locks

### Code Generation
Velocity-based code generator that produces both frontend and backend code from database table metadata:
- **Backend output:** Controller, Service, Manager, DAO, Entity, *Form, *VO, Mapper XML, Menu SQL
- **Frontend output:** API module, constant file, list page, form page
- Configured via database table (persistent generation config)

### Database
- MySQL 8.x, database: `smart_admin_v3`
- p6spy for SQL logging in dev/test
- Soft delete via `deleted_flag` Boolean column (filtered in every Mapper XML query)
- Auto-fill `createTime`/`updateTime` via `MybatisPlusFillHandler`
- Keyword search uses `INSTR()` (not `LIKE`)

### Exception Handling
- `GlobalExceptionHandler` (`@ControllerAdvice`) — centralized error handling
- `BusinessException` extends `RuntimeException`, carries `ErrorCode`
- Error codes: interface `ErrorCode` → `SystemErrorCode`(10001+) and `UserErrorCode`(30001+)
- Validation errors collected and returned as comma-separated message
- Non-prod environments include stack traces in error responses

### Swagger / API Docs
- SpringDoc OpenAPI 3 + Knife4j UI
- Two API groups: "business" (all paths) and "support" (`/support/**`)
- `@Tag` on controllers, `@Operation(summary = "desc @author")` on methods, `@Schema` on DTO fields
- Swagger UI at `/doc.html`
- Swagger whitelist paths excluded from authentication interceptor

### Maven Profiles
| Profile | Active By Default | Purpose |
|---------|-------------------|---------|
| `dev` | Yes | Local development |
| `test` | No | Testing environment |
| `pre` | No | Pre-production |
| `prod` | No | Production |

### Configuration Files
- `sa-base/src/main/resources/{env}/sa-base.yaml` — shared config (datasource, Redis, mail, file storage, Sa-Token, Swagger, heart-beat, reload, job)
- `sa-admin/src/main/resources/{env}/application.yaml` — admin-specific config (server port, project name, log dir)

### Key Backend Conventions
- `@Resource` (not `@Autowired`) for dependency injection
- DAO = MyBatis-Plus `BaseMapper<Entity>` interface with `@Mapper`
- Manager often extends `ServiceImpl<Dao, Entity>` to inherit MyBatis-Plus CRUD
- Controller methods: `@PostMapping` for mutations, `@GetMapping` for queries
- Mapper XML: dynamic SQL with `<where>`, `<if>`, `<foreach>`; INSTR() for keyword search
- **新增 Entity 字段时，必须同步更新以下所有位置**：
  1. `*Entity.java` — 新增字段
  2. `*VO.java` / `*AddForm.java` / `*UpdateForm.java` — 对应的 DTO 也需要新增
  3. **`*Mapper.xml` 的 `base_columns` SQL 片段** — 添加对应数据库列名（极易遗漏！）
  4. 数据库表 — ALTER TABLE 添加列
  5. 前端表单和列表 — 对应的字段绑定
- All entity tables use `t_` prefix: `t_employee`, `t_menu`, etc.

---

## Git Conventions
- `.gitignore` excludes: `node_modules/`, `dist/`, `target/`, `.idea/`, `.vscode/`, `*.iml`
- Source code under `src/main/` and `src/test/` is included

---

## Common Tasks Quick Reference

### Frontend
```bash
cd smart-admin-web-javascript
npm install
npm run localhost    # Start dev server (localhost mode, proxies to 127.0.0.1:1024)
npm run dev          # Start dev server
npm run build:prod   # Production build
```

### Backend
```bash
cd smart-admin-api-java8-springboot2
mvn clean install -Pdev     # Build with dev profile
mvn spring-boot:run -Pdev   # Run with dev profile (working dir: sa-admin/)
```

### Database
```bash
# Import SQL:
# mysql -u root -p < 数据库SQL脚本/mysql/smart_admin_v3.sql
```
