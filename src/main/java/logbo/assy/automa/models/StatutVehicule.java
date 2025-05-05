package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class StatutVehicule {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idStatutVehicule;
    private String libelle;

    public StatutVehicule(String libelle) {
        this.idStatutVehicule = "STAT_" + libelle.toUpperCase().trim();
        this.libelle = libelle;
    }

    public StatutVehicule() {
    }

    public String getIdStatutVehicule() {
        return idStatutVehicule;
    }

    public void setIdStatutVehicule(String idStatut) {
        this.idStatutVehicule = idStatut;
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
