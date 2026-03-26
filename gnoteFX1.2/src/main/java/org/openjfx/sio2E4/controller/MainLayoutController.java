package org.openjfx.sio2E4.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.openjfx.sio2E4.constants.StyleConstants;
import org.openjfx.sio2E4.model.Role;
import org.openjfx.sio2E4.model.User;
import org.openjfx.sio2E4.service.AuthService;
import org.openjfx.sio2E4.service.NetworkService;
import org.openjfx.sio2E4.controller.user.*;
import org.openjfx.sio2E4.controller.user.cards.*;
import org.openjfx.sio2E4.controller.evaluation.*;
import org.openjfx.sio2E4.controller.auth.*;

import java.io.IOException;

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
                            (TeacherCardController c) -> {
                                c.setMainLayoutController(this);
                                c.loadUser(userId);
                            });
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

            // Actualisation systématique après chargement
            updateMenuState();
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


    @FXML private HBox statusBadge;       // Le conteneur (pour changer le fond)
    @FXML private Circle statusIndicator; // Le petit point
    @FXML private Label statusLabel;      // Le texte (En ligne / Hors ligne)

    private enum AppState {
        ONLINE,
        OFFLINE
    }

    /**
     * Reconstruit le menu dynamiquement selon l'état
     */
    private void updateMenuState() {
        if (statusBadge == null || statusIndicator == null || statusLabel == null) {
            return;
        }

        statusBadge.getStyleClass().removeAll("status-online", "status-offline");
        statusIndicator.getStyleClass().removeAll("circle-online", "circle-offline");

        AppState currentState = (NetworkService.isOnline() ? AppState.ONLINE : AppState.OFFLINE);

        switch (currentState) {
            case ONLINE:
                statusLabel.setText("En ligne");
                statusBadge.getStyleClass().add("status-online");
                statusIndicator.getStyleClass().add("circle-online");
                break;

            case OFFLINE:
                statusLabel.setText("Hors ligne");
                statusBadge.getStyleClass().add("status-offline");
                statusIndicator.getStyleClass().add("circle-offline");
                break;
        }
    }
}
