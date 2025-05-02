package logbo.assy.automa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import logbo.assy.automa.dao.Database;

public class AuditLogger {
    private static final Logger LOGGER = Logger.getLogger(AuditLogger.class.getName());
    public static void log(String utilisateur, String action, String entite, String idEntite, String details) {
        String sql = "INSERT INTO audit_log (utilisateur, action, entite, id_entite, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = new Database().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur);
            stmt.setString(2, action);
            stmt.setString(3, entite);
            stmt.setString(4, idEntite);
            stmt.setString(5, details);

            stmt.executeUpdate();
        } catch (SQLException e) {
           LOGGER.warning("Erreur lors de la piste d'Audit : "+e.getMessage());
        }
    }
}