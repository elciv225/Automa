package logbo.assy.automa.models;

public class CategorieVehicule {
    private String idCategorie;
    private String libelle;
    private String nombrePlace;

    public CategorieVehicule(String libelle, String nombrePlace) {
        this.idCategorie = "CAT_" + libelle.toUpperCase().trim();
        this.libelle = libelle;
        this.nombrePlace = nombrePlace;
    }

    public CategorieVehicule() {

    }

    public String getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(String idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNombrePlace() {
        return nombrePlace;
    }

    public void setNombrePlace(String nombrePlace) {
        this.nombrePlace = nombrePlace;
    }
}
