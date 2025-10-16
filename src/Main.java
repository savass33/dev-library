import config.ConnectionDB;
import dao.*;
import model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("Iniciando aplicação...");
        System.out.println("Testando conexão...");

        try (Connection conn = ConnectionDB.getConnection()) {

            if (conn != null) {
                System.out.println("Conexão bem-sucedida!");
            } else {
                System.out.println("Falha na conexão!");
                return;
            }

            // ======== TESTE FUNCIONARIO ========
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);
            Funcionario funcionario = new Funcionario(0, "João Silva", "1234", "joao@email.com", "99999-9999");
            funcionarioDAO.inserir(funcionario);
            System.out.println("\n=== Funcionários cadastrados ===");
            List<Funcionario> funcionarios = funcionarioDAO.listar();
            funcionarios.forEach(System.out::println);

            // ======== TESTE LEITOR ========
            LeitorDAO leitorDAO = new LeitorDAO(conn);
            Leitor leitor = new Leitor("Levi", "levi@gays.com", "98765-4321", "2469");
            leitorDAO.inserir(leitor);
            System.out.println("\n=== Leitores cadastrados ===");
            List<Leitor> leitores = leitorDAO.listar();
            leitores.forEach(System.out::println);

            // ======== TESTE LIVRO ========
            LivroDAO livroDAO = new LivroDAO(conn);
            Livro livro = new Livro("O Senhor dos Anéis", "12345", "J.R.R. Tolkien", "1954", "Fantasia");
            livroDAO.inserir(livro);
            System.out.println("\n=== Livros cadastrados ===");
            List<Livro> livros = livroDAO.listar();
            livros.forEach(System.out::println);

            // ======== TESTE EMPRÉSTIMO ========
            EmprestimoDAO emprestimoDAO = new EmprestimoDAO(conn);
            Emprestimo emprestimo = new Emprestimo(
                    0,
                    livros.get(0),
                    funcionarios.get(0),
                    "2025-10-16",
                    "2025-10-23",
                    null,
                    leitores.get(0));
            emprestimoDAO.inserir(emprestimo);
            System.out.println("\n=== Empréstimos cadastrados ===");
            List<Emprestimo> emprestimos = emprestimoDAO.listar();
            emprestimos.forEach(e -> System.out.println(
                    "ID: " + e.getid() +
                            ", Livro: " + e.getLivro().getTitulo() +
                            ", Leitor: " + e.getLeitor().getNome() +
                            ", Funcionário: " + e.getFuncionario().getNome() +
                            ", Data empréstimo: " + e.getData_emprestimo() +
                            ", Data prevista: " + e.getData_prevista() +
                            ", Data devolução: " + e.getData_devolucao()));

            // ======== TESTE MULTA ========
            MultaDAO multaDAO = new MultaDAO(conn);
            Multa multa = new Multa(
                    0,
                    emprestimos.get(0),
                    10.0,
                    false,
                    null);
            multaDAO.inserir(multa);
            System.out.println("\n=== Multas cadastradas ===");
            List<Multa> multas = multaDAO.listar();
            multas.forEach(m -> System.out.println(
                    "ID: " + m.getId() +
                            ", Empréstimo ID: " + m.getEmprestimo().getid() +
                            ", Valor: " + m.getValor() +
                            ", Pago: " + m.isPago() +
                            ", Data pagamento: " + m.getData_pagamento()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
