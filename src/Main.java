import config.ConnectionDB;
import dao.*;
import model.*;

import view.AppContext;
import view.LoginFrame;
import view.AuthService;
import view.DBAuthService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        setLookAndFeel();

        System.out.println("Iniciando aplicação...");
        testarConexao();

        // ===== Seus testes existentes =====
        try (Connection conn = ConnectionDB.getConnection()) {
            testarFuncionario(conn);
            testarLeitor(conn);
            testarLivro(conn);
            testarEmprestimo(conn);
            testarMulta(conn);
        } catch (SQLException e) {
            System.out.println("Erro geral na execução dos testes:");
            e.printStackTrace();
        }

        // ===== Conexão dedicada para a UI =====
        Connection uiConn = null;
        try {
            uiConn = ConnectionDB.getConnection();
            if (uiConn == null) {
                System.out.println("Falha na conexão da UI. Encerrando.");
                return;
            }

            AppContext ctx = new AppContext(uiConn);

            final Connection finalUiConn = uiConn;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { if (finalUiConn != null && !finalUiConn.isClosed()) finalUiConn.close(); } catch (Exception ignored) {}
            }));

            SwingUtilities.invokeLater(() -> {
                AuthService auth = new DBAuthService(ctx);
                ctx.auth = auth; // guarda no contexto para reabrir login ao sair
                new LoginFrame(ctx, auth).setVisible(true);
            });

        } catch (Exception e) {
            System.out.println("Erro ao abrir a UI:");
            e.printStackTrace();
            try { if (uiConn != null) uiConn.close(); } catch (Exception ignored) {}
        }
    }

    // ---------------- MÉTODOS DE TESTE ----------------
    private static void testarConexao() {
        try (Connection conn = ConnectionDB.getConnection()) {
            if (conn != null) System.out.println("Conexão bem-sucedida!\n");
            else { System.out.println("Falha na conexão.\n"); System.exit(0); }
        } catch (SQLException e) {
            System.out.println("Erro ao testar conexão:");
            e.printStackTrace();
        }
    }

    private static void testarFuncionario(Connection conn) throws SQLException {
        System.out.println("===== TESTE: FUNCIONÁRIO =====");
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
        Funcionario funcionario = new Funcionario(0, "João Silva", "joao@email.com", "999999999", "100001");
        funcionarioDAO.inserir(funcionario);
        List<Funcionario> funcionarios = funcionarioDAO.listar();
        funcionarios.forEach(System.out::println);
        System.out.println();
    }

    private static void testarLeitor(Connection conn) throws SQLException {
        System.out.println("===== TESTE: LEITOR =====");
        LeitorDAO leitorDAO = new LeitorDAO(conn);
        Leitor leitor = new Leitor("Levi", "levi@email.com", "987654321", "000001");
        leitorDAO.inserir(leitor);
        List<Leitor> leitores = leitorDAO.listar();
        leitores.forEach(System.out::println);
        System.out.println();
    }

    private static void testarLivro(Connection conn) throws SQLException {
        System.out.println("===== TESTE: LIVRO =====");
        LivroDAO livroDAO = new LivroDAO(conn);
        Livro livro = new Livro("O Senhor dos Anéis", "1234567890123", "J.R.R. Tolkien", "1954", "Fantasia");
        livroDAO.inserir(livro);
        List<Livro> livros = livroDAO.listar();
        livros.forEach(System.out::println);
        System.out.println();
    }

    private static void testarEmprestimo(Connection conn) throws SQLException {
        System.out.println("===== TESTE: EMPRÉSTIMO =====");
        EmprestimoDAO emprestimoDAO = new EmprestimoDAO(conn);
        LivroDAO livroDAO = new LivroDAO(conn);
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
        LeitorDAO leitorDAO = new LeitorDAO(conn);

        Livro livro = livroDAO.listar().get(0);
        Funcionario funcionario = funcionarioDAO.listar().get(0);
        Leitor leitor = leitorDAO.listar().get(0);

        Emprestimo emprestimo = new Emprestimo(
                0, livro, funcionario, "2025-10-16", "2025-10-23", null, leitor
        );
        emprestimoDAO.inserir(emprestimo);

        List<Emprestimo> emprestimos = emprestimoDAO.listar();
        emprestimos.forEach(e -> System.out.println(
                "ID: " + e.getid() +
                ", Livro: " + e.getLivro().getTitulo() +
                ", Leitor: " + e.getLeitor().getNome() +
                ", Funcionário: " + e.getFuncionario().getNome() +
                ", Data empréstimo: " + e.getData_emprestimo() +
                ", Data prevista: " + e.getData_prevista() +
                ", Data devolução: " + e.getData_devolucao()
        ));
        System.out.println();
    }

    private static void testarMulta(Connection conn) throws SQLException {
        System.out.println("===== TESTE: MULTA =====");
        MultaDAO multaDAO = new MultaDAO(conn);
        EmprestimoDAO emprestimoDAO = new EmprestimoDAO(conn);
        Emprestimo emprestimo = emprestimoDAO.listar().get(0);
        Multa multa = new Multa(0, emprestimo, 10.0, false, null);
        multaDAO.inserir(multa);
        List<Multa> multas = multaDAO.listar();
        multas.forEach(m -> System.out.println(
                "ID: " + m.getId() +
                ", Empréstimo ID: " + m.getEmprestimo().getid() +
                ", Valor: " + m.getValor() +
                ", Pago: " + m.isPago() +
                ", Data pagamento: " + m.getData_pagamento()
        ));
        System.out.println();
    }

    private static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
}
