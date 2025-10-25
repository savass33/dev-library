package model;

public class Emprestimo {
    private int id;
    private Livro livro;
    private Leitor leitor;
    private Funcionario funcionario;
    private String data_emprestimo;
    private String data_prevista;
    private String data_devolucao;

    public Emprestimo(int id, Livro livro, Funcionario funcionario, String data_emprestimo, String data_prevista,
            String data_devolucao, Leitor leitor) {
        this.id = id;
        this.livro = livro;
        this.leitor = leitor;
        this.funcionario = funcionario;
        this.data_emprestimo = data_emprestimo;
        this.data_prevista = data_prevista;
        this.data_devolucao = data_devolucao;

    }

    public int getid() {
        return id;
    }

    public void setID(int iD) {
        id = iD;
    }

    public Livro getLivro() {
        return livro;
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public void setLeitor(Leitor leitor) {
        this.leitor = leitor;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public String getData_emprestimo() {
        return data_emprestimo;
    }

    public void setData_emprestimo(String data_emprestimo) {
        this.data_emprestimo = data_emprestimo;
    }

    public String getData_prevista() {
        return data_prevista;
    }

    public void setData_prevista(String data_prevista) {
        this.data_prevista = data_prevista;
    }

    public String getData_devolucao() {
        return data_devolucao;
    }

    public void setData_devolucao(String data_devolucao) {
        this.data_devolucao = data_devolucao;
    }
}
