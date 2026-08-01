package bf.colocation.immo.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bf.colocation.immo.IntegrationTest;
import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.Favori;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.repository.FavoriRepository;
import bf.colocation.immo.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vérifie le cloisonnement des données entre utilisateurs.
 *
 * <p>C'est le test qui manquait : la correction de sécurité (IDOR) a rendu les listes
 * propres à chaque utilisateur, mais aucun test ne le démontrait. On monte ici deux
 * comptes distincts, chacun avec son favori, et on s'assure qu'un utilisateur ne voit
 * ni n'atteint le favori de l'autre — tout en confirmant qu'il accède bien au sien.</p>
 *
 * <p>Le favori sert de cas représentatif : la même règle protège les seize ressources
 * sensibles, car toutes passent par le même service d'autorisation.</p>
 */
@IntegrationTest
@Transactional
class CloisonnementIT {

    private static final String ALICE = "alice-cloisonnement";
    private static final String BOB = "bob-cloisonnement";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriRepository favoriRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMockMvc;

    private User alice;
    private User bob;
    private Favori favoriDAlice;
    private Favori favoriDeBob;

    private User creerUtilisateur(String login) {
        User u = new User();
        u.setLogin(login);
        u.setPassword("00000000000000000000000000000000000000000000000000000000000");
        u.setActivated(true);
        u.setEmail(login + "@colocimmo.test");
        u.setLangKey("fr");
        return userRepository.saveAndFlush(u);
    }

    private Favori creerFavori(User proprietaire) {
        Annonce annonce = AnnonceResourceIT.createEntity(em);
        em.persist(annonce);
        Favori f = new Favori().dateAjout(Instant.now());
        f.setUtilisateur(proprietaire);
        f.setAnnonce(annonce);
        return favoriRepository.saveAndFlush(f);
    }

    @BeforeEach
    void setUp() {
        alice = creerUtilisateur(ALICE);
        bob = creerUtilisateur(BOB);
        favoriDAlice = creerFavori(alice);
        favoriDeBob = creerFavori(bob);
    }

    @Test
    @WithMockUser(username = ALICE)
    void laListeNeRenvoieQueSesPropresFavoris() throws Exception {
        restMockMvc
            .perform(get("/api/favoris?size=100"))
            .andExpect(status().isOk())
            // Le favori d'Alice est présent…
            .andExpect(jsonPath("$[?(@.id == " + favoriDAlice.getId() + ")]").exists())
            // …celui de Bob ne l'est jamais.
            .andExpect(jsonPath("$[?(@.id == " + favoriDeBob.getId() + ")]").doesNotExist());
    }

    @Test
    @WithMockUser(username = ALICE)
    void accederAuFavoriDAutruiEstRefuse() throws Exception {
        // Accès direct par identifiant au favori de Bob : doit être refusé (403).
        restMockMvc.perform(get("/api/favoris/" + favoriDeBob.getId())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ALICE)
    void accederASonPropreFavoriReussit() throws Exception {
        restMockMvc
            .perform(get("/api/favoris/" + favoriDAlice.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriDAlice.getId().intValue()));
    }

    @Test
    void listeSansAuthentificationEstFermee() throws Exception {
        // Sans authentification, la ressource protégée n'est pas accessible.
        restMockMvc.perform(get("/api/favoris/" + favoriDAlice.getId())).andExpect(status().isUnauthorized());
    }
}
