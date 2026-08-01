# Remplacer les illustrations par de vraies photographies

Les visuels livrés sont des **illustrations**, pas des photographies. Elles
garantissent la cohérence — une colocation montre une cour partagée, un terrain
montre une parcelle — mais votre encadrant verra la différence.

Ce dossier vous permet de basculer sur de vraies photos **sans toucher au code**.

---

## Le principe

Le site et l'application mobile cherchent huit fichiers, toujours aux mêmes noms.
Remplacez-les par vos photographies et tout suit automatiquement : catalogue,
fiche d'annonce, galerie, application Android.

| Fichier | Ce qu'il doit montrer | Où il apparaît |
|---|---|---|
| `location.png` | Façade d'une maison ou villa à louer | Carte et 1re photo des annonces LOCATION |
| `vente.png` | Façade d'une maison à vendre | Carte et 1re photo des annonces VENTE |
| `colocation.png` | Cour d'une concession, plusieurs portes | Carte et 1re photo des COLOCATION |
| `terrain.png` | Parcelle nue, bornes visibles | Terrains et parcelles |
| `salon.png` | Salon meublé | 2e photo (location et vente) |
| `chambre.png` | Chambre avec lit | 3e photo (location, vente, colocation) |
| `cuisine.png` | Cuisine | 4e photo (location, colocation) |
| `cour.png` | Cour intérieure, linge, canaris | 4e photo (vente, colocation) |

---

## Ce qu'il faut photographier

Une demi-journée à Ouagadougou suffit largement. Huit photos, c'est le minimum ;
si vous en avez davantage, gardez les meilleures.

**Les quatre extérieurs.** Une villa ou maison de plain-pied derrière son mur de
clôture pour `location`. Une maison plus cossue, R+1 si possible, pour `vente`.
Une cour de concession avec plusieurs portes alignées pour `colocation` — c'est
la réalité de la colocation ici, ne cherchez pas un appartement partagé. Une
parcelle non bâtie, si possible avec ses bornes, pour `terrain`.

**Les quatre intérieurs.** Un salon, une chambre, une cuisine, une cour intérieure.
Ils n'ont pas besoin d'appartenir aux maisons précédentes : ce sont des visuels
d'illustration, pas des photos d'un bien précis.

**Conseils de prise de vue.** Photographiez en format paysage, jamais en portrait :
les cartes sont horizontales et une photo verticale sera recadrée sévèrement.
Préférez la lumière du matin ou de fin d'après-midi, le soleil de midi écrase les
volumes. Reculez-vous pour montrer l'ensemble plutôt qu'un détail. Évitez les
visages identifiables et les plaques d'immatriculation.

**Droits.** Si vous ne prenez pas les photos vous-même, notez d'où elles viennent
et sous quelle licence. Un jury peut légitimement poser la question, et ne pas
savoir répondre fait mauvais effet.

---

## Installation

Placez vos photos dans ce dossier, sous n'importe quel nom, puis :

```
cd photos
python installer-photos.py
```

Le script vous demande, pour chaque emplacement, quelle photo lui associer. Il se
charge du recadrage au bon format, de la conversion et de la copie vers le site
comme vers le backend.

Si vous avez déjà nommé vos fichiers correctement (`location.jpg`, `salon.png`…),
le script les reconnaît et ne pose aucune question.

**Rien d'autre à faire.** Reconstruisez le site (`npm run build`) et
l'application, les photos apparaissent.

---

## Et les annonces existantes ?

Vos annonces de démonstration portent des adresses d'images héritées du jeu de
test — un chien, des chaussures. Le code les ignore déjà : seules les photos
téléversées depuis l'application sont affichées, tout le reste bascule sur les
visuels ci-dessus. Vous n'avez donc rien à corriger en base.

Pour donner à une annonce **sa propre** photo, téléversez-la depuis l'écran
« Mes biens » de l'application. Elle prendra alors le pas sur l'illustration.
