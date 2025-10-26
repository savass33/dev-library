package view;

import model.Emprestimo;
import model.Leitor;
import service.EmprestimoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Mostra histórico de empréstimos (UI BR; aluno vê só os próprios). */
public class HistoricoLeitorView extends JPanel {
    private final AppContext ctx;
    private final EmprestimoService service;

    private final JComboBox<Leitor> cbLeitor = new JComboBox<>();
    private final JLabel lbLeitor = new JLabel("Leitor:");
    private final JButton btBuscar = new JButton("Buscar");
    private final JButton btAtualizar = new JButton("Atualizar leitores");

    private final DateTimeFormatter fmtBR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter fmtISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Livro", "Empréstimo", "Prevista", "Devolução", "Status" }, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    @SuppressWarnings("unused")
    public HistoricoLeitorView(AppContext ctx) {
        this.ctx = ctx;
        this.service = ctx.emprestimoService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        String titulo = (ctx.session != null && ctx.session.isAluno())
                ? "Meus Empréstimos"
                : "Histórico de Empréstimos por Leitor";

        JLabel title = new JLabel(titulo);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.add(title, BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(lbLeitor);
        filtros.add(cbLeitor);
        filtros.add(btBuscar);
        filtros.add(btAtualizar);

        north.add(filtros, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);

        cbLeitor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Leitor le) setText("#" + le.getId() + " - " + le.getNome());
                return this;
            }
        });

        btAtualizar.addActionListener(e -> loadLeitores());
        btBuscar.addActionListener(e -> loadHistorico());

        if (ctx.session != null && ctx.session.isAluno() && ctx.session.leitor != null) {
            // Aluno: trava no próprio leitor e esconde botões
            cbLeitor.removeAllItems();
            cbLeitor.addItem(ctx.session.leitor);
            cbLeitor.setEnabled(false);
            lbLeitor.setText("Leitor (sua conta):");
            btBuscar.setVisible(false);
            btAtualizar.setVisible(false);
            loadHistorico();
        } else {
            loadLeitores();
        }
    }

    private void loadLeitores() {
        try {
            cbLeitor.removeAllItems();
            for (Leitor le : ctx.leitorDAO.listar()) cbLeitor.addItem(le);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar leitores: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHistorico() {
        Leitor le = (Leitor) cbLeitor.getSelectedItem();
        if ((le == null) && ctx.session != null && ctx.session.isAluno()) {
            le = ctx.session.leitor;
            if (le == null) {
                JOptionPane.showMessageDialog(this, "Sessão de aluno sem leitor associado.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            cbLeitor.removeAllItems();
            cbLeitor.addItem(le);
            cbLeitor.setEnabled(false);
        }

        if (le == null) {
            JOptionPane.showMessageDialog(this, "Selecione um leitor.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Emprestimo> list = service.historicoDeLeitor(le.getId());
            model.setRowCount(0);
            for (Emprestimo e : list) {
                String status = e.getData_devolucao() == null ? "Em aberto" : "Devolvido";
                model.addRow(new Object[] {
                        e.getid(),
                        e.getLivro() != null ? e.getLivro().getTitulo() : "-",
                        isoToBr(e.getData_emprestimo()),
                        isoToBr(e.getData_prevista()),
                        e.getData_devolucao() != null ? isoToBr(e.getData_devolucao()) : "-",
                        status
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar histórico: " + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

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
