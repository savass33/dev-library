import config.ConnectionDB;
import dao.*;
import model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando aplicação...");
        testarConexao();

        try (Connection conn = ConnectionDB.getConnection()) {

            // ======== TESTE FUNCIONÁRIO ========
            testarFuncionario(conn);

            // ======== TESTE LEITOR ========
            testarLeitor(conn);

            // ======== TESTE LIVRO ========
            testarLivro(conn);

            // ======== TESTE EMPRÉSTIMO ========
            testarEmprestimo(conn);

            // ======== TESTE MULTA ========
            testarMulta(conn);

        } catch (SQLException e) {
            System.out.println("Erro geral na execução dos testes:");
            e.printStackTrace();
        }
    }

    // ---------------- MÉTODOS DE TESTE ----------------

    private static void testarConexao() {
        try (Connection conn = ConnectionDB.getConnection()) {
            if (conn != null) {
                System.out.println("Conexão bem-sucedida!\n");
            } else {
                System.out.println("Falha na conexão.\n");
                System.exit(0);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao testar conexão:");
            e.printStackTrace();
        }
    }

    private static void testarFuncionario(Connection conn) throws SQLException {
        System.out.println("===== TESTE: FUNCIONÁRIO =====");
        FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

        Funcionario funcionario = new Funcionario(0, "João Silva", "1234", "joao@email.com", "99999-9999");
        funcionarioDAO.inserir(funcionario);

        List<Funcionario> funcionarios = funcionarioDAO.listar();
        funcionarios.forEach(System.out::println);
        System.out.println();
    }

    private static void testarLeitor(Connection conn) throws SQLException {
        System.out.println("===== TESTE: LEITOR =====");
        LeitorDAO leitorDAO = new LeitorDAO(conn);

        Leitor leitor = new Leitor("Levi", "levi@gays.com", "98765-4321", "2469");
        leitorDAO.inserir(leitor);

        List<Leitor> leitores = leitorDAO.listar();
        leitores.forEach(System.out::println);
        System.out.println();
    }

    private static void testarLivro(Connection conn) throws SQLException {
        System.out.println("===== TESTE: LIVRO =====");
        LivroDAO livroDAO = new LivroDAO(conn);

        Livro livro = new Livro("O Senhor dos Anéis", "12345", "J.R.R. Tolkien", "1954", "Fantasia");
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
                0,
                livro,
                funcionario,
                "2025-10-16",
                "2025-10-23",
                null,
                leitor);

        emprestimoDAO.inserir(emprestimo);

        List<Emprestimo> emprestimos = emprestimoDAO.listar();
        emprestimos.forEach(e -> System.out.println(
                "ID: " + e.getid() +
                        ", Livro: " + e.getLivro().getTitulo() +
                        ", Leitor: " + e.getLeitor().getNome() +
                        ", Funcionário: " + e.getFuncionario().getNome() +
                        ", Data empréstimo: " + e.getData_emprestimo() +
                        ", Data prevista: " + e.getData_prevista() +
                        ", Data devolução: " + e.getData_devolucao()));
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
                        ", Data pagamento: " + m.getData_pagamento()));
        System.out.println();
    }
}
