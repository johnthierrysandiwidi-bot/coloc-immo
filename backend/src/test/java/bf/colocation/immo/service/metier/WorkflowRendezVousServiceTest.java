package bf.colocation.immo.service.metier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import bf.colocation.immo.domain.Annonce;
import bf.colocation.immo.domain.RendezVous;
import bf.colocation.immo.domain.User;
import bf.colocation.immo.domain.enumeration.StatutRendezVous;
import bf.colocation.immo.domain.enumeration.TypeNotification;
import bf.colocation.immo.repository.AnnonceRepository;
import bf.colocation.immo.repository.RendezVousRepository;
import bf.colocation.immo.repository.UserRepository;
import bf.colocation.immo.security.AuthoritiesConstants;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/** Cycle de vie du rendez-vous (EF-06). */
@ExtendWith(MockitoExtension.class)
class WorkflowRendezVousServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMetierService notificationService;

    @InjectMocks
    private WorkflowRendezVousService service;

    private User proprietaire;
    private User demandeur;
    private Annonce annonce;
    private RendezVous rdv;

    @BeforeEach
    void setUp() {
        proprietaire = new User();
        proprietaire.setId(1L);
        proprietaire.setLogin("awa");

        demandeur = new User();
        demandeur.setId(2L);
        demandeur.setLogin("ibrahim");

        annonce = new Annonce();
        annonce.setId(1000L);
        annonce.setTitre("Villa à Ouaga 2000");
        annonce.setAuteur(proprietaire);

        rdv = new RendezVous();
        rdv.setId(500L);
        rdv.setAnnonce(annonce);
        rdv.setDemandeur(demandeur);
        rdv.setDateHeure(Instant.now().plus(3, ChronoUnit.DAYS));
        rdv.setStatut(StatutRendezVous.DEMANDE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecter(String login, String role) {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(login, "x", List.of(new SimpleGrantedAuthority(role))));
    }

    @Test
    @DisplayName("Le propriétaire accepte : le demandeur est notifié")
    void accepterNotifieLeDemandeur() {
        connecter("awa", AuthoritiesConstants.PROPRIETAIRE);
        when(rendezVousRepository.findById(500L)).thenReturn(Optional.of(rdv));
        when(userRepository.findOneByLogin("awa")).thenReturn(Optional.of(proprietaire));

        assertThat(service.accepter(500L).getStatut()).isEqualTo(StatutRendezVous.ACCEPTE);
        verify(notificationService).notifier(eq(demandeur), eq(TypeNotification.RDV_ACCEPTE), any(), any(), any());
    }

    @Test
    @DisplayName("Un tiers ne peut pas traiter le rendez-vous d'autrui")
    void unTiersNePeutPasAccepter() {
        connecter("intrus", AuthoritiesConstants.PROPRIETAIRE);
        User intrus = new User();
        intrus.setId(99L);
        intrus.setLogin("intrus");

        when(rendezVousRepository.findById(500L)).thenReturn(Optional.of(rdv));
        when(userRepository.findOneByLogin("intrus")).thenReturn(Optional.of(intrus));

        assertThatThrownBy(() -> service.accepter(500L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("ne vous appartient pas");
    }

    @Test
    @DisplayName("On ne peut pas accepter un rendez-vous déjà annulé")
    void transitionDepuisUnEtatFinalRefusee() {
        connecter("awa", AuthoritiesConstants.PROPRIETAIRE);
        rdv.setStatut(StatutRendezVous.ANNULE);
        when(rendezVousRepository.findById(500L)).thenReturn(Optional.of(rdv));
        when(userRepository.findOneByLogin("awa")).thenReturn(Optional.of(proprietaire));

        assertThatThrownBy(() -> service.accepter(500L))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Transition impossible");
    }

    @Test
    @DisplayName("Reporter dans le passé est refusé")
    void reportDansLePasseRefuse() {
        connecter("awa", AuthoritiesConstants.PROPRIETAIRE);
        when(rendezVousRepository.findById(500L)).thenReturn(Optional.of(rdv));
        when(userRepository.findOneByLogin("awa")).thenReturn(Optional.of(proprietaire));

        Instant hier = Instant.now().minus(1, ChronoUnit.DAYS);
        assertThatThrownBy(() -> service.reporter(500L, hier))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("futur");
    }

    @Test
    @DisplayName("Le demandeur peut annuler, et c'est le propriétaire qui est notifié")
    void leDemandeurAnnuleEtLeProprietaireEstNotifie() {
        connecter("ibrahim", AuthoritiesConstants.UTILISATEUR);
        when(rendezVousRepository.findById(500L)).thenReturn(Optional.of(rdv));
        when(userRepository.findOneByLogin("ibrahim")).thenReturn(Optional.of(demandeur));

        assertThat(service.annuler(500L, "Empêchement").getStatut()).isEqualTo(StatutRendezVous.ANNULE);
        verify(notificationService).notifier(eq(proprietaire), any(), any(), any(), any());
    }
}
