-- Script d'insertion de données pour le mois d'avril 2025

-- Nouvelles insertions de personnel
INSERT INTO personnel (id_personnel, nom, prenom, login, mot_de_passe, contrat, id_fonction, id_service, email,
                       telephone)
VALUES ('PERS_CHAUF_01', 'Kouamé', 'Jean', 'jkouame', 'pass123', 'CDI', 'FONC_CHAUFFEUR', 'SERV_LOGISTIQUE',
        'jkouame@automa.ci', '0505060708'),
       ('PERS_CHAUF_02', 'Koné', 'Amadou', 'akone', 'pass123', 'CDI', 'FONC_CHAUFFEUR', 'SERV_LOGISTIQUE',
        'akone@automa.ci', '0708091011'),
       ('PERS_CHAUF_03', 'Traoré', 'Ibrahim', 'itraore', 'pass123', 'CDD', 'FONC_CHAUFFEUR', 'SERV_LOGISTIQUE',
        'itraore@automa.ci', '0607080910'),
       ('PERS_CHAUF_04', 'Diallo', 'Mohamed', 'mdiallo', 'pass123', 'CDD', 'FONC_CHAUFFEUR', 'SERV_LOGISTIQUE',
        'mdiallo@automa.ci', '0102030405'),
       ('PERS_MECA_01', 'Sanogo', 'Paul', 'psanogo', 'pass123', 'CDI', 'FONC_MECANICIEN', 'SERV_LOGISTIQUE',
        'psanogo@automa.ci', '0304050607'),
       ('PERS_MECA_02', 'Bamba', 'Seydou', 'sbamba', 'pass123', 'CDI', 'FONC_MECANICIEN', 'SERV_LOGISTIQUE',
        'sbamba@automa.ci', '0405060708'),
       ('PERS_LOG_02', 'Touré', 'Fatou', 'ftoure', 'pass123', 'CDI', 'FONC_RESPONSABLELOG', 'SERV_LOGISTIQUE',
        'ftoure@automa.ci', '0506070809'),
       ('PERS_ADMIN_02', 'Ouattara', 'Alice', 'aouattara', 'pass123', 'CDI', 'FONC_ADMIN', 'SERV_INFORMATIQUE',
        'aouattara@automa.ci', '0607080910');

-- Ajout de deux nouveaux véhicules en avril 2025
INSERT INTO vehicule (id_vehicule, num_chassis, immatriculation, marque, modele, energie,
                      date_acquisition, date_amortissement, date_mise_en_service,
                      puissance, couleur, prix_achat, id_categorie_vehicule)
VALUES ('VEH_IJ7890KL', 'CHS789012345', 'IJ7890KL', 'Honda', 'CR-V', 'Essence',
        '2025-04-05', '2030-04-05', '2025-04-10', '110 CV', 'Bleu nuit', '15 500 000 FCFA', 'CAT_BERLINE'),
       ('VEH_KL1234MN', 'CHS567890123', 'KL1234MN', 'Mercedes', 'Sprinter', 'Diesel',
        '2025-04-12', '2030-04-12', '2025-04-15', '160 CV', 'Blanc', '22 000 000 FCFA', 'CAT_MINIBUS');

-- Nouvelles assurances pour les véhicules acquis en avril 2025
INSERT INTO assurance (id_assurance, date_debut, date_fin, agence, contrat, prix, id_vehicule)
VALUES ('ASSU_SAHAM_VEH_IJ7890KL_20250410_20260409', '2025-04-10', '2026-04-09', 'SAHAM', 'Tous Risques', '220000',
        'VEH_IJ7890KL'),
       ('ASSU_NSIA_VEH_KL1234MN_20250415_20260414', '2025-04-15', '2026-04-14', 'NSIA', 'Tous Risques', '280000',
        'VEH_KL1234MN');

-- Statuts des véhicules pour avril 2025
INSERT INTO historique_statut (id_statut_vehicule, id_vehicule, date_etat)
VALUES
-- Statuts pour les véhicules existants
('STAT_DISPONIBLE', 'VEH_AB1234CD', '2025-04-01'),
('STAT_MISSION', 'VEH_AB1234CD', '2025-04-05'),
('STAT_DISPONIBLE', 'VEH_AB1234CD', '2025-04-12'),
('STAT_ENTRETIEN', 'VEH_AB1234CD', '2025-04-22'),
('STAT_DISPONIBLE', 'VEH_AB1234CD', '2025-04-25'),

('STAT_DISPONIBLE', 'VEH_CD5678EF', '2025-04-01'),
('STAT_MISSION', 'VEH_CD5678EF', '2025-04-03'),
('STAT_DISPONIBLE', 'VEH_CD5678EF', '2025-04-08'),
('STAT_MISSION', 'VEH_CD5678EF', '2025-04-15'),
('STAT_DISPONIBLE', 'VEH_CD5678EF', '2025-04-20'),

('STAT_ENTRETIEN', 'VEH_EF9012GH', '2025-04-01'),
('STAT_DISPONIBLE', 'VEH_EF9012GH', '2025-04-05'),
('STAT_MISSION', 'VEH_EF9012GH', '2025-04-10'),
('STAT_DISPONIBLE', 'VEH_EF9012GH', '2025-04-18'),

('STAT_DISPONIBLE', 'VEH_GH3456IJ', '2025-04-01'),
('STAT_MISSION', 'VEH_GH3456IJ', '2025-04-08'),
('STAT_DISPONIBLE', 'VEH_GH3456IJ', '2025-04-15'),
('STAT_ENTRETIEN', 'VEH_GH3456IJ', '2025-04-28'),
('STAT_INDISPONIBLE', 'VEH_GH3456IJ', '2025-04-30'),

-- Statuts pour les nouveaux véhicules
('STAT_DISPONIBLE', 'VEH_IJ7890KL', '2025-04-10'),
('STAT_MISSION', 'VEH_IJ7890KL', '2025-04-18'),
('STAT_DISPONIBLE', 'VEH_IJ7890KL', '2025-04-25'),

('STAT_DISPONIBLE', 'VEH_KL1234MN', '2025-04-15'),
('STAT_MISSION', 'VEH_KL1234MN', '2025-04-22'),
('STAT_DISPONIBLE', 'VEH_KL1234MN', '2025-04-29');

-- Entretiens des véhicules en avril 2025
INSERT INTO entretien (id_entretien, date_entree, date_sortie, motif, observation, prix, lieu, id_vehicule)
VALUES ('ENTR_20250401_VEH_EF9012GH', '2025-04-01', '2025-04-05', 'Révision complète',
        'Changement des filtres, vidange et vérification des freins', '350000', 'Garage Central Abidjan',
        'VEH_EF9012GH'),
       ('ENTR_20250422_VEH_AB1234CD', '2025-04-22', '2025-04-25', 'Problème de démarrage',
        'Remplacement du démarreur et de la batterie', '280000', 'Toyota Abidjan', 'VEH_AB1234CD'),
       ('ENTR_20250428_VEH_GH3456IJ', '2025-04-28', '2025-04-30', 'Problème de transmission',
        'Diagnostic en cours, pièces à commander', '150000', 'Ford Abidjan', 'VEH_GH3456IJ');

-- Missions pour avril 2025
INSERT INTO mission (id_mission, date_debut, date_fin, cout, observation, circuit, cout_carburant, id_vehicule)
VALUES ('MISS_20250405_AB1234CD', '2025-04-05', '2025-04-12', '520000', 'Transport du personnel informatique',
        'Abidjan-Yamoussoukro-Bouaké-Abidjan', '150000', 'VEH_AB1234CD'),
       ('MISS_20250403_CD5678EF', '2025-04-03', '2025-04-08', '380000', 'Livraison de matériel',
        'Abidjan-San Pedro-Abidjan', '120000', 'VEH_CD5678EF'),
       ('MISS_20250410_EF9012GH', '2025-04-10', '2025-04-18', '850000', 'Transport de marchandises',
        'Abidjan-Korhogo-Abidjan', '320000', 'VEH_EF9012GH'),
       ('MISS_20250408_GH3456IJ', '2025-04-08', '2025-04-15', '620000', 'Transport du personnel administratif',
        'Abidjan-Man-Abidjan', '250000', 'VEH_GH3456IJ'),
       ('MISS_20250415_CD5678EF', '2025-04-15', '2025-04-20', '420000', 'Livraison urgente de pièces',
        'Abidjan-Daloa-Abidjan', '140000', 'VEH_CD5678EF'),
       ('MISS_20250418_IJ7890KL', '2025-04-18', '2025-04-25', '480000', 'Transport de clients VIP',
        'Abidjan-Grand Bassam-Assinie-Abidjan', '110000', 'VEH_IJ7890KL'),
       ('MISS_20250422_KL1234MN', '2025-04-22', '2025-04-29', '720000', 'Transport d\'équipe pour formation',
        'Abidjan-Yamoussoukro-Abidjan', '180000', 'VEH_KL1234MN');

-- Participation aux missions pour avril 2025
INSERT INTO participer_mission (id_personnel, id_mission)
VALUES ('PERS_CHAUF_01', 'MISS_20250405_AB1234CD'),
       ('PERS_ADMIN_02', 'MISS_20250405_AB1234CD'),
       ('PERS_CHAUF_02', 'MISS_20250403_CD5678EF'),
       ('PERS_LOG_01', 'MISS_20250403_CD5678EF'),
       ('PERS_CHAUF_03', 'MISS_20250410_EF9012GH'),
       ('PERS_CHAUF_04', 'MISS_20250408_GH3456IJ'),
       ('PERS_ADMIN_01', 'MISS_20250408_GH3456IJ'),
       ('PERS_LOG_02', 'MISS_20250408_GH3456IJ'),
       ('PERS_CHAUF_02', 'MISS_20250415_CD5678EF'),
       ('PERS_MECA_01', 'MISS_20250415_CD5678EF'),
       ('PERS_CHAUF_01', 'MISS_20250418_IJ7890KL'),
       ('PERS_LOG_01', 'MISS_20250418_IJ7890KL'),
       ('PERS_CHAUF_04', 'MISS_20250422_KL1234MN'),
       ('PERS_ADMIN_02', 'MISS_20250422_KL1234MN'),
       ('PERS_LOG_02', 'MISS_20250422_KL1234MN');

-- Attribution de véhicule au personnel (avec remboursement)
INSERT INTO attribution_vehicule (id_vehicule, id_personnel, date_attribution, montant_total, date_debut_remboursement)
VALUES ('VEH_AB1234CD', 'PERS_LOG_01', '2025-04-02', '8000000', '2025-05-01'),
       ('VEH_IJ7890KL', 'PERS_ADMIN_02', '2025-04-20', '12000000', '2025-05-01');

-- Paiements pour les attributions de véhicules
INSERT INTO paiement_attribution (id_vehicule, id_personnel, mois_paiement, montant_verse, date_versement)
VALUES ('VEH_AB1234CD', 'PERS_LOG_01', '2025-05-01', '200000', '2025-04-30'),
       ('VEH_IJ7890KL', 'PERS_ADMIN_02', '2025-05-01', '300000', '2025-04-30');

-- Journal d'audit pour les actions d'avril 2025
INSERT INTO audit_log (utilisateur, action, entite, id_entite, date_action, details)
VALUES ('admin', 'Ajout', 'vehicule', 'VEH_IJ7890KL', '2025-04-05 09:15:23', 'Ajout d\'un nouveau véhicule Honda CR-V'),
       ('admin', 'Ajout', 'vehicule', 'VEH_KL1234MN', '2025-04-12 10:30:45',
        'Ajout d\'un nouveau véhicule Mercedes Sprinter'),
       ('resplog', 'Ajout', 'mission', 'MISS_20250405_AB1234CD', '2025-04-04 14:22:10',
        'Création d\'une nouvelle mission Abidjan-Yamoussoukro-Bouaké'),
       ('resplog', 'Ajout', 'mission', 'MISS_20250403_CD5678EF', '2025-04-02 16:05:38',
        'Création d\'une nouvelle mission Abidjan-San Pedro'),
       ('admin', 'Modification', 'personnel', 'PERS_CHAUF_03', '2025-04-15 11:45:20', 'Mise à jour des coordonnées'),
       ('resplog', 'Ajout', 'entretien', 'ENTR_20250401_VEH_EF9012GH', '2025-04-01 08:10:15',
        'Enregistrement d\'un nouvel entretien'),
       ('ftoure', 'Ajout', 'entretien', 'ENTR_20250422_VEH_AB1234CD', '2025-04-22 09:25:42',
        'Enregistrement d\'un nouvel entretien'),
       ('resplog', 'Ajout', 'attribution_vehicule', 'VEH_AB1234CD', '2025-04-02 13:15:30',
        'Attribution du véhicule au responsable logistique'),
       ('admin', 'Ajout', 'attribution_vehicule', 'VEH_IJ7890KL', '2025-04-20 15:40:25',
        'Attribution du véhicule à l\'administratrice système'),
       ('admin', 'Modification', 'vehicule', 'VEH_GH3456IJ', '2025-04-30 17:20:15',
        'Changement de statut à indisponible suite à problème mécanique');