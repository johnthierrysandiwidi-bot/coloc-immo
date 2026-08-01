-- #####################################################################
-- SEED 1/2 — RÉFÉRENTIEL OUAGADOUGOU (MySQL)
--
-- Les 55 « Secteur N » ont été retirés : ils n'apportent rien à une
-- démonstration et noient les vrais quartiers dans la liste déroulante.
-- Restent 57 quartiers réels + 24 types de biens.
--
-- PRÉREQUIS : faker désactivé (application-dev.yml → contexts: dev)
-- À exécuter APRÈS le premier démarrage du backend (Liquibase crée les tables).
-- #####################################################################

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE quartier;
TRUNCATE TABLE localite;
TRUNCATE TABLE type_immobilier;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) LOCALITÉ
INSERT INTO localite (id, nom) VALUES (1, 'Ouagadougou');

-- 2) QUARTIERS (57, sans les secteurs numérotés)
INSERT INTO quartier (id, nom, localite_id) VALUES
  (1, 'Ouaga 2000', 1),
  (2, 'Koulouba', 1),
  (3, 'Pissy', 1),
  (4, 'Dassasgho', 1),
  (5, 'Gounghin', 1),
  (6, 'Bilbalogo', 1),
  (7, 'Cissin', 1),
  (8, 'Tanghin', 1),
  (9, 'Tampouy', 1),
  (10, 'Somgandé', 1),
  (11, 'Patte d''Oie', 1),
  (12, 'Zogona', 1),
  (13, 'Karpala', 1),
  (14, 'Rimkieta', 1),
  (15, 'Wayalghin', 1),
  (16, 'Boulmiougou', 1),
  (17, 'Wemtenga', 1),
  (18, 'Silmissin', 1),
  (19, 'Paglayiri', 1),
  (20, 'Balkuy', 1),
  (21, 'Bonheur Ville', 1),
  (22, 'Nagrin', 1),
  (23, 'Nioko I', 1),
  (24, 'Nioko II', 1),
  (25, 'Rayongo', 1),
  (26, 'Saaba', 1),
  (27, 'Bassinko', 1),
  (28, 'Kilwin', 1),
  (29, 'Kamboinsin', 1),
  (30, 'Yagma', 1),
  (31, 'Marcoussis', 1),
  (32, 'Delwende', 1),
  (33, 'Dapoya', 1),
  (34, 'Larlé', 1),
  (35, 'Hamdalaye', 1),
  (36, 'Paspanga', 1),
  (37, 'Ouidi', 1),
  (38, 'Zone du Bois', 1),
  (39, 'Bendogo', 1),
  (40, 'Nongremassom', 1),
  (41, 'Zagtouli', 1),
  (42, 'Sandogo', 1),
  (43, 'Kossodo', 1),
  (44, 'Kouritenga', 1),
  (45, 'Gampéla', 1),
  (46, 'Bissighin', 1),
  (47, 'Belleville', 1),
  (48, 'Zongo', 1),
  (49, 'Samandin', 1),
  (50, 'Toécin', 1),
  (51, 'Polesgo', 1),
  (52, 'Katre-Yaar', 1),
  (53, 'Balkuy Nord', 1),
  (54, 'Balkuy Sud', 1),
  (55, 'Komsilga', 1),
  (56, 'Goughin Nord', 1),
  (57, 'Goughin Sud', 1);

-- 3) TYPES DE BIENS (24)
INSERT INTO type_immobilier (id, nom) VALUES
  (1, 'Maison'),
  (2, 'Villa'),
  (3, 'Appartement'),
  (4, 'Studio'),
  (5, 'Chambre'),
  (6, 'Duplex'),
  (7, 'Triplex'),
  (8, 'Immeuble d''habitation'),
  (9, 'Résidence'),
  (10, 'Maison traditionnelle'),
  (11, 'Maison moderne'),
  (12, 'Parcelle'),
  (13, 'Terrain constructible'),
  (14, 'Terrain agricole'),
  (15, 'Terrain industriel'),
  (16, 'Terrain commercial'),
  (17, 'Terrain viabilisé'),
  (18, 'Terrain non viabilisé'),
  (19, 'Boutique'),
  (20, 'Magasin'),
  (21, 'Local commercial'),
  (22, 'Bureau'),
  (23, 'Espace de coworking'),
  (24, 'Restaurant');

-- Contrôle
SELECT (SELECT COUNT(*) FROM localite)        AS localites,
       (SELECT COUNT(*) FROM quartier)        AS quartiers,
       (SELECT COUNT(*) FROM type_immobilier) AS types_de_biens;
