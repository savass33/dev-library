package view;
import config.ConnectionDB;

public class Main {
    public static void main(String[] args) {
        ConnectionDB con = new ConnectionDB();

        try {
            con.getConnection();
            System.out.println("Conexão realizada com sucesso");
        } catch (Exception e) {
            System.out.println("Conexão não foi estabelecida");
        }
    }
}
