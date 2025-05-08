package logbo.assy.automa.dao;

import logbo.assy.automa.models.AuditLog;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditLogDAO {
    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());
    private final Database db;

    public AuditLogDAO() throws SQLException {
        this.db = new Database();
    }

    /**
     * Récupère tous les logs d'audit.
     */
    public List<AuditLog> getAllLogs() throws SQLException {
        return getLogs(null, null, null);
    }

    /**
     * Récupère les logs d'audit filtrés par date et mot-clé.
     */
    public List<AuditLog> getLogs(LocalDate dateDebut, LocalDate dateFin, String motCle) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM audit_log WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Ajout des filtres de date si nécessaire
        if (dateDebut != null) {
            sql.append(" AND date_action >= ?");
            params.add(Timestamp.valueOf(dateDebut.atStartOfDay()));
        }

        if (dateFin != null) {
            sql.append(" AND date_action <= ?");
            params.add(Timestamp.valueOf(dateFin.atTime(LocalTime.MAX)));
        }

        // Ajout du filtre de mot-clé si nécessaire
        if (motCle != null && !motCle.isEmpty()) {
            sql.append(" AND (utilisateur LIKE ? OR action LIKE ? OR entite LIKE ? OR details LIKE ?)");
            String pattern = "%" + motCle + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        // Tri par date et heure décroissantes (du plus récent au plus ancien)
        sql.append(" ORDER BY date_action DESC");

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())
        ) {
            // Configuration des paramètres
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setIdLog(rs.getInt("id_log"));
                log.setUtilisateur(rs.getString("utilisateur"));
                log.setAction(rs.getString("action"));
                log.setEntite(rs.getString("entite"));
                log.setIdEntite(rs.getString("id_entite"));

                Timestamp timestamp = rs.getTimestamp("date_action");
                log.setDateAction(timestamp != null ? timestamp.toLocalDateTime() : null);

                log.setDetails(rs.getString("details"));
                logs.add(log);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des logs d'audit", e);
            throw e;
        }

        return logs;
    }
}