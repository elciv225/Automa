package logbo.assy.automa.dao;

import logbo.assy.automa.models.PaiementAttribution;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementAttributionDAO {
    private final Database db;

    public PaiementAttributionDAO() throws SQLException {
        this.db = new Database();
    }

    public void ajouterPaiement(PaiementAttribution paiement) throws SQLException {
        String sql = "INSERT INTO paiement_attribution (id_vehicule, id_personnel, mois_paiement, montant_verse, date_versement) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paiement.getIdVehicule());
            stmt.setString(2, paiement.getIdPersonnel());
            stmt.setString(3, paiement.getMoisPaiement());
            stmt.setString(4, paiement.getMontantVerse());
            stmt.setString(5, paiement.getDateVersement());
            stmt.executeUpdate();
        }
    }

    public List<PaiementAttribution> getPaiementsPourAttribution(String idVehicule, String idPersonnel) throws SQLException {
        List<PaiementAttribution> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement_attribution WHERE id_vehicule = ? AND id_personnel = ? ORDER BY mois_paiement ASC";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idVehicule);
            stmt.setString(2, idPersonnel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                paiements.add(new PaiementAttribution(
                        rs.getInt("id_paiement"),
                        rs.getString("id_vehicule"),
                        rs.getString("id_personnel"),
                        rs.getString("mois_paiement"),
                        rs.getString("montant_verse"),
                        rs.getString("date_versement")
                ));
            }
        }
        return paiements;
    }

    public double getTotalVerse(String idVehicule, String idPersonnel) throws SQLException {
        String sql = "SELECT SUM(CAST(montant_verse AS DECIMAL)) AS total FROM paiement_attribution WHERE id_vehicule = ? AND id_personnel = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idVehicule);
            stmt.setString(2, idPersonnel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        }
        return 0.0;
    }

    public int getMoisPayes(String idVehicule, String idPersonnel) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT mois_paiement) AS mois FROM paiement_attribution WHERE id_vehicule = ? AND id_personnel = ?";
        try (Connection conn = db.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idVehicule);
            stmt.setString(2, idPersonnel);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("mois");
        }
        return 0;
    }
}
