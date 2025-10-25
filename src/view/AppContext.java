package view;

import dao.*;
import service.EmprestimoService;

import javax.swing.SwingUtilities;
import java.awt.Window;
import java.sql.Connection;

/** Guarda Connection, DAOs, Services, Auth e a sessão do usuário logado. */
public class AppContext {
    public final Connection conn;

    // DAOs
    public final EmprestimoDAO emprestimoDAO;
    public final LivroDAO livroDAO;
    public final LeitorDAO leitorDAO;
    public final FuncionarioDAO funcionarioDAO;
    public final MultaDAO multaDAO;

    // Services
    public final EmprestimoService emprestimoService;

    // Auth (definido na Main ao abrir o LoginFrame)
    public AuthService auth;

    // Sessão atual
    public final SessionInfo session = new SessionInfo();

    public AppContext(Connection conn) {
        this.conn = conn;

        this.emprestimoDAO = new EmprestimoDAO(conn);
        this.livroDAO = new LivroDAO(conn);
        this.leitorDAO = new LeitorDAO(conn);
        this.funcionarioDAO = new FuncionarioDAO(conn);
        this.multaDAO = new MultaDAO(conn);

        this.emprestimoService = new EmprestimoService(
                this.emprestimoDAO, this.livroDAO, this.leitorDAO, this.funcionarioDAO, this.multaDAO);
    }

    /**
     * Encerra a sessão e volta para a tela de login (funciona para aluno e
     * funcionário).
     */
    public void logoutToLogin(Window from) {
        // limpa sessão sem depender de SessionInfo.clear()
        try {
            if (session != null) {
                session.matricula = null;
                session.leitor = null;
                session.funcionario = null;
                session.role = null;
            }
        } catch (Throwable ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            if (from != null)
                from.dispose();
            // se auth não tiver sido setado por algum motivo, cria um de fallback
            AuthService useAuth = (auth != null) ? auth : new DBAuthService(this);
            new LoginFrame(this, useAuth).setVisible(true);
        });
    }
}
