package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.EditarProductoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.HistorialPrecioProducto;
import org.ups.cafestock.catalog.domain.model.Producto;
import org.ups.cafestock.catalog.domain.port.HistorialPrecioProductoRepositoryPort;
import org.ups.cafestock.catalog.domain.port.ProductoRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EditarProductoUseCaseTest {

    @Mock
    private ProductoRepositoryPort productoRepositoryPort;

    @Mock
    private HistorialPrecioProductoRepositoryPort historialPrecioProductoRepositoryPort;

    @Test
    void debeEditarNombreYPrecioDeUnProductoExistente() {
        // given
        Producto existente = Producto.crear("Producto Original", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("producto editado", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(productoRepositoryPort.guardar(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when
        Producto resultado = useCase.ejecutar(existente.getId(), "Producto Editado", new BigDecimal("2.00"));

        // then
        assertThat(resultado.getNombre()).isEqualTo("Producto Editado");
        assertThat(resultado.getPrecio()).isEqualByComparingTo("2.00");
        verify(productoRepositoryPort).guardar(existente);
    }

    @Test
    void debeRegistrarElPrecioAnteriorEnElHistorialCuandoElPrecioCambia() {
        // given
        Producto existente = Producto.crear("Producto Historial", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(productoRepositoryPort.guardar(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when
        useCase.ejecutar(existente.getId(), null, new BigDecimal("5.00"));

        // then: FR-011/SC-003 — el precio reemplazado (1.00) queda persistido en el
        // historial antes de aplicar el nuevo precio (5.00)
        ArgumentCaptor<HistorialPrecioProducto> captor = ArgumentCaptor.forClass(HistorialPrecioProducto.class);
        verify(historialPrecioProductoRepositoryPort).guardar(captor.capture());
        assertThat(captor.getValue().getProductoId()).isEqualTo(existente.getId());
        assertThat(captor.getValue().getPrecio()).isEqualByComparingTo("1.00");
    }

    @Test
    void noDebeRegistrarHistorialCuandoElPrecioNoCambia() {
        // given
        Producto existente = Producto.crear("Producto Sin Cambio De Precio", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("nuevo nombre", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(productoRepositoryPort.guardar(any(Producto.class))).willAnswer(inv -> inv.getArgument(0));
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when: solo se edita el nombre, el precio permanece igual (1.00)
        useCase.ejecutar(existente.getId(), "Nuevo Nombre", new BigDecimal("1.00"));

        // then: no hay precio reemplazado, no debe crearse entrada de historial
        verify(historialPrecioProductoRepositoryPort, never()).guardar(any(HistorialPrecioProducto.class));
    }

    @Test
    void debeFallarCuandoElProductoNoExiste() {
        // given
        UUID idInexistente = UUID.randomUUID();
        given(productoRepositoryPort.buscarPorId(idInexistente)).willReturn(Optional.empty());
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(idInexistente, "Nombre", new BigDecimal("1.00")))
                .isInstanceOf(RegistroNoEncontradoException.class);
    }

    @Test
    void debeRechazarNombreDuplicadoAlEditar() {
        // given
        Producto existente = Producto.crear("Producto Original", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(productoRepositoryPort.existsByNombreNormalizadoYEstado("otro producto", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(existente.getId(), "Otro Producto", new BigDecimal("1.00")))
                .isInstanceOf(NombreDuplicadoException.class);
    }

    @Test
    void debeRechazarPrecioInvalidoAlEditar() {
        // given
        Producto existente = Producto.crear("Producto Original", new BigDecimal("1.00"));
        given(productoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        EditarProductoUseCase useCase =
                new EditarProductoUseCase(productoRepositoryPort, historialPrecioProductoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(existente.getId(), null, BigDecimal.ZERO))
                .isInstanceOf(ValorInvalidoException.class);
    }
}
