package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Service {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idService;
    private String libelle;
    private String localisation;

    public Service(String libelle, String localisation) {
        this.idService = "SERV_" + libelle.toUpperCase().trim();
        this.libelle = libelle;
        this.localisation = localisation;
    }

    public Service() {
    }

    public String getIdService() {
        return idService;
    }

    public void setIdService(String idService) {
        this.idService = idService;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getlocalisation() {
        return localisation;
    }

    public void setlocalisation(String localisation) {
        this.localisation = localisation;
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
