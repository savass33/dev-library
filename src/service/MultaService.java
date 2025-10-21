package service;

import dao.MultaDAO;
import dao.EmprestimoDAO;
import model.Multa;
import model.Emprestimo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pelo cálculo, geração e quitação de multas.
 * Integra-se diretamente com os registros de empréstimo (para determinar atraso).
 */
public class MultaService {
    private final MultaDAO multaDAO;
    private final EmprestimoDAO emprestimoDAO;
    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // Regras configuráveis
    private final double VALOR_DIARIO_PADRAO = 1.0; // exemplo: R$1,00 por dia de atraso
    private final double VALOR_MAXIMO_POR_MULTA = 200.0; // teto por multa, se desejar
    private final int DIAS_ISENCAO = 0; // se houver isenção de X dias (ex: 0 = sem isenção)

    public MultaService(MultaDAO multaDAO, EmprestimoDAO emprestimoDAO) {
        this.multaDAO = multaDAO;
        this.emprestimoDAO = emprestimoDAO;
    }

    /**
     * Gera uma multa para um empréstimo caso ele esteja em atraso.
     * Se já existir uma multa vinculada ao empréstimo, atualiza o valor.
     *
     * @param idEmprestimo id do empréstimo
     * @param valorDiario  opcional: valor diário a ser usado (se <=0 usa padrão)
     * @throws ServiceException em caso de erro
     */
    public Multa gerarOuAtualizarMultaPorAtraso(int idEmprestimo, double valorDiario) throws ServiceException {
        try {
            Emprestimo emp = emprestimoDAO.buscarPorId(idEmprestimo);
            if (emp == null) throw new ServiceException("Empréstimo não encontrado (id: " + idEmprestimo + ")");

            long diasAtraso = calcularDiasAtraso(emp);
            if (diasAtraso <= DIAS_ISENCAO) {
                // Sem multa
                return null;
            }

            double rate = (valorDiario > 0) ? valorDiario : VALOR_DIARIO_PADRAO;
            double valor = diasAtraso * rate;
            if (valor > VALOR_MAXIMO_POR_MULTA) valor = VALOR_MAXIMO_POR_MULTA;

            // Verificar se já existe multa para esse empréstimo
            List<Multa> multas = multaDAO.listar();
            Multa existente = multas.stream()
                    .filter(m -> m.getEmprestimo() != null && m.getEmprestimo().getid() == idEmprestimo)
                    .findFirst().orElse(null);

            String hojeStr = LocalDate.now().format(fmt);
            if (existente != null) {
                existente.setValor(valor);
                existente.setPago(false);
                existente.setData_pagamento(null);
                multaDAO.excluir(existente.getId()); // simplificamos atualizando: excluir + inserir (ou você pode criar update)
                Multa nova = new Multa(0, emp, valor, false, null);
                multaDAO.inserir(nova);
                return nova;
            } else {
                Multa nova = new Multa(0, emp, valor, false, null);
                multaDAO.inserir(nova);
                return nova;
            }
        } catch (SQLException e) {
            throw new ServiceException("Erro ao gerar/atualizar multa: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula dias de atraso para um empréstimo.
     *
     * @param emprestimo empréstimo a avaliar
     * @return dias de atraso (>=0)
     */
    public long calcularDiasAtraso(Emprestimo emprestimo) {
        if (emprestimo == null) return 0;
        try {
            LocalDate prevista = LocalDate.parse(emprestimo.getData_prevista(), fmt);
            LocalDate devolucao = emprestimo.getData_devolucao() == null ? LocalDate.now() : LocalDate.parse(emprestimo.getData_devolucao(), fmt);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(prevista, devolucao);
            return Math.max(0, dias - DIAS_ISENCAO);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Marca uma multa como paga e grava data de pagamento.
     *
     * @param idMulta id da multa
     * @param dataPagamentoStr data do pagamento (yyyy-MM-dd) ou null para hoje
     * @throws ServiceException em caso de erro
     */
    public void pagarMulta(int idMulta, String dataPagamentoStr) throws ServiceException {
        try {
            Multa m = multaDAO.buscarPorId(idMulta);
            if (m == null) throw new ServiceException("Multa não encontrada (id: " + idMulta + ")");
            String data = (dataPagamentoStr == null || dataPagamentoStr.isBlank())
                    ? LocalDate.now().format(fmt)
                    : LocalDate.parse(dataPagamentoStr, fmt).format(fmt);

            multaDAO.atualizarPagamento(idMulta, true, data);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao pagar multa: " + e.getMessage(), e);
        }
    }

    /**
     * Busca todas as multas pendentes (pago == false).
     *
     * @return lista de multas pendentes
     * @throws ServiceException em caso de erro
     */
    public List<Multa> listarMultasPendentes() throws ServiceException {
        try {
            return multaDAO.listar().stream().filter(m -> !m.isPago()).toList();
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar multas: " + e.getMessage(), e);
        }
    }

    /**
     * Exceção interna para erros do serviço.
     */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) { super(message); }
        public ServiceException(String message, Throwable cause) { super(message, cause); }
    }
}
