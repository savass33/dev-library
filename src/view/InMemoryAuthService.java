package view;

import model.Funcionario;
import model.Leitor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InMemoryAuthService implements AuthService {

    private final AppContext ctx;
    private final Map<String, String> senhaPorMatricula = new HashMap<>();

    public InMemoryAuthService(AppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Role login(String matricula, String senha) throws Exception {
        String s = senhaPorMatricula.get(matricula);
        if (s == null || !s.equals(senha))
            throw new IllegalArgumentException("Credenciais inválidas.");
        return matricula.startsWith("1") ? Role.FUNCIONARIO : Role.ALUNO;
    }

    @Override
    public RegisterResult cadastrarAluno(String nome, String email, String telefone) throws Exception {
        String mat = gerarMatricula('0');
        String senha = gerarSenha4();
        try {
            Leitor l = new Leitor(nome, email, telefone, mat);
            ctx.leitorDAO.inserir(l);
            senhaPorMatricula.put(mat, senha);
            return new RegisterResult(nome, mat, senha, Role.ALUNO);
        } catch (SQLException e) {
            throw new Exception("Erro ao cadastrar aluno: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisterResult cadastrarFuncionario(String nome, String email, String telefone) throws Exception {
        String mat = gerarMatricula('1');
        String senha = gerarSenha4();
        try {
            Funcionario f = new Funcionario(0, nome, mat, email, telefone);
            ctx.funcionarioDAO.inserir(f);
            senhaPorMatricula.put(mat, senha);
            return new RegisterResult(nome, mat, senha, Role.FUNCIONARIO);
        } catch (SQLException e) {
            throw new Exception("Erro ao cadastrar funcionário: " + e.getMessage(), e);
        }
    }

    private String gerarMatricula(char prefixo) throws SQLException {
        Random r = new Random();
        while (true) {
            String mat = prefixo + String.format("%05d", r.nextInt(100000));
            boolean existe = ctx.leitorDAO.listar().stream().anyMatch(le -> mat.equals(le.getMatricula()))
                    || ctx.funcionarioDAO.listar().stream().anyMatch(fu -> mat.equals(fu.getMatricula()));
            if (!existe)
                return mat;
        }
    }

    private String gerarSenha4() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}
