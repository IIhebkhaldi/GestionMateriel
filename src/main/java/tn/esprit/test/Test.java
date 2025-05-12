package tn.esprit.test;

import tn.esprit.models.Materiel;
import tn.esprit.services.ServiceMateriel;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        ServiceMateriel sm = new ServiceMateriel();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Menu Materiel ===");
            System.out.println("1. Ajouter un matériel");
            System.out.println("2. Afficher tout");
            System.out.println("3. Modifier un matériel");
            System.out.println("4. Supprimer un matériel");
            System.out.println("5. Exporter les données");
            System.out.println("6. Quitter");
            System.out.print("Choix : ");
            int choix = sc.nextInt();
            sc.nextLine(); // vider le buffer

            switch (choix) {
                case 1:
                    System.out.print("Nom : ");
                    String nom = sc.nextLine();
                    System.out.print("Type : ");
                    String type = sc.nextLine();
                    System.out.print("Quantité : ");
                    int quantite = sc.nextInt();
                    sm.add(new Materiel(nom, type, quantite));
                    break;

                case 2:
                    System.out.println(sm.getAll());
                    break;

                case 3:
                    System.out.print("ID à modifier : ");
                    int idUpdate = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Nouveau nom : ");
                    String newNom = sc.nextLine();
                    System.out.print("Nouveau type : ");
                    String newType = sc.nextLine();
                    System.out.print("Nouvelle quantité : ");
                    int newQuantite = sc.nextInt();
                    sm.update(new Materiel(idUpdate, newNom, newType, newQuantite));
                    break;

                case 4:
                    System.out.print("ID à supprimer : ");
                    int idDelete = sc.nextInt();
                    sm.delete(new Materiel(idDelete, "", "", 0));
                    break;

                case 5:
                    System.out.print("Nom du fichier   ");
                    String fileName = sc.next();
                    sm.exportToCSV(fileName);
                    break;
                case 6:
                    System.exit(0);


                default:
                    System.out.println("Choix invalide !");
            }
        }
    }
}
