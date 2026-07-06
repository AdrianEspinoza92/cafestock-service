package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.CambiarEstadoInsumoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.RegistroNoEncontradoException;
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
class CambiarEstadoInsumoUseCaseTest {

    @Mock
    private InsumoRepositoryPort insumoRepositoryPort;

    @Test
    void debeDesactivarUnInsumoExistente() {
        // given
        Insumo insumo = Insumo.crear("Insumo Activo", UnidadMedida.LITRO, BigDecimal.TEN);
        given(insumoRepositoryPort.buscarPorId(insumo.getId())).willReturn(Optional.of(insumo));
        given(insumoRepositoryPort.guardar(any(Insumo.class))).willAnswer(inv -> inv.getArgument(0));
        CambiarEstadoInsumoUseCase useCase = new CambiarEstadoInsumoUseCase(insumoRepositoryPort);

        // when
        Insumo resultado = useCase.desactivar(insumo.getId());

        // then
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.INACTIVO);
    }

    @Test
    void debeActivarUnInsumoInactivoSinDuplicados() {
        // given
        Insumo insumo = Insumo.crear("Insumo Inactivo", UnidadMedida.LITRO, BigDecimal.TEN);
        insumo.desactivar();
        given(insumoRepositoryPort.buscarPorId(insumo.getId())).willReturn(Optional.of(insumo));
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("insumo inactivo", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(insumoRepositoryPort.guardar(any(Insumo.class))).willAnswer(inv -> inv.getArgument(0));
        CambiarEstadoInsumoUseCase useCase = new CambiarEstadoInsumoUseCase(insumoRepositoryPort);

        // when
        Insumo resultado = useCase.activar(insumo.getId());

        // then
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
    }

    @Test
    void debeRechazarActivacionSiYaHayOtroInsumoActivoConElMismoNombre() {
        // given
        Insumo insumo = Insumo.crear("Insumo Inactivo", UnidadMedida.LITRO, BigDecimal.TEN);
        insumo.desactivar();
        given(insumoRepositoryPort.buscarPorId(insumo.getId())).willReturn(Optional.of(insumo));
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("insumo inactivo", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        CambiarEstadoInsumoUseCase useCase = new CambiarEstadoInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.activar(insumo.getId()))
                .isInstanceOf(NombreDuplicadoException.class);
    }

    @Test
    void debeFallarCuandoElInsumoNoExiste() {
        // given
        UUID idInexistente = UUID.randomUUID();
        given(insumoRepositoryPort.buscarPorId(idInexistente)).willReturn(Optional.empty());
        CambiarEstadoInsumoUseCase useCase = new CambiarEstadoInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.desactivar(idInexistente))
                .isInstanceOf(RegistroNoEncontradoException.class);
    }
}
