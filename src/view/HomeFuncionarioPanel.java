package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Tela inicial para funcionários: mensagem acolhedora e explicações. */
public class HomeFuncionarioPanel extends JPanel {
    public HomeFuncionarioPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Bem-vindo(a) à área do Funcionário");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JTextArea msg = new JTextArea("""
                Aqui você gerencia o acervo e os leitores da DevLibrary:

                • Livros — cadastrar, atualizar e remover títulos.
                • Leitores — gerenciar dados de alunos.
                • Novo Empréstimo — registrar retirada de um livro.
                • Devolução — registrar devoluções e liberar o livro.
                • Atrasados — acompanhar prazos vencidos.
                • Histórico por Leitor — consultar movimentação de um aluno.

                Observações:
                - O campo ISBN deve conter apenas números.
                - As devoluções em atraso geram multa automaticamente.
                """);
        msg.setEditable(false);
        msg.setOpaque(false);
        msg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        add(title, BorderLayout.NORTH);
        add(msg, BorderLayout.CENTER);
    }
}
