package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Assurance {

    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idAssurance;
    private String agence;
    private String contrat;
    private String dateDebut;
    private String dateFin;
    private String idVehicule;

    public Assurance(String agence, String contrat, String dateDebut, String dateFin, String idVehicule) {
        this.idAssurance = "ASSU_" + agence.toUpperCase().trim() + "_" + idVehicule.replace("_", "") + "_" + dateDebut.replace("-", "") + "_" + dateFin.replace("-", "");
        this.agence = agence;
        this.contrat = contrat;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.idVehicule = idVehicule;
    }

    public Assurance() {
    }

    public String getIdAssurance() {
        return idAssurance;
    }

    public void setIdAssurance(String idAssurance) {
        this.idAssurance = idAssurance;
    }

    public String getAgence() {
        return agence;
    }

    public void setAgence(String agence) {
        this.agence = agence;
    }

    public String getContrat() {
        return contrat;
    }

    public void setContrat(String contrat) {
        this.contrat = contrat;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
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

