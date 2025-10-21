package service;

import dao.EmprestimoDAO;
import dao.LivroDAO;
import dao.LeitorDAO;
import dao.MultaDAO;
import model.Emprestimo;
import model.Livro;
import model.Leitor;
import model.Multa;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serviço responsável por gerar relatórios e estatísticas sobre o funcionamento da biblioteca.
 * Os métodos retornam coleções e mapas prontos para conversão em tabelas/CSV/PDF pela camada de apresentação.
 */
public class RelatorioService {
    private final EmprestimoDAO emprestimoDAO;
    private final LivroDAO livroDAO;
    private final LeitorDAO leitorDAO;
    private final MultaDAO multaDAO;

    public RelatorioService(EmprestimoDAO emprestimoDAO, LivroDAO livroDAO, LeitorDAO leitorDAO, MultaDAO multaDAO) {
        this.emprestimoDAO = emprestimoDAO;
        this.livroDAO = livroDAO;
        this.leitorDAO = leitorDAO;
        this.multaDAO = multaDAO;
    }

    /**
     * Retorna o histórico completo de empréstimos (ordenação decrescente pela data do empréstimo).
     *
     * @return lista de empréstimos ordenada
     * @throws ServiceException em caso de erro
     */
    public List<Emprestimo> gerarHistoricoEmprestimos() throws ServiceException {
        try {
            List<Emprestimo> todas = emprestimoDAO.listar();
            todas.sort(Comparator.comparing(Emprestimo::getData_emprestimo).reversed());
            return todas;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao gerar histórico de empréstimos: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna os N livros mais retirados (por número de empréstimos).
     *
     * @param topN número de livros desejado
     * @return lista de pares (Livro, quantidade)
     * @throws ServiceException em caso de erro
     */
    public List<Map.Entry<Livro, Long>> livrosMaisRetirados(int topN) throws ServiceException {
        try {
            List<Emprestimo> todas = emprestimoDAO.listar();
            Map<Integer, Long> counts = todas.stream()
                    .filter(e -> e.getLivro() != null)
                    .collect(Collectors.groupingBy(e -> e.getLivro().getId(), Collectors.counting()));

            Map<Integer, Livro> livrosMap = new HashMap<>();
            for (Integer id : counts.keySet()) {
                Livro l = livroDAO.buscarPorId(id);
                if (l != null) livrosMap.put(id, l);
            }

            return counts.entrySet().stream()
                    .map(ent -> Map.entry(livrosMap.get(ent.getKey()), ent.getValue()))
                    .filter(entry -> entry.getKey() != null)
                    .sorted(Map.Entry.<Livro, Long>comparingByValue().reversed())
                    .limit(topN)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao calcular livros mais retirados: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna os leitores com maior número de atrasos (baseado em multas ou empréstimos atrasados).
     *
     * @param topN número de leitores
     * @return lista de pares (Leitor, qtdAtrasos)
     * @throws ServiceException em caso de erro
     */
    public List<Map.Entry<Leitor, Long>> leitoresComMaisAtrasos(int topN) throws ServiceException {
        try {
            List<Multa> multas = multaDAO.listar();
            Map<Integer, Long> contagem = multas.stream()
                    .filter(m -> m.getEmprestimo() != null && m.getEmprestimo().getLeitor() != null)
                    .collect(Collectors.groupingBy(m -> m.getEmprestimo().getLeitor().getId(), Collectors.counting()));

            Map<Integer, Leitor> leitoresMap = new HashMap<>();
            for (Integer id : contagem.keySet()) {
                Leitor l = leitorDAO.buscarPorId(id);
                if (l != null) leitoresMap.put(id, l);
            }

            return contagem.entrySet().stream()
                    .map(ent -> Map.entry(leitoresMap.get(ent.getKey()), ent.getValue()))
                    .filter(entry -> entry.getKey() != null)
                    .sorted(Map.Entry.<Leitor, Long>comparingByValue().reversed())
                    .limit(topN)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Erro ao gerar relatório de leitores com mais atrasos: " + e.getMessage(), e);
        }
    }

    /**
     * Estatísticas simples: total de livros, total de leitores, total de empréstimos e multas pendentes.
     *
     * @return mapa com chaves/valores de métricas
     * @throws ServiceException em caso de erro
     */
    public Map<String, Long> gerarEstatisticasBasicas() throws ServiceException {
        try {
            long totalLivros = livroDAO.listar().size();
            long totalLeitores = leitorDAO.listar().size();
            long totalEmprestimos = emprestimoDAO.listar().size();
            long multasPendentes = multaDAO.listar().stream().filter(m -> !m.isPago()).count();

            Map<String, Long> stats = new LinkedHashMap<>();
            stats.put("totalLivros", totalLivros);
            stats.put("totalLeitores", totalLeitores);
            stats.put("totalEmprestimos", totalEmprestimos);
            stats.put("multasPendentes", multasPendentes);
            return stats;
        } catch (SQLException e) {
            throw new ServiceException("Erro ao gerar estatísticas básicas: " + e.getMessage(), e);
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
