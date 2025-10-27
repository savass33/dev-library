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

public class Main {

    public static void main(String[] args) {
        setLookAndFeel();

        System.out.println("Iniciando aplicação...");
        testarConexao();

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
                try {
                    if (finalUiConn != null && !finalUiConn.isClosed())
                        finalUiConn.close();
                } catch (Exception ignored) {
                }
            }));

            SwingUtilities.invokeLater(() -> {
                AuthService auth = new DBAuthService(ctx);
                ctx.auth = auth; // guarda no contexto para reabrir login ao sair
                new LoginFrame(ctx, auth).setVisible(true);
            });

        } catch (Exception e) {
            System.out.println("Erro ao abrir a UI:");
            e.printStackTrace();
            try {
                if (uiConn != null)
                    uiConn.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ---------------- MÉTODOS DE TESTE ----------------
    private static void testarConexao() {
        try (Connection conn = ConnectionDB.getConnection()) {
            if (conn != null)
                System.out.println("Conexão bem-sucedida!\n");
            else {
                System.out.println("Falha na conexão.\n");
                System.exit(0);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao testar conexão:");
            e.printStackTrace();
        }
    }

    private static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}
