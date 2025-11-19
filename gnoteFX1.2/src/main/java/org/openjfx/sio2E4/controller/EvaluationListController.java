package org.openjfx.sio2E4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import org.openjfx.sio2E4.constants.APIConstants;
import org.openjfx.sio2E4.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.LocalStorageService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.util.AlertHelper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EvaluationListController {

	User currentUser = AuthService.getCurrentUser();
	Role role = currentUser.getRole();

	/* Tableau d'affichage de note */
	@FXML
	private TableView<Note> notesTable;
	@FXML
	private TableColumn<Note, String> eleveColumn;
	@FXML
	private TableColumn<Note, String> enseignantColumn;
	@FXML
	private TableColumn<Note, String> matiereColumn;
	@FXML
	private TableColumn<Note, String> valeurColumn;
	@FXML
	private TableColumn<Note, String> dateColumn;
	@FXML
	private TableColumn<Note, String> commentaireColumn;
	@FXML
	private TableColumn<Note, String> noteTypeColumn;
	@FXML
	private TableColumn<Note, String> coefficientColumn;

	@FXML
	private TableColumn<Note, String> modificationColumn;

	private final String API_URL = "http://localhost:8080/api/notes";
	private final String BEARER_TOKEN = "Bearer " + AuthService.getToken();

	private void clearForm() {
		Platform.runLater(() -> {
			eleveComboBox.setValue(null);
			matiereComboBox.setValue(null);
			valeurField.clear();
			noteTypeComboBox.setValue(null);
			datePicker.setValue(null);
			commentaireField.clear();
			coefficientField.clear();

			// Si l'utilisateur connecté est un enseignant, on le sélectionne à nouveau
			if ("ENSEIGNANT".equalsIgnoreCase(currentUser.getRole().getLibelle())) {
				// On cherche l'enseignant correspondant dans la liste (important si l'objet
				// n'est pas le même en mémoire)
				User enseignant = enseignantComboBox.getItems().stream().filter(u -> u.getId() == currentUser.getId())
						.findFirst().orElse(null);

				if (enseignant != null) {
					enseignantComboBox.setValue(enseignant);
					enseignantComboBox.setDisable(true); // on le rend non modifiable
				}
			} else {
				enseignantComboBox.setValue(null);
				enseignantComboBox.setDisable(false); // autoriser la sélection si ce n’est pas un enseignant
			}
		});
	}

	@FXML
	public void initialize() {

		// Mapping des colonnes
		eleveColumn.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().getEleve().getPrenom() + " " + data.getValue().getEleve().getNom()));

		enseignantColumn.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().getEnseignant().getPrenom() + " " + data.getValue().getEnseignant().getNom()));

		matiereColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMatiere().getLibelle()));

		valeurColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getValeur())));

		dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));

		commentaireColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommentaire()));

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

		/*----FORMATTAGE DES COMBOBOX----*/

		noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
			@Override
			protected void updateItem(NoteType item, boolean empty) {
				super.updateItem(item, empty);
				// Affiche le libellé ou "vide" si l'élément est null ou la cellule vide
				setText(empty || item == null ? null : item.getLibelle());
			}
		});
		noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null)); // Rendu du bouton du ComboBox

		eleveComboBox.setCellFactory(lv -> new ListCell<User>() {
			@Override
			protected void updateItem(User item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
			}
		});
		eleveComboBox.setButtonCell(eleveComboBox.getCellFactory().call(null)); // Rendu du bouton du ComboBox

		enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
			@Override
			protected void updateItem(User item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
			}
		});
		enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null)); // Rendu du bouton du ComboBox

		// Chargement des données du tableaus
		fetchNotes();

		// Chargement des données du formulaire
		fetchUsers();
		fetchMatieres();
		fetchNoteTypes();
	}

	private void fetchNotes() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.NOTES))
					.header("Authorization", BEARER_TOKEN)
					.GET().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
					.thenAccept(this::parseNotes).exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			System.out.println("Mode hors ligne activé — chargement des notes en local");
			ArrayList<Note> localNotes = LocalStorageService.loadNotes();
			Platform.runLater(() -> notesTable.getItems().setAll(localNotes));
		}

	}

	private void parseNotes(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {

			List<Note> notes = Arrays.asList(mapper.readValue(responseBody, Note[].class));
			Platform.runLater(() -> notesTable.getItems().setAll(notes));

			eleveAvecMeilleureMoyenne(notes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private Label meilleurEleveLabel; // Le Label qui affichera l'élève avec la meilleure moyenne

	private void eleveAvecMeilleureMoyenne(List<Note> notes) {
	    // Map pour stocker les totaux des valeurs pondérées et des coefficients par élève
	    Map<Integer, Double> totalNotes = new HashMap<>();
	    Map<Integer, Double> totalCoefficients = new HashMap<>();

	    // Parcours des notes pour calculer les totaux
	    for (Note note : notes) {
	        int eleveId = note.getEleve().getId();
	        double valeur = note.getValeur();
	        double coefficient = note.getCoefficient();

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

	    // Trouver l'élève avec la meilleure moyenne
	    int meilleurEleveId = -1;
	    double meilleureMoyenne = -1;
	    for (Map.Entry<Integer, Double> entry : moyennes.entrySet()) {
	        if (entry.getValue() > meilleureMoyenne) {
	            meilleureMoyenne = entry.getValue();
	            meilleurEleveId = entry.getKey();
	        }
	    }

	    // Trouver l'élève correspondant à l'ID avec la meilleure moyenne
	    User meilleurEleve = null;
	    for (Note note : notes) {
	        if (note.getEleve().getId() == meilleurEleveId) {
	            meilleurEleve = note.getEleve();
	            break;
	        }
	    }

	    // Trouver la date la plus récente parmi les notes
	    Instant dateRecenteInstant = null;
	    DateTimeFormatter formatterAffichage = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	    for (Note note : notes) {
	        String dateStr = note.getDate();

	        try {
	            Instant instantNote;

	            if (dateStr.contains("T")) {
	                // Cas avec date + heure + offset, ex: "2025-05-23T12:26:22.884+00:00"
	                instantNote = OffsetDateTime.parse(dateStr).toInstant();
	            } else {
	                // Cas avec date seule, ex: "2024-03-17"
	                LocalDate ld = LocalDate.parse(dateStr);
	                instantNote = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
	            }

	            if (dateRecenteInstant == null || instantNote.isAfter(dateRecenteInstant)) {
	                dateRecenteInstant = instantNote;
	            }
	        } catch (DateTimeParseException e) {
	            System.err.println("Erreur de parsing date : " + dateStr);
	        }
	    }

	    // Mettre à jour le Label avec l'élève ayant la meilleure moyenne et la date la plus récente
	    if (meilleurEleve != null) {
	        String texte = "Major de promotion : " + meilleurEleve.getPrenom() + " " + meilleurEleve.getNom()
	                + " avec une moyenne de " + String.format("%.2f", meilleureMoyenne);

	        if (dateRecenteInstant != null) {
	            ZonedDateTime dateRecenteLocal = dateRecenteInstant.atZone(ZoneId.systemDefault());
	            texte += " | Date la plus récente : " + dateRecenteLocal.format(formatterAffichage);
	        }

	        final String texteFinal = texte; // variable finale pour la lambda
	        Platform.runLater(() -> meilleurEleveLabel.setText(texteFinal));
	    }
	}


	/* Formulaire de saisie de note */
	@FXML
	private TextField valeurField;
	@FXML
	private TextField coefficientField;
	@FXML
	private TextArea commentaireField;

	@FXML
	private ComboBox<User> eleveComboBox;
	@FXML
	private ComboBox<User> enseignantComboBox;
	@FXML
	private ComboBox<Matiere> matiereComboBox;
	@FXML
	private ComboBox<NoteType> noteTypeComboBox;

	@FXML
	private DatePicker datePicker;

	@FXML
	private Button ajouterNoteButton;

	@FXML
	private void ajouterNote() {
		// Récupérer les données du formulaire
		User eleve = eleveComboBox.getValue();
		User enseignant = enseignantComboBox.getValue();
		Matiere matiere = matiereComboBox.getValue();
		NoteType noteType = noteTypeComboBox.getValue();

		double valeur = Double.parseDouble(valeurField.getText());
		double coefficient = Double.parseDouble(coefficientField.getText());
		String date = datePicker.getValue().toString();
		String commentaire = commentaireField.getText();
		if (NetworkService.isOnline()) {
			try {
				String json = String.format(
						"{" + "\"eleve\": { \"id\": %d }," + "\"enseignant\": { \"id\": %d },"
								+ "\"matiere\": { \"id\": %d }," + "\"coefficient\": %s," + "\"valeur\": %s,"
								+ "\"noteType\": { \"id\": %d }," + "\"commentaire\": \"%s\"," + "\"date\": \"%s\"" + "}",
						eleve.getId(), enseignant.getId(), matiere.getId(), coefficient, valeur, noteType.getId(),
						commentaire, date);

				// Préparer et envoyer la requête POST
				HttpClient client = HttpClient.newHttpClient();
				HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.NOTES))
						.header("Authorization", BEARER_TOKEN).header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(json)).build();

				client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
					if (response.statusCode() == 201 || response.statusCode() == 200) {
						// Succès : rafraîchir la liste
						fetchNotes();
						clearForm();
					} else {
						System.err.println("Erreur à l'ajout : " + response.body());
					}
				}).exceptionally(e -> {
					e.printStackTrace();
					return null;
				});

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Note newNote = new Note();
			newNote.setEleve(eleve);
			newNote.setEnseignant(enseignant);
			newNote.setMatiere(matiere);
			newNote.setNoteType(noteType);

			newNote.setValeur(valeur);
			newNote.setCoefficient(coefficient);
			newNote.setDate(date);
			newNote.setCommentaire(commentaire);

			LocalStorageService.save(newNote);

			Platform.runLater(() -> {
				AlertHelper.showInformation("Note ajouté en local (mode hors ligne).");
				fetchNotes();
				clearForm();
			});
		}
	}

	@FXML
	private void handleDeleteNote() {
		Note selectedNote = notesTable.getSelectionModel().getSelectedItem();

		if (selectedNote == null) {
			AlertHelper.showWarning("Veuillez sélectionner une note à supprimer.");
			return;
		}

		deleteNote(selectedNote.getId());
	}

	private void deleteNote(int noteId) {
		if(NetworkService.isOnline()) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, noteId)))
                        .header("Authorization", "Bearer " + AuthService.getToken()).DELETE().build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                    if (response.statusCode() == 204) {
                        Platform.runLater(() -> {
                            AlertHelper.showInformation("Note supprimée avec succès.");
                            fetchNotes(); // Méthode pour recharger la liste
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
			ArrayList<Note> notes = LocalStorageService.loadNotes();
			Optional<Note> note  = notes.stream()
					.filter(u -> u.getId()==noteId)
					.findFirst();
			if (note.isPresent()) {
				LocalStorageService.remove(note.get());

				Platform.runLater(() -> {
                    AlertHelper.showInformation("Note supprimé en local (mode hors ligne).");
					fetchNotes();
				});
			}
		}
	}

	private void fetchUsers() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.USERS))
					.header("Authorization", BEARER_TOKEN).GET().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
					.thenAccept(this::parseUsers).exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			parseUsers("");
		}
	}

	private void parseUsers(String responseBody) {
		User user = AuthService.getCurrentUser();
		ObjectMapper mapper = new ObjectMapper();
		if (NetworkService.isOnline()) {
			try {
				List<User> users = Arrays.asList(mapper.readValue(responseBody, User[].class));

				// Filtrer les utilisateurs par rôle
				List<User> eleves = users.stream().filter(u -> "ETUDIANT".equalsIgnoreCase(u.getRole().getLibelle()))
						.collect(Collectors.toList());

				List<User> enseignants = users.stream().filter(u -> "ENSEIGNANT".equalsIgnoreCase(u.getRole().getLibelle()))
						.collect(Collectors.toList());

				// Mettre à jour les ComboBox dans le thread JavaFX
				Platform.runLater(() -> {
					// Mettre les utilisateurs dans les ComboBox
					eleveComboBox.getItems().setAll(eleves);
					enseignantComboBox.getItems().setAll(enseignants);

					// Personnaliser l'affichage des ComboBox pour afficher le nom complet
					enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
						@Override
						protected void updateItem(User item, boolean empty) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
						}
					});

					eleveComboBox.setCellFactory(lv -> new ListCell<User>() {
						@Override
						protected void updateItem(User item, boolean empty) {
							super.updateItem(item, empty);
							setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
						}
					});

					// Rendre l'affichage correct pour le bouton du ComboBox (afficher le nom
					// complet)
					enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));
					eleveComboBox.setButtonCell(eleveComboBox.getCellFactory().call(null));

					// Si l'utilisateur est un enseignant connecté, sélectionner son nom dans le
					// ComboBox
					if ("ENSEIGNANT".equalsIgnoreCase(user.getRole().getLibelle())) {
						// Trouver l'objet User correspondant à l'enseignant
						User enseignant = enseignants.stream().filter(
										u -> (u.getPrenom() + " " + u.getNom()).equals(user.getPrenom() + " " + user.getNom()))
								.findFirst().orElse(null);

						if (enseignant != null) {
							enseignantComboBox.setValue(enseignant);
							enseignantComboBox.setDisable(true);
						}
					}
				});

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Mode hors ligne activé — chargement des utilisateur en local");
			ArrayList<User> localUsers = LocalStorageService.loadUsers();

			// Filtrer les utilisateurs par rôle
			List<User> eleves = localUsers.stream().filter(u -> "ETUDIANT".equalsIgnoreCase(u.getRole().getLibelle()))
					.collect(Collectors.toList());

			List<User> enseignants = localUsers.stream().filter(u -> "ENSEIGNANT".equalsIgnoreCase(u.getRole().getLibelle()))
					.collect(Collectors.toList());

			// Mettre à jour les ComboBox dans le thread JavaFX
			Platform.runLater(() -> {
				// Mettre les utilisateurs dans les ComboBox
				eleveComboBox.getItems().setAll(eleves);
				enseignantComboBox.getItems().setAll(enseignants);

				// Personnaliser l'affichage des ComboBox pour afficher le nom complet
				/*enseignantComboBox.setCellFactory(lv -> new ListCell<User>() {
					@Override
					protected void updateItem(User item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
					}
				});

				eleveComboBox.setCellFactory(lv -> new ListCell<User>() {
					@Override
					protected void updateItem(User item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item.getPrenom() + " " + item.getNom());
					}
				});*/

				// Rendre l'affichage correct pour le bouton du ComboBox (afficher le nom
				// complet)
				enseignantComboBox.setButtonCell(enseignantComboBox.getCellFactory().call(null));
				eleveComboBox.setButtonCell(eleveComboBox.getCellFactory().call(null));

				// Si l'utilisateur est un enseignant connecté, sélectionner son nom dans le
				// ComboBox
				if ("ENSEIGNANT".equalsIgnoreCase(user.getRole().getLibelle())) {
					// Trouver l'objet User correspondant à l'enseignant
					User enseignant = enseignants.stream().filter(
									u -> (u.getPrenom() + " " + u.getNom()).equals(user.getPrenom() + " " + user.getNom()))
							.findFirst().orElse(null);

					if (enseignant != null) {
						enseignantComboBox.setValue(enseignant);
						enseignantComboBox.setDisable(true);
					}
				}
			});
		}
	}

	private void fetchMatieres() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.MATIERES))
					.header("Authorization", BEARER_TOKEN).GET().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
					.thenAccept(this::parseMatieres).exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			System.out.println("Mode hors ligne activé — chargement des matières local");
			ArrayList<Matiere> localMatieres = LocalStorageService.loadMatieres();
			Platform.runLater(() -> matiereComboBox.getItems().setAll(localMatieres));
		}
	}

	private void parseMatieres(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<Matiere> matieres = Arrays.asList(mapper.readValue(responseBody, Matiere[].class));

			Platform.runLater(() -> {
				// Ajouter les objets Matiere directement au ComboBox
				matiereComboBox.getItems().setAll(matieres);

				// Afficher uniquement le libellé dans la liste déroulante
				matiereComboBox.setCellFactory(lv -> new ListCell<Matiere>() {
					@Override
					protected void updateItem(Matiere item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item.getLibelle());
					}
				});

				// Rendu du bouton du ComboBox
				matiereComboBox.setButtonCell(matiereComboBox.getCellFactory().call(null));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void fetchNoteTypes() {
		if (NetworkService.isOnline()) {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.NOTE_TYPES))
					.header("Authorization", BEARER_TOKEN).GET().build();

			client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
					.thenAccept(this::parseNoteTypes).exceptionally(e -> {
						e.printStackTrace();
						return null;
					});
		} else {
			System.out.println("Mode hors ligne activé — chargement des types de notes en local");
			ArrayList<NoteType> localNoteType = LocalStorageService.loadNoteTypes();
			Platform.runLater(() -> noteTypeComboBox.getItems().setAll(localNoteType));
		}
	}

	private void parseNoteTypes(String responseBody) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<NoteType> types = Arrays.asList(mapper.readValue(responseBody, NoteType[].class));

			Platform.runLater(() -> {
				// Ajouter les objets NoteType directement au ComboBox
				noteTypeComboBox.getItems().setAll(types);

				// Afficher uniquement le libellé dans la liste déroulante
				noteTypeComboBox.setCellFactory(lv -> new ListCell<NoteType>() {
					@Override
					protected void updateItem(NoteType item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? null : item.getLibelle());
					}
				});

				// Rendu du bouton du ComboBox
				noteTypeComboBox.setButtonCell(noteTypeComboBox.getCellFactory().call(null));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void handleUpdateNote() {
		Note selectedNote = notesTable.getSelectionModel().getSelectedItem();

		if (selectedNote == null) {
			AlertHelper.showWarning("Veuillez sélectionner une note à modifier.");
			return;
		}

		User currentUser = AuthService.getCurrentUser();
		String userRole = currentUser.getRole().getLibelle();

		// Empêcher un enseignant de modifier une note qui ne lui appartient pas
		if ("ENSEIGNANT".equalsIgnoreCase(userRole) && selectedNote.getEnseignant().getId() != currentUser.getId()) {
			AlertHelper.showError("Vous ne pouvez modifier que vos propres notes.");
			return;
		}

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("Modifier une note");

		DialogPane dialogPane = new DialogPane();
		dialog.setDialogPane(dialogPane);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

		// Création de nouveaux champs indépendants
		ComboBox<User> eleveBox = new ComboBox<>(eleveComboBox.getItems());
		ComboBox<User> enseignantBox = new ComboBox<>(enseignantComboBox.getItems());
		ComboBox<Matiere> matiereBox = new ComboBox<>(matiereComboBox.getItems());
		ComboBox<NoteType> noteTypeBox = new ComboBox<>(noteTypeComboBox.getItems());

		TextField valeurFieldLocal = new TextField();
		TextField coefficientFieldLocal = new TextField();
		TextArea commentaireFieldLocal = new TextArea();
		DatePicker datePickerLocal = new DatePicker();

		// Préremplissage
		eleveBox.setValue(selectedNote.getEleve());
		enseignantBox.setValue(selectedNote.getEnseignant());
		matiereBox.setValue(selectedNote.getMatiere());
		valeurFieldLocal.setText(String.valueOf(selectedNote.getValeur()));
		coefficientFieldLocal.setText(String.valueOf(selectedNote.getCoefficient()));
		datePickerLocal.setValue(LocalDate.parse(selectedNote.getDate()));
		commentaireFieldLocal.setText(selectedNote.getCommentaire());

		// Sélectionner le type de note
		for (NoteType nt : noteTypeBox.getItems()) {
			if (nt.getLibelle().equalsIgnoreCase(selectedNote.getNoteType().getLibelle())) {
				noteTypeBox.setValue(nt);
				break;
			}
		}

		// 🔒 Désactiver le champ enseignant si c'est un enseignant connecté
		if ("ENSEIGNANT".equalsIgnoreCase(userRole)) {
			enseignantBox.setDisable(true);
		}

		VBox form = new VBox(10, eleveBox, enseignantBox, matiereBox, valeurFieldLocal, coefficientFieldLocal,
				datePickerLocal, noteTypeBox, commentaireFieldLocal);
		dialogPane.setContent(form);

		dialog.showAndWait().ifPresent(response -> {
			if (response == ButtonType.OK) {
				if(NetworkService.isOnline()) {
					try {
						String json = String.format("{" + "\"eleve\": { \"id\": %d }," + "\"enseignant\": { \"id\": %d },"
										+ "\"matiere\": { \"id\": %d }," + "\"coefficient\": %s," + "\"valeur\": %s,"
										+ "\"noteType\": { \"id\": %d }," + "\"commentaire\": \"%s\"," + "\"date\": \"%s\"" + "}",
								eleveBox.getValue().getId(), enseignantBox.getValue().getId(),
								matiereBox.getValue().getId(), Double.parseDouble(coefficientFieldLocal.getText()),
								Double.parseDouble(valeurFieldLocal.getText()), noteTypeBox.getValue().getId(),
									commentaireFieldLocal.getText(), datePickerLocal.getValue().toString());

						HttpClient client = HttpClient.newHttpClient();
						HttpRequest request = HttpRequest.newBuilder().uri(URI.create(APIConstants.formatUrl(APIConstants.NOTE_BY_ID, selectedNote.getId())))
								.header("Authorization", BEARER_TOKEN).header("Content-Type", "application/json")
								.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

						client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(resp -> {
							if (resp.statusCode() == 200) {
								fetchNotes();
								Platform.runLater(
										() -> AlertHelper.showInformation("Note mise à jour avec succès."));
							} else {
								Platform.runLater(
										() -> AlertHelper.showError("Erreur de mise à jour : " + resp.body()));
							}
						}).exceptionally(e -> {
							e.printStackTrace();
							Platform.runLater(() -> AlertHelper.showError("Erreur réseau : " + e.getMessage()));
							return null;
						});

					} catch (Exception e) {
						AlertHelper.showError("Erreur dans le formulaire : " + e.getMessage());
						e.printStackTrace();
					}
				} else {
					ArrayList<Note> notes = LocalStorageService.loadNotes();
					Optional<Note> noteOpt  = notes.stream()
							.filter(n -> n.getId()==selectedNote.getId())
							.findFirst();
					if (noteOpt.isPresent()) {
						Note note = noteOpt.get();

						note.setEleve(eleveBox.getValue());
						note.setEnseignant(enseignantBox.getValue());
						note.setMatiere(matiereBox.getValue());
						note.setNoteType(noteTypeBox.getValue());
						note.setCoefficient(Double.parseDouble(coefficientFieldLocal.getText()));
						note.setValeur(Double.parseDouble(valeurFieldLocal.getText()));
						note.setCommentaire(commentaireFieldLocal.getText());
						note.setDate(datePickerLocal.getValue().toString());

						LocalStorageService.update(note);

						Platform.runLater(() -> {
							AlertHelper.showInformation("Note mis a jour en local (mode hors ligne).");
							fetchMatieres(); // Rafraîchit la liste des utilisateurs
						});
					}
				}
				fetchNotes();
			}
		});
	}

	private MainLayoutController mainLayoutController;

	public void setMainLayoutController(MainLayoutController controller) {
		this.mainLayoutController = controller;
	}
	@FXML
	private void showCreateEvaluationPage() {
        mainLayoutController.showCreateEvaluationPage();
	}
}
