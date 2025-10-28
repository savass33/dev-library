package view;

import model.Emprestimo;
import model.Funcionario;
import model.Leitor;
import model.Livro;
import service.EmprestimoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/** Lista empréstimos atrasados e permite criar atraso de teste e pagar multa. */
public class AtrasadosView extends JPanel {
    private final AppContext ctx;
    private final EmprestimoService service;

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Livro", "Leitor", "Prevista (BR)", "Dias atraso"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final JLabel lbMulta = new JLabel("Multa estimada: R$ 0,00 (R$ 1,00/dia)");
    private final JButton btCriarAtraso = new JButton("Criar atraso de teste");
    private final JButton btPagar = new JButton("Pagar multa do selecionado");
    private final JButton btRefresh = new JButton("Atualizar");

    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter BR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Cache da listagem para mapear seleção -> objeto. */
    private List<Emprestimo> cache = new ArrayList<>();

    public AtrasadosView(AppContext ctx) {
        this.ctx = ctx;
        this.service = ctx.emprestimoService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Empréstimos Atrasados");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());

        JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 6, 4, 6);
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        left.add(lbMulta, gc);
        gc.gridy++; left.add(btPagar, gc);
        gc.gridy++; left.add(btCriarAtraso, gc);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btRefresh);

        south.add(left, BorderLayout.WEST);
        south.add(right, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        btRefresh.addActionListener(e -> loadData());
        btCriarAtraso.addActionListener(e -> criarAtrasoDeTeste());
        btPagar.addActionListener(e -> pagarSelecionado());
        table.getSelectionModel().addListSelectionListener(this::onSelectRow);

        loadData();
    }

    private void onSelectRow(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0 || row >= cache.size()) {
            lbMulta.setText("Multa estimada: R$ 0,00 (R$ 1,00/dia)");
            return;
        }
        Emprestimo emp = cache.get(row);
        double valor = service.valorMultaAtual(emp, LocalDate.now());
        lbMulta.setText(String.format("Multa estimada: R$ %.2f (R$ 1,00/dia)", valor));
    }

    private void loadData() {
        try {
            List<Emprestimo> atrasados = service.listarAtrasados();

            // Se for aluno, ver apenas os seus
            if (ctx.session != null && ctx.session.isAluno() && ctx.session.leitor != null) {
                int idLeitor = ctx.session.leitor.getId();
                atrasados = atrasados.stream()
                        .filter(e -> e.getLeitor() != null && e.getLeitor().getId() == idLeitor)
                        .collect(Collectors.toList());
            }

            cache = atrasados;
            model.setRowCount(0);
            for (Emprestimo e : atrasados) {
                long dias = service.calcularDiasAtraso(e);
                String previstaBr = "-";
                try {
                    if (e.getData_prevista() != null)
                        previstaBr = LocalDate.parse(e.getData_prevista(), ISO).format(BR);
                } catch (Exception ignored) {}
                model.addRow(new Object[]{
                        e.getid(),
                        e.getLivro() != null ? e.getLivro().getTitulo() : "-",
                        e.getLeitor() != null ? e.getLeitor().getNome() : "-",
                        previstaBr,
                        dias
                });
            }
            lbMulta.setText("Multa estimada: R$ 0,00 (R$ 1,00/dia)");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao listar atrasados: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cria um empréstimo artificialmente atrasado com um livro disponível. */
    private void criarAtrasoDeTeste() {
        try {
            // Livro disponível
            List<Livro> disponiveis = ctx.livroDAO.listar().stream()
                    .filter(l -> "Disponível".equalsIgnoreCase(l.getStatus()))
                    .collect(Collectors.toList());
            if (disponiveis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há livros disponíveis para teste.",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Livro livro = disponiveis.get(0);

            // Leitor
            Leitor leitor;
            if (ctx.session != null && ctx.session.isAluno() && ctx.session.leitor != null) {
                leitor = ctx.session.leitor;
            } else {
                List<Leitor> leitores = ctx.leitorDAO.listar();
                if (leitores.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Não há leitores cadastrados.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                leitor = leitores.get(0);
            }

            // Funcionário
            Funcionario func;
            if (ctx.session != null && ctx.session.funcionario != null) {
                func = ctx.session.funcionario;
            } else {
                List<Funcionario> funcs = ctx.funcionarioDAO.listar();
                if (funcs.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Não há funcionários cadastrados.",
                            "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                func = funcs.get(0);
            }

            // Datas para garantir atraso
            LocalDate dataEmp = LocalDate.now().minusDays(10);
            LocalDate dataPrev = LocalDate.now().minusDays(3);

            Emprestimo criado = service.criarEmprestimoAtrasado(
                    livro.getId(), leitor.getId(), func.getID(), dataEmp, dataPrev);

            JOptionPane.showMessageDialog(this,
                    "Empréstimo atrasado criado:\n" +
                    "Livro: " + (criado.getLivro() != null ? criado.getLivro().getTitulo() : "-") + "\n" +
                    "Leitor: " + (criado.getLeitor() != null ? criado.getLeitor().getNome() : "-"),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            loadData();

            // Atualiza listas de disponibilidade no menu
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainMenu mm) mm.refreshEmprestimoLists();
            if (w instanceof AlunoMenu am) am.repaint();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar atraso de teste: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Paga a multa: devolve o livro (libera disponibilidade), registra pagamento e atualiza a lista. */
    private void pagarSelecionado() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= cache.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Emprestimo emp = cache.get(row);

        try {
            // calcula valor antes de fechar o empréstimo
            double valor = service.valorMultaAtual(emp, LocalDate.now());

            // devolve: grava data_devolucao e libera livro; gera multa (não-paga)
            service.devolverLivro(emp.getid(), null);

            // registra pagamento (marca como paga/atualiza valor)
            service.pagarMulta(emp.getid(), valor, LocalDate.now());

            JOptionPane.showMessageDialog(this,
                    String.format("Multa paga com sucesso!\nValor: R$ %.2f", valor),
                    "Pagamento registrado", JOptionPane.INFORMATION_MESSAGE);

            // atualiza tela: empréstimo saiu da condição de atrasado
            loadData();

            // atualiza disponibilidade no restante do app
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof MainMenu mm) mm.refreshEmprestimoLists();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao pagar multa: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
