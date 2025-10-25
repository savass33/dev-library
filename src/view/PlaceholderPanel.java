package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Placeholder genérico para cada seção, até integrar com formulários/tabelas
 * reais.
 */
public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title, String description) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 22f));

        JTextArea txt = new JTextArea(description);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFont(txt.getFont().deriveFont(14f));
        txt.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(txt,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }
}