package logbo.assy.automa.dao;

import logbo.assy.automa.models.Vehicule;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class VehiculeDAO {
    private static final Logger LOGGER = Logger.getLogger(VehiculeDAO.class.getName());
    private final Database db;

    public VehiculeDAO() throws SQLException {
        this.db = new Database();
    }

    public int getTotalVehicules() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicule";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }


    public void addVehicule(Vehicule vehicule) throws SQLException {
        String sql = "INSERT INTO vehicule (Id_Vehicule, num_chassis, immatriculation, marque, modele, energie, date_acquisition, date_amortissement, date_mise_en_service, puissance, couleur, prix_achat, id_categorie_vehicule) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            String idVehicule = "VEH_" + vehicule.getImmatriculation();
            pstmt.setString(1, idVehicule);
            pstmt.setString(2, vehicule.getNumeroChassis());
            pstmt.setString(3, vehicule.getImmatriculation());
            pstmt.setString(4, vehicule.getMarque());
            pstmt.setString(5, vehicule.getModele());
            pstmt.setString(6, vehicule.getEnergie());
            pstmt.setDate(7, parseDate(vehicule.getDateAchat(), sdf));
            pstmt.setDate(8, parseDate(vehicule.getDateAmmortissement(), sdf));
            pstmt.setDate(9, parseDate(vehicule.getDateMiseEnService(), sdf));
            pstmt.setString(10, vehicule.getPuissance());
            pstmt.setString(11, vehicule.getCouleur());
            pstmt.setString(12, vehicule.getPrixAchat());
            pstmt.setString(13, vehicule.getIdCategorie());

            pstmt.executeUpdate();
            vehicule.setIdVehicule(idVehicule);
        } catch (SQLException e) {
            LOGGER.warning("Erreur lors de l'ajout du véhicule : " + e.getMessage());
            throw new SQLException("Erreur lors de l'ajout du véhicule", e);
        } catch (java.text.ParseException e) {
            LOGGER.warning("Erreur de format de date : " + e.getMessage());
            throw new SQLException("Format de date incorrect", e);
        }
    }

    private java.sql.Date parseDate(String dateStr, SimpleDateFormat sdf) throws java.text.ParseException {
        if (dateStr != null && !dateStr.isEmpty()) {
            return new java.sql.Date(sdf.parse(dateStr).getTime());
        }
        return null;
    }

    public List<Vehicule> getAllVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setIdVehicule(rs.getString("Id_Vehicule"));
                v.setNumeroChassis(rs.getString("num_chassis"));
                v.setImmatriculation(rs.getString("immatriculation"));
                v.setMarque(rs.getString("marque"));
                v.setModele(rs.getString("modele"));
                v.setEnergie(rs.getString("energie"));
                v.setDateAchat(rs.getString("date_acquisition"));
                v.setDateAmmortissement(rs.getString("date_amortissement"));
                v.setDateMiseEnService(rs.getString("date_mise_en_service"));
                v.setPuissance(rs.getString("puissance"));
                v.setCouleur(rs.getString("couleur"));
                v.setPrixAchat(rs.getString("prix_achat"));
                v.setIdCategorie(rs.getString("id_categorie_vehicule"));

                vehicules.add(v);
            }
        }
        return vehicules;
    }

    public List<Vehicule> getVehiculesPagines(int page, int pageSize) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule LIMIT ? OFFSET ?";

        int offset = (page - 1) * pageSize;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setIdVehicule(rs.getString("Id_Vehicule"));
                    v.setNumeroChassis(rs.getString("num_chassis"));
                    v.setImmatriculation(rs.getString("immatriculation"));
                    v.setMarque(rs.getString("marque"));
                    v.setModele(rs.getString("modele"));
                    v.setEnergie(rs.getString("energie"));
                    v.setDateAchat(rs.getString("date_acquisition"));
                    v.setDateAmmortissement(rs.getString("date_amortissement"));
                    v.setDateMiseEnService(rs.getString("date_mise_en_service"));
                    v.setPuissance(rs.getString("puissance"));
                    v.setCouleur(rs.getString("couleur"));
                    v.setPrixAchat(rs.getString("prix_achat"));
                    v.setIdCategorie(rs.getString("id_categorie_vehicule"));

                    vehicules.add(v);
                }
            }
        }
        return vehicules;
    }

    /**
     * Fonction pour les tris
     */
    public List<Vehicule> findVehiculesByMarque(String marque) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE marque LIKE ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + marque + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vehicule v = new Vehicule();
                    v.setIdVehicule(rs.getString("Id_Vehicule"));
                    v.setNumeroChassis(rs.getString("num_chassis"));
                    v.setImmatriculation(rs.getString("immatriculation"));
                    v.setMarque(rs.getString("marque"));
                    v.setModele(rs.getString("modele"));
                    v.setEnergie(rs.getString("energie"));
                    v.setDateAchat(rs.getString("date_acquisition"));
                    v.setDateAmmortissement(rs.getString("date_amortissement"));
                    v.setDateMiseEnService(rs.getString("date_mise_en_service"));
                    v.setPuissance(rs.getString("puissance"));
                    v.setCouleur(rs.getString("couleur"));
                    v.setPrixAchat(rs.getString("prix_achat"));
                    v.setIdCategorie(rs.getString("id_categorie_vehicule"));

                    vehicules.add(v);
                }
            }
        }
        return vehicules;
    }

    /**
     * Fonction pour la mise à jour
     */
    public void updateVehicule(Vehicule vehicule) throws SQLException {
        String sql = "UPDATE vehicule SET num_chassis=?, immatriculation=?, marque=?, modele=?, energie=?, date_acquisition=?, date_amortissement=?, date_mise_en_service=?, puissance=?, couleur=?, prix_achat=?, id_categorie_vehicule=? WHERE Id_Vehicule=?";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, vehicule.getNumeroChassis());
            pstmt.setString(2, vehicule.getImmatriculation());
            pstmt.setString(3, vehicule.getMarque());
            pstmt.setString(4, vehicule.getModele());
            pstmt.setString(5, vehicule.getEnergie());
            pstmt.setDate(6, parseDate(vehicule.getDateAchat(), sdf));
            pstmt.setDate(7, parseDate(vehicule.getDateAmmortissement(), sdf));
            pstmt.setDate(8, parseDate(vehicule.getDateMiseEnService(), sdf));
            pstmt.setString(9, vehicule.getPuissance());
            pstmt.setString(10, vehicule.getCouleur());
            pstmt.setString(11, vehicule.getPrixAchat());
            pstmt.setString(12, vehicule.getIdCategorie());
            pstmt.setString(13, vehicule.getIdVehicule());

            pstmt.executeUpdate();
        } catch (ParseException e) {
            LOGGER.severe("Erreur lors de la mise à jour du véhicule : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public float getAmortissementAnnuel(Vehicule v) {
        try {
            float prix = Float.parseFloat(v.getPrixAchat().replaceAll("\\s+", ""));
            LocalDate dateAchat = LocalDate.parse(v.getDateAchat());
            LocalDate dateFin = LocalDate.parse(v.getDateAmmortissement());
            int annees = Period.between(dateAchat, dateFin).getYears();
            return annees > 0 ? prix / annees : prix;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Vehicule> rechercherVehicules(String motCle) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE " +
                "immatriculation LIKE ? OR marque LIKE ? OR modele LIKE ? OR num_chassis LIKE ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            String like = "%" + motCle + "%";
            for (int i = 1; i <= 4; i++) stmt.setString(i, like);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vehicules.add(mapVehicule(rs));
            }
        }
        return vehicules;
    }

    public List<Vehicule> filtrerVehicules(String champ, String valeur) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE " + champ + " = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, valeur);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                vehicules.add(mapVehicule(rs));
            }
        }
        return vehicules;
    }

    public boolean supprimerVehicule(String idVehicule) throws SQLException {
        String sql = "DELETE FROM vehicule WHERE Id_Vehicule = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, idVehicule);
            return stmt.executeUpdate() > 0;
        }
    }

    private Vehicule mapVehicule(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule();
        v.setIdVehicule(rs.getString("Id_Vehicule"));
        v.setNumeroChassis(rs.getString("num_chassis"));
        v.setImmatriculation(rs.getString("immatriculation"));
        v.setMarque(rs.getString("marque"));
        v.setModele(rs.getString("modele"));
        v.setEnergie(rs.getString("energie"));
        v.setDateAchat(rs.getString("date_acquisition"));
        v.setDateAmmortissement(rs.getString("date_amortissement"));
        v.setDateMiseEnService(rs.getString("date_mise_en_service"));
        v.setPuissance(rs.getString("puissance"));
        v.setCouleur(rs.getString("couleur"));
        v.setPrixAchat(rs.getString("prix_achat"));
        v.setIdCategorie(rs.getString("id_categorie_vehicule"));
        return v;
    }


}
