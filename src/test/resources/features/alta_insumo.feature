Feature: Alta de insumo
  Como encargado de compras
  Quiero dar de alta un insumo nuevo
  Para que quede disponible para asociarse a recetas y a mínimos

  Scenario: Alta exitosa de un insumo nuevo
    Given que soy encargado de compras
    When doy de alta un insumo con nombre "Vainilla" unidad de medida "MILILITRO" y stock inicial 5
    Then el insumo queda disponible para asociarse a recetas y a mínimos

  Scenario: Rechazo de alta con stock inicial negativo
    Given que soy encargado de compras
    When intento dar de alta un insumo con nombre "Canela" unidad de medida "GRAMO" y stock inicial -1
    Then el sistema rechaza la operación y no crea el insumo

  Scenario: Rechazo de alta con nombre duplicado
    Given que ya existe un insumo activo llamado "Leche entera"
    When intento dar de alta otro insumo con nombre "Leche entera" unidad de medida "LITRO" y stock inicial 10
    Then el sistema rechaza el alta de insumo por duplicado

  Scenario: Rechazo de alta con nombre duplicado en mayúsculas y con espacios de borde
    Given que ya existe un insumo activo llamado "Leche entera"
    When intento dar de alta otro insumo con nombre "  LECHE ENTERA  " unidad de medida "LITRO" y stock inicial 10
    Then el sistema rechaza el alta de insumo por duplicado
