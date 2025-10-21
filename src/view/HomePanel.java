package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Tela inicial com dicas.
 */
public class HomePanel extends JPanel {
    public HomePanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Bem-vindo ao DevLibrary");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JTextArea tips = new JTextArea(
            "Dicas rápidas:\n" +
            " • Use o menu lateral para navegar entre as seções.\n" +
            " • Cadastre autores, livros e usuários antes de criar empréstimos.\n" +
            " • Ao devolver, calcule multa se houver atraso e atualize o status do livro.\n\n" +
            "Integração:\n" +
            " • Conecte seus DAOs/Services às ações de cada tela.\n" +
            " • Para listas e formulários, utilize JTable/JDialog conforme necessário.\n"
        );
        tips.setEditable(false);
        tips.setFont(tips.getFont().deriveFont(14f));
        tips.setLineWrap(true);
        tips.setWrapStyleWord(true);

        JPanel north = new JPanel(new BorderLayout());
        north.add(title, BorderLayout.NORTH);
        north.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(tips,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }
}
