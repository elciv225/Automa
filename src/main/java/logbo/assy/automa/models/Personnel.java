package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.UUID;

public class Personnel {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idPersonnel;
    private String nom;
    private String prenom;
    private String contrat;
    private String idFonction;
    private String idService;
    private String email;
    private String telephone;
    private String login;
    private String motDePasse;

    public Personnel(String nom, String prenom, String contrat, String idFonction, String idService) {
        this.idPersonnel = "PERS_" + nom.toUpperCase().trim() + "_" + prenom.toUpperCase().trim() + "_" + UUID.randomUUID().toString().substring(0, 8);
        this.nom = nom;
        this.prenom = prenom;
        this.contrat = contrat;
        this.idFonction = idFonction;
        this.idService = idService;
        // Génération automatique du login (première lettre du prénom + nom en minuscules)
        this.login = (prenom.charAt(0) + nom).toLowerCase().replaceAll("\\s+", "");
        // Mot de passe par défaut (à changer par l'utilisateur)
        this.motDePasse = "password123";
    }

    public Personnel() {
    }

    public String getIdPersonnel() {
        return idPersonnel;
    }

    public void setIdPersonnel(String idPersonnel) {
        this.idPersonnel = idPersonnel;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getContrat() {
        return contrat;
    }

    public void setContrat(String contrat) {
        this.contrat = contrat;
    }

    public String getIdFonction() {
        return idFonction;
    }

    public void setIdFonction(String idFonction) {
        this.idFonction = idFonction;
    }

    public String getIdService() {
        return idService;
    }

    public void setIdService(String idService) {
        this.idService = idService;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    @Override
    public String toString() {
        return nom.toUpperCase() + " " + prenom;
    }

}
