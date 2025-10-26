package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Menu principal (FUNCIONÁRIO) sem barra de menus e sem a aba de Autores. */
public class MainMenu extends JFrame {

    private final AppContext ctx;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel center = new JPanel(cardLayout);
    private final JLabel statusLabel = new JLabel("Pronto.");
    private final JLabel clockLabel = new JLabel();

    final Map<String, JComponent> cards = new LinkedHashMap<>();

    public MainMenu(AppContext ctx) {
        super("DevLibrary - Menu Principal");
        this.ctx = ctx;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        // (Removido) setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        registerCards();
        pack();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setPreferredSize(new Dimension(240, 0));
        side.setBorder(new EmptyBorder(16, 12, 16, 12));
        side.setBackground(new Color(245, 247, 250));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel title = new JLabel("Atalhos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        side.add(title, gc);

        gc.gridy++; side.add(primaryButton("Início", () -> showCard("home")), gc);
        // (removido) Autores
        gc.gridy++; side.add(primaryButton("Livros", () -> showCard("livros")), gc);
        gc.gridy++; side.add(primaryButton("Leitores", () -> showCard("leitores")), gc);
        gc.gridy++; side.add(primaryButton("Empréstimos", () -> showCard("emprestimos")), gc);
        gc.gridy++; side.add(primaryButton("Devoluções", () -> showCard("devolucoes")), gc);
        gc.gridy++; side.add(primaryButton("Atrasados", () -> showCard("atrasados")), gc);
        gc.gridy++; side.add(primaryButton("Histórico Leitor", () -> showCard("historico")), gc);

        gc.gridy++; gc.weighty = 1; side.add(Box.createVerticalGlue(), gc);

        gc.gridy++; side.add(secondaryButton("Sair", this::confirmExit), gc);
        return side;
    }

    private JPanel buildCenter() {
        center.setBorder(new EmptyBorder(12, 12, 12, 12));
        return center;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        statusLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        clockLabel.setBorder(new EmptyBorder(6, 10, 6, 10));
        status.add(statusLabel, BorderLayout.WEST);
        status.add(clockLabel, BorderLayout.EAST);

        Timer t = new Timer(1000, e ->
                clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        t.setRepeats(true);
        t.start();

        return status;
    }

    // ===================== CARDS / NAVEGAÇÃO =====================

    private void registerCards() {
        addCard("home", new HomePanel());
        addCard("livros", new LivroView(ctx));
        addCard("leitores", new LeitoresView(ctx));

        addCard("emprestimos", new EmprestimoView(ctx));
        addCard("devolucoes", new DevolucaoView(ctx));
        addCard("atrasados", new AtrasadosView(ctx));
        addCard("historico", new HistoricoLeitorView(ctx));

        showCard("home");
    }

    private void addCard(String name, JComponent comp) {
        cards.put(name, comp);
        center.add(comp, name);
    }

    private void showCard(String name) {
        if (!cards.containsKey(name)) return;
        if ("emprestimos".equals(name)) {
            refreshEmprestimoLists();
        }
        cardLayout.show(center, name);
        setStatus("Tela: " + capitalize(name));
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    /** Recria o card de empréstimo para atualizar as listas (livros disponíveis, etc.). */
    public void refreshEmprestimoLists() {
        JComponent old = cards.get("emprestimos");
        if (old != null) {
            center.remove(old);
        }
        EmprestimoView novo = new EmprestimoView(ctx);
        cards.put("emprestimos", novo);
        center.add(novo, "emprestimos");
        center.revalidate();
        center.repaint();
    }

    private JButton primaryButton(String text, Runnable action) {
        JButton b = new JButton(text);
        stylePrimary(b);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JButton secondaryButton(String text, Runnable action) {
        JButton b = new JButton(text);
        styleSecondary(b);
        b.addActionListener(e -> action.run());
        return b;
    }

    private void stylePrimary(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                new EmptyBorder(10, 12, 10, 12)));
        b.setBackground(new Color(255, 255, 255));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(8, 10, 8, 10)));
        b.setBackground(new Color(250, 250, 250));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void confirmExit() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja sair?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            // Limpa sessão e volta para a tela de login
            try {
                if (ctx.session != null) {
                    ctx.session.matricula = null;
                    ctx.session.funcionario = null;
                    ctx.session.leitor = null;
                    ctx.session.role = null;
                }
            } catch (Throwable ignored) {}

            dispose();

            if (ctx.auth == null) ctx.auth = new DBAuthService(ctx);
            SwingUtilities.invokeLater(() -> new LoginFrame(ctx, ctx.auth).setVisible(true));
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
