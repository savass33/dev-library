package view;

import controller.AlunoController;

import java.util.Scanner;

public class MenuPrincipal {
    public static void main(String[] args) {
        AlunoController alunoController = new AlunoController();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== MENU BIBLIOTECA ===");
            System.out.println("1 - Listar Alunos");
            System.out.println("0 - Sair");
            System.out.print("Escolha: ");
            int opcao = sc.nextInt();

            switch (opcao) {
                case 1 -> alunoController.listarAlunos();
                case 0 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }
}
