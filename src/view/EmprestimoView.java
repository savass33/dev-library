package view;

import service.EmprestimoService;
import service.EmprestimoService.ServiceException;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/** Tela para criar novo empréstimo (com escolha de data e data prevista). */
public class EmprestimoView extends JPanel {
    private final AppContext ctx;
    private final EmprestimoService service;

    private final JComboBox<Livro> cbLivro = new JComboBox<>();
    private final JComboBox<Leitor> cbLeitor = new JComboBox<>();
    private final JComboBox<Funcionario> cbFuncionario = new JComboBox<>();

    private final JTextField tfDataEmprestimo = new JTextField(10); // yyyy-MM-dd
    private final JTextField tfDataPrevista = new JTextField(10); // yyyy-MM-dd

    private final DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

    @SuppressWarnings("unused")
    public EmprestimoView(AppContext ctx) {
        this.ctx = ctx;
        this.service = ctx.emprestimoService;

        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JLabel title = new JLabel("Novo Empréstimo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gc.gridx = 0;
        gc.gridy = y++;
        gc.gridwidth = 3;
        add(title, gc);
        gc.gridwidth = 1;

        // Livro
        gc.gridx = 0;
        gc.gridy = y;
        add(new JLabel("Livro (disponível):"), gc);
        gc.gridx = 1;
        add(cbLivro, gc);
        y++;

        // Leitor
        gc.gridx = 0;
        gc.gridy = y;
        add(new JLabel("Leitor:"), gc);
        gc.gridx = 1;
        add(cbLeitor, gc);
        y++;

        // Funcionário
        gc.gridx = 0;
        gc.gridy = y;
        add(new JLabel("Funcionário:"), gc);
        gc.gridx = 1;
        add(cbFuncionario, gc);
        y++;

        // Data empréstimo + botão Hoje
        gc.gridx = 0;
        gc.gridy = y;
        add(new JLabel("Data do empréstimo (yyyy-MM-dd):"), gc);
        JPanel pEmp = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pEmp.add(tfDataEmprestimo);
        JButton btHoje = new JButton("Hoje");
        btHoje.addActionListener(e -> tfDataEmprestimo.setText(LocalDate.now().format(fmt)));
        pEmp.add(btHoje);
        gc.gridx = 1;
        add(pEmp, gc);
        y++;

        // Data prevista + botão +7 dias
        gc.gridx = 0;
        gc.gridy = y;
        add(new JLabel("Data prevista (yyyy-MM-dd):"), gc);
        JPanel pPrev = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pPrev.add(tfDataPrevista);

        gc.gridx = 1;
        add(pPrev, gc);
        y++;

        // Botões
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btEmprestar = new JButton("Emprestar");
        JButton btAtualizar = new JButton("Atualizar listas");
        buttons.add(btAtualizar);
        buttons.add(btEmprestar);

        gc.gridx = 0;
        gc.gridy = y;
        gc.gridwidth = 3;
        add(buttons, gc);

        // Ações
        btAtualizar.addActionListener(e -> loadCombos());
        btEmprestar.addActionListener(e -> doEmprestar());

        // Renderers (exibição dos combos)
        cbLivro.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Livro l)
                    setText("#" + l.getId() + " - " + l.getTitulo() + " [" + l.getStatus() + "]");
                return this;
            }
        });
        cbLeitor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Leitor le)
                    setText("#" + le.getId() + " - " + le.getNome() + " (" + le.getEmail() + ")");
                return this;
            }
        });
        cbFuncionario.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Funcionario f)
                    setText("#" + f.getID() + " - " + f.getNome());
                return this;
            }
        });

        // Defaults
        tfDataEmprestimo.setText(LocalDate.now().format(fmt));
        tfDataPrevista.setText(LocalDate.now().plusDays(7).format(fmt));
        loadCombos();
    }

    private void loadCombos() {
        try {
            // Livros apenas disponíveis
            List<Livro> livrosDisp = ctx.livroDAO.listar().stream()
                    .filter(l -> "Disponível".equalsIgnoreCase(l.getStatus()))
                    .collect(Collectors.toList());
            cbLivro.removeAllItems();
            for (Livro l : livrosDisp)
                cbLivro.addItem(l);

            // Leitores
            cbLeitor.removeAllItems();
            if (ctx.session.isAluno() && ctx.session.leitor != null) {
                cbLeitor.addItem(ctx.session.leitor);
                cbLeitor.setEnabled(false); // aluno só pode para ele mesmo
            } else {
                cbLeitor.setEnabled(true);
                for (Leitor le : ctx.leitorDAO.listar())
                    cbLeitor.addItem(le);
            }

            // Funcionários
            cbFuncionario.removeAllItems();
            for (Funcionario f : ctx.funcionarioDAO.listar())
                cbFuncionario.addItem(f);

            if (!ctx.session.isAluno()) {
                JOptionPane.showMessageDialog(this,
                        "Listas atualizadas (" + livrosDisp.size() + " livros disponíveis).",
                        "OK", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showError("Erro ao carregar dados: " + ex.getMessage());
        }
    }

    private void doEmprestar() {
        Livro livro = (Livro) cbLivro.getSelectedItem();
        Leitor leitor = ctx.session.isAluno() ? ctx.session.leitor : (Leitor) cbLeitor.getSelectedItem();
        Funcionario func = (Funcionario) cbFuncionario.getSelectedItem();

        if (livro == null || leitor == null || func == null) {
            showWarn("Selecione livro e funcionário. (Leitor é fixo para alunos)");
            return;
        }

        String dataEmp = tfDataEmprestimo.getText().trim();
        String dataPrev = tfDataPrevista.getText().trim();

        // valida datas
        try {
            LocalDate.parse(dataEmp, fmt);
            LocalDate.parse(dataPrev, fmt);
        } catch (Exception e) {
            showWarn("Datas inválidas. Use o formato yyyy-MM-dd.");
            return;
        }

        try {
            Emprestimo emp = service.emprestarLivro(livro.getId(), leitor.getId(), func.getID(), dataEmp);
            if (emp != null) {
                // Se o usuário ajustou manualmente a data prevista, atualize no DAO se for
                // diferente
                if (!emp.getData_prevista().equals(dataPrev)) {
                    ctx.emprestimoDAO.atualizarDataPrevista(emp.getid(), dataPrev);
                }

                JOptionPane.showMessageDialog(this,
                        "Empréstimo criado!\nLivro: " + emp.getLivro().getTitulo() +
                                "\nLeitor: " + emp.getLeitor().getNome() +
                                "\nPrevista: " + dataPrev,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                loadCombos(); // livro saiu da lista de disponíveis
            } else {
                showWarn("Empréstimo criado, mas não foi possível recarregar os dados.");
            }
        } catch (ServiceException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Erro: " + ex.getMessage());
        }
    }

    public void refresh() {
        loadCombos();
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
