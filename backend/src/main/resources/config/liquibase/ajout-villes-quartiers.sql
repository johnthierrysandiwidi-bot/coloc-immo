-- =============================================================================
--  Villes et quartiers supplémentaires — ColocImmo
-- =============================================================================
--  Ajoute sept villes du Burkina Faso et leurs quartiers, en complément de
--  Ouagadougou déjà présent. Le script est idempotent : relancé, il n'insère
--  pas de doublon (il vérifie l'existence avant chaque insertion).
--
--  À exécuter sur la base `colocationImmo`, après le seed de Ouagadougou :
--      mysql -u root -p colocationImmo < ajout-villes-quartiers.sql
-- =============================================================================

-- --- Villes (table localite) -------------------------------------------------
INSERT INTO localite (nom, description)
SELECT * FROM (
    SELECT 'Bobo-Dioulasso' AS nom, 'Deuxième ville du pays, région des Hauts-Bassins' AS description UNION ALL
    SELECT 'Koudougou',   'Chef-lieu de la région du Centre-Ouest' UNION ALL
    SELECT 'Banfora',     'Chef-lieu de la région des Cascades' UNION ALL
    SELECT 'Ouahigouya',  'Chef-lieu de la région du Nord' UNION ALL
    SELECT 'Kaya',        'Chef-lieu de la région du Centre-Nord' UNION ALL
    SELECT 'Fada N''Gourma', 'Chef-lieu de la région de l''Est' UNION ALL
    SELECT 'Tenkodogo',   'Chef-lieu de la région du Centre-Est'
) AS v
WHERE NOT EXISTS (SELECT 1 FROM localite l WHERE l.nom = v.nom);


-- --- Quartiers (table quartier) ----------------------------------------------
--  Chaque quartier est rattaché à sa ville par son nom. La sous-requête retrouve
--  l'identifiant de la ville, et le NOT EXISTS empêche les doublons.

-- Procédure implicite : on insère quartier par quartier via un motif uniforme.

-- Bobo-Dioulasso
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Bobo-Dioulasso')
FROM (
    SELECT 'Dioulassoba' AS nom UNION ALL SELECT 'Sarfalao' UNION ALL
    SELECT 'Belleville'  UNION ALL SELECT 'Colsama'  UNION ALL
    SELECT 'Kodéni'      UNION ALL SELECT 'Bindougousso' UNION ALL
    SELECT 'Accart-Ville' UNION ALL SELECT 'Dogona'
) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Bobo-Dioulasso') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Bobo-Dioulasso')
  );

-- Koudougou
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Koudougou')
FROM (
    SELECT 'Dapoya' AS nom UNION ALL SELECT 'Palogo' UNION ALL
    SELECT 'Issouka' UNION ALL SELECT 'Bourkina'
) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Koudougou') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Koudougou')
  );

-- Banfora
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Banfora')
FROM (
    SELECT 'Nafona' AS nom UNION ALL SELECT 'Tangora'
) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Banfora') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Banfora')
  );

-- Ouahigouya
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Ouahigouya')
FROM (
    SELECT 'Kossodo' AS nom UNION ALL SELECT 'Centre-ville'
) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Ouahigouya') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Ouahigouya')
  );

-- Kaya
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Kaya')
FROM (SELECT 'Centre-ville' AS nom) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Kaya') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Kaya')
  );

-- Fada N'Gourma
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Fada N''Gourma')
FROM (SELECT 'Yendabli' AS nom) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Fada N''Gourma') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Fada N''Gourma')
  );

-- Tenkodogo
INSERT INTO quartier (nom, localite_id)
SELECT q.nom, (SELECT id FROM localite WHERE nom = 'Tenkodogo')
FROM (SELECT 'Centre-ville' AS nom) AS q
WHERE (SELECT id FROM localite WHERE nom = 'Tenkodogo') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM quartier x
      WHERE x.nom = q.nom
        AND x.localite_id = (SELECT id FROM localite WHERE nom = 'Tenkodogo')
  );


-- --- Contrôle -----------------------------------------------------------------
SELECT l.nom AS ville, COUNT(q.id) AS nombre_quartiers
FROM localite l
LEFT JOIN quartier q ON q.localite_id = l.id
GROUP BY l.id, l.nom
ORDER BY l.nom;
