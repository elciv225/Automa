package logbo.assy.automa.dao;

import logbo.assy.automa.models.Fonction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FonctionDAO {

    private static final Logger LOGGER = Logger.getLogger(FonctionDAO.class.getName());
    private final Database db;

    public FonctionDAO() throws SQLException {
        LOGGER.info("Initialisation de FonctionDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les fonctions depuis la BDD.
     */
    public List<Fonction> getAllFonctions() throws SQLException {
        LOGGER.info("Chargement de toutes les fonctions");
        List<Fonction> fonctions = new ArrayList<>();
        String sql = "SELECT id_fonction, libelle FROM fonction";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Fonction f = new Fonction();
                f.setIdFonction( rs.getString("id_fonction") );
                f.setLibelle(     rs.getString("libelle") );
                fonctions.add(f);
                LOGGER.finer("Fonction chargée => " + f.getIdFonction() + " | " + f.getLibelle());
            }
            LOGGER.info("Nombre total de fonctions récupérées : " + fonctions.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des fonctions", e);
            throw e;
        }

        return fonctions;
    }

    /**
     * Ajoute une nouvelle fonction.
     */
    public void addFonction(Fonction fonction) throws SQLException {
        LOGGER.info("Ajout d'une nouvelle fonction : " + fonction.getLibelle());
        String sql = "INSERT INTO fonction (id_fonction, libelle) VALUES (?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, fonction.getIdFonction());
            stmt.setString(2, fonction.getLibelle());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la fonction", e);
            throw e;
        }
    }

    /**
     * Met à jour une fonction existante.
     */
    public void updateFonction(Fonction fonction) throws SQLException {
        LOGGER.info("Mise à jour de la fonction : " + fonction.getIdFonction());
        String sql = "UPDATE fonction SET libelle = ? WHERE id_fonction = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, fonction.getLibelle());
            stmt.setString(2, fonction.getIdFonction());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la fonction", e);
            throw e;
        }
    }

    /**
     * Supprime une fonction par son identifiant.
     */
    public void deleteFonction(String fonctionId) throws SQLException {
        LOGGER.info("Suppression de la fonction id=" + fonctionId);
        String sql = "DELETE FROM fonction WHERE id_fonction = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, fonctionId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la fonction", e);
            throw e;
        }
    }
}
