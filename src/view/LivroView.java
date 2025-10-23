package view;

import model.Livro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LivroView extends JPanel {
    private final AppContext ctx;

    private final JTextField tfTitulo = new JTextField(28);
    private final JTextField tfIsbn   = new JTextField(16);
    private final JTextField tfAutor  = new JTextField(24);
    private final JTextField tfAno    = new JTextField(6);
    private final JTextField tfGenero = new JTextField(16);

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Título", "ISBN", "Autor", "Ano", "Gênero", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public LivroView(AppContext ctx) {
        this.ctx = ctx;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header
        JLabel title = new JLabel("Gerenciar Livros");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.add(title, BorderLayout.NORTH);
        north.add(buildForm(), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadLivros();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;

        int y = 0;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Título:"), gc);
        gc.gridx = 1; form.add(tfTitulo, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("ISBN:"), gc);
        gc.gridx = 1; form.add(tfIsbn, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Autor:"), gc);
        gc.gridx = 1; form.add(tfAutor, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Ano:"), gc);
        gc.gridx = 1; form.add(tfAno, gc); y++;

        gc.gridx = 0; gc.gridy = y; form.add(new JLabel("Gênero:"), gc);
        gc.gridx = 1; form.add(tfGenero, gc); y++;

        return form;
    }

    private JPanel buildButtons() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btAdd = new JButton("Adicionar");
        JButton btRemove = new JButton("Remover selecionado");
        JButton btRefresh = new JButton("Atualizar");
        JButton btClear = new JButton("Limpar");

        south.add(btClear);
        south.add(btRefresh);
        south.add(btRemove);
        south.add(btAdd);

        btAdd.addActionListener(e -> doAdicionar());
        btRemove.addActionListener(e -> doRemover());
        btRefresh.addActionListener(e -> loadLivros());
        btClear.addActionListener(e -> clearForm());

        return south;
    }

    private void loadLivros() {
        try {
            List<Livro> livros = ctx.livroDAO.listar();
            model.setRowCount(0);
            for (Livro l : livros) {
                model.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getIsbn(),
                        l.getAutor(),
                        l.getAnoPublicacao(),
                        l.getGenero(),
                        l.getStatus() != null ? l.getStatus() : "Disponível"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar livros: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAdicionar() {
        String titulo = tfTitulo.getText().trim();
        String isbn   = tfIsbn.getText().trim();
        String autor  = tfAutor.getText().trim();
        String ano    = tfAno.getText().trim();
        String genero = tfGenero.getText().trim();

        if (titulo.isEmpty() || isbn.isEmpty() || autor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha Título, ISBN e Autor.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // No seu modelo: new Livro(titulo, isbn, autor, ano, genero)
            Livro novo = new Livro(titulo, isbn, autor, ano, genero);
            ctx.livroDAO.inserir(novo); // status cai no DEFAULT 'Disponível'
            JOptionPane.showMessageDialog(this, "Livro adicionado.", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadLivros();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRemover() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um livro na tabela.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 6));

        if (!"Disponível".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Não é possível remover um livro emprestado.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opt = JOptionPane.showConfirmDialog(this,
                "Remover o livro selecionado (ID " + id + ")?",
                "Confirmar remoção", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt != JOptionPane.YES_OPTION) return;

        try {
            ctx.livroDAO.excluir(id);
            JOptionPane.showMessageDialog(this, "Livro removido.", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            loadLivros();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao remover: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        tfTitulo.setText("");
        tfIsbn.setText("");
        tfAutor.setText("");
        tfAno.setText("");
        tfGenero.setText("");
        tfTitulo.requestFocus();
    }
}
