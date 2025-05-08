package logbo.assy.automa.models;

public class PaiementAttribution {
    private int idPaiement;
    private String idVehicule;
    private String idPersonnel;
    private String moisPaiement; // au format YYYY-MM-01
    private String montantVerse;
    private String dateVersement;

    public PaiementAttribution(int idPaiement, String idVehicule, String idPersonnel, String moisPaiement, String montantVerse, String dateVersement) {
        this.idPaiement = idPaiement;
        this.idVehicule = idVehicule;
        this.idPersonnel = idPersonnel;
        this.moisPaiement = moisPaiement;
        this.montantVerse = montantVerse;
        this.dateVersement = dateVersement;
    }

    public PaiementAttribution() {}

    public int getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
    }

    public String getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getIdPersonnel() {
        return idPersonnel;
    }

    public void setIdPersonnel(String idPersonnel) {
        this.idPersonnel = idPersonnel;
    }

    public String getMoisPaiement() {
        return moisPaiement;
    }

    public void setMoisPaiement(String moisPaiement) {
        this.moisPaiement = moisPaiement;
    }

    public String getMontantVerse() {
        return montantVerse;
    }

    public void setMontantVerse(String montantVerse) {
        this.montantVerse = montantVerse;
    }

    public String getDateVersement() {
        return dateVersement;
    }

    public void setDateVersement(String dateVersement) {
        this.dateVersement = dateVersement;
    }


}
