package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import config.ConnectionDB;
import model.Autor;

public class AutorDAO {

    // inserir novo autor
    public void inserir(Autor autor) {

        // query pro sql
        String queryInserir = "INSERT INTO AUTOR (nome, nacionalidade) VALUES (?, ?)";

        // try-with-resources: garante que a conexão e o PreparedStatement
        // serão fechados automaticamente ao final do bloco
        try (
                Connection conn = ConnectionDB.getConnection(); // abre a conexão dentro do método
                PreparedStatement ps = conn.prepareStatement(queryInserir);) {

            // Preenche os ? da query com os valores do objeto Autor
            ps.setString(1, autor.getNome());
            ps.setString(2, autor.getNacionalidade());

            // Executa a query -> executeUpdate serve para INSERT, UPDATE e DELETE
            ps.executeUpdate();

            System.out.println("Inserção bem sucedida"); // debug (tirar dps)

        } catch (SQLException e) {
            System.out.println("Inseriu nao, deu erro aqui oh"); // debug (tirar dps)
            e.printStackTrace();
        }

    }

    // listar todos os autores do db
    public void listarTodos() {

        String queryListar = "SELECT * FROM AUTOR";

        try (
                Connection conn = ConnectionDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(queryListar);) {

            // executa o SELECT e retorna um ResultSet com os dados
            ResultSet rs = ps.executeQuery();

            // percorre o ResultSet e imprime cada autor
            while (rs.next()) { // enquanto ainda houver autor, continua loop dentro do while
                imprimeAutor(rs); // chama metodo de impressao
            }

            System.out.println("Fim do select"); // debug (tirar dps)

        } catch (SQLException e) {
            System.out.println("Inseriu nao, deu erro aqui oh"); // debug (tirar dps)
            e.printStackTrace();
        }

    }

    public void buscarPorId(int id) {

        String queryBuscaID = "SELECT * FROM AUTOR WHERE id = (?)";

        try (
                Connection conn = ConnectionDB.getConnection();
                PreparedStatement ps = conn.prepareStatement(queryBuscaID);) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                imprimeAutor(rs);
            }

            System.out.println("Fim do select");

        } catch (SQLException e) {
            System.out.println("Select deu bom nao, erro aqui oh" + e);
        }
    }

    public void remover(int id) {

        String queryRemover = "DELETE FROM AUTOR WHERE id = (?)"; // a remocao ocorre por id

        try (
                Connection con = ConnectionDB.getConnection();
                PreparedStatement ps = con.prepareStatement(queryRemover);) {
            ps.setInt(1, id);

            ps.executeUpdate();

            System.out.println("Removido autor de ID: " + id); // debug (tirar dps)

        }

        catch (SQLException e) {
            System.out.println("Delete deu bom nao viado presta atencao aqui oh" + e);
        }
    }

    // auxiliar
    private void imprimeAutor(ResultSet rs) throws SQLException {
        System.out.println(
                rs.getInt("id") + " - " + rs.getString("nome") + " - " + rs.getString("nacionalidade"));
    }
}
