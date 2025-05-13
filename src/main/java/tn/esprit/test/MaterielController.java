package tn.esprit.test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import tn.esprit.models.Materiel;
import tn.esprit.services.ServiceMateriel;
import io.github.cdimascio.dotenv.Dotenv;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class MaterielController implements Initializable {

    @FXML private TextField txtNom, txtQuantite, txtRecherche;
    @FXML private ComboBox<String> comboType, comboFiltre;
    @FXML private FlowPane materialCards;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnExporter, btnExportPDF, btnEnvoyerSMS;

    private ServiceMateriel service;
    private ObservableList<Materiel> materielList;
    private FilteredList<Materiel> filteredList;
    private Materiel selectedMateriel;
    private static final Dotenv dotenv = Dotenv.load();
    private static final List<String> TYPES = Arrays.asList(
            "Informatique", "Bureau", "Électronique", "Outil", "Mobilier", "Autre"
    );

    // Twilio credentials
    private static final String ACCOUNT_SID = dotenv.get("TWILIO_SID");
    private static final String AUTH_TOKEN = dotenv.get("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_FROM = dotenv.get("TWILIO_FROM", "+12177658106");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new ServiceMateriel();

        // Initialize Twilio
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Initialize ComboBox
        comboType.setItems(FXCollections.observableArrayList(TYPES));
        comboFiltre.setItems(FXCollections.observableArrayList(TYPES));
        comboFiltre.getItems().add(0, "Tous les types");
        comboFiltre.setValue("Tous les types");

        // Load and display cards
        refreshCards();

        // Handle search and filter
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        comboFiltre.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Initial button states
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnEnvoyerSMS.setDisable(false);
    }

    private void refreshCards() {
        materielList = FXCollections.observableArrayList(service.getAll());
        filteredList = new FilteredList<>(materielList, p -> true);
        updateCards();
    }

    private void updateCards() {
        materialCards.getChildren().clear();
        for (Materiel m : filteredList) {
            materialCards.getChildren().add(createCard(m));
        }
    }

    private VBox createCard(Materiel m) {
        VBox card = new VBox(5);
        card.getStyleClass().add("material-card");
        card.getChildren().addAll(
                new Label("Nom : " + m.getNom()),
                new Label("Type : " + m.getType()),
                new Label("Quantité : " + m.getQuantite())
        );
        card.setOnMouseClicked(event -> selectMaterial(m, card));
        return card;
    }

    private void selectMaterial(Materiel m, VBox card) {
        materialCards.getChildren().forEach(node -> node.getStyleClass().remove("selected-card"));
        card.getStyleClass().add("selected-card");
        selectedMateriel = m;
        txtNom.setText(m.getNom());
        comboType.setValue(m.getType());
        txtQuantite.setText(String.valueOf(m.getQuantite()));
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    private void applyFilters() {
        String search = txtRecherche.getText().toLowerCase();
        String filter = comboFiltre.getValue();
        filteredList.setPredicate(m -> {
            boolean matchesName = m.getNom().toLowerCase().contains(search);
            boolean matchesType = "Tous les types".equals(filter) || m.getType().equals(filter);
            return matchesName && matchesType;
        });
        updateCards();
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String type = comboType.getValue();
        String quantiteText = txtQuantite.getText().trim();

        if (nom.isEmpty() || type == null || quantiteText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont requis.");
            return;
        }

        try {
            int quantite = Integer.parseInt(quantiteText);
            if (quantite <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être positive.");
                return;
            }
            Materiel materiel = new Materiel(nom, type, quantite);
            service.add(materiel);
            refreshCards();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel ajouté avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre valide.");
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedMateriel == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun matériel sélectionné.");
            return;
        }

        String nom = txtNom.getText().trim();
        String type = comboType.getValue();
        String quantiteText = txtQuantite.getText().trim();

        if (nom.isEmpty() || type == null || quantiteText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Tous les champs sont requis.");
            return;
        }

        try {
            int quantite = Integer.parseInt(quantiteText);
            if (quantite <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être positive.");
                return;
            }
            selectedMateriel.setNom(nom);
            selectedMateriel.setType(type);
            selectedMateriel.setQuantite(quantite);
            service.update(selectedMateriel);
            refreshCards();
            clearForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel modifié avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La quantité doit être un nombre valide.");
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedMateriel == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun matériel sélectionné.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression de " + selectedMateriel.getNom() + " ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.delete(selectedMateriel);
                refreshCards();
                clearForm();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel supprimé avec succès.");
            }
        });
    }

    @FXML
    private void handleExporter() {
        TextInputDialog dialog = new TextInputDialog("materiel.csv");
        dialog.setTitle("Exporter CSV");
        dialog.setHeaderText("Exporter les données en CSV");
        dialog.setContentText("Nom du fichier :");
        dialog.showAndWait().ifPresent(fileName -> {
            service.exportToCSV(fileName);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Données exportées dans " + fileName);
        });
    }

    @FXML
    private void handleExportPDF() {
        TextInputDialog dialog = new TextInputDialog("materiel.pdf");
        dialog.setTitle("Exporter PDF");
        dialog.setHeaderText("Exporter les données en PDF");
        dialog.setContentText("Nom du fichier :");
        dialog.showAndWait().ifPresent(fileName -> {
            try {
                PdfWriter writer = new PdfWriter(new File(fileName));
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
                document.add(new Paragraph("Liste des Matériels"));
                for (Materiel m : materielList) {
                    document.add(new Paragraph(
                            "ID: " + m.getId() + ", Nom: " + m.getNom() + ", Type: " + m.getType() + ", Quantité: " + m.getQuantite()
                    ));
                }
                document.close();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF exporté dans " + fileName);
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation PDF : " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEnvoyerSMS() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Envoyer SMS");
        dialog.setHeaderText("Envoi de SMS via Twilio");
        dialog.setContentText("Numéro du destinataire (+216...) :");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String to = result.get().trim();
        String body = "Bonjour, inventaire matériel mis à jour. Total: " + materielList.size() + " éléments.";

        // Run Twilio SMS sending asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                Message message = Message.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(TWILIO_FROM),
                        body
                ).create();
                Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Succès", "SMS envoyé avec succès ! SID: " + message.getSid()));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur SMS : " + e.getMessage()));
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void clearForm() {
        txtNom.clear();
        txtQuantite.clear();
        comboType.setValue(null);
        selectedMateriel = null;
        materialCards.getChildren().forEach(node -> node.getStyleClass().remove("selected-card"));
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }
}