package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Leitor;

public class LeitorDAO {
    private Connection conn;

    public LeitorDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserir um novo leitor
    public void inserir(Leitor leitor) throws SQLException {
        String sql = "INSERT INTO LEITOR (nome, matricula, email, telefone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, leitor.getNome());
            stmt.setString(2, leitor.getMatricula());
            stmt.setString(3, leitor.getEmail());
            stmt.setString(4, leitor.getTelefone());
            stmt.executeUpdate();
        }
    }

    // Listar todos os leitores
    public List<Leitor> listar() throws SQLException {
        List<Leitor> leitores = new ArrayList<>();
        String sql = "SELECT * FROM LEITOR";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Leitor leitor = new Leitor();
                leitor.setId(rs.getInt("id_leitor"));
                leitor.setNome(rs.getString("nome"));
                leitor.setMatricula(rs.getString("matricula"));
                leitor.setEmail(rs.getString("email"));
                leitor.setTelefone(rs.getString("telefone"));

                leitores.add(leitor);
            }
        }
        return leitores;
    }

    // Buscar por ID
    public Leitor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM LEITOR WHERE id_leitor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Leitor leitor = new Leitor();
                leitor.setId(rs.getInt("id_leitor"));
                leitor.setNome(rs.getString("nome"));
                leitor.setMatricula(rs.getString("matricula"));
                leitor.setEmail(rs.getString("email"));
                leitor.setTelefone(rs.getString("telefone"));
                return leitor;
            }
        }
        return null;
    }

    // Excluir
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM LEITOR WHERE id_leitor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
}
