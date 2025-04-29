package logbo.assy.automa.models;

public class Fonction {
    private String idFonction;
    private String libelle;

    public Fonction(String libelle) {
        this.idFonction = "FUNC_" + libelle.toUpperCase().trim();
        this.libelle = libelle;
    }

    public Fonction() {
    }

    public String getIdFonction() {
        return idFonction;
    }

    public void setIdFonction(String idFonction) {
        this.idFonction = idFonction;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
}
