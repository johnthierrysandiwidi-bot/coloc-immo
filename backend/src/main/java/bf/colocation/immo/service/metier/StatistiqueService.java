package bf.colocation.immo.service.metier;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.StatutBien;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.domain.enumeration.StatutValidation;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.repository.*;
import bf.colocation.immo.security.SecurityUtils;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tableaux de bord (EF-11). Un endpoint par rôle, chacun ne renvoyant que ce que le rôle doit voir.
 */
@Service
@Transactional(readOnly = true)
public class StatistiqueService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StatistiqueService.class);

    private final UserRepository userRepository;
    private final AnnonceRepository annonceRepository;
    private final ImmobilierRepository immobilierRepository;
    private final RendezVousRepository rendezVousRepository;
    private final DocumentRepository documentRepository;
    private final FavoriRepository favoriRepository;
    private final AlerteRepository alerteRepository;
    private final VueAnnonceRepository vueAnnonceRepository;
    private final ProfilDemarcheurRepository profilDemarcheurRepository;

    public StatistiqueService(
        UserRepository userRepository,
        AnnonceRepository annonceRepository,
        ImmobilierRepository immobilierRepository,
        RendezVousRepository rendezVousRepository,
        DocumentRepository documentRepository,
        FavoriRepository favoriRepository,
        AlerteRepository alerteRepository,
        VueAnnonceRepository vueAnnonceRepository,
        ProfilDemarcheurRepository profilDemarcheurRepository
    ) {
        this.userRepository = userRepository;
        this.annonceRepository = annonceRepository;
        this.immobilierRepository = immobilierRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.documentRepository = documentRepository;
        this.favoriRepository = favoriRepository;
        this.alerteRepository = alerteRepository;
        this.vueAnnonceRepository = vueAnnonceRepository;
        this.profilDemarcheurRepository = profilDemarcheurRepository;
    }

    /** Administrateur : vue globale. */
    public Map<String, Object> pourAdministrateur() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("utilisateurs", userRepository.count());
        stats.put("biens", immobilierRepository.count());
        stats.put("annonces", annonceRepository.count());
        stats.put("rendezVous", rendezVousRepository.count());
        stats.put(
            "documentsEnAttente",
            documentRepository.findAll().stream().filter(d -> d.getStatut() == StatutValidation.EN_ATTENTE).count()
        );
        stats.put(
            "demarcheursEnAttente",
            profilDemarcheurRepository.findAll().stream().filter(p -> p.getStatutValidation() == StatutValidation.EN_ATTENTE).count()
        );

        Map<String, Long> parType = new LinkedHashMap<>();
        for (TypeAnnonce t : TypeAnnonce.values()) {
            parType.put(t.name(), annonceRepository.findAll().stream().filter(a -> a.getType() == t).count());
        }
        stats.put("annoncesParType", parType);
        return stats;
    }

    /** Propriétaire / démarcheur : son activité, avec revenus estimés. */
    public Map<String, Object> pourBailleur() {
        try {
            return calculerStatsBailleur();
        } catch (Exception e) {
            // Un enregistrement inattendu ne doit pas transformer le tableau de bord
            // en écran d'erreur. En cas d'échec d'un calcul, on renvoie une structure
            // complète à zéro : la page s'affiche, simplement sans chiffres.
            LOG.warn("Calcul des statistiques bailleur impossible, valeurs par défaut renvoyées.", e);
            return statsBailleurParDefaut();
        }
    }

    /** Structure complète attendue par l'écran, tous indicateurs à zéro. */
    private Map<String, Object> statsBailleurParDefaut() {
        Map<String, Object> stats = new LinkedHashMap<>();
        for (String cle : new String[] { "biens", "annonces", "vuesCumulees", "demandesVisite",
            "biensLoues", "biensVendus", "revenusEstimes", "rendezVous" }) {
            stats.put(cle, 0);
        }
        stats.put("tauxOccupation", 0d);
        stats.put("perfParType", new LinkedHashMap<>());
        stats.put("topAnnonces", new java.util.ArrayList<>());
        stats.put("mensuelles", new java.util.ArrayList<>());
        return stats;
    }

    private Map<String, Object> calculerStatsBailleur() {
        User moi = utilisateurCourant();
        List<Annonce> mesAnnonces = annonceRepository
            .findAll()
            .stream()
            .filter(a -> a.getAuteur() != null && a.getAuteur().getId().equals(moi.getId()))
            .toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put(
            "biens",
            immobilierRepository.findAll().stream().filter(b -> b.getProprietaire() != null && b.getProprietaire().getId().equals(moi.getId())).count()
        );
        stats.put("annonces", mesAnnonces.size());
        stats.put("annoncesPubliees", mesAnnonces.stream().filter(a -> a.getStatut() == StatutAnnonce.PUBLIEE).count());
        stats.put("vuesCumulees", mesAnnonces.stream().mapToInt(a -> a.getNombreVues() == null ? 0 : a.getNombreVues()).sum());

        // Revenus estimés : somme des loyers des annonces de location/colocation publiées
        double revenus = mesAnnonces
            .stream()
            .filter(a -> a.getStatut() == StatutAnnonce.PUBLIEE)
            .filter(a -> a.getType() == TypeAnnonce.LOCATION || a.getType() == TypeAnnonce.COLOCATION)
            .mapToDouble(a -> a.getPrix() == null ? 0 : a.getPrix())
            .sum();
        stats.put("revenusEstimes", revenus);

        List<Long> idsAnnonces = mesAnnonces.stream().map(Annonce::getId).toList();
        stats.put(
            "rendezVous",
            rendezVousRepository.findAll().stream().filter(r -> r.getAnnonce() != null && idsAnnonces.contains(r.getAnnonce().getId())).count()
        );
        stats.put(
            "rendezVousEnAttente",
            rendezVousRepository
                .findAll()
                .stream()
                .filter(r -> r.getAnnonce() != null && idsAnnonces.contains(r.getAnnonce().getId()))
                .filter(r -> r.getStatut() == StatutRendezVous.DEMANDE)
                .count()
        );

        // Démarcheur : son statut de validation, l'information la plus importante pour lui
        profilDemarcheurRepository
            .findByUtilisateurId(moi.getId())
            .ifPresent(p -> stats.put("statutValidation", p.getStatutValidation().name()));

        // Une annonce « active » est publiée et non encore expirée.
        Instant maintenant = Instant.now();
        stats.put(
            "annoncesActives",
            mesAnnonces
                .stream()
                .filter(a -> a.getStatut() == StatutAnnonce.PUBLIEE)
                .filter(a -> a.getDateExpiration() == null || a.getDateExpiration().isAfter(maintenant))
                .count()
        );
        stats.put("annoncesExpirees", mesAnnonces.stream().filter(a -> a.getStatut() == StatutAnnonce.EXPIREE).count());

        // Suivi des pièces justificatives
        List<bf.colocation.immo.domain.Document> mesDocuments = documentRepository
            .findAll()
            .stream()
            .filter(d -> d.getDemarcheur() != null && d.getDemarcheur().getId().equals(moi.getId()))
            .toList();
        stats.put("documentsValides", mesDocuments.stream().filter(d -> d.getStatut() == StatutValidation.VALIDE).count());
        stats.put("documentsEnAttente", mesDocuments.stream().filter(d -> d.getStatut() == StatutValidation.EN_ATTENTE).count());
        stats.put("documentsRefuses", mesDocuments.stream().filter(d -> d.getStatut() == StatutValidation.REFUSE).count());

        stats.put("mensuelles", publicationsParMois(mesAnnonces));

        // ------------------------------------------------------------------
        //  Clés attendues par l'écran « Statistiques ».
        //
        //  Elles manquaient : la page lisait « demandesVisite », « biensLoues »,
        //  « tauxOccupation », « perfParType » et « topAnnonces », qui n'étaient
        //  jamais renseignées. Tous ces indicateurs affichaient donc zéro alors
        //  que les données existaient — d'où l'incohérence entre les 2331 vues
        //  cumulées et le message « Pas encore de données de vues ».
        // ------------------------------------------------------------------

        List<bf.colocation.immo.domain.RendezVous> rdvSurMesAnnonces = rendezVousRepository
            .findAll()
            .stream()
            .filter(r -> r.getAnnonce() != null && idsAnnonces.contains(r.getAnnonce().getId()))
            .toList();

        // Nom attendu côté écran, à côté de « rendezVous » conservé pour compatibilité.
        stats.put("demandesVisite", rdvSurMesAnnonces.size());

        List<bf.colocation.immo.domain.Immobilier> mesBiens = immobilierRepository
            .findAll()
            .stream()
            .filter(b ->
                (b.getProprietaire() != null && b.getProprietaire().getId().equals(moi.getId())) ||
                (b.getDemarcheur() != null && b.getDemarcheur().getId().equals(moi.getId()))
            )
            .toList();

        long biensLoues = mesBiens.stream().filter(b -> b.getStatut() == StatutBien.LOUE).count();
        // Un bien vendu n'a pas de statut dédié : on le repère par son annonce de
        // vente clôturée, seule trace de la transaction dans le modèle actuel.
        long biensVendus = mesAnnonces
            .stream()
            .filter(a -> a.getType() == TypeAnnonce.VENTE && a.getStatut() == StatutAnnonce.CLOTUREE)
            .count();

        stats.put("biensLoues", biensLoues);
        stats.put("biensVendus", biensVendus);
        stats.put(
            "tauxOccupation",
            mesBiens.isEmpty() ? 0d : Math.round(((biensLoues + biensVendus) * 1000d) / mesBiens.size()) / 10d
        );

        // Performance par type : annonces, vues et demandes de visite regroupées.
        Map<String, Object> perfParType = new LinkedHashMap<>();
        for (TypeAnnonce t : TypeAnnonce.values()) {
            List<Annonce> duType = mesAnnonces.stream().filter(a -> a.getType() == t).toList();
            List<Long> idsDuType = duType.stream().map(Annonce::getId).toList();
            Map<String, Object> ligne = new LinkedHashMap<>();
            ligne.put("annonces", duType.size());
            ligne.put("vues", duType.stream().mapToInt(a -> a.getNombreVues() == null ? 0 : a.getNombreVues()).sum());
            ligne.put(
                "demandes",
                rdvSurMesAnnonces.stream().filter(r -> idsDuType.contains(r.getAnnonce().getId())).count()
            );
            perfParType.put(t.name(), ligne);
        }
        stats.put("perfParType", perfParType);

        // Annonces les plus consultées, classées par nombre de vues.
        stats.put(
            "topAnnonces",
            mesAnnonces
                .stream()
                .sorted((a, b) -> Integer.compare(
                    b.getNombreVues() == null ? 0 : b.getNombreVues(),
                    a.getNombreVues() == null ? 0 : a.getNombreVues()
                ))
                .limit(8)
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("titre", a.getTitre());
                    m.put("vues", a.getNombreVues() == null ? 0 : a.getNombreVues());
                    m.put("demandes", rdvSurMesAnnonces.stream().filter(r -> r.getAnnonce().getId().equals(a.getId())).count());
                    return m;
                })
                .toList()
        );

        return stats;
    }

    /** Publications des 6 derniers mois, pour le graphique du tableau de bord. */
    private List<Map<String, Object>> publicationsParMois(List<Annonce> annonces) {
        List<Map<String, Object>> series = new ArrayList<>();
        YearMonth courant = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth mois = courant.minusMonths(i);
            long compte = annonces
                .stream()
                .filter(a -> a.getDatePublication() != null)
                .filter(a -> YearMonth.from(a.getDatePublication().atZone(ZoneId.systemDefault())).equals(mois))
                .count();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("mois", mois.toString());
            point.put("publications", compte);
            series.add(point);
        }
        return series;
    }

    /** Utilisateur : favoris, alertes, rendez-vous, annonces consultées récemment. */
    public Map<String, Object> pourUtilisateur() {
        User moi = utilisateurCourant();
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put(
            "favoris",
            favoriRepository.findAll().stream().filter(f -> f.getUtilisateur() != null && f.getUtilisateur().getId().equals(moi.getId())).count()
        );
        stats.put(
            "alertes",
            alerteRepository.findAll().stream().filter(a -> a.getTitulaire() != null && a.getTitulaire().getId().equals(moi.getId())).count()
        );
        stats.put(
            "rendezVous",
            rendezVousRepository.findAll().stream().filter(r -> r.getDemandeur() != null && r.getDemandeur().getId().equals(moi.getId())).count()
        );

        // Annonces consultées récemment (EF-11, tableau de bord Utilisateur)
        List<Map<String, Object>> recentes = vueAnnonceRepository
            .findAll()
            .stream()
            .filter(v -> v.getUtilisateur() != null && v.getUtilisateur().getId().equals(moi.getId()))
            .filter(v -> v.getAnnonce() != null)
            .sorted((a, b) -> b.getDateVue().compareTo(a.getDateVue()))
            .map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getAnnonce().getId());
                m.put("titre", v.getAnnonce().getTitre());
                m.put("prix", v.getAnnonce().getPrix());
                m.put("dateVue", v.getDateVue());
                return m;
            })
            .distinct()
            .limit(6)
            .toList();
        stats.put("consulteesRecemment", recentes);

        return stats;
    }

    private User utilisateurCourant() {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non identifié"));
    }
}
