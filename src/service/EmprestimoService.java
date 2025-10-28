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

/** Regras de negócio de empréstimos, devoluções e multas. */
public class EmprestimoService {
    private final EmprestimoDAO emprestimoDAO;
    private final LivroDAO livroDAO;
    private final LeitorDAO leitorDAO;
    private final FuncionarioDAO funcionarioDAO;
    private final MultaDAO multaDAO;

    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    private final int LIMITE_EMPRESTIMOS_POR_LEITOR = 5;
    private final int PRAZO_PADRAO_DIAS = 7;
    private final double TAXA_MULTA_DIARIA = 1.00; // R$ 1,00/dia

    public EmprestimoService(EmprestimoDAO emprestimoDAO, LivroDAO livroDAO,
                             LeitorDAO leitorDAO, FuncionarioDAO funcionarioDAO, MultaDAO multaDAO) {
        this.emprestimoDAO = emprestimoDAO;
        this.livroDAO = livroDAO;
        this.leitorDAO = leitorDAO;
        this.funcionarioDAO = funcionarioDAO;
        this.multaDAO = multaDAO;
    }

    public Emprestimo emprestarLivro(int idLivro, int idLeitor, int idFuncionario, String dataEmprestimoStr)
            throws ServiceException {
        try {
            Livro livro = livroDAO.buscarPorId(idLivro);
            if (livro == null) throw new ServiceException("Livro não encontrado (id: " + idLivro + ")");
            if (!"Disponível".equalsIgnoreCase(livro.getStatus()))
                throw new ServiceException("Livro não está disponível (status: " + livro.getStatus() + ")");

            Leitor leitor = leitorDAO.buscarPorId(idLeitor);
            if (leitor == null) throw new ServiceException("Leitor não encontrado.");

            Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);
            if (funcionario == null) throw new ServiceException("Funcionário não encontrado.");

            long ativos = emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor && e.getData_devolucao() == null)
                    .count();
            if (ativos >= LIMITE_EMPRESTIMOS_POR_LEITOR)
                throw new ServiceException("Leitor atingiu o limite de empréstimos ativos (" + LIMITE_EMPRESTIMOS_POR_LEITOR + ").");

            LocalDate dataEmp = (dataEmprestimoStr == null || dataEmprestimoStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataEmprestimoStr, fmt);
            LocalDate dataPrev = dataEmp.plusDays(PRAZO_PADRAO_DIAS);

            Emprestimo emp = new Emprestimo(0, livro, funcionario,
                    dataEmp.format(fmt), dataPrev.format(fmt), null, leitor);

            emprestimoDAO.inserir(emp);
            livroDAO.atualizarStatus(livro.getId(), "Emprestado");

            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getLivro().getId() == livro.getId()
                            && e.getLeitor().getId() == leitor.getId()
                            && e.getData_emprestimo().equals(dataEmp.format(fmt)))
                    .findFirst().orElse(emp);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao efetuar empréstimo: " + e.getMessage(), e);
        }
    }

    /** Cria empréstimo com datas definidas (usado para gerar atraso de teste). */
    public Emprestimo criarEmprestimoAtrasado(int idLivro, int idLeitor, int idFuncionario,
                                              LocalDate dataEmp, LocalDate dataPrev) throws ServiceException {
        try {
            Livro livro = livroDAO.buscarPorId(idLivro);
            if (livro == null) throw new ServiceException("Livro não encontrado (id: " + idLivro + ")");
            if (!"Disponível".equalsIgnoreCase(livro.getStatus()))
                throw new ServiceException("Livro não está disponível (status: " + livro.getStatus() + ")");

            Leitor leitor = leitorDAO.buscarPorId(idLeitor);
            if (leitor == null) throw new ServiceException("Leitor não encontrado.");

            Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);
            if (funcionario == null) throw new ServiceException("Funcionário não encontrado.");

            long ativos = emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor && e.getData_devolucao() == null)
                    .count();
            if (ativos >= LIMITE_EMPRESTIMOS_POR_LEITOR)
                throw new ServiceException("Leitor atingiu o limite de empréstimos ativos (" + LIMITE_EMPRESTIMOS_POR_LEITOR + ").");

            Emprestimo emp = new Emprestimo(0, livro, funcionario,
                    dataEmp.format(fmt), dataPrev.format(fmt), null, leitor);

            emprestimoDAO.inserir(emp);
            livroDAO.atualizarStatus(livro.getId(), "Emprestado");

            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getLivro().getId() == livro.getId()
                            && e.getLeitor().getId() == leitor.getId()
                            && e.getData_emprestimo().equals(dataEmp.format(fmt)))
                    .findFirst().orElse(emp);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao criar empréstimo atrasado: " + e.getMessage(), e);
        }
    }

    /** Registra devolução; se houver atraso, cria multa (não paga) e libera o livro. */
    public void devolverLivro(int idEmprestimo, String dataDevolucaoStr) throws ServiceException {
        try {
            Emprestimo emprestimo = emprestimoDAO.buscarPorId(idEmprestimo);
            if (emprestimo == null) throw new ServiceException("Empréstimo não encontrado (id: " + idEmprestimo + ")");
            if (emprestimo.getData_devolucao() != null) throw new ServiceException("Empréstimo já devolvido.");

            LocalDate dataDev = (dataDevolucaoStr == null || dataDevolucaoStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataDevolucaoStr, fmt);

            // devolve
            emprestimoDAO.atualizarDevolucao(idEmprestimo, dataDev.format(fmt));

            // libera livro
            Livro livro = emprestimo.getLivro();
            if (livro != null) {
                livroDAO.atualizarStatus(livro.getId(), "Disponível");
            }

            // multa automática em caso de atraso
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
        if (emprestimo == null) return 0;
        try {
            LocalDate prevista = LocalDate.parse(emprestimo.getData_prevista(), fmt);
            LocalDate fim = (emprestimo.getData_devolucao() == null)
                    ? LocalDate.now()
                    : LocalDate.parse(emprestimo.getData_devolucao(), fmt);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(prevista, fim);
            return Math.max(0, dias);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Valor atual da multa considerando hoje se ainda não devolvido. */
    public double valorMultaAtual(Emprestimo emprestimo, LocalDate hoje) {
        long dias = calcularDiasAtraso(emprestimo);
        return dias * TAXA_MULTA_DIARIA;
    }

    /** Marca a multa do empréstimo como paga (upsert). */
    public void pagarMulta(int idEmprestimo, double valor, LocalDate data) throws ServiceException {
        try {
            multaDAO.registrarPagamento(idEmprestimo, valor, data.format(fmt));
        } catch (SQLException e) {
            throw new ServiceException("Erro ao registrar pagamento: " + e.getMessage(), e);
        }
    }

    /** Exceção de serviço. */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) { super(message); }
        public ServiceException(String message, Throwable cause) { super(message, cause); }
    }
}
