---

## Table des matières

1. [Introduction](#introduction)  
2. [Architecture générale](#architecture-générale)  
3. [Classe utilitaire `Database`](#classe-utilitaire-database)  
4. [Conventions de nommage et de journalisation](#conventions-de-nommage-et-de-journalisation)  
5. [Patron DAO](#patron-dao)  
6. [Documentation détaillée des DAO](#documentation-détaillée-des-dao)  
   - [CategorieVehiculeDAO](#categorievehiculedao)  
   - [StatutVehiculeDAO](#statutvehiculedao)  
   - [ServiceDAO](#servicedao)  
   - [FonctionDAO](#fonctiondao)  
   - [VehiculeDAO](#vehiculedao)  
   - [PersonnelDAO](#personneldao)  
   - [AssuranceDAO](#assurancedao)  
   - [EntretienDAO](#entretiendao)  
   - [MissionDAO](#missiondao)  
   - [HistoriqueStatutDAO](#historiquestatutdao)  
   - [AttributionVehiculeDAO](#attributionvehiculedao)  
   - [ParticiperMissionDAO](#participermissiondao)  
7. [Exemples d’utilisation](#exemples-dutilisation)  
8. [Gestion des erreurs](#gestion-des-erreurs)  
9. [Bonnes pratiques et évolutions possibles](#bonnes-pratiques-et-évolutions-possibles)  

---

## Introduction

Les Data Access Objects (DAO) encapsulent l’accès aux données dans la base MySQL. Chaque DAO est responsable des opérations CRUD (Create, Read, Update, Delete) sur une table spécifique. Ils utilisent la classe utilitaire `Database` pour se connecter à la base et reposent systématiquement sur :

- **`java.sql.Connection`**, **`PreparedStatement`**, **`ResultSet`**  
- **Logging** via `java.util.logging.Logger` pour tracer les étapes et erreurs.  
- Gestion des exceptions SQL pour remonter les erreurs au niveau appelant.

---

## Architecture générale

```
┌────────────────────────────┐      ┌─────────────┐
│     Classe métier (Model)  │<─────│   DAO       │
│  (ex. CategorieVehicule)   │      └─────────────┘
└────────────────────────────┘              │
         ▲         ▲                       ▼
         │         │                 ┌───────────┐
         │         │                 │ Database  │
         │         └────────────────►│  (helper) │
         │                           └───────────┘
         │                                 │
         │                    JDBC URL,     │
         │                    identifiants, Credentials
         ▼                                   │
┌────────────────────────────┐                ▼
│     Base de données        │       MySQL (automa@localhost)
└────────────────────────────┘
```

1. **Model** : classe Java avec attributs + getters/setters + logique de génération d’ID.  
2. **DAO** :  
   - Constructeur : initialise `Database`.  
   - Méthodes CRUD : `getAll…`, `add…`, `update…`, `delete…`.  
   - Chaque méthode prépare une requête SQL, exécute, mappe le résultat sur un Model ou obtient le nombre de lignes affectées.  
3. **Database** : centralise la configuration JDBC (URL, user, mot de passe) et fournit la connexion.

---

## Classe utilitaire `Database`

```java
public class Database {
    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private final String url    = "jdbc:mysql://localhost:3306/automa?useSSL=true&serverTimezone=UTC";
    private final String user   = "votre_user";
    private final String pass   = "votre_mot_de_passe";

    public Database() {
        LOGGER.info("Initialisation de la configuration JDBC");
    }

    public Connection getConnection() throws SQLException {
        LOGGER.info("Connexion à la base de données " + url);
        return DriverManager.getConnection(url, user, pass);
    }
}
```

- **Rôle** : Factoriser la création de `Connection`.  
- **Logging** : indique l’URL utilisée et les erreurs de connexion.

---

## Conventions de nommage et de journalisation

- **Logger** : `private static final Logger LOGGER = Logger.getLogger(NomDeClasse.class.getName());`  
- **Requêtes SQL** :  
  - Toujours en `String sql = "…"; LOGGER.fine("Requête SQL => " + sql);`  
- **Méthodes** :  
  - `getAllXxx()`, `addXxx()`, `updateXxx()`, `deleteXxx()`  
- **Mapping** :  
  - Instanciation du Model, puis `rs.getString("colonne")` → setter.  
- **Gestion des ressources JDBC** :  
  - `try (Connection …; PreparedStatement …; ResultSet …) { … }`  
- **Exceptions** :  
  - On logge en `Level.SEVERE` et on propage l’`SQLException` vers le caller.

---

## Patron DAO

1. **Constructeur**  
   ```java
   public XxxDAO() throws SQLException {
     LOGGER.info("Initialisation de XxxDAO");
     this.db = new Database();
   }
   ```
2. **Read all**  
   ```java
   public List<Xxx> getAllXxx() throws SQLException { … }
   ```
3. **Create**  
   ```java
   public void addXxx(Xxx obj) throws SQLException { … }
   ```
4. **Update**  
   ```java
   public void updateXxx(Xxx obj) throws SQLException { … }
   ```
5. **Delete**  
   ```java
   public void deleteXxx(KeyType id) throws SQLException { … }
   ```

---

## Documentation détaillée des DAO

### CategorieVehiculeDAO

- **Table** : `categorie_vehicule`  
- **Model** : `CategorieVehicule`  
- **Méthodes** :  
  - `List<CategorieVehicule> getAllCategories()`  
  - `void addCategory(CategorieVehicule category)`  
  - `void updateCategory(CategorieVehicule category)`  
  - `void deleteCategory(String categoryId)`  
- **Spécificité** : génère l’ID en prefixant `CAT_`.

### StatutVehiculeDAO

- **Table** : `statut_vehicule`  
- **Model** : `StatutVehicule`  
- **Méthodes** :  
  - `List<StatutVehicule> getAllStatuts()`  
  - `void addStatut(StatutVehicule statut)`  
  - `void updateStatut(StatutVehicule statut)`  
  - `void deleteStatut(String statutId)`  
- **ID** : préfixe `STAT_`.

### ServiceDAO

- **Table** : `service`  
- **Model** : `Service`  
- **Méthodes** :  
  - `List<Service> getAllServices()`  
  - `void addService(Service service)`  
  - `void updateService(Service service)`  
  - `void deleteService(String serviceId)`  
- **ID** : préfixe `SERV_`.

### FonctionDAO

- **Table** : `fonction`  
- **Model** : `Fonction`  
- **Méthodes** :  
  - `List<Fonction> getAllFonctions()`  
  - `void addFonction(Fonction fonction)`  
  - `void updateFonction(Fonction fonction)`  
  - `void deleteFonction(String fonctionId)`  
- **ID** : préfixe `FUNC_`.

### VehiculeDAO

- **Table** : `vehicule`  
- **Model** : `Vehicule`  
- **Méthodes** :  
  - `int getTotalVehicules()`  
  - `void addVehicule(Vehicule vehicule)`  
  - `List<Vehicule> getAllVehicules()`  
  - `List<Vehicule> getVehiculesPagines(int page, int pageSize)`  
  - `List<Vehicule> findVehiculesByMarque(String marque)`  
  - `void updateVehicule(Vehicule vehicule)`  
- **ID** : généré en `VEH_<immatriculation>`.  
- **Date** : conversion `String` → `java.sql.Date` via `SimpleDateFormat`.

### PersonnelDAO

- **Table** : `personnel`  
- **Model** : `Personnel`  
- **Méthodes** :  
  - `Personnel connexion(String login, String password)`  
- **ID** : `PERS_<NOM>_<PRENOM>_<UUID8>`.  
- **Particularité** : authentification via `login` + `mot_de_passe`.

### AssuranceDAO

- **Table** : `assurance`  
- **Model** : `Assurance`  
- **Méthodes** :  
  - `List<Assurance> getAllAssurances()`  
  - `void addAssurance(Assurance assurance)`  
  - `void updateAssurance(Assurance assurance)`  
  - `void deleteAssurance(String assuranceId)`  
- **ID** : `ASSU_<AGENCE>_<ID_VEHICLE>_<DEBUT>_<FIN>`.

### EntretienDAO

- **Table** : `Entretient`  
- **Model** : `Entretien`  
- **Méthodes** :  
  - `List<Entretien> getAllEntretiens()`  
  - `void addEntretien(Entretien entretien)`  
  - `void updateEntretien(Entretien entretien)`  
  - `void deleteEntretien(String entretienId)`  
- **ID** : `ENT_<ID_VEHICLE>_<DEBUT>_<SORTIE>`.

### MissionDAO

- **Table** : `mission`  
- **Model** : `Mission`  
- **Méthodes** :  
  - `List<Mission> getAllMissions()`  
  - `void addMission(Mission mission)`  
  - `void updateMission(Mission mission)`  
  - `void deleteMission(String missionId)`  
- **ID** : `MIS_<ID_VEHICLE>_<DEBUT>_<FIN>`.

### HistoriqueStatutDAO

- **Table** : `historique_statut`  
- **Model** : `HistoriqueStatut`  
- **Méthodes** :  
  - `List<HistoriqueStatut> getAllHistoriques()`  
  - `void addHistorique(HistoriqueStatut hs)`  
  - `void updateHistorique(HistoriqueStatut hs)`  
  - `void deleteHistorique(String statutId, String vehiculeId)`  
- **Clé composées** : `(id_statutvehicule, id_vehicule)`.

### AttributionVehiculeDAO

- **Table** : `attribution_vehicule`  
- **Model** : `AttributionVehicule`  
- **Méthodes** :  
  - `List<AttributionVehicule> getAllAttributions()`  
  - `void addAttribution(AttributionVehicule attribution)`  
  - `void updateAttribution(AttributionVehicule attribution)`  
  - `void deleteAttribution(String vehiculeId, String personnelId)`  
- **Clé composées** : `(id_vehicule, id_personnel)`.

### ParticiperMissionDAO

- **Table** : `participer_mission`  
- **Model** : `ParticiperMission`  
- **Méthodes** :  
  - `List<ParticiperMission> getAllParticipations()`  
  - `void addParticipation(ParticiperMission pm)`  
  - `void deleteParticipation(String personnelId, String missionId)`  
- **Clé composées** : `(id_personnel, id_mission)`.

---

## Exemples d’utilisation

```java
// Instanciation d’un DAO
CategorieVehiculeDAO catDao = new CategorieVehiculeDAO();

// Lecture de toutes les catégories
List<CategorieVehicule> cats = catDao.getAllCategories();

// Ajout d’une catégorie
CategorieVehicule newCat = new CategorieVehicule("Minibus", "12");
catDao.addCategory(newCat);

// Mise à jour
newCat.setLibelle("Minibus VIP");
catDao.updateCategory(newCat);

// Suppression
catDao.deleteCategory(newCat.getIdCategorie());
```

---

## Gestion des erreurs

- Les méthodes jettent toujours `SQLException`.  
- Les erreurs SQL sont journalisées en `Level.SEVERE`.  
- Pour les conversions de date, un `ParseException` est converti en `SQLException`.

---

## Bonnes pratiques et évolutions possibles

1. **Factoriser** le mapping JDBC → POJO (utiliser un `RowMapper` ou mini-ORM).  
2. **Transactions** : envelopper plusieurs opérations dans un même commit/rollback.  
3. **Connection Pooling** : remplacer `DriverManager` par un pool (HikariCP, DBCP).  
4. **Paramétrer** l’URL JDBC, user, pass dans un fichier de configuration externe.  
5. **Tests unitaires** : simuler la base avec H2 et tester chaque DAO isolément.  

---
