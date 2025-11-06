package org.openjfx.sio2E4.constants;

public class StyleConstants {
    public static final String COEFFICIENT_STYLE = "-fx-font-size: 10; -fx-translate-y: 4;";
    public static final String NOTE_CONTAINER_STYLE = "-fx-padding: 0 4 0 0;";
    public static final String ERROR_LABEL_STYLE = "-fx-text-fill: red;";

    public static final String LOGOUT_BUTTON_HOVER = "-fx-background-color: #aa2e4a; -fx-text-fill: white;";
    public static final String LOGOUT_BUTTON_HOVER_EXIT = "-fx-background-color: #f4f4f4; -fx-text-fill: #82263c;";


    public static class ButtonActionsColumn {

        public static final String ACTION_TABLE_BUTTON_STYLE = "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 8 12; " +
                "-fx-cursor: hand; " +
                "-fx-min-width: 40; " +
                "-fx-min-height: 36;";

        // VIEW
        public static final String VIEW_BUTTON = "-fx-background-color: #d1fae5; " +
                "-fx-border-color: #a7f3d0; " +
                ACTION_TABLE_BUTTON_STYLE;

        public static final String VIEW_BUTTON_ICON = "-fx-fill: #059669;";

        public static final String VIEW_BUTTON_HOVER = "-fx-background-color: #059669; " +
                "-fx-border-color: #059669; " +
                ACTION_TABLE_BUTTON_STYLE;

        public static final String VIEW_BUTTON_ICON_HOVER = "-fx-fill: white;";

        // EDIT
        public static final String EDIT_BUTTON = "-fx-background-color: white; " +
                "-fx-border-color: #e5e7eb; " +
                ACTION_TABLE_BUTTON_STYLE;

        public static final String EDIT_BUTTON_ICON = "-fx-fill: #3b82f6;";

        public static final String EDIT_BUTTON_HOVER = "-fx-background-color: #eff6ff; " +
                "-fx-border-color: #3b82f6; " +
                ACTION_TABLE_BUTTON_STYLE;

        // DELETE
        public static final String DELETE_BUTTON = "-fx-background-color: #fee2e2; " +
                "-fx-border-color: #fecaca; " +
                ACTION_TABLE_BUTTON_STYLE;

        public static final String DELETE_BUTTON_ICON = "-fx-fill: #dc2626;";

        public static final String DELETE_BUTTON_HOVER = "-fx-background-color: #dc2626; " +
                "-fx-border-color: #dc2626; " +
                ACTION_TABLE_BUTTON_STYLE;

        public static final String DELETE_BUTTON_ICON_HOVER = "-fx-fill: white;";
    }
}
