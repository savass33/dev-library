package view;

import dao.*;
import service.EmprestimoService;

import java.sql.Connection;

/** Guarda Connection, DAOs e Services para a camada de UI. */
public class AppContext {
    public final Connection conn;

    // DAOs
    public final EmprestimoDAO emprestimoDAO;
    public final LivroDAO livroDAO;
    public final LeitorDAO leitorDAO;
    public final FuncionarioDAO funcionarioDAO;

    // Services
    public final EmprestimoService emprestimoService;

    public AppContext(Connection conn) {
        this.conn = conn;

        // Instancia DAOs com a mesma conexão
        this.emprestimoDAO = new EmprestimoDAO(conn);
        this.livroDAO = new LivroDAO(conn);
        this.leitorDAO = new LeitorDAO(conn);
        this.funcionarioDAO = new FuncionarioDAO(conn);

        // Service com regras de negócio
        this.emprestimoService = new EmprestimoService(
                this.emprestimoDAO, this.livroDAO, this.leitorDAO, this.funcionarioDAO
        );
    }
}
