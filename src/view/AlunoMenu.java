package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Menu principal para ALUNO, sem barra de menus no topo.
 * Ao sair, NÃO encerra a aplicação: volta para a tela de login.
 */
public class AlunoMenu extends JFrame {

    private final AppContext ctx;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel center = new JPanel(cardLayout);
    private final JLabel statusLabel = new JLabel("Seja bem-vindo(a)!");
    private final JLabel clockLabel = new JLabel();

    private final Map<String, JComponent> cards = new LinkedHashMap<>();

    public AlunoMenu(AppContext ctx) {
        super("DevLibrary - Área do Aluno");
        this.ctx = ctx;

        // Não matar a JVM ao fechar
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
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
        side.setPreferredSize(new Dimension(220, 0));
        side.setBorder(new EmptyBorder(16, 12, 16, 12));
        side.setBackground(new Color(245, 247, 250));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel title = new JLabel("Atalhos do Aluno");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        side.add(title, gc);

        gc.gridy++; side.add(primary("Início", () -> showCard("home")), gc);
        gc.gridy++; side.add(primary("Novo Empréstimo", () -> showCard("emprestimos")), gc);
        gc.gridy++; side.add(primary("Devolução", () -> showCard("devolucoes")), gc);
        gc.gridy++; side.add(primary("Atrasados", () -> showCard("atrasados")), gc);
        gc.gridy++; side.add(primary("Meus Empréstimos", () -> showCard("historico")), gc);

        gc.gridy++; gc.weighty = 1; side.add(Box.createVerticalGlue(), gc);

        gc.gridy++; side.add(secondary("Sair", this::confirmExit), gc);
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
        t.start();
        return status;
    }

    private void registerCards() {
        addCard("home", new HomeAlunoPanel(ctx));
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
        // ao abrir empréstimos, recarrega listas para refletir disponibilidades
        if ("emprestimos".equals(name) && cards.get("emprestimos") instanceof EmprestimoView ev) {
            ev.refresh();
        }
        cardLayout.show(center, name);
        statusLabel.setText("Tela: " + name);
    }

    private JButton primary(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220)),
                new EmptyBorder(10, 12, 10, 12)));
        b.addActionListener(e -> r.run());
        return b;
    }

    private JButton secondary(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(8, 10, 8, 10)));
        b.addActionListener(e -> r.run());
        return b;
    }

    private void confirmExit() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja sair?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            // limpa sessão e volta ao login (sem encerrar a aplicação)
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
}
