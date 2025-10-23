package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Funcionario;

public class FuncionarioDAO {
    private final Connection conn;

    public FuncionarioDAO(Connection conn) {
        this.conn = conn;
    }

    // Inserir novo funcionário (sem senha - usa DEFAULT '0000')
    public void inserir(Funcionario funcionario) throws SQLException {
        String sql = "INSERT INTO FUNCIONARIO (nome, matricula, email, telefone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getMatricula());
            stmt.setString(3, funcionario.getEmail());
            stmt.setString(4, funcionario.getTelefone());
            stmt.executeUpdate();
        }
    }

    // Inserir com senha (recomendado para cadastro via UI)
    public void inserir(Funcionario funcionario, String senha) throws SQLException {
        String sql = "INSERT INTO FUNCIONARIO (nome, matricula, email, telefone, senha) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, funcionario.getNome());
            stmt.setString(2, funcionario.getMatricula());
            stmt.setString(3, funcionario.getEmail());
            stmt.setString(4, funcionario.getTelefone());
            stmt.setString(5, senha);
            stmt.executeUpdate();
        }
    }

    // Atualizar senha por matrícula
    public void atualizarSenhaPorMatricula(String matricula, String novaSenha) throws SQLException {
        String sql = "UPDATE FUNCIONARIO SET senha=? WHERE matricula=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novaSenha);
            ps.setString(2, matricula);
            ps.executeUpdate();
        }
    }

    // Verifica existência da matrícula
    public boolean existsMatricula(String matricula) throws SQLException {
        String sql = "SELECT 1 FROM FUNCIONARIO WHERE matricula=? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // Buscar por matrícula (não retorna senha aqui)
    public Funcionario buscarPorMatricula(String matricula) throws SQLException {
        String sql = "SELECT * FROM FUNCIONARIO WHERE matricula=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Funcionario(
                            rs.getInt("id_funcionario"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            rs.getString("matricula"));
                }
                return null;
            }
        }
    }

    // Listar todos os funcionários
    public List<Funcionario> listar() throws SQLException {
        List<Funcionario> funcionarios = new ArrayList<>();
        String sql = "SELECT * FROM FUNCIONARIO";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Funcionario func = new Funcionario(
                        rs.getInt("id_funcionario"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getString("matricula"));
                funcionarios.add(func);
            }
        }
        return funcionarios;
    }

    // Buscar funcionário por ID
    public Funcionario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM FUNCIONARIO WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Funcionario(
                        rs.getInt("id_funcionario"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("telefone"),
                        rs.getString("matricula"));
            }
        }
        return null;
    }

    // Excluir funcionário
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM FUNCIONARIO WHERE id_funcionario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
