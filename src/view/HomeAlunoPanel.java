package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Tela inicial para alunos: mensagem acolhedora e explicações. */
public class HomeAlunoPanel extends JPanel {
    public HomeAlunoPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Bem-vindo(a) à DevLibrary!");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JTextArea msg = new JTextArea("""
                Esta é a sua área de aluno. Aqui você pode:

                • Novo Empréstimo — escolha um livro disponível e confirme o empréstimo.
                • Devolução — registre a devolução de um empréstimo em aberto.
                • Histórico por Leitor — consulte todos os seus empréstimos anteriores.
                • Atrasados — veja o que está com prazo vencido e evite multas.

                Dicas:
                - Se tiver dúvidas, fale com um funcionário.
                - Fique de olho nas datas para não gerar multa por atraso.
                """);
        msg.setEditable(false);
        msg.setOpaque(false);
        msg.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        add(title, BorderLayout.NORTH);
        add(msg, BorderLayout.CENTER);
    }
}
