import view.MenuPrincipal;
import config.ConnectionDB;

public class Main {
    public static void main(String[] args) {

        System.out.println("Iniciando aplicação...");
        MenuPrincipal.main(args);
        
        System.out.println("Testando conexão...");
        ConnectionDB.getConnection();

    }
}
