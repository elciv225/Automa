-- Script MySQL : création des tables pour gestion de parc automobile

CREATE TABLE statut_vehicule
(
    id_statut_vehicule VARCHAR(50) PRIMARY KEY,
    libelle            VARCHAR(20) NOT NULL
);

CREATE TABLE service
(
    id_service   VARCHAR(50) PRIMARY KEY,
    libelle      VARCHAR(50) NOT NULL,
    localisation VARCHAR(50)
);

CREATE TABLE fonction
(
    id_fonction VARCHAR(50) PRIMARY KEY,
    libelle     VARCHAR(50) NOT NULL
);

CREATE TABLE categorie_vehicule
(
    id_categorie_vehicule VARCHAR(50) PRIMARY KEY,
    libelle               VARCHAR(50) NOT NULL,
    nombre_place          INT         NOT NULL
);

CREATE TABLE vehicule
(
    id_vehicule           VARCHAR(50) PRIMARY KEY,
    num_chassis           VARCHAR(20) NOT NULL,
    immatriculation       VARCHAR(9)  NOT NULL UNIQUE,
    marque                VARCHAR(20) NOT NULL,
    modele                VARCHAR(50) NOT NULL,
    energie               VARCHAR(50),
    date_acquisition      DATE,
    date_amortissement    DATE,
    date_mise_en_service  DATE,
    puissance             VARCHAR(50),
    couleur               VARCHAR(50),
    prix_achat            VARCHAR(50),
    id_categorie_vehicule VARCHAR(15) NOT NULL,
    CONSTRAINT fk_veh_categ
        FOREIGN KEY (id_categorie_vehicule)
            REFERENCES categorie_vehicule (id_categorie_vehicule)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

CREATE TABLE personnel
(
    id_personnel VARCHAR(50) PRIMARY KEY,
    nom          VARCHAR(50)  NOT NULL,
    prenom       VARCHAR(50)  NOT NULL,
    login        VARCHAR(50)  NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    contrat      VARCHAR(50),
    id_fonction  VARCHAR(20)  NOT NULL,
    id_service   VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_pers_fonc
        FOREIGN KEY (id_fonction)
            REFERENCES fonction (id_fonction)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_pers_serv
        FOREIGN KEY (id_service)
            REFERENCES service (id_service)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

CREATE TABLE assurance
(
    id_assurance VARCHAR(50) PRIMARY KEY,
    date_debut   DATE        NOT NULL,
    date_fin     DATE        NOT NULL,
    agence       VARCHAR(50),
    contrat      VARCHAR(50),
    id_vehicule  VARCHAR(15) NOT NULL,
    CONSTRAINT fk_assur_veh
        FOREIGN KEY (id_vehicule)
            REFERENCES vehicule (id_vehicule)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

CREATE TABLE entretien
(
    id_entretien VARCHAR(50) PRIMARY KEY,
    date_entree  DATE        NOT NULL,
    date_sortie  DATE,
    motif        VARCHAR(100),
    observation  TEXT,
    prix         VARCHAR(50),
    lieu         VARCHAR(50),
    id_vehicule  VARCHAR(15) NOT NULL,
    CONSTRAINT fk_entretien_veh
        FOREIGN KEY (id_vehicule)
            REFERENCES vehicule (id_vehicule)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

CREATE TABLE mission
(
    id_mission     VARCHAR(50) PRIMARY KEY,
    date_debut     DATE        NOT NULL,
    date_fin       DATE,
    cout           VARCHAR(50),
    observation    TEXT,
    circuit        VARCHAR(100),
    cout_carburant VARCHAR(50),
    id_vehicule    VARCHAR(15) NOT NULL,
    CONSTRAINT fk_mission_veh
        FOREIGN KEY (id_vehicule)
            REFERENCES vehicule (id_vehicule)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

CREATE TABLE historique_statut
(
    id_statut_vehicule VARCHAR(20) NOT NULL,
    id_vehicule        VARCHAR(15) NOT NULL,
    date_etat          DATE        NOT NULL,
    PRIMARY KEY (id_statut_vehicule, id_vehicule, date_etat),
    CONSTRAINT fk_hist_stat
        FOREIGN KEY (id_statut_vehicule)
            REFERENCES statut_vehicule (id_statut_vehicule)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_hist_veh
        FOREIGN KEY (id_vehicule)
            REFERENCES vehicule (id_vehicule)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

CREATE TABLE attribution_vehicule
(
    id_vehicule      VARCHAR(15) NOT NULL,
    id_personnel     VARCHAR(25) NOT NULL,
    date_attribution DATE        NOT NULL,
    PRIMARY KEY (id_vehicule, id_personnel, date_attribution),
    CONSTRAINT fk_attr_veh
        FOREIGN KEY (id_vehicule)
            REFERENCES vehicule (id_vehicule)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_attr_pers
        FOREIGN KEY (id_personnel)
            REFERENCES personnel (id_personnel)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
);

CREATE TABLE participer_mission
(
    id_personnel VARCHAR(25) NOT NULL,
    id_mission   VARCHAR(25) NOT NULL,
    PRIMARY KEY (id_personnel, id_mission),
    CONSTRAINT fk_part_pers
        FOREIGN KEY (id_personnel)
            REFERENCES personnel (id_personnel)
            ON UPDATE CASCADE
            ON DELETE CASCADE,
    CONSTRAINT fk_part_miss
        FOREIGN KEY (id_mission)
            REFERENCES mission (id_mission)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

CREATE TABLE audit_log
(
    id_log      INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur VARCHAR(50)  NOT NULL,
    action      VARCHAR(100) NOT NULL,
    entite      VARCHAR(50),
    id_entite   VARCHAR(50),
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details     TEXT
);


-- Insertion des données de référence

-- 1) Insère des statuts
INSERT INTO statut_vehicule (id_statut_vehicule, libelle)
VALUES ('STAT_DISPONIBLE', 'Disponible'),
       ('STAT_INDISPONIBLE', 'Indisponible'),
       ('STAT_MISSION', 'Mission'),
       ('STAT_ENTRETIEN', 'Entretien');

-- 2) Insère des services
INSERT INTO service (id_service, libelle, localisation)
VALUES ('SERV_LOGISTIQUE', 'Logistique', 'Abidjan'),
       ('SERV_INFORMATIQUE', 'Informatique', 'Abidjan');

-- 3) Insère des fonctions
INSERT INTO fonction (id_fonction, libelle)
VALUES ('FONC_RESPONSABLELOG', 'Responsable Logistique'),
       ('FONC_ADMIN', 'Administrateur Système'),
       ('FONC_CHAUFFEUR', 'Chauffeur'),
       ('FONC_MECANICIEN', 'Mécanicien');

-- 4) Insère des catégories de véhicules de bases
INSERT INTO categorie_vehicule (id_categorie_vehicule, libelle, nombre_place)
VALUES ('CAT_BERLINE', 'Berline', '5'),
       ('CAT_UTILITAIRE', 'Utilitaire', '2'),
       ('CAT_CAMION', 'Camion', '3'),
       ('CAT_MINIBUS', 'Minibus', '15');

-- Insertion d’un administrateur
INSERT INTO personnel (id_personnel, nom, prenom, login, mot_de_passe, contrat, id_fonction, id_service)
VALUES ('PERS_ADMIN_01',
        'Admin',
        'Systeme',
        'admin',
        'admin123', -- Tu peux plus tard chiffrer ce mot de passe
        'CDI',
        'FONC_ADMIN',
        'SERV_INFORMATIQUE');

-- Insertion d’un responsable logistique
INSERT INTO personnel (id_personnel, nom, prenom, login, mot_de_passe, contrat, id_fonction, id_service)
VALUES ('PERS_LOG_01',
        'Responsable',
        'Logistique',
        'resplog',
        'log123', -- Pareil ici, à chiffrer en production
        'CDI',
        'FONC_RESPONSABLELOG',
        'SERV_LOGISTIQUE');

-- Insertion de véhicules
INSERT INTO vehicule (id_vehicule, num_chassis, immatriculation, marque, modele, energie,
                      date_acquisition, date_amortissement, date_mise_en_service,
                      puissance, couleur, prix_achat, id_categorie_vehicule)
VALUES ('VEH_AB1234CD', 'CHS123456789', 'AB1234CD', 'Toyota', 'Corolla', 'Essence',
        '2022-03-15', '2027-03-15', '2022-04-01', '90 CV', 'Gris', '10 000 000 FCFA', 'CAT_BERLINE'),

       ('VEH_CD5678EF', 'CHS987654321', 'CD5678EF', 'Renault', 'Kangoo', 'Diesel',
        '2021-06-10', '2026-06-10', '2021-07-01', '70 CV', 'Blanc', '8 500 000 FCFA', 'CAT_UTILITAIRE'),

       ('VEH_EF9012GH', 'CHS456789123', 'EF9012GH', 'Hyundai', 'HD78', 'Diesel',
        '2020-01-20', '2025-01-20', '2020-02-10', '150 CV', 'Bleu', '18 000 000 FCFA', 'CAT_CAMION'),

       ('VEH_GH3456IJ', 'CHS321654987', 'GH3456IJ', 'Ford', 'Transit', 'Diesel',
        '2023-02-05', '2028-02-05', '2023-03-01', '130 CV', 'Noir', '12 000 000 FCFA', 'CAT_MINIBUS');

-- Insertion d’assurances pour les véhicules
INSERT INTO assurance (id_assurance, date_debut, date_fin, agence, contrat, id_vehicule)
VALUES
    ('ASSU_NSIA_VEH_AB1234CD_20230101_20231231', '2023-01-01', '2023-12-31', 'NSIA', 'Tous Risques', 'VEH_AB1234CD'),

    ('ASSU_SUNU_VEH_CD5678EF_20230401_20240331', '2023-04-01', '2024-03-31', 'SUNU', 'RC', 'VEH_CD5678EF'),

    ('ASSU_AXA_VEH_EF9012GH_20230115_20240114', '2023-01-15', '2024-01-14', 'AXA', 'Intermédiaire', 'VEH_EF9012GH'),

    ('ASSU_ALLIANZ_VEH_GH3456IJ_20230710_20240709', '2023-07-10', '2024-07-09', 'ALLIANZ', 'Tous Risques', 'VEH_GH3456IJ');
