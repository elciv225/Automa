package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Entretien {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idEntretien;
    private String motif;
    private String observation;
    private String dateEntree;
    private String dateSortie;
    private String prix;
    private String lieu;
    private String idVehicule;

    public Entretien(String motif, String observation, String dateEntree, String dateSortie, String prix, String lieu, String idVehicule) {
        this.idEntretien = "ENT_" + idVehicule.toUpperCase().replace("_","") + "_" + dateEntree.replace("-", "") + "_" + dateSortie.replace("-", "");
        this.motif = motif;
        this.observation = observation;
        this.dateEntree = dateEntree;
        this.dateSortie = dateSortie;
        this.prix = prix;
        this.lieu = lieu;
        this.idVehicule = idVehicule;
    }

    public Entretien() {
    }

    public String getIdEntretien() {
        return idEntretien;
    }

    public void setIdEntretien(String idEntretien) {
        this.idEntretien = idEntretien;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getDateEntree() {
        return dateEntree;
    }

    public void setDateEntree(String dateEntree) {
        this.dateEntree = dateEntree;
    }

    public String getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(String dateSortie) {
        this.dateSortie = dateSortie;
    }

    public String getPrix() {
        return prix;
    }

    public void setPrix(String prix) {
        this.prix = prix;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
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
