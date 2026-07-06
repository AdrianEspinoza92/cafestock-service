MERGE INTO producto (id, nombre, nombre_normalizado, precio, estado, creado_en, actualizado_en)
KEY (id)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Espresso', 'espresso', 1.50, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'Cappuccino', 'cappuccino', 2.25, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'Latte', 'latte', 2.50, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

MERGE INTO insumo (id, nombre, nombre_normalizado, unidad_medida, stock_inicial, estado, creado_en, actualizado_en)
KEY (id)
VALUES
    ('44444444-4444-4444-4444-444444444444', 'Leche entera', 'leche entera', 'LITRO', 20.0, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', 'Café en grano', 'cafe en grano', 'KILOGRAMO', 10.0, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('66666666-6666-6666-6666-666666666666', 'Azúcar', 'azucar', 'KILOGRAMO', 15.0, 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
