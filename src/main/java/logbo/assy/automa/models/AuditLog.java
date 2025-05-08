package logbo.assy.automa.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLog {
    private int idLog;
    private String utilisateur;
    private String action;
    private String entite;
    private String idEntite;
    private LocalDateTime dateAction;
    private String details;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AuditLog() {
    }

    public AuditLog(int idLog, String utilisateur, String action, String entite,
                    String idEntite, LocalDateTime dateAction, String details) {
        this.idLog = idLog;
        this.utilisateur = utilisateur;
        this.action = action;
        this.entite = entite;
        this.idEntite = idEntite;
        this.dateAction = dateAction;
        this.details = details;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public String getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(String utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntite() {
        return entite;
    }

    public void setEntite(String entite) {
        this.entite = entite;
    }

    public String getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(String idEntite) {
        this.idEntite = idEntite;
    }

    public LocalDateTime getDateAction() {
        return dateAction;
    }

    public void setDateAction(LocalDateTime dateAction) {
        this.dateAction = dateAction;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Méthodes additionnelles pour formatter la date et l'heure pour l'affichage
    public String getDate() {
        return dateAction != null ? dateAction.format(DATE_FORMATTER) : "";
    }

    public String getHeure() {
        return dateAction != null ? dateAction.format(TIME_FORMATTER) : "";
    }
}