# Quickstart: Validar el catálogo semilla de productos e insumos

## Prerrequisitos

- Java 17 y el wrapper de Gradle del repo (`./gradlew`).
- Contrato disponible en `contracts/catalog.openapi.yaml` (copiado a
  `src/main/resources/openapi/catalog.yaml` durante la implementación, ver
  [research.md](./research.md) §3).

## Levantar el servicio

```bash
./gradlew bootRun
```

El servicio queda disponible en `http://localhost:8080/api/v1` (H2 en
memoria/archivo, consola en `/h2-console` si está habilitada). Al arrancar,
Spring ejecuta automáticamente `src/main/resources/schema.sql` (crea las
tablas `producto`/`insumo`) y `src/main/resources/data.sql` (carga un
catálogo semilla de ejemplo ya activo) — ver Research §6 — por lo que
`GET /productos` y `GET /insumos` devuelven resultados sin necesidad de
ningún alta manual previa.

## Escenario 1 — Alta de producto (User Story 1)

```bash
curl -s -X POST http://localhost:8080/api/v1/productos \
  -H 'Content-Type: application/json' \
  -d '{"nombre": "Espresso", "precio": 1.50}'
```

**Resultado esperado**: `201 Created` con el producto en estado `ACTIVO`.

```bash
curl -s 'http://localhost:8080/api/v1/productos?estado=ACTIVO'
```

**Resultado esperado**: el producto "Espresso" aparece en la lista (esto es
lo que consume la pantalla de venta).

Casos negativos a validar manualmente:
- Repetir el `POST` con el mismo nombre → `409 Conflict`.
- `POST` con `precio: 0` o `precio: -1` → `400 Bad Request`.

## Escenario 2 — Alta de insumo (User Story 2)

```bash
curl -s -X POST http://localhost:8080/api/v1/insumos \
  -H 'Content-Type: application/json' \
  -d '{"nombre": "Leche entera", "unidadMedida": "LITRO", "stockInicial": 20}'
```

**Resultado esperado**: `201 Created` con el insumo en estado `ACTIVO`,
disponible para futuras recetas/mínimos.

Casos negativos a validar manualmente:
- `stockInicial: -5` → `400 Bad Request`.
- Nombre duplicado entre insumos activos → `409 Conflict`.

## Escenario 3 — Edición y baja de un registro existente (User Story 3)

```bash
# Editar precio de un producto
curl -s -X PATCH http://localhost:8080/api/v1/productos/{id} \
  -H 'Content-Type: application/json' \
  -d '{"precio": 1.75}'

# Desactivar el producto
curl -s -X POST http://localhost:8080/api/v1/productos/{id}/desactivar

# Verificar que ya no aparece en el listado de activos
curl -s 'http://localhost:8080/api/v1/productos?estado=ACTIVO'

# Reactivarlo
curl -s -X POST http://localhost:8080/api/v1/productos/{id}/activar
```

**Resultado esperado**: el precio cambia para nuevas consultas; tras
desactivar, el producto desaparece del listado de `ACTIVO` pero sigue
siendo consultable por `GET /productos/{id}`; cualquier venta previamente
registrada con este producto conserva su precio histórico (verificar contra
los datos de prueba de ventas existentes, sin que el `PATCH` los altere).

## Ejecutar las pruebas

```bash
./gradlew test                 # unit + integration + funcional (Cucumber)
./gradlew jacocoTestReport      # reporte HTML/XML de cobertura
./gradlew jacocoTestCoverageVerification   # falla si no se cumplen los umbrales (>80% por clase, ≥80% global)
```

Los escenarios funcionales en `src/test/resources/features/*.feature`
(`alta_producto.feature`, `alta_insumo.feature`,
`edicion_baja_catalogo.feature`) son la traducción ejecutable de los
Acceptance Scenarios de [spec.md](./spec.md); deben pasar en verde como
condición de aceptación de esta historia.
