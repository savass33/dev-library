package dao;

import model.Livro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {
    private Connection conn;

    public LivroDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserir um novo livro
    public void inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO LIVRO (titulo, isbn, autor, ano_publicacao, genero, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getIsbn());
            stmt.setString(3, livro.getAutor());
            stmt.setString(4, livro.getAnoPublicacao());
            stmt.setString(5, livro.getGenero());
            stmt.setString(6, livro.getStatus());
            stmt.executeUpdate();
        }
    }

    // Listar todos os livros
    public List<Livro> listar() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM LIVRO";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id_livro"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setIsbn(rs.getString("isbn"));
                livro.setAutor(rs.getString("autor"));
                livro.setAnoPublicacao(rs.getString("ano_publicacao"));
                livro.setGenero(rs.getString("genero"));
                livro.setStatus(rs.getString("status"));
                livros.add(livro);
            }
        }
        return livros;
    }

    // Buscar por ID
    public Livro buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM LIVRO WHERE id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Livro livro = new Livro();
                livro.setId(rs.getInt("id_livro"));
                livro.setTitulo(rs.getString("titulo"));
                livro.setIsbn(rs.getString("isbn"));
                livro.setAutor(rs.getString("autor"));
                livro.setAnoPublicacao(rs.getString("ano_publicacao"));
                livro.setGenero(rs.getString("genero"));
                livro.setStatus(rs.getString("status"));
                return livro;
            }
        }
        return null;
    }

    // Atualizar status
    public void atualizarStatus(int id, String novoStatus) throws SQLException {
        String sql = "UPDATE LIVRO SET status = ? WHERE id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    // Excluir
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM LIVRO WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
