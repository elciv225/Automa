package logbo.assy.automa.dao;

import logbo.assy.automa.models.ParticiperMission;
import logbo.assy.automa.models.Mission;
import logbo.assy.automa.models.Personnel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParticiperMissionDAO {

    private static final Logger LOGGER = Logger.getLogger(ParticiperMissionDAO.class.getName());
    private final Database db;

    public ParticiperMissionDAO() throws SQLException {
        LOGGER.info("Initialisation de ParticiperMissionDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les participations aux missions depuis la BDD.
     */
    public List<ParticiperMission> getAllParticipations() throws SQLException {
        LOGGER.info("Chargement de toutes les participations aux missions");
        List<ParticiperMission> list = new ArrayList<>();
        String sql = "SELECT id_personnel, id_mission FROM participer_mission";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                ParticiperMission pm = new ParticiperMission();

                Personnel pers = new Personnel();
                pers.setIdPersonnel(rs.getString("id_personnel"));
                pm.setPersonnel(pers);

                Mission mission = new Mission();
                mission.setIdMission(rs.getString("id_mission"));
                pm.setMission(mission);

                list.add(pm);
                LOGGER.finer("Participation chargée => personnel="
                             + pers.getIdPersonnel()
                             + " | mission="
                             + mission.getIdMission());
            }
            LOGGER.info("Nombre de participations récupérées : " + list.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des participations", e);
            throw e;
        }

        return list;
    }

    /**
     * Ajoute une nouvelle participation à une mission.
     */
    public void addParticipation(ParticiperMission pm) throws SQLException {
        LOGGER.info("Ajout d'une participation : personnel="
                    + pm.getPersonnel().getIdPersonnel()
                    + " | mission="
                    + pm.getMission().getIdMission());
        String sql = "INSERT INTO participer_mission (id_personnel, id_mission) VALUES (?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, pm.getPersonnel().getIdPersonnel());
            stmt.setString(2, pm.getMission().getIdMission());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la participation", e);
            throw e;
        }
    }

    /**
     * Supprime une participation par ses clés composées.
     */
    public void deleteParticipation(String personnelId, String missionId) throws SQLException {
        LOGGER.info("Suppression d'une participation : personnel="
                    + personnelId
                    + " | mission="
                    + missionId);
        String sql = "DELETE FROM participer_mission WHERE id_personnel = ? AND id_mission = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, personnelId);
            stmt.setString(2, missionId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la participation", e);
            throw e;
        }
    }
}
