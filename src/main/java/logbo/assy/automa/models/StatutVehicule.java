package logbo.assy.automa.models;

public class StatutVehicule {
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
}
