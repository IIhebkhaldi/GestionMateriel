package tn.esprit.test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import tn.esprit.models.Materiel;
import tn.esprit.services.ServiceMateriel;
import tn.esprit.utils.InfobipSmsService;

import io.github.cdimascio.dotenv.Dotenv;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class MaterielController implements Initializable {
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltre;
    @FXML private FlowPane materialCards;
    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnExporter, btnExportPDF, btnEnvoyerSMS;

    private ServiceMateriel service;
    private ObservableList<Materiel> materielList;
    private FilteredList<Materiel> filteredList;
    private Materiel selectedMateriel;

    private static final String SMS_FROM;
    private static final List<String> TYPES = Arrays.asList(
            "Informatique", "Bureau", "Électronique", "Outil", "Mobilier", "Autre"
    );

    static {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        SMS_FROM = dotenv.get("SMS_FROM", "InfoApp");
    }

    private InfobipSmsService smsService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new ServiceMateriel();
        smsService = new InfobipSmsService();
        comboFiltre.setItems(FXCollections.observableArrayList(TYPES));
        comboFiltre.getItems().add(0, "Tous les types");
        comboFiltre.setValue("Tous les types");
        refreshCards();
        txtRecherche.textProperty().addListener((o, old, n) -> applyFilters());
        comboFiltre.valueProperty().addListener((o, old, n) -> applyFilters());
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private void refreshCards() {
        materielList = FXCollections.observableArrayList(service.getAll());
        filteredList = new FilteredList<>(materielList, m -> true);
        updateCards();
    }

    private void updateCards() {
        materialCards.getChildren().clear();
        for (Materiel m : filteredList) materialCards.getChildren().add(createCard(m));
    }

    private VBox createCard(Materiel m) {
        VBox card = new VBox(5);
        card.getStyleClass().add("material-card");
        card.getChildren().addAll(
                new Label("Nom : " + m.getNom()),
                new Label("Type : " + m.getType()),
                new Label("Quantité : " + m.getQuantite())
        );
        card.setOnMouseClicked(e -> selectMaterial(m, card));
        return card;
    }

    private void selectMaterial(Materiel m, VBox card) {
        materialCards.getChildren().forEach(n -> n.getStyleClass().remove("selected-card"));
        card.getStyleClass().add("selected-card");
        selectedMateriel = m;
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    private void applyFilters() {
        String search = txtRecherche.getText().toLowerCase();
        String filter = comboFiltre.getValue();
        filteredList.setPredicate(m ->
                m.getNom().toLowerCase().contains(search) &&
                        ("Tous les types".equals(filter) || m.getType().equals(filter))
        );
        updateCards();
    }

    @FXML
    private void handleAjouter() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Matériel");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getStyleClass().add("form-panel");
        TextField txtNom = new TextField();
        txtNom.setPromptText("Nom du matériel");
        ComboBox<String> comboType = new ComboBox<>(FXCollections.observableArrayList(TYPES));
        comboType.setPromptText("Sélectionner un type");
        TextField txtQuantite = new TextField();
        txtQuantite.setPromptText("Quantité");

        content.getChildren().addAll(
                new Label("Nom :"), txtNom,
                new Label("Type :"), comboType,
                new Label("Quantité :"), txtQuantite
        );
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String nom = txtNom.getText().trim();
                String type = comboType.getValue();
                int quantite = Integer.parseInt(txtQuantite.getText().trim());
                if (nom.isEmpty() || type == null || quantite <= 0) throw new IllegalArgumentException();
                service.add(new Materiel(0, nom, type, quantite));
                refreshCards();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel ajouté");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vérifiez les champs: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedMateriel == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun matériel sélectionné");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier un Matériel");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getStyleClass().add("form-panel");
        TextField txtNom = new TextField(selectedMateriel.getNom());
        ComboBox<String> comboType = new ComboBox<>(FXCollections.observableArrayList(TYPES));
        comboType.setValue(selectedMateriel.getType());
        TextField txtQuantite = new TextField(String.valueOf(selectedMateriel.getQuantite()));

        content.getChildren().addAll(
                new Label("Nom :"), txtNom,
                new Label("Type :"), comboType,
                new Label("Quantité :"), txtQuantite
        );
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selectedMateriel.setNom(txtNom.getText().trim());
                selectedMateriel.setType(comboType.getValue());
                selectedMateriel.setQuantite(Integer.parseInt(txtQuantite.getText().trim()));
                service.update(selectedMateriel);
                refreshCards();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel modifié");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Modification impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedMateriel == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Supprimer un Matériel");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.getStyleClass().add("form-panel");
        content.getChildren().add(new Label("Confirmer la suppression de : " + selectedMateriel.getNom() + " ?"));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.delete(selectedMateriel);
                refreshCards();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Matériel supprimé");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExporter() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = chooser.showSaveDialog(materialCards.getScene().getWindow());
        if (f != null) {
            try {
                service.exportToCSV(f.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Exporté");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Export: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File f = chooser.showSaveDialog(materialCards.getScene().getWindow());
        if (f != null) {
            try (PdfWriter w = new PdfWriter(f); PdfDocument pd = new PdfDocument(w); Document doc = new Document(pd)) {
                doc.add(new Paragraph("Inventaire").setBold());
                for (Materiel m : materielList) {
                    doc.add(new Paragraph(m.getId() + "|" + m.getNom() + "|" + m.getType() + "|" + m.getQuantite()));
                }
                showAlert(Alert.AlertType.INFORMATION, "Succès", "PDF généré");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEnvoyerSMS() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Envoyer SMS");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        VBox c = new VBox(5);
        TextField ph = new TextField();
        ph.setPromptText("+216...");
        TextArea msg = new TextArea();
        msg.setText("Inventaire: " + materielList.size());
        c.getChildren().addAll(new Label("Tel:"), ph, new Label("Msg:"), msg);
        d.getDialogPane().setContent(c);
        Optional<ButtonType> r = d.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.OK) {
            final String to = ph.getText().trim().startsWith("+") ? ph.getText().trim() : "+216" + ph.getText().trim();
            final String m = msg.getText().trim();
            final AtomicBoolean cancel = new AtomicBoolean(false);
            ProgressIndicator p = new ProgressIndicator();
            Button b = new Button("Annuler");
            b.setOnAction(e -> cancel.set(true));
            Dialog<Void> pd = new Dialog<>();
            pd.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            pd.getDialogPane().setContent(new HBox(5, p, b));
            Platform.runLater(pd::show);
            CompletableFuture.runAsync(() -> {
                try {
                    if (!cancel.get()) smsService.sendSms(SMS_FROM, to, m);
                } catch (Exception ex) {
                    if (!cancel.get()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur SMS", "" + ex.getMessage()));
                        return;
                    }
                }
                if (!cancel.get()) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Succès", "Envoyé"));
                }
            });
        }
    }

    private void showAlert(Alert.AlertType t, String ti, String ms) {
        Alert a = new Alert(t);
        a.setTitle(ti);
        a.setHeaderText(null);
        a.setContentText(ms);
        a.showAndWait();
    }
}