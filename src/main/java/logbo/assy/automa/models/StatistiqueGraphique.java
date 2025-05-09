package logbo.assy.automa.models;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Modèle pour représenter les données statistiques dans les graphiques
 */
public class StatistiqueGraphique {
    private String mois; // Format "yyyy-MM"
    private double coutEntretien;
    private double coutMission;
    private double coutCarburant;
    private double coutTotal;

    public StatistiqueGraphique() {
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public double getCoutEntretien() {
        return coutEntretien;
    }

    public void setCoutEntretien(double coutEntretien) {
        this.coutEntretien = coutEntretien;
        calculerCoutTotal();
    }

    public double getCoutMission() {
        return coutMission;
    }

    public void setCoutMission(double coutMission) {
        this.coutMission = coutMission;
        calculerCoutTotal();
    }

    public double getCoutCarburant() {
        return coutCarburant;
    }

    public void setCoutCarburant(double coutCarburant) {
        this.coutCarburant = coutCarburant;
        calculerCoutTotal();
    }

    public double getCoutTotal() {
        return coutTotal;
    }

    private void calculerCoutTotal() {
        this.coutTotal = this.coutEntretien + this.coutMission + this.coutCarburant;
    }

    /**
     * Obtient le mois formaté pour l'affichage (ex: "Jan 2023")
     */
    public String getMoisFormate() {
        try {
            YearMonth yearMonth = YearMonth.parse(mois);
            return yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));
        } catch (Exception e) {
            return mois;
        }
    }

    @Override
    public String toString() {
        return "StatistiqueGraphique{" +
                "mois='" + mois + '\'' +
                ", coutEntretien=" + coutEntretien +
                ", coutMission=" + coutMission +
                ", coutCarburant=" + coutCarburant +
                ", coutTotal=" + coutTotal +
                '}';
    }
}