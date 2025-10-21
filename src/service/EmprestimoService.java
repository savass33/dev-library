package service;

import dao.EmprestimoDAO;
import dao.LivroDAO;
import dao.LeitorDAO;
import dao.FuncionarioDAO;
import model.Emprestimo;
import model.Livro;
import model.Leitor;
import model.Funcionario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço responsável pelas regras de negócio de empréstimo e devolução de livros.
 * Valida disponibilidade, limite de empréstimos por usuário, prazos, atualiza status de livro,
 * e delega persistência para os DAOs.
 */
public class EmprestimoService {
    private final EmprestimoDAO emprestimoDAO;
    private final LivroDAO livroDAO;
    private final LeitorDAO leitorDAO;
    private final FuncionarioDAO funcionarioDAO;

    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    // Regras configuráveis
    private final int LIMITE_EMPRESTIMOS_POR_LEITOR = 5; // exemplo
    private final int PRAZO_PADRAO_DIAS = 7; // dias por empréstimo padrão

    public EmprestimoService(EmprestimoDAO emprestimoDAO, LivroDAO livroDAO,
                             LeitorDAO leitorDAO, FuncionarioDAO funcionarioDAO) {
        this.emprestimoDAO = emprestimoDAO;
        this.livroDAO = livroDAO;
        this.leitorDAO = leitorDAO;
        this.funcionarioDAO = funcionarioDAO;
    }

    /**
     * Efetua um empréstimo de livro a um leitor, validando disponibilidade e limite de empréstimos.
     *
     * @param idLivro      id do livro a emprestar
     * @param idLeitor     id do leitor que pega emprestado
     * @param idFuncionario id do funcionário que registra o empréstimo
     * @param dataEmprestimoDataString opcional: data do empréstimo (yyyy-MM-dd). Se nulo usa hoje.
     * @return Emprestimo criado
     * @throws ServiceException em caso de violação de regra ou erro de persistência
     */
    public Emprestimo emprestarLivro(int idLivro, int idLeitor, int idFuncionario, String dataEmprestimoDataString) throws ServiceException {
        try {
            Livro livro = livroDAO.buscarPorId(idLivro);
            if (livro == null) throw new ServiceException("Livro não encontrado (id: " + idLivro + ")");

            if (!"Disponível".equalsIgnoreCase(livro.getStatus())) {
                throw new ServiceException("Livro não está disponível para empréstimo (status: " + livro.getStatus() + ")");
            }

            Leitor leitor = leitorDAO.buscarPorId(idLeitor);
            if (leitor == null) throw new ServiceException("Leitor não encontrado (id: " + idLeitor + ")");

            Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);
            if (funcionario == null) throw new ServiceException("Funcionário não encontrado (id: " + idFuncionario + ")");

            // Verificar limite de empréstimos ativos do leitor
            List<Emprestimo> atuais = emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor && e.getData_devolucao() == null)
                    .collect(Collectors.toList());

            if (atuais.size() >= LIMITE_EMPRESTIMOS_POR_LEITOR) {
                throw new ServiceException("Leitor atingiu o limite de empréstimos ativos (" + LIMITE_EMPRESTIMOS_POR_LEITOR + ")");
            }

            LocalDate dataEmprestimo = (dataEmprestimoDataString == null || dataEmprestimoDataString.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataEmprestimoDataString, fmt);

            LocalDate dataPrevista = dataEmprestimo.plusDays(PRAZO_PADRAO_DIAS);

            Emprestimo emprestimo = new Emprestimo(
                    0,
                    livro,
                    funcionario,
                    dataEmprestimo.format(fmt),
                    dataPrevista.format(fmt),
                    null,
                    leitor
            );

            emprestimoDAO.inserir(emprestimo);

            // Atualiza status do livro para "Emprestado"
            livro.setStatus("Emprestado");
            livroDAO.atualizarStatus(livro.getId(), livro.getStatus());

            // Recarrega e retorna o empréstimo recém-criado - busca por critérios (por simplificação busca último empréstimo do leitor + livro)
            List<Emprestimo> todos = emprestimoDAO.listar();
            // heurística: procurar empréstimo com mesmo livro, leitor e dataEmprestimo igual
            Optional<Emprestimo> created = todos.stream()
                    .filter(e ->
                            e.getLivro() != null && e.getLivro().getId() == livro.getId() &&
                            e.getLeitor() != null && e.getLeitor().getId() == leitor.getId() &&
                            dataEmprestimo.format(fmt).equals(e.getData_emprestimo()))
                    .findFirst();

            return created.orElse(null);
        } catch (SQLException e) {
            throw new ServiceException("Erro ao efetuar empréstimo: " + e.getMessage(), e);
        }
    }

    /**
     * Registra a devolução de um empréstimo, atualiza data de devolução e status do livro.
     *
     * @param idEmprestimo    id do empréstimo a devolver
     * @param dataDevolucaoStr opcional: data de devolução (yyyy-MM-dd). Se nulo, usa hoje.
     * @throws ServiceException em caso de erro de regra ou persistência
     */
    public void devolverLivro(int idEmprestimo, String dataDevolucaoStr) throws ServiceException {
        try {
            Emprestimo emprestimo = emprestimoDAO.buscarPorId(idEmprestimo);
            if (emprestimo == null) throw new ServiceException("Empréstimo não encontrado (id: " + idEmprestimo + ")");

            if (emprestimo.getData_devolucao() != null) {
                throw new ServiceException("Empréstimo já possui data de devolução registrada.");
            }

            LocalDate dataDevolucao = (dataDevolucaoStr == null || dataDevolucaoStr.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(dataDevolucaoStr, fmt);

            emprestimoDAO.atualizarDevolucao(idEmprestimo, dataDevolucao.format(fmt));

            // Atualiza status do livro para "Disponível"
            Livro livro = emprestimo.getLivro();
            if (livro != null) {
                livro.setStatus("Disponível");
                livroDAO.atualizarStatus(livro.getId(), livro.getStatus());
            }
        } catch (SQLException e) {
            throw new ServiceException("Erro ao registrar devolução: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica se um livro está disponível (status == "Disponível").
     *
     * @param idLivro id do livro
     * @return true se disponível
     * @throws ServiceException em caso de erro de acesso ao DAO
     */
    public boolean isLivroDisponivel(int idLivro) throws ServiceException {
        try {
            Livro l = livroDAO.buscarPorId(idLivro);
            return l != null && "Disponível".equalsIgnoreCase(l.getStatus());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao verificar disponibilidade do livro: " + e.getMessage(), e);
        }
    }

    /**
     * Lista empréstimos atualmente em atraso (data_prevista < hoje e data_devolucao == null).
     *
     * @return lista de empréstimos atrasados
     * @throws ServiceException em caso de erro
     */
    public List<Emprestimo> listarAtrasados() throws ServiceException {
        try {
            LocalDate hoje = LocalDate.now();
            return emprestimoDAO.listar().stream()
                    .filter(e -> {
                        if (e.getData_devolucao() != null) return false;
                        try {
                            LocalDate prevista = LocalDate.parse(e.getData_prevista(), fmt);
                            return prevista.isBefore(hoje);
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao listar empréstimos: " + e.getMessage(), e);
        }
    }

    /**
     * Obtém o histórico de empréstimos de um leitor.
     *
     * @param idLeitor id do leitor
     * @return lista de empréstimos do leitor
     * @throws ServiceException em caso de erro
     */
    public List<Emprestimo> historicoDeLeitor(int idLeitor) throws ServiceException {
        try {
            return emprestimoDAO.listar().stream()
                    .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao buscar histórico de empréstimos: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula dias de atraso para um empréstimo (0 se não atrasado ou já devolvido dentro do prazo).
     *
     * @param emprestimo empréstimo
     * @return número de dias de atraso (>=0)
     */
    public long calcularDiasAtraso(Emprestimo emprestimo) {
        if (emprestimo == null) return 0;
        try {
            LocalDate prevista = LocalDate.parse(emprestimo.getData_prevista(), fmt);
            LocalDate devolucao = emprestimo.getData_devolucao() == null ? LocalDate.now() : LocalDate.parse(emprestimo.getData_devolucao(), fmt);
            long dias = java.time.temporal.ChronoUnit.DAYS.between(prevista, devolucao);
            return Math.max(0, dias);
        } catch (Exception e) {
            return 0;
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
