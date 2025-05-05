package logbo.assy.automa.dao;

import logbo.assy.automa.models.Personnel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PersonnelDAO {

    private final Database db;

    public PersonnelDAO() throws SQLException {
        this.db = new Database();
    }

    public Personnel connexion(String login, String password) throws SQLException {
        // On vérifie d'abords si le login exite
        String sqlLogin = "SELECT * FROM personnel WHERE login = ?";
        try (PreparedStatement psLogin = db.getConnection().prepareStatement(sqlLogin)) {
            psLogin.setString(1, login);
            try (ResultSet rs = psLogin.executeQuery()) {
                if (!rs.next()) {
                    // pas d’utilisateur avec ce login
                    throw new SQLException("Login inconnu");
                }

                // 2) On compare le mot de passe
                String dbPassword = rs.getString("mot_de_passe");
                if (!dbPassword.equals(password)) {
                    // login correct, mais mot de passe ne correspond pas
                    throw new SQLException("Mot de passe incorrect");
                }

                // 3) Construire l’objet Personnel et le retourner
                Personnel personnel = new Personnel(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("contrat"),
                        rs.getString("id_fonction"),
                        rs.getString("id_service")
                );
                personnel.setIdPersonnel(rs.getString("id_personnel"));
                return personnel;
            }
        }
    }

    public List<Personnel> getAllPersonnel() throws SQLException {
        List<Personnel> personnels = new ArrayList<>();
        String sql = "SELECT * FROM personnel";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Personnel personnel = new Personnel(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("contrat"),
                        rs.getString("id_fonction"),
                        rs.getString("id_service")
                );
                personnel.setIdPersonnel(rs.getString("id_personnel"));
                personnels.add(personnel);
            }
        }
        return personnels;
    }

}
