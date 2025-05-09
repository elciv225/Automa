package logbo.assy.automa.dao;

import logbo.assy.automa.models.StatistiqueGraphique;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO pour les statistiques et les coûts du tableau de bord
 */
public class StatistiqueDAO {
    private static final Logger LOGGER = Logger.getLogger(StatistiqueDAO.class.getName());
    private final Database db;

    public StatistiqueDAO() throws SQLException {
        this.db = new Database();
    }

    /**
     * Récupère la distribution des véhicules par statut actuel
     */
    public Map<String, Integer> getVehiculeParStatut() throws SQLException {
        LOGGER.info("Récupération de la distribution des véhicules par statut");
        Map<String, Integer> distribution = new HashMap<>();

        // SQL pour obtenir le statut le plus récent de chaque véhicule
        String sql = """
                SELECT sv.libelle, COUNT(*) as count 
                FROM (
                    SELECT hs.id_vehicule, hs.id_statut_vehicule
                    FROM historique_statut hs
                    JOIN (
                        SELECT id_vehicule, MAX(date_etat) as date_recente
                        FROM historique_statut
                        GROUP BY id_vehicule
                    ) recent 
                    ON hs.id_vehicule = recent.id_vehicule 
                    AND hs.date_etat = recent.date_recente
                ) dernier_statut
                JOIN statut_vehicule sv ON dernier_statut.id_statut_vehicule = sv.id_statut_vehicule
                GROUP BY sv.libelle
                """;

        try (
                Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                distribution.put(rs.getString("libelle"), rs.getInt("count"));
            }

            LOGGER.info("Distribution des véhicules par statut récupérée avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de la distribution des véhicules par statut", e);
            throw e;
        }

        return distribution;
    }

    /**
     * Récupère des statistiques sur les assurances
     */
    public Map<String, Object> getStatistiquesAssurances() throws SQLException {
        LOGGER.info("Récupération des statistiques sur les assurances");
        Map<String, Object> stats = new HashMap<>();

        // Nombre d'assurances actives
        String sqlActives = "SELECT COUNT(*) FROM assurance WHERE date_fin >= CURRENT_DATE";

        // Assurances expirant dans les 30 prochains jours
        String sqlExpirantBientot = """
                SELECT COUNT(*) FROM assurance 
                WHERE date_fin >= CURRENT_DATE 
                AND date_fin <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
                """;

        // Coût total des assurances actives
        String sqlCoutTotal = """
                SELECT SUM(CAST(REPLACE(REPLACE(prix, ' ', ''), 'FCFA', '') AS DECIMAL)) 
                FROM assurance 
                WHERE date_fin >= CURRENT_DATE
                """;

        // Distribution par agence d'assurance
        String sqlParAgence = """
                SELECT agence, COUNT(*) as count 
                FROM assurance 
                WHERE date_fin >= CURRENT_DATE 
                GROUP BY agence
                """;

        try (Connection conn = db.getConnection()) {
            // Assurances actives
            try (PreparedStatement stmt = conn.prepareStatement(sqlActives);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("assurancesActives", rs.getInt(1));
                }
            }

            // Assurances expirant bientôt
            try (PreparedStatement stmt = conn.prepareStatement(sqlExpirantBientot);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("assurancesExpirantBientot", rs.getInt(1));
                }
            }

            // Coût total
            try (PreparedStatement stmt = conn.prepareStatement(sqlCoutTotal);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("coutTotalAssurances", rs.getDouble(1));
                }
            }

            // Distribution par agence
            Map<String, Integer> distributionAgence = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlParAgence);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributionAgence.put(rs.getString("agence"), rs.getInt("count"));
                }
            }
            stats.put("distributionParAgence", distributionAgence);

            LOGGER.info("Statistiques sur les assurances récupérées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des statistiques sur les assurances", e);
            throw e;
        }

        return stats;
    }

    /**
     * Récupère les statistiques sur les missions et entretiens
     */
    public Map<String, Object> getStatistiquesOperations() throws SQLException {
        LOGGER.info("Récupération des statistiques sur les opérations (missions et entretiens)");
        Map<String, Object> stats = new HashMap<>();

        // Missions en cours
        String sqlMissionsEnCours = """
                SELECT COUNT(*) FROM mission 
                WHERE date_debut <= CURRENT_DATE 
                AND (date_fin IS NULL OR date_fin >= CURRENT_DATE)
                """;

        // Véhicules en entretien
        String sqlEntretiens = """
                SELECT COUNT(*) FROM entretien
                WHERE date_entree <= CURRENT_DATE
                AND (date_sortie IS NULL OR date_sortie >= CURRENT_DATE)
                """;

        // Coût total des entretiens sur l'année en cours
        String sqlCoutEntretiens = """
                SELECT COALESCE(SUM(CAST(REPLACE(REPLACE(prix, ' ', ''), 'FCFA', '') AS DECIMAL)), 0) 
                FROM entretien
                WHERE YEAR(date_entree) = YEAR(CURRENT_DATE)
                """;

        // Coût total des missions sur l'année en cours
        String sqlCoutMissions = """
                SELECT COALESCE(SUM(CAST(REPLACE(REPLACE(cout, ' ', ''), 'FCFA', '') AS DECIMAL)), 0) 
                FROM mission
                WHERE YEAR(date_debut) = YEAR(CURRENT_DATE)
                """;

        // Coût total du carburant sur l'année en cours
        String sqlCoutCarburant = """
                SELECT COALESCE(SUM(CAST(REPLACE(REPLACE(cout_carburant, ' ', ''), 'FCFA', '') AS DECIMAL)), 0) 
                FROM mission
                WHERE YEAR(date_debut) = YEAR(CURRENT_DATE)
                """;

        try (Connection conn = db.getConnection()) {
            // Missions en cours
            try (PreparedStatement stmt = conn.prepareStatement(sqlMissionsEnCours);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("missionsEnCours", rs.getInt(1));
                }
            }

            // Véhicules en entretien
            try (PreparedStatement stmt = conn.prepareStatement(sqlEntretiens);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("vehiculesEnEntretien", rs.getInt(1));
                }
            }

            // Coût entretiens
            try (PreparedStatement stmt = conn.prepareStatement(sqlCoutEntretiens);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("coutEntretiensAnnuels", rs.getDouble(1));
                }
            }

            // Coût missions
            try (PreparedStatement stmt = conn.prepareStatement(sqlCoutMissions);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("coutMissionsAnnuels", rs.getDouble(1));
                }
            }

            // Coût carburant
            try (PreparedStatement stmt = conn.prepareStatement(sqlCoutCarburant);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("coutCarburantAnnuel", rs.getDouble(1));
                }
            }

            LOGGER.info("Statistiques sur les opérations récupérées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des statistiques sur les opérations", e);
            throw e;
        }

        return stats;
    }

    /**
     * Récupère les attributions de véhicules et leurs paiements
     */
    public Map<String, Object> getStatistiquesAttributions() throws SQLException {
        LOGGER.info("Récupération des statistiques sur les attributions de véhicules");
        Map<String, Object> stats = new HashMap<>();

        // Nombre total d'attributions
        String sqlTotalAttributions = "SELECT COUNT(*) FROM attribution_vehicule";

        // Montant total des attributions
        String sqlMontantTotal = """
                SELECT COALESCE(SUM(CAST(REPLACE(REPLACE(montant_total, ' ', ''), 'FCFA', '') AS DECIMAL)), 0) 
                FROM attribution_vehicule
                """;

        // Montant total des paiements effectués
        String sqlMontantPaye = """
                SELECT COALESCE(SUM(CAST(REPLACE(REPLACE(montant_verse, ' ', ''), 'FCFA', '') AS DECIMAL)), 0) 
                FROM paiement_attribution
                """;

        try (Connection conn = db.getConnection()) {
            // Total attributions
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotalAttributions);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("totalAttributions", rs.getInt(1));
                }
            }

            // Montant total
            try (PreparedStatement stmt = conn.prepareStatement(sqlMontantTotal);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("montantTotalAttributions", rs.getDouble(1));
                }
            }

            // Montant payé
            try (PreparedStatement stmt = conn.prepareStatement(sqlMontantPaye);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("montantTotalPaye", rs.getDouble(1));
                }
            }

            // Calcul du pourcentage de recouvrement
            double montantTotal = (double) stats.getOrDefault("montantTotalAttributions", 0.0);
            double montantPaye = (double) stats.getOrDefault("montantTotalPaye", 0.0);

            if (montantTotal > 0) {
                stats.put("pourcentageRecouvrement", (montantPaye / montantTotal) * 100);
            } else {
                stats.put("pourcentageRecouvrement", 0.0);
            }

            LOGGER.info("Statistiques sur les attributions récupérées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des statistiques sur les attributions", e);
            throw e;
        }

        return stats;
    }

    /**
     * Récupère les données pour le graphique des coûts d'entretien et de mission par mois
     */
    public List<StatistiqueGraphique> getDonneesGraphiqueCouts(int nombreMois) throws SQLException {
        LOGGER.info("Récupération des données pour le graphique des coûts");
        List<StatistiqueGraphique> resultats = new ArrayList<>();

        // Formatter pour les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // Calculer la période de début
        LocalDate dateDebut = LocalDate.now().minusMonths(nombreMois - 1).withDayOfMonth(1);

        // Requête SQL pour les coûts d'entretien par mois
        String sqlEntretien = """
                SELECT 
                    DATE_FORMAT(date_entree, '%Y-%m') AS mois,
                    SUM(CAST(REPLACE(REPLACE(prix, ' ', ''), 'FCFA', '') AS DECIMAL)) AS total
                FROM entretien
                WHERE date_entree >= ?
                GROUP BY DATE_FORMAT(date_entree, '%Y-%m')
                ORDER BY mois
                """;

        // Requête SQL pour les coûts de mission par mois
        String sqlMission = """
                SELECT 
                    DATE_FORMAT(date_debut, '%Y-%m') AS mois,
                    SUM(CAST(REPLACE(REPLACE(cout, ' ', ''), 'FCFA', '') AS DECIMAL)) AS total
                FROM mission
                WHERE date_debut >= ?
                GROUP BY DATE_FORMAT(date_debut, '%Y-%m')
                ORDER BY mois
                """;

        // Requête SQL pour les coûts de carburant par mois
        String sqlCarburant = """
                SELECT 
                    DATE_FORMAT(date_debut, '%Y-%m') AS mois,
                    SUM(CAST(REPLACE(REPLACE(cout_carburant, ' ', ''), 'FCFA', '') AS DECIMAL)) AS total
                FROM mission
                WHERE date_debut >= ?
                GROUP BY DATE_FORMAT(date_debut, '%Y-%m')
                ORDER BY mois
                """;

        try (Connection conn = db.getConnection()) {
            // Initialiser les données pour chaque mois
            for (int i = 0; i < nombreMois; i++) {
                YearMonth yearMonth = YearMonth.from(dateDebut.plusMonths(i));
                String moisKey = yearMonth.format(formatter);

                StatistiqueGraphique stat = new StatistiqueGraphique();
                stat.setMois(moisKey);
                stat.setCoutEntretien(0.0);
                stat.setCoutMission(0.0);
                stat.setCoutCarburant(0.0);

                resultats.add(stat);
            }

            // Carte pour accéder facilement aux données par mois
            Map<String, StatistiqueGraphique> mapResultats = new HashMap<>();
            for (StatistiqueGraphique stat : resultats) {
                mapResultats.put(stat.getMois(), stat);
            }

            // Coûts d'entretien
            try (PreparedStatement stmt = conn.prepareStatement(sqlEntretien)) {
                stmt.setDate(1, java.sql.Date.valueOf(dateDebut));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String mois = rs.getString("mois");
                        double total = rs.getDouble("total");

                        if (mapResultats.containsKey(mois)) {
                            mapResultats.get(mois).setCoutEntretien(total);
                        }
                    }
                }
            }

            // Coûts de mission
            try (PreparedStatement stmt = conn.prepareStatement(sqlMission)) {
                stmt.setDate(1, java.sql.Date.valueOf(dateDebut));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String mois = rs.getString("mois");
                        double total = rs.getDouble("total");

                        if (mapResultats.containsKey(mois)) {
                            mapResultats.get(mois).setCoutMission(total);
                        }
                    }
                }
            }

            // Coûts de carburant
            try (PreparedStatement stmt = conn.prepareStatement(sqlCarburant)) {
                stmt.setDate(1, java.sql.Date.valueOf(dateDebut));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String mois = rs.getString("mois");
                        double total = rs.getDouble("total");

                        if (mapResultats.containsKey(mois)) {
                            mapResultats.get(mois).setCoutCarburant(total);
                        }
                    }
                }
            }

            LOGGER.info("Données pour le graphique des coûts récupérées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des données pour le graphique des coûts", e);
            throw e;
        }

        return resultats;
    }

    /**
     * Récupère les alertes importantes du système
     */
    public List<String> getAlertes() throws SQLException {
        LOGGER.info("Récupération des alertes système");
        List<String> alertes = new ArrayList<>();

        // Assurances expirant dans les 30 prochains jours
        String sqlAssurances = """
                SELECT COUNT(*) as count, MIN(DATEDIFF(date_fin, CURRENT_DATE)) as jours_min
                FROM assurance 
                WHERE date_fin >= CURRENT_DATE 
                AND date_fin <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
                """;

        // Véhicules en entretien depuis plus de 7 jours
        String sqlEntretienLong = """
                SELECT COUNT(*) as count
                FROM entretien
                WHERE date_entree <= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
                AND (date_sortie IS NULL OR date_sortie >= CURRENT_DATE)
                """;

        // Véhicules sans assurance valide
        String sqlSansAssurance = """
                SELECT COUNT(*) as count
                FROM vehicule v
                WHERE NOT EXISTS (
                    SELECT 1 FROM assurance a 
                    WHERE a.id_vehicule = v.id_vehicule 
                    AND a.date_fin >= CURRENT_DATE
                )
                """;

        try (Connection conn = db.getConnection()) {
            // Assurances expirant bientôt
            try (PreparedStatement stmt = conn.prepareStatement(sqlAssurances);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("count") > 0) {
                    int count = rs.getInt("count");
                    int joursMin = rs.getInt("jours_min");

                    if (joursMin <= 7) {
                        alertes.add("⚠️ URGENT: " + count + " assurance(s) expirent dans moins de 7 jours");
                    } else {
                        alertes.add("⚠️ " + count + " assurance(s) expirent dans les 30 prochains jours");
                    }
                }
            }

            // Entretiens longs
            try (PreparedStatement stmt = conn.prepareStatement(sqlEntretienLong);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("count") > 0) {
                    int count = rs.getInt("count");
                    alertes.add("🔧 " + count + " véhicule(s) en entretien depuis plus de 7 jours");
                }
            }

            // Véhicules sans assurance
            try (PreparedStatement stmt = conn.prepareStatement(sqlSansAssurance);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("count") > 0) {
                    int count = rs.getInt("count");
                    alertes.add("🚨 " + count + " véhicule(s) sans assurance valide");
                }
            }

            LOGGER.info("Alertes système récupérées avec succès: " + alertes.size() + " alerte(s)");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des alertes système", e);
            throw e;
        }

        return alertes;
    }

    /**
     * Récupère des statistiques sur les personnels
     */
    public Map<String, Object> getStatistiquesPersonnel() throws SQLException {
        LOGGER.info("Récupération des statistiques sur le personnel");
        Map<String, Object> stats = new HashMap<>();

        // Distribution du personnel par service
        String sqlParService = """
                SELECT s.libelle, COUNT(*) as count
                FROM personnel p
                JOIN service s ON p.id_service = s.id_service
                GROUP BY s.libelle
                """;

        // Distribution du personnel par fonction
        String sqlParFonction = """
                SELECT f.libelle, COUNT(*) as count
                FROM personnel p
                JOIN fonction f ON p.id_fonction = f.id_fonction
                GROUP BY f.libelle
                """;

        // Personnel avec attributions de véhicules
        String sqlAvecAttribution = """
                SELECT COUNT(DISTINCT id_personnel) as count
                FROM attribution_vehicule
                """;

        try (Connection conn = db.getConnection()) {
            // Distribution par service
            Map<String, Integer> distributionService = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlParService);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributionService.put(rs.getString("libelle"), rs.getInt("count"));
                }
            }
            stats.put("distributionParService", distributionService);

            // Distribution par fonction
            Map<String, Integer> distributionFonction = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlParFonction);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distributionFonction.put(rs.getString("libelle"), rs.getInt("count"));
                }
            }
            stats.put("distributionParFonction", distributionFonction);

            // Personnel avec attributions
            try (PreparedStatement stmt = conn.prepareStatement(sqlAvecAttribution);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("personnelAvecAttributions", rs.getInt("count"));
                }
            }

            LOGGER.info("Statistiques sur le personnel récupérées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des statistiques sur le personnel", e);
            throw e;
        }

        return stats;
    }
}