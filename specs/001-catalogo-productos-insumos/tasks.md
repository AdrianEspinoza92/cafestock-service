---

description: "Task list for: Administrar catálogo semilla de productos e insumos"
---

# Tasks: Administrar catálogo semilla de productos e insumos

**Input**: Design documents from `/specs/001-catalogo-productos-insumos/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/catalog.openapi.yaml, research.md, quickstart.md

**Tests**: Incluidas y son OBLIGATORIAS — la Constitución del proyecto
(Principio II, NON-NEGOTIABLE) exige pruebas unitarias, de integración y
funcionales BDD para toda historia; no son opcionales en este proyecto.

**Organization**: Tareas agrupadas por historia de usuario (spec.md) para
permitir implementación y prueba independientes de cada una.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin
  dependencias pendientes)
- **[Story]**: US1, US2, US3 — mapea a las historias de `spec.md`
- Cada tarea incluye la ruta de archivo exacta

## Path Conventions

Proyecto backend único (Spring Boot, Gradle): `src/main/java/org/ups/cafestock/...`,
`src/main/resources/...`, `src/test/java/org/ups/cafestock/...`,
`src/test/resources/...` — ver "Project Structure" en `plan.md`.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el build para Clean Architecture + API-First (OpenAPI) + BDD + cobertura JaCoCo

- [X] T001 Añadir el plugin `jacoco` y `org.openapi.generator` en `build.gradle` (plugins block)
- [X] T002 [P] Añadir dependencias de prueba BDD en `build.gradle`: `io.cucumber:cucumber-java`, `io.cucumber:cucumber-spring`, `io.cucumber:cucumber-junit-platform-engine`, `org.junit.platform:junit-platform-suite`, `org.assertj:assertj-core`, `org.mockito:mockito-core`, `org.mockito:mockito-junit-jupiter` (scope `testImplementation`)
- [X] T003 Copiar el contrato `specs/001-catalogo-productos-insumos/contracts/catalog.openapi.yaml` a `src/main/resources/openapi/catalog.yaml` (fuente única versionada para el generador)
- [X] T004 Configurar la tarea Gradle `openApiGenerate` en `build.gradle`: generador `spring`, `interfaceOnly=true`, `inputSpec=src/main/resources/openapi/catalog.yaml`, salida en `build/generated/openapi`, y enlazarla a `compileJava` (`sourceSets.main.java.srcDir`)
- [X] T005 [P] Configurar `jacocoTestReport` (HTML+XML) y `jacocoTestCoverageVerification` en `build.gradle` con reglas `CLASS` (mínimo 0.80, excluyendo clases generadas de `build/generated/openapi` y `*Application`) y `BUNDLE` global (mínimo 0.80); enlazar `check` a `jacocoTestCoverageVerification`

**Checkpoint**: El proyecto compila con las interfaces generadas del contrato y el pipeline de cobertura está listo antes de escribir cualquier código de negocio.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Esquema de base de datos, datos semilla y tipos de dominio compartidos por Producto e Insumo

**⚠️ CRITICAL**: Ninguna historia de usuario puede implementarse sin esta fase

- [X] T006 Crear `src/main/resources/schema.sql` con las tablas `producto` (`id`, `nombre`, `precio`, `estado`, `creado_en`, `actualizado_en`) e `insumo` (`id`, `nombre`, `unidad_medida`, `stock_inicial`, `estado`, `creado_en`, `actualizado_en`), incluyendo restricción única compuesta `(nombre_normalizado, estado)` en cada tabla (H2 no soporta índice filtrado; ver `data-model.md`)
- [X] T007 Crear `src/main/resources/data.sql` con un catálogo semilla de ejemplo (productos e insumos ya en estado `ACTIVO`) referenciado en `quickstart.md`
- [X] T008 [P] Configurar `src/main/resources/application.properties`: `spring.sql.init.mode=always`, `spring.jpa.hibernate.ddl-auto=validate`, datasource H2 (ver `research.md` §6)
- [X] T009 [P] Crear enum `EstadoCatalogo` (`ACTIVO`, `INACTIVO`) en `src/main/java/org/ups/cafestock/catalog/domain/model/EstadoCatalogo.java`
- [X] T010 [P] Crear enum `UnidadMedida` (`UNIDAD`, `GRAMO`, `KILOGRAMO`, `MILILITRO`, `LITRO`) en `src/main/java/org/ups/cafestock/catalog/domain/model/UnidadMedida.java`
- [X] T011 [P] Crear excepciones de dominio `NombreDuplicadoException`, `RegistroNoEncontradoException` y `ValorInvalidoException` en `src/main/java/org/ups/cafestock/catalog/domain/exception/`
- [X] T012 [P] Crear el runner de Cucumber `src/test/java/org/ups/cafestock/catalog/functional/CucumberFunctionalTestRunner.java` (`@Suite`, `@IncludeEngines("cucumber")`, `glue` apuntando a `catalog.functional.steps`) y `src/test/resources/cucumber.properties`

**Checkpoint**: Esquema, semilla y tipos base listos — las historias de usuario pueden implementarse en paralelo a partir de aquí.

---

## Phase 3: User Story 1 - Alta de producto (Priority: P1) 🎯 MVP

**Goal**: Un encargado de compras puede dar de alta un producto (nombre + precio) que queda disponible de inmediato en la pantalla de venta.

**Independent Test**: `POST /api/v1/productos` con nombre y precio válidos → `201` y aparece en `GET /api/v1/productos?estado=ACTIVO`.

### Tests for User Story 1 ⚠️ (escribir primero, deben fallar antes de implementar)

- [X] T013 [P] [US1] Feature Gherkin `src/test/resources/features/alta_producto.feature` transcribiendo los 3 Acceptance Scenarios de US1 en `spec.md`
- [X] T014 [P] [US1] Step definitions `src/test/java/org/ups/cafestock/catalog/functional/steps/AltaProductoSteps.java`
- [X] T015 [P] [US1] Test unitario Given/When/Then (BDDMockito) de `CrearProductoUseCase` en `src/test/java/org/ups/cafestock/catalog/application/CrearProductoUseCaseTest.java` (casos: alta válida, nombre vacío, nombre duplicado, precio ≤ 0)
- [X] T016 [P] [US1] Test de integración `@DataJpaTest` en `src/test/java/org/ups/cafestock/catalog/infrastructure/persistence/ProductoRepositoryAdapterIT.java` (persistir y recuperar, constraint de nombre único)
- [X] T017 [P] [US1] Test de integración `MockMvc` en `src/test/java/org/ups/cafestock/catalog/infrastructure/web/ProductoControllerCrearIT.java` (`POST /productos` contra el contrato: 201/400/409)

### Implementation for User Story 1

- [X] T018 [P] [US1] Modelo de dominio `Producto` (sin anotaciones de framework) en `src/main/java/org/ups/cafestock/catalog/domain/model/Producto.java`
- [X] T019 [US1] Puerto `ProductoRepositoryPort` en `src/main/java/org/ups/cafestock/catalog/domain/port/ProductoRepositoryPort.java` (depende de T018)
- [X] T020 [US1] `CrearProductoUseCase` (valida nombre no vacío/duplicado y precio > 0) en `src/main/java/org/ups/cafestock/catalog/application/usecase/CrearProductoUseCase.java` (depende de T019, T011)
- [X] T021 [P] [US1] `ProductoJpaEntity` en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/entity/ProductoJpaEntity.java`
- [X] T022 [US1] `ProductoJpaRepository` (Spring Data) en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/repository/ProductoJpaRepository.java` (depende de T021)
- [X] T023 [US1] `ProductoRepositoryAdapter` implementando `ProductoRepositoryPort` en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/adapter/ProductoRepositoryAdapter.java` (depende de T019, T022)
- [X] T024 [P] [US1] `ProductoMapper` (dominio ↔ DTO generado) en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/mapper/ProductoMapper.java` (depende de T018)
- [X] T025 [US1] `ProductoController` implementando la interfaz generada del contrato (operaciones `crearProducto`, `listarProductos`) en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/controller/ProductoController.java` (depende de T020, T023, T024)

**Checkpoint**: User Story 1 funcional y probable de forma independiente (MVP).

---

## Phase 4: User Story 2 - Alta de insumo (Priority: P1)

**Goal**: Un encargado de compras puede dar de alta un insumo (unidad de medida + stock inicial) que queda disponible para recetas y mínimos.

**Independent Test**: `POST /api/v1/insumos` con unidad de medida y stock inicial válidos → `201` y aparece en `GET /api/v1/insumos?estado=ACTIVO`.

### Tests for User Story 2 ⚠️

- [X] T026 [P] [US2] Feature Gherkin `src/test/resources/features/alta_insumo.feature` transcribiendo los 3 Acceptance Scenarios de US2 en `spec.md`
- [X] T027 [P] [US2] Step definitions `src/test/java/org/ups/cafestock/catalog/functional/steps/AltaInsumoSteps.java`
- [X] T028 [P] [US2] Test unitario Given/When/Then (BDDMockito) de `CrearInsumoUseCase` en `src/test/java/org/ups/cafestock/catalog/application/CrearInsumoUseCaseTest.java` (casos: alta válida, sin unidad de medida, stock negativo, nombre duplicado)
- [X] T029 [P] [US2] Test de integración `@DataJpaTest` en `src/test/java/org/ups/cafestock/catalog/infrastructure/persistence/InsumoRepositoryAdapterIT.java`
- [X] T030 [P] [US2] Test de integración `MockMvc` en `src/test/java/org/ups/cafestock/catalog/infrastructure/web/InsumoControllerCrearIT.java` (`POST /insumos` contra el contrato: 201/400/409)

### Implementation for User Story 2

- [X] T031 [P] [US2] Modelo de dominio `Insumo` en `src/main/java/org/ups/cafestock/catalog/domain/model/Insumo.java`
- [X] T032 [US2] Puerto `InsumoRepositoryPort` en `src/main/java/org/ups/cafestock/catalog/domain/port/InsumoRepositoryPort.java` (depende de T031)
- [X] T033 [US2] `CrearInsumoUseCase` (valida nombre no vacío/duplicado, unidad de medida presente y stock ≥ 0) en `src/main/java/org/ups/cafestock/catalog/application/usecase/CrearInsumoUseCase.java` (depende de T032, T011)
- [X] T034 [P] [US2] `InsumoJpaEntity` en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/entity/InsumoJpaEntity.java`
- [X] T035 [US2] `InsumoJpaRepository` en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/repository/InsumoJpaRepository.java` (depende de T034)
- [X] T036 [US2] `InsumoRepositoryAdapter` implementando `InsumoRepositoryPort` en `src/main/java/org/ups/cafestock/catalog/infrastructure/persistence/adapter/InsumoRepositoryAdapter.java` (depende de T032, T035)
- [X] T037 [P] [US2] `InsumoMapper` en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/mapper/InsumoMapper.java` (depende de T031)
- [X] T038 [US2] `InsumoController` implementando la interfaz generada del contrato (operaciones `crearInsumo`, `listarInsumos`) en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/controller/InsumoController.java` (depende de T033, T036, T037)

**Checkpoint**: User Stories 1 y 2 funcionan de forma independiente.

---

## Phase 5: User Story 3 - Edición y baja de productos e insumos existentes (Priority: P2)

**Goal**: Un encargado de compras puede editar o desactivar/reactivar un producto o insumo existente sin afectar ventas ya registradas.

**Independent Test**: `PATCH /api/v1/productos/{id}` cambia nombre/precio; `POST /api/v1/productos/{id}/desactivar` lo excluye de `GET ?estado=ACTIVO` sin alterar ventas históricas; `POST /activar` lo restaura. Análogo para insumos.

**Depends on**: Phase 3 (US1) y Phase 4 (US2) — reutiliza los puertos, adaptadores y entidades de Producto e Insumo ya creados.

### Tests for User Story 3 ⚠️

- [X] T039 [P] [US3] Feature Gherkin `src/test/resources/features/edicion_baja_catalogo.feature` transcribiendo los 3 Acceptance Scenarios de US3 en `spec.md`
- [X] T040 [P] [US3] Step definitions `src/test/java/org/ups/cafestock/catalog/functional/steps/EdicionBajaCatalogoSteps.java`
- [X] T041 [P] [US3] Test unitario `EditarProductoUseCaseTest` (Given/When/Then) en `src/test/java/org/ups/cafestock/catalog/application/EditarProductoUseCaseTest.java`
- [X] T042 [P] [US3] Test unitario `CambiarEstadoProductoUseCaseTest` (activar/desactivar) en `src/test/java/org/ups/cafestock/catalog/application/CambiarEstadoProductoUseCaseTest.java`
- [X] T043 [P] [US3] Test unitario `EditarInsumoUseCaseTest` en `src/test/java/org/ups/cafestock/catalog/application/EditarInsumoUseCaseTest.java`
- [X] T044 [P] [US3] Test unitario `CambiarEstadoInsumoUseCaseTest` en `src/test/java/org/ups/cafestock/catalog/application/CambiarEstadoInsumoUseCaseTest.java`
- [X] T045 [P] [US3] Test de integración `MockMvc` en `src/test/java/org/ups/cafestock/catalog/infrastructure/web/ProductoControllerEditarEstadoIT.java` (`PATCH`, `/activar`, `/desactivar` contra el contrato); incluir aserción de que el producto desaparece de `GET /productos?estado=ACTIVO` tras `/desactivar` y reaparece tras `/activar` (FR-010, FR-012, SC-002)
- [X] T046 [P] [US3] Test de integración `MockMvc` en `src/test/java/org/ups/cafestock/catalog/infrastructure/web/InsumoControllerEditarEstadoIT.java` (`PATCH`, `/activar`, `/desactivar` contra el contrato); incluir aserción de que el insumo desaparece de `GET /insumos?estado=ACTIVO` tras `/desactivar` y reaparece tras `/activar` (FR-010, FR-012, SC-002)

### Implementation for User Story 3

- [X] T047 [US3] `EditarProductoUseCase` (nombre/precio, revalida duplicados) en `src/main/java/org/ups/cafestock/catalog/application/usecase/EditarProductoUseCase.java` (depende de T019)
- [X] T048 [US3] `CambiarEstadoProductoUseCase` (activar/desactivar) en `src/main/java/org/ups/cafestock/catalog/application/usecase/CambiarEstadoProductoUseCase.java` (depende de T019)
- [X] T049 [US3] `EditarInsumoUseCase` (nombre/unidad de medida, revalida duplicados) en `src/main/java/org/ups/cafestock/catalog/application/usecase/EditarInsumoUseCase.java` (depende de T032)
- [X] T050 [US3] `CambiarEstadoInsumoUseCase` (activar/desactivar) en `src/main/java/org/ups/cafestock/catalog/application/usecase/CambiarEstadoInsumoUseCase.java` (depende de T032)
- [X] T051 [US3] Extender `ProductoController` con `editarProducto`, `activarProducto`, `desactivarProducto` en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/controller/ProductoController.java` (depende de T047, T048)
- [X] T052 [US3] Extender `InsumoController` con `editarInsumo`, `activarInsumo`, `desactivarInsumo` en `src/main/java/org/ups/cafestock/catalog/infrastructure/web/controller/InsumoController.java` (depende de T049, T050)

**Checkpoint**: Las 3 historias de usuario funcionan de manera independiente; CRUD completo de Producto e Insumo disponible.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verificación final de calidad transversal a las 3 historias

- [X] T053 [P] Ejecutar manualmente los escenarios de `quickstart.md` (curl) contra el servicio levantado con `./gradlew bootRun` y confirmar los resultados esperados
- [X] T054 Ejecutar `./gradlew check` (tests unitarios + integración + funcionales Cucumber + `jacocoTestCoverageVerification`) y corregir cualquier clase por debajo del 80% o cobertura global por debajo del 80% antes de cerrar la historia

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — inicia de inmediato
- **Foundational (Phase 2)**: Depende de Setup — BLOQUEA todas las historias de usuario
- **User Story 1 (Phase 3)**: Depende de Foundational — sin dependencia de otras historias
- **User Story 2 (Phase 4)**: Depende de Foundational — sin dependencia de otras historias (puede ir en paralelo con US1 si hay más de un desarrollador)
- **User Story 3 (Phase 5)**: Depende de Foundational **y** de que existan los puertos/adaptadores de Producto (US1) e Insumo (US2), ya que edita y cambia el estado de esos mismos registros
- **Polish (Phase 6)**: Depende de que las historias que se quieran entregar estén completas

### Within Each User Story

- Tests (T013-T017, T026-T030, T039-T046) se escriben y deben FALLAR antes de implementar
- Modelo de dominio → puerto → caso de uso → entidad JPA → repositorio → adaptador → mapper → controlador
- Historia completa antes de pasar a la siguiente en orden de prioridad

### Parallel Opportunities

- Todas las tareas [P] de Setup (T002, T005) en paralelo tras T001
- T009, T010, T011, T012 (Foundational) en paralelo entre sí
- Todos los tests [P] de una misma historia en paralelo entre sí (deben fallar antes de implementar)
- US1 (Phase 3) y US2 (Phase 4) pueden implementarse en paralelo por desarrolladores distintos una vez completada Foundational
- T018/T021/T024 (US1) y T031/T034/T037 (US2) en paralelo entre sí dentro de cada historia

---

## Parallel Example: User Story 1

```bash
# Lanzar juntos los tests de User Story 1:
Task: "Feature Gherkin en src/test/resources/features/alta_producto.feature"
Task: "Step definitions en src/test/java/.../functional/steps/AltaProductoSteps.java"
Task: "Test unitario CrearProductoUseCase en src/test/java/.../application/CrearProductoUseCaseTest.java"
Task: "Test de integración @DataJpaTest en src/test/java/.../infrastructure/persistence/ProductoRepositoryAdapterIT.java"
Task: "Test de integración MockMvc en src/test/java/.../infrastructure/web/ProductoControllerCrearIT.java"

# Lanzar juntos los modelos/entidades de User Story 1:
Task: "Modelo de dominio Producto en src/main/java/.../domain/model/Producto.java"
Task: "ProductoJpaEntity en src/main/java/.../infrastructure/persistence/entity/ProductoJpaEntity.java"
Task: "ProductoMapper en src/main/java/.../infrastructure/web/mapper/ProductoMapper.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 solamente)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational (CRÍTICO — bloquea todas las historias)
3. Completar Phase 3: User Story 1
4. **DETENER y VALIDAR**: probar User Story 1 de forma independiente (`quickstart.md` Escenario 1)
5. Desplegar/demostrar si está listo

### Incremental Delivery

1. Setup + Foundational → base lista
2. Añadir User Story 1 → probar independientemente → demo (¡MVP!)
3. Añadir User Story 2 → probar independientemente → demo
4. Añadir User Story 3 → probar independientemente → demo
5. Cada historia añade valor sin romper las anteriores (US3 depende de US1/US2 por diseño, no por accidente)

### Parallel Team Strategy

Con dos o más desarrolladores:

1. El equipo completa juntos Setup + Foundational
2. Una vez lista Foundational:
   - Desarrollador A: User Story 1
   - Desarrollador B: User Story 2
3. User Story 3 inicia cuando US1 y US2 estén ambas completas (dependencia real de diseño)

---

## Notes

- [P] = archivos distintos, sin dependencias pendientes
- [Story] mapea cada tarea a su historia de usuario para trazabilidad
- Los tests son obligatorios en este proyecto (Constitución, Principio II) y deben fallar antes de implementar
- Confirmar que `./gradlew check` pasa (incluye JaCoCo) antes de dar por cerrada cualquier historia
- Evitar: tareas vagas, conflictos de archivo entre tareas [P], dependencias cruzadas entre historias que rompan su independencia (salvo la dependencia explícita y documentada de US3 sobre US1/US2)
- FR-011 y SC-003 (preservar ventas ya registradas ante ediciones/bajas)
  NO tienen tarea propia en esta historia: `Venta` está fuera de alcance
  (ver spec.md Assumptions) y hoy se cumplen solo porque este código nunca
  toca esa tabla. Cuando se implemente la funcionalidad de Ventas, esa
  historia DEBE incluir una prueba de integración que edite/desactive un
  Producto/Insumo con una Venta histórica asociada y verifique que el
  registro de Venta no cambia.
