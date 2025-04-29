package logbo.assy.automa.dao;


import logbo.assy.automa.models.*;

import java.sql.SQLException;
import java.util.List;


public class TestDAO {

    public static void main(String[] args) {
        try {
            // --- CategorieVehiculeDAO ---
            CategorieVehiculeDAO catDao = new CategorieVehiculeDAO();
            List<CategorieVehicule> cats = catDao.getAllCategories();
            System.out.println("Catégories avant ajout : " + cats.size());
            CategorieVehicule catTest = new CategorieVehicule("TestCat", "5");
            catDao.addCategory(catTest);
            System.out.println("Catégories après ajout : " + catDao.getAllCategories().size());
            catDao.deleteCategory(catTest.getIdCategorie());
            System.out.println("Catégories après suppression : " + catDao.getAllCategories().size());
            System.out.println("------------------------------------------------");

            // --- StatutVehiculeDAO ---
            StatutVehiculeDAO statutDao = new StatutVehiculeDAO();
            List<StatutVehicule> statuts = statutDao.getAllStatuts();
            System.out.println("Statuts avant ajout : " + statuts.size());
            StatutVehicule statutTest = new StatutVehicule("En Revision");
            statutDao.addStatut(statutTest);
            System.out.println("Statuts après ajout : " + statutDao.getAllStatuts().size());
            statutDao.deleteStatut(statutTest.getIdStatutVehicule());
            System.out.println("Statuts après suppression : " + statutDao.getAllStatuts().size());
            System.out.println("------------------------------------------------");

            // --- FonctionDAO ---
            FonctionDAO funcDao = new FonctionDAO();
            List<Fonction> funcs = funcDao.getAllFonctions();
            System.out.println("Fonctions avant ajout : " + funcs.size());
            Fonction funcTest = new Fonction("TestFonction");
            funcDao.addFonction(funcTest);
            System.out.println("Fonctions après ajout : " + funcDao.getAllFonctions().size());
            funcDao.deleteFonction(funcTest.getIdFonction());
            System.out.println("Fonctions après suppression : " + funcDao.getAllFonctions().size());
            System.out.println("------------------------------------------------");

            // --- ServiceDAO ---
            ServiceDAO servDao = new ServiceDAO();
            List<Service> services = servDao.getAllServices();
            System.out.println("Services avant ajout : " + services.size());
            Service servTest = new Service("TestService", "Cocody");
            servDao.addService(servTest);
            System.out.println("Services après ajout : " + servDao.getAllServices().size());
            servDao.deleteService(servTest.getIdService());
            System.out.println("Services après suppression : " + servDao.getAllServices().size());
            System.out.println("------------------------------------------------");

            // The following DAOs have FK dependencies. We only test retrieval:
            // --- VehiculeDAO ---
            VehiculeDAO vehDao = new VehiculeDAO();
            System.out.println("Total véhicules : " + vehDao.getTotalVehicules());
            System.out.println("Tous véhicules : " + vehDao.getAllVehicules().size());
            System.out.println("Véhicules page 1 (size 5) : " + vehDao.getVehiculesPagines(1, 5).size());
            System.out.println("Véhicules 'Toyota' : " + vehDao.findVehiculesByMarque("Toyota").size());
            System.out.println("------------------------------------------------");

            // --- PersonnelDAO (connexion) ---
            PersonnelDAO persDao = new PersonnelDAO();
            try {
                Personnel p = persDao.connexion("admin", "admin123");
                System.out.println("Connexion Personnel réussie : " + p.getIdPersonnel());
            } catch (SQLException ex) {
                System.out.println("Connexion Personnel a échoué : " + ex.getMessage());
            }
            System.out.println("------------------------------------------------");

            // --- AssuranceDAO ---
            AssuranceDAO assuDao = new AssuranceDAO();
            System.out.println("Assurances : " + assuDao.getAllAssurances().size());
            System.out.println("------------------------------------------------");

            // --- EntretienDAO ---
            EntretienDAO entDao = new EntretienDAO();
            System.out.println("Entretiens : " + entDao.getAllEntretiens().size());
            System.out.println("------------------------------------------------");

            // --- MissionDAO ---
            MissionDAO misDao = new MissionDAO();
            System.out.println("Missions : " + misDao.getAllMissions().size());
            System.out.println("------------------------------------------------");

            // --- HistoriqueStatutDAO ---
            HistoriqueStatutDAO histDao = new HistoriqueStatutDAO();
            System.out.println("Historiques statut : " + histDao.getAllHistoriques().size());
            System.out.println("------------------------------------------------");

            // --- AttributionVehiculeDAO ---
            AttributionVehiculeDAO attrDao = new AttributionVehiculeDAO();
            System.out.println("Attributions véhicules : " + attrDao.getAllAttributions().size());
            System.out.println("------------------------------------------------");

            // --- ParticiperMissionDAO ---
            ParticiperMissionDAO pmDao = new ParticiperMissionDAO();
            System.out.println("Participations mission : " + pmDao.getAllParticipations().size());
            System.out.println("------------------------------------------------");

            System.out.println("=== Tests DAO terminés ===");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
