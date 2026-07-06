package org.ups.cafestock.catalog.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.cafestock.catalog.application.usecase.CrearInsumoUseCase;
import org.ups.cafestock.catalog.domain.exception.NombreDuplicadoException;
import org.ups.cafestock.catalog.domain.exception.ValorInvalidoException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.domain.port.InsumoRepositoryPort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrearInsumoUseCaseTest {

    @Mock
    private InsumoRepositoryPort insumoRepositoryPort;

    @Test
    void debeCrearInsumoCuandoDatosSonValidos() {
        // given
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("leche entera", EstadoCatalogo.ACTIVO))
                .willReturn(false);
        given(insumoRepositoryPort.guardar(any(Insumo.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        CrearInsumoUseCase useCase = new CrearInsumoUseCase(insumoRepositoryPort);

        // when
        Insumo resultado = useCase.ejecutar("Leche entera", UnidadMedida.LITRO, new BigDecimal("20"));

        // then
        assertThat(resultado.getNombre()).isEqualTo("Leche entera");
        assertThat(resultado.getUnidadMedida()).isEqualTo(UnidadMedida.LITRO);
        assertThat(resultado.getStockInicial()).isEqualByComparingTo("20");
        assertThat(resultado.getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
        verify(insumoRepositoryPort).guardar(any(Insumo.class));
    }

    @Test
    void debeRechazarSinUnidadDeMedida() {
        // given
        CrearInsumoUseCase useCase = new CrearInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("Leche entera", null, new BigDecimal("20")))
                .isInstanceOf(ValorInvalidoException.class);
        verify(insumoRepositoryPort, never()).guardar(any(Insumo.class));
    }

    @Test
    void debeRechazarStockNegativo() {
        // given
        CrearInsumoUseCase useCase = new CrearInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("Leche entera", UnidadMedida.LITRO, new BigDecimal("-1")))
                .isInstanceOf(ValorInvalidoException.class);
        verify(insumoRepositoryPort, never()).guardar(any(Insumo.class));
    }

    @Test
    void debeRechazarNombreDuplicadoEntreActivos() {
        // given
        given(insumoRepositoryPort.existsByNombreNormalizadoYEstado("leche entera", EstadoCatalogo.ACTIVO))
                .willReturn(true);
        CrearInsumoUseCase useCase = new CrearInsumoUseCase(insumoRepositoryPort);

        // when / then
        assertThatThrownBy(() -> useCase.ejecutar("Leche entera", UnidadMedida.LITRO, new BigDecimal("20")))
                .isInstanceOf(NombreDuplicadoException.class);
        verify(insumoRepositoryPort, never()).guardar(any(Insumo.class));
    }
}
