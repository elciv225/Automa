package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AttributionVehicule {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private Vehicule vehicule;
    private Personnel personnel;
    private String dateAttribution;
    private String montantTotal;
    private String dateDebutRemboursement;

    public AttributionVehicule(Vehicule vehicule, Personnel personnel, String dateAttribution) {
        this.vehicule = vehicule;
        this.personnel = personnel;
        this.dateAttribution = dateAttribution;
    }

    public AttributionVehicule() {
    }

    // Ajoutez ce getter et setter
    public String getDateDebutRemboursement() {
        return dateDebutRemboursement;
    }

    public void setDateDebutRemboursement(String dateDebutRemboursement) {
        this.dateDebutRemboursement = dateDebutRemboursement;
    }

    public String getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(String dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public Personnel getPersonnel() {
        return personnel;
    }

    public void setPersonnel(Personnel personnel) {
        this.personnel = personnel;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
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

    public String getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(String montantTotal) {
        this.montantTotal = montantTotal;
    }
}
