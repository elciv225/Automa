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
                Personnel personnel = new Personnel();
                personnel.setIdPersonnel(rs.getString("id_personnel"));
                personnel.setNom(rs.getString("nom"));
                personnel.setPrenom(rs.getString("prenom"));
                personnel.setLogin(rs.getString("login"));
                personnel.setMotDePasse(rs.getString("mot_de_passe"));
                personnel.setContrat(rs.getString("contrat"));
                personnel.setIdFonction(rs.getString("id_fonction"));
                personnel.setIdService(rs.getString("id_service"));
                personnel.setEmail(rs.getString("email"));
                personnel.setTelephone(rs.getString("telephone"));
                personnels.add(personnel);
            }
        }
        return personnels;
    }

    /**
     * Ajoute un nouveau personnel dans la base de données.
     */
    public void ajouterPersonnel(Personnel personnel) throws SQLException {
        String sql = "INSERT INTO personnel (id_personnel, nom, prenom, login, mot_de_passe, contrat, id_fonction, id_service, email, telephone) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getIdPersonnel());
            stmt.setString(2, personnel.getNom());
            stmt.setString(3, personnel.getPrenom());

            // Utilisation du login généré automatiquement
            stmt.setString(4, personnel.getLogin());

            // Utilisation du mot de passe par défaut
            stmt.setString(5, personnel.getMotDePasse());

            stmt.setString(6, personnel.getContrat());
            stmt.setString(7, personnel.getIdFonction());
            stmt.setString(8, personnel.getIdService());
            stmt.setString(9, personnel.getEmail());
            stmt.setString(10, personnel.getTelephone());

            stmt.executeUpdate();
        }
    }

    /**
     * Supprime un personnel par son identifiant.
     */
    public void supprimerPersonnel(String idPersonnel) throws SQLException {
        String sql = "DELETE FROM personnel WHERE id_personnel = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idPersonnel);
            stmt.executeUpdate();
        }
    }

    /**
     * Vérifie si un login existe déjà dans la base de données.
     */
    public boolean loginExiste(String login) throws SQLException {
        String sql = "SELECT COUNT(*) FROM personnel WHERE login = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Récupère un utilisateur par son login.
     */
    public Personnel getPersonnelByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM personnel WHERE login = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Personnel personnel = new Personnel();
                    personnel.setIdPersonnel(rs.getString("id_personnel"));
                    personnel.setNom(rs.getString("nom"));
                    personnel.setPrenom(rs.getString("prenom"));
                    personnel.setLogin(rs.getString("login"));
                    personnel.setMotDePasse(rs.getString("mot_de_passe"));
                    personnel.setContrat(rs.getString("contrat"));
                    personnel.setIdFonction(rs.getString("id_fonction"));
                    personnel.setIdService(rs.getString("id_service"));
                    personnel.setEmail(rs.getString("email"));
                    personnel.setTelephone(rs.getString("telephone"));
                    return personnel;
                }
            }
        }
        return null;
    }

    /**
     * Met à jour les informations personnelles d'un utilisateur.
     */
    public void updatePersonnel(Personnel personnel) throws SQLException {
        String sql = "UPDATE personnel SET nom = ?, prenom = ?, email = ?, telephone = ? WHERE id_personnel = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getNom());
            stmt.setString(2, personnel.getPrenom());
            stmt.setString(3, personnel.getEmail());
            stmt.setString(4, personnel.getTelephone());
            stmt.setString(5, personnel.getIdPersonnel());

            stmt.executeUpdate();
        }
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     */
    public void updateMotDePasse(Personnel personnel) throws SQLException {
        String sql = "UPDATE personnel SET mot_de_passe = ? WHERE id_personnel = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, personnel.getMotDePasse());
            stmt.setString(2, personnel.getIdPersonnel());

            stmt.executeUpdate();
        }
    }

}
