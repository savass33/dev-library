package view;

import model.Emprestimo;
import service.EmprestimoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/** Tela para registrar devolução (UI BR; persistência ISO). */
public class DevolucaoView extends JPanel {
    private final AppContext ctx;
    private final EmprestimoService service;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Livro", "Leitor", "Funcionário", "Empréstimo", "Prevista" }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JTextField tfDataDev = new JTextField(10); // dd/MM/yyyy
    private final DateTimeFormatter fmtBR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter fmtISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @SuppressWarnings("unused")
    public DevolucaoView(AppContext ctx) {
        this.ctx = ctx;
        this.service = ctx.emprestimoService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Devolução de Empréstimo");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tfDataDev.setToolTipText("Deixe vazio para usar a data de hoje (dd/MM/yyyy).");
        south.add(new JLabel("Data devolução (dd/MM/yyyy, opcional):"));
        south.add(tfDataDev);

        JButton btRefresh = new JButton("Atualizar");
        JButton btDevolver = new JButton("Devolver selecionado");
        south.add(btRefresh);
        south.add(btDevolver);
        add(south, BorderLayout.SOUTH);

        btRefresh.addActionListener(e -> loadOpenLoans());
        btDevolver.addActionListener(e -> doDevolver());

        loadOpenLoans();
    }

    private void loadOpenLoans() {
        try {
            List<Emprestimo> todos = ctx.emprestimoDAO.listar().stream()
                    .filter(e -> e.getData_devolucao() == null)
                    .collect(Collectors.toList());

            // Se for aluno, só mostra os dele
            if (ctx.session.isAluno() && ctx.session.leitor != null) {
                int idLeitor = ctx.session.leitor.getId();
                todos = todos.stream()
                        .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor)
                        .collect(Collectors.toList());
            }

            model.setRowCount(0);
            for (Emprestimo e : todos) {
                Vector<Object> row = new Vector<>();
                row.add(e.getid());
                row.add(e.getLivro() != null ? e.getLivro().getTitulo() : "-");
                row.add(e.getLeitor() != null ? e.getLeitor().getNome() : "-");
                row.add(e.getFuncionario() != null ? e.getFuncionario().getNome() : "-");
                row.add(isoToBr(e.getData_emprestimo()));
                row.add(isoToBr(e.getData_prevista()));
                model.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar empréstimos: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDevolver() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idEmp = (int) model.getValueAt(row, 0);

        String dataISO = null;
        String dataBR = tfDataDev.getText().trim();
        if (!dataBR.isEmpty()) {
            try {
                LocalDate d = LocalDate.parse(dataBR, fmtBR);
                dataISO = d.format(fmtISO);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Data inválida (dd/MM/yyyy).", "Aviso",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            service.devolverLivro(idEmp, dataISO);
            JOptionPane.showMessageDialog(this, "Devolução registrada.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            loadOpenLoans();

            // avisa o menu principal para atualizar a tela de empréstimos (livros disponíveis)
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainMenu mm) mm.refreshEmprestimoLists();
        } catch (EmprestimoService.ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* Helpers */
    private String isoToBr(String iso) {
        try {
            if (iso == null || iso.isBlank()) return "-";
            LocalDate d = LocalDate.parse(iso, fmtISO);
            return d.format(fmtBR);
        } catch (Exception e) {
            return iso != null ? iso : "-";
        }
    }
}
