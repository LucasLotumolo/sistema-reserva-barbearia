package br.ifsp.demo.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Configuração do relógio do sistema")
class RelogioConfigTest {

    @Test
    @Tag("UnitTest")
    @Tag("Mutation")
    @DisplayName("O relógio fornecido usa o fuso padrão do sistema")
    void relogioDeveUsarOFusoPadraoDoSistema() {
        Clock clock = new RelogioConfig().clock();

        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(ZoneId.systemDefault());
    }
}
