package view;

import model.Funcionario;
import model.Leitor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/** AuthService persistente salvando a senha diretamente em LEITOR e FUNCIONARIO. */
public class DBAuthService implements AuthService {

    private final AppContext ctx;
    private final Random rnd = new Random();

    public DBAuthService(AppContext ctx) { this.ctx = ctx; }

    @Override
    public Role login(String matricula, String senha) throws Exception {
        if (matricula == null || senha == null) throw new IllegalArgumentException("Informe matrícula e senha.");
        if (!matricula.matches("\\d{6}")) throw new IllegalArgumentException("Matrícula deve ter 6 dígitos.");
        if (!senha.matches("\\d{4}")) throw new IllegalArgumentException("Senha deve ter 4 dígitos.");

        try {
            // Checa diretamente nas tabelas
            if (checkSenha("FUNCIONARIO", matricula, senha)) return Role.FUNCIONARIO;
            if (checkSenha("LEITOR", matricula, senha))      return Role.ALUNO;

            throw new IllegalArgumentException("Credenciais inválidas.");
        } catch (SQLException e) {
            throw new Exception("Erro ao validar login: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisterResult cadastrarAluno(String nome, String email, String telefone) throws Exception {
        String matricula = gerarMatricula('0');
        String senha = gerarSenha4();
        try {
            Leitor novo = new Leitor(nome, email, telefone, matricula);
            ctx.leitorDAO.inserir(novo, senha);
            return new RegisterResult(nome, matricula, senha, Role.ALUNO);
        } catch (SQLException e) {
            throw new Exception("Erro ao cadastrar aluno: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisterResult cadastrarFuncionario(String nome, String email, String telefone) throws Exception {
        String matricula = gerarMatricula('1');
        String senha = gerarSenha4();
        try {
            // ORDEM CORRETA: (id, nome, email, telefone, matricula)
            Funcionario novo = new Funcionario(0, nome, email, telefone, matricula);
            ctx.funcionarioDAO.inserir(novo, senha);
            return new RegisterResult(nome, matricula, senha, Role.FUNCIONARIO);
        } catch (SQLException e) {
            throw new Exception("Erro ao cadastrar funcionário: " + e.getMessage(), e);
        }
    }

    // ---------- helpers ----------

    private boolean checkSenha(String table, String matricula, String senha) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE matricula=? AND senha=? LIMIT 1";
        try (PreparedStatement ps = ctx.conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setString(2, senha);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private String gerarMatricula(char prefixo) throws SQLException {
        while (true) {
            String mat = prefixo + String.format("%05d", rnd.nextInt(100000));
            if (!ctx.leitorDAO.existsMatricula(mat) && !ctx.funcionarioDAO.existsMatricula(mat)) return mat;
        }
    }

    private String gerarSenha4() { return String.format("%04d", rnd.nextInt(10000)); }
}
