package logbo.assy.automa.dao;

import logbo.assy.automa.models.AttributionVehicule;
import logbo.assy.automa.models.Vehicule;
import logbo.assy.automa.models.Personnel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttributionVehiculeDAO {

    private static final Logger LOGGER = Logger.getLogger(AttributionVehiculeDAO.class.getName());
    private final Database db;

    public AttributionVehiculeDAO() throws SQLException {
        LOGGER.info("Initialisation de AttributionVehiculeDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les attributions depuis la BDD.
     */
    public List<AttributionVehicule> getAllAttributions() throws SQLException {
        LOGGER.info("Chargement de toutes les attributions de véhicules");
        List<AttributionVehicule> attributions = new ArrayList<>();
        String sql = "SELECT id_vehicule, id_personnel, date_attribution FROM attribution_vehicule";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                AttributionVehicule attr = new AttributionVehicule();

                Vehicule vehicule = new Vehicule();
                vehicule.setIdVehicule(rs.getString("id_vehicule"));
                attr.setVehicule(vehicule);

                Personnel pers = new Personnel();
                pers.setIdPersonnel(rs.getString("id_personnel"));
                attr.setPersonnel(pers);

                attr.setDateAttribution(rs.getString("date_attribution"));

                attributions.add(attr);
                LOGGER.finer("Attribution chargée => véhicule="
                             + vehicule.getIdVehicule()
                             + " | personnel="
                             + pers.getIdPersonnel());
            }
            LOGGER.info("Nombre total d'attributions récupérées : " + attributions.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des attributions", e);
            throw e;
        }

        return attributions;
    }

    /**
     * Ajoute une nouvelle attribution.
     */
    public void addAttribution(AttributionVehicule attribution) throws SQLException {
        LOGGER.info("Ajout d'une nouvelle attribution : véhicule="
                    + attribution.getVehicule().getIdVehicule()
                    + " | personnel="
                    + attribution.getPersonnel().getIdPersonnel());
        String sql = "INSERT INTO attribution_vehicule (id_vehicule, id_personnel, date_attribution) VALUES (?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, attribution.getVehicule().getIdVehicule());
            stmt.setString(2, attribution.getPersonnel().getIdPersonnel());
            stmt.setString(3, attribution.getDateAttribution());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'attribution", e);
            throw e;
        }
    }

    /**
     * Met à jour la date d'une attribution existante.
     */
    public void updateAttribution(AttributionVehicule attribution) throws SQLException {
        LOGGER.info("Mise à jour de l'attribution : véhicule="
                    + attribution.getVehicule().getIdVehicule()
                    + " | personnel="
                    + attribution.getPersonnel().getIdPersonnel());
        String sql = "UPDATE attribution_vehicule SET date_attribution = ? "
                   + "WHERE id_vehicule = ? AND id_personnel = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, attribution.getDateAttribution());
            stmt.setString(2, attribution.getVehicule().getIdVehicule());
            stmt.setString(3, attribution.getPersonnel().getIdPersonnel());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'attribution", e);
            throw e;
        }
    }

    /**
     * Supprime une attribution par ses clés composées.
     */
    public void deleteAttribution(String vehiculeId, String personnelId) throws SQLException {
        LOGGER.info("Suppression de l'attribution : véhicule="
                    + vehiculeId
                    + " | personnel="
                    + personnelId);
        String sql = "DELETE FROM attribution_vehicule WHERE id_vehicule = ? AND id_personnel = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, vehiculeId);
            stmt.setString(2, personnelId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'attribution", e);
            throw e;
        }
    }
}
