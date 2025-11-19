package org.openjfx.sio2E4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.service.NoteService;
import org.openjfx.sio2E4.util.AlertHelper;

public class EvaluationListController {

	User currentUser = AuthService.getCurrentUser();
	String role = currentUser.getRole().getLibelle();

	/* Tableau d'affichage de note */
	@FXML
	private TableView<Evaluation> evaluationTable;
	@FXML
	private TableColumn<Evaluation, String> titleColumn;
	@FXML
	private TableColumn<Evaluation, String> enseignantColumn;
	@FXML
	private TableColumn<Evaluation, String> matiereColumn;
	@FXML
	private TableColumn<Evaluation, Object> moyennes;
	@FXML
	private TableColumn<Evaluation, String> moyenneColumn;
	@FXML
	private TableColumn<Evaluation, String> moyenneMinColumn;
	@FXML
	private TableColumn<Evaluation, String> moyenneMaxColumn;

	@FXML
	private TableColumn<Evaluation, String> dateColumn;
	@FXML
	private TableColumn<Evaluation, String> noteTypeColumn;
	@FXML
	private TableColumn<Evaluation, String> coefficientColumn;

	@FXML
	private TableColumn<Evaluation, String> modificationColumn;

	@FXML
	private TableColumn actionsColumn;

	private final String API_URL = "http://localhost:8080/api/notes";
	private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();

	@FXML
	public void initialize() {

		// Mapping des colonnes
		titleColumn.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().getTitre()));

		enseignantColumn.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().getEnseignant().getPrenom() + " " + data.getValue().getEnseignant().getNom()));

		matiereColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMatiere().getLibelle()));


		moyenneColumn.setCellValueFactory(data -> {
			return new SimpleStringProperty(String.valueOf(NoteService.calculateMoyenne(data.getValue().getNotes())));
		});

		// TODO mettre en place un calcul de moyenne, note min et note max

		dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));

		// Nouvelle colonne pour le type de la note
		noteTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoteType().getLibelle()));

		coefficientColumn.setCellValueFactory(
				data -> new SimpleStringProperty(String.valueOf(data.getValue().getCoefficient())));

		modificationColumn.setCellValueFactory(data -> {
			String modif = data.getValue().getModification();
			if (modif == null || modif.isEmpty()) {
				return new SimpleStringProperty("");
			}
			// Remplacer 'T' par un espace
			modif = modif.replace("T", " ");

			// Trouver la position du point '.' qui précède les millisecondes
			int dotIndex = modif.indexOf('.');
			if (dotIndex != -1) {
				// On coupe la chaîne juste avant le point pour supprimer tout après
				modif = modif.substring(0, dotIndex);
			}

			return new SimpleStringProperty(modif);
		});


		actionsColumn.setCellFactory(column -> new TableCell<Evaluation, Void>() {
			private final Button viewButton = new Button();
			private final Button editButton = new Button();
			private final Button deleteButton = new Button();
			private final HBox buttonsBox = new HBox(8);

			{
				// Icône SVG Eye (Voir)
				SVGPath viewIcon = new SVGPath();
				viewIcon.setContent("M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z");
				viewIcon.setScaleX(0.8);
				viewIcon.setScaleY(0.8);
				viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);
				viewButton.setGraphic(viewIcon);
				viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
				viewButton.setTooltip(new Tooltip("Voir la fiche"));

				// Icône SVG Pencil (Modifier)
				SVGPath editIcon = new SVGPath();
				editIcon.setContent("M15.728 9.686l-1.414-1.414L5 17.586V19h1.414l9.314-9.314zm1.414-1.414l1.414-1.414-1.414-1.414-1.414 1.414 1.414 1.414zM7.242 21H3v-4.243L16.435 3.322a1 1 0 0 1 1.414 0l2.829 2.829a1 1 0 0 1 0 1.414L7.243 21z");
				editIcon.setScaleX(0.8);
				editIcon.setScaleY(0.8);
				editIcon.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_ICON);
				editButton.setGraphic(editIcon);
				editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
				editButton.setTooltip(new Tooltip("Modifier"));

				// Icône SVG Trash (Supprimer)
				SVGPath deleteIcon = new SVGPath();
				deleteIcon.setContent("M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z");
				deleteIcon.setScaleX(0.8);
				deleteIcon.setScaleY(0.8);
				deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
				deleteButton.setGraphic(deleteIcon);
				deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
				deleteButton.setTooltip(new Tooltip("Supprimer"));

				// Effets hover pour le bouton Voir fiche utilisateur
				viewButton.setOnMouseEntered(e -> {
					viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_HOVER);
					viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON_HOVER);
				});
				viewButton.setOnMouseExited(e -> {
					viewButton.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON);
					viewIcon.setStyle(StyleConstants.ButtonActionsColumn.VIEW_BUTTON_ICON);
				});

				// Effets hover pour le bouton Modifier
				editButton.setOnMouseEntered(e -> {
					editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON_HOVER);
				});
				editButton.setOnMouseExited(e -> {
					editButton.setStyle(StyleConstants.ButtonActionsColumn.EDIT_BUTTON);
				});

				// Effets hover pour le bouton Supprimer
				deleteButton.setOnMouseEntered(e -> {
					deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_HOVER);
					deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON_HOVER);
				});
				deleteButton.setOnMouseExited(e -> {
					deleteButton.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON);
					deleteIcon.setStyle(StyleConstants.ButtonActionsColumn.DELETE_BUTTON_ICON);
				});

				buttonsBox.setAlignment(Pos.CENTER_RIGHT);
				buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);
				buttonsBox.setPadding(new Insets(0, 10, 0, 0));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || getTableRow() == null) {
					setGraphic(null);
				} else {
					Evaluation evaluation = getTableView().getItems().get(getIndex());

					// Actions des boutons
					viewButton.setOnAction(event -> showViewEvaluationPage(evaluation));
					editButton.setOnAction(event -> showEditEvaluationPage(evaluation));
					deleteButton.setOnAction(event -> handleDeleteEvaluation(evaluation));

					setGraphic(buttonsBox);
				}
			}
		});


		// Définir les largeurs en pourcentage de la largeur totale du tableau
		titleColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10)
		);
		enseignantColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10) // 30
		);
		matiereColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10)
		);
		moyennes.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.20) // 60
		);
		coefficientColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.06) //
		);
		noteTypeColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10) // 90
		);
		dateColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10) // 90
		);
		modificationColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.10) // 90
		);
		actionsColumn.prefWidthProperty().bind(
				evaluationTable.widthProperty().multiply(0.14)
		);
		evaluationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// Chargement des données du tableau
		fetchEvaluations();
	}

	private void fetchEvaluations() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.NOTES))
					.header("Authorization", BEARER_TOKEN)
					.GET().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
					.thenAccept(this::parseEvaluations).exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			System.out.println("Mode hors ligne activé — chargement des notes en local");
			ArrayList<Evaluation> localEvaluations = LocalStorageService.loadEvaluations();
			Platform.runLater(() -> evaluationTable.getItems().setAll(localEvaluations));
		}

	}

	private void parseEvaluations(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {

			List<Evaluation> evaluations = Arrays.asList(mapper.readValue(responseBody, Evaluation[].class));
			Platform.runLater(() -> evaluationTable.getItems().setAll(evaluations));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@FXML
	private void handleDeleteEvaluation(Evaluation evaluation) {
		if (evaluation == null) {
			AlertHelper.showWarning("Veuillez sélectionner une note à supprimer.");
			return;
		}

		deleteEvaluation(evaluation.getId());
	}

	private void deleteEvaluation(int noteId) {
		if(NetworkService.isOnline()) {
			try {
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, noteId)))
						.header("Authorization", "Bearer " + AuthService.getToken()).DELETE().build();

				client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
					if (response.statusCode() == 204) {
						Platform.runLater(() -> {
							AlertHelper.showInformation("Note supprimée avec succès.");
							fetchEvaluations(); // Méthode pour recharger la liste
						});
					} else {
						Platform.runLater(() -> AlertHelper.showError("Erreur lors de la suppression de la note."));
					}
				}).exceptionally(e -> {
					e.printStackTrace();
					Platform.runLater(() -> AlertHelper.showError("Erreur réseau : " + e.getMessage()));
					return null;
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			ArrayList<Evaluation> evaluations = LocalStorageService.loadEvaluations();
			Optional<Evaluation> evaluation = evaluations.stream()
					.filter(u -> u.getId()==noteId)
					.findFirst();
			if (evaluation.isPresent()) {
				for (Note note : evaluation.get().getNotes()) {
					LocalStorageService.remove(note);
				}
				LocalStorageService.remove(evaluation.get());

				Platform.runLater(() -> {
					AlertHelper.showInformation("Evaluation (et notes associées) supprimé en local (mode hors ligne).");
					fetchEvaluations();
				});
			}
		}
	}

	private MainLayoutController mainLayoutController;

	public void setMainLayoutController(MainLayoutController controller) {
		this.mainLayoutController = controller;
	}

	@FXML
	private void showViewEvaluationPage(Evaluation evaluation) {
		if (evaluation != null && mainLayoutController != null) {
			mainLayoutController.showViewEvaluationFormPage(evaluation.getId());
		}
	}

	@FXML
	private void showCreateEvaluationPage() {
		mainLayoutController.showCreateEvaluationFormPage();
	}

	@FXML
	private void showEditEvaluationPage(Evaluation evaluation) {
		if (evaluation != null && mainLayoutController != null) {
			mainLayoutController.showEditEvaluationFormPage(evaluation.getId());
		}
	}
}