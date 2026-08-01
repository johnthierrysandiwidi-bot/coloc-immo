package bf.colocation.immo.service.metier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutAnnonce;
import bf.colocation.immo.domain.enumeration.TypeAnnonce;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * Publication d'annonce (EF-04). Le test le plus important du projet :
 * un démarcheur non validé doit être bloqué.
 */
@ExtendWith(MockitoExtension.class)
class PublicationAnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationDemarcheurService validationDemarcheurService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PublicationAnnonceService service;

    private User auteur;
    private Annonce annonce;

    @BeforeEach
    void setUp() {
        auteur = new User();
        auteur.setId(2L);
        auteur.setLogin("moussa");

        annonce = new Annonce();
        annonce.setId(1000L);
        annonce.setTitre("Chambre à Karpala");
        annonce.setType(TypeAnnonce.LOCATION);
        annonce.setStatut(StatutAnnonce.BROUILLON);
        annonce.setAuteur(auteur);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecterEnTantQue(String login, String... roles) {
        var autorites = java.util.Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(login, "x", autorites));
    }

    @Test
    @DisplayName("Un démarcheur NON validé ne peut pas publier — le verrou du projet (EF-02.1)")
    void demarcheurNonValideEstBloque() {
        connecterEnTantQue("moussa", AuthoritiesConstants.DEMARCHEUR);
        when(annonceRepository.findById(1000L)).thenReturn(Optional.of(annonce));
        when(userRepository.findOneByLogin("moussa")).thenReturn(Optional.of(auteur));
        when(validationDemarcheurService.peutPublier(auteur)).thenReturn(false);

        assertThatThrownBy(() -> service.publier(1000L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("pas encore validés");

        assertThat(annonce.getStatut()).isEqualTo(StatutAnnonce.BROUILLON);
        verify(annonceRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Un démarcheur validé publie, et le moteur d'alertes est déclenché")
    void demarcheurValidePublie() {
        connecterEnTantQue("moussa", AuthoritiesConstants.DEMARCHEUR);
        when(annonceRepository.findById(1000L)).thenReturn(Optional.of(annonce));
        when(userRepository.findOneByLogin("moussa")).thenReturn(Optional.of(auteur));
        when(validationDemarcheurService.peutPublier(auteur)).thenReturn(true);
        when(annonceRepository.save(annonce)).thenReturn(annonce);

        Annonce publiee = service.publier(1000L);

        assertThat(publiee.getStatut()).isEqualTo(StatutAnnonce.PUBLIEE);
        assertThat(publiee.getDatePublication()).isNotNull();
        assertThat(publiee.getDateExpiration()).isNotNull();
        assertThat(publiee.getNombreVues()).isZero();
        verify(eventPublisher).publishEvent(any(PublicationAnnonceService.AnnoncePublieeEvent.class));
    }

    @Test
    @DisplayName("Un propriétaire n'est pas soumis au verrou documentaire")
    void proprietairePublieSansValidation() {
        connecterEnTantQue("moussa", AuthoritiesConstants.PROPRIETAIRE);
        when(annonceRepository.findById(1000L)).thenReturn(Optional.of(annonce));
        when(userRepository.findOneByLogin("moussa")).thenReturn(Optional.of(auteur));
        when(annonceRepository.save(annonce)).thenReturn(annonce);

        assertThat(service.publier(1000L).getStatut()).isEqualTo(StatutAnnonce.PUBLIEE);
        verify(validationDemarcheurService, never()).peutPublier(any());
    }

    @Test
    @DisplayName("On ne publie pas l'annonce d'un autre (anti-IDOR)")
    void publierLAnnonceDunAutreEstRefuse() {
        connecterEnTantQue("intrus", AuthoritiesConstants.PROPRIETAIRE);
        User intrus = new User();
        intrus.setId(99L);
        intrus.setLogin("intrus");

        when(annonceRepository.findById(1000L)).thenReturn(Optional.of(annonce));
        when(userRepository.findOneByLogin("intrus")).thenReturn(Optional.of(intrus));

        assertThatThrownBy(() -> service.publier(1000L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("ne vous appartient pas");
    }

    @Test
    @DisplayName("Une colocation sans détail est refusée (EF-05)")
    void colocationSansDetailRefusee() {
        connecterEnTantQue("moussa", AuthoritiesConstants.PROPRIETAIRE);
        annonce.setType(TypeAnnonce.COLOCATION);
        annonce.setDetailColocation(null);

        when(annonceRepository.findById(1000L)).thenReturn(Optional.of(annonce));
        when(userRepository.findOneByLogin("moussa")).thenReturn(Optional.of(auteur));

        assertThatThrownBy(() -> service.publier(1000L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("détail");
    }
}
