package bf.colocation.immo.service.metier;

import static org.assertj.core.api.Assertions.assertThat;

import bf.colocation.immo.domain.*;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Correspondance alerte / annonce (EF-08.1).
 * Règle : un critère nul ne filtre pas ; un critère renseigné doit être satisfait.
 */
class MoteurAlerteServiceTest {

    private MoteurAlerteService moteur;
    private Annonce annonce;
    private Localite ouaga;
    private Quartier karpala;

    @BeforeEach
    void setUp() {
        moteur = new MoteurAlerteService(null, null, null, null);

        ouaga = new Localite();
        ouaga.setId(1L);
        ouaga.setNom("Ouagadougou");

        karpala = new Quartier();
        karpala.setId(10L);
        karpala.setNom("Karpala");

        Immobilier bien = new Immobilier();
        bien.setId(100L);
        bien.setLocalite(ouaga);
        bien.setQuartier(karpala);
        bien.setNombreChambres(3);
        bien.setSurface(120.0);
        bien.setMeuble(true);

        annonce = new Annonce();
        annonce.setId(1000L);
        annonce.setType(TypeAnnonce.COLOCATION);
        annonce.setPrix(75000.0);
        annonce.setImmobilier(bien);
    }

    @Test
    @DisplayName("Une alerte sans aucun critère accepte toute annonce")
    void alerteVideAccepteTout() {
        assertThat(moteur.correspond(new Alerte(), annonce)).isTrue();
    }

    @Test
    @DisplayName("Le type d'annonce doit correspondre")
    void filtreParTypeAnnonce() {
        Alerte alerte = new Alerte();
        alerte.setTypeAnnonce(TypeAnnonce.COLOCATION);
        assertThat(moteur.correspond(alerte, annonce)).isTrue();

        alerte.setTypeAnnonce(TypeAnnonce.VENTE);
        assertThat(moteur.correspond(alerte, annonce)).isFalse();
    }

    @Test
    @DisplayName("Le prix doit tomber dans la fourchette, bornes incluses")
    void filtreParFourchetteDePrix() {
        Alerte alerte = new Alerte();
        alerte.setPrixMin(50000.0);
        alerte.setPrixMax(100000.0);
        assertThat(moteur.correspond(alerte, annonce)).isTrue();

        alerte.setPrixMin(75000.0); // borne basse exactement
        assertThat(moteur.correspond(alerte, annonce)).isTrue();

        alerte.setPrixMin(80000.0); // trop cher pour le budget
        assertThat(moteur.correspond(alerte, annonce)).isFalse();
    }

    @Test
    @DisplayName("La localité et le quartier doivent correspondre")
    void filtreParGeographie() {
        Alerte alerte = new Alerte();
        alerte.setLocalite(ouaga);
        alerte.setQuartier(karpala);
        assertThat(moteur.correspond(alerte, annonce)).isTrue();

        Quartier autre = new Quartier();
        autre.setId(99L);
        alerte.setQuartier(autre);
        assertThat(moteur.correspond(alerte, annonce)).isFalse();
    }

    @Test
    @DisplayName("Le nombre de chambres du bien doit atteindre le minimum demandé")
    void filtreParNombreDeChambres() {
        Alerte alerte = new Alerte();
        alerte.setNombreChambresMin(3);
        assertThat(moteur.correspond(alerte, annonce)).isTrue();

        alerte.setNombreChambresMin(4);
        assertThat(moteur.correspond(alerte, annonce)).isFalse();
    }

    @Test
    @DisplayName("Une annonce sans bien ne correspond jamais")
    void annonceSansBienNeCorrespondPas() {
        annonce.setImmobilier(null);
        assertThat(moteur.correspond(new Alerte(), annonce)).isFalse();
    }
}
