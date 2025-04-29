package logbo.assy.automa.dao;

import logbo.assy.automa.models.Mission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionDAO {

    private static final Logger LOGGER = Logger.getLogger(MissionDAO.class.getName());
    private final Database db;

    public MissionDAO() throws SQLException {
        LOGGER.info("Initialisation de MissionDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les missions depuis la BDD.
     */
    public List<Mission> getAllMissions() throws SQLException {
        LOGGER.info("Chargement de toutes les missions");
        List<Mission> missions = new ArrayList<>();
        String sql = "SELECT id_mission, date_debut, date_fin, cout, cout_carburant, observation, circuit, id_vehicule "
                   + "FROM mission";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Mission m = new Mission();
                m.setIdMission(     rs.getString("id_mission") );
                m.setDateDebut(     rs.getString("date_debut") );
                m.setDateFin(       rs.getString("date_fin") );
                m.setCout(          rs.getString("cout") );
                m.setCoutCarburant( rs.getString("cout_carburant") );
                m.setObservation(   rs.getString("observation") );
                m.setCircuit(       rs.getString("circuit") );
                m.setIdVehicule(    rs.getString("id_vehicule") );
                missions.add(m);
                LOGGER.finer("Mission chargée => "
                             + m.getIdMission()
                             + " | véhicule="
                             + m.getIdVehicule());
            }
            LOGGER.info("Nombre total de missions récupérées : " + missions.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des missions", e);
            throw e;
        }

        return missions;
    }

    /**
     * Ajoute une nouvelle mission.
     */
    public void addMission(Mission mission) throws SQLException {
        LOGGER.info("Ajout d'une nouvelle mission : " + mission.getIdMission());
        String sql = "INSERT INTO mission "
                   + "(id_mission, date_debut, date_fin, cout, cout_carburant, observation, circuit, id_vehicule) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, mission.getIdMission());
            stmt.setString(2, mission.getDateDebut());
            stmt.setString(3, mission.getDateFin());
            stmt.setString(4, mission.getCout());
            stmt.setString(5, mission.getCoutCarburant());
            stmt.setString(6, mission.getObservation());
            stmt.setString(7, mission.getCircuit());
            stmt.setString(8, mission.getIdVehicule());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la mission", e);
            throw e;
        }
    }

    /**
     * Met à jour une mission existante.
     */
    public void updateMission(Mission mission) throws SQLException {
        LOGGER.info("Mise à jour de la mission : " + mission.getIdMission());
        String sql = "UPDATE mission SET "
                   + "date_debut = ?, date_fin = ?, cout = ?, cout_carburant = ?, observation = ?, circuit = ?, id_vehicule = ? "
                   + "WHERE id_mission = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, mission.getDateDebut());
            stmt.setString(2, mission.getDateFin());
            stmt.setString(3, mission.getCout());
            stmt.setString(4, mission.getCoutCarburant());
            stmt.setString(5, mission.getObservation());
            stmt.setString(6, mission.getCircuit());
            stmt.setString(7, mission.getIdVehicule());
            stmt.setString(8, mission.getIdMission());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la mission", e);
            throw e;
        }
    }

    /**
     * Supprime une mission par son identifiant.
     */
    public void deleteMission(String missionId) throws SQLException {
        LOGGER.info("Suppression de la mission id=" + missionId);
        String sql = "DELETE FROM mission WHERE id_mission = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, missionId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la mission", e);
            throw e;
        }
    }
}
