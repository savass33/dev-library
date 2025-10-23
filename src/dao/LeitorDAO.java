package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Leitor;

public class LeitorDAO {
    private final Connection conn;

    public LeitorDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserir um novo leitor (sem senha - usa DEFAULT '0000' ou será atualizada depois)
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

    // Inserir leitor já definindo a senha (recomendado para cadastro via UI)
    public void inserir(Leitor leitor, String senha) throws SQLException {
        String sql = "INSERT INTO LEITOR (nome, matricula, email, telefone, senha) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, leitor.getNome());
            stmt.setString(2, leitor.getMatricula());
            stmt.setString(3, leitor.getEmail());
            stmt.setString(4, leitor.getTelefone());
            stmt.setString(5, senha);
            stmt.executeUpdate();
        }
    }

    // Atualizar senha por matrícula
    public void atualizarSenhaPorMatricula(String matricula, String novaSenha) throws SQLException {
        String sql = "UPDATE LEITOR SET senha=? WHERE matricula=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaSenha);
            ps.setString(2, matricula);
            ps.executeUpdate();
        }
    }

    // Verifica existência da matrícula
    public boolean existsMatricula(String matricula) throws SQLException {
        String sql = "SELECT 1 FROM LEITOR WHERE matricula=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // Buscar por matrícula (não expõe senha no objeto, mantém seu modelo atual)
    public Leitor buscarPorMatricula(String matricula) throws SQLException {
        String sql = "SELECT * FROM LEITOR WHERE matricula=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Leitor leitor = new Leitor();
                    leitor.setId(rs.getInt("id_leitor"));
                    leitor.setNome(rs.getString("nome"));
                    leitor.setMatricula(rs.getString("matricula"));
                    leitor.setEmail(rs.getString("email"));
                    leitor.setTelefone(rs.getString("telefone"));
                    return leitor;
                }
                return null;
            }
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

    // Atualizar dados básicos do leitor (não altera matrícula)
    public void atualizar(Leitor leitor) throws SQLException {
        String sql = "UPDATE LEITOR SET nome=?, email=?, telefone=? WHERE id_leitor=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, leitor.getNome());
            ps.setString(2, leitor.getEmail());
            ps.setString(3, leitor.getTelefone());
            ps.setInt(4, leitor.getId());
            ps.executeUpdate();
        }
    }


}
