-- Script MySQL : création des tables pour gestion de parc automobile

CREATE TABLE statut_vehicule
(
    id_statut_vehicule VARCHAR(20) PRIMARY KEY,
    libelle            VARCHAR(20) NOT NULL
);

CREATE TABLE service
(
    id_service   VARCHAR(20) PRIMARY KEY,
    libelle      VARCHAR(50) NOT NULL,
    localisation VARCHAR(50)
);

CREATE TABLE fonction
(
    id_fonction VARCHAR(20) PRIMARY KEY,
    libelle     VARCHAR(50) NOT NULL
);

CREATE TABLE categorie_vehicule
(
    id_categorie_vehicule VARCHAR(25) PRIMARY KEY,
    libelle               VARCHAR(50) NOT NULL,
    nombre_place          INT         NOT NULL
);

CREATE TABLE vehicule
(
    id_vehicule           VARCHAR(25) PRIMARY KEY,
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
    id_personnel VARCHAR(25) PRIMARY KEY,
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
    id_assurance VARCHAR(25) PRIMARY KEY,
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
    id_entretien VARCHAR(25) PRIMARY KEY,
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
    id_mission     VARCHAR(25) PRIMARY KEY,
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

-- Insertion des données de référence

-- 1) Insère des statuts
INSERT INTO statut_vehicule (id_statut_vehicule, libelle)
VALUES ('STAT_DISPONIBLE', 'Disponible'),
       ('STAT_INDISPONIBLE', 'Indisponible'),
       ('STAT_MISSION', 'Mission'),
       ('STAT_ENTRETIEN', 'Entretien');

-- 2) Insère des services
INSERT INTO service (id_service, libelle, localisation) VALUES
  ('SERV_LOGISTIQUE', 'Logistique', 'Abidjan'),
  ('SERV_INFORMATIQUE', 'Informatique', 'Abidjan');

-- 3) Insère des fonctions
INSERT INTO fonction (id_fonction, libelle) VALUES
  ('FONC_CHAUFFEUR', 'Chauffeur'),
  ('FONC_MECANICIEN', 'Mécanicien');