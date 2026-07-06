# Implementation Plan: Administrar catálogo semilla de productos e insumos

**Branch**: `001-catalogo-productos-insumos` | **Date**: 2026-07-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-catalogo-productos-insumos/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Dar de alta, editar, desactivar y reactivar productos e insumos a través de
una API REST contract-first (OpenAPI), organizada en capas de Clean
Architecture, para que las ventas, recetas y mínimos de stock operen sobre
un catálogo semilla confiable desde el primer día sin alterar el historial
de ventas ya registrado.

## Technical Context

**Language/Version**: Java 17 (Gradle toolchain, ya configurado en el repo)

**Primary Dependencies**: Spring Boot (`spring-boot-starter-webmvc`,
`spring-boot-starter-data-jpa`), Lombok, `openapi-generator-gradle-plugin`
(genera interfaces de servidor a partir del contrato OpenAPI), `springdoc`
no es necesario porque el contrato se escribe a mano y se versiona en el
repo; `jacoco` Gradle plugin para cobertura; `cucumber-java` +
`cucumber-spring` + `cucumber-junit-platform-engine` para pruebas
funcionales BDD; `assertj` + `mockito` (`BDDMockito`) para pruebas
unitarias/integración en estilo Given/When/Then.

**Storage**: H2 (ya presente en `build.gradle`) como base de datos
relacional para esta fase; acceso exclusivamente vía Spring Data JPA en la
capa de infraestructura. El esquema (tablas `producto`, `insumo`, columnas,
constraint de unicidad de nombre) se declara explícitamente en
`src/main/resources/schema.sql` y los datos semilla (catálogo inicial de
ejemplo) en `src/main/resources/data.sql`, cargados automáticamente al
arrancar (`spring.sql.init.mode=always`) en vez de depender de
`ddl-auto` de Hibernate — ver Research §6.

**Testing**: JUnit 5 (`junit-platform-launcher`, ya presente) como motor de
ejecución; Cucumber para escenarios funcionales (mapeo 1:1 con los
Acceptance Scenarios de `spec.md`); `@DataJpaTest` para integración de
persistencia; `MockMvc`/`@SpringBootTest(webEnvironment=RANDOM_PORT)` para
integración de la API contra el contrato OpenAPI.

**Target Platform**: Servicio backend JVM (Spring Boot embebido en Tomcat),
desplegable como servicio único en Linux server.

**Project Type**: Servicio web único (single Gradle module), organizado
internamente en capas de Clean Architecture mediante paquetes.

**Performance Goals**: Sin metas específicas de throughput; herramienta de
back-office para uso interno de una sola cafetería/tienda (decenas de
usuarios concurrentes como máximo). Se prioriza correctitud y consistencia
sobre rendimiento.

**Constraints**: Las operaciones de edición/baja NO deben modificar datos
de ventas ya registradas (FR-011); las validaciones de duplicados y rangos
deben aplicarse en el momento de la escritura (FR-005, FR-006).

**Scale/Scope**: Catálogo de bajo volumen (decenas a un par de cientos de
productos e insumos activos); alcance de esta historia limitado a alta,
edición, baja y reactivación de Producto e Insumo — NO incluye gestión de
Recetas ni movimientos de inventario (ver Assumptions en spec.md).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación |
|---|---|
| I. Clean Architecture | PASS — El diseño usa paquetes `domain` (entidades Producto/Insumo, puertos de repositorio), `application` (casos de uso), `infrastructure.persistence` (adaptadores JPA) e `infrastructure.web` (controladores que implementan las interfaces generadas por OpenAPI). El dominio no importa tipos de Spring/JPA. |
| II. BDD Testing Discipline | PASS — Los 3 escenarios Gherkin de `spec.md` se implementan como features de Cucumber (funcional); los casos de uso se prueban con JUnit5 + BDDMockito en estilo Given/When/Then (unitario); los adaptadores de persistencia y web se prueban con `@DataJpaTest`/`MockMvc` (integración), también estructurados como Given/When/Then. |
| III. SOLID, YAGNI, DRY | PASS — Un solo módulo Gradle (no se introduce multi-módulo prematuramente); puertos de repositorio segregados por agregado (`ProductoRepository`, `InsumoRepository`) siguiendo ISP/DIP; validación de duplicados/rango centralizada en los casos de uso, sin duplicarla en el controlador. |
| IV. API-First con OpenAPI | PASS — Contrato `contracts/catalog.openapi.yaml` se define antes que el código; las interfaces de controlador se generan con `openapi-generator-gradle-plugin`; los controladores solo implementan esas interfaces, sin código generado editado a mano. |
| V. Coverage Quality Gates | PASS (a verificar en implementación) — Se configurará `jacoco` con reglas de verificación por clase (>80%) y global (≥80%) ligadas a la tarea `check`. |

**Resultado**: Sin violaciones. No se requiere la tabla de Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-catalogo-productos-insumos/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/
│   └── catalog.openapi.yaml   # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/resources/
├── application.properties          # + spring.sql.init.mode=always, spring.jpa.hibernate.ddl-auto=validate
├── schema.sql                       # DDL versionado: tablas producto, insumo (con constraint de nombre único por estado activo)
└── data.sql                         # Datos semilla precargados (productos e insumos activos de ejemplo)

src/main/java/org/ups/cafestock/
├── CafestockServiceApplication.java
└── catalog/
    ├── domain/
    │   ├── model/              # Producto, Insumo, UnidadMedida, EstadoCatalogo (entidades/VOs puros, sin anotaciones de framework)
    │   ├── exception/          # NombreDuplicadoException, RegistroNoEncontradoException, etc.
    │   └── port/                # ProductoRepositoryPort, InsumoRepositoryPort (interfaces)
    ├── application/
    │   └── usecase/            # CrearProductoUseCase, EditarProductoUseCase, CambiarEstadoProductoUseCase,
    │                             # CrearInsumoUseCase, EditarInsumoUseCase, CambiarEstadoInsumoUseCase
    └── infrastructure/
        ├── persistence/
        │   ├── entity/          # ProductoJpaEntity, InsumoJpaEntity
        │   ├── repository/      # Spring Data JPA repositories
        │   └── adapter/         # Implementaciones de los ports usando los repositorios JPA
        └── web/
            ├── controller/      # ProductoController, InsumoController (implementan interfaces generadas por OpenAPI)
            └── mapper/          # DTO (generado) <-> modelo de dominio

build/generated/openapi/          # Salida de openapi-generator-gradle-plugin (interfaces + DTOs), no versionada

src/test/java/org/ups/cafestock/catalog/
├── domain/                       # Pruebas unitarias de entidades/VOs
├── application/                  # Pruebas unitarias de casos de uso (BDDMockito, Given/When/Then)
├── infrastructure/
│   ├── persistence/              # Pruebas de integración (@DataJpaTest)
│   └── web/                      # Pruebas de integración de API (MockMvc contra el contrato)
└── functional/
    ├── CucumberFunctionalTestRunner.java
    └── steps/                    # Step definitions de Cucumber

src/test/resources/features/
├── alta_producto.feature         # User Story 1
├── alta_insumo.feature           # User Story 2
└── edicion_baja_catalogo.feature # User Story 3
```

**Structure Decision**: Módulo Gradle único (el ya existente,
`cafestock-service`), organizado por paquetes que reflejan las capas de
Clean Architecture dentro de un contexto acotado `catalog`. Se evita
dividir en módulos Gradle separados (YAGNI): el aislamiento del dominio
frente a frameworks se logra por convención de paquetes y se puede reforzar
más adelante con una prueba de arquitectura (ArchUnit) si el equipo lo
considera necesario; no se agrega esa dependencia ahora porque no fue
solicitada y no es indispensable para cumplir la Constitución en esta
historia. El esquema de base de datos y los datos semilla se versionan como
archivos SQL planos en `src/main/resources` (`schema.sql` + `data.sql`,
ver Research §6) en vez de depender de la generación automática de esquema
de Hibernate, para que el catálogo tenga datos existentes desde el primer
arranque tal como exige la motivación de la historia de usuario.

## Complexity Tracking

*Sin violaciones registradas en el Constitution Check; tabla no aplica.*
