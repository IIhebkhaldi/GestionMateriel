package tn.esprit.test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import tn.esprit.models.Materiel;
import tn.esprit.services.ServiceMateriel;
import io.github.cdimascio.dotenv.Dotenv;

public class MaterielController implements Initializable {

    @FXML private TextField txtNom, txtQuantite, txtRecherche;
    @FXML private ComboBox<String> comboType, comboFiltre;
    @FXML private FlowPane materialCards;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnExporter, btnExportPDF, btnEnvoyerSMS;

    private ServiceMateriel service;
    private ObservableList<Materiel> materielList;
    private FilteredList<Materiel> filteredList;
    private Materiel selectedMateriel;
    static Dotenv dotenv = Dotenv.load();
    private static final List<String> TYPES = Arrays.asList(
            "Informatique", "Bureau", "Électronique", "Outil", "Mobilier", "Autre"
    );

    // Twilio credentials
    private static final String ACCOUNT_SID = dotenv.get("TWILIO_SID");
    private static final String AUTH_TOKEN  = dotenv.get("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_FROM = "+12177658106";

    private HttpClient httpClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new ServiceMateriel();
        httpClient = HttpClient.newHttpClient();

        // Initialisation des ComboBox
        comboType.setItems(FXCollections.observableArrayList(TYPES));
        comboFiltre.setItems(FXCollections.observableArrayList(TYPES));
        comboFiltre.getItems().add(0, "Tous les types");
        comboFiltre.setValue("Tous les types");

        // Charger et afficher les cartes
        refreshCards();

        // Gestion de la recherche et du filtrage
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        comboFiltre.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Etat initial des boutons
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

    @FXML private void handleAjouter() {
        // Implémentation ajout
    }
    @FXML private void handleModifier() {
        // Implémentation modification
    }
    @FXML private void handleSupprimer() {
        // Implémentation suppression
    }
    @FXML private void handleExporter() {
        // Implémentation CSV
    }
    @FXML private void handleExportPDF() {
        // Implémentation PDF
    }

    @FXML private void handleEnvoyerSMS() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Envoyer SMS");
        dialog.setHeaderText("Envoi de SMS via Twilio");
        dialog.setContentText("Numéro du destinataire (+216...) :");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        String to = result.get().trim();
        String body = "Bonjour depuis JavaFX !";
        try {
            String auth = Base64.getEncoder()
                    .encodeToString((ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(StandardCharsets.UTF_8));
            String form = "To=" + URLEncoder.encode(to, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(TWILIO_FROM, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + ACCOUNT_SID + "/Messages.json"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) showAlert("SMS envoyé avec succès !");
            else showAlert("Échec SMS (" + response.statusCode() + "): " + response.body());
        } catch (IOException | InterruptedException e) {
            showAlert("Erreur SMS : " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        // Réinitialiser les champs et sélection
    }
}
