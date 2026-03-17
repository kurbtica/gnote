package org.openjfx.sio2E4.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingAction {
    private String entityType; // Ex: "Evaluation", "Etudiant"
    private String actionType; // "CREATE", "UPDATE", "DELETE"
    private JsonNode payload;  // L'objet complet en JSON
    private int localId;       // L'ID utilisé localement (utile pour la suppression)

    // Constructeurs, Getters, Setters
    public PendingAction() {

    }

    public PendingAction(String entityType, String actionType, JsonNode payload, int localId) {
        this.entityType = entityType;
        this.actionType = actionType;
        this.payload = payload;
        this.localId = localId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }
}
