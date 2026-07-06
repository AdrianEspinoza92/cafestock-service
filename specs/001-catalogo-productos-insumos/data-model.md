# Phase 1 Data Model: Administrar catálogo semilla de productos e insumos

## Enumeraciones compartidas

### EstadoCatalogo

| Valor | Significado |
|---|---|
| `ACTIVO` | Disponible para nuevas ventas, recetas o configuraciones de mínimo. Valor por defecto al crear. |
| `INACTIVO` | Dado de baja lógica; oculto de las opciones para nuevas operaciones pero conservado para registros históricos (FR-012). |

**Transiciones válidas**: `ACTIVO → INACTIVO` (baja), `INACTIVO → ACTIVO`
(reactivación). No hay estado de borrado físico.

### UnidadMedida

Enumeración cerrada para la unidad de medida de un Insumo:
`UNIDAD`, `GRAMO`, `KILOGRAMO`, `MILILITRO`, `LITRO`.

*(Puede ampliarse en el futuro sin romper el contrato si se documenta como
extensión aditiva del enum en el OpenAPI.)*

## Entidades

### Producto

Artículo vendible del catálogo. Es referenciado (por identificador) desde
`Venta`, entidad que no administra esta funcionalidad.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | UUID | Generado por el sistema al crear. Inmutable. |
| `nombre` | String | Obligatorio, no vacío tras recortar espacios (FR-005). Único (case-insensitive) entre productos `ACTIVO` (ver Research §5). |
| `precio` | Decimal | Obligatorio, estrictamente mayor que 0 (FR-006). |
| `estado` | EstadoCatalogo | Por defecto `ACTIVO` al crear. |
| `creadoEn` | Timestamp | Auditoría, asignado por el sistema. |
| `actualizadoEn` | Timestamp | Auditoría, actualizado en cada edición o cambio de estado. |

**Reglas de negocio**:
- FR-001/FR-002: al crear, queda inmediatamente disponible (estado
  `ACTIVO`) para ser listado por la pantalla de venta.
- FR-007: la edición solo puede modificar `nombre` y/o `precio`; no
  modifica `id` ni el historial de ventas ya emitido con el precio
  anterior (FR-011 — el precio de una venta pasada se copia en el momento
  de la venta y no es una referencia viva a `Producto.precio`).
- FR-008/FR-009: cambio de `estado` vía operación explícita de
  activar/desactivar, no vía el mismo endpoint de edición de atributos.

### Insumo

Materia prima o ingrediente controlado por inventario. Es referenciado por
`Receta` y por configuraciones de stock mínimo, entidades no administradas
por esta funcionalidad.

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | UUID | Generado por el sistema al crear. Inmutable. |
| `nombre` | String | Obligatorio, no vacío tras recortar espacios (FR-005). Único (case-insensitive) entre insumos `ACTIVO`. |
| `unidadMedida` | UnidadMedida | Obligatorio. Editable (FR-007). |
| `stockInicial` | Decimal | Obligatorio al crear, mayor o igual a 0 (FR-006). Inmutable tras la creación: los ajustes de cantidad en curso pertenecen a una funcionalidad de inventario fuera de este alcance (ver Assumptions en spec.md). |
| `estado` | EstadoCatalogo | Por defecto `ACTIVO` al crear. |
| `creadoEn` | Timestamp | Auditoría. |
| `actualizadoEn` | Timestamp | Auditoría. |

**Reglas de negocio**:
- FR-003/FR-004: al crear, queda inmediatamente disponible (estado
  `ACTIVO`) para asociarse a recetas y a configuraciones de mínimo.
- FR-007: la edición solo puede modificar `nombre` y/o `unidadMedida`; NO
  expone forma de modificar `stockInicial` (eso es responsabilidad de una
  funcionalidad de inventario separada).
- FR-008/FR-009: activar/desactivar es una operación explícita e
  independiente de la edición de atributos.

## Esquema físico y datos semilla

El DDL de `producto` e `insumo` (columnas, tipos y la restricción única de
nombre normalizado descrita en Validaciones transversales) vive en
`src/main/resources/schema.sql`, y un catálogo inicial de ejemplo — ya en
estado `ACTIVO` — se carga desde `src/main/resources/data.sql` en cada
arranque. Ver [research.md](./research.md) §6 para la justificación de
usar SQL versionado en vez de generación automática de esquema.

## Entidades referenciadas (fuera de alcance de esta historia)

- **Venta**: registra, entre otros datos, el `productoId` y el precio al
  momento de la venta. No se crea, edita ni consulta desde esta
  funcionalidad; se documenta únicamente para dejar explícito que sus
  registros no deben verse afectados por ediciones o bajas de `Producto`
  (FR-011).
- **Receta**: asocia uno o más `Insumo` a un `Producto`. No se administra
  en esta historia; solo se garantiza que los `Insumo`/`Producto` activos
  quedan disponibles para que una futura funcionalidad de recetas los
  consuma (FR-004).

## Validaciones transversales

- Nombres se comparan normalizados (trim + lower-case) para detectar
  duplicados, tanto en `Producto` como en `Insumo`, cada uno en su propio
  espacio de nombres. La unicidad exigida por FR-005 aplica únicamente
  entre registros en estado `ACTIVO`: un nombre desactivado SÍ puede
  reutilizarse en un alta nueva. Se intentó un índice único filtrado
  (`CREATE UNIQUE INDEX ... WHERE estado = 'ACTIVO'`), pero H2 2.x no
  soporta esa sintaxis; se usa el fallback documentado: una restricción
  única compuesta sobre `(nombre_normalizado, estado)` en `schema.sql`,
  que bloquea duplicados dentro del mismo estado pero permite reutilizar
  un nombre una vez que el registro original quedó `INACTIVO`.
- Todo intento de alta con datos inválidos (nombre vacío/duplicado, precio
  ≤ 0, stock inicial < 0) se rechaza sin crear ningún registro (FR-005,
  FR-006, SC-004).
