#!/usr/bin/env python3
"""
Installe vos photographies à la place des illustrations.

Usage :
    cd photos
    python installer-photos.py

Le script recadre au format des cartes, convertit en PNG et copie vers le site
et vers le backend. Voir README.md pour la liste des prises de vue attendues.
"""

import os
import shutil
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOSSIER_PHOTOS = os.path.dirname(os.path.abspath(__file__))

CIBLES = [
    ("location", "Façade d'une maison ou villa à louer"),
    ("vente", "Façade d'une maison à vendre"),
    ("colocation", "Cour de concession, plusieurs portes"),
    ("terrain", "Parcelle nue, bornes visibles"),
    ("salon", "Salon meublé"),
    ("chambre", "Chambre avec lit"),
    ("cuisine", "Cuisine"),
    ("cour", "Cour intérieure"),
]

DESTINATIONS = [
    os.path.join(RACINE, "frontend-web", "public", "illustrations"),
    os.path.join(RACINE, "backend", "src", "main", "resources", "static", "illustrations"),
]

LARGEUR, HAUTEUR = 960, 600  # proportions des cartes du catalogue
EXTENSIONS = (".jpg", ".jpeg", ".png", ".webp", ".bmp")


def verifier_pillow():
    try:
        from PIL import Image  # noqa: F401
        return True
    except ImportError:
        print("La bibliothèque Pillow est nécessaire :\n    pip install Pillow")
        return False


def photos_disponibles():
    """Photos présentes dans ce dossier, hors fichiers du script."""
    fichiers = []
    for f in sorted(os.listdir(DOSSIER_PHOTOS)):
        if f.lower().endswith(EXTENSIONS):
            fichiers.append(f)
    return fichiers


def recadrer(source, destination):
    """Recadre au centre aux proportions des cartes, puis enregistre en PNG."""
    from PIL import Image

    img = Image.open(source).convert("RGB")
    ratio_cible = LARGEUR / HAUTEUR
    ratio_source = img.width / img.height

    if ratio_source > ratio_cible:
        # Photo trop large : on rogne les côtés.
        nouvelle_largeur = int(img.height * ratio_cible)
        gauche = (img.width - nouvelle_largeur) // 2
        img = img.crop((gauche, 0, gauche + nouvelle_largeur, img.height))
    else:
        # Photo trop haute : on rogne le haut et le bas.
        nouvelle_hauteur = int(img.width / ratio_cible)
        haut = (img.height - nouvelle_hauteur) // 2
        img = img.crop((0, haut, img.width, haut + nouvelle_hauteur))

    img = img.resize((LARGEUR, HAUTEUR), Image.LANCZOS)
    img.save(destination, "PNG", optimize=True)


def choisir(nom, description, disponibles):
    """Retrouve la photo correspondante, en demandant à l'utilisateur si besoin."""
    # Correspondance automatique par le nom du fichier.
    for f in disponibles:
        if os.path.splitext(f)[0].lower() == nom:
            return f

    print(f"\n  {nom.upper()} — {description}")
    if not disponibles:
        print("    Aucune photo dans le dossier.")
        return None

    for i, f in enumerate(disponibles, 1):
        print(f"    {i}. {f}")
    print("    0. Conserver l'illustration actuelle")

    while True:
        try:
            reponse = input("    Votre choix : ").strip()
        except EOFError:
            # Entrée non interactive (script lancé sans terminal) : on n'insiste pas.
            print("    Ignoré.")
            return None
        if reponse == "0" or reponse == "":
            return None
        if reponse.isdigit() and 1 <= int(reponse) <= len(disponibles):
            return disponibles[int(reponse) - 1]
        print("    Choix invalide.")


def main():
    if not verifier_pillow():
        return 1

    disponibles = photos_disponibles()
    print("Installation des photographies\n" + "=" * 32)
    print(f"Dossier      : {DOSSIER_PHOTOS}")
    print(f"Photos vues  : {len(disponibles)}")

    if not disponibles:
        print("\nAucune photo trouvée. Déposez vos fichiers dans ce dossier,")
        print("puis relancez le script. Voir README.md pour la liste attendue.")
        return 0

    installees = 0
    for nom, description in CIBLES:
        source = choisir(nom, description, disponibles)
        if source is None:
            continue
        chemin_source = os.path.join(DOSSIER_PHOTOS, source)
        temporaire = os.path.join(DOSSIER_PHOTOS, f".{nom}.tmp.png")
        try:
            recadrer(chemin_source, temporaire)
        except Exception as e:
            print(f"    Échec sur {source} : {e}")
            continue

        for dossier in DESTINATIONS:
            os.makedirs(dossier, exist_ok=True)
            shutil.copy(temporaire, os.path.join(dossier, f"{nom}.png"))
        os.remove(temporaire)
        print(f"    {nom}.png installé depuis {source}")
        installees += 1

    print("\n" + "=" * 32)
    print(f"{installees} visuel(s) remplacé(s) sur {len(CIBLES)}.")
    if installees:
        print("\nPour finir :")
        print("  cd ../frontend-web && npm run build")
        print("  puis reconstruisez l'application Android.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
