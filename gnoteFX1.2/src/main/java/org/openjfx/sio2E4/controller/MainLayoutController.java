package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainLayoutController {

    @FXML
    private Label usernameLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        User user = AuthService.getCurrentUser();
        if (user != null) {
            Role role = user.getRole();
            String nom = user.getNom().toUpperCase();
            String prenom = capitalize(user.getPrenom());

            usernameLabel.setText(role.getLibelle() + " - " + nom + " " + prenom);
        } else {
            usernameLabel.setText("Bienvenue invité");
        }
        javafx.application.Platform.runLater(() -> {
            Scene scene = usernameLabel.getScene();
            if (scene != null) {
                scene.getStylesheets()
                        .add(getClass().getResource("/org/openjfx/sio2E4/css/AppLayout.css").toExternalForm());
            }
        });

        // Charge la vue par défaut (dashboard)
        showDashboard();
        updateMenuState();
    }

    @FXML
    private void handleLogout() {
        // Réinitialise les infos d'auth (si tu utilises un service d'authentification)
        AuthService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/loginPage.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene loginScene = new Scene(loginRoot);

            stage.setScene(loginScene);
            stage.setTitle("Gnotes");

            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();

            stage.setWidth(600);
            stage.setHeight(400);
            stage.setMinWidth(600);
            stage.setMinHeight(400);

            // Gestionnaire d'événements pour empêcher la réduction de la taille
            stage.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, event -> {
                // Réajuster les dimensions si elles sont trop petites
                if (stage.getWidth() < 800 || stage.getHeight() < 600) {
                    stage.setWidth(600);
                    stage.setHeight(400);
                }
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Gérer les exceptions
        }
    }

    @FXML
    public void showDashboard() {
        //System.out.println("MainLayoutController: loading dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/openjfx/sio2E4/view/HomeView.fxml"));

            Node homeView = loader.load();
            if (homeView == null) {
                System.err.println("MainLayoutController: loader returned null for HomeView.fxml");
                contentArea.getChildren().setAll(new Label("Impossible de charger la page d'accueil."));
                return;
            }

            contentArea.getChildren().setAll(homeView); // Remplace tout le contenu du StackPane
            //System.out.println("MainLayoutController: dashboard loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            // show a friendly error node instead of leaving blank
            contentArea.getChildren().setAll(new Label("Erreur lors du chargement de l'accueil : " + e.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            contentArea.getChildren().setAll(new Label("Erreur inattendue lors du chargement de l'accueil."));
        }
    }

    @FXML
    private void showMatieres() {
        loadView("/org/openjfx/sio2E4/view/MatieresView.fxml");
    }

    @FXML
    public void showEvaluationList() {
        loadViewWithController("/org/openjfx/sio2E4/view/EvaluationListView.fxml",
                (EvaluationListController c) -> c.setMainLayoutController(this));
    }

    //----------------------- Evaluation Form -----------------------
    public void showCreateEvaluationFormPage() {
        loadViewWithController("/org/openjfx/sio2E4/view/EvaluationFormView.fxml",
                (EvaluationFormController c) -> c.setMainLayoutController(this));
    }

    public void showViewEvaluationFormPage(int evaluationId) {
        loadViewWithController("/org/openjfx/sio2E4/view/EvaluationFormView.fxml",
                (EvaluationFormController c) -> {
                    c.setMainLayoutController(this);
                    c.loadViewEvaluation(evaluationId);
                });
    }

    public void showEditEvaluationFormPage(int evaluationId) {
        loadViewWithController("/org/openjfx/sio2E4/view/EvaluationFormView.fxml",
                (EvaluationFormController c) -> {
                    c.setMainLayoutController(this);
                    c.loadEditEvaluation(evaluationId);
                });
    }


    //----------------------- Users -----------------------

    @FXML
    private void showUsers() {
        loadViewWithController("/org/openjfx/sio2E4/view/UserView.fxml",
                (UsersController c) -> {
                    c.setMainLayoutController(this);
                });
    }

    public void showUserCard(int userId) {
        // Determine the role of the user and load the appropriate view (Student/Teacher)
        org.openjfx.sio2E4.repository.UserRepository repo = new org.openjfx.sio2E4.repository.UserRepository();
        repo.getUser(userId).thenAccept(user -> {
            Platform.runLater(() -> {
                if (user != null && user.getRole() != null && "ENSEIGNANT".equalsIgnoreCase(user.getRole().getLibelle())) {
                    loadViewWithController("/org/openjfx/sio2E4/view/TeacherCardView.fxml",
                            (TeacherCardController c) -> c.loadUser(userId));
                } else {
                    loadViewWithController("/org/openjfx/sio2E4/view/UserCardView.fxml",
                            (UserCardController c) -> c.loadUser(userId));
                }
            });
        }).exceptionally(e -> {
            // fallback to default user card on error
            loadViewWithController("/org/openjfx/sio2E4/view/UserCardView.fxml",
                    (UserCardController c) -> c.loadUser(userId));
            return null;
        });
    }


    //----------------------- Etudiants -----------------------

	@FXML
	private void showEtudiants() {
		loadViewWithController("/org/openjfx/sio2E4/view/EtudiantsView.fxml",
				(EtudiantsController c) -> {
					c.setMainLayoutController(this);
				});
	}

    public void showEtudiantCard(int etudiantId) {
		loadViewWithController("/org/openjfx/sio2E4/view/EtudiantCardView.fxml",
				(EtudiantCardController c) -> {
					c.loadUser(etudiantId);
				});
    }


	//----------------------- Loaders -----------------------

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void loadViewWithController(String fxmlPath, java.util.function.Consumer<T> controllerConsumer) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                if (controllerConsumer != null) {
                    T controller = loader.getController();
                    controllerConsumer.accept(controller);
                }

                contentArea.getChildren().setAll(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    // Fonction pour faire du FULL MAJ
    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    // Syle CSS réactif
    @FXML
    private Button logoutButton;

    @FXML
    private void onLogoutHover() {
        if (logoutButton != null) {
            logoutButton.setStyle(StyleConstants.LOGOUT_BUTTON_HOVER);
        }
    }

    @FXML
    private void onLogoutHoverExit() {
        logoutButton.setStyle(StyleConstants.LOGOUT_BUTTON_HOVER_EXIT);
    }



    @FXML private MenuButton statusMenuButton;

    // Références aux items du FXML
    @FXML private MenuItem menuGoOffline;
    @FXML private SeparatorMenuItem sepInfo;
    @FXML private Circle statusIndicator;
    @FXML private MenuItem headerPending;
    @FXML private MenuItem infoEvaluations;
    @FXML private MenuItem infoNotes;
    @FXML private SeparatorMenuItem sepActions;
    @FXML private MenuItem actionSyncOnline;
    @FXML private MenuItem actionIgnore;
    @FXML private MenuItem infoNothing;
    @FXML private MenuItem menuGoOnline;

    // Simulation de données locales
    private int pendingEvals = 0;
    private int pendingNotes = 0;

    private enum AppState {
        ONLINE,
        OFFLINE_NO_DATA,
        OFFLINE_PENDING
    }

    private AppState currentState = AppState.ONLINE;

    /**
     * Reconstruit le menu dynamiquement selon l'état
     */
    private void updateMenuState() {
        // Vider le menu actuel
        ObservableList<MenuItem> currentItems = FXCollections.observableArrayList();

        // Nettoyage du style du bouton principal
        statusIndicator.getStyleClass().removeAll("circle-online", "circle-offline", "circle-sync");

        switch (currentState) {
            case ONLINE:
                statusMenuButton.setText("En ligne");
                statusIndicator.getStyleClass().add("circle-online");

                // Menu : Juste "Passer hors ligne"
                currentItems.add(menuGoOffline);
                break;

            case OFFLINE_NO_DATA:
                statusMenuButton.setText("Hors ligne");
                statusIndicator.getStyleClass().add("circle-offline");

                // Menu : Info "Rien" + "Passer en ligne"
                currentItems.add(infoNothing);
                currentItems.add(menuGoOnline);
                break;

            case OFFLINE_PENDING:
                statusMenuButton.setText("Hors ligne – Données à sync");
                statusIndicator.getStyleClass().add("circle-sync");

                // Mise à jour des textes des compteurs
                infoEvaluations.setText("• Evaluations modifiées (" + pendingEvals + ")");
                infoNotes.setText("• Notes ajoutées (" + pendingNotes + ")");

                // Construction du menu complexe
                currentItems.add(headerPending);
                if(pendingEvals > 0) currentItems.add(infoEvaluations);
                if(pendingNotes > 0) currentItems.add(infoNotes);

                currentItems.add(sepActions);

                //currentItems.add(actionSyncStay);
                currentItems.add(actionSyncOnline);
                currentItems.add(actionIgnore);
                break;
        }

        // Appliquer la nouvelle liste d'items au bouton
        statusMenuButton.getItems().setAll(currentItems);
    }

    // --- ACTIONS ---

    @FXML
    private void setModeOffline() {
        // Simulation : on vérifie s'il y a des données locales
        // Pour le test, disons qu'on a des données
        this.pendingEvals = 3;
        this.pendingNotes = 12;

        if (pendingEvals > 0 || pendingNotes > 0) {
            currentState = AppState.OFFLINE_PENDING;
        } else {
            currentState = AppState.OFFLINE_NO_DATA;
        }
        updateMenuState();
    }

    @FXML
    private void setModeOnline() {
        System.out.println("Passage en ligne...");
        currentState = AppState.ONLINE;
        updateMenuState();
    }

    @FXML
    private void syncAndGoOnline() {
        System.out.println("Sync et connexion...");
        pendingEvals = 0;
        pendingNotes = 0;
        currentState = AppState.ONLINE;
        updateMenuState();
    }

    @FXML
    private void stayOffline() {
        // Juste fermer le menu (automatique)
        System.out.println("Action ignorée pour le moment");
    }
}
