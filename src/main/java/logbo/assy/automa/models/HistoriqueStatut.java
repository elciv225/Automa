package logbo.assy.automa.models;

public class HistoriqueStatut {
    private StatutVehicule statutVehicule;
    private Vehicule vehicule;
    private String dateStatut;

    public HistoriqueStatut(StatutVehicule statutVehicule, Vehicule vehicule, String dateStatut) {
        this.statutVehicule = statutVehicule;
        this.vehicule = vehicule;
        this.dateStatut = dateStatut;
    }
    public HistoriqueStatut() {
    }

    public StatutVehicule getStatutVehicule() {
        return statutVehicule;
    }

    public void setStatutVehicule(StatutVehicule statutVehicule) {
        this.statutVehicule = statutVehicule;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public String getDateStatut() {
        return dateStatut;
    }

    public void setDateStatut(String dateStatut) {
        this.dateStatut = dateStatut;
    }
}
