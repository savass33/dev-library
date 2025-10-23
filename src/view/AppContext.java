package view;

import dao.*;
import service.EmprestimoService;

import java.sql.Connection;

/** Guarda Connection, DAOs, Services e a sessão do usuário logado. */
public class AppContext {
    public final Connection conn;

    // DAOs
    public final EmprestimoDAO emprestimoDAO;
    public final LivroDAO livroDAO;
    public final LeitorDAO leitorDAO;
    public final FuncionarioDAO funcionarioDAO;

    // Services
    public final EmprestimoService emprestimoService;

    // Sessão atual
    public final SessionInfo session = new SessionInfo();

    public AppContext(Connection conn) {
        this.conn = conn;

        this.emprestimoDAO = new EmprestimoDAO(conn);
        this.livroDAO = new LivroDAO(conn);
        this.leitorDAO = new LeitorDAO(conn);
        this.funcionarioDAO = new FuncionarioDAO(conn);

        this.emprestimoService = new EmprestimoService(
                this.emprestimoDAO, this.livroDAO, this.leitorDAO, this.funcionarioDAO
        );
    }
}
