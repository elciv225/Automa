package logbo.assy.automa.dao;

import logbo.assy.automa.AuditLogger;
import logbo.assy.automa.SessionManager;
import logbo.assy.automa.models.Assurance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AssuranceDAO {
    private static final Logger LOGGER = Logger.getLogger(AssuranceDAO.class.getName());
    private final Database db;

    public AssuranceDAO() throws SQLException {
        this.db = new Database();
    }

    /**
     * Mapper ResultSet vers une entité Assurance
     */
    private Assurance mapResultSetToEntity(ResultSet rs) throws SQLException {
        Assurance assurance = new Assurance();
        assurance.setIdAssurance(rs.getString("id_assurance"));
        assurance.setAgence(rs.getString("agence"));
        assurance.setContrat(rs.getString("contrat"));
        assurance.setPrix(rs.getString("prix"));
        assurance.setDateDebut(rs.getString("date_debut"));
        assurance.setDateFin(rs.getString("date_fin"));
        assurance.setIdVehicule(rs.getString("id_vehicule"));
        return assurance;
    }

    /**
     * Récupère toutes les assurances
     */
    public List<Assurance> getAll() throws SQLException {
        List<Assurance> assurances = new ArrayList<>();
        String sql = "SELECT * FROM assurance";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                assurances.add(mapResultSetToEntity(rs));
            }
        }

        return assurances;
    }

    /**
     * Vérifie si un véhicule a une assurance encore valide (non expirée)
     */
    public boolean hasActiveAssurance(String idVehicule) throws SQLException {
        String sql = "SELECT COUNT(*) FROM assurance WHERE id_vehicule = ? AND date_fin >= CURRENT_DATE";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idVehicule);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }

        return false;
    }


    /**
     * Ajoute une assurance
     */
    public void add(Assurance assurance) throws SQLException {
        // Vérifie si le véhicule a déjà une assurance
        if (hasActiveAssurance(assurance.getIdVehicule())) {
            throw new SQLException("Ce véhicule possède déjà une assurance valide. Impossible d’en ajouter une nouvelle.");
        }

        String sql = "INSERT INTO assurance (id_assurance, agence, contrat, date_debut, date_fin, prix, id_vehicule) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, assurance.getIdAssurance());
            stmt.setString(2, assurance.getAgence());
            stmt.setString(3, assurance.getContrat());
            stmt.setString(4, assurance.getDateDebut());
            stmt.setString(5, assurance.getDateFin());
            stmt.setString(6, assurance.getPrix());
            stmt.setString(7, assurance.getIdVehicule());

            stmt.executeUpdate();

            AuditLogger.log(
                    SessionManager.getLogin(),
                    "AJOUT",
                    "ASSURANCE",
                    assurance.getIdAssurance(),
                    "Ajout de l'assurance liée au véhicule " + assurance.getIdVehicule()
            );
        }
    }

    /**
     * Modifie une assurance existante
     */
    public void update(Assurance assurance) throws SQLException {
        String sql = "UPDATE assurance SET agence = ?, contrat = ?, date_debut = ?, date_fin = ?, prix = ?, id_vehicule = ? WHERE id_assurance = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, assurance.getAgence());
            stmt.setString(2, assurance.getContrat());
            stmt.setString(3, assurance.getDateDebut());
            stmt.setString(4, assurance.getDateFin());
            stmt.setString(5, assurance.getPrix());
            stmt.setString(6, assurance.getIdVehicule());
            stmt.setString(7, assurance.getIdAssurance());

            stmt.executeUpdate();

            AuditLogger.log(
                    SessionManager.getLogin(),
                    "MODIFICATION",
                    "ASSURANCE",
                    assurance.getIdAssurance(),
                    "Modification de l'assurance " + assurance.getIdAssurance()
            );
        }
    }

    /**
     * Supprime une assurance par ID
     */
    public void delete(String idAssurance) throws SQLException {
        String sql = "DELETE FROM assurance WHERE id_assurance = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idAssurance);
            stmt.executeUpdate();

            AuditLogger.log(
                    SessionManager.getLogin(),
                    "SUPPRESSION",
                    "ASSURANCE",
                    idAssurance,
                    "Suppression de l'assurance " + idAssurance
            );
        }
    }

    /**
     * Récupère les assurances d'un véhicule donné
     */
    public List<Assurance> getByVehiculeId(String idVehicule) throws SQLException {
        List<Assurance> result = new ArrayList<>();
        String sql = "SELECT * FROM assurance WHERE id_vehicule = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idVehicule);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(mapResultSetToEntity(rs));
            }
        }

        return result;
    }
}
