package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Tela inicial acolhedora para o aluno (sem System.exit). */
public class HomeAlunoPanel extends JPanel {

    public HomeAlunoPanel(AppContext ctx) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(16, 16, 16, 16));

        var title = new JLabel("Bem-vindo(a) à sua biblioteca!");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        var msg = "<html><body style='width:560px;'>"
                + "<b>O que você pode fazer por aqui?</b><br><br>"
                + "• <b>Novo Empréstimo</b>: escolha um livro disponível e confirme o período.<br>"
                + "• <b>Devolução</b>: finalize um empréstimo ativo informando a data de devolução.<br>"
                + "• <b>Atrasados</b>: veja o que passou do prazo (apenas seus empréstimos).<br>"
                + "• <b>Meus Empréstimos</b>: consulte seu histórico completo.<br><br>"
                + "Precisa encerrar a sessão? Use <b>Conta → Sair</b> ou o botão <b>Sair</b> na lateral — "
                + "você voltará para a tela de login, sem fechar o aplicativo."
                + "</body></html>";

        var text = new JLabel(msg);
        text.setFont(text.getFont().deriveFont(14f));

        var north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);
        north.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
    }
}
