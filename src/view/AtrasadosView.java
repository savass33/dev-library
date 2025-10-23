package view;

import view.AppContext;
import model.Emprestimo;
import service.EmprestimoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Lista empréstimos atrasados e dias de atraso (baseado no Service). */
public class AtrasadosView extends JPanel {
    private final EmprestimoService service;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Livro", "Leitor", "Prevista", "Dias atraso"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public AtrasadosView(AppContext ctx) {
        this.service = ctx.emprestimoService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Empréstimos Atrasados");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btRefresh = new JButton("Atualizar");
        south.add(btRefresh);
        add(south, BorderLayout.SOUTH);

        btRefresh.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        try {
            List<Emprestimo> atrasados = service.listarAtrasados();
            model.setRowCount(0);
            for (Emprestimo e : atrasados) {
                long dias = service.calcularDiasAtraso(e);
                model.addRow(new Object[]{
                        e.getid(),
                        e.getLivro() != null ? e.getLivro().getTitulo() : "-",
                        e.getLeitor() != null ? e.getLeitor().getNome() : "-",
                        e.getData_prevista(),
                        dias
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar atrasados: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

