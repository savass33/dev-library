package view;

public interface AuthService {
    enum Role {
        ALUNO, FUNCIONARIO
    }

    class RegisterResult {
        public final String nome, matricula, senha;
        public final Role role;

        public RegisterResult(String nome, String matricula, String senha, Role role) {
            this.nome = nome;
            this.matricula = matricula;
            this.senha = senha;
            this.role = role;
        }
    }

    Role login(String matricula, String senha) throws Exception;

    RegisterResult cadastrarAluno(String nome, String email, String telefone) throws Exception;

    RegisterResult cadastrarFuncionario(String nome, String email, String telefone) throws Exception;
}
