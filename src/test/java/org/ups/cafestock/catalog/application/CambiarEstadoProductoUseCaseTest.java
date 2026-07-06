package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.CambiarEstadoProductoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CambiarEstadoProductoUseCaseTest {

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Test
    void debeDesactivarUnProductoExistente() {
        // given
        Producto producto = Producto.crear("Producto Activo", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(producto.getId())).willReturn(Optional.of(producto));
        given(productoRepositoryPort.guardar(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));
        CambiarEstadoProductoUseCase useCase = new CambiarEstadoProductoUseCase(productoRepositoryPort);

        // when
        Producto resultado = useCase.desactivar(producto.getId());

        // then
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.INACTIVO);
    }

    @Test
    void debeActivarUnProductoInactivoSinDuplicados() {
        // given
        Producto producto = Producto.crear("Producto Inactivo", new BigDecimal("1.00"));
        producto.desactivar();
        given(productoRepositoryPort.buscarPorId(producto.getId())).willReturn(Optional.of(producto));
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("producto inactivo", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(productoRepositoryPort.guardar(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));
        CambiarEstadoProductoUseCase useCase = new CambiarEstadoProductoUseCase(productoRepositoryPort);

        // when
        Producto resultado = useCase.activar(producto.getId());

        // then
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
    }

    @Test
    void debeRechazarActivacionSiYaHayOtroProductoActivoConElMismoNombre() {
        // given
        Producto producto = Producto.crear("Producto Inactivo", new BigDecimal("1.00"));
        producto.desactivar();
        given(productoRepositoryPort.buscarPorId(producto.getId())).willReturn(Optional.of(producto));
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("producto inactivo", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        CambiarEstadoProductoUseCase useCase = new CambiarEstadoProductoUseCase(productoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.activar(producto.getId()))
                .isInstanceOf(NombreDuplicadoException.class);
    }

    @Test
    void debeFallarCuandoElProductoNoExiste() {
        // given
        UUID idInexistente = UUID.randomUUID();
        given(productoRepositoryPort.buscarPorId(idInexistente)).willReturn(Optional.empty());
        CambiarEstadoProductoUseCase useCase = new CambiarEstadoProductoUseCase(productoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.desactivar(idInexistente))
                .isInstanceOf(RegistroNoEncontradoException.class);
    }
}
