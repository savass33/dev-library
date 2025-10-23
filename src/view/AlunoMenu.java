package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlunoMenu extends JFrame {
    private final AppContext ctx;
    private final CardLayout card = new CardLayout();
    private final JPanel center = new JPanel(card);
    private final JLabel status = new JLabel("Pronto.");
    private final JLabel clock = new JLabel();

    public AlunoMenu(AppContext ctx) {
        super("DevLibrary - Área do Aluno");
        this.ctx = ctx;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { confirmExit(); }});

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);

        registerCards();
        pack();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu op = new JMenu("Operações");
        op.add(item("Novo Empréstimo", () -> show("emprestimos")));
        op.add(item("Devolução", () -> show("devolucoes")));
        mb.add(op);
        JMenu aj = new JMenu("Ajuda");
        aj.add(item("Sobre", this::showAbout));
        mb.add(Box.createHorizontalGlue());
        mb.add(aj);
        return mb;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(14, 12, 14, 12));
        side.setBackground(new Color(245,247,250));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx=0; gc.gridy=0; gc.insets=new Insets(6,0,6,0);
        gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1;

        JLabel t = new JLabel("Atalhos (Aluno)");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 15f));
        side.add(t, gc);

        gc.gridy++; side.add(btn("Início", () -> show("home")), gc);
        gc.gridy++; side.add(btn("Empréstimos", () -> show("emprestimos")), gc);
        gc.gridy++; side.add(btn("Devoluções", () -> show("devolucoes")), gc);

        gc.gridy++; gc.weighty=1; side.add(Box.createVerticalGlue(), gc);
        gc.gridy++; side.add(btnSec("Sair", this::confirmExit), gc);
        return side;
    }

    private JPanel buildCenter() { center.setBorder(new EmptyBorder(12,12,12,12)); return center; }

    private JPanel buildStatus() {
        JPanel s = new JPanel(new BorderLayout());
        s.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(220,220,220)));
        status.setBorder(new EmptyBorder(6,10,6,10));
        clock.setBorder(new EmptyBorder(6,10,6,10));
        s.add(status, BorderLayout.WEST);
        s.add(clock, BorderLayout.EAST);
        Timer t = new Timer(1000, e -> clock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        t.start();
        return s;
    }

    private void registerCards() {
        addCard("home", new HomePanel());
        addCard("emprestimos", new EmprestimoView(ctx));
        addCard("devolucoes", new DevolucaoView(ctx));
        show("home");
    }

    private void addCard(String name, JComponent comp) { center.add(comp, name); }
    private void show(String name) { card.show(center, name); status.setText("Tela: " + name); }

    private JMenuItem item(String text, Runnable r) { JMenuItem it = new JMenuItem(text); it.addActionListener(e -> r.run()); return it; }
    private JButton btn(String text, Runnable r) { JButton b = new JButton(text); styleP(b); b.addActionListener(e -> r.run()); return b; }
    private JButton btnSec(String text, Runnable r) { JButton b = new JButton(text); styleS(b); b.addActionListener(e -> r.run()); return b; }
    private void styleP(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,210,220)), new EmptyBorder(10,12,10,12)));
        b.setBackground(Color.WHITE);
    }
    private void styleS(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(12f));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)), new EmptyBorder(8,10,8,10)));
        b.setBackground(new Color(250,250,250));
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "DevLibrary — Área do Aluno\nAcesso restrito a Empréstimos e Devoluções.",
                "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmExit() {
        int opt = JOptionPane.showConfirmDialog(this, "Deseja sair?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) dispose();
    }
}
