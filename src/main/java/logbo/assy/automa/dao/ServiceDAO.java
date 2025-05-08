package logbo.assy.automa.dao;

import logbo.assy.automa.models.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceDAO {

    private static final Logger LOGGER = Logger.getLogger(ServiceDAO.class.getName());
    private final Database db;

    public ServiceDAO() throws SQLException {
        LOGGER.info("Initialisation de ServiceDAO");
        this.db = new Database();
    }

    /**
     * Récupère tous les services depuis la BDD.
     */
    public List<Service> getAllServices() throws SQLException {
        LOGGER.info("Chargement de tous les services");
        List<Service> services = new ArrayList<>();
        String sql = "SELECT id_service, libelle, localisation FROM service";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Service s = new Service();
                s.setIdService(    rs.getString("id_service") );
                s.setLibelle(      rs.getString("libelle") );
                s.setlocalisation( rs.getString("localisation") );
                services.add(s);
                LOGGER.finer("Service chargé => "
                             + s.getIdService()
                             + " | "
                             + s.getLibelle());
            }
            LOGGER.info("Nombre total de services récupérés : " + services.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des services", e);
            throw e;
        }

        return services;
    }

    /**
     * Ajoute un nouveau service.
     */
    public void addService(Service service) throws SQLException {
        LOGGER.info("Ajout d'un nouveau service : " + service.getLibelle());
        String sql = "INSERT INTO service (id_service, libelle, localisation) VALUES (?, ?, ?)";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, service.getIdService());
            stmt.setString(2, service.getLibelle());
            stmt.setString(3, service.getlocalisation());
            int updated = stmt.executeUpdate();
            LOGGER.info("Insertion terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout du service", e);
            throw e;
        }
    }

    /**
     * Met à jour un service existant.
     */
    public void updateService(Service service) throws SQLException {
        LOGGER.info("Mise à jour du service : " + service.getIdService());
        String sql = "UPDATE service SET libelle = ?, localisation = ? WHERE id_service = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, service.getLibelle());
            stmt.setString(2, service.getlocalisation());
            stmt.setString(3, service.getIdService());
            int updated = stmt.executeUpdate();
            LOGGER.info("Mise à jour terminée, lignes affectées : " + updated);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour du service", e);
            throw e;
        }
    }

    /**
     * Supprime un service par son identifiant.
     */
    public void deleteService(String serviceId) throws SQLException {
        LOGGER.info("Suppression du service id=" + serviceId);
        String sql = "DELETE FROM service WHERE id_service = ?";
        LOGGER.fine("Requête SQL => " + sql);

        try (
            Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, serviceId);
            int deleted = stmt.executeUpdate();
            LOGGER.info("Suppression terminée, lignes affectées : " + deleted);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression du service", e);
            throw e;
        }
    }

    public Service getServiceById(String idService) throws SQLException {
        String sql = "SELECT * FROM service WHERE id_service = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idService);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Service service = new Service();
                    service.setIdService(rs.getString("id_service"));
                    service.setLibelle(rs.getString("libelle"));
                    service.setlocalisation(rs.getString("localisation"));
                    return service;
                }
            }
        }
        return null;
    }
}
