package logbo.assy.automa.dao;

import logbo.assy.automa.models.Assurance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssuranceDAO {

    private static final Logger LOGGER = Logger.getLogger(AssuranceDAO.class.getName());
    private final Database db;

    public AssuranceDAO() throws SQLException {
        LOGGER.info("Initialisation de AssuranceDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les assurances depuis la BDD.
     */
    public List<Assurance> getAllAssurances() throws SQLException {
        LOGGER.info("Chargement de toutes les assurances");
        List<Assurance> assurances = new ArrayList<>();
        String sql = "SELECT id_assurance, agence, contrat, date_debut, date_fin, id_vehicule FROM assurance";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Assurance a = new Assurance();
                a.setIdAssurance( rs.getString("id_assurance") );
                a.setAgence(       rs.getString("agence") );
                a.setContrat(      rs.getString("contrat") );
                a.setDateDebut(    rs.getString("date_debut") );
                a.setDateFin(      rs.getString("date_fin") );
                a.setIdVehicule(   rs.getString("id_vehicule") );
                assurances.add(a);
                LOGGER.finer("Assurance chargée => " + a.getIdAssurance() + " | " + a.getAgence());
            }
            LOGGER.info("Nombre total d'assurances récupérées : " + assurances.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des assurances", e);
            throw e;
        }

        return assurances;
    }

    /**
     * Ajoute une nouvelle assurance.
     */
    public void addAssurance(Assurance assurance) throws SQLException {
        LOGGER.info("Ajout d'une nouvelle assurance : " + assurance.getIdAssurance());
        String sql = "INSERT INTO assurance (id_assurance, agence, contrat, date_debut, date_fin, id_vehicule) VALUES (?, ?, ?, ?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, assurance.getIdAssurance());
            stmt.setString(2, assurance.getAgence());
            stmt.setString(3, assurance.getContrat());
            stmt.setString(4, assurance.getDateDebut());
            stmt.setString(5, assurance.getDateFin());
            stmt.setString(6, assurance.getIdVehicule());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'assurance", e);
            throw e;
        }
    }

    /**
     * Met à jour une assurance existante.
     */
    public void updateAssurance(Assurance assurance) throws SQLException {
        LOGGER.info("Mise à jour de l'assurance : " + assurance.getIdAssurance());
        String sql = "UPDATE assurance SET agence = ?, contrat = ?, date_debut = ?, date_fin = ?, id_vehicule = ? WHERE id_assurance = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, assurance.getAgence());
            stmt.setString(2, assurance.getContrat());
            stmt.setString(3, assurance.getDateDebut());
            stmt.setString(4, assurance.getDateFin());
            stmt.setString(5, assurance.getIdVehicule());
            stmt.setString(6, assurance.getIdAssurance());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'assurance", e);
            throw e;
        }
    }

    /**
     * Supprime une assurance par son identifiant.
     */
    public void deleteAssurance(String assuranceId) throws SQLException {
        LOGGER.info("Suppression de l'assurance id=" + assuranceId);
        String sql = "DELETE FROM assurance WHERE id_assurance = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, assuranceId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'assurance", e);
            throw e;
        }
    }
}
