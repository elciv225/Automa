package logbo.assy.automa.dao;

import logbo.assy.automa.models.Entretien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntretienDAO {

    private static final Logger LOGGER = Logger.getLogger(EntretienDAO.class.getName());
    private final Database db;

    public EntretienDAO() throws SQLException {
        LOGGER.info("Initialisation de EntretienDAO");
        this.db = new Database();
    }

    /**
     * Récupère tous les entretiens depuis la BDD.
     */
    public List<Entretien> getAllEntretiens() throws SQLException {
        LOGGER.info("Chargement de tous les entretiens");
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT id_entretien, motif, observation, date_entree, date_sortie, prix, lieu, id_vehicule "
                + "FROM entretien";
        LOGGER.fine("Requête SQL => " + sql);

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Entretien e = new Entretien();
                e.setIdEntretien(rs.getString("id_entretien"));
                e.setMotif(rs.getString("motif"));
                e.setObservation(rs.getString("observation"));
                e.setDateEntree(rs.getString("date_entree"));
                e.setDateSortie(rs.getString("date_sortie"));
                e.setPrix(rs.getString("prix"));
                e.setLieu(rs.getString("lieu"));
                e.setIdVehicule(rs.getString("id_vehicule"));
                entretiens.add(e);
                LOGGER.finer("Entretien chargé => " + e.getIdEntretien() + " | véhicule=" + e.getIdVehicule());
            }
            LOGGER.info("Nombre total d'entretiens récupérés : " + entretiens.size());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des entretiens", ex);
            throw ex;
        }

        return entretiens;
    }

    /**
     * Ajoute un nouvel entretien.
     */
    public void addEntretien(Entretien entretien) throws SQLException {
        LOGGER.info("Ajout d'un nouvel entretien : " + entretien.getIdEntretien());
        String sql = "INSERT INTO entretien "
                + "(id_entretien, motif, observation, date_entree, date_sortie, prix, lieu, id_vehicule) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, entretien.getIdEntretien());
            stmt.setString(2, entretien.getMotif());
            stmt.setString(3, entretien.getObservation());
            stmt.setString(4, entretien.getDateEntree());
            stmt.setString(5, entretien.getDateSortie());
            stmt.setString(6, entretien.getPrix());
            stmt.setString(7, entretien.getLieu());
            stmt.setString(8, entretien.getIdVehicule());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de l'entretien", ex);
            throw ex;
        }
    }

    /**
     * Met à jour un entretien existant.
     */
    public void updateEntretien(Entretien entretien) throws SQLException {
        LOGGER.info("Mise à jour de l'entretien : " + entretien.getIdEntretien());
        String sql = "UPDATE entretien SET "
                + "motif = ?, observation = ?, date_entree = ?, date_sortie = ?, prix = ?, lieu = ?, id_vehicule = ? "
                + "WHERE id_entretien = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, entretien.getMotif());
            stmt.setString(2, entretien.getObservation());
            stmt.setString(3, entretien.getDateEntree());
            stmt.setString(4, entretien.getDateSortie());
            stmt.setString(5, entretien.getPrix());
            stmt.setString(6, entretien.getLieu());
            stmt.setString(7, entretien.getIdVehicule());
            stmt.setString(8, entretien.getIdEntretien());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de l'entretien", ex);
            throw ex;
        }
    }

    /**
     * Supprime un entretien par son identifiant.
     */
    public void deleteEntretien(String entretienId) throws SQLException {
        LOGGER.info("Suppression de l'entretien id=" + entretienId);
        String sql = "DELETE FROM entretien WHERE id_entretien = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, entretienId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'entretien", ex);
            throw ex;
        }
    }

    public List<Entretien> getEntretiensEntre(String dateDebut, String dateFin) throws SQLException {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE date_entree >= ? AND date_entree <= ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dateDebut);
            stmt.setString(2, dateFin);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Entretien e = new Entretien();
                e.setIdEntretien(rs.getString("id_entretien"));
                e.setMotif(rs.getString("motif"));
                e.setObservation(rs.getString("observation"));
                e.setDateEntree(rs.getString("date_entree"));
                e.setDateSortie(rs.getString("date_sortie"));
                e.setPrix(rs.getString("prix"));
                e.setLieu(rs.getString("lieu"));
                e.setIdVehicule(rs.getString("id_vehicule"));
                entretiens.add(e);
            }
        }
        return entretiens;
    }

    /**
     * Recherche des entretiens en fonction d'un mot clé.
     */
    public List<Entretien> rechercherEntretiens(String motCle) throws SQLException {
        List<Entretien> resultats = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE id_vehicule LIKE ? OR motif LIKE ? OR observation LIKE ?";

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            String pattern = "%" + motCle + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Entretien e = new Entretien();
                    e.setIdEntretien(rs.getString("id_entretien"));
                    e.setDateEntree(rs.getString("date_entree"));
                    e.setDateSortie(rs.getString("date_sortie"));
                    e.setMotif(rs.getString("motif"));
                    e.setObservation(rs.getString("observation"));
                    e.setPrix(rs.getString("prix"));
                    e.setLieu(rs.getString("lieu"));
                    e.setIdVehicule(rs.getString("id_vehicule"));
                    resultats.add(e);
                }
            }
        }

        return resultats;
    }

}
