package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Multa;

public class MultaDAO {
    private final Connection conn;

    public MultaDAO(Connection conn) { this.conn = conn; }

    public void inserir(Multa multa) throws SQLException {
        String sql = "INSERT INTO MULTA (fk_emprestimo, valor, pago, data_pagamento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, multa.getEmprestimo().getid());
            stmt.setDouble(2, multa.getValor());
            stmt.setBoolean(3, multa.isPago());
            stmt.setString(4, multa.getData_pagamento());
            stmt.executeUpdate();
        }
    }

    public List<Multa> listar() throws SQLException {
        List<Multa> multas = new ArrayList<>();
        String sql = "SELECT * FROM MULTA";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            EmprestimoDAO emprestimoDAO = new EmprestimoDAO(conn);
            while (rs.next()) {
                Multa m = new Multa(
                        rs.getInt("id_multa"),
                        emprestimoDAO.buscarPorId(rs.getInt("fk_emprestimo")),
                        rs.getDouble("valor"),
                        rs.getBoolean("pago"),
                        rs.getString("data_pagamento"));
                multas.add(m);
            }
        }
        return multas;
    }

    public Multa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM MULTA WHERE id_multa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            EmprestimoDAO emprestimoDAO = new EmprestimoDAO(conn);
            if (rs.next()) {
                return new Multa(
                        rs.getInt("id_multa"),
                        emprestimoDAO.buscarPorId(rs.getInt("fk_emprestimo")),
                        rs.getDouble("valor"),
                        rs.getBoolean("pago"),
                        rs.getString("data_pagamento"));
            }
        }
        return null;
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM MULTA WHERE id_multa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void atualizarPagamento(int id, boolean pago, String dataPagamento) throws SQLException {
        String sql = "UPDATE MULTA SET pago = ?, data_pagamento = ? WHERE id_multa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, pago);
            stmt.setString(2, dataPagamento);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }
}
