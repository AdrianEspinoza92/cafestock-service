Feature: Edición y baja de productos e insumos existentes
  Como encargado de compras
  Quiero editar o desactivar/reactivar un producto o insumo existente
  Para mantener el catálogo actualizado sin afectar las ventas ya registradas

  Scenario: Edición exitosa de un producto existente
    Given que existe un producto activo llamado "Mocha Editable" con precio 2.00
    When edito el precio del producto "Mocha Editable" a 2.50
    Then el cambio se refleja en las pantallas que lo usan

  Scenario: Desactivar y reactivar un producto existente
    Given que existe un producto activo llamado "Producto Desactivable"
    When desactivo el producto "Producto Desactivable"
    Then el producto desaparece de la lista de productos activos
    When activo nuevamente el producto "Producto Desactivable"
    Then el producto vuelve a aparecer en la lista de productos activos

  Scenario: Desactivar y reactivar un insumo existente
    Given que existe un insumo activo llamado "Insumo Desactivable"
    When desactivo el insumo "Insumo Desactivable"
    Then el insumo desaparece de la lista de insumos activos
    When activo nuevamente el insumo "Insumo Desactivable"
    Then el insumo vuelve a aparecer en la lista de insumos activos
