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
 * Janela principal com:
 * - Menu (Cadastros, Operações, Relatórios, Ajuda)
 * - Sidebar de atalhos
 * - Área central (CardLayout)
 * - Status bar (mensagem + relógio)
 *
 * Onde integrar:
 * Troque os PlaceholderPanel pelos JPanels reais (CRUD) e,
 * nos ActionListeners, chame seus DAOs/Services.
 */
public class MainMenu extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel center = new JPanel(cardLayout);
    private final JLabel statusLabel = new JLabel("Pronto.");
    private final JLabel clockLabel = new JLabel();

    private final Map<String, JComponent> cards = new LinkedHashMap<>();

    public MainMenu() {
        super("DevLibrary - Menu Principal");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
        });

        setJMenuBar(buildMenuBar());
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        registerCards();
        pack();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Cadastros
        JMenu cadastros = new JMenu("Cadastros");
        cadastros.setMnemonic(KeyEvent.VK_C);
        cadastros.add(menuItem("Autores", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK),
                () -> showCard("autores")));
        cadastros.add(menuItem("Livros", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK),
                () -> showCard("livros")));
        cadastros.add(menuItem("Usuários", KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK),
                () -> showCard("usuarios")));

        // Operações
        JMenu operacoes = new JMenu("Operações");
        operacoes.setMnemonic(KeyEvent.VK_O);
        operacoes.add(menuItem("Novo Empréstimo", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK),
                () -> showCard("emprestimos")));
        operacoes.add(menuItem("Devolução", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
                () -> showCard("devolucoes")));

        // Relatórios
        JMenu relatorios = new JMenu("Relatórios");
        relatorios.setMnemonic(KeyEvent.VK_R);
        relatorios.add(menuItem("Empréstimos em Aberto", null, () -> infoWIP("Relatório de empréstimos em aberto")));
        relatorios.add(menuItem("Multas por Usuário", null, () -> infoWIP("Relatório de multas por usuário")));

        // Ajuda
        JMenu ajuda = new JMenu("Ajuda");
        ajuda.setMnemonic(KeyEvent.VK_J);
        ajuda.add(menuItem("Sobre", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), this::showAbout));

        menuBar.add(cadastros);
        menuBar.add(operacoes);
        menuBar.add(relatorios);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(ajuda);
        return menuBar;
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
        gc.gridy++; side.add(primaryButton("Autores", () -> showCard("autores")), gc);
        gc.gridy++; side.add(primaryButton("Livros", () -> showCard("livros")), gc);
        gc.gridy++; side.add(primaryButton("Usuários", () -> showCard("usuarios")), gc);
        gc.gridy++; side.add(primaryButton("Empréstimos", () -> showCard("emprestimos")), gc);
        gc.gridy++; side.add(primaryButton("Devoluções", () -> showCard("devolucoes")), gc);

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

        // Relógio
        Timer t = new Timer(1000, e ->
                clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        t.setRepeats(true);
        t.start();

        return status;
    }

    private void registerCards() {
        addCard("home", new HomePanel());
        addCard("autores", new PlaceholderPanel(
                "Autores",
                "Gerencie autores da biblioteca.\n\nAções: Cadastrar, Listar, Atualizar, Remover."
        ));
        addCard("livros", new PlaceholderPanel(
                "Livros",
                "Gerencie livros do acervo.\n\nAções: Cadastrar, Buscar por título/ISBN, Atualizar status."
        ));
        addCard("usuarios", new PlaceholderPanel(
                "Usuários",
                "Gerencie leitores.\n\nAções: Cadastrar, atualizar contato, consultar histórico."
        ));
        addCard("emprestimos", new PlaceholderPanel(
                "Novo Empréstimo",
                "Fluxo: selecionar usuário -> selecionar livro disponível -> definir datas -> confirmar.\n\n" +
                "Use transação para inserir o empréstimo e atualizar o status do livro."
        ));
        addCard("devolucoes", new PlaceholderPanel(
                "Devolução",
                "Fluxo: localizar empréstimo ativo -> registrar devolução -> calcular multa -> atualizar status."
        ));

        showCard("home");
    }

    private void addCard(String name, JComponent comp) {
        cards.put(name, comp);
        center.add(comp, name);
    }

    private void showCard(String name) {
        if (!cards.containsKey(name)) return;
        cardLayout.show(center, name);
        setStatus("Tela: " + capitalize(name));
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private void infoWIP(String title) {
        JOptionPane.showMessageDialog(this,
                title + " — em construção.\nIntegre aqui seu relatório/consulta ao banco.",
                "Em breve", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "DevLibrary — Menu Principal\n" +
                "UI em Swing (JFrame) com CardLayout, Sidebar, Menubar e Status bar.\n\n" +
                "Integre seus DAOs/Services nos listeners de cada seção.",
                "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmExit() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja sair?", "Sair",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) { dispose(); }
    }

    // Helpers ---------------------------------------------------------------

    private JMenuItem menuItem(String text, KeyStroke ks, Runnable action) {
        JMenuItem it = new JMenuItem(text);
        if (ks != null) it.setAccelerator(ks);
        it.addActionListener(e -> action.run());
        return it;
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
                new EmptyBorder(10, 12, 10, 12)
        ));
        b.setBackground(new Color(255, 255, 255));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(AbstractButton b) {
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12f));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        b.setBackground(new Color(250, 250, 250));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
