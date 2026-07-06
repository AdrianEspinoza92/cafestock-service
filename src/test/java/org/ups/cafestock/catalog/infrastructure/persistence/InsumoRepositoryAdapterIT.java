package org.ups.cafestock.catalog.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.ups.cafestock.catalog.domain.model.EstadoCatalogo;
import org.ups.cafestock.catalog.domain.model.Insumo;
import org.ups.cafestock.catalog.domain.model.UnidadMedida;
import org.ups.cafestock.catalog.infrastructure.persistence.adapter.InsumoRepositoryAdapter;
import org.ups.cafestock.catalog.infrastructure.persistence.repository.InsumoJpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(InsumoRepositoryAdapter.class)
class InsumoRepositoryAdapterIT {

    @Autowired
    private InsumoRepositoryAdapter insumoRepositoryAdapter;

    @Autowired
    private InsumoJpaRepository insumoJpaRepository;

    @Test
    void debePersistirYRecuperarUnInsumo() {
        // given
        Insumo insumo = Insumo.crear("Test Insumo Persistencia", UnidadMedida.KILOGRAMO, new BigDecimal("5.5"));

        // when
        Insumo guardado = insumoRepositoryAdapter.guardar(insumo);
        Optional<Insumo> recuperado = insumoRepositoryAdapter.buscarPorId(guardado.getId());

        // then
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getNombre()).isEqualTo("Test Insumo Persistencia");
        assertThat(recuperado.get().getUnidadMedida()).isEqualTo(UnidadMedida.KILOGRAMO);
        assertThat(recuperado.get().getStockInicial()).isEqualByComparingTo("5.5");
        assertThat(recuperado.get().getEstado()).isEqualTo(EstadoCatalogo.ACTIVO);
    }

    @Test
    void debeRechazarPersistenciaDeNombreDuplicadoEnMismoEstado() {
        // given
        insumoRepositoryAdapter.guardar(Insumo.crear("Test Insumo Constraint", UnidadMedida.GRAMO, BigDecimal.ZERO));
        insumoJpaRepository.flush();

        // when
        Insumo duplicado = Insumo.crear("Test Insumo Constraint", UnidadMedida.GRAMO, BigDecimal.ONE);

        // then
        assertThatThrownBy(() -> {
            insumoRepositoryAdapter.guardar(duplicado);
            insumoJpaRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
