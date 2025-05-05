package logbo.assy.automa.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Mission {
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private String idMission;
    private String dateDebut;
    private String dateFin;
    private String cout;
    private String coutCarburant;
    private String observation;
    private String circuit;
    private String idVehicule;

    public Mission(String dateDebut, String dateFin, String cout, String coutCarburant, String observation, String circuit, String idVehicule) {
        this.idMission = "MIS_" + idVehicule.toUpperCase().replace("_","") + "_" + dateDebut.replace("-", "") + "_" + dateFin.replace("-", "");
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.cout = cout;
        this.coutCarburant = coutCarburant;
        this.observation = observation;
        this.circuit = circuit;
        this.idVehicule = idVehicule;
    }

    public Mission() {
    }

    public String getIdMission() {
        return idMission;
    }

    public void setIdMission(String idMission) {
        this.idMission = idMission;
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

    public String getCout() {
        return cout;
    }

    public void setCout(String cout) {
        this.cout = cout;
    }

    public String getCoutCarburant() {
        return coutCarburant;
    }

    public void setCoutCarburant(String coutCarburant) {
        this.coutCarburant = coutCarburant;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getCircuit() {
        return circuit;
    }

    public void setCircuit(String circuit) {
        this.circuit = circuit;
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
    }}
