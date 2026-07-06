Feature: Alta de producto
  Como encargado de compras
  Quiero dar de alta un producto nuevo
  Para que quede disponible de inmediato en la pantalla de venta

  Scenario: Alta exitosa de un producto nuevo
    Given que soy encargado de compras
    When doy de alta un producto con nombre "Mocha" y precio 2.75
    Then el producto queda disponible para marcarse en la pantalla de venta

  Scenario: Rechazo de alta con datos inválidos
    Given que soy encargado de compras
    When intento dar de alta un producto con nombre "" y precio 2.75
    Then el sistema rechaza la operación y no crea el producto

  Scenario: Rechazo de alta con nombre duplicado
    Given que ya existe un producto activo llamado "Espresso"
    When intento dar de alta otro producto con nombre "Espresso" y precio 1.80
    Then el sistema rechaza el alta por duplicado
