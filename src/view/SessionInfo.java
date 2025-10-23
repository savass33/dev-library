package view;

import model.Funcionario;
import model.Leitor;

public class SessionInfo {
    public AuthService.Role role;
    public String matricula;
    public Leitor leitor;           // preenchido se role == ALUNO
    public Funcionario funcionario; // preenchido se role == FUNCIONARIO

    public boolean isAluno() { return role == AuthService.Role.ALUNO; }
    public boolean isFuncionario() { return role == AuthService.Role.FUNCIONARIO; }
}
