package logbo.assy.automa.models;

import java.util.UUID;

public class Personnel {
    private String idPersonnel;
    private String nom;
    private String prenom;
    private String contrat;
    private String idFonction;
    private String idService;

    public Personnel(String nom, String prenom, String contrat, String idFonction, String idService) {
        this.idPersonnel = "PERS_" + nom.toUpperCase().trim() + "_" + prenom.toUpperCase().trim() + "_" + UUID.randomUUID().toString().substring(0, 8);
        this.nom = nom;
        this.prenom = prenom;
        this.contrat = contrat;
        this.idFonction = idFonction;
        this.idService = idService;
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
}
