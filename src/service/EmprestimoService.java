package service;

import dao.EmprestimoDAO;
import dao.LivroDAO;
import dao.LeitorDAO;
import dao.FuncionarioDAO;
import dao.MultaDAO;
import model.Emprestimo;
import model.Livro;
import model.Leitor;
import model.Funcionario;
import model.Multa;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelas regras de negócio de empréstimo e devolução de
 * livros.
 * Valida disponibilidade, limite de empréstimos por usuário, prazos, atualiza
 * status do livro,
 * e delega persistência para os DAOs. Também cria multa se houver atraso na
 * devolução.
 */
public class EmprestimoService {
    private final EmprestimoDAO emprestimoDAO;
    private final LivroDAO livroDAO;
    private final LeitorDAO leitorDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final MultaDAO multaDAO;

    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // Regras configuráveis
    private final int LIMITE_EMPRESTIMOS_POR_LEITOR = 5; // máximo simultâneo
    private final int PRAZO_PADRAO_DIAS = 7; // dias padrão
    private final double TAXA_MULTA_DIARIA = 2.00; // R$ por dia de atraso

    public EmprestimoService(EmprestimoDAO emprestimoDAO, LivroDAO livroDAO,
            LeitorDAO leitorDAO, FuncionarioDAO funcionarioDAO, MultaDAO multaDAO) {
        this.emprestimoDAO = emprestimoDAO;
        this.livroDAO = livroDAO;
        this.leitorDAO = leitorDAO;
        this.funcionarioDAO = funcionarioDAO;
        this.multaDAO = multaDAO;
    }

    /** Cria um empréstimo (data prevista pode ser calculada pelo prazo padrão). */
    public Emprestimo emprestarLivro(int idLivro, int idLeitor, int idFuncionario, String dataEmprestimoStr)
            throws ServiceException {
        try {
            Livro livro = livroDAO.buscarPorId(idLivro);
            if (livro == null)
                throw new ServiceException("Livro não encontrado (id: " + idLivro + ")");
            if (!"Disponível".equalsIgnoreCase(livro.getStatus()))
                throw new ServiceException("Livro não está disponível (status: " + livro.getStatus() + ")");

            Leitor leitor = leitorDAO.buscarPorId(idLeitor);
            if (leitor == null)
                throw new ServiceException("Leitor não encontrado.");

            Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);
            if (funcionario == null)
                throw new ServiceException("Funcionário não encontrado.");

            // Verificar limite de empréstimos ativos do leitor
            long ativos = emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor
                            && e.getData_devolucao() == null)
                    .count();
            if (ativos >= LIMITE_EMPRESTIMOS_POR_LEITOR)
                throw new ServiceException(
                        "Leitor atingiu o limite de empréstimos ativos (" + LIMITE_EMPRESTIMOS_POR_LEITOR + ").");

            LocalDate dataEmp = (dataEmprestimoStr == null || dataEmprestimoStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataEmprestimoStr, fmt);
            LocalDate dataPrev = dataEmp.plusDays(PRAZO_PADRAO_DIAS);

            Emprestimo emp = new Emprestimo(0, livro, funcionario,
                    dataEmp.format(fmt), dataPrev.format(fmt), null, leitor);

            emprestimoDAO.inserir(emp);

            // Atualiza status do livro para "Emprestado"
            livroDAO.atualizarStatus(livro.getId(), "Emprestado");

            // Retorna um matching do que foi salvo (heurística)
            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getLivro().getId() == livro.getId()
                            && e.getLeitor().getId() == leitor.getId()
                            && e.getData_emprestimo().equals(dataEmp.format(fmt)))
                    .findFirst().orElse(emp);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao efetuar empréstimo: " + e.getMessage(), e);
        }
    }

    /** Registra devolução. Se houver atraso, gera multa automática. */
    public void devolverLivro(int idEmprestimo, String dataDevolucaoStr) throws ServiceException {
        try {
            Emprestimo emprestimo = emprestimoDAO.buscarPorId(idEmprestimo);
            if (emprestimo == null)
                throw new ServiceException("Empréstimo não encontrado (id: " + idEmprestimo + ")");
            if (emprestimo.getData_devolucao() != null)
                throw new ServiceException("Empréstimo já devolvido.");

            LocalDate dataDev = (dataDevolucaoStr == null || dataDevolucaoStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataDevolucaoStr, fmt);

            // Atualiza devolução
            emprestimoDAO.atualizarDevolucao(idEmprestimo, dataDev.format(fmt));

            // Atualiza livro para "Disponível"
            Livro livro = emprestimo.getLivro();
            if (livro != null) {
                livroDAO.atualizarStatus(livro.getId(), "Disponível");
            }

            // Multa se atraso
            LocalDate prevista = LocalDate.parse(emprestimo.getData_prevista(), fmt);
            long diasAtraso = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(prevista, dataDev));
            if (diasAtraso > 0) {
                double valor = diasAtraso * TAXA_MULTA_DIARIA;
                Multa m = new Multa(0, emprestimo, valor, false, null);
                multaDAO.inserir(m);
            }
        } catch (SQLException e) {
            throw new ServiceException("Erro ao registrar devolução: " + e.getMessage(), e);
        }
    }

    public boolean isLivroDisponivel(int idLivro) throws ServiceException {
        try {
            Livro l = livroDAO.buscarPorId(idLivro);
            return l != null && "Disponível".equalsIgnoreCase(l.getStatus());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao verificar disponibilidade do livro: " + e.getMessage(), e);
        }
    }

    public java.util.List<Emprestimo> listarAtrasados() throws ServiceException {
        try {
            LocalDate hoje = LocalDate.now();
            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getData_devolucao() == null &&
                            LocalDate.parse(e.getData_prevista(), fmt).isBefore(hoje))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar empréstimos: " + e.getMessage(), e);
        }
    }

    public java.util.List<Emprestimo> historicoDeLeitor(int idLeitor) throws ServiceException {
        try {
            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar histórico de empréstimos: " + e.getMessage(), e);
        }
    }

    public long calcularDiasAtraso(Emprestimo emprestimo) {
        if (emprestimo == null)
            return 0;
        try {
            LocalDate prevista = LocalDate.parse(emprestimo.getData_prevista(), fmt);
            LocalDate devolucao = emprestimo.getData_devolucao() == null ? LocalDate.now()
                    : LocalDate.parse(emprestimo.getData_devolucao(), fmt);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(prevista, devolucao);
            return Math.max(0, dias);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Exceção interna para erros do serviço. */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
