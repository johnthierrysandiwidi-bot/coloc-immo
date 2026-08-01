-- =============================================================================
--  Cohérence des images d'annonces — ColocImmo / Ouagadougou
-- =============================================================================
--  Contexte : les visuels affichés ne correspondaient pas aux biens (plage,
--  désert, villa de bord de mer pour un immeuble à Dassasgho). Ce script
--  remplace les images incohérentes par une illustration correspondant au TYPE
--  de l'annonce, aux couleurs de Ouagadougou.
--
--  À exécuter sur la base `colocationImmo`, backend arrêté de préférence.
--  Exécuter les sections DANS L'ORDRE, et lire le diagnostic avant de modifier.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 1. DIAGNOSTIC — à lancer d'abord, ne modifie rien
-- -----------------------------------------------------------------------------

-- 1.a Quelles URL sont actuellement stockées, et pour quel type d'annonce ?
SELECT a.type            AS type_annonce,
       a.titre,
       i.url             AS url_image,
       CASE
           WHEN i.url IS NULL OR i.url = ''            THEN 'AUCUNE IMAGE'
           WHEN i.url NOT LIKE '%.jpg' AND i.url NOT LIKE '%.jpeg'
            AND i.url NOT LIKE '%.png' AND i.url NOT LIKE '%.webp'
            AND i.url NOT LIKE '%.svg'                 THEN 'URL SUSPECTE (pas une image)'
           WHEN i.url LIKE 'http%' AND i.url NOT LIKE '%/api/files/%'
                                                       THEN 'IMAGE EXTERNE (à vérifier)'
           ELSE 'OK'
       END               AS diagnostic
FROM   annonce a
       LEFT JOIN immobilier m ON m.id = a.immobilier_id
       LEFT JOIN image i      ON i.immobilier_id = m.id
ORDER  BY diagnostic DESC, a.type, a.titre;

-- 1.b Combien d'annonces n'ont aucune image exploitable ?
SELECT a.type, COUNT(*) AS annonces_sans_image
FROM   annonce a
       LEFT JOIN image i ON i.immobilier_id = a.immobilier_id
WHERE  i.id IS NULL
GROUP  BY a.type;


-- -----------------------------------------------------------------------------
-- 2. SAUVEGARDE — obligatoire avant toute modification
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS image_sauvegarde;
CREATE TABLE image_sauvegarde AS SELECT * FROM image;
-- Restauration si besoin :
--   DELETE FROM image;
--   INSERT INTO image SELECT * FROM image_sauvegarde;


-- -----------------------------------------------------------------------------
-- 3. CORRECTION — remplace les URL incohérentes par l'illustration du type
-- -----------------------------------------------------------------------------
--  Les illustrations sont servies par le backend en statique :
--      /illustrations/colocation.png
--      /illustrations/location.png
--      /illustrations/vente.png
--      /illustrations/terrain.png
--  Le format PNG est retenu car il s'affiche sans dépendance supplémentaire,
--  aussi bien dans le navigateur que dans l'application Android (Coil).

-- 3.a Terrains et parcelles (repérés par le titre, quel que soit le type)
UPDATE image i
       JOIN immobilier m ON m.id = i.immobilier_id
       JOIN annonce   a ON a.immobilier_id = m.id
SET    i.url = '/illustrations/terrain.png'
WHERE  (LOWER(a.titre) LIKE '%terrain%' OR LOWER(a.titre) LIKE '%parcelle%')
  AND  (i.url IS NULL OR i.url NOT LIKE '/api/files/images/%');

-- 3.b Colocations
UPDATE image i
       JOIN immobilier m ON m.id = i.immobilier_id
       JOIN annonce   a ON a.immobilier_id = m.id
SET    i.url = '/illustrations/colocation.png'
WHERE  a.type = 'COLOCATION'
  AND  LOWER(a.titre) NOT LIKE '%terrain%'
  AND  (i.url IS NULL OR i.url NOT LIKE '/api/files/images/%');

-- 3.c Ventes
UPDATE image i
       JOIN immobilier m ON m.id = i.immobilier_id
       JOIN annonce   a ON a.immobilier_id = m.id
SET    i.url = '/illustrations/vente.png'
WHERE  a.type = 'VENTE'
  AND  LOWER(a.titre) NOT LIKE '%terrain%'
  AND  LOWER(a.titre) NOT LIKE '%parcelle%'
  AND  (i.url IS NULL OR i.url NOT LIKE '/api/files/images/%');

-- 3.d Locations
UPDATE image i
       JOIN immobilier m ON m.id = i.immobilier_id
       JOIN annonce   a ON a.immobilier_id = m.id
SET    i.url = '/illustrations/location.png'
WHERE  a.type = 'LOCATION'
  AND  LOWER(a.titre) NOT LIKE '%terrain%'
  AND  (i.url IS NULL OR i.url NOT LIKE '/api/files/images/%');

--  Remarque : la condition « NOT LIKE '/api/files/images/%' » préserve les
--  photographies que vous avez réellement téléversées. Seules les URL héritées
--  du jeu de démonstration sont remplacées.


-- -----------------------------------------------------------------------------
-- 4. CONTRÔLE — après correction
-- -----------------------------------------------------------------------------
SELECT a.type, i.url, COUNT(*) AS nombre
FROM   annonce a
       JOIN immobilier m ON m.id = a.immobilier_id
       JOIN image      i ON i.immobilier_id = m.id
GROUP  BY a.type, i.url
ORDER  BY a.type;


-- -----------------------------------------------------------------------------
-- 5. POUR ALLER PLUS LOIN — remplacer par de vraies photographies
-- -----------------------------------------------------------------------------
--  Les illustrations garantissent la cohérence, mais une soutenance gagne à
--  montrer de vraies photographies de Ouagadougou. La démarche :
--    1. photographier ou réunir des clichés libres de droits (villa à cour,
--       chambre de colocation, parcelle bornée, immeuble R+1) ;
--    2. les téléverser depuis l'application, écran « Mes biens » ;
--    3. le backend les enregistre sous /api/files/images/<uuid>, et le présent
--       script ne les écrasera plus jamais grâce à la condition du point 3.
--
--  Conservez la trace de la provenance des photographies : votre encadrant peut
--  légitimement demander qui en détient les droits.
