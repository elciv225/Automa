package logbo.assy.automa.dao;

import logbo.assy.automa.models.HistoriqueStatut;
import logbo.assy.automa.models.StatutVehicule;
import logbo.assy.automa.models.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoriqueStatutDAO {

    private static final Logger LOGGER = Logger.getLogger(HistoriqueStatutDAO.class.getName());
    private final Database db;

    public HistoriqueStatutDAO() throws SQLException {
        LOGGER.info("Initialisation de HistoriqueStatutDAO");
        this.db = new Database();
    }

    /**
     * Récupère tous les historiques de statut depuis la BDD.
     */
    public List<HistoriqueStatut> getAllHistoriques() throws SQLException {
        LOGGER.info("Chargement de tous les historiques de statut");
        List<HistoriqueStatut> historiques = new ArrayList<>();
        String sql = "SELECT id_statut_vehicule, id_vehicule, date_etat FROM historique_statut";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                HistoriqueStatut hs = new HistoriqueStatut();

                StatutVehicule statut = new StatutVehicule();
                statut.setIdStatutVehicule(rs.getString("id_statut_vehicule"));
                hs.setStatutVehicule(statut);

                Vehicule vehicule = new Vehicule();
                vehicule.setIdVehicule(rs.getString("id_vehicule"));
                hs.setVehicule(vehicule);

                hs.setDateStatut(rs.getString("date_etat"));

                historiques.add(hs);
                LOGGER.finer("Historique chargé => statut="
                             + statut.getIdStatutVehicule()
                             + " | véhicule="
                             + vehicule.getIdVehicule());
            }
            LOGGER.info("Nombre total d'historiques récupérés : " + historiques.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des historiques de statut", e);
            throw e;
        }

        return historiques;
    }

    /**
     * Ajoute un nouvel historique de statut.
     */
    public void addHistorique(HistoriqueStatut hs) throws SQLException {
        LOGGER.info("Ajout d'un historique de statut : véhicule="
                    + hs.getVehicule().getIdVehicule()
                    + " | statut="
                    + hs.getStatutVehicule().getIdStatutVehicule());
        String sql = "INSERT INTO historique_statut (id_statut_vehicule, id_vehicule, date_etat) VALUES (?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, hs.getStatutVehicule().getIdStatutVehicule());
            stmt.setString(2, hs.getVehicule().getIdVehicule());
            stmt.setString(3, hs.getDateStatut());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'historique de statut", e);
            throw e;
        }
    }

    /**
     * Met à jour la date d'un historique de statut existant.
     */
    public void updateHistorique(HistoriqueStatut hs) throws SQLException {
        LOGGER.info("Mise à jour de l'historique : véhicule="
                    + hs.getVehicule().getIdVehicule()
                    + " | statut="
                    + hs.getStatutVehicule().getIdStatutVehicule());
        String sql = "UPDATE historique_statut SET date_etat = ? "
                   + "WHERE id_statut_vehicule = ? AND id_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, hs.getDateStatut());
            stmt.setString(2, hs.getStatutVehicule().getIdStatutVehicule());
            stmt.setString(3, hs.getVehicule().getIdVehicule());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'historique de statut", e);
            throw e;
        }
    }

    /**
     * Supprime un historique de statut par ses clés composées.
     */
    public void deleteHistorique(String statutId, String vehiculeId) throws SQLException {
        LOGGER.info("Suppression de l'historique : véhicule="
                    + vehiculeId
                    + " | statut="
                    + statutId);
        String sql = "DELETE FROM historique_statut WHERE id_statut_vehicule = ? AND id_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, statutId);
            stmt.setString(2, vehiculeId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'historique de statut", e);
            throw e;
        }
    }
}
