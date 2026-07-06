# Feature Specification: Administrar catálogo semilla de productos e insumos

**Feature Branch**: `001-catalogo-productos-insumos`

**Created**: 2026-07-05

**Status**: Draft

**Input**: User description: "US-00 · Administrar el catálogo semilla de productos, insumos y recetas · épica E-01 · 5 pts. Como encargado de compras, quiero administrar el catálogo semilla de productos e insumos (alta, edición y baja) que usa toda la aplicación, para que las ventas y los descuentos operen sobre datos existentes desde el primer día."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Alta de producto (Priority: P1)

Como encargado de compras, quiero dar de alta un producto nuevo indicando su
nombre y precio, para que quede disponible de inmediato en la pantalla de
venta.

**Why this priority**: Sin productos dados de alta no existe nada que vender;
esta es la capacidad mínima indispensable para que el resto de la aplicación
(ventas, descuentos) tenga datos sobre los que operar.

**Independent Test**: Puede probarse de forma aislada dando de alta un
producto con nombre y precio válidos y verificando que aparece como opción
seleccionable en la pantalla de venta.

**Acceptance Scenarios**:

1. **Given** que soy encargado de compras, **When** doy de alta un producto
   con nombre y precio, **Then** queda disponible para marcarse en la
   pantalla de venta.
2. **Given** que intento dar de alta un producto con nombre vacío o precio
   inválido (cero o negativo), **When** confirmo el alta, **Then** el
   sistema rechaza la operación y muestra el motivo, sin crear el producto.
3. **Given** que ya existe un producto activo con un nombre determinado,
   **When** intento dar de alta otro producto con el mismo nombre, **Then**
   el sistema rechaza el alta por duplicado.

---

### User Story 2 - Alta de insumo (Priority: P1)

Como encargado de compras, quiero dar de alta un insumo nuevo indicando su
unidad de medida y su stock inicial, para que quede disponible para
asociarse a recetas y a niveles mínimos de stock.

**Why this priority**: Los insumos son la base del control de inventario y
de las recetas; sin ellos no se puede calcular consumo ni configurar
mínimos, por lo que tiene la misma urgencia que el alta de productos.

**Independent Test**: Puede probarse de forma aislada dando de alta un
insumo con unidad de medida y stock inicial válidos y verificando que queda
disponible para asociarse a una receta o a un mínimo de stock.

**Acceptance Scenarios**:

1. **Given** que soy encargado de compras, **When** doy de alta un insumo
   con unidad de medida y stock inicial, **Then** queda disponible para
   asociarse a recetas y a mínimos.
2. **Given** que intento dar de alta un insumo con stock inicial negativo o
   sin unidad de medida, **When** confirmo el alta, **Then** el sistema
   rechaza la operación y muestra el motivo, sin crear el insumo.
3. **Given** que ya existe un insumo activo con un nombre determinado,
   **When** intento dar de alta otro insumo con el mismo nombre, **Then**
   el sistema rechaza el alta por duplicado.

---

### User Story 3 - Edición y baja de productos e insumos existentes (Priority: P2)

Como encargado de compras, quiero editar o desactivar un producto o insumo
existente, para mantener el catálogo actualizado sin afectar las ventas ya
registradas.

**Why this priority**: Depende de que existan productos e insumos dados de
alta (US1/US2); una vez que el catálogo tiene datos, mantenerlo correcto en
el tiempo es el siguiente valor más importante.

**Independent Test**: Puede probarse de forma aislada editando el nombre o
precio de un producto existente (o la unidad de medida de un insumo
existente) y verificando que el cambio se refleja en las pantallas que lo
usan; y también desactivando un producto o insumo y verificando que deja de
ofrecerse como opción nueva sin alterar ventas históricas que ya lo
referenciaban.

**Acceptance Scenarios**:

1. **Given** un producto o insumo existente, **When** edito su registro
   (por ejemplo nombre, precio o unidad de medida), **Then** el cambio se
   refleja en las pantallas que lo usan sin romper ventas ya registradas.
2. **Given** un producto o insumo existente, **When** lo desactivo, **Then**
   deja de estar disponible para nuevas ventas, recetas o mínimos, pero las
   ventas ya registradas que lo referencian permanecen intactas y
   consultables.
3. **Given** un producto o insumo desactivado, **When** lo reactivo,
   **Then** vuelve a estar disponible como opción para nuevas operaciones.

---

### Edge Cases

- ¿Qué sucede si se intenta editar el precio de un producto que ya tiene
  ventas registradas? El precio histórico de cada venta ya registrada no
  debe modificarse; solo las ventas futuras usan el nuevo precio. Como la
  funcionalidad de Ventas es futura y no forma parte de esta historia (ver
  Assumptions), lo que esta historia garantiza concretamente es que el
  precio reemplazado en cada edición queda preservado en un historial de
  precio del producto, inmutable ante ediciones posteriores; la futura
  historia de Ventas debe consumir ese historial (o el precio vigente al
  momento de cada venta) en vez de recalcularlo desde el precio actual del
  producto.
- ¿Qué sucede si se intenta desactivar un insumo que está asociado a una
  receta activa? **Fuera de alcance de esta historia**: la funcionalidad de
  Recetas es futura (ver Assumptions) y hoy no existe ninguna asociación
  Insumo-Receta que verificar. Cuando se implemente Recetas, esa historia
  DEBE definir e implementar la advertencia descrita aquí (permitir la
  desactivación pero advertir que la receta que lo usa quedará afectada).
- ¿Qué sucede si se intenta dar de baja (desactivar) un producto o insumo
  que no tiene ninguna venta ni receta asociada? Debe desactivarse sin
  restricciones adicionales.
- ¿Cómo maneja el sistema el intento de crear un producto o insumo con
  nombre duplicado (incluyendo variaciones de mayúsculas/minúsculas o
  espacios)? Debe rechazarse como duplicado.
- ¿Qué ocurre si se intenta reducir el stock inicial de un insumo por medio
  de la edición del catálogo? La edición del catálogo no ajusta cantidades
  de stock en curso; los ajustes de inventario son responsabilidad de otra
  funcionalidad fuera de este alcance.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al encargado de compras dar de alta
  un producto especificando nombre y precio.
- **FR-002**: El sistema DEBE hacer que un producto recién creado quede
  disponible de inmediato como opción seleccionable en la pantalla de
  venta.
- **FR-003**: El sistema DEBE permitir al encargado de compras dar de alta
  un insumo especificando unidad de medida y stock inicial.
- **FR-004**: El sistema DEBE hacer que un insumo recién creado quede
  disponible de inmediato para asociarse a recetas y a niveles mínimos de
  stock.
- **FR-005**: El sistema DEBE validar que el nombre de un producto o
  insumo no esté vacío y no esté duplicado entre los registros activos.
- **FR-006**: El sistema DEBE validar que el precio de un producto sea
  mayor que cero y que el stock inicial de un insumo sea mayor o igual a
  cero.
- **FR-007**: El sistema DEBE permitir editar los atributos de un producto
  (nombre, precio) o de un insumo (nombre, unidad de medida) existente.
- **FR-008**: El sistema DEBE permitir desactivar (baja lógica) un producto
  o un insumo existente sin eliminarlo físicamente del sistema.
- **FR-009**: El sistema DEBE permitir reactivar un producto o insumo
  previamente desactivado.
- **FR-010**: El sistema DEBE propagar automáticamente cualquier edición o
  cambio de estado (activo/inactivo) de un producto o insumo a todas las
  pantallas que lo consumen (venta, recetas, mínimos), sin requerir
  sincronización manual.
- **FR-011**: El sistema DEBE preservar sin cambios las ventas ya
  registradas (incluido el precio con el que se vendieron) cuando el
  producto o insumo asociado sea editado o desactivado posteriormente. La
  funcionalidad de Ventas es futura y no se administra en esta historia
  (ver Key Entities y Assumptions); esta historia cumple la parte que le
  corresponde manteniendo un historial de precio del producto: cada vez
  que el precio vigente cambia, el precio reemplazado queda registrado de
  forma inmutable, disponible para que la futura historia de Ventas lo
  consulte en vez de depender del precio actual del producto.
- **FR-012**: El sistema DEBE excluir los productos e insumos desactivados
  de las opciones ofrecidas para nuevas ventas, recetas o configuraciones
  de mínimo, manteniéndolos visibles únicamente en los registros
  históricos que ya los referencian.

### Key Entities *(include if feature involves data)*

- **Producto**: Artículo vendible del catálogo. Atributos clave: nombre,
  precio, estado (activo/inactivo). Es referenciado por las ventas.
- **Insumo**: Materia prima o ingrediente controlado por inventario.
  Atributos clave: nombre, unidad de medida, stock inicial/actual, estado
  (activo/inactivo). Es referenciado por recetas y por configuraciones de
  stock mínimo.
- **Venta** *(referenciada, no administrada por esta funcionalidad)*:
  Registro histórico que queda inalterado ante ediciones o bajas
  posteriores del producto o insumo que referencia.
- **HistorialPrecioProducto** *(administrada por esta funcionalidad, en
  soporte de FR-011/SC-003)*: Registro append-only del precio de un
  Producto que queda reemplazado en cada edición de precio. Es el
  mecanismo concreto con el que esta historia demuestra la preservación de
  precios que exige FR-011, sin necesitar la entidad Venta.
- **Receta** *(referenciada, no administrada por esta funcionalidad)*:
  Asociación entre uno o más insumos y un producto; esta funcionalidad solo
  garantiza que los insumos e productos activos estén disponibles para que
  la administración de recetas los use.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un encargado de compras puede dar de alta un producto o un
  insumo nuevo en menos de 1 minuto.
- **SC-002**: El 100% de los productos e insumos dados de alta aparecen
  disponibles en las pantallas correspondientes (venta, recetas, mínimos)
  inmediatamente después de guardarse, sin pasos manuales adicionales.
- **SC-003**: El 100% de los precios reemplazados por una edición de
  producto quedan preservados de forma inmutable en el historial de precio
  (proxy verificable de "las ventas registradas conservan sus datos
  originales" mientras la funcionalidad de Ventas no exista — ver FR-011).
- **SC-004**: El 0% de los intentos de alta con datos inválidos (nombre
  vacío, duplicado, precio o stock fuera de rango) resulta en un registro
  creado.

## Assumptions

- El único rol que administra este catálogo es "encargado de compras"; no
  se definen en esta funcionalidad otros roles con permisos distintos sobre
  productos e insumos.
- "Baja" se interpreta como desactivación lógica (el registro deja de estar
  disponible para nuevas operaciones) y no como eliminación física, para
  preservar la integridad de ventas y recetas históricas.
- La edición del catálogo cubre atributos descriptivos (nombre, precio,
  unidad de medida); los ajustes de cantidades de stock en curso (entradas,
  salidas, mermas) pertenecen a una funcionalidad de inventario separada y
  quedan fuera del alcance de esta historia.
- La administración de recetas (creación/edición de la relación
  insumo-producto) es una funcionalidad distinta y futura; esta historia
  solo asegura que productos e insumos activos estén disponibles para que
  esa funcionalidad los consuma. Por lo mismo, el edge case de "advertir al
  desactivar un insumo asociado a una receta activa" queda fuera de
  alcance: no existe asociación Insumo-Receta que verificar hasta que esa
  funcionalidad exista.
- La administración de ventas es igualmente una funcionalidad futura fuera
  de alcance de esta historia. FR-011/SC-003 (preservar el precio de ventas
  ya registradas) se satisfacen dentro de este alcance mediante un
  historial de precio del propio Producto (`HistorialPrecioProducto`): se
  acepta que este mecanismo — y no un registro de Venta real — es la
  evidencia válida de cumplimiento hasta que la historia de Ventas exista y
  lo consuma.
- No se especifican reglas de auditoría (quién y cuándo editó un registro)
  en esta historia; se asume que quedan fuera de alcance salvo que se
  indique lo contrario en una historia posterior.
