package view;

import model.Leitor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/** Gerenciamento de Leitores (exclusivo para funcionários). */
public class LeitoresView extends JPanel {

    private final AppContext ctx;
    private final Random rnd = new Random();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Nome", "Matrícula", "E-mail", "Telefone" }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField tfId   = ro("0");
    private final JTextField tfNome = new JTextField(26);
    private final JTextField tfMat  = ro("");
    private final JTextField tfMail = new JTextField(26);
    private final JTextField tfTel  = new JTextField(14);

    public LeitoresView(AppContext ctx) {
        this.ctx = ctx;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Gerenciar Leitores");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        add(buildForm(), BorderLayout.WEST);
        add(new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        // máscaras/validações de entrada
        SwingMasks.lettersAndSpacesOnly(tfNome);
        SwingMasks.digitsOnlyMax(tfTel, 9);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::onSelectRow);

        // Se um aluno abrir isso por engano: somente leitura
        if (ctx.session != null && ctx.session.isAluno()) {
            setEnabledRecursively(this, false);
            table.setEnabled(true);
        }

        loadLeitores();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(0, 0, 8, 12));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int y = 0;
        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("ID:"), gc);
        gc.gridx = 1; form.add(tfId, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Nome:"), gc);
        gc.gridx = 1; form.add(tfNome, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Matrícula:"), gc);
        gc.gridx = 1; form.add(tfMat, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("E-mail:"), gc);
        gc.gridx = 1; form.add(tfMail, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Telefone:"), gc);
        gc.gridx = 1; form.add(tfTel, gc); y++;

        return form;
    }

    private JPanel buildButtons() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btNovo  = new JButton("Adicionar");
        JButton btSalvar= new JButton("Atualizar");
        JButton btReset = new JButton("Resetar senha");
        JButton btDel   = new JButton("Remover selecionado");
        JButton btRef   = new JButton("Atualizar lista");
        JButton btClear = new JButton("Limpar");

        south.add(btClear);
        south.add(btRef);
        south.add(btReset);
        south.add(btDel);
        south.add(btSalvar);
        south.add(btNovo);

        btNovo.addActionListener(e -> addLeitor());
        btSalvar.addActionListener(e -> updateLeitor());
        btDel.addActionListener(e -> removeLeitor());
        btReset.addActionListener(e -> resetSenhaLeitor());
        btRef.addActionListener(e -> loadLeitores());
        btClear.addActionListener(e -> clearForm());

        return south;
    }

    private void onSelectRow(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0) return;
        tfId.setText(String.valueOf(model.getValueAt(row, 0)));
        tfNome.setText(String.valueOf(model.getValueAt(row, 1)));
        tfMat.setText(String.valueOf(model.getValueAt(row, 2)));
        tfMail.setText(String.valueOf(model.getValueAt(row, 3)));
        tfTel.setText(String.valueOf(model.getValueAt(row, 4)));
    }

    private void loadLeitores() {
        try {
            List<Leitor> list = ctx.leitorDAO.listar();
            model.setRowCount(0);
            for (Leitor l : list) {
                model.addRow(new Object[] { l.getId(), l.getNome(), l.getMatricula(), l.getEmail(), l.getTelefone() });
            }
        } catch (SQLException ex) {
            err("Erro ao listar leitores: " + ex.getMessage());
        }
    }

    /** Cadastro agora passa pelo AuthService para garantir todas as regras (e unicidade de telefone/e-mail cruzada). */
    private void addLeitor() {
        String nome = tfNome.getText().trim();
        String email = tfMail.getText().trim();
        String tel = tfTel.getText().trim();

        // validações rápidas de UI
        if (nome.isEmpty() || nome.length() < 2 || !nome.matches("[\\p{L}][\\p{L} .'-]+")) {
            warn("Informe um nome válido (somente letras).");
            return;
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            warn("E-mail inválido. Ex.: usuario@dominio.com");
            return;
        }
        if (!tel.matches("\\d{9}")) {
            warn("Telefone deve ter exatamente 9 dígitos (somente números).");
            return;
        }

        try {
            // usa o serviço para validar unicidade (e-mail e telefone) em ambas as tabelas
            AuthService.RegisterResult r = ctx.auth.cadastrarAluno(nome, email, tel);
            info("Leitor adicionado.\nMatrícula: " + r.matricula + "\nSenha: " + r.senha);
            clearForm();
            loadLeitores();
        } catch (Exception ex) {
            err("Erro ao adicionar leitor: " + ex.getMessage());
        }
    }

    private void updateLeitor() {
        int id = parseId(tfId.getText());
        if (id <= 0) {
            warn("Selecione um leitor na tabela.");
            return;
        }

        String nome = tfNome.getText().trim();
        String email = tfMail.getText().trim();
        String tel = tfTel.getText().trim();

        // validações de formato
        if (nome.isEmpty() || nome.length() < 2 || !nome.matches("[\\p{L}][\\p{L} .'-]+")) {
            warn("Informe um nome válido (somente letras).");
            return;
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            warn("E-mail inválido. Ex.: usuario@dominio.com");
            return;
        }
        if (!tel.matches("\\d{9}")) {
            warn("Telefone deve ter exatamente 9 dígitos (somente números).");
            return;
        }

        // checagens de unicidade (email e telefone) contra LEITOR (exceto o próprio) e FUNCIONARIO
        try {
            if (ctx.leitorDAO.existsEmailForOtherId(id, email) || ctx.funcionarioDAO.existsEmail(email)) {
                warn("E-mail já cadastrado para outro usuário.");
                return;
            }
            if (ctx.leitorDAO.existsTelefoneForOtherId(id, tel) || ctx.funcionarioDAO.existsTelefone(tel)) {
                warn("Telefone já cadastrado para outro usuário.");
                return;
            }
        } catch (SQLException ex) {
            err("Erro validando unicidade: " + ex.getMessage());
            return;
        }

        Leitor l = new Leitor();
        l.setId(id);
        l.setNome(nome);
        l.setMatricula(tfMat.getText().trim()); // não alteramos matrícula no UPDATE
        l.setEmail(email);
        l.setTelefone(tel);

        try {
            ctx.leitorDAO.atualizar(l);
            info("Leitor atualizado.");
            loadLeitores();
        } catch (SQLException ex) {
            err("Erro ao atualizar leitor: " + ex.getMessage());
        }
    }

    private void removeLeitor() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Selecione um leitor."); return; }
        int id = (int) model.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Remover o leitor selecionado?",
                "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            ctx.leitorDAO.excluir(id);
            info("Leitor removido.");
            loadLeitores();
        } catch (SQLException ex) {
            err("Erro ao remover leitor: " + ex.getMessage());
        }
    }

    private void resetSenhaLeitor() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Selecione um leitor."); return; }
        String matricula = String.valueOf(model.getValueAt(row, 2));
        String nova = gerarSenha4();
        try {
            ctx.leitorDAO.atualizarSenhaPorMatricula(matricula, nova);
            info("Senha redefinida para: " + nova);
        } catch (SQLException ex) {
            err("Erro ao redefinir senha: " + ex.getMessage());
        }
    }

    private void clearForm() {
        tfId.setText("0");
        tfNome.setText("");
        tfMat.setText("");
        tfMail.setText("");
        tfTel.setText("");
        tfNome.requestFocus();
    }

    // ===== helpers =====

    private JTextField ro(String text) {
        JTextField tf = new JTextField(text, 20);
        tf.setEditable(false);
        tf.setBackground(new Color(245, 245, 245));
        return tf;
    }

    private void info(String m) { JOptionPane.showMessageDialog(this, m, "Sucesso", JOptionPane.INFORMATION_MESSAGE); }
    private void warn(String m) { JOptionPane.showMessageDialog(this, m, "Aviso", JOptionPane.WARNING_MESSAGE); }
    private void err(String m)  { JOptionPane.showMessageDialog(this, m, "Erro", JOptionPane.ERROR_MESSAGE); }

    private int parseId(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }

    private void setEnabledRecursively(Component c, boolean enabled) {
        c.setEnabled(enabled);
        if (c instanceof Container cont) {
            for (Component child : cont.getComponents())
                setEnabledRecursively(child, enabled);
        }
    }

    /** Gera matrícula única iniciando com prefixo (0 para leitor). (mantido para casos pontuais, não usado no add) */
    private String gerarMatricula(char prefixo) throws SQLException {
        while (true) {
            String mat = prefixo + String.format("%05d", rnd.nextInt(100000));
            if (!ctx.leitorDAO.existsMatricula(mat) && !ctx.funcionarioDAO.existsMatricula(mat))
                return mat;
        }
    }

    /** Senha numérica de 4 dígitos. */
    private String gerarSenha4() { return String.format("%04d", rnd.nextInt(10000)); }
}
