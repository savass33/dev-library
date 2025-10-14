import dao.LivroDAO;
import model.Livro;
import config.ConnectionDB;

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

            LivroDAO livroDAO = new LivroDAO(conn);

            // Inserir um livro
            Livro novo = new Livro("O viado do Levi", "25", "Pedro Caliope", "2025", "boiolagem");
            livroDAO.inserir(novo);
            System.out.println("Livro inserido com sucesso!");

            // Listar livros
            System.out.println("\n=== Lista de Livros ===");
            List<Livro> livros = livroDAO.listar();
            for (Livro l : livros) {
                System.out.println(l);
            }

            // Buscar por ID
            // System.out.println("\n=== Buscar Livro ID 1 ===");
            // Livro encontrado = livroDAO.buscarPorId(1);
            // if (encontrado != null) {
            //     System.out.println(encontrado);
            // } else {
            //     System.out.println("Livro não encontrado.");
            // }

            // Atualizar status
            livroDAO.atualizarStatus(1, "Emprestado");
            System.out.println("\nStatus atualizado!");

            // Excluir livro (opcional)
            // livroDAO.excluir(1);
            // System.out.println("\nLivro excluído!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
