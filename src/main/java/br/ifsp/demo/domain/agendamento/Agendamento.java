package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.exception.HorarioIndisponivelException;
import br.ifsp.demo.exception.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.Duration;
import java.time.LocalTime;

public class Agendamento {
    private static final int MAXIMO_DE_ITENS = 5;
    private static final int DURACAO_MAXIMA_EM_MINUTOS = 240;
    private static final int ANTECEDENCIA_MINIMA_REAGENDAMENTO_EM_MINUTOS = 120;
    private static final int MAXIMO_DE_REAGENDAMENTOS = 3;
    private static final LocalTime HORARIO_ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime HORARIO_FECHAMENTO = LocalTime.of(19, 0);
    private static final int JANELA_CONFIRMACAO_ABERTURA_EM_HORAS = 24;
    private static final int JANELA_CONFIRMACAO_ENCERRAMENTO_EM_MINUTOS = 30;

    private final AgendamentoId id;
    private final ClienteId clienteId;
    private final BarbeiroId barbeiroId;
    private final Contato contato;
    private Periodo periodo;
    private final List<ItemDeServico> itens;
    private StatusAgendamento status;
    private int quantidadeDeReagendamentos;
    private Avaliacao avaliacao;

    private Agendamento(AgendamentoId id, ClienteId clienteId, BarbeiroId barbeiroId, Contato contato,
                        Periodo periodo, List<ItemDeServico> itens, StatusAgendamento status,
                        int quantidadeDeReagendamentos, Avaliacao avaliacao) {
        this.id = id;
        this.clienteId = clienteId;
        this.barbeiroId = barbeiroId;
        this.contato = contato;
        this.periodo = periodo;
        this.itens = new ArrayList<>(itens);
        this.status = status;
        this.quantidadeDeReagendamentos = quantidadeDeReagendamentos;
        this.avaliacao = avaliacao;
    }

    public static Agendamento reconstituir(AgendamentoId id, ClienteId clienteId, BarbeiroId barbeiroId,
                                           Contato contato, Periodo periodo, List<ItemDeServico> itens,
                                           StatusAgendamento status, int quantidadeDeReagendamentos,
                                           Avaliacao avaliacao) {
        return new Agendamento(id, clienteId, barbeiroId, contato, periodo, itens, status,
                quantidadeDeReagendamentos, avaliacao);
    }

    public void adicionarItem(ItemDeServico item, AgendaDoBarbeiro agenda) {
        validarAgendamentoAtivo();
        validarLimiteDeItens();

        int duracaoComNovoItem = duracaoTotalEmMinutos() + item.getDuracaoEmMinutos();
        validarDuracaoMaxima(duracaoComNovoItem);

        Periodo novoPeriodo = new Periodo(periodo.inicio(), periodo.inicio().plusMinutes(duracaoComNovoItem));
        validarDisponibilidadeDeHorario(novoPeriodo, agenda);

        itens.add(item);
        this.periodo = novoPeriodo;
    }


    public void removerItem(ItemId itemId) {
        validarAgendamentoAtivo();

        if (itens.size() == 1) {
            throw new RegraDeNegocioException("O agendamento deve conter ao menos um serviço");
        }

        ItemDeServico itemParaRemover = null;
        for (ItemDeServico item : itens) {
            if (item.getId().equals(itemId)) {
                itemParaRemover = item;
            }
        }
        itens.remove(itemParaRemover);
        recalcularPeriodo();
    }

    public void cancelar(LocalDateTime horarioDoCancelamento) {
        if (status != StatusAgendamento.AGENDADO && status != StatusAgendamento.CONFIRMADO) {
            throw new RegraDeNegocioException("Apenas agendamentos com status 'agendado' ou 'confirmado' podem ser cancelados");
        }

        if (!horarioDoCancelamento.isBefore(periodo.inicio())) {
            throw new RegraDeNegocioException("O atendimento já foi iniciado");
        }

        this.status = StatusAgendamento.CANCELADO;
    }

    private void recalcularPeriodo() {
        int duracaoAtualizada = duracaoTotalEmMinutos();
        LocalDateTime novoFim = periodo.inicio().plusMinutes(duracaoAtualizada);
        this.periodo = new Periodo(periodo.inicio(), novoFim);
    }

    public void reagendarPara(LocalDateTime novoInicio, AgendaDoBarbeiro agenda, LocalDateTime agora) {
        Periodo novoPeriodo = new Periodo(novoInicio, novoInicio.plusMinutes(duracaoTotalEmMinutos()));
        validarReagendamento(novoPeriodo, agenda, agora);

        this.periodo = novoPeriodo;
        this.quantidadeDeReagendamentos++;
    }

    private void validarReagendamento(Periodo novoPeriodo, AgendaDoBarbeiro agenda, LocalDateTime agora) {
        validarLimiteDeReagendamentos();
        validarAntecedenciaMinima(agora);
        validarExpediente(novoPeriodo);
        validarDisponibilidadeDeHorario(novoPeriodo, agenda);
    }

    public void confirmarPresenca(LocalDateTime agora) {
        validarNaoConfirmado();
        Duration tempoAteInicio = Duration.between(agora, periodo.inicio());
        validarJanelaDeConfirmacaoAberta(tempoAteInicio);
        validarJanelaDeConfirmacaoNaoEncerrada(tempoAteInicio);
        this.status = StatusAgendamento.CONFIRMADO;
    }

    private void validarNaoConfirmado() {
        if (status == StatusAgendamento.CONFIRMADO) {
            throw new RegraDeNegocioException("O agendamento ja esta confirmado");
        }
    }

    private void validarJanelaDeConfirmacaoAberta(Duration tempoAteInicio) {
        if (tempoAteInicio.compareTo(Duration.ofHours(JANELA_CONFIRMACAO_ABERTURA_EM_HORAS)) > 0) {
            throw new RegraDeNegocioException("A confirmacao so e liberada 24 horas antes do atendimento");
        }
    }

    private void validarJanelaDeConfirmacaoNaoEncerrada(Duration tempoAteInicio) {
        if (tempoAteInicio.compareTo(Duration.ofMinutes(JANELA_CONFIRMACAO_ENCERRAMENTO_EM_MINUTOS)) < 0) {
            throw new RegraDeNegocioException("O prazo de confirmacao foi encerrado");
        }
    }

    public int duracaoTotalEmMinutos() {
        int total = 0;
        for (ItemDeServico item : itens) {
            total = total + item.getDuracaoEmMinutos();
        }
        return total;
    }

    public Dinheiro valorTotal() {
        Dinheiro total = new Dinheiro(BigDecimal.ZERO);
        for (ItemDeServico item : itens) {
            total = total.somar(item.getPreco());
        }
        return total;
    }

    private void validarAgendamentoAtivo() {
        if (status != StatusAgendamento.AGENDADO) {
            throw new RegraDeNegocioException("Apenas agendamentos ativos podem ser alterados");
        }
    }

    private void validarLimiteDeItens() {
        if (itens.size() == MAXIMO_DE_ITENS) {
            throw new RegraDeNegocioException("Limite de serviços por agendamento atingido");
        }
    }

    private void validarDuracaoMaxima(int duracaoEmMinutos) {
        if (duracaoEmMinutos > DURACAO_MAXIMA_EM_MINUTOS) {
            throw new RegraDeNegocioException("O agendamento ultrapassaria a duração máxima permitida");
        }
    }

    private void validarExpediente(Periodo periodo) {
        LocalTime inicio = periodo.inicio().toLocalTime();
        LocalTime fim = periodo.fim().toLocalTime();
        if (inicio.isBefore(HORARIO_ABERTURA) || fim.isAfter(HORARIO_FECHAMENTO)) {
            throw new RegraDeNegocioException("O periodo deve estar dentro do horario de funcionamento");
        }
    }

    private void validarDisponibilidadeDeHorario(Periodo novoPeriodo, AgendaDoBarbeiro agenda) {
        if (!agenda.estaLivre(novoPeriodo, this.id)) {
            throw new HorarioIndisponivelException("Horário indisponível para o barbeiro");
        }
    }

    private void validarLimiteDeReagendamentos() {
        if (quantidadeDeReagendamentos >= MAXIMO_DE_REAGENDAMENTOS) {
            throw new RegraDeNegocioException("Limite de reagendamentos atingido");
        }
    }

    private void validarAntecedenciaMinima(LocalDateTime agora) {
        long minutosAteInicio = Duration.between(agora, periodo.inicio()).toMinutes();
        if (minutosAteInicio < ANTECEDENCIA_MINIMA_REAGENDAMENTO_EM_MINUTOS) {
            throw new RegraDeNegocioException("Reagendamento exige no mínimo " + ANTECEDENCIA_MINIMA_REAGENDAMENTO_EM_MINUTOS + " minutos de antecedência");
        }
    }

    public List<ItemDeServico> getItens() {
        return itens;
    }

    public AgendamentoId getId() {
        return id;
    }

    public int getQuantidadeDeReagendamentos() { return quantidadeDeReagendamentos; }

    public Periodo getPeriodo() {
        return periodo;
    }

    public StatusAgendamento getStatus() { return status; }

}