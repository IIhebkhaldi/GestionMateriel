package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Materiel;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class ServiceMateriel implements IService<Materiel> {
    private Connection cnx;

    public ServiceMateriel() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    private boolean validerSaisie(Materiel m) {
        if (m.getNom() == null || m.getNom().trim().isEmpty()) {
            System.out.println("Erreur : Le nom est obligatoire.");
            return false;
        }
        if (m.getType() == null || m.getType().trim().isEmpty()) {
            System.out.println("Erreur : Le type est obligatoire.");
            return false;
        }
        if (m.getQuantite() <= 0) {
            System.out.println("Erreur : La quantité doit être supérieure à zéro.");
            return false;
        }
        return true;
    }

    @Override
    public void add(Materiel m) {
        if (!validerSaisie(m)) return;

        String qry = "INSERT INTO `materiel`(`nom`, `type`, `quantite`) VALUES (?,?,?)";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, m.getNom());
            pstm.setString(2, m.getType());
            pstm.setInt(3, m.getQuantite());
            pstm.executeUpdate();

            // Récupérer l'ID généré
            ResultSet generatedKeys = pstm.getGeneratedKeys();
            if (generatedKeys.next()) {
                m.setId(generatedKeys.getInt(1));
                System.out.println("Matériel ajouté avec ID: " + m.getId());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Materiel> getAll() {
        List<Materiel> materiels = new ArrayList<>();
        String qry = "SELECT * FROM `materiel`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Materiel m = new Materiel();
                m.setId(rs.getInt("id"));
                m.setNom(rs.getString("nom"));
                m.setType(rs.getString("type"));
                m.setQuantite(rs.getInt("quantite"));

                // Débogage: Afficher les IDs lors du chargement
                System.out.println("Chargé matériel avec ID: " + m.getId());

                materiels.add(m);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des matériels: " + e.getMessage());
        }
        return materiels;
    }

    @Override
    public void update(Materiel m) {
        if (!validerSaisie(m)) return;

        String qry = "UPDATE `materiel` SET `nom`=?, `type`=?, `quantite`=? WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, m.getNom());
            pstm.setString(2, m.getType());
            pstm.setInt(3, m.getQuantite());
            pstm.setInt(4, m.getId());
            int rowsAffected = pstm.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Matériel modifié avec succès, ID: " + m.getId());
            } else {
                System.out.println("Aucun matériel trouvé avec ID: " + m.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public void delete(Materiel m) {
        String qry = "DELETE FROM `materiel` WHERE `id`=?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, m.getId());
            int rowsAffected = pstm.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Matériel supprimé avec succès, ID: " + m.getId());
            } else {
                System.out.println("Aucun matériel trouvé avec ID: " + m.getId());
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    public void exportToCSV(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("id,nom,type,quantite\n");
            for (Materiel m : getAll()) {
                writer.write(m.getId() + "," + m.getNom() + "," + m.getType() + "," + m.getQuantite() + "\n");
            }
            System.out.println("Exportation réussie dans le fichier : " + fileName);
        } catch (IOException e) {
            System.out.println("Erreur d'exportation : " + e.getMessage());
        }
    }
}