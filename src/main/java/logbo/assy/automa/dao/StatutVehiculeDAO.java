package logbo.assy.automa.dao;

import logbo.assy.automa.models.StatutVehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatutVehiculeDAO {

    private static final Logger LOGGER = Logger.getLogger(StatutVehiculeDAO.class.getName());
    private final Database db;

    public StatutVehiculeDAO() throws SQLException {
        LOGGER.info("Initialisation de StatutVehiculeDAO");
        this.db = new Database();
    }

    /**
     * Récupère tous les statuts de véhicule depuis la BDD.
     */
    public List<StatutVehicule> getAllStatuts() throws SQLException {
        LOGGER.info("Chargement de tous les statuts de véhicule");
        List<StatutVehicule> statuts = new ArrayList<>();
        String sql = "SELECT id_statut_vehicule, libelle FROM statut_vehicule";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                StatutVehicule s = new StatutVehicule();
                s.setIdStatutVehicule(rs.getString("id_statut_vehicule"));
                s.setLibelle(        rs.getString("libelle"));
                statuts.add(s);
                LOGGER.finer("Statut chargé => "
                             + s.getIdStatutVehicule()
                             + " | "
                             + s.getLibelle());
            }
            LOGGER.info("Nombre total de statuts récupérés : " + statuts.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des statuts de véhicule", e);
            throw e;
        }

        return statuts;
    }

    /**
     * Ajoute un nouveau statut de véhicule.
     */
    public void addStatut(StatutVehicule statut) throws SQLException {
        LOGGER.info("Ajout d'un nouveau statut : " + statut.getLibelle());
        String sql = "INSERT INTO statut_vehicule (id_statut_vehicule, libelle) VALUES (?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, statut.getIdStatutVehicule());
            stmt.setString(2, statut.getLibelle());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du statut de véhicule", e);
            throw e;
        }
    }

    /**
     * Met à jour un statut de véhicule existant.
     */
    public void updateStatut(StatutVehicule statut) throws SQLException {
        LOGGER.info("Mise à jour du statut : " + statut.getIdStatutVehicule());
        String sql = "UPDATE statut_vehicule SET libelle = ? WHERE id_statut_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, statut.getLibelle());
            stmt.setString(2, statut.getIdStatutVehicule());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du statut de véhicule", e);
            throw e;
        }
    }

    /**
     * Supprime un statut de véhicule par son identifiant.
     */
    public void deleteStatut(String statutId) throws SQLException {
        LOGGER.info("Suppression du statut id=" + statutId);
        String sql = "DELETE FROM statut_vehicule WHERE id_statut_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, statutId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du statut de véhicule", e);
            throw e;
        }
    }
}
