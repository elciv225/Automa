package logbo.assy.automa.dao;

import logbo.assy.automa.models.CategorieVehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CategorieVehiculeDAO {

    private static final Logger LOGGER = Logger.getLogger(CategorieVehiculeDAO.class.getName());
    private final Database db;

    public CategorieVehiculeDAO() throws SQLException {
        LOGGER.info("Initialisation de CategorieVehiculeDAO");
        this.db = new Database();
    }

    /**
     * Récupère toutes les catégories depuis la BDD.
     */
    public List<CategorieVehicule> getAllCategories() throws SQLException {
        LOGGER.info("Chargement de toutes les catégories de véhicules");
        List<CategorieVehicule> categories = new ArrayList<>();
        String sql = "SELECT id_categorie_vehicule, libelle, nombre_place FROM categorie_vehicule";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                CategorieVehicule cat = new CategorieVehicule();
                cat.setIdCategorie(rs.getString("id_categorie_vehicule"));
                cat.setLibelle(      rs.getString("libelle"));
                cat.setNombrePlace(  rs.getString("nombre_place"));
                categories.add(cat);
                LOGGER.finer("Catégorie chargée => " + cat.getIdCategorie() + " | " + cat.getLibelle());
            }
            LOGGER.info("Nombre total de catégories récupérées : " + categories.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des catégories", e);
            throw e;
        }

        return categories;
    }

    /**
     * Ajoute une nouvelle catégorie.
     */
    public void addCategory(CategorieVehicule category) throws SQLException {
        LOGGER.info("Ajout d'une nouvelle catégorie : " + category.getLibelle());
        String sql = "INSERT INTO categorie_vehicule (id_categorie_vehicule, libelle, nombre_place) VALUES (?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, category.getIdCategorie());
            stmt.setString(2, category.getLibelle());
            stmt.setString(3, category.getNombrePlace());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la catégorie", e);
            throw e;
        }
    }

    /**
     * Met à jour une catégorie existante.
     */
    public void updateCategory(CategorieVehicule category) throws SQLException {
        LOGGER.info("Mise à jour de la catégorie : " + category.getIdCategorie());
        String sql = "UPDATE categorie_vehicule SET libelle = ?, nombre_place = ? WHERE id_categorie_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, category.getLibelle());
            stmt.setString(2, category.getNombrePlace());
            stmt.setString(3, category.getIdCategorie());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la catégorie", e);
            throw e;
        }
    }

    /**
     * Supprime une catégorie par son identifiant.
     */
    public void deleteCategory(String categoryId) throws SQLException {
        LOGGER.info("Suppression de la catégorie id=" + categoryId);
        String sql = "DELETE FROM categorie_vehicule WHERE id_categorie_vehicule = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, categoryId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la catégorie", e);
            throw e;
        }
    }
}
