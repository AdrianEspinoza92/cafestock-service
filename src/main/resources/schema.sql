-- Nota: H2 no soporta índices únicos filtrados (CREATE UNIQUE INDEX ... WHERE).
-- Se usa el fallback documentado en data-model.md: restricción única compuesta
-- (nombre_normalizado, estado), que bloquea duplicados dentro del mismo estado
-- pero permite reutilizar un nombre cuando el registro original está INACTIVO.
CREATE TABLE IF NOT EXISTS producto (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    nombre_normalizado VARCHAR(255) NOT NULL,
    precio DECIMAL(19, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP NOT NULL,
    CONSTRAINT uq_producto_nombre_estado UNIQUE (nombre_normalizado, estado)
);

CREATE TABLE IF NOT EXISTS insumo (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    nombre_normalizado VARCHAR(255) NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    stock_inicial DECIMAL(19, 3) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    actualizado_en TIMESTAMP NOT NULL,
    CONSTRAINT uq_insumo_nombre_estado UNIQUE (nombre_normalizado, estado)
);

-- FR-011/SC-003: registro append-only del precio reemplazado en cada edición
-- de Producto. Permite verificar que un precio ya persistido (equivalente al
-- que capturaría una futura Venta) no se ve afectado por ediciones
-- posteriores, sin necesitar la entidad Venta (fuera de alcance de esta historia).
CREATE TABLE IF NOT EXISTS producto_precio_historico (
    id UUID PRIMARY KEY,
    producto_id UUID NOT NULL REFERENCES producto(id),
    precio DECIMAL(19, 2) NOT NULL,
    registrado_en TIMESTAMP NOT NULL
);
