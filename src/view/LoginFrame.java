package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AppContext ctx;
    private final AuthService auth;

    public LoginFrame(AppContext ctx, AuthService auth) {
        super("DevLibrary - Acesso");
        this.ctx = ctx; this.auth = auth;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(580, 460));
        setLocationRelativeTo(null);
        setContentPane(buildTabs());
        pack();
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Entrar", buildLogin());
        tabs.addTab("Cadastrar", buildCadastro());
        return tabs;
    }

    private JPanel buildLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(18,18,18,18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        JLabel t = new JLabel("Acesse com matrícula e senha");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 20f));
        gc.gridx=0; gc.gridy=y++; gc.gridwidth=2; p.add(t, gc);

        gc.gridwidth=1;
        gc.gridx=0; gc.gridy=y; p.add(new JLabel("Matrícula (6 dígitos):"), gc);
        JTextField tfMat = new JTextField(12);
        gc.gridx=1; p.add(tfMat, gc); y++;

        gc.gridx=0; gc.gridy=y; p.add(new JLabel("Senha (4 dígitos):"), gc);
        JPasswordField pf = new JPasswordField(12);
        gc.gridx=1; p.add(pf, gc); y++;

        JButton bt = new JButton("Entrar");
        gc.gridx=0; gc.gridy=y; gc.gridwidth=2; p.add(bt, gc);

        bt.addActionListener(e -> {
            String m = tfMat.getText().trim();
            String s = new String(pf.getPassword()).trim();
            if (!m.matches("\\d{6}")) { msg("Matrícula deve ter 6 dígitos.", JOptionPane.WARNING_MESSAGE); return; }
            if (!s.matches("\\d{4}")) { msg("Senha deve ter 4 dígitos.", JOptionPane.WARNING_MESSAGE); return; }
            try {
                AuthService.Role role = auth.login(m, s);
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    if (role == AuthService.Role.FUNCIONARIO) new MainMenu(ctx).setVisible(true);
                    else new AlunoMenu(ctx).setVisible(true);
                });
            } catch (Exception ex) { msg("Erro: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE); }
        });

        return p;
    }

    private JPanel buildCadastro() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(18,18,18,18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        int y=0;

        JLabel t = new JLabel("Criar conta");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 20f));
        gc.gridx=0; gc.gridy=y++; gc.gridwidth=2; p.add(t, gc);

        ButtonGroup bg = new ButtonGroup();
        JRadioButton rbAluno = new JRadioButton("Aluno", true);
        JRadioButton rbFunc  = new JRadioButton("Funcionário");
        bg.add(rbAluno); bg.add(rbFunc);

        gc.gridwidth=1;
        gc.gridx=0; gc.gridy=y; p.add(new JLabel("Tipo:"), gc);
        JPanel tipos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tipos.add(rbAluno); tipos.add(rbFunc);
        gc.gridx=1; p.add(tipos, gc); y++;

        gc.gridx=0; gc.gridy=y; p.add(new JLabel("Nome:"), gc);
        JTextField tfNome = new JTextField(28);
        gc.gridx=1; p.add(tfNome, gc); y++;

        gc.gridx=0; gc.gridy=y; p.add(new JLabel("E-mail:"), gc);
        JTextField tfEmail = new JTextField(28);
        gc.gridx=1; p.add(tfEmail, gc); y++;

        gc.gridx=0; gc.gridy=y; p.add(new JLabel("Telefone:"), gc);
        JTextField tfTel = new JTextField(20);
        gc.gridx=1; p.add(tfTel, gc); y++;

        JButton bt = new JButton("Cadastrar");
        gc.gridx=0; gc.gridy=y; gc.gridwidth=2; p.add(bt, gc);

        bt.addActionListener(e -> {
            String nome = tfNome.getText().trim();
            String email = tfEmail.getText().trim();
            String tel = tfTel.getText().trim();
            if (nome.isEmpty() || email.isEmpty()) { msg("Preencha nome e e-mail.", JOptionPane.WARNING_MESSAGE); return; }
            try {
                AuthService.RegisterResult r = rbAluno.isSelected()
                        ? auth.cadastrarAluno(nome, email, tel)
                        : auth.cadastrarFuncionario(nome, email, tel);

                JOptionPane.showMessageDialog(this,
                        "Cadastro realizado!\n\n" +
                        "Tipo: " + (r.role == AuthService.Role.ALUNO ? "Aluno" : "Funcionário") + "\n" +
                        "Matrícula: " + r.matricula + "\n" +
                        "Senha: " + r.senha + "\n\n" +
                        "Guarde seus dados para login.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { msg("Erro: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE); }
        });

        return p;
    }

    private void msg(String m, int type) {
        JOptionPane.showMessageDialog(this, m, type == JOptionPane.WARNING_MESSAGE ? "Aviso" :
                (type == JOptionPane.ERROR_MESSAGE ? "Erro" : "Info"), type);
    }
}
