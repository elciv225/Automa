package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Fonction {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
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
    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }}
