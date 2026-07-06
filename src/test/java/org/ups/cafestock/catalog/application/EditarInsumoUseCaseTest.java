package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.EditarInsumoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EditarInsumoUseCaseTest {

    @Mock
    private InsumoRepositoryPort insumoRepositoryPort;

    @Test
    void debeEditarNombreYUnidadDeMedidaDeUnInsumoExistente() {
        // given
        Insumo existente = Insumo.crear("Insumo Original", UnidadMedida.GRAMO, new BigDecimal("5"));
        given(insumoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("insumo editado", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(insumoRepositoryPort.guardar(any(Insumo.class))).willAnswer(inv -> inv.getArgument(0));
        EditarInsumoUseCase useCase = new EditarInsumoUseCase(insumoRepositoryPort);

        // when
        Insumo resultado = useCase.ejecutar(existente.getId(), "Insumo Editado", UnidadMedida.KILOGRAMO);

        // then
        assertThat(resultado.getNombre()).isEqualTo("Insumo Editado");
        assertThat(resultado.getUnidadMedida()).isEqualTo(UnidadMedida.KILOGRAMO);
    }

    @Test
    void debeFallarCuandoElInsumoNoExiste() {
        // given
        UUID idInexistente = UUID.randomUUID();
        given(insumoRepositoryPort.buscarPorId(idInexistente)).willReturn(Optional.empty());
        EditarInsumoUseCase useCase = new EditarInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(idInexistente, "Nombre", UnidadMedida.LITRO))
                .isInstanceOf(RegistroNoEncontradoException.class);
    }

    @Test
    void debeRechazarNombreDuplicadoAlEditar() {
        // given
        Insumo existente = Insumo.crear("Insumo Original", UnidadMedida.GRAMO, new BigDecimal("5"));
        given(insumoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("otro insumo", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        EditarInsumoUseCase useCase = new EditarInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(existente.getId(), "Otro Insumo", UnidadMedida.GRAMO))
                .isInstanceOf(NombreDuplicadoException.class);
    }

    @Test
    void debeRechazarNombreVacioAlEditar() {
        // given
        Insumo existente = Insumo.crear("Insumo Original", UnidadMedida.GRAMO, new BigDecimal("5"));
        given(insumoRepositoryPort.buscarPorId(existente.getId())).willReturn(Optional.of(existente));
        EditarInsumoUseCase useCase = new EditarInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar(existente.getId(), "   ", null))
                .isInstanceOf(ValorInvalidoException.class);
    }
}
