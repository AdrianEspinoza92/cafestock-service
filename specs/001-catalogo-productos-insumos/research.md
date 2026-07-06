# Phase 0 Research: Administrar catálogo semilla de productos e insumos

No quedaron marcadores `NEEDS CLARIFICATION` en el Technical Context del
plan. Este documento consolida las decisiones técnicas necesarias para
cumplir la Constitución (Clean Architecture, BDD, SOLID/YAGNI/DRY,
API-First con OpenAPI, cobertura con JaCoCo) aplicada a esta historia.

## 1. Cómo estructurar Clean Architecture dentro de un único módulo Gradle

- **Decision**: Organizar el código por paquetes `domain` → `application` →
  `infrastructure` dentro de un contexto acotado `catalog`, en el mismo
  módulo Gradle existente. El paquete `domain` no declara ninguna
  dependencia de Spring/JPA; `application` depende solo de `domain`
  (puertos); `infrastructure` depende de `domain` y `application` e
  implementa los puertos con Spring Data JPA y Spring MVC.
- **Rationale**: Un único módulo Gradle es suficiente para separar capas
  mediante convención de paquetes; introducir módulos Gradle separados
  (`domain`, `application`, `infrastructure`) sería una inversión de
  complejidad de build no justificada por el tamaño actual de la historia
  (violaría YAGNI). La separación por paquetes ya permite pruebas
  unitarias del dominio sin levantar Spring.
- **Alternatives considered**:
  - *Multi-módulo Gradle por capa*: Ofrece cumplimiento en tiempo de
    compilación de las fronteras, pero añade complejidad de build
    (múltiples `build.gradle`, publicación interna de artefactos) que no
    se justifica para una sola historia de catálogo. Se puede adoptar más
    adelante si el proyecto crece.
  - *ArchUnit para verificar fronteras de paquete*: Buena opción de
    refuerzo automático, pero no es indispensable para satisfacer la
    Constitución en esta iteración; queda como mejora futura opcional, no
    bloqueante.

## 2. Herramientas de pruebas BDD (unit, integración, funcional)

- **Decision**:
  - **Funcional**: Cucumber-JVM (`cucumber-java`, `cucumber-spring`,
    `cucumber-junit-platform-engine`) ejecutando los `.feature` en Gherkin
    que son transcripción directa de los Acceptance Scenarios de
    `spec.md`, contra el contexto completo de Spring Boot y la API HTTP
    real (o `MockMvc` como transporte).
  - **Unitario/Integración**: JUnit 5 + AssertJ + Mockito, usando
    `BDDMockito.given/willReturn` y estructurando cada método de prueba en
    tres bloques comentados `// given / // when / // then` para mantener
    la disciplina Given/When/Then sin necesitar Gherkin en estos niveles.
- **Rationale**: Cucumber es el estándar de facto en el ecosistema JVM
  para expresar escenarios de aceptación en Gherkin y ya se alinea 1:1 con
  el formato en que está escrita `spec.md`. Para unit/integration, exigir
  archivos `.feature` sería sobre-ingeniería (no aportan valor sobre un
  test JUnit bien nombrado); usar BDDMockito + estructura Given/When/Then
  cumple el principio constitucional sin herramientas adicionales.
- **Alternatives considered**:
  - *JGiven*: DSL BDD más rico para unit/integration, pero añade una
    dependencia y curva de aprendizaje no justificada (YAGNI) cuando
    JUnit5 + BDDMockito ya cubre la necesidad.
  - *Cucumber también para unit tests*: Gherkin es costoso de mantener
    para lógica de bajo nivel (casos de uso aislados); se reserva Cucumber
    para el nivel funcional/aceptación donde su legibilidad aporta más.

## 3. Generación de código a partir del contrato OpenAPI

- **Decision**: Usar `org.openapi.generator` Gradle plugin
  (`openapi-generator-gradle-plugin`) con generador `spring`, modo
  `interfaceOnly=true` (genera solo interfaces de controlador + DTOs, sin
  implementación), apuntando al archivo `contracts/catalog.openapi.yaml`
  copiado/mantenido en `src/main/resources/openapi/catalog.yaml` como
  fuente única de verdad. Los controladores de `infrastructure.web`
  implementan esas interfaces generadas.
- **Rationale**: `interfaceOnly=true` evita que el generador produzca
  lógica de negocio o persistencia, dejando esa responsabilidad en las
  capas internas (cumple Clean Architecture) mientras garantiza que los
  DTOs y firmas de los endpoints nunca diverjan del contrato (cumple
  API-First).
- **Alternatives considered**:
  - *Escribir DTOs y controladores a mano y solo documentar con
    springdoc*: Viola directamente el principio IV de la Constitución
    (contrato como fuente generada, no como documentación post-hoc).
  - *Generador `spring-boot` completo (con implementación)*: Generaría
    controladores concretos que tendríamos que sobreescribir,
    contradiciendo la prohibición de editar código generado a mano.

## 4. Cobertura con JaCoCo (por clase >80% y global ≥80%)

- **Decision**: Aplicar el plugin `jacoco` de Gradle, configurar
  `jacocoTestReport` para generar reporte HTML+XML tras `test`, y añadir
  `jacocoTestCoverageVerification` con dos reglas: (a) `element: CLASS`,
  `counter: INSTRUCTION`, `minimum: 0.80` (excluyendo clases generadas por
  `openapi-generator`, clases de configuración trivial y la clase
  `*Application`), y (b) `element: BUNDLE` (global), `minimum: 0.80`.
  `check` depende de `jacocoTestCoverageVerification`.
- **Rationale**: Es la forma estándar de Gradle/JaCoCo de expresar ambos
  umbrales requeridos por la Constitución; excluir código generado y la
  clase de arranque evita penalizar código que no es responsabilidad del
  equipo o que no tiene lógica propia que probar.
- **Alternatives considered**:
  - *Solo umbral global*: No cumple el requisito constitucional de umbral
    por clase (>80%), que existe precisamente para evitar que una clase
    completamente sin probar quede oculta detrás de un buen promedio.

## 5. Validación de duplicados y baja lógica

- **Decision**: La unicidad de `nombre` entre registros **activos** se
  valida en el caso de uso (normalizando a minúsculas y recortando
  espacios antes de comparar) y se refuerza con una restricción única a
  nivel de base de datos sobre `(nombre_normalizado)` filtrada por estado
  activo (o un índice único parcial si el motor lo soporta; en su defecto,
  una restricción única sobre `nombre_normalizado` general, aceptando que
  un nombre no podrá reutilizarse ni siquiera si el original fue
  desactivado, lo cual es una simplificación razonable para esta fase).
  "Baja" se implementa como cambio de campo `estado` a `INACTIVO`, nunca
  como `DELETE`.
- **Rationale**: Validar en el caso de uso da mensajes de error de negocio
  claros; la restricción en base de datos es la red de seguridad final
  contra condiciones de carrera. La baja lógica preserva la integridad
  referencial con `Venta` (FR-011) sin necesitar borrado en cascada ni
  claves foráneas opcionales.
- **Alternatives considered**:
  - *Únicamente restricción a nivel de aplicación (sin constraint en BD)*:
    Vulnerable a condiciones de carrera con escrituras concurrentes.
  - *DELETE físico con soft-delete vía columna `deleted_at` + trigger*:
    Añade complejidad de infraestructura (triggers) no justificada frente
    a un simple campo de estado enumerado.

## 6. Esquema y datos semilla precargados en `resources`

- **Decision**: Declarar el esquema explícitamente en
  `src/main/resources/schema.sql` (tablas `producto` e `insumo`, tipos,
  restricción única sobre nombre normalizado) y los datos de catálogo
  iniciales en `src/main/resources/data.sql` (un puñado de productos e
  insumos `ACTIVO` de ejemplo). Se habilita `spring.sql.init.mode=always`
  y se fija `spring.jpa.hibernate.ddl-auto=validate`, de modo que Hibernate
  solo valida que las entidades JPA coincidan con el esquema versionado en
  vez de generarlo automáticamente.
- **Rationale**: La motivación explícita de la historia de usuario es que
  "las ventas y los descuentos operen sobre datos existentes desde el
  primer día"; versionar el esquema y la semilla como SQL plano en
  `resources` hace que ese catálogo inicial exista de forma reproducible en
  cualquier entorno (dev, pruebas de integración con H2) sin pasos
  manuales. Usar `ddl-auto=validate` (en lugar de `update`/`create`) evita
  que Hibernate genere un esquema implícito que diverja del `schema.sql`
  versionado, manteniendo una única fuente de verdad para la estructura de
  datos.
- **Alternatives considered**:
  - *`spring.jpa.hibernate.ddl-auto=update`/`create-drop` sin `schema.sql`*:
    Es el camino de menor esfuerzo, pero el esquema queda implícito en las
    anotaciones JPA y no hay un lugar único y versionado para revisarlo ni
    para cargar datos semilla de forma determinista; además contradice la
    intención explícita del usuario de tener "un db con los schemas y
    datos precargados" en `resources`.
  - *Flyway/Liquibase*: Herramientas más robustas para evolucionar el
    esquema con migraciones versionadas e historial, recomendables si el
    proyecto crece o necesita despliegues incrementales en producción.
    Para esta fase (catálogo semilla inicial, sin historial de migraciones
    previas) añaden una dependencia y convención adicional no solicitada
    (YAGNI); se puede introducir más adelante si se requieren migraciones
    incrementales sobre datos ya existentes en producción.
