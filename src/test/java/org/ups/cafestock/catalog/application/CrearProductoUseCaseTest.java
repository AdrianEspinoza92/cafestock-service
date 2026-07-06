package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.CrearProductoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrearProductoUseCaseTest {

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Test
    void debeCrearProductoCuandoNombreYPrecioSonValidos() {
        // given
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("espresso", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(productoRepositoryPort.guardar(any(Producto.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        CrearProductoUseCase useCase = new CrearProductoUseCase(productoRepositoryPort);

        // when
        Producto resultado = useCase.ejecutar("Espresso", new BigDecimal("1.50"));

        // then
        assertThat(resultado.getNombre()).isEqualTo("Espresso");
        assertThat(resultado.getPrecio()).isEqualByComparingTo("1.50");
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
        verify(productoRepositoryPort).guardar(any(Producto.class));
    }

    @Test
    void debeRechazarNombreVacio() {
        // given
        CrearProductoUseCase useCase = new CrearProductoUseCase(productoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("   ", new BigDecimal("1.50")))
                .isInstanceOf(ValorInvalidoException.class);
        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    void debeRechazarNombreDuplicadoEntreActivos() {
        // given
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("espresso", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        CrearProductoUseCase useCase = new CrearProductoUseCase(productoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("Espresso", new BigDecimal("1.50")))
                .isInstanceOf(NombreDuplicadoException.class);
        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }

    @Test
    void debeRechazarPrecioMenorOIgualACero() {
        // given
        CrearProductoUseCase useCase = new CrearProductoUseCase(productoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("Espresso", BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> useCase.ejecutar("Espresso", new BigDecimal("-1")))
                .isInstanceOf(ValorInvalidoException.class);
        verify(productoRepositoryPort, never()).guardar(any(Producto.class));
    }
}
