package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CategorieVehicule {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
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

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
