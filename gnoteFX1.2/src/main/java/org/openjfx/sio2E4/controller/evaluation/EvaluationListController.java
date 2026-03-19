package org.openjfx.sio2E4.controller.evaluation;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.controller.MainLayoutController;
import org.openjfx.sio2E4.model.*;

import org.openjfx.sio2E4.repository.EvaluationRepository;
import org.openjfx.sio2E4.repository.NoteRepository;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.service.NoteService;
import org.openjfx.sio2E4.util.AlertHelper;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.lang.StringBuilder;

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

	@FXML
	private TableView<MajorEntry> majorsTable;
	@FXML
	private TableColumn<MajorEntry, String> semestreColumn;
	@FXML
	private TableColumn<MajorEntry, String> majorsColumn;

	// Injection du service
	private final EvaluationRepository evaluationRepository = new EvaluationRepository();
	private final NoteRepository noteRepository = new NoteRepository();

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

		moyenneMinColumn.setCellValueFactory(data -> {
			return new SimpleStringProperty(String.valueOf(data.getValue().getNotes().stream()
					.mapToDouble(Note::getValeur)
					.min()
					.orElse(Double.NaN)));
		});

		moyenneMaxColumn.setCellValueFactory(data -> {
			return new SimpleStringProperty(String.valueOf(data.getValue().getNotes().stream()
					.mapToDouble(Note::getValeur)
					.max()
					.orElse(Double.NaN)));
		});

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
			private final Button pushButton = new Button(); // Push to API button
			private final HBox buttonsBox = new HBox(8);

			{
				// Push Button Icon (Upload)
				SVGPath pushIcon = new SVGPath();
				pushIcon.setContent("M8 2a5.53 5.53 0 0 0-3.594 1.342c-.766.66-1.321 1.52-1.464 2.383C1.266 6.095 0 7.555 0 9.318 0 11.366 1.708 13 3.781 13h8.906C14.502 13 16 11.57 16 9.773c0-1.636-1.242-2.969-2.834-3.194C12.923 3.999 10.69 2 8 2zm2.354 5.146a.5.5 0 0 1-.708.708L8.5 6.707V10.5a.5.5 0 0 1-1 0V6.707L6.354 7.854a.5.5 0 1 1-.708-.708l2-2a.5.5 0 0 1 .708 0l2 2z");
				pushIcon.setScaleX(0.8);
				pushIcon.setScaleY(0.8);
				pushIcon.setStyle("-fx-fill: #1E90FF;");
				pushButton.setGraphic(pushIcon);
				pushButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
				pushButton.setTooltip(new Tooltip("Pousser vers l'API"));
				
				pushButton.setOnMouseEntered(e -> pushButton.setStyle("-fx-background-color: #E6F3FF; -fx-cursor: hand; -fx-background-radius: 5px;"));
				pushButton.setOnMouseExited(e -> pushButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;"));

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
					pushButton.setOnAction(event -> handlePushEvaluation(evaluation));

					buttonsBox.getChildren().clear();
					
					// If the evaluation is pending sync, show the push button
					if (evaluation.getId() < 0 && NetworkService.isOnline()) {
						buttonsBox.getChildren().addAll(pushButton, viewButton, editButton, deleteButton);
					} else {
						buttonsBox.getChildren().addAll(viewButton, editButton, deleteButton);
					}

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

		// Mapping des colonnes pour le tableau des majors
		semestreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSemestre()));
		majorsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMajors()));

		// Définir la couleur de la ligne si pending sync (ID négatif)
		evaluationTable.setRowFactory(tv -> new TableRow<Evaluation>() {
			@Override
			protected void updateItem(Evaluation item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setStyle("");
				} else if (item.getId() < 0) {
					// Pending sync, color en jaune pâle
					setStyle("-fx-background-color: #ffffe0; -fx-text-inner-color: #333;");
				} else {
					setStyle("");
				}
			}
		});

		// Chargement des données du tableau
		loadEvaluationsList();
	}

	public void loadEvaluationsList() {
		// Appel au service pour l'utilisateur
		evaluationRepository.getEvaluationsList()
				.thenAccept(user -> {
					if (user != null) {
						// Mise à jour UI Utilisateur
						Platform.runLater(() -> {
							evaluationTable.getItems().setAll(user);
							afficherMajorsParSemestre(user);
						});
					}
				})
				.exceptionally(e -> {
					e.printStackTrace(); // Gérez l'erreur (ex: afficher une alerte)
					return null;
				});
	}

	@FXML
	public void handleDeleteEvaluation(Evaluation evaluation) {
		for (Note note : evaluation.getNotes()) {
			noteRepository.deleteNote(note.getId())
					.exceptionally(e -> {
						Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
						return null;
					});
		}
		// Appel au service pour l'utilisateur
		evaluationRepository.deleteEvaluation(evaluation.getId())
				.thenAccept(success -> {
					Platform.runLater(() -> {
						if (success) {
							AlertHelper.showInformation("Evaluation (et notes associées) supprimé avec succès.");
							loadEvaluationsList();
						} else {
							AlertHelper.showError("Impossible de supprimer cette évaluation.");
						}
					});
				})
				.exceptionally(e -> {
					Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
					return null;
				});
	}

	private void afficherMajorsParSemestre(List<Evaluation> evaluations) {
		// Grouper les évaluations par semestre
		Map<String, List<Evaluation>> evaluationsParSemestre = evaluations.stream()
				.collect(Collectors.groupingBy(this::getSemestre));

		List<MajorEntry> entries = new ArrayList<>();

		for (Map.Entry<String, List<Evaluation>> entry : evaluationsParSemestre.entrySet()) {
			String semestre = entry.getKey();
			List<Evaluation> evals = entry.getValue();

			// Collecter toutes les notes de ce semestre
			List<Note> notesSemestre = evals.stream()
					.flatMap(eval -> eval.getNotes().stream())
					.collect(Collectors.toList());

			if (!notesSemestre.isEmpty()) {
				List<User> majors = calculerMajors(notesSemestre);
				if (!majors.isEmpty()) {
					StringBuilder majorsStr = new StringBuilder();
					for (User major : majors) {
						majorsStr.append(major.getPrenom()).append(" ").append(major.getNom()).append(", ");
					}
					if (majorsStr.length() > 0) {
						majorsStr.setLength(majorsStr.length() - 2); // remove last comma and space
					}
					entries.add(new MajorEntry(semestre, majorsStr.toString()));
				}
			}
		}

		// Mettre à jour le tableau
		Platform.runLater(() -> {
			majorsTable.getItems().setAll(entries);
		});
	}

	private String getSemestre(Evaluation eval) {
		try {
			String dateStr = eval.getDate();
			if (dateStr.contains("T")) {
				dateStr = dateStr.split("T")[0];
			}
			LocalDate date = LocalDate.parse(dateStr);
			int year = date.getYear();
			int month = date.getMonthValue();
			String sem = month <= 6 ? "Semestre 1" : "Semestre 2";
			return sem + "-" + year;
		} catch (DateTimeParseException e) {
			return "Inconnu";
		}
	}

	private List<User> calculerMajors(List<Note> notes) {
		// Map pour stocker les totaux des valeurs pondérées et des coefficients par élève
		Map<Integer, Double> totalNotes = new HashMap<>();
		Map<Integer, Double> totalCoefficients = new HashMap<>();

		// Parcours des notes pour calculer les totaux
		for (Note note : notes) {
			int eleveId = note.getEleve().getId();
			double valeur = note.getValeur();
			double coefficient = note.getEvaluation().getCoefficient();

			// Ajoute la valeur pondérée à l'élève
			totalNotes.put(eleveId, totalNotes.getOrDefault(eleveId, 0.0) + (valeur * coefficient));
			totalCoefficients.put(eleveId, totalCoefficients.getOrDefault(eleveId, 0.0) + coefficient);
		}

		// Calculer la moyenne pondérée pour chaque élève
		Map<Integer, Double> moyennes = new HashMap<>();
		for (int eleveId : totalNotes.keySet()) {
			double moyenne = totalNotes.get(eleveId) / totalCoefficients.get(eleveId);
			moyennes.put(eleveId, moyenne);
		}

		// Trouver la meilleure moyenne
		double meilleureMoyenne = -1;
		for (double moy : moyennes.values()) {
			if (moy > meilleureMoyenne) {
				meilleureMoyenne = moy;
			}
		}

		// Trouver tous les élèves avec cette moyenne
		List<User> majors = new ArrayList<>();
		for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
			if (entry.getValue() == meilleureMoyenne) {
				int eleveId = entry.getKey();
				for (Note note : notes) {
					if (note.getEleve().getId() == eleveId) {
						majors.add(note.getEleve());
						break;
					}
				}
			}
		}
		return majors;
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

	public static class MajorEntry {
		private final String semestre;
		private final String majors;

		public MajorEntry(String semestre, String majors) {
			this.semestre = semestre;
			this.majors = majors;
		}

		public String getSemestre() { return semestre; }
		public String getMajors() { return majors; }
	}

	@FXML
	public void handlePushEvaluation(Evaluation evaluation) {
		if (!NetworkService.isOnline()) {
			AlertHelper.showError("Vous devez être en ligne pour pousser les modifications.");
			return;
		}

		// Push to API
		evaluationRepository.createEvaluation(evaluation)
				.thenAccept(success -> {
					Platform.runLater(() -> {
						if (success) {
							// Push succeed: remove it from sync_data.json
							LocalStorageService.remove(evaluation);
							AlertHelper.showInformation("Évaluation synchronisée avec succès !");
							loadEvaluationsList(); // Reload
						} else {
							AlertHelper.showError("Erreur lors de la synchronisation de l'évaluation.");
						}
					});
				})
				.exceptionally(e -> {
					Platform.runLater(() -> AlertHelper.showError("Erreur technique : " + e.getMessage()));
					return null;
				});
	}
}