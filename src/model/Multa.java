package model;

public class Multa {
    int id;
    Emprestimo emprestimo;
    double valor;
    boolean pago;
    String data_pagamento;

    public Multa(int id, Emprestimo emprestimo, double valor, boolean pago, String data_pagamento) {
        this.id = id;
        this.emprestimo = emprestimo;
        this.valor = valor;
        this.pago = pago;
        this.data_pagamento = data_pagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public void setEmprestimo(Emprestimo emprestimo) {
        this.emprestimo = emprestimo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public String getData_pagamento() {
        return data_pagamento;
    }

    public void setData_pagamento(String data_pagamento) {
        this.data_pagamento = data_pagamento;
    }
}
