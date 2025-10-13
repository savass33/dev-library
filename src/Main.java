import view.MenuPrincipal;
import config.ConnectionDB;
import dao.AutorDAO;
import model.Autor;

public class Main {
    public static void main(String[] args) {

        System.out.println("Iniciando aplicação...");
        MenuPrincipal.main(args);

        System.out.println("Testando conexão...");
        ConnectionDB.getConnection();

        Autor autor = new Autor();
        // autor.setNome("Levi");
        // autor.setNacionalidade("Transgenero");

        AutorDAO autorDAO = new AutorDAO();
        // autorDAO.inserir(autor);
        autorDAO.listarTodos();
        autorDAO.remover(2); // remocao ocorre por id
        autorDAO.listarTodos();
    }
}
