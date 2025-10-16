package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Emprestimo;
import model.Livro;
import model.Leitor;
import model.Funcionario;

public class EmprestimoDAO {
    private Connection conn;

    public EmprestimoDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserir empréstimo
    public void inserir(Emprestimo emprestimo) throws SQLException {
        String sql = "INSERT INTO EMPRESTIMO (fk_livro, fk_leitor, fk_funcionario, data_emprestimo, data_prevista_devolucao, data_devolucao) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, emprestimo.getLivro().getId());
            stmt.setInt(2, emprestimo.getLeitor().getId());
            stmt.setInt(3, emprestimo.getFuncionario().getID());
            stmt.setString(4, emprestimo.getData_emprestimo());
            stmt.setString(5, emprestimo.getData_prevista());
            stmt.setString(6, emprestimo.getData_devolucao());
            stmt.executeUpdate();
        }
    }

    // Listar todos os empréstimos
    public List<Emprestimo> listar() throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM EMPRESTIMO";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            LivroDAO livroDAO = new LivroDAO(conn);
            LeitorDAO leitorDAO = new LeitorDAO(conn);
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            while (rs.next()) {
                Emprestimo e = new Emprestimo(
                        rs.getInt("id_emprestimo"),
                        livroDAO.buscarPorId(rs.getInt("fk_livro")),
                        funcionarioDAO.buscarPorId(rs.getInt("fk_funcionario")),
                        rs.getString("data_emprestimo"),
                        rs.getString("data_prevista_devolucao"),
                        rs.getString("data_devolucao"),
                        leitorDAO.buscarPorId(rs.getInt("fk_leitor")));
                emprestimos.add(e);
            }
        }
        return emprestimos;
    }

    // Buscar empréstimo por ID
    public Emprestimo buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM EMPRESTIMO WHERE id_emprestimo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            LivroDAO livroDAO = new LivroDAO(conn);
            LeitorDAO leitorDAO = new LeitorDAO(conn);
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            if (rs.next()) {
                return new Emprestimo(
                        rs.getInt("id_emprestimo"),
                        livroDAO.buscarPorId(rs.getInt("fk_livro")),
                        funcionarioDAO.buscarPorId(rs.getInt("fk_funcionario")),
                        rs.getString("data_emprestimo"),
                        rs.getString("data_prevista_devolucao"),
                        rs.getString("data_devolucao"),
                        leitorDAO.buscarPorId(rs.getInt("fk_leitor")));
            }
        }
        return null;
    }

    // Excluir empréstimo
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM EMPRESTIMO WHERE id_emprestimo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Atualizar data de devolução
    public void atualizarDevolucao(int id, String dataDevolucao) throws SQLException {
        String sql = "UPDATE EMPRESTIMO SET data_devolucao = ? WHERE id_emprestimo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dataDevolucao);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
}
